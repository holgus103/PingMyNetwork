package pingMyNetwork.test;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import pingMyNetwork.exception.InvalidIPAddressException;
import pingMyNetwork.model.IPv4Address;
import static org.junit.Assert.*;
import org.junit.*;
/**
 *
 * @author Jakub
 */
public class IPv4AddressTest {
    private static final String LOOPBACK = "127.0.0.1";
    private static final String RESCRICTED_IP = "0.0.0.0";
    private static final String MAX_IP = "255.255.255.255";
    private static final String INVALID_IP = "512.512.512.512";
    private static final String GOOGLE_DNS = "8.8.8.8";
    private static final String CONNECTION_STRING = "jdbc:derby://localhost:1527/PingMyNetwork";
    private static final String DB_USER = "app";
    private static final String DB_PASSWORD = "password";
    private static final String TABLE_NAME = "nodes";
    IPv4Address address;
    
    /**
     * Tests the toString method of IPV4Address
     */
    @Test
    public void testToString(){
        try{
            this.address = new IPv4Address(this.LOOPBACK);
            assertEquals(this.LOOPBACK,this.address.toString());
        }
        catch(InvalidIPAddressException | IndexOutOfBoundsException | NumberFormatException e){
            fail("Test failed");           
        }
        
        try{
            this.address = new IPv4Address(RESCRICTED_IP);
            assertEquals(RESCRICTED_IP,this.address.toString());
        }
        catch(InvalidIPAddressException | IndexOutOfBoundsException | NumberFormatException e){
            fail("Test failed");           
        }
        
        try{
            this.address = new IPv4Address(this.MAX_IP);
            assertEquals(MAX_IP,this.address.toString());
        }
        catch(InvalidIPAddressException | IndexOutOfBoundsException | NumberFormatException e){
            fail("Test failed");           
        }
        
        try{
            this.address = new IPv4Address(this.INVALID_IP);
            fail("Test failed");
        }
        catch(InvalidIPAddressException | IndexOutOfBoundsException | NumberFormatException e){         
        }
    }

    /** 
     * Tests the isReachable method
     */
    @Test
    public void testIsReachable(){
        try{
            this.address = new IPv4Address(this.LOOPBACK);
            assertTrue(this.address.isReachable(1000));
        }
        catch(IndexOutOfBoundsException |InvalidIPAddressException |NumberFormatException |IOException
                |IllegalAccessError e){
        }
        
        try{
            this.address = new IPv4Address(this.GOOGLE_DNS);
            assertTrue(this.address.isReachable(1000));
        }
        catch(IndexOutOfBoundsException |InvalidIPAddressException |NumberFormatException |IOException
                |IllegalAccessError e){
        }
        try{
            this.address = new IPv4Address(this.RESCRICTED_IP);
            assertFalse(this.address.isReachable(1000));
        }
        catch(IndexOutOfBoundsException |InvalidIPAddressException |NumberFormatException |IOException
                |IllegalAccessError e){
        }
    }  
        
    @Test
    public void testSave(){
        Connection con = null;
        try{
            Class.forName("org.apache.derby.jdbc.ClientDriver");
            con = DriverManager.getConnection(CONNECTION_STRING, DB_USER, DB_PASSWORD);
            Statement statement = con.createStatement();
            try{
                statement.executeUpdate("Delete from " + TABLE_NAME + " where address = '" + IPv4AddressTest.LOOPBACK +"' and scanID = 0");
            }
            catch(SQLException e){
                System.out.println("Record did not exist");
            }
            this.address = new IPv4Address(IPv4AddressTest.LOOPBACK);
            this.address.save(con,0);

            ResultSet res = statement.executeQuery("Select * from " + TABLE_NAME + " where address = '" + IPv4AddressTest.LOOPBACK +"' and scanID = 0");
            if(!res.next()){
                fail("Record not added");
            }
            else{
                statement.executeUpdate("Delete from " + TABLE_NAME + " where address = '" + IPv4AddressTest.LOOPBACK+"' and scanID = 0"); 
            }

        }    
        catch(ClassNotFoundException | IndexOutOfBoundsException |
                InvalidIPAddressException | NumberFormatException | SQLException e){
            fail(e.getMessage());
        }
        finally {
            if (con != null) {
                try{
                    con.close();
                }
                catch(SQLException e){
                    
                }
            }
        }
    }
}
