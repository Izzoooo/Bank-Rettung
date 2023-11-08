package com.vs.izzdin;

import java.sql.*;

public class Database{

    String jdbcURL = "jdbc:postgresql://localhost:5432/postgres"; //statt localhost db  für docker compose
    String username = "postgres";
    String password = "1234IzzoReng";
    Connection connection;
    public boolean connection(){
        try {
            connection = DriverManager.getConnection(jdbcURL,username,password);
            System.out.println("Connected to Database");
        } catch (SQLException e) {
            System.out.println("Error in Connection");
            throw new RuntimeException(e);
        }
        return true;
    }
    private static final String INSERT_SQL_Beliebig = "create table information("+
            "firstname varchar(20) NOT NULL," +
            "lastname varchar(20) NOT NULL);";
    private static final String ausgabe_SQL_Beliebig = "SELECT * FROM information;";

    private static final String INSERT_USERS_SQL = "INSERT INTO information" +
            "  (firstname, lastname) VALUES " +
            " (?, ?);";

    private static final String loeschen_SQL = "Delete FROM information *;";

    public void insertDataBeliebig(String firstname, String lastname) throws SQLException {
        if(connection != null){
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USERS_SQL);
                preparedStatement.setString(1, firstname);
                preparedStatement.setString(2, lastname);
                System.out.println(preparedStatement);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        connection.close();
    }

    public void createTabelBeliebig() throws SQLException {
        if (connection != null) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL_Beliebig);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            connection.close();

        }
    }
    public void DatenAusgebenBeliebig() throws SQLException {
        if(connection != null){
            try {
                Statement stm = connection.createStatement();
                ResultSet rs =stm.executeQuery(ausgabe_SQL_Beliebig);

                while(rs.next()){
                    System.out.print("Firstname: " + rs.getString(1));
                    System.out.println("  | Lastname: "+ rs.getString(2));
                }


            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        connection.close();
    }
    public void loeschen() throws SQLException {
        if (connection != null) {
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(loeschen_SQL);
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            connection.close();

        }
    }
}