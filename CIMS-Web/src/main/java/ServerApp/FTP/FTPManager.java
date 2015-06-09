/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.FTP;


/**
 *
 * @author Linda
 */
public class FTPManager {
    private static final int BUFFER_SIZE = 4096;
    
    String ftpUrl;
        String host = "www.myserver.com";
        String user = "tom";
        String pass = "secret";

    public FTPManager() {
        this.ftpUrl = "ftp://145.85.4.24";
        
    }
}
