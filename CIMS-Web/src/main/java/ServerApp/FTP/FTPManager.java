/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.FTP;

import java.io.IOException;
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
    
        private String ftpUrl = "ftp://%s:%s@%s;type=i";
        private String host = "145.85.4.24";
        private String user = "i204267";
        private String pass = "lve201090";

    public FTPManager() {
        ftpUrl = String.format(ftpUrl, user, pass, host);
        try{
            URL url = new URL(ftpUrl);
            System.out.println(ftpUrl);
            URLConnection conn = url.openConnection();
        }
        catch(IOException ex){
            Logger.getLogger(FTPManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
