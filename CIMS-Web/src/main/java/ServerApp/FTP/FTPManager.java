/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.FTP;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;

/**
 *
 * @author Linda
 */
public class FTPManager {

    private static final int BUFFER_SIZE = 4096;

    private String ftpUrl = null;
    private String host = null;
    private String user = "i204267";
    private String pass = "lve201090";
    private URL url = null;
    private String uploadPath = null;

    private FTPClient client;
    private FileInputStream fis = null;

    public FTPManager() {
        FTPClient ftpClient = new FTPClient();

        try {
            // pass directory path on server to connect  
            ftpClient.connect("145.85.4.24");

            // pass username and password, returned true if authentication is  
            // successful  
            boolean login = ftpClient.login("i204267", "lve201090");

            if (login) {
                System.out.println("Connection established...");
                System.out.println("Status: " + ftpClient.getStatus());
                // logout the user, returned true if logout successfully  
                boolean logout = ftpClient.logout();
                if (logout) {
                    System.out.println("Connection close...");
                }
            } else {
                System.out.println("Connection fail...");
            }

        } catch (SocketException e) {
            e.printStackTrace();
        } catch(IOException ex){
            ex.printStackTrace();
        }finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    
    }
}
