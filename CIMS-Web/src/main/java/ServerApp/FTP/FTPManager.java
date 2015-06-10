/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.FTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPSClient;

/**
 *
 * @author Linda
 */
public class FTPManager {

    private String user = "i204267";
    private String pass = "lve201090";
    private boolean login = false;

    private FTPSClient ftpClient = null;

    public FTPManager() {
        try {
            ftpClient = new FTPSClient();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(FTPManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean logInToFTP() {
        try {
            // pass directory path on server to connect  
            this.ftpClient.connect("athena.fhict.nl");

            // pass username and password, returned true if authentication is  
            // successful  
            this.login = this.ftpClient.login(this.user, this.pass);

            if (login) {
                return true;
            } else {
                System.out.println("Connection FTPS fail...");
                return false;
            }
        } catch (SocketException ex) {
            Logger.getLogger(FTPManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(FTPManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean uploadFile(String filePath, String newName) {
        try {
            boolean succeed = logInToFTP();
            if (succeed) {
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();

                FileInputStream fis = new FileInputStream(filePath);
                boolean done = ftpClient.storeFile(newName, fis);
                if (done) {
                    return true;
                } else {
                    return false;
                }
            } else {
                System.out.println("Connection with ftps server failed");
                return false;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FTPManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(FTPManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
