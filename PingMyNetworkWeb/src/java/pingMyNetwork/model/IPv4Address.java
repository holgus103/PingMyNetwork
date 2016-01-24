package pingMyNetwork.model;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;
import pingMyNetwork.exception.InvalidIPAddressException;

/**
 *
 * @author Jakub Suchan
 * @version %I%, %G%
 * @since 1.0
 */
public class IPv4Address {

    // Private members
    /**
     * Bits per Byte
     */
    private static final int BITS_PER_BYTE = 8;
    /**
     * Number of bit groups in a IP v4 address
     */
    private static final int IP4_GROUPS = 4;
    /**
     * Number of bits in a v4 IPaddress
     */
    public static final int IPv4_BITS = 32;
    /**
     * Binary system base
     */
    public static final int BINARY_BASE = 2;
    /**
     * Max IP value
     */
    private static final int MAX_IP = 255;
    public static final String TABLE_NAME = "nodes";
    // Private properties
    /**
     * Raw address
     */
    private int address;
    private int mask;
    
    private void createTable(Connection con) throws ClassNotFoundException, SQLException {
        Statement statement = con.createStatement();
        statement.executeUpdate("CREATE TABLE " + IPv4Address.TABLE_NAME + "("
                + "nodeId INTEGER not null PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                + "scanId INTEGER, "
                + "address VARCHAR(15))");
    }

    /**
     * Returns the binary version of the IP address (on a 32-bit int)
     *
     * @return raw IP address (as int)
     */
    public int getRawIP() {
        return this.address;
    }

    /**
     * @param ip IP address to initialize the object with
     * @throws pingMyNetwork.exception.InvalidIPAddressException
     */
    public IPv4Address(String ip) throws NumberFormatException, IndexOutOfBoundsException, InvalidIPAddressException {
        if (ip.charAt(0) == '/') {
            ip = ip.substring(1);
        }
        this.address = 0;
        short addr[] = new short[IPv4Address.IP4_GROUPS];
        int endIndex;
        for (int i = 0; i < IPv4Address.IP4_GROUPS; i++) {
            endIndex = ip.indexOf(".");
            if (endIndex < 0) {
                addr[i] = Short.parseShort(ip);
                break;

            }
            addr[i] = Short.parseShort(ip.substring(0, endIndex));
            if (addr[i] > IPv4Address.MAX_IP || addr[i] < 0) {
                throw new InvalidIPAddressException("The provided IP address is invalid");
            }
            ip = ip.substring(endIndex + 1);

        }
        for (int i = 0; i < IPv4Address.IP4_GROUPS; i++) {
            this.address |= addr[i] << ((IPv4Address.IP4_GROUPS - 1) * IPv4Address.BITS_PER_BYTE - IPv4Address.BITS_PER_BYTE * i);
        }
    }

    /**
     *
     * @param ip IP address of the new object
     * @param mask Mask of the new IP address
     */
    public IPv4Address(int ip) {
        this.address = ip;
    }

    /**
     * Converts the IP to a string
     *
     * @return IP as String
     */
    @Override
    public String toString() {
        String ret = "" + (this.address >> (IPv4Address.IP4_GROUPS - 1) * IPv4Address.BITS_PER_BYTE & 0xFF);
        for (int i = 1; i < IPv4Address.IP4_GROUPS; i++) {
            ret += ".";
            ret += this.address >> ((IPv4Address.IP4_GROUPS - 1) * IPv4Address.BITS_PER_BYTE - IPv4Address.BITS_PER_BYTE * i) & 0xFF;
        }
        return ret;
    }

    /**
     *
     * @param timeout time the program waits (in ms) for a response
     * @return whether or not a IP is reachable
     * @throws java.io.IOException
     */
    public boolean isReachable(int timeout) throws IOException, IllegalAccessError {
        final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
        request.setHost(this.toString());
        request.setTimeout(timeout);
        final IcmpPingResponse response = IcmpPingUtil.executePingRequest(request);
        return response.getSuccessFlag();

    }

    public void save(Connection con, int scanID) throws ClassNotFoundException, SQLException {
        try{
            this.createTable(con);
        }
        catch(SQLException e){
            System.out.println("Table already exists");
        }
        Statement statement = con.createStatement();
        statement.executeUpdate("INSERT into " + IPv4Address.TABLE_NAME + "(scanID,address) values(" + scanID  + ",'" + this.toString()  + "')");
    }

    

}
