/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import Shared.Connection.SerializeUtils;
import Shared.Connection.Transaction.ClientBoundTransaction;
import Shared.Connection.Transaction.ConnCommand;
import Shared.Data.IData;
import Shared.Data.IDataRequest;
import Shared.Data.INewsItem;
import Shared.Data.ISortedData;
import Shared.Tag;
import Shared.Tasks.ITask;
import Shared.Users.UserRole;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Kargathia
 */
public class PushHandler {

    // used for tasks
    private final HashSet<Socket> chiefConnections;
    // all HQ users, including the chief - can be toggled mid-session
    private final HashSet<Socket> unsortedSubscribers;
    // all HQ users, including the chief
    private final HashSet<Socket> hqSubscribers;
    // key: username (ServiceUser)
    private final HashMap<String, Set<Socket>> usernameSubscribers;
    // service users subscribing by tag - used for sorted data + requests
    private final HashMap<Tag, Set<Socket>> tagSubscribers;

    private final Set<Socket> faultySockets;

    /**
     * Initiates collections
     */
    public PushHandler() {
        this.faultySockets = Collections.newSetFromMap(new ConcurrentHashMap<>());
        chiefConnections = new HashSet<>();
        unsortedSubscribers = new HashSet<>();
        hqSubscribers = new HashSet<>();
        usernameSubscribers = new HashMap<>();
        tagSubscribers = new HashMap<>();
        for (Tag tag : Tag.values()) {
            tagSubscribers.put(tag, new HashSet<>());
        }
    }

    /**
     * Checks if there are unsorted subscribers to whom can be pushed.
     *
     * @param exclude is disregarded to avoid pushing data back to a client
     * mid-shutdown.
     * @return
     */
    public boolean canPushUnsorted(SocketChannel exclude) {
        synchronized (unsortedSubscribers) {
            if (exclude == null) {
                return !unsortedSubscribers.isEmpty();
            } else {
                return (unsortedSubscribers.size() > 1
                        || unsortedSubscribers.iterator().next() != exclude.socket());
            }
        }
    }

    /**
     * Checks whether socket is open before bothering it.
     *
     * @param socket
     * @return
     */
    private boolean trySend(Socket socket, byte[] data) {
        if (socket.isClosed()
                || !socket.isConnected()
                || socket.isInputShutdown()
                || socket.isOutputShutdown()) {
            return false;
        }
        ServerMain.connectionHandler.send(socket.getChannel(), data);
        return true;
    }

    /**
     *
     * @param role can't be null
     * @param tag can be null if role != SERVICE
     * @param username can be null if role != SERVICE
     * @param channel
     * @return
     */
    public boolean subscribe(UserRole role, Tag tag, String username, SocketChannel channel) {
        if (role == null) {
            System.out.println("invalid registration - null UserRole");
            return false;
        }
        if (role == UserRole.SERVICE
                && (tag == null || username == null || username.isEmpty())) {
            System.out.println("invalid registration - null tag or username");
            return false;
        }

        if (role == UserRole.CHIEF) {
            boolean newsResult, chiefSubsResult;
            synchronized (chiefConnections) {
                chiefSubsResult = chiefConnections.add(channel.socket());
            }
            synchronized (hqSubscribers) {
                newsResult = hqSubscribers.add(channel.socket());
            }
            return (newsResult && chiefSubsResult);
        } else if (role == UserRole.HQ) {
            synchronized (hqSubscribers) {
                return hqSubscribers.add(channel.socket());
            }
        } else if (role == UserRole.SERVICE) {
            boolean usernameResult, tagResult;
            synchronized (usernameSubscribers) {
                // tasks and sent data
                usernameSubscribers.putIfAbsent(username, new HashSet<>());
                usernameResult = usernameSubscribers.get(username).add(channel.socket());
            }
            synchronized (tagSubscribers) {
                // requests, sorted data
                tagResult = tagSubscribers.get(tag).add(channel.socket());
            }
            return (usernameResult && tagResult);
        }
        // shouldn't be hit
        return false;
    }

    private void cleanFaultySockets() {
        synchronized (this.faultySockets) {
            for (Socket socket : this.faultySockets) {
                this.unsubscribe(socket);
            }
            this.faultySockets.clear();
        }
    }

