/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import ServerApp.ServerMain;
import Shared.Data.Advice;
import Shared.Data.DataRequest;
import Shared.Data.IDataRequest;
import Shared.Data.INewsItem;
import Shared.Data.ISortedData;
import Shared.Data.NewsItem;
import Shared.Data.Situation;
import Shared.Data.SortedData;
import Shared.NetworkException;
import Shared.Tag;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;

/**
 *
 * @author Kargathia
 */
public class SortedDatabaseManagerTest {

    private static SortedDatabaseManager myDB;

    private SortedData sortedData;
    private DataRequest request;

    public SortedDatabaseManagerTest() {
        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.POLICE);
        sortedData = new SortedData(100, "title", "description",
                "location", "source", 1, 2, 3, tags);
        request = new DataRequest("title", "description", "location", "source", 100, tags);
    }

    @BeforeClass
    public static void setUpClass() throws NetworkException {
        if (ServerMain.sortedDatabaseManager == null) {
            ServerMain.startDatabases(null);
        }
        myDB = ServerMain.sortedDatabaseManager;
        myDB.resetDatabase();
    }

    @AfterClass
    public static void tearDownClass() throws NetworkException {
        myDB.resetDatabase();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testInsertToSortedData() throws NetworkException {
        try{
            myDB.insertToSortedData(sortedData);
        } catch (NetworkException ex) {
            Logger.getLogger(SortedDatabaseManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Next line should be an error (Duplicate entry)");
        try{
        myDB.insertToSortedData(sortedData);
        } catch (NetworkException ex) {
            Logger.getLogger(SortedDatabaseManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testGetFromSortedData() throws NetworkException {
        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.POLICE);
        List<ISortedData> sortedDataList = myDB.getFromSortedData(tags);
        assertNotNull("No data found in getFromSortedData", sortedDataList);

        for (ISortedData data : sortedDataList) {
            // checks not null
            assertNotNull("title was null (getFromSortedData)", data.getTitle());
            assertNotNull("description was null (getFromSortedData)", data.getDescription());
            assertNotNull("location was null (getFromSortedData)", data.getLocation());
            assertNotNull("source was null (getFromSortedData)", data.getSource());
            assertNotNull("tasks was null (getFromSortedData)", data.getTasks());

            assertFalse("id was -1 (getFromSortedData)", data.getId() == -1);

            // checks tags
            boolean hasTag = false;
            Iterator it = data.getTags().iterator();
            while (it.hasNext()) {
                if (it.next().equals(Tag.POLICE)) {
                    hasTag = true;
                    break;
                }
            }
            assertTrue("Item retrieved in getFromSortedData did not have the requested tag", hasTag);
        }
    }

    @Test
    public void testInsertDataRequest() {
        try{
            myDB.insertDataRequest(request);
            myDB.insertDataRequest(request);
        } catch (NetworkException ex) {
            Logger.getLogger(SortedDatabaseManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testGetUpdateRequests() throws NetworkException {
        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.POLICE);
        List<IDataRequest> dataRequests = myDB.getUpdateRequests(tags);
        assertNotNull("output getUpdateRequests was null", dataRequests);
        for (IDataRequest req : dataRequests) {
            assertNotNull("title was null (getUpdateRequests)", req.getTitle());
            assertNotNull("description was null (getUpdateRequests)", req.getDescription());
            assertNotNull("location was null (getUpdateRequests)", req.getLocation());
            assertNotNull("source was null (getUpdateRequests)", req.getSource());

            assertFalse("id was -1 (getUpdateRequests)", req.getId() == -1);
        }
    }

    @Test
    public void testNewsItems() throws NetworkException {
        try{
            myDB.getNewsItems(0, 0);
            myDB.getNewsItems(0, -1);
            fail();
        }catch(IllegalArgumentException ex){
            Logger.getLogger(SortedDatabaseManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<INewsItem> items = myDB.getNewsItems(0, 20);
        assertEquals("wrong number of items", 18, items.size());
        assertEquals("getNewsItemCount incorrect", items.size(), myDB.getNewsItemCount());

        items = myDB.getNewsItems(0, 2);
        assertEquals("wrong number of limited items", 2, items.size());

        items = myDB.getNewsItems(4, 2);
        assertEquals("wrong number of limited items with startindex", 2, items.size());

        Set<Situation> situations = myDB.getSituations();
        assertEquals("wrong number of situations", 9, situations.size());

        HashMap<Integer, Situation> sitMap = myDB.getSituationsMap();
        Situation sit = sitMap.get(2);
        assertTrue("situation has wrong number of advices",
                sit.getAdvices().size() == 3);
        for (Advice ad : sit.getAdvices()) {
            String expectedDesc = "???????";
            switch (ad.getID()) {
                case 1:
                    expectedDesc = "Ga niet bij het rampgebied kijken.";
                    break;
                case 2:
                    expectedDesc = "Volg de aanwijzingen van de hulpverleners.";
                    break;
                case 5:
                    expectedDesc = "Sluit ramen en deuren.";
                    break;
                default:
                    fail("unrecognised advice ID");
            }
            assertEquals("wrong description", expectedDesc, ad.getDescription());
        }

        // test insert
        INewsItem expectedItem, insertedItem;
        situations = new HashSet<>();
        situations.add(sitMap.get(4));
        situations.add(sitMap.get(6));
        String expectedTitle = "title",
                expectedDesc = "desc",
                expectedLoc = "loc",
                expectedSource = "source";
        int expectedVictims = 9001;
        int expectedID = 19;
        long expectedTime = System.currentTimeMillis();

        expectedItem = new NewsItem(expectedID, expectedTitle, expectedDesc,
                expectedLoc, expectedSource, (HashSet) situations,
                expectedVictims, new Date(expectedTime));

        // tests for return type insertion
        insertedItem = myDB.insertNewsItem(
                new NewsItem(expectedTitle, expectedDesc, expectedLoc,
                        expectedSource, (HashSet) situations, expectedVictims));

        this.testNewsItem("insertion return", expectedItem, insertedItem);

        // runs same tests on newsItem gotten from getNewsItems
        insertedItem = null;
        List<INewsItem> list = myDB.getNewsItems(0, 10);
        for (INewsItem item : list) {
            if (item.getId() == expectedID) {
                insertedItem = item;
                break;
            }
        }
        this.testNewsItem("retrieved item after insertion",
                expectedItem, insertedItem);

        // test update
        ((NewsItem) insertedItem).addSituation(sitMap.get(2));
        ((NewsItem) expectedItem).addSituation(sitMap.get(2));
        try{
            myDB.updateNewsItem(insertedItem);
        }catch (NetworkException ex) {
            Logger.getLogger(SortedDatabaseManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        // and reruns tests
        insertedItem = null;
        for (INewsItem item : myDB.getNewsItems(0, 10)) {
            if (item.getId() == expectedID) {
                insertedItem = item;
                break;
            }
        }
        this.testNewsItem("retrieved item after update",
                expectedItem, insertedItem);
    }

    /**
     * fully tests NewsItems
     *
     * @param expected
     * @param tested
     */
    private void testNewsItem(String testDesc, INewsItem expected, INewsItem tested) {
        assertNotNull("tested item was null (" + testDesc + ")", tested);

        String expectedTitle = expected.getTitle(),
                expectedDesc = expected.getDescription(),
                expectedLoc = expected.getLocation(),
                expectedSource = expected.getSource();
        int expectedVictims = expected.getVictims();
        int expectedID = expected.getId();
        long expectedTime = expected.getDate().getTime();
        Set<Situation> expectedSituations = expected.getSituations();

        assertEquals("wrong ID (" + testDesc + ")",
                expectedID, tested.getId());
        assertEquals("wrong title (" + testDesc + ")",
                expectedTitle, tested.getTitle());
        assertEquals("wrong desc (" + testDesc + ")",
                expectedDesc, tested.getDescription());
        assertEquals("wrong loc (" + testDesc + ")",
                expectedLoc, tested.getLocation());
        assertEquals("wrong victims (" + testDesc + ")",
                expectedVictims, tested.getVictims());
        assertEquals("wrong number of situations ("
                + testDesc + ")", expectedSituations.size(),
                tested.getSituations().size());
        long dateMillis = tested.getDate().getTime();
        assertTrue("date was more than an hour off (" + testDesc + ")",
                (expectedTime + 3600000 > dateMillis)
                || (expectedTime - 3600000 < dateMillis));
    }
    
    @Test
    public void testPictures() throws NetworkException {
        List<INewsItem> items = myDB.getNewsItems(0, 5);
        String picture1 = "plaatje1.jpg";
        String picture2 = "plaatje2.jpg";
        
        try{
            myDB.insertPicture(items.get(0), picture1);
            myDB.insertPicture(items.get(0), picture2);
            myDB.insertPicture(items.get(1), picture1);
        } catch (NetworkException ex) {
            Logger.getLogger(SortedDatabaseManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        List<String> pictures = myDB.getNewsItemByID(items.get(0).getId()).getPictures();
        boolean result = false;
        if(pictures.contains(picture1)) {
            result = true;
        }
        assertTrue("picture1 was not added to newsitem", result);
        
        result = false;
        if(pictures.contains(picture2)) {
            result = true;
        }
        assertTrue("picture2 was not added to newsitem", result);
        
        result = false;        
        pictures = myDB.getNewsItemByID(items.get(1).getId()).getPictures();
        if(pictures.contains(picture1)) {
            result = true;
        }
        assertTrue("picture1 was added to newsitem", result);
    }

}
