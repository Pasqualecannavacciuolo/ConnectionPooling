import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

public class ConnectionPool {

    // JDBC Driver Name & Database URL
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String JDBC_DB_URL = "jdbc:mysql://localhost:3306/JDBC?";

    // JDBC Database Credentials
    static final String JDBC_USER = "root";
    static final String JDBC_PASS = "Toor123@";

    private static GenericObjectPool gPool = null;

    // This method create the Pool Object with the specified configuration
    public PoolingDataSource setUpPool() throws Exception {
        Class.forName(JDBC_DRIVER);

        // Creates an Instance of GenericObjectPool That Holds Our Pool of Connections Object!
        gPool = new GenericObjectPool();
        gPool.setMaxActive(5);

        // Creates a ConnectionFactory Object Which Will Be Use by the Pool to Create the Connection Object!
        ConnectionFactory cf = new DriverManagerConnectionFactory(JDBC_DB_URL, JDBC_USER, JDBC_PASS);

        /*
         * Creates a PoolableConnectionFactory That Will Wraps the Connection Object Created
         * by the ConnectionFactory to Add Object Pooling Functionality!
         */
        PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, gPool, null, null, false, true);
        return new PoolingDataSource(gPool);
    }

    // This method get the connection pool
    public GenericObjectPool getConnectionPool() {
        return gPool;
    }

    // This Method Is Used To Print The Connection Pool Status
    private void printDbStatus() {
        System.out.println("Max.: " + getConnectionPool().getMaxActive() + "; Active: " + getConnectionPool().getNumActive() + "; Idle: " + getConnectionPool().getNumIdle());
    }

    public static void main(String[] args) {
        ResultSet rsObj = null;
        Connection connObj = null;
        Connection connObj2 = null;
        PreparedStatement pstmtObj = null;
        PreparedStatement pstmtObj2 = null;
        // New connectionpool object
        ConnectionPool jdbcObj = new ConnectionPool();
        try {
            PoolingDataSource connectionPool = jdbcObj.setUpPool();
            jdbcObj.printDbStatus();

            // Performing Database Operation!
            System.out.println("\n=====Making A New Connection Object For Db Transaction=====\n");
            connObj = connectionPool.getConnection();
            connObj2 = connectionPool.getConnection();
            jdbcObj.printDbStatus(); // Printing the actual status Active:2 -  Idle:0

            pstmtObj = connObj.prepareStatement("SELECT * FROM Auto");
            rsObj = pstmtObj.executeQuery();
            while (rsObj.next()) {
                System.out.println(rsObj.getString("Brand")+"\t\t"+rsObj.getString("Nationality"));
            }
            connObj.close(); // Closing here we get 1 active and 1 in idle

            jdbcObj.printDbStatus(); // Printing the actual status Active:1 -  Idle:1

            pstmtObj2 = connObj2.prepareStatement("SELECT * FROM Auto WHERE Nationality='ITA'");
            rsObj = pstmtObj2.executeQuery();
            while (rsObj.next()) {
                System.out.println(rsObj.getString("Brand")+"\t\t"+rsObj.getString("Nationality"));
            }
            System.out.println("\n=====Releasing Connection Object To Pool=====\n");
        } catch (Exception sqlException) {
            sqlException.printStackTrace();
        } finally {
            try {
                // Closing ResultSet Object
                if (rsObj != null) {
                    rsObj.close();
                }
                // Closing PreparedStatement Object
                if (pstmtObj != null) {
                    pstmtObj.close();
                }
                if (pstmtObj2 != null) {
                    pstmtObj2.close();
                }
                // Closing Connection Object
                if (connObj != null) {
                    connObj.close();
                }
                if (connObj2 != null) {
                    connObj2.close();
                }
            } catch (Exception sqlException) {
                sqlException.printStackTrace();
            }
        }
        jdbcObj.printDbStatus();
    }
}

