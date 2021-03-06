/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import ServerApp.ServerMain;
import Shared.Data.SortedData;
import Shared.NetworkException;
import Shared.Tag;
import Shared.Tasks.IPlan;
import Shared.Tasks.IStep;
import Shared.Tasks.ITask;
import Shared.Tasks.Plan;
import Shared.Tasks.Step;
import Shared.Tasks.Task;
import Shared.Tasks.TaskStatus;
import Shared.Users.HQChief;
import Shared.Users.IServiceUser;
import Shared.Users.IUser;
import Shared.Users.ServiceUser;
import Shared.Users.Citizen;
import Shared.Users.ICitizen;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
public class TasksDatabaseManagerTest {

    private static TasksDatabaseManager myDB;

    public TasksDatabaseManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws NetworkException {
        if (ServerMain.sortedDatabaseManager == null) {
            ServerMain.startDatabases(null);
        }
        myDB = ServerMain.tasksDatabaseManager;
        ServerMain.sortedDatabaseManager.resetDatabase();
    }

    @AfterClass
    public static void tearDownClass() throws NetworkException {
        myDB.resetDatabase();
        ServerMain.sortedDatabaseManager.resetDatabase();
    }

    @Before
    public void setUp() throws NetworkException {
        myDB.resetDatabase();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testTasks() throws NetworkException {
        // insert new task
        Task task = new Task(-1, "title", "desc", TaskStatus.UNASSIGNED, null, Tag.POLICE, null);
        assertNotNull("Unable to insert barebones task", myDB.insertNewTask(task));

        IServiceUser executor = (IServiceUser) myDB.getUser("firefighter01");
        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.POLICE);
        SortedData data = new SortedData(100, "title", "description",
                "location", "source", 1, 2, 3, tags);

        try {
            ServerMain.sortedDatabaseManager.insertToSortedData(data);
        } catch (NetworkException ex) {
            Logger.getLogger(TasksDatabaseManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        task = new Task(-1, "title", "desc", TaskStatus.UNASSIGNED, data,
                Tag.FIREDEPARTMENT, executor);
        task = (Task) myDB.insertNewTask(task);
        assertNotNull("Unable to insert full task", task);

        // get task
        Task task2 = (Task) myDB.getTask(task.getId());
        assertNull("declineReason was not null",
                task2.getDeclineReason());
        assertTrue("different Description",
                task.getDescription().equals(task2.getDescription()));
        assertTrue("differentExecutorTag",
                task.getTargetExecutor() == task2.getTargetExecutor());
        assertTrue("different executor",
                task.getExecutor().getUsername().equals(task2.getExecutor().getUsername()));
        assertTrue("different sortedData ID",
                task.getSortedData().getId() == task2.getSortedData().getId());
        assertTrue("different sortedData title",
                task.getSortedData().getTitle().equals(task2.getSortedData().getTitle()));

        // get tasks // TODO 
        List<ITask> tasks = myDB.getTasks(executor.getUsername(), new HashSet<>());
        assertEquals("wrong number of tasks", 2, tasks.size());

        // tests if tasks are correct
        for (ITask taskItem : tasks) {
            try {
                String expectedTitle = "?????";
                String expectedDescription = "??????";
                String expectedReason = "??????";
                Tag expectedTag = null;
                int expectedDataID = -9999;
                TaskStatus expectedStatus = null;
                boolean expectedHasData = true;

                if (taskItem.getId() == 3) {
                    expectedTitle = "Zet ladder neer";
                    expectedDescription = "Zet de ladder tegen de boom.";
                    expectedReason = null;
                    expectedTag = Tag.FIREDEPARTMENT;
                    expectedHasData = true;
                    expectedDataID = 3;
                    expectedStatus = TaskStatus.SENT;
                } else if (taskItem.getId() == myDB.getMaxID("USERTASK")) {
                    expectedTitle = task2.getTitle();
                    expectedDescription = task2.getDescription();
                    expectedReason = task2.getDeclineReason();
                    expectedTag = task2.getTargetExecutor();
                    expectedHasData = true;
                    expectedDataID = task2.getSortedData().getId();
                    expectedStatus = task2.getStatus();
                } else {
                    fail("no recognized task ID");
                }

                assertEquals("different title " + taskItem.getId(),
                        expectedTitle, taskItem.getTitle());
                assertEquals("different description " + taskItem.getId(),
                        expectedDescription, taskItem.getDescription());
                assertEquals("different decline reason " + taskItem.getId(),
                        expectedReason, taskItem.getDeclineReason());
                assertEquals("different tag " + taskItem.getId(),
                        expectedTag, taskItem.getTargetExecutor());
                if (expectedHasData) {
                    assertEquals("different Data" + taskItem.getId(),
                            expectedDataID, taskItem.getSortedData().getId());
                } else {
                    assertNull("has data when he shouldn't " + taskItem.getId(),
                            taskItem.getSortedData());
                }
                assertEquals("different status " + taskItem.getId(),
                        expectedStatus, taskItem.getStatus());
            } catch (SQLException ex) {
                Logger.getLogger(TasksDatabaseManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // gets only active tasks
        tasks = myDB.getTasks(executor.getUsername(), new HashSet<>(
                Arrays.asList(TaskStatus.SENT, TaskStatus.INPROCESS)));
        for (ITask taskItem : tasks) {
            assertTrue("wrong tag in active tasks",
                    taskItem.getStatus() == TaskStatus.SENT
                    || taskItem.getStatus() == TaskStatus.INPROCESS);
        }

        // get sorted data tasks
        tasks = myDB.getSortedDataTasks(data);
        assertEquals("wrong number of tasks", 1, tasks.size());
        task = (Task) tasks.get(0);
        assertEquals("wrong data ID", data.getId(), task.getSortedData().getId());
        assertEquals("wrong title", task2.getTitle(), task.getTitle());
        assertEquals("wrong description", task2.getDescription(), task.getDescription());
        assertEquals("wrong decline reason", task2.getDeclineReason(), task.getDeclineReason());
        assertEquals("wrong tag", task2.getTargetExecutor(), task.getTargetExecutor());
        assertEquals("wrong status", task2.getStatus(), task.getStatus());
    }

    @Test
    public void testSetTaskStatus() throws NetworkException {
        HashSet<Tag> tags = new HashSet<>();
        tags.add(Tag.POLICE);
        SortedData data = new SortedData(1, "title", "description",
                "location", "source", 1, 2, 3, tags);
        Task task = new Task(-1, "title", "desc", TaskStatus.UNASSIGNED, data,
                Tag.AMBULANCE, null);
        task = (Task) myDB.insertNewTask(task);
        assertNotNull("failed to insert new task", task);

        task.setStatus(TaskStatus.FAILED);
        try {
            myDB.updateTask(task);
        } catch (NetworkException ex) {
            Logger.getLogger(TasksDatabaseManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        Task task2 = (Task) myDB.getTask(task.getId());
        assertEquals("task was not updated", task.getStatus(), task2.getStatus());
    }

    @Test
    public void testPlans() throws NetworkException {
        HashSet<String> keywords = new HashSet<>();
        keywords.add("keyword");

        List<IStep> steps = new ArrayList<>();
        steps.add(new Step(-1, "title", "desc", TaskStatus.UNASSIGNED,
                null, Tag.POLICE, null, 1, "condition"));

        Plan plan = new Plan(-1, "title", "description", keywords, steps, true);

        assertNotNull("Unable to insert new plan", myDB.insertNewPlan(plan));
        assertNotNull("Unable to insert other new plan", myDB.insertNewPlan(plan));

        HashSet<String> keywordSet = new HashSet<>();

        keywordSet.add("ran");
        List<IPlan> plans = myDB.getTemplatePlans(keywordSet);
        assertTrue("did not retrieve the right number of plans", plans.size() == 1);
        assertEquals("did not retrieve the right plan", 1, plans.get(0).getId());
        keywordSet.clear();

        keywordSet.add("brand");
        plans = myDB.getTemplatePlans(keywordSet);
        assertTrue("did not retrieve the right number of plans", plans.size() == 1);
        assertEquals("did not retrieve the right plan", 1, plans.get(0).getId());

        keywordSet.add("drie verdiepingen");
        plans = myDB.getTemplatePlans(keywordSet);
        assertTrue("did not retrieve the right number of plans", plans.size() == 1);
        assertEquals("did not retrieve the right plan", 1, plans.get(0).getId());

        plans = myDB.getTemplatePlans(new HashSet<>());
        assertTrue("Not all plans were retrieved", plans.size() == 4);

        for (IPlan planItem : plans) {
            try {
                String expectedTitle = "??????????";
                String expectedDesc = "???????????";
                int expectedKeywordsCount = -1;
                int expectedStepCount = -1;
                // sets expected
                if (planItem.getId() == 1) {
                    expectedTitle = "Brand";
                    expectedDesc = "Brand in een gebouw met 3 verdiepingen.";
                    expectedKeywordsCount = 3;
                    expectedStepCount = 2;
                } else if (planItem.getId() == 2) {
                    expectedTitle = "Auto ongeluk";
                    expectedDesc = "Er is een auto ongeluk op de snelweg. "
                            + "Er zijn geen gewonden, maar wel veel schade aan de"
                            + " autos en troep op de weg.";
                    expectedKeywordsCount = 6;
                    expectedStepCount = 4;
                } else if (planItem.getId() == myDB.getMaxID("PLAN") - 1 || planItem.getId() == myDB.getMaxID("PLAN")) {
                    expectedTitle = plan.getTitle();
                    expectedDesc = plan.getDescription();
                    expectedKeywordsCount = plan.getKeywords().size();
                    expectedStepCount = plan.getSteps().size();
                } else {
                    fail("id fail (testGetPlans): " + planItem.getId());
                }

                // checks values
                assertEquals("title fail (testGetPlans)", expectedTitle, planItem.getTitle());
                assertEquals("desc fail (testGetPlans)", expectedDesc, planItem.getDescription());
                assertEquals("keyword count fail (testGetPlans)", expectedKeywordsCount,
                        planItem.getKeywords().size());
                assertEquals("step count fail (testGetPlans)", expectedStepCount,
                        planItem.getSteps().size());
            } catch (SQLException ex) {
                Logger.getLogger(TasksDatabaseManagerTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Test
    public void testUsers() throws NetworkException {
        // register citizen
        ICitizen result = new Citizen("testey", "Test McTestey", "Eindhoven", "Rachelsmolen 4");
        result = myDB.registerCitizen(result, "test123");

        assertNotNull("citizen was not registered", result);
        // loginUser
        IUser chiefUser = myDB.loginUser("chief01", "chief01");
        IUser fireUser = myDB.loginUser("firefighter01", "firefighter01");
        IUser citizenUser = myDB.loginUser("testey", "test123");

        assertNotNull("chiefUser was null", chiefUser);
        assertNotNull("fireUser was null", fireUser);
        assertNotNull("citizenUser was null", citizenUser);
        
        try {
            myDB.loginUser("", "");
            myDB.loginUser("CHIEF01", "CHIEF01");
            myDB.loginUser("chief01", "firefighter01");
        } catch (NetworkException ex) {
            Logger.getLogger(TasksDatabaseManagerTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        assertEquals("chiefUser had wrong name",
                "Melanie Kwetters", chiefUser.getName());
        assertEquals("fireUser had wrong name",
                "Bart Bouten", fireUser.getName());
        assertEquals("citizenUser had wrong name",
                "Test McTestey", citizenUser.getName());
        assertTrue("chiefUser was not a hqChief",
                chiefUser instanceof HQChief);
        assertTrue("fireUser was not a ServiceUser",
                fireUser instanceof ServiceUser);
        assertTrue("citizenUser was not a Citizen",
                citizenUser instanceof Citizen);

        HQChief chief = (HQChief) chiefUser;
        ServiceUser firefighter = (ServiceUser) fireUser;
        Citizen citizen = (Citizen) citizenUser;

        assertEquals("firefighter had wrong tag",
                Tag.FIREDEPARTMENT, firefighter.getType());

        // get user
        chiefUser = myDB.getUser("chief01");
        fireUser = myDB.getUser("firefighter01");
        citizenUser = myDB.getUser("testey");
        assertNull("database returned user on blank name",
                myDB.getUser(""));
        assertEquals("getUser chiefUser had wrong name",
                chief.getName(), chiefUser.getName());
        assertEquals("getUser fireUser had wrong name",
                firefighter.getName(), fireUser.getName());
        assertEquals("getUser citizenUser had wrong name",
                citizen.getName(), citizenUser.getName());
        assertTrue("getUser chiefUser was not a hqChief",
                chiefUser instanceof HQChief);
        assertTrue("getUser fireUser was not a ServiceUser",
                fireUser instanceof ServiceUser);
        assertTrue("getUser citizenUser was not a Citizen",
                citizenUser instanceof Citizen);

        // get service users
        List<IServiceUser> serviceUsers = myDB.getServiceUsers();
        assertTrue("not all serviceUsers were retrieved",
                serviceUsers.size() == 9);

        for (IServiceUser sUser : serviceUsers) {
            String expectedName = "????????";
            Tag expectedTag = null;

            switch (sUser.getUsername()) {
                case "firefighter01":
                    expectedName = "Bart Bouten";
                    expectedTag = Tag.FIREDEPARTMENT;
                    break;
                case "firefighter02":
                    expectedName = "Henk van Wylijck";
                    expectedTag = Tag.FIREDEPARTMENT;
                    break;
                case "firefighter03":
                    expectedName = "Jos Hegger";
                    expectedTag = Tag.FIREDEPARTMENT;
                    break;
                case "firefighter04":
                    expectedName = "Frank Haverkort";
                    expectedTag = Tag.FIREDEPARTMENT;
                    break;
                case "paramedic01":
                    expectedName = "Linda van Engelen";
                    expectedTag = Tag.AMBULANCE;
                    break;
                case "paramedic02":
                    expectedName = "Yori Bogie";
                    expectedTag = Tag.AMBULANCE;
                    break;
                case "policeofficer01":
                    expectedName = "Bobby de Hond";
                    expectedTag = Tag.POLICE;
                    break;
                case "policeofficer02":
                    expectedName = "Klaas Smits";
                    expectedTag = Tag.POLICE;
                    break;
                case "policeofficer03":
                    expectedName = "Piet Zwarts";
                    expectedTag = Tag.POLICE;
                    break;
            }

            assertEquals("wrong name serviceUser", expectedName, sUser.getName());
            assertEquals("wrong tag serviceUser", expectedTag, sUser.getType());
        }
    }

}
