package pingMyNetwork.model;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 *
 * @author holgus103
 * @version %I%
 */
public class IPv4Address{

    // Constants
    static final private int IP4_GROUPS = 4;

    // Private properties
    private int address;
    private int mask;

    /**
     *
     * @return Returns the mask of the IP address
     */
    public int getMask() {
        return this.mask;
    }

    /**
     *
     * @param ip IP address to initialize the object with
     * @param mask Mask of the IP address
     */
    public IPv4Address(String ip, int mask) {
        if(ip.charAt(0) == '/'){
            ip = ip.substring(1);
        }
        this.address = 0;
        this.mask = mask < 8 || mask > 32 ? 24 : mask;
        short address[] = new short[this.IP4_GROUPS];
        int endIndex;
        try {
            for (int i = 0; i < this.IP4_GROUPS; i++) {
                endIndex = ip.indexOf(".");
                if (endIndex < 0) {
                    address[i] = Short.parseShort(ip);
                    break;

                }
                address[i] = Short.parseShort(ip.substring(0, endIndex));
                ip = ip.substring(endIndex + 1);

            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println(e.getMessage());
        }
        for (int i = 0; i < this.IP4_GROUPS; i++) {
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

    /**
     *
     * @return Returns an ArrayList of Strings with valid IPs of the subnet
     */
    public ArrayList<IPv4Address> generateSubnetIPs() {
        ArrayList<IPv4Address> addressList = new ArrayList<IPv4Address>();
        int count = (int) Math.pow(2, 32 - this.mask);
        int prefix = this.address & 0xFFFFFFFF << (32 - mask);
        for (int i = 0; i < count; i++) {
            addressList.add(new IPv4Address(prefix | i, this.mask));
        }
        return addressList;
    }

    @Override
    public String toString() {
        String ret = "" + (this.address >> 24 & 0xFF);
        for (int i = 1; i < this.IP4_GROUPS; i++) {
            ret += ".";
            ret += this.address >> (24 - 8 * i) & 0xFF;
        }
        return ret;
    }
    
    /**
     *
     * @param timeout time the program waits (in ms) for a response
     * @return whether or not a IP is reachable
     */
    public boolean isReachable(int timeout){
        boolean ret = false;
        try{
            ret = InetAddress.getByName(this.toString()).isReachable(timeout);
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
        catch(IllegalArgumentException e){
            System.out.println(e.getMessage());
        }
        return ret;
    }

}
