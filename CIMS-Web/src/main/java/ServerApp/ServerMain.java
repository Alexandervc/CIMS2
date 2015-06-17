/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import ServerApp.Connection.ConnectionWorker;
import ServerApp.Connection.ConnectionHandler;
import ServerApp.Database.DummyDatabaseManager;
import ServerApp.Database.SortedDatabaseManager;
import ServerApp.Database.TasksDatabaseManager;
import ServerApp.Database.UnsortedDatabaseManager;
import ServerApp.Database.UnsortedDatabaseManager;
import ServerApp.FTP.FTPManager;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author Kargathia
 */
public class ServerMain {
    
    public static final String SERVER_ADDRESS = "127.0.0.1";
    public static final int SERVER_PORT = 9090;

    public static SortedDatabaseManager sortedDatabaseManager = null;
    public static UnsortedDatabaseManager unsortedDatabaseManager = null;
    public static TasksDatabaseManager tasksDatabaseManager = null;
    public static PlanExecutorHandler planExecutorHandler = null;
    public static ConnectionHandler connectionHandler = null;
    public static PushHandler pushHandler = null;
    public static FTPManager ftpManager = null;

    public static DummyDatabaseManager dummyDatabaseManager = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        startDatabases(null);
        startConnection(false);
    }

    public static void startDatabases(String baseDir) {
        if(baseDir == null){
            baseDir = "";
        } else if (!baseDir.endsWith("\\")){
            baseDir+= "\\";
        }
        System.out.println("baseDir: " + baseDir);
        sortedDatabaseManager = new SortedDatabaseManager(baseDir + "sorteddatabase.properties");
        unsortedDatabaseManager = new UnsortedDatabaseManager(baseDir + "unsorteddatabase.properties");
        tasksDatabaseManager = new TasksDatabaseManager(baseDir + "taskdatabase.properties");

        planExecutorHandler = new PlanExecutorHandler();        
        dummyDatabaseManager = new DummyDatabaseManager();
        
        ftpManager = new FTPManager();
    }

    public static void stopDatabases(){
        if(sortedDatabaseManager != null){
            sortedDatabaseManager.shutDownConnection();
        }
        if(unsortedDatabaseManager != null){
            unsortedDatabaseManager.shutDownConnection();
        }
        if(tasksDatabaseManager != null){
            tasksDatabaseManager.shutDownConnection();
        }
    }

    public static void startConnection(boolean isDaemon) {
        try {
            connectionHandler = new ConnectionHandler(
                    InetAddress.getByName(SERVER_ADDRESS), SERVER_PORT);
            Thread t = new Thread(connectionHandler);
            t.setDaemon(isDaemon);
            t.start();
            pushHandler = new PushHandler();
            System.out.println("connection started");
        } catch (IOException e) {
            System.out.println("failed to start connection");
            e.printStackTrace();
        }
    }
}

//private static final int portnumber = 1099;
//    private static final String bindingname = "Administration";
//    
//    private Registry registry;
//    private IAdministration admin;
//    
//    public ServerController(String ipAdress) {
//        try {
//            System.setProperty("java.rmi.server.hostname", ipAdress);
//
//            Properties props = new Properties();
//            props.setProperty("ServerIp", ipAdress);
//            props.setProperty("ServerPort", String.valueOf(portnumber));
//            try (FileOutputStream out = new FileOutputStream("network.props")) {
//                props.store(out, null);
//            }
//            
//            this.admin = Administration.getInstance();
//            
//            this.registry = LocateRegistry.createRegistry(portnumber);
//            if(this.registry != null){
//                this.registry.rebind(bindingname, admin);
//            }
//            System.out.println("running...");
//            this.setUpSocketServer();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }
//    
//    private static String getIpAddress() {
//        String ipAddress = null;        
//        try {
//            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//                NetworkInterface intf = en.nextElement();
//                if(intf.getName().contains("eth1")) {
//                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//                        InetAddress addr = enumIpAddr.nextElement();
//                        if(ipAddress == null) {
//                            ipAddress = addr.getHostAddress();
//                        }
//                    }
//                }
//            }
//        } catch (SocketException ex) {
//            System.out.println("Server: Cannot retrieve network interface list");
//            System.out.println("Server: UnknownHostException: " + ex.getMessage());
//        }
//        return ipAddress;
//    }
//    
//    private void setUpSocketServer() throws IOException {
//        ServerSocket serverSocket = null;
//        boolean listening = true;
//
//        try {
//            serverSocket = new ServerSocket(4444);
//            System.out.println("Socket Server is listening: Sound supported");
//            System.out.println("Waiting for incoming clients...");
//        } catch (IOException e) {
//            System.out.println("Could not create Server Socket: Sound not supported");
//        }
//        
//        while (listening) {
//            Thread t = new Thread(new SSSrunnable(serverSocket.accept()));
//            t.start();
//        }
//        serverSocket.close();
//    }
//    
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
//        System.out.println("SERVER AIRHOCKEY");
//        String ipAdress = getIpAddress();
//        if(ipAdress == null || !ipAdress.contains(".")) {
//            ipAdress = "127.0.0.1";
//        }
//        System.out.println(ipAdress);
//        new ServerController(ipAdress);
//    }
