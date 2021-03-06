/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kargathia
 */
public class SortedDatabaseManager extends DatabaseManager {

    private final String sortedDataTable = "SORTEDDATA",
            sortedDataTagsTable = "SORTEDDATATAGS",
            requestsTable = "REQUEST",
            requestTagsTable = "REQUESTTAGS",
            newsItemTable = "NEWSITEM",
            pictureTable = "PICTURE",
            newsSituationsTable = "NEWSSITUATION",
            situationsTable = "SITUATION",
            situationsAdviceTable = "SITUATIONADVICE",
            adviceTable = "ADVICE";

    public SortedDatabaseManager(String propsFileName) throws NetworkException {
        super(propsFileName);
    }

    private List<INewsItem> extractNewsItems(ResultSet rs) throws SQLException, NetworkException {
        // key: newsItem ID
        HashMap<Integer, NewsItem> newsItems;
        // key: newsItem ID, value = situation IDs
        HashMap<NewsItem, Set<Integer>> newsSituations;
        List<Integer> order = new ArrayList<>();
        List<INewsItem> output = new ArrayList<>();

        newsItems = new HashMap<>();
        newsSituations = new HashMap<>();
        while (rs.next()) {
                // query will display items as a completely expanded tree
            // with total number of rows being the sum of all newsitem situations

            // first checks newsitems
            // only generates a new item if ID is not duplicate
            int newsID = rs.getInt("ni.ID");
            if (newsItems.get(newsID) == null) {
                order.add(newsID);
                String title = rs.getString("ni.TITLE");
                String newsDesc = rs.getString("ni.DESCRIPTION");
                String loc = rs.getString("ni.LOCATION");
                String source = rs.getString("ni.SOURCE");
                int victims = rs.getInt("ni.VICTIMS");
                Timestamp stamp = rs.getTimestamp("ni.ITEMDATE");
                Date date = new Date(stamp.getTime());
                NewsItem item = new NewsItem(newsID, title, newsDesc,
                        loc, source, null, victims, date);
                newsItems.put(newsID, item);
                newsSituations.put(item, new HashSet<>());
            }
            // adds situation ID
            int sitID = rs.getInt("ns.SITUATIONID");
            if (sitID > 0) {
                newsSituations.get(newsItems.get(newsID)).add(sitID);
            }
        }

        HashMap<Integer, INewsItem> itemsMap = assignSituations(newsSituations);
        for (int id : order) {
            output.add(itemsMap.get(id));
        }

        return output;
    }

    /**
     *
     * @param newsItems
     * @return
     */
    private HashMap<Integer, INewsItem> assignSituations(HashMap<NewsItem, Set<Integer>> newsItems) throws NetworkException {
        if (newsItems == null) {
            return null;
        }
        HashMap<Integer, INewsItem> output = new HashMap<>();

        // gets all unique situations from database, then assigns them on ID
        // prevents constantly regenerating a limited number of situations/advices
        HashMap<Integer, Situation> situations = this.getSituationsMap();
        for (NewsItem item : newsItems.keySet()) {
            for (int sitID : newsItems.get(item)) {
                item.addSituation(situations.get(sitID));
            }
            output.put(item.getId(), item);
        }
        return output;
    }

    /**
     *
     * @param news
     * @param rs
     * @return INewsItem
     * @throws SQLException
     */
    private INewsItem addPicturesToNewsItem(INewsItem news, ResultSet rs) throws SQLException {
        if (news == null || rs == null) {
            return news;
        }

        INewsItem output = news;
        while (rs.next()) {
            String link = rs.getString("LINK");
            output.addPicture(link);
        }
        return output;
    }
    
