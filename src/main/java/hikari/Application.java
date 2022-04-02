package hikari;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;

public class Application {
    // Substitute for Connection object
    private static HikariDataSource dataSource;

    /**
     * Initializing the connection to the database
     */
    private static void initDatabaseConnectionPool(){
        System.out.println("CONNECTING TO THE DATABASE...");
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/JDBC?");
        dataSource.setUsername("root");
        dataSource.setPassword("Toor123@");
    }

    /**
     * Closing the connection to the database
     */
    private static void closeDatabaseConnectionPool() {
        dataSource.close();
    }

    // Insert new data into the database
    public static void insertData() throws SQLException {
        Connection connection = dataSource.getConnection();
        String query = "INSERT INTO Auto(Brand, Nationality) VALUES ('Leff', 'FRA')";
        try(PreparedStatement ps = connection.prepareStatement(query)) {
            ps.executeUpdate();
        }
        connection.close();
    }

    public static void printData() throws SQLException {

        String query = "SELECT * FROM Auto";
        // This try takes care automatically to close the connection at the end
        try(Connection connection = dataSource.getConnection()) {
            try(Statement st = connection.createStatement()) {
                ResultSet rs = st.executeQuery(query);
                while(rs.next()) {
                    String brand = rs.getString("Brand");
                    String nationality = rs.getString("Nationality");
                    System.out.println(brand+"\t\t"+nationality);
                }
            }
        }
    }

    public static void main(String[] args) throws SQLException {
        initDatabaseConnectionPool();
        insertData();
        closeDatabaseConnectionPool();
        initDatabaseConnectionPool();
        printData();
        closeDatabaseConnectionPool();
    }
}
