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
public class IPV4AddressTest {
    IPv4Address address;
    
    @Test
    public void testToString(){
        try{
        
            this.address = new IPv4Address("127.0.0.1",IPv4Address.DEFAULT_MASK);
            assertEquals("127.0.0.1",this.address.toString());
        }
        catch(InvalidIPAddressException | IndexOutOfBoundsException | NumberFormatException e){
                       
        }
    }
    @Test
    public void testISReachable(){
        try{
            this.address = new IPv4Address("127.0.0.1",IPv4Address.DEFAULT_MASK);
            assertTrue(this.address.isReachable(1000, false));
        }
        catch(IndexOutOfBoundsException |InvalidIPAddressException |NumberFormatException |IOException
                |IllegalAccessError e){
        }
    }
}
