/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import org.junit.*;
import pingMyNetwork.model.IPv4Address;
import pingMyNetwork.model.Scan;
import static org.junit.Assert.*;
import pingMyNetwork.exception.InvalidIPAddressException;

/**
 *
 * @author Administrator
 */
public class ScanTest {

    private static final String CONNECTION_STRING = "jdbc:derby://localhost:1527/PingMyNetwork";
    private static final String DB_USER = "app";
    private static final String DB_PASSWORD = "password";
    private static final String TABLE_NAME = "scans";
    private static final String LOOPBACK = "127.0.0.1";
    private Scan scan;
    private Connection con;

    /**
     * Prepares the database connection
     */
    @Before
    public void setup() {
        try {
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            con = DriverManager.getConnection(CONNECTION_STRING, DB_USER, DB_PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Closes the database connection
     */
    @After
    public void teardown() {
        try {
            this.con.close();
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests the save methods without any related nodes
     */
    @Test
    public void testSave() {
        try {
            this.scan = new Scan(new Date(), 24, new IPv4Address(ScanTest.LOOPBACK));
            this.scan.save(this.con);
        } catch (ClassNotFoundException | SQLException | InvalidIPAddressException e) {
            fail(e.getMessage());
        }
    }
    
    /**
     * Tests the save method with related nodes
     */
    @Test
    public void testSaveWithNodes(){
        try{
            this.scan = new Scan(new Date(),29, new IPv4Address(ScanTest.LOOPBACK));
            scan.add(new IPv4Address(ScanTest.LOOPBACK));
            this.scan.save(this.con);
        }
        catch(InvalidIPAddressException | ClassNotFoundException | SQLException e){
            fail(e.getMessage());
        }
        
    }

    /**
     * Tests the indexing method
     */
    @Test
    public void testSelect() {
        try {
            ArrayList<Scan> asd = Scan.getIndex(this.con);
        } catch (SQLException | InvalidIPAddressException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Tests the get getNodes method
     */
    @Test
    public void testEmptyGetNodes() {
        try {
            this.scan = new Scan(new Date(), 30, new IPv4Address(ScanTest.LOOPBACK));
            this.scan.getOnlineNodes();
        } catch(InvalidIPAddressException e){
            fail(e.getMessage());
        }  
    }
    
}