    /**
     * Removes given sockets from all lists of subscribers. <br>
     * Not specified because it's also used on lapsed connections where client
     * didn't provide his role/tag/username
     *
     * @param socket
     */
    public void unsubscribe(Socket socket) {
        System.out.println("unsubscribing socket"); // debugging
        synchronized (chiefConnections) {
            chiefConnections.remove(socket);
        }
        synchronized (unsortedSubscribers) {
            unsortedSubscribers.remove(socket);
        }
        synchronized (hqSubscribers) {
            hqSubscribers.remove(socket);
        }
        synchronized (usernameSubscribers) {
            for (String username : usernameSubscribers.keySet()) {
                usernameSubscribers.get(username).remove(socket);
            }
        }
        synchronized (tagSubscribers) {
            for (Tag tag : tagSubscribers.keySet()) {
                tagSubscribers.get(tag).remove(socket);
            }
        }
    }

    /**
     *
     * @param channel
     * @return
     */
    public boolean subscribeUnsorted(SocketChannel channel) {
        synchronized (unsortedSubscribers) {
            return unsortedSubscribers.add(channel.socket());
        }
    }

    /**
     *
     * @param channel
     * @return
     */
    public boolean unsubscribeUnsorted(SocketChannel channel) {
        synchronized (unsortedSubscribers) {
            return unsortedSubscribers.remove(channel.socket());
        }
    }

    /**
     *
     * @param task
     */
    public void pushTaskToChief(ITask task) {
        if (task == null) {
            return;
        }
        ClientBoundTransaction transaction
                = new ClientBoundTransaction(ConnCommand.TASKS_PUSH, task);
        byte[] output = SerializeUtils.serialize(transaction);
        // to chief
        synchronized (chiefConnections) {
            for (Socket socket : chiefConnections) {
                System.out.println("pushing task to chief"); // debugging
                if (!this.trySend(socket, output)) {
                    this.faultySockets.add(socket);
                }
            }
        }
        this.cleanFaultySockets();
    }

    /**
     *
     * @param task
     */
    public void pushTaskToService(ITask task){
        if(task == null){
            return;
        }
        ClientBoundTransaction transaction
                = new ClientBoundTransaction(ConnCommand.TASKS_PUSH, task);
        byte[] output = SerializeUtils.serialize(transaction);

        // to relevant serviceuser
        if (task.getExecutor() != null) {
            String userName = task.getExecutor().getUsername();
            synchronized (usernameSubscribers) {
                if (usernameSubscribers.containsKey(userName)) {
                    for (Socket socket : usernameSubscribers.get(userName)) {
                        System.out.println("pushing task"); // debugging
                        if (!this.trySend(socket, output)) {
                            this.faultySockets.add(socket);
                        }
                    }
                }
            }
        }
        this.cleanFaultySockets();
    }

