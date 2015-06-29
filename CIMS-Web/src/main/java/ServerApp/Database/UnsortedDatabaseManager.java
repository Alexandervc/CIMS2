/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import Shared.Data.IData;
import Shared.Data.IUnsortedData;
import Shared.Data.Status;
import Shared.Data.UnsortedData;
import Shared.NetworkException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia
 */
public class UnsortedDatabaseManager extends DatabaseManager {

    private final String unsortedDataTable = "UNSORTEDDATA";

    public UnsortedDatabaseManager(String propsFileName) throws NetworkException {
        super(propsFileName);
    }

    /**
     * @param data object unsorteddata
     * @return success on attempting to insert unsorted data.
     */
    public IData insertToUnsortedData(IData data) throws NetworkException {
        IData output = null;
        int id = -1;
        
        if (data == null) {
            throw new IllegalArgumentException("Voer een data in");
        }
        
        openConnection();
        
        try {
            String query = "INSERT INTO " + unsortedDataTable + " VALUES (ID,?,?,?,?,?)";
            PreparedStatement unsortedData = conn.prepareStatement(query);
            unsortedData.setString(1, data.getTitle());
            unsortedData.setString(2, data.getDescription());
            unsortedData.setString(3, data.getLocation());
            unsortedData.setString(4, data.getSource());
            unsortedData.setString(5, Status.NONE.toString());
            unsortedData.execute();            

            id = super.getMaxID(unsortedDataTable);
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon ongesorteerde data niet opslaan: " + ex.getMessage());
        } finally {
            closeConnection();
        }
        return this.getDataItem(id);
    }

    /**
     * @return List unsorteddata first get information from database second
     * change status to INPROCESS
     */
    public List<IData> getFromUnsortedData() throws NetworkException {
        List<IData> unsorted = new ArrayList();

        int id;
        String title;
        String description;
        String location;
        String source;

        openConnection();

        try {
            String query = "SELECT * FROM " + unsortedDataTable + " WHERE STATUS "
                    + " = 'NONE' ORDER BY ID  LIMIT 50";
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            //getting unsorteddata
            while (result.next()) {
                    id = result.getInt("ID");
                    title = result.getString("TITLE");
                    description = result.getString("DESCRIPTION");
                    location = result.getString("LOCATION");
                    source = result.getString("SOURCE");
                    Status status = Status.valueOf(result.getString("STATUS"));

                    unsorted.add(new UnsortedData(id, title, description, location, source, status));
            }

            //update data
            for (IData x : unsorted) {
                String update = "UPDATE " + unsortedDataTable + " SET STATUS = '"
                        + Status.INPROCESS.toString() + "' WHERE ID = " + x.getId();
                PreparedStatement updateData = conn.prepareStatement(update);
                updateData.execute();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon ongesorteerde data niet ophalen: " + ex.getMessage());
        } finally {
            closeConnection();
        }
        return unsorted;
    }

    /**
     * @param data list of unsorteddata
     * @return succeed reset status unsorted data
     */
    public void resetUnsortedData(List<IData> data) throws NetworkException {
        if(data.isEmpty()){
            return;
        }
        
        openConnection();

        try {
            for (IData x : data) {
                String query = "UPDATE " + unsortedDataTable + " SET STATUS = '"
                        + Status.NONE.toString() + "' WHERE id = " + x.getId();
                PreparedStatement reset = conn.prepareStatement(query);

                reset.execute();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon ongesorteerde data niet resetten: " + ex.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * @param data object of unsorteddata
     * @return succeed reset status unsorted data to Completed
     */
    public void updateStatusUnsortedData(IData data) throws NetworkException {
        openConnection();
        
        try {
            String query = "UPDATE " + unsortedDataTable + " SET STATUS = '"
                    + Status.COMPLETED.toString() + "' WHERE id = " + data.getId();

            PreparedStatement update = conn.prepareStatement(query);

            update.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon de status van de ongesorteerde data niet updaten: " + ex.getMessage());
        } finally {
            closeConnection();
        }
    }
    
    /**
     * Updates the status in the database of the unsortedData to the current status
     * @param data object of unsorteddata
     * @return succeed reset status unsorted data to Completed
     */
    public void updateUnsortedStatus(IUnsortedData data) throws NetworkException {
        openConnection();
        
        try {
            String query = "UPDATE " + unsortedDataTable + " SET STATUS = '"
                    + data.getStatus() + "' WHERE id = " + data.getId();

            PreparedStatement update = conn.prepareStatement(query);

            update.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon de status van de ongesorteerde data niet updaten: " + ex.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * Updates piece of unsorted data with given id.
     *
     * @param id
     * @param iData
     * @return false if id not found
     */
    public void updateUnsortedData(IData iData) throws NetworkException {
        openConnection();
        
        try {
            String query = "UPDATE " + unsortedDataTable + " SET TITLE = '"
                    + iData.getTitle() + "', DESCRIPTION = '"
                    + iData.getDescription() + "', LOCATION = '"
                    + iData.getLocation() + "', SOURCE = '"
                    + iData.getSource() + "', STATUS = '"
                    + Status.NONE + "' WHERE ID=" + iData.getId();

            PreparedStatement update = conn.prepareStatement(query);

            update.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon ongesorteerde data niet updaten: " + ex.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * @param iData object of unsorteddata
     * @return succeed reset status unsorted data to Discard
     */
    public void discardUnsortedData(IData iData) throws NetworkException {
        openConnection();

        try {
            String query = "UPDATE " + unsortedDataTable + " SET STATUS = '"
                    + Status.DISCARDED.toString() + "' WHERE id = " + iData.getId();

            PreparedStatement update = conn.prepareStatement(query);

            update.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon ongesorteerde data niet verwijderen: " + ex.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * @param id of object
     * @return unsorted-object with correct id
     */
    public IData getDataItem(int id) throws NetworkException {
        openConnection();
        IData unsorted = null;

        String title;
        String description;
        String location;
        String source;

        try {
            String query = "SELECT * FROM " + unsortedDataTable + " WHERE ID = " + id;
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            //getting unsorteddata
            while (result.next() && unsorted == null) {
                title = result.getString("TITLE");
                description = result.getString("DESCRIPTION");
                location = result.getString("LOCATION");
                source = result.getString("SOURCE");
                Status status = Status.valueOf(result.getString("STATUS"));

                unsorted = new UnsortedData(id, title, description, location, source, status);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon data niet ophalen: " + ex.getMessage());
        } finally {
            closeConnection();
        }
        return unsorted;
    }

    /**
     * @param source of object
     * @return list unsorted-objects with correct source
     */
    public List<IData> getSentData(String source) throws NetworkException {
        openConnection();
        List<IData> unsorted = new ArrayList();

        int id;
        String title;
        String description;
        String location;
        String realSource;

        try {
            String query = "SELECT * FROM " + unsortedDataTable + " ";
            if (!source.isEmpty()) {
                query += "WHERE SOURCE = '" + source + "' ";
            }
            query += "ORDER BY ID";
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            //getting unsorteddata
            while (result.next() && unsorted.size() < 50) {
                id = result.getInt("ID");
                title = result.getString("TITLE");
                description = result.getString("DESCRIPTION");
                location = result.getString("LOCATION");
                realSource = result.getString("SOURCE");
                Status status = Status.valueOf(result.getString("STATUS"));

                unsorted.add(new UnsortedData(id, title, description, location, realSource, status));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon verzonden data niet ophalen: " + ex.getMessage());
        } finally {
            closeConnection();
        }
        return unsorted;
    }
}
