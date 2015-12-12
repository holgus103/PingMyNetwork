package pingMyNetwork.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import pingMyNetwork.exception.InvalidIPAddressException;
import pingMyNetwork.model.*;
import pingMyNetwork.view.*;
import pingMyNetwork.enums.Flags;

/**
 *
 * @author Jakub Suchan
 * @version %I%, %G%
 * @since 1.0
 */
public class PingController {

    /**
     * The default interface used for pinging
     */
    private static final int DEFAULT_INTERFACE = 0;
    /**
     * The default timeout used for pinging
     */
    private static final int DEFAULT_TIMEOUT = 1000;

    /**
     * Array of current machine's IPs
     */
    private ArrayList<IPv4Address> ips;
    /**
     * Selected interface
     */
    private int currentInterfaceId;
    /**
     * Subnet IPs
     */
    private List<IPv4Address> ipsLeft = new ArrayList<>();
    /**
     * Blocks multiple discoveries at a time
     */
    private boolean isDiscoveryRunning;
    /**
     * ServerSocket for accepting clients
     */
    private ServerSocket servSocket;

    /**
     * Private class used for client support
     *
     * @author Jakub Suchan
     * @version %I%, %G%
     * @since 1.0
     */
    private class SingleService extends SwingWorker<String, Void> {

        /**
         *
         */
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;

        /**
         * The constructor of instance of the SingleService class. Use the
         * socket as a parameter.
         *
         * @param socket socket representing connection to the client
         */
        public SingleService(Socket socket) throws IOException {
            this.clientSocket = socket;
            out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    socket.getOutputStream())), true);
            in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));
        }   
        @Override
        public void done() {
            super.done(); //To change body of generated methods, choose Tools | Templates.
        }
        @Override
        public String doInBackground(){
            return "asd";
        }

    }

    /**
     * Private class used for pinging asynchronously
     *
     * @author Jakub Suchan
     * @version %I%, %G%
     * @since 1.0
     */
    private class Worker extends SwingWorker<IPv4Address, Void> {

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
        private Integer ipCounter;

        /**
         * Worker constructor
         *
         * @param ip IP to ping
         * @param t timeout
         * @param ipCounter Counter to be incremented
         */
        public Worker(IPv4Address ip, int t, Integer ipCounter) {
            this.ip = ip;
            this.timeout = t;
            this.ipCounter = ipCounter;
        }

        /**
         * Method for background pinging
         *
         * @return IP that was being pinged
         * @throws Exception
         */
        @Override
        public IPv4Address doInBackground() throws Exception {
            if (this.ip.isReachable(this.timeout)) {
                return this.ip;
            } else {
                return null;
            }
        }

        /**
         * Methods to execute after doInBackground has ended
         */
        @Override
        public void done() {
            try {
                IPv4Address ip = this.get();
                if (ip != null) {
//                    menu.displayIP(ip);
                }
                if (ipsLeft.size() > 0) {
                    nextIp(this.timeout, this.ipCounter);
                } else {
                    isDiscoveryRunning = false;
//                    menu.renderEnd(this.ipCounter);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(PingController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(PingController.class.getName()).log(Level.SEVERE, null, ex);
            }
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
     */
    public PingController() {
        //this.menu = new ConsoleOutput();
        this.ips = this.getLocalIPs();
    }

    /**
     * Initializes the pinging of the next free IP address
     *
     * @param timeout timeout
     * @param ipCounter Counter to be incremented
     */
    private void nextIp(int timeout, Integer ipCounter) {
        if (ipsLeft.size() > 0) {
            IPv4Address ip = ipsLeft.remove(0);
            new Worker(ip, timeout, ipCounter).execute();
        }
    }

    /**
     * Method exploring the network and pining all subnet IPs, the IPs that are
     * found online will be displayer in real-time in the view that's been
     * initialized within this class as menu. By default the method will use the
     * first interface and a timeout of 1000 ms.
     *
     * @param i no of the interface used for pinging
     * @param sec pinging timeout
     */
    private void ping(int i, int sec) {
        this.isDiscoveryRunning = true;
        Integer ipCounter = 0;
        menu.renderInit(this.ips.get(i));
        if (!this.isCLISession) {
            try {
                ipsLeft = this.getSubnetIPs(this.ips.get(i));
                nextIp(sec, ipCounter);
            } catch (IndexOutOfBoundsException e) {
                menu.renderException(e);
            }
        } else {
            for (IPv4Address value : this.getSubnetIPs(this.ips.get(i))) {
                try {
                    if (value.isReachable(sec)) {
                        menu.displayIP(value);
                        ipCounter++;
                    }
                } catch (IOException e) {
                    menu.renderException(e);
                }
            }
            menu.renderEnd(ipCounter);
        }

    }

    /**
     * Method fetching all IPs of the current machine
     *
     * @return machine's IPs
     */
    private ArrayList<IPv4Address> getLocalIPs() {
        ArrayList<IPv4Address> validIPs = new ArrayList<>();
        try {
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
        } catch (SocketException | NumberFormatException | IndexOutOfBoundsException | InvalidIPAddressException e) {
            menu.renderException(e);
        }
        return validIPs;
    }
}