    /**
     *
     * @param data
     */
    public boolean push(List<IData> data, Socket disregard) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        ClientBoundTransaction transaction
                = new ClientBoundTransaction(ConnCommand.UNSORTED_GET, data);
        byte[] output = SerializeUtils.serialize(transaction);
        // Pushes it towards the first valid subscriber it finds in an iterator
        // HashSet do not guarantee order, so this should happen semi-randomly
        synchronized (unsortedSubscribers) {
            if (!unsortedSubscribers.isEmpty()) {
                for (Socket socket : unsortedSubscribers) {
                    if (socket != disregard) {
                        if (!this.trySend(socket, output)) {
                            this.faultySockets.add(socket);
                        } else {
                            System.out.println("pushing list of unsorted data"); // debugging
                            return true;
                        }
                    }
                }
                this.cleanFaultySockets();
            }
        }
        return false;
    }
    
    /**
     *
     * @param data
     */
    public boolean pushSentData(IData data, Socket disregard) {
        if (data == null) {
            return false;
        }
        List<IData> sentData = new ArrayList<>();
        sentData.add(data);
        
        boolean result = true;
        
        ClientBoundTransaction transaction
                = new ClientBoundTransaction(ConnCommand.UNSORTED_GET_SOURCE, sentData);
        byte[] output = SerializeUtils.serialize(transaction);
        // Pushes it towards the first valid subscriber it finds in an iterator
        // HashSet do not guarantee order, so this should happen semi-randomly
        synchronized (usernameSubscribers) {
            if (!usernameSubscribers.isEmpty()) {
                for (Socket socket : usernameSubscribers.get(data.getSource())) {
                    if (socket != disregard) {
                        if (!this.trySend(socket, output)) {
                            this.faultySockets.add(socket);
                            result = false;
                        } else {
                            System.out.println("pushing list of unsorted data"); // debugging
                        }
                    }
                }
                this.cleanFaultySockets();
            }
        }
        return result;
    }
    
    /**
     *
     * @param data
     */
    public boolean pushUnsortedUpdate(IData data, Socket disregard) {
        if (data == null) {
            return false;
        }
        List<IData> unsortedUpdate = new ArrayList<>();
        unsortedUpdate.add(data);
        
        boolean result = true;
        
        ClientBoundTransaction transaction
                = new ClientBoundTransaction(ConnCommand.UNSORTED_GET_UPDATE, unsortedUpdate);
        byte[] output = SerializeUtils.serialize(transaction);
        // Pushes it towards the first valid subscriber it finds in an iterator
        // HashSet do not guarantee order, so this should happen semi-randomly
        synchronized (hqSubscribers) {
            if (!hqSubscribers.isEmpty()) {
                for (Socket socket : hqSubscribers) {
                    if (socket != disregard) {
                        if (!this.trySend(socket, output)) {
                            this.faultySockets.add(socket);
                            result = false;
                        } else {
                            System.out.println("pushing list of unsorted data"); // debugging
                        }
                    }
                }
                this.cleanFaultySockets();
            }
        }
        return result;
    }

    /**
     *
     * @param data
     */
    public void push(ISortedData data) {
        if (data == null) {
            return;
        }
        ClientBoundTransaction transaction
                = new ClientBoundTransaction(ConnCommand.SORTED_GET,
                        Arrays.asList(new ISortedData[]{data}));
        byte[] output = SerializeUtils.serialize(transaction);
        // sends to chief
        synchronized (chiefConnections) {
            for (Socket socket : chiefConnections) {
                System.out.println("pushing sorted data to chief"); // debugging
                if (!this.trySend(socket, output)) {
                    this.faultySockets.add(socket);
                }
            }
        }
        // sends to relevant serviceusers
        synchronized (tagSubscribers) {
            for (Tag target : data.getTags()) {
                for (Socket socket : tagSubscribers.get(target)) {
                    System.out.println("pushing sorted data"); // debugging
                    if (!this.trySend(socket, output)) {
                        this.faultySockets.add(socket);
                    }
                }
            }
        }
        this.cleanFaultySockets();
    }

    /**
     *
     * @param request
     */
    public void push(IDataRequest request) {
        if (request == null) {
            return;
        }
        List<IDataRequest> requests = new ArrayList<>();
        requests.add(request);
        ClientBoundTransaction transaction
                = new ClientBoundTransaction(ConnCommand.UPDATE_REQUEST_GET, requests);
        byte[] output = SerializeUtils.serialize(transaction);
        // sends to relevant serviceusers
        synchronized (tagSubscribers) {
            for (Tag target : request.getTags()) {
                for (Socket socket : tagSubscribers.get(target)) {
                    System.out.println("pushing datarequest");
                    if (!this.trySend(socket, output)) {
                        this.faultySockets.add(socket);
                    }
                }
            }
        }
        this.cleanFaultySockets();
    }

    public void push(INewsItem item) {
        if (item == null) {
            return;
        }
        ClientBoundTransaction transaction
                = new ClientBoundTransaction(ConnCommand.NEWSITEMS_GET,
                        Arrays.asList(new INewsItem[]{item}));
        byte[] output = SerializeUtils.serialize(transaction);
        // sends newsitems
        synchronized (hqSubscribers) {
            for (Socket socket : hqSubscribers) {
                System.out.println("pushing newsitems"); // debugging
                if (!this.trySend(socket, output)) {
                    this.faultySockets.add(socket);
                }
            }
        }
        this.cleanFaultySockets();
    }

}
