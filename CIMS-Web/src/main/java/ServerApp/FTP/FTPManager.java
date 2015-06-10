/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.FTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
                System.out.println("Log in FTP failed");
                return false;
            }
        } catch (SocketException ex) {
            System.out.println("Log in FTP failed, SocketException error");
            Logger.getLogger(FTPManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            System.out.println("Log in FTP failed, IOException error");
            Logger.getLogger(FTPManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    private void closeLogIn(){
        try{
        ftpClient.logout();
        ftpClient.disconnect();  
        }catch (IOException ex) {
            System.out.println("Logout FTP failed");
            Logger.getLogger(FTPManager.class.getName()).log(Level.SEVERE, null, ex);
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
                    closeLogIn();
                    return true;
                } else {
                    closeLogIn();
                    return false;
                }
            } else {
                System.out.println("Connection with ftps server failed");
                closeLogIn();
                return false;
            }
        } catch (FileNotFoundException ex) {
            closeLogIn();
            Logger.getLogger(FTPManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            closeLogIn();
            Logger.getLogger(FTPManager.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    //return filepath

    public File downloadFile(String filename) {
        try {
            boolean succeed = logInToFTP();
            if (succeed) {
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                
                File file = new File(filename);
                FileOutputStream fos = new FileOutputStream(file);
                boolean done = ftpClient.retrieveFile(filename, fos);
                fos.close();
                
                if(done){
                    closeLogIn();
                    return file;
                }
                else{
                    closeLogIn();
                    return null;
                }           
            } else {
                closeLogIn();
                return null;
            }
        } catch (IOException ex) {
            closeLogIn();
            Logger.getLogger(FTPManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
