/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 *
 */
public abstract class PingMyNetworkModel {

    protected static final String CONNECTION_STRING = "jdbc:derby://localhost:1527/PingMyNetwork";
    protected static final String DB_USER = "app";
    protected static final String DB_PASSWORD = "password";  
    protected final void executeSQL(String sql) throws SQLException, ClassNotFoundException{
        Connection con = null;
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            con = DriverManager.getConnection(CONNECTION_STRING, DB_USER, DB_PASSWORD);
            Statement statement = con.createStatement();
            statement.executeUpdate(sql);
        } finally {
            if (con != null) {
                try{
                con.close();
                }
                catch(SQLException e){
                    
                }
            }
        }
    }
    protected abstract void createTable() throws ClassNotFoundException, SQLException;
    protected abstract void save() throws ClassNotFoundException, SQLException;
    protected abstract void loadByID(int id);
}
