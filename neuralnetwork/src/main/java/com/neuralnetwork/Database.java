package com.neuralnetwork;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.google.gson.Gson; 

// manages h2 database connection and network storage operations
public class Database {

    public static final String closeDatabase = null;
    private static Connection connection;
    private static int Network;
    private static Database database;


    public final String sql = """
            CREATE TABLE IF NOT EXISTS network (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    architecture VARCHAR(255),
    sparsity INT,
    loops_through INT,
    learning_rate DOUBLE,
    network_json CLOB,
    accuracy DOUBLE
);
            """;

    // retrievs the connection to the database
    public static Connection findConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:file:./neuralnetwork/database2;AUTO_SERVER=TRUE", "sa", "");
    }

    // method that checks the application has made a connection with the database
    public void checkConnection(Connection connection) throws SQLException{
        System.out.println(connection.isValid(0));
    }

    // adding a network to the network database and then obtaining the network ID
    public void addNetwork(Connection connection, Network net, String name, String arch, int sparsity, int loops, double learning_rate, double accuracy) throws SQLException{
        // turning network object into json string
        Gson gson = new Gson();
        String json = gson.toJson(net);
        String sql = """
                        INSERT INTO network (
                        name, architecture, sparsity, loops_through, learning_rate, network_json, accuracy) 
                        VALUES (?, ?, ?, ?, ?, ?, ?)""";
        
        // entering table values
        PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        stmt.setString(1, name);
        stmt.setString(2, arch);
        stmt.setInt(3, sparsity);
        stmt.setInt(4, loops);
        stmt.setDouble(5, learning_rate);
        stmt.setString(6, json);
        stmt.setDouble(7, accuracy);
        stmt.executeUpdate();

        // Get the generated ID
        ResultSet keys = stmt.getGeneratedKeys();
        int id = -1;

        if (keys.next()) {
            id = keys.getInt(1);   // column 1 = generated ID
            setNetwork(id);
        }
        stmt.close();
                
    }

    // deletes a network from database by its id
    public static boolean deleteNetworkById(int id){

        String sql = "DELETE FROM network WHERE id = ?";

        try(PreparedStatement stmt = getConnection().prepareStatement(sql)){
            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    // initializes database connection and creates network table if it doesn't exist
    public static void sortConnection() throws SQLException{
        try {
            database = new Database();
            connection = findConnection();
            database.checkConnection(connection);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute(database.sql);
            }
        } catch (SQLException e) {
            throw e;
        }
    }
    // gets the id of the currently selected network
    public static int getNetwork(){
        return Network;
    }
    
    //retrieve database
    public static Database getDatabase() {
        return database;
    }


    public static Connection getConnection(){
        return connection;
    }
    // a way to set the id of the neural network aboout to be used
    public static void setNetwork(int id){
        Network = id;
    }

    // closes the database connection when the application exits
    public static void closeDatabase() throws SQLException{
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