    public void insertPicture(INewsItem news, String link) throws NetworkException {
        if(news == null) {
            throw new IllegalArgumentException("Voer een nieuwsbericht in");
        }
        if(link == null) {
            throw new IllegalArgumentException("Voer een link in");
        }
        
        openConnection();
        
        boolean success = false;
        String query;
        PreparedStatement prepStat;
        try {
            query = "INSERT INTO " + pictureTable
                    + " VALUES (?, ?)";
            prepStat = conn.prepareStatement(query);
            prepStat.setInt(1, news.getId());
            prepStat.setString(2, link);
            prepStat.execute();
        } catch(SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon foto niet toevoegen: " + ex.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * @param sorted object sorteddata
     * @return succeed on attempting to insert sorted data.
     */
    public void insertToSortedData(ISortedData sorted) throws NetworkException {
        openConnection();
        
        Set<Tag> tags = sorted.getTags();
        try {
            //insert to sorteddata
            String query = "INSERT INTO " + sortedDataTable + " VALUES (?,?,?,?,?,?,?,?)";
            PreparedStatement sortedData = conn.prepareStatement(query);
            sortedData.setInt(1, sorted.getId());
            sortedData.setString(2, sorted.getTitle());
            sortedData.setString(3, sorted.getDescription());
            sortedData.setString(4, sorted.getLocation());
            sortedData.setString(5, sorted.getSource());
            sortedData.setInt(6, sorted.getRelevance());
            sortedData.setInt(7, sorted.getReliability());
            sortedData.setInt(8, sorted.getQuality());
            sortedData.execute();

            Iterator it = tags.iterator();
            while (it.hasNext()) {
                // Get element
                Object element = it.next();

                //insert into sorteddatatags database
                query = "INSERT INTO " + sortedDataTagsTable + " VALUES (?,?) ";
                sortedData = conn.prepareStatement(query);
                sortedData.setInt(1, sorted.getId());
                sortedData.setString(2, element.toString());
                sortedData.execute();
            }

            ServerMain.unsortedDatabaseManager.updateStatusUnsortedData(sorted);
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon gesorteerde data niet toevoegen: " + ex.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * @param info list of tags
     * @return List sorteddata
     */
    public List<ISortedData> getFromSortedData(HashSet<Tag> info) throws NetworkException {
        openConnection();

        List<ISortedData> sorted = new ArrayList();
        HashSet<Integer> numbers = new HashSet<Integer>();

        //list of id's with correct tags
        int id;
        String title;
        String description;
        String location;
        String source;
        int relevance;
        int reliability;
        int quality;
        HashSet<Tag> newTags = new HashSet<Tag>();

        try {
            String query = "SELECT DATAID FROM " + sortedDataTagsTable + " ";
            int sizeList = info.size();
            Iterator it = info.iterator();
            int aantal = 1;
            while (it.hasNext()) {
                // Get element
                Object element = it.next();
                if (aantal == 1) {
                    query += "WHERE TAGNAME = '" + element.toString() + "' ";
                    aantal++;
                } else {
                    query += "AND DATAID IN (SELECT DATAID FROM"
                            + " " + sortedDataTagsTable + " WHERE  TAGNAME = '"
                            + element.toString() + "' ";
                }
            }
            for (int x = 1; x < sizeList; x++) {
                query += ")";
            }

            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            while (result.next()) {
                numbers.add(result.getInt("DATAID"));
            }

            //make list of object with correct id's
            String update = "";
            Iterator it2 = numbers.iterator();
            while (it2.hasNext()) {
                // Get element
                Object element = it2.next();
                if (sorted.size() < 50) {
                    update = "SELECT * FROM " + sortedDataTable + " WHERE ID = " + element.toString();
                    PreparedStatement updateData = conn.prepareStatement(update);
                    ResultSet resultTag = updateData.executeQuery();
                    while (resultTag.next()) {
                        id = resultTag.getInt("ID");
                        title = resultTag.getString("TITLE");
                        description = resultTag.getString("DESCRIPTION");
                        location = resultTag.getString("LOCATION");
                        source = resultTag.getString("SOURCE");
                        relevance = resultTag.getInt("RELEVANCE");
                        reliability = resultTag.getInt("RELIABILITY");
                        quality = resultTag.getInt("QUALITY");

                        String getTags = "Select TAGNAME From " + sortedDataTagsTable
                                + " WHERE "
                                + "DATAID = " + id;
                        PreparedStatement getTagsData = conn.prepareStatement(getTags);
                        ResultSet tagsData = getTagsData.executeQuery();
                        while (tagsData.next()) {
                            Tag tag = Tag.valueOf(tagsData.getString("TAGNAME"));
                            newTags.add(tag);
                        }
                        SortedData sortedItem = new SortedData(id, title, description,
                                location, source, relevance,
                                reliability, quality, newTags);
                        sortedItem.setTasks(ServerMain.tasksDatabaseManager
                                .getSortedDataTasks(sortedItem));

                        sorted.add(sortedItem);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon gesorteerde data niet ophalen: " + ex.getMessage());
        } finally {
            closeConnection();
        }
        return sorted;
    }

    /**
     * @param data object of datarequest
     * @return succeed if succeeded
     */
    public void insertDataRequest(IDataRequest data) throws NetworkException {
        openConnection();

        try {
            Set<Tag> tags = data.getTags();
            //insert to sorteddata
            String query = "INSERT INTO " + requestsTable + " VALUES (ID,?,?,?,?,?)";
            PreparedStatement requestData = conn.prepareStatement(query);
            requestData.setString(1, data.getTitle());
            requestData.setString(2, data.getDescription());
            requestData.setString(3, data.getLocation());
            requestData.setString(4, data.getSource());
            requestData.setInt(5, data.getRequestId());
            requestData.execute();

            //Find id from this object
            int id = super.getMaxID(requestsTable);

            Iterator it = tags.iterator();
            while (it.hasNext()) {
                // Get tagid
                Object element = it.next();

                //insert into requesttag database
                query = "INSERT INTO " + requestTagsTable + " VALUES (?,?) ";
                requestData = conn.prepareStatement(query);
                requestData.setInt(1, id);
                requestData.setString(2, element.toString());
                requestData.execute();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon melding niet opslaan: " + ex.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     * @param tags list of tags
     * @return succeed if succeeded
     */
    public List<IDataRequest> getUpdateRequests(HashSet tags) throws NetworkException {
        openConnection();

        List<IDataRequest> request = new ArrayList();
        HashSet<Integer> numbers = new HashSet<Integer>();

        int id;
        String title;
        String description;
        String location;
        String source;
        int dataID;
        HashSet<Tag> newTags = new HashSet<Tag>();

        try {
            //build a string with all tha tags
            String query = "SELECT REQUESTID FROM " + requestTagsTable + " ";
            int sizeList = tags.size();
            Iterator it = tags.iterator();
            int amount = 1;
            while (it.hasNext()) {
                // Get element
                Object element = it.next();
                if (amount == 1) {
                    query += "WHERE TAGNAME = '" + element.toString() + "' ";
                    amount++;
                } else {
                    query += "AND REQUESTID IN (SELECT REQUESTID FROM " + requestTagsTable
                            + " WHERE  TAGNAME ='" + element.toString() + "' ";
                }
            }
            for (int x = 1; x < sizeList; x++) {
                query += ")";
            }
            //making a list with al de id's with correct tags
            PreparedStatement readData = conn.prepareStatement(query);
            ResultSet result = readData.executeQuery();

            while (result.next()) {
                numbers.add(result.getInt("REQUESTID"));
            }

            //Get request data from database with correct id's
            String update = "";
            Iterator it2 = numbers.iterator();
            while (it2.hasNext()) {
                // Get element
                Object element = it2.next();
                if (request.size() < 50) {
                    update = "SELECT * FROM " + requestsTable + " WHERE ID = "
                            + element.toString();
                    PreparedStatement updateData = conn.prepareStatement(update);
                    ResultSet resultTag = updateData.executeQuery();
                    while (resultTag.next()) {
                        id = resultTag.getInt("ID");
                        title = resultTag.getString("TITLE");
                        description = resultTag.getString("DESCRIPTION");
                        location = resultTag.getString("LOCATION");
                        source = resultTag.getString("SOURCE");
                        dataID = resultTag.getInt("DATAID");

                        String getTags = "Select TAGNAME From "
                                + requestTagsTable + " WHERE "
                                + "REQUESTID = " + id;
                        PreparedStatement getTagsData = conn.prepareStatement(getTags);
                        ResultSet tagsData = getTagsData.executeQuery();
                        while (tagsData.next()) {
                            Tag tag = Tag.valueOf(tagsData.getString("TAGNAME"));
                            newTags.add(tag);
                        }

                        request.add(new DataRequest(id, title, description, location, source, dataID, tags));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon meldingen niet ophalen: " + ex.getMessage());
        } finally {
            closeConnection();
        }

        return request;
    }

    /**
     *
     * @param ID
     * @return SortedData with given ID. null if unknown.
     */
    public ISortedData getFromSortedData(int ID) throws NetworkException {
        if(ID == -1) {
            throw new IllegalArgumentException("ID mag niet -1 zijn");
        }
        
        openConnection();

        ISortedData output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        try {
            // gets sorted data
            query = "SELECT * FROM " + sortedDataTable + " WHERE ID = ?";
            prepStat = conn.prepareStatement(query);
            prepStat.setInt(1, ID);
            rs = prepStat.executeQuery();

            int outputID = -1,
                    outputRelevance = -1,
                    outputReliability = -1,
                    outputQuality = -1;
            String outputTitle = null,
                    outputDesc = null,
                    outputLocation = null,
                    outputSource = null;

            while (rs.next()) {
                outputID = rs.getInt("ID");
                outputTitle = rs.getString("TITLE");
                outputDesc = rs.getString("DESCRIPTION");
                outputLocation = rs.getString("LOCATION");
                outputSource = rs.getString("SOURCE");
                outputRelevance = rs.getInt("RELEVANCE");
                outputReliability = rs.getInt("RELIABILITY");
                outputQuality = rs.getInt("QUALITY");
            }
            // if no data found
            if (outputID == -1) {
                throw new SQLException("Kon data niet vinden");
            }

            HashSet<Tag> tags = new HashSet<>();
            // gets tags
            query = "SELECT * FROM " + sortedDataTagsTable + " WHERE DATAID = ?";
            prepStat = conn.prepareStatement(query);
            prepStat.setInt(1, outputID);
            rs = prepStat.executeQuery();

            while (rs.next()) {
                tags.add(Tag.valueOf(rs.getString("TAGNAME")));
            }
            output = new SortedData(outputID, outputTitle, outputDesc,
                    outputLocation, outputSource, outputRelevance,
                    outputReliability, outputQuality, tags);

            output.setTasks(ServerMain.tasksDatabaseManager.getSortedDataTasks(output));
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon sortedData niet ophalen: " + ex.getMessage());
        } finally {
            closeConnection();
        }
        return output;
    }

    /**
     *
     * @return all situations as set
     */
    public Set<Situation> getSituations() throws NetworkException {
        HashMap<Integer, Situation> map = this.getSituationsMap();
        if (map != null) {
            return new HashSet(map.values());
        } else {
            return null;
        }
    }

    /**
     *
     * @return all situations as HashMap, key being their ID
     */
    public HashMap<Integer, Situation> getSituationsMap() throws NetworkException {
        openConnection();

        HashMap<Integer, Situation> output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        try {
            query = "SELECT ad.*, st.* FROM " + situationsTable + " AS st"
                    + " LEFT JOIN " + situationsAdviceTable + " AS sa"
                    + " ON st.ID = sa.SITUATIONID"
                    + " LEFT JOIN " + adviceTable + " AS ad"
                    + " ON sa.ADVICEID = ad.ID"
                    + " ORDER BY st.ID";
            prepStat = conn.prepareStatement(query);
            rs = prepStat.executeQuery();

            output = new HashMap<>();
            while (rs.next()) {
                int sitID = rs.getInt("st.ID");
                String sitDesc = rs.getString("st.DESCRIPTION");
                output.putIfAbsent(sitID, new Situation(sitID, null, sitDesc));

                int advID = rs.getInt("ad.ID");
                String advDesc = rs.getString("ad.DESCRIPTION");
                output.get(sitID).addAdvice(new Advice(advID, advDesc));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon situaties niet ophalen: " + ex.getMessage());
        } finally {
            closeConnection();
        }
        return output;
    }

    /**
     *
     * @param startIndex index of first item shown, starts at 0
     * @param limit must be >= 1
     * @return news items from database
     */
    public List<INewsItem> getNewsItems(int startIndex, int limit) throws NetworkException {
        if(limit < 1) {
            throw new IllegalArgumentException("Limiet mag niet kleiner zijn dan 1");
        }
        
        openConnection();

        // key: newsItem ID, value = situation IDs
        HashMap<NewsItem, Set<Integer>> newsItems;

        List<INewsItem> output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        try {
            query = "SELECT ni.*, ns.*"
                    + " FROM (SELECT * FROM " + newsItemTable
                    + " ORDER BY ITEMDATE DESC, TITLE LIMIT ?,?) AS ni"
                    + " LEFT JOIN " + newsSituationsTable + " AS ns"
                    + " ON ni.ID = ns.NEWSID"
                    + " ORDER BY ni.ITEMDATE DESC, ni.TITLE";
            prepStat = conn.prepareCall(query);
            prepStat.setInt(1, startIndex);
            prepStat.setInt(2, limit);
            rs = prepStat.executeQuery();
            output = this.extractNewsItems(rs);

            for (INewsItem n : output) {
                query = "SELECT LINK FROM " + pictureTable
                        + " WHERE ID = ?";
                prepStat = conn.prepareStatement(query);
                prepStat.setInt(1, n.getId());
                rs = prepStat.executeQuery();
                this.addPicturesToNewsItem(n, rs);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon geen nieuwsberichten ophalen: " + ex.getMessage());
        } finally {
            closeConnection();
        }

        return output;
    }

    /**
     *
     * @return all NewsItems
     */
    public List<INewsItem> getNewsItems() throws NetworkException {
        return this.getNewsItems(0, Integer.MAX_VALUE);
    }

    /**
     * Inserts given item. Returns item including ID.
     *
     * @param item
     * @return
     */
    public INewsItem insertNewsItem(INewsItem item) throws NetworkException {
        if(item == null) {
            throw new IllegalArgumentException("Voer een item in");
        }
        
        openConnection();

        INewsItem output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        try {
            // inserts news item
            query = "INSERT INTO " + newsItemTable
                    + " (TITLE, DESCRIPTION, LOCATION, SOURCE, VICTIMS)"
                    + " VALUES (?,?,?,?,?)";
            prepStat = conn.prepareStatement(query);
            prepStat.setString(1, item.getTitle());
            prepStat.setString(2, item.getDescription());
            prepStat.setString(3, item.getLocation());
            prepStat.setString(4, item.getSource());
            prepStat.setInt(5, item.getVictims());
            prepStat.execute();

            // gets ID and Date
            item.setID(super.getMaxID(newsItemTable));
            query = "SELECT ITEMDATE FROM " + newsItemTable
                    + " WHERE ID = ?";
            prepStat = conn.prepareStatement(query);
            prepStat.setInt(1, item.getId());
            rs = prepStat.executeQuery();
            while (rs.next()) {
                item.setDate(rs.getDate("ITEMDATE"));
            }

            // inserts references to all situations
            query = "INSERT INTO " + newsSituationsTable
                    + " (NEWSID, SITUATIONID)"
                    + " VALUES (?, ?)";
            prepStat = conn.prepareStatement(query);
            for (Situation sit : item.getSituations()) {
                prepStat.setInt(1, item.getId());
                prepStat.setInt(2, sit.getID());
                prepStat.addBatch();
            }
            prepStat.executeBatch();
            
            //inserts picture links
            query = "INSERT INTO " + pictureTable
                    + " (ID, LINK)"
                    + " VALUES (?, ?)";
            prepStat = conn.prepareStatement(query);
            for (String p : item.getPictures()) {
                prepStat.setInt(1, item.getId());
                prepStat.setString(2, p);
                prepStat.addBatch();
            }
            prepStat.executeBatch();

            output = item;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon nieuwsbericht niet opslaan: " + ex.getMessage());
        } finally {
            closeConnection();
        }
        return output;
    }

    /**
     * Updates given news item.
     *
     * @param item
     * @return
     */
    public void updateNewsItem(INewsItem item) throws NetworkException {
        if(item == null) {
            throw new IllegalArgumentException("Voer een item in");
        }
        if(item.getId() == -1) {
            throw new IllegalArgumentException("ID van item mag niet -1 zijn");
        }
        
        openConnection();

        String query;
        PreparedStatement prepStat;

        try {
            query = "UPDATE " + newsItemTable + " SET"
                    + " TITLE = ?,"
                    + " DESCRIPTION = ?,"
                    + " LOCATION = ?,"
                    + " SOURCE = ?,"
                    + " VICTIMS = ?"
                    + " WHERE ID = ?";
            prepStat = conn.prepareStatement(query);
            prepStat.setString(1, item.getTitle());
            prepStat.setString(2, item.getDescription());
            prepStat.setString(3, item.getLocation());
            prepStat.setString(4, item.getSource());
            prepStat.setInt(5, item.getVictims());
            prepStat.setInt(6, item.getId());
            prepStat.execute();

            // wipes current references to situations
            query = "DELETE FROM " + newsSituationsTable
                    + " WHERE NEWSID = ?";
            prepStat = conn.prepareStatement(query);
            prepStat.setInt(1, item.getId());
            prepStat.execute();

            // inserts references to all situations
            query = "INSERT INTO " + newsSituationsTable
                    + " (NEWSID, SITUATIONID)"
                    + " VALUES (?, ?)";
            prepStat = conn.prepareStatement(query);
            for (Situation sit : item.getSituations()) {
                prepStat.setInt(1, item.getId());
                prepStat.setInt(2, sit.getID());
                prepStat.addBatch();
            }
            prepStat.executeBatch();
            
            // inserts new pictures
            query = "INSERT INTO " + pictureTable
                    + " (ID, LINK)"
                    + " VALUES (?, ?)";
            prepStat = conn.prepareStatement(query);
            for (String p : item.getPictures()) {
                prepStat.setInt(1, item.getId());
                prepStat.setString(2, p);
                prepStat.addBatch();
            }
            prepStat.executeBatch();
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon nieuwsbericht niet updaten: " + ex.getMessage());
        } finally {
            closeConnection();
        }
    }

    /**
     *
     * @return total number of newsitems present in database.
     */
    public int getNewsItemCount() throws NetworkException {
        openConnection();

        String query;
        ResultSet rs;
        Integer output = -1;

        try {
            query = "SELECT COUNT(ID) FROM " + newsItemTable;
            rs = conn.prepareStatement(query).executeQuery();
            while (rs.next()) {
                output = rs.getInt("COUNT(ID)");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon aantal nieuwsberichten niet ophalen: " + ex.getMessage());
        } finally {
            closeConnection();
        }
        return output;
    }

    public INewsItem getNewsItemByID(int ID) throws NetworkException {
        if(ID < 0) {
            throw new IllegalArgumentException("ID mag niet kleiner zijn dan 0");
        }
        
        openConnection();

        // key: newsItem ID, value = situation IDs
        List<INewsItem> outputList = new ArrayList<>();

        INewsItem output = null;
        String query;
        PreparedStatement prepStat;
        ResultSet rs;

        try {
            query = "SELECT ni.*, ns.*"
                    + " FROM (SELECT * FROM " + newsItemTable
                    + " WHERE ID = ?) AS ni"
                    + " LEFT JOIN " + newsSituationsTable + " AS ns"
                    + " ON ni.ID = ns.NEWSID"
                    + " ORDER BY ni.ID";
            prepStat = conn.prepareStatement(query);
            prepStat.setInt(1, ID);
            rs = prepStat.executeQuery();
            outputList = this.extractNewsItems(rs);

            for (INewsItem n : outputList) {
                query = "SELECT LINK FROM " + pictureTable
                        + " WHERE ID = ?";
                prepStat = conn.prepareStatement(query);
                prepStat.setInt(1, ID);
                rs = prepStat.executeQuery();
                n = this.addPicturesToNewsItem(n, rs);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new NetworkException("Kon nieuwsbericht niet ophalen: " + ex.getMessage());
        } finally {
            closeConnection();
        }

        if (outputList != null && !outputList.isEmpty()) {
            output = outputList.get(0);
        }
        return output;
    }
}
