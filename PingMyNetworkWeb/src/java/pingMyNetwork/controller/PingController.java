package pingMyNetwork.controller;

import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
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
    private ArrayList<IPv4Address> onlineNodes = new ArrayList<IPv4Address>();
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

        // Private properties

        /**
         * The IP this thread is going to ping
         */
        private final IPv4Address ip;
        /**
         * Ping timeout
         */
        private final int timeout;
        /**
         * Counter used to determine the amount of discovered IPs
         */
        private ArrayList<IPv4Address> output;

        /**
         * Worker constructor
         *
         * @param ip IP to ping
         * @param t timeout
         * @param ipCounter Counter to be incremented
         */
        public Worker(IPv4Address ip, int t) {
            this.ip = ip;
            this.timeout = t;            
        }

        /**
         * Method for background pinging
         *
         * @return IP that was being pinged
         * @throws Exception
         */
        @Override
        public Void doInBackground() throws Exception {
            for(IPv4Address value:getSubnetIPs(ip)){
                if(value.isReachable(timeout))
                    onlineNodes.add(value);
            }
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
     * Method generating all IPs of a subnet.
     *
     * @return a set of IPs from the machine's subnet
     */
    private ArrayList<IPv4Address> getSubnetIPs(IPv4Address address) {
        ArrayList<IPv4Address> addressList = new ArrayList<>();
        int count = (int) Math.pow(IPv4Address.BINARY_BASE, IPv4Address.IPv4_BITS - address.getMask());
        int prefix = address.getRawIP() & 0xFFFFFFFF << (IPv4Address.IPv4_BITS - address.getMask());
        for (int i = 0; i < count; i++) {
            addressList.add(new IPv4Address(prefix | i, address.getMask()));
        }
        return addressList;
    }

    /**
     * The constructor initializes the array of references to threads and
     * creates a view object.
     * @throws pingMyNetwork.exception.InvalidIPAddressException
     * @throws java.net.SocketException
     * @throws java.lang.IndexOutOfBoundsException
     * @throws java.lang.NumberFormatException
     */
    public PingController() throws IndexOutOfBoundsException, InvalidIPAddressException, NumberFormatException, SocketException{
        this.ips = this.getLocalIPs();
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
        this.isDiscoveryRunning = true;
                new Worker(ip,sec).execute();

    }

    /**
     *
     * @return Whether the discovery is still running
     */
    public boolean isDiscoveryRunning(){
        return this.isDiscoveryRunning;
    }

    /**
     *
     * @return ArrayList of online IPs
     */
    public ArrayList<IPv4Address> getResults(){
        return this.onlineNodes;
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
    public ArrayList<IPv4Address> getLocalIPs() throws IndexOutOfBoundsException, InvalidIPAddressException, NumberFormatException, SocketException{
        ArrayList<IPv4Address> validIPs = new ArrayList<>();
            Enumeration<NetworkInterface> netInts = NetworkInterface.getNetworkInterfaces();
            while (netInts.hasMoreElements()) {
                List<InterfaceAddress> intAddrs = netInts.nextElement().getInterfaceAddresses();
                for (InterfaceAddress value : intAddrs) {
                    // Check if IP is IPv4 or loopback
                    if (value.getAddress() instanceof Inet4Address && !value.getAddress().isLoopbackAddress()) {
                        validIPs.add(new IPv4Address(value.getAddress().toString(), value.getNetworkPrefixLength()));
                    }
                }
            }
        return validIPs;
    }
    
}
