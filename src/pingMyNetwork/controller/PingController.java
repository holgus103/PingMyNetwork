package pingMyNetwork.controller;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import pingMyNetwork.exception.InvalidIPAddressException;
import pingMyNetwork.model.*;
import pingMyNetwork.view.*;

/**
 *
 * @author holgus103
 * @version %I%
 */
public class PingController {
    
    /**
     * The maximum number of threads used for network discovery
     */
    private static final int MAX_THREADS = 64;
    /**
     * Array of references to the above mentioned threads
     */
    private final Thread[] threads;
    /**
     * Reference to this controllers view
     */
    private final ConsoleOutput menu;
    /**
     * Array of current machine's IPs
     */
    private final ArrayList<IPv4Address> ips;
    /**
     * Private class used for pinging asynchronously
     * @author holgus103
     * @version %I%
     */

    private class PingIP extends Thread {

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
         * Thread constructor
         * @param ip IP the thread is going to ping
         * @param timeout pinging timeout
         * @param ipCounter counter of discovered IPs
         */
        PingIP(IPv4Address ip, int timeout, Integer ipCounter) {
            this.ip = ip;
            this.timeout = timeout;
            this.ipCounter = ipCounter;
        }
        
        /**
         * Thread's main method that checks for IP availability.
         * If the current IP is found online the global counter will be increased
         * and the IP address rendered in the view right away. 
         */
        @Override
        public void run() {
            try{
            if (this.ip.isReachable(timeout)) {
                this.ipCounter++;
                menu.displayIP(ip);
            }
            }
            catch(IOException | IllegalAccessError e){
                menu.renderException(e);
            }
        }
    }

    /**
     * The constructor initializes the array of references to threads and creates a view object.
     */
    public PingController() {
        this.menu = new ConsoleOutput();
        this.ips = this.getLocalIPs();
        this.threads = new Thread[PingController.MAX_THREADS];
    }

    /**
     *  Main method of the controller that analyzes user input and fires up the
     *  corresponding methods.
     * @param args
     */
    public void run(String[] args) {
        switch (args.length) {
            case 1:
                if (args.length > 0) {
                    switch (args[0]) {
                        case "-h":
                            menu.renderHelp();
                            break;
                        case "-l":
                            menu.renderInterfaces(this.ips);
                            break;
                        case "-p":
                            this.ping();
                            break;
                        default:
                            menu.renderArgsError();

                    }
                }
                break;
            case 2:
                if (args[0].equals("-p")) {
                    try {
                        this.ping(Integer.parseInt(args[1]));
                    } catch (NumberFormatException e) {
                        menu.renderException(e);
                    }
                } else {
                    menu.renderArgsError();
                }
                break;
            case 3:
                if (args[0].equals("-p")) {
                    if (args[1].equals("-t")) {
                        try {
                            this.ping(0, Integer.parseInt(args[1]));
                        } catch (NumberFormatException e) {
                            menu.renderException(e);
                        }
                    } else {
                        menu.renderArgsError();
                    }
                } else {
                    menu.renderArgsError();
                }
                break;
            case 4:
                if (args[0].equals("-p")) {
                    if (args[2].equals("-t")) {
                        try {
                            this.ping(Integer.parseInt(args[3]), Integer.parseInt(args[1]));
                        } catch (NumberFormatException e) {
                            menu.renderException(e);
                        }
                    } else {
                        menu.renderArgsError();
                    }
                } else {
                    menu.renderArgsError();
                }
                break;
            default:
                menu.renderArgsError();
        }
    }
        /**
         * Method exploring the network and pining all subnet IPs,
         * the IPs that are found online will be displayer in real-time 
         * in the view that's been initialized within this class as menu.
         * By default the method will use the first interface and
         * a timeout of1000 ms.
         * @param args array of parameters:
         * 0 - network interface id
         * 1 - timeout for pinging
         * @return count of all found IPs
         */
    public int ping(int... args) {
        // Default parameters 
        int i = 0, sec = 1000, threadCounter = 0;
        Integer ipCounter = 0;
        // Check if params were supplied
        if (args.length > 0) {
            i = args[0];
            if (args.length > 1) {
                sec = args[1];
            }
        }
        try {
            for (IPv4Address value : this.getSubnetIPs(this.ips.get(i))) {
                if (threadCounter < PingController.MAX_THREADS) {
                    this.threads[threadCounter] = new PingIP(value, sec, ipCounter);
                    this.threads[threadCounter].start();
                    threadCounter++;
                } else {
                    threadCounter = 0;
                    for (Thread worker : this.threads) {
                        try {
                            worker.join();
                        } catch (InterruptedException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            menu.renderException(e);
        }
        return ipCounter;

    }

    /**
     * Method fetching all IPs of the current machine
     * @return machine's  IPs
     */
    public ArrayList<IPv4Address> getLocalIPs() {
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

    /**
     * Method generating all IPs of a subnet.
     * @return a set of IPs from the machine's subnet
     */
    private ArrayList<IPv4Address> getSubnetIPs(IPv4Address address) {
        ArrayList<IPv4Address> addressList = new ArrayList<>();
        int count = (int) Math.pow(2, 32 - address.getMask());
        int prefix = address.getRawIP() & 0xFFFFFFFF << (32 - address.getMask());
        for (int i = 0; i < count; i++) {
            addressList.add(new IPv4Address(prefix | i, address.getMask()));
        }
        return addressList;
    }

}
