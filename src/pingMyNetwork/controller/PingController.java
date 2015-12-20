package pingMyNetwork.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import javax.swing.SwingWorker;
import pingMyNetwork.enums.Flags;
import pingMyNetwork.exception.InvalidIPAddressException;
import pingMyNetwork.model.*;

/**
 *
 * @author Jakub Suchan
 * @version %I%, %G%
 * @since 1.0
 */
public class PingController {
    
    private static final int PORT = 9999;
    /**
     * Blocks multiple discoveries at a time
     */
    private boolean isDiscoveryRunning;
    private long lastRefresh;
    /**
     * Array of current machine's IPs
     */
    private ArrayList<IPv4Address> ips;
    /**
     * Subnet IPs
     */
    private List<IPv4Address> ipsLeft = new ArrayList<>();
    private List<IPv4Address> onlineIPs = new ArrayList<>();
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
    private class SingleService extends SwingWorker<Void, Void> {

        /**
         *
         */
        private static final int handShakeVal = 13378888;
        private Socket clientSocket;
        private ObjectInputStream in;
        private ObjectOutputStream out;
        
        private boolean handShake() {
            try {
                this.out.writeInt(SingleService.handShakeVal);
                this.out.flush();
                if (SingleService.handShakeVal == this.in.readInt()) {
                    return true;
                }
                return false;
            } catch (IOException e) {
                return false;
            }
        }

        /**
         * The constructor of instance of the SingleService class. Use the
         * socket as a parameter.
         *
         * @param socket socket representing connection to the client
         */
        public SingleService(Socket socket) throws IOException {
            this.clientSocket = socket;
            this.out = new ObjectOutputStream(new BufferedOutputStream(this.clientSocket.getOutputStream()));
            this.out.flush();
            this.in = new ObjectInputStream(new BufferedInputStream(this.clientSocket.getInputStream()));
        }
        
        @Override
        public void done() {
            super.done(); //To change body of generated methods, choose Tools | Templates.
        }
        
        @Override
        public Void doInBackground() {
            boolean isOnline = true;
            while (isOnline) {
                try {
                    String command = this.in.readUTF();
                    Flags f = Flags.valueOf(command);
                    switch (f) {
                        case LIST_FLAG:
                            if (this.handShake()) {
                                this.out.writeObject(ips);
                                this.out.flush();
                            } else {
                                isOnline = false;
                            }
                            break;
                        case PING_FLAG:
                            if (!this.handShake()) {
                                isOnline = false;
                                break;
                            }
                            if (((lastRefresh == 0 || lastRefresh - System.currentTimeMillis() / 1000L > 10000)) && !isDiscoveryRunning) {
                                isDiscoveryRunning = true;
                                int id = in.readInt();
                                int timeout = in.readInt();
                                ping(id, timeout, this.out);
                            } else {
                                while (isDiscoveryRunning) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        
                                    }
                                    
                                }
                            }
                            this.out.writeObject(onlineIPs);
                            this.out.flush();
                        case EXIT_FLAG:
                            if (this.handShake()) {
                                isOnline = false;
                            } else {
                                isOnline = false;
                            }
                            break;
                        
                    }
                } catch (IOException e) {
                    logException(e);
                }
            }
            return null;
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
        private ObjectOutputStream out;

        /**
         * Worker constructor
         *
         * @param ip IP to ping
         * @param t timeout
         * @param ipCounter Counter to be incremented
         */
        public Worker(IPv4Address ip, int t, Integer ipCounter, ObjectOutputStream out) {
            this.out = out;
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
                    try {
                        out.writeObject(ip);
                        out.flush();
                    } catch (IOException e) {
                        logException(e);
                    }
                    onlineIPs.add(ip);
                }
                if (ipsLeft.size() > 0) {
                    nextIp(this.timeout, this.ipCounter, out);
                } else {
                    try {
                        out.writeObject(null);
                        out.flush();
                    } catch (IOException e) {
                        logException(e);
                    }
                    isDiscoveryRunning = false;
                    lastRefresh = System.currentTimeMillis() / 1000L;
                }
            } catch (InterruptedException | ExecutionException ex) {
                logException(ex);
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
        this.ips = this.getLocalIPs();
    }

    /**
     * Initializes the pinging of the next free IP address
     *
     * @param timeout timeout
     * @param ipCounter Counter to be incremented
     */
    private void nextIp(int timeout, Integer ipCounter, ObjectOutputStream out) {
        if (ipsLeft.size() > 0) {
            IPv4Address ip = ipsLeft.remove(0);
            new Worker(ip, timeout, ipCounter, out).execute();
        }
    }

    /**
     *
     *
     * @throws java.io.IOException
     */
    public void run() throws IOException {
        ServerSocket server = new ServerSocket(PingController.PORT);
        while (true) {
            new SingleService(server.accept()).execute();
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
    private void ping(int i, int sec, ObjectOutputStream out) {
        this.isDiscoveryRunning = true;
        Integer ipCounter = 0;
        try {
            ipsLeft = this.getSubnetIPs(this.ips.get(i));
            nextIp(sec, ipCounter, out);
        } catch (IndexOutOfBoundsException e) {
            this.logException(e);
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
            this.logException(e);
        }
        return validIPs;
    }
    
    private void logException(Throwable e) {
        Logger.getLogger(PingController.class.getName()).log(Level.SEVERE, null, e);
    }
}
