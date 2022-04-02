package com.stackCP;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class StackCP {
    private String databaseUrl;
    private String userName;
    private String password;
    private int maxPoolSize = 10;
    private int connNum = 0;
    static StackCP pool = new StackCP(
            "jdbc:mysql://localhost:3306/JDBC?",
            "root", "Toor123@", 2);

    private static final String SQL_VERIFYCONN = "select 1";

    Stack<Connection> freePool = new Stack<>();
    Set<Connection> occupiedPool = new HashSet<>();

    /**
     * Constructor
     *
     * @param databaseUrl The connection url
     * @param userName    user name
     * @param password    password
     * @param maxSize     max size of the connection pool
     */
    public StackCP(String databaseUrl, String userName,
                   String password, int maxSize) {
        this.databaseUrl = databaseUrl;
        this.userName = userName;
        this.password = password;
        this.maxPoolSize = maxSize;
    }

    /**
     * Get an available connection
     *
     * @return An available connection
     * @throws SQLException Fail to get an available connection
     */
    public synchronized Connection getConnection() throws SQLException {
        Connection conn = null;

        if (isFull()) {
            throw new SQLException("The connection pool is full.");
        }

        conn = getConnectionFromPool();

        // If there is no free connection, create a new one.
        if (conn == null) {
            conn = createNewConnectionForPool();
        }


        conn = makeAvailable(conn);
        return conn;
    }

    /**
     * Return a connection to the pool
     *
     * @param conn The connection
     * @throws SQLException When the connection is returned already or it isn't gotten
     *                      from the pool.
     */
    public synchronized void returnConnection(Connection conn)
            throws SQLException {
        if (conn == null) {
            throw new NullPointerException();
        }
        if (!occupiedPool.remove(conn)) {
            throw new SQLException(
                    "The connection is returned already or it isn't for this pool");
        }
        freePool.push(conn);
    }

    /**
     * Verify if the connection is full.
     *
     * @return if the connection is full
     */
    private synchronized boolean isFull() {
        return ((freePool.size() == 0) && (connNum >= maxPoolSize));
    }

    /**
     * Create a connection for the pool
     *
     * @return the new created connection
     * @throws SQLException When fail to create a new connection.
     */
    private Connection createNewConnectionForPool() throws SQLException {
        Connection conn = createNewConnection();
        connNum++;
        occupiedPool.add(conn);
        return conn;
    }

    /**
     * Crate a new connection
     *
     * @return the new created connection
     * @throws SQLException When fail to create a new connection.
     */
    private Connection createNewConnection() throws SQLException {
        Connection conn = null;
        conn = DriverManager.getConnection(databaseUrl, userName, password);
        return conn;
    }

    /**
     * Get a connection from the pool. If there is no free connection, return
     * null
     *
     * @return the connection.
     */
    private Connection getConnectionFromPool() {
        Connection conn = null;
        if (freePool.size() > 0) {
            conn = freePool.pop();
            occupiedPool.add(conn);
        }
        return conn;
    }

    /**
     * Make sure the connection is available now. Otherwise, reconnect it.
     *
     * @param conn The connection for verification.
     * @return the available connection.
     * @throws SQLException Fail to get an available connection
     */
    private Connection makeAvailable(Connection conn) throws SQLException {
        if (isConnectionAvailable(conn)) {
            return conn;
        }

        // If the connection isn't available, reconnect it.
        occupiedPool.remove(conn);
        connNum--;
        conn.close();

        conn = createNewConnection();
        occupiedPool.add(conn);
        connNum++;
        return conn;
    }

    /**
     * By running a sql to verify if the connection is available
     *
     * @param conn The connection for verification
     * @return if the connection is available for now.
     */
    private boolean isConnectionAvailable(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.executeQuery(SQL_VERIFYCONN);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void tryExecute(String query) throws SQLException {
        Connection conn = null;
        try {
            conn = pool.getConnection();

            try (Statement statement = conn.createStatement()) {
                ResultSet res = statement.executeQuery(query);
                while (res.next()) {
                    String tblName = res.getString(1);
                    System.out.println(tblName);
                }
            }
        } finally {
            if (conn != null) {
                pool.returnConnection(conn);
            }
        }
    }

    // Just an Example
    public static void main(String[] args) throws SQLException {
        String query1 = "SELECT * FROM Auto";
        String query3 = "SELECT * FROM Auto WHERE Nationality='ITA'";
        String query2 = "show tables";
        tryExecute(query1);
        tryExecute(query2);
        tryExecute(query3);

    }

}
