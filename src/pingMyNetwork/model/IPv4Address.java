package pingMyNetwork.model;

import java.io.IOException;
import java.net.InetAddress;
import pingMyNetwork.exception.InvalidIPAddressException;

/**
 *
 * @author holgus103
 * @version %I%
 */
public class IPv4Address{
    // Private members
    // Constants
    static final private int IP4_GROUPS = 4;
    // Private properties
    private int address;
    private int mask;
    // Public members
    // Public methods
    /**
     * @return Returns the mask of the IP address
     */
    public int getMask() {
        return this.mask;
    }
    
    /**
     * Returns the binary version of the IP address (on a 32-bit int)
     * @return raw IP address (as int)
     */
    public int getRawIP(){
        return this.address;
    }
    /**
     *
     * @param ip IP address to initialize the object with
     * @param mask Mask of the IP address
     * @throws pingMyNetwork.exception.InvalidIPAddressException
     */
    public IPv4Address(String ip, int mask) throws NumberFormatException,IndexOutOfBoundsException, InvalidIPAddressException  {
        if(ip.charAt(0) == '/'){
            ip = ip.substring(1);
        }
        this.address = 0;
        this.mask = mask < 8 || mask > 32 ? 24 : mask;
        short address[] = new short[IPv4Address.IP4_GROUPS];
        int endIndex;
            for (int i = 0; i < IPv4Address.IP4_GROUPS; i++) {
                endIndex = ip.indexOf(".");
                if (endIndex < 0) {
                    address[i] = Short.parseShort(ip);
                    break;

                }
                address[i] = Short.parseShort(ip.substring(0, endIndex));
                if(address[i]>255){
                    throw new InvalidIPAddressException("The provided IP address is invalid");
                }
                ip = ip.substring(endIndex + 1);

            }
        for (int i = 0; i < IPv4Address.IP4_GROUPS; i++) {
            this.address |= address[i] << (24 - 8 * i);
        }
    }

    /**
     *
     * @param ip IP address of the new object
     * @param mask Mask of the new IP address
     */
    public IPv4Address(int ip, int mask) {
        this.address = ip;
        this.mask = mask;
    }

    @Override
    public String toString() {
        String ret = "" + (this.address >> 24 & 0xFF);
        for (int i = 1; i < IPv4Address.IP4_GROUPS; i++) {
            ret += ".";
            ret += this.address >> (24 - 8 * i) & 0xFF;
        }
        return ret;
    }
    
    /**
     *
     * @param timeout time the program waits (in ms) for a response
     * @return whether or not a IP is reachable
     * @throws java.io.IOException
     */
    public boolean isReachable(int timeout) throws IOException,IllegalAccessError{
            return InetAddress.getByName(this.toString()).isReachable(timeout);

    }

}
