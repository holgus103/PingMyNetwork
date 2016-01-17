package pingMyNetwork.test;
import java.io.IOException;
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
}
