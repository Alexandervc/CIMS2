/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp.Database;

import Shared.NetworkException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Linda
 */
class DatabaseManager {

    protected Connection conn;
    private Properties props;
    private static final ReentrantLock lock = new ReentrantLock(true);
    private String managerName;

    /**
     *
     * @param fileName
     */
    public DatabaseManager(String fileName) throws NetworkException {
        this.managerName = fileName;
        this.configure(fileName);
    }

    /**
     * configureproperties
     *
     * @param fileName
     */
    private void configure(String fileName) throws NetworkException {
        props = new Properties();

        try (FileInputStream in = new FileInputStream(fileName)) {
            props.load(in);
            Class.forName("com.mysql.jdbc.Driver");
        } catch (FileNotFoundException ex) {
            System.out.println("file not found in database configure: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("IOException in database configure: " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException in database configure: " + ex.getMessage());
        }

        try {
            if (!openConnection() || conn == null || conn.isClosed()) {
                throw new SQLException("Connection was null or closed");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    /**
     * Resets database to dummy data state
     *
     * @return
     */
    protected boolean resetDatabase() throws NetworkException {
        if (!openConnection()) {
            return false;
        }

        try {
            CallableStatement cs = this.conn.prepareCall("{call ResetDatabase()}");
            cs.executeQuery();
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            closeConnection();
        }
    }

    /**
     * open connection
     *
     * @return
     */
    protected synchronized boolean openConnection() throws NetworkException {

        try {
            if (conn != null && !conn.isClosed()) {
                return true;
            }

            if (!lock.isHeldByCurrentThread()
                    && !lock.tryLock(10000, TimeUnit.MILLISECONDS)) {
                throw new NetworkException("Database lock timeout for "
                        + Thread.currentThread().getName()
                        + " on " + managerName);
            }

            System.setProperty("jdbc.drivers", props.getProperty("driver"));
            this.conn = DriverManager.getConnection(
                    (String) props.get("url"),
                    (String) props.get("username"),
                    (String) props.get("password"));
            return true;
        } catch (Exception ex) {
            closeConnection();
            ex.printStackTrace();
            throw new NetworkException("Kon geen verbinding maken met de database: "
                                        + ex.getMessage());
        }
    }

    /**
     * closing connection
     */
    protected synchronized void closeConnection() {
        if (!lock.isHeldByCurrentThread()) {
            return;
        }
        lock.unlock();
    }

    public void shutDownConnection() {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            conn = null;
        }
    }

    /**
     * gets max ID from given table. Does not open its own connection.
     *
     * @param tableName
     * @return
     * @throws SQLException
     */
    protected int getMaxID(String tableName) throws SQLException {
        int output = -1;
        // Gets assigned ID. Throws Exception if not found
        String query = "SELECT MAX(ID) FROM " + tableName;
        PreparedStatement prepStat = conn.prepareStatement(query);
        ResultSet rs = prepStat.executeQuery();
        while (rs.next()) {
            output = rs.getInt(1);
        }
        return output;
    }

}
