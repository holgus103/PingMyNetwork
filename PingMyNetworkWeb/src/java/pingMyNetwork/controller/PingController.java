package pingMyNetwork.controller;

import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import javax.swing.SwingWorker;
import pingMyNetwork.exception.InvalidIPAddressException;
import pingMyNetwork.model.*;

/**
 *
 * @author Jakub Suchan
 * @version %I%, %G%
 * @since 1.0
 */
public class PingController {
    /**
     * The default timeout used for pinging
     */
    private static final int DEFAULT_TIMEOUT = 1000;
    /**
     * Array of current machine's IPs
     */
    private ArrayList<IPv4Address> ips;
    /**
     * Subnet IPs
     */
    private Scan scan;
    /**
     * Database connection
     */
    private Connection con;
    /**
     * Blocks multiple discoveries at a time
     */
    private boolean isDiscoveryRunning;
    /**
     * Private class used for pinging asynchronously
     *
     * @author Jakub Suchan
     * @version %I%, %G%
     * @since 1.0
     */
    private class Worker extends SwingWorker<Void, Void> {
        /**
         * Ping timeout
         */
        private final int timeout;
        /**
         * Worker constructor
         * @param ip IP to ping
         * @param t timeout
         */
        public Worker(int t) {
            this.timeout = t;            
        }

        /**
         * Method for background pinging
         * @return IP that was being pinged
         * @throws Exception
         */
        @Override
        public Void doInBackground() throws Exception {
            for(IPv4Address value:scan.getSubnetIPs()){
                if(value.isReachable(timeout))
                    scan.add(value);
            }
            scan.save(con);
            return null;
        }

        /**
         * Methods to execute after doInBackground has ended
         */
        @Override
        public void done() {
            isDiscoveryRunning = false;
        }
    }

    /**
     * The constructor initializes the array of references to threads and
     * creates a view object.
     * @param connectionString
     * @param dbUser
     * @param dbPassword
     * @throws java.lang.ClassNotFoundException
     * @throws java.sql.SQLException
     * @throws pingMyNetwork.exception.InvalidIPAddressException
     * @throws java.net.SocketException
     */
    public PingController(String connectionString, String dbUser, String dbPassword) throws ClassNotFoundException, SQLException, SocketException,                                                                                   InvalidIPAddressException{
        this.ips = this.getLocalIPs();
        Class.forName("org.apache.derby.jdbc.ClientDriver");
        this.con = DriverManager.getConnection(connectionString,dbUser,dbPassword);
    }
    
    /**
     * Closes the database connection
     * @throws Throwable 
     */
    @Override
    protected void finalize() throws Throwable {
        this.con.close();
        super.finalize();
    }

    
    /**
     * Method exploring the network and pining all subnet IPs, the IPs that are
     * found online will be displayer in real-time in the view that's been
     * initialized within this class as menu. By default the method will use the
     * first interface and a timeout of 1000 ms.
     *
     * @param ip interface used for pinging
     * @param sec pinging timeout
     */
    public void ping(IPv4Address ip, int sec) {
        this.scan = new Scan(new Date(),ip.getMask(), ip);
        this.isDiscoveryRunning = true;
                new Worker(sec).execute();

    }

    /**
     * Checks if there is a discovery running
     * @return Whether the discovery is still running
     */
    public boolean isDiscoveryRunning(){
        return this.isDiscoveryRunning;
    }

    /**
     * Returns an array of online nodes from the most recent scan
     * @return ArrayList of online IPs
     * @throws java.sql.SQLException
     * @throws pingMyNetwork.exception.InvalidIPAddressException
     */
    public ArrayList<IPv4Address> getResults() throws SQLException, InvalidIPAddressException{
        return this.scan.getOnlineNodes();
    }
    
    /**
     * Returns an array of Scan objects representing the past network scans
     * @return Array of Scan objects
     * @throws SQLException
     * @throws InvalidIPAddressException
     */
    public ArrayList<Scan> getScanIndex() throws SQLException,InvalidIPAddressException{
        return Scan.getIndex(con);
    }
        
    /**
     * Method fetching all IPs of the current machine
     *
     * @return machine's IPs
     * @throws pingMyNetwork.exception.InvalidIPAddressException
     * @throws java.net.SocketException
     * @throws java.lang.IndexOutOfBoundsException
     * @throws java.lang.NumberFormatException
     */
    public ArrayList<IPv4Address> getLocalIPs() throws InvalidIPAddressException, SocketException{
        ArrayList<IPv4Address> validIPs = new ArrayList<IPv4Address>();
            Enumeration<NetworkInterface> netInts = NetworkInterface.getNetworkInterfaces();
            while (netInts.hasMoreElements()) {
                List<InterfaceAddress> intAddrs = netInts.nextElement().getInterfaceAddresses();
                for (InterfaceAddress value : intAddrs) {
                    // Check if IP is IPv4 or loopback
                    if (value.getAddress() instanceof Inet4Address && !value.getAddress().isLoopbackAddress()) {
                        validIPs.add(new IPv4Address(value.getAddress().toString(),value.getNetworkPrefixLength()));
                    }
                }
            }
        return validIPs;
    }
    
    /**
     * Loads a Scan object by id from the database
     * @param id scan id
     * @return Scan object
     * @throws SQLException
     * @throws InvalidIPAddressException
     */
    public Scan loadScan(int id) throws SQLException, InvalidIPAddressException{
        return new Scan(con, id);
    }
    
}
 