/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FTP;

import ServerApp.FTP.FTPManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Linda
 */
public class ftpTest {
    private FTPManager manager = new FTPManager();
    
    public ftpTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    public void Upload(){
        String filepath = "E:/64277_816878888362056_5899858130298802750_n.jpg";
        String remotename = "UnitTest1.jpg";
        boolean succeed = manager.uploadFile(filepath, remotename);
        assertEquals(succeed, true);
    }
}
