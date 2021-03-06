/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Connection;

import Shared.Connection.ChangeRequest;
import Shared.Connection.SerializeUtils;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ConnectionHandler implements Runnable {

    // The host:port combination to listen on
    private final InetAddress hostAddress;
    private final int port;

    // The channel on which we'll accept connections
    private ServerSocketChannel serverChannel;

    // The selector we'll be monitoring
    private final Selector selector;

    // The buffer into which we'll read data when it's available
    private final int bufferCapacity = 10485760;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(bufferCapacity);
    private final ByteBuffer overflowBuffer = ByteBuffer.allocate(bufferCapacity);

    // Used for processing data
    private final HashSet<ConnectionWorker> workers;
    private final int numWorkers = 5;

    // A list of PendingChange instances
    private final List<ChangeRequest> pendingChanges = new LinkedList<>();

    // Maps a SocketChannel to a list of ByteBuffer instances
    private final Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<>();

    /**
     *
     * @param hostAddress
     * @param port
     * @throws IOException
     */
    public ConnectionHandler(InetAddress hostAddress, int port) throws IOException {
        this.hostAddress = hostAddress;
        this.port = port;
        this.selector = this.initSelector();

        // A fixed number of worker threads are started to handle transactions
        this.workers = new HashSet<>();
        for (int i = 0; i < numWorkers; i++) {
            ConnectionWorker worker = new ConnectionWorker();
            workers.add(worker);
            Thread t = new Thread(worker);
            t.setDaemon(true);
            t.start();
        }
    }

    /**
     *
     * @param socket
     * @param data
     */
    public synchronized void send(SocketChannel socket, byte[] data) {
        synchronized (this.pendingChanges) {
            // Indicate we want the interest ops set changed
            this.pendingChanges.add(new ChangeRequest(
                    socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

            // And queue the data we want written
            synchronized (this.pendingData) {
                List<ByteBuffer> queue = (List) this.pendingData.get(socket);
                if (queue == null) {
                    queue = new ArrayList<>();
                    this.pendingData.put(socket, queue);
                }
                // inserts a preceding int with array size
//                System.out.println("transaction size: " + data.length);
                byte[] length = ByteBuffer.allocate(4).putInt(data.length).array();
                byte[] dataWithLength = SerializeUtils.concat(length, data);
                queue.add(ByteBuffer.wrap(dataWithLength));
            }
        }

        // Finally, wake up our selecting thread so it can make the required changes
        this.selector.wakeup();
    }

    public synchronized void close() {
        try {
            this.selector.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * The only thread that can make changes to a channel's keys, as they are
     * not thread-safe. It checks channels for activity (either from send() or
     * data coming from clients), and acts on it.
     */
    @Override
    public void run() {
        boolean isRunning = true;
        while (isRunning) {
            try {
                // Process any pending changes
                synchronized (this.pendingChanges) {
                    Iterator changes = this.pendingChanges.iterator();
                    while (changes.hasNext()) {
                        ChangeRequest change = (ChangeRequest) changes.next();
                        switch (change.type) {
                            case ChangeRequest.CHANGEOPS:
                                SelectionKey key = change.socket.keyFor(this.selector);
                                key.interestOps(change.ops);
                        }
                    }
                    this.pendingChanges.clear();
                }

                // Wait for an event one of the registered channels
                this.selector.select();

                // Iterate over the set of keys for which events are available
                Iterator selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    // Check what event is available and deal with it
                    if (key.isAcceptable()) {
                        this.accept(key);
                    } else if (key.isReadable()) {
                        this.read(key);
                    } else if (key.isWritable()) {
                        this.write(key);
                    }
                }
            } catch (ClosedSelectorException ex) {
                // Thrown by Servlet shutting down
                try {
                    isRunning = false;
                    this.serverChannel.close();
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                }
            } catch (NullPointerException ex) {
                System.out.println("Null pointer thrown in main loop ConnectionHandler");
                ex.printStackTrace();
                isRunning = false;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Accepts a connection to a client. Only called by run() thread.
     *
     * @param key
     * @throws IOException
     */
    private void accept(SelectionKey key) throws IOException {
        // For an accept to be pending the channel must be a server socket channel.
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when there's data waiting to be read
        socketChannel.register(this.selector, SelectionKey.OP_READ);
    }

    /**
     * Reads data from channel belonging to given key. Only called from run()
     * thread.
     *
     * @param key
     * @throws IOException
     */
    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        this.overflowBuffer.flip();
        this.readBuffer.clear();
        this.readBuffer.put(overflowBuffer);
        this.overflowBuffer.clear();
        if(this.readBuffer.position() > 0){
            System.out.println("overflowBuffer found: " + readBuffer.position());
        }

        // Attempt to read off the channel
        int numRead;
        try {
            numRead = socketChannel.read(this.readBuffer);
        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel
            // the selection key and close the channel.
            key.cancel();
            socketChannel.close();
            return;
        }

        if (numRead == -1) {
            // Remote entity shut the socket down cleanly. Do the
            // same from our end and cancel the channel.
            key.channel().close();
            key.cancel();
            return;
        }

        // while is repeated for every Transaction currently queued.
        // A portion of the byte array the size of a transaction is read into a new array
        // That array is handed off to the channel's responsehandler
        readBuffer.flip();
        while (readBuffer.limit() - readBuffer.position() > 4) {
            int size = readBuffer.getInt();
            System.out.println("transaction size: " + size + " - numRead: " + numRead);
            if (readBuffer.limit() - readBuffer.position() >= size) {
                byte[] data = new byte[size];
                readBuffer.get(data);

                // Hand the data off to our worker threads
                // Look up the handler for this channel
                ConnectionWorker.processData(this, socketChannel, data, size);
            } else {
                readBuffer.position(readBuffer.position() - 4);
                overflowBuffer.put(readBuffer.slice());
                readBuffer.position(readBuffer.limit());
            }
        }
    }

    /**
     * Writes data in queue to given key's channel. Only called from run() as
     * keys are not thread-safe.
     *
     * @param key
     * @throws IOException
     */
    private void write(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        synchronized (this.pendingData) {
            List queue = (List) this.pendingData.get(socketChannel);

            // Write until there's not more data ...
            while (!queue.isEmpty()) {
                ByteBuffer buf = (ByteBuffer) queue.get(0);
                socketChannel.write(buf);
                if (buf.remaining() > 0) {
                    // ... or the socket's buffer fills up
                    break;
                }
                queue.remove(0);
            }

            if (queue.isEmpty()) {
                // We wrote away all data, so we're no longer interested
                // in writing on this socket. Switch back to waiting for
                // data.
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    /**
     * Starts the SocketSelector, which is the control center of all
     * keys/channels/sockets handled by this server.
     *
     * @return @throws IOException
     */
    private Selector initSelector() throws IOException {
        // Create a new selector
        Selector socketSelector = SelectorProvider.provider().openSelector();

        // Create a new non-blocking server socket channel
        this.serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // Bind the server socket to the specified address and port
        InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.port);
        serverChannel.socket().bind(isa);

        // Register the server socket channel, indicating an interest in
        // accepting new connections
        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

        return socketSelector;
    }

}
