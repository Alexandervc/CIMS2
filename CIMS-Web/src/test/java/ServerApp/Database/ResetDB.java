/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import ServerApp.ServerMain;
import Shared.NetworkException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Kargathia
 */
public class ResetDB {

    public ResetDB() {
    }

    @BeforeClass
    public static void setUp() throws NetworkException {
        if(ServerMain.sortedDatabaseManager == null){
            ServerMain.startDatabases(null);
        }
        assertTrue(ServerMain.sortedDatabaseManager.resetDatabase());
        assertTrue(ServerMain.tasksDatabaseManager.resetDatabase());
        assertTrue(ServerMain.unsortedDatabaseManager.resetDatabase());
    }

    @After
    public void tearDown() {
    }

    @Test
    public void printReset(){
        System.out.println("resetting database");
    }

}
