/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import ServerApp.ServerMain;
import Shared.Data.IData;
import Shared.Data.UnsortedData;
import Shared.NetworkException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author Kargathia
 */
public class UnsortedDatabaseManagerTest {

    private static UnsortedDatabaseManager myDB;

    private IData unsortedData;
    private List<IData> unsortedBatch;

    public UnsortedDatabaseManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws NetworkException {
        if(ServerMain.sortedDatabaseManager == null){
            ServerMain.startDatabases(null);
        }   
        myDB = ServerMain.unsortedDatabaseManager;
        myDB.resetDatabase();
    }

    @AfterClass
    public static void tearDownClass() throws NetworkException {
        myDB.resetDatabase();
    }

    @Before
    public void setUp() throws NetworkException {
        myDB.resetDatabase();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testInsertToUnsortedData() throws NetworkException {
        unsortedData = new UnsortedData("title", "desc", "loc", "source");
        assertNotNull("Failed to insert unsorted data", myDB.insertToUnsortedData(unsortedData));       
        assertNotNull("Failed to insert second unsorted data", myDB.insertToUnsortedData(unsortedData));
    }

    @Test
    public void testGetUnsortedData() throws NetworkException{
        unsortedData = new UnsortedData("title", "desc", "loc", "source");
        for(int i = 1; i < 60; i++){
            myDB.insertToUnsortedData(unsortedData);
        }
        assertEquals("Unsorted not limited to 50", 50, myDB.getFromUnsortedData().size());
    }

    @Test
    public void testGetAndResetFromUnsortedData() throws NetworkException {
        // gets first batch
        unsortedBatch = myDB.getFromUnsortedData();
        assertNotNull("unsorted batch (getFromUnsortedData) was null", unsortedBatch);

        // checks whether item status was updated
        List<IData> unsortedBatch2 = myDB.getFromUnsortedData();
        assertTrue("second unsorted batch was not empty (getfromUnsortedData)",
                unsortedBatch2.isEmpty());

        int size = unsortedBatch.size();
        
        try{
            myDB.resetUnsortedData(unsortedBatch);
        }catch (NetworkException ex) {
            Logger.getLogger(UnsortedDatabaseManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        long now = System.currentTimeMillis();
        while(System.currentTimeMillis() < now + 500){}
        
        // gets new (resetted batch)
        List<IData> copy = myDB.getFromUnsortedData();
        assertTrue("size was not equal (getAndResetUnsorted)", copy.size() == size);

        int pos = 0;
        for (IData data : copy) {
            assertTrue("IData ID at pos " + pos + " was not equal",
                    unsortedBatch.get(pos).getId() == data.getId());
            assertTrue("IData description at pos " + pos + " was not equal",
                    unsortedBatch.get(pos).getDescription().equals(data.getDescription()));
            pos++;
        }
    }

}
