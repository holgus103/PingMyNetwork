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
 * @author Jakub Suchan
 * @version %I%, %G%
 * @since 1.0
 */
public class PingController {

    /**
     * The maximum number of threads used for network discovery
     */
    private static final int MAX_THREADS = 8;
    /**
     * The default interface used for pinging
     */
    private static final int DEFAULT_INTERFACE = 0;
    /**
     * The default timeout used for pinging
     */
    private static final int DEFAULT_TIMEOUT = 1000;
    /**
     * Multithreading is disabled by default as it causes problems on Windows
     */
    private static final boolean DEFAULT_MULTITHREADING = false;
//    /**
//     * Flag for option ping
//     */
//    private static final String PING_FLAG = "-p";
//    /**
//     * Flag for option timeout
//     */
//    private static final String TIMEOUT_FLAG = "-t";
//    /**
//     * Flag for option list
//     */
//    private static final String LIST_FLAG = "-l";
//    /**
//     * Flag for option help
//     */
//    private static final String HELP_FLAG = "-h";
//    /**
//     * Flag for option multi-threading
//     */
//    private static final String MULTITHREADING_FLAG = "-m";
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
     *
     * @author Jakub Suchan
     * @version %I%, %G%
     * @since 1.0
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
         *
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
         * Thread's main method that checks for IP availability. If the current
         * IP is found online the global counter will be increased and the IP
         * address rendered in the view right away.
         */
        @Override
        public void run() {
            try {
                if (ip.isReachable(timeout, true)) {
                    this.ipCounter++;
                    menu.displayIP(ip);
                }
            } catch (IOException e) {
                menu.renderException(e);
            }
        }
    }
    /**
     * Enum for switch
     */
    private enum Flags {
        PING_FLAG("-p"),
        TIMEOUT_FLAG("-t"),
        LIST_FLAG("-l"),
        HELP_FLAG("-h"),
        MULTITHREADING_FLAG("-m");
        private final String flag;
        public Flags(String flag){
            this.flag = flag;
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
        this.menu = new ConsoleOutput();
        this.ips = this.getLocalIPs();
        this.threads = new Thread[PingController.MAX_THREADS];
    }

    /**
     * Main method of the controller that analyzes user input and fires up the
     * corresponding methods.
     *
     * @param args
     */
    public void run(String[] args) {
        switch (args.length) {
            case 1:
                if (args.length > 0) {
                    switch (Flags(args[0])) {
                        case Flags.HELP_FLAG:
                            menu.renderHelp();
                            break;
                        case Flags.LIST_FLAG:
                            menu.renderInterfaces(this.ips);
                            break;
                        case Flags.PING_FLAG:
                            this.ping(PingController.DEFAULT_INTERFACE, PingController.DEFAULT_TIMEOUT, PingController.DEFAULT_MULTITHREADING);
                            break;
                        case Flags.MULTITHREADING_FLAG:
                            this.ping(PingController.DEFAULT_INTERFACE, PingController.DEFAULT_TIMEOUT, true);
                        default:
                            menu.renderArgsError();

                    }
                }
                break;
            case 2:
                if (args[0].equals(PingController.PING_FLAG)) {
                    if (args[1].equals(PingController.MULTITHREADING_FLAG)) {
                        this.ping(PingController.DEFAULT_INTERFACE, PingController.DEFAULT_TIMEOUT, PingController.DEFAULT_MULTITHREADING);
                    } else {
                        try {
                            this.ping(Integer.parseInt(args[1]), PingController.DEFAULT_TIMEOUT, PingController.DEFAULT_MULTITHREADING);
                        } catch (NumberFormatException e) {
                            menu.renderException(e);
                        }
                    }
                } else {
                    menu.renderArgsError();
                }
                break;
            case 3:
                if (args[0].equals(PingController.PING_FLAG)) {
                    if (args[1].equals(PingController.TIMEOUT_FLAG)) {
                        try {
                            this.ping(PingController.DEFAULT_INTERFACE, Integer.parseInt(args[1]), PingController.DEFAULT_MULTITHREADING);
                        } catch (NumberFormatException e) {
                            menu.renderException(e);
                        }
                    } else {
                        if (args[2].equals(PingController.MULTITHREADING_FLAG)) {
                            try {
                                this.ping(Integer.parseInt(args[1]), PingController.DEFAULT_TIMEOUT, true);
                            } catch (NumberFormatException e) {
                                menu.renderException(e);
                            }
                        } else {
                            menu.renderArgsError();
                        }
                    }
                } else {
                    menu.renderArgsError();
                }
                break;
            case 4:
                if (args[0].equals(PingController.PING_FLAG)) {
                    if (args[1].equals(PingController.TIMEOUT_FLAG) && args[3].equals(PingController.MULTITHREADING_FLAG)) {
                        try {
                            this.ping(PingController.DEFAULT_INTERFACE, Integer.parseInt(args[2]), true);
                        } catch (NumberFormatException e) {
                            menu.renderException(e);
                        }
                    } else {
                        if (args[2].equals(PingController.TIMEOUT_FLAG)) {
                            try {
                                this.ping(Integer.parseInt(args[1]), Integer.parseInt(args[3]), PingController.DEFAULT_MULTITHREADING);
                            } catch (NumberFormatException e) {
                                menu.renderException(e);
                            }
                        } else {
                            menu.renderArgsError();
                        }
                    }
                } else {
                    menu.renderArgsError();
                }
                break;
            case 5:
                if (args[0].equals(PingController.PING_FLAG) && args[2].equals(PingController.TIMEOUT_FLAG) && args[4].equals(PingController.MULTITHREADING_FLAG)) {
                    try {
                        this.ping(Integer.parseInt(args[1]), Integer.parseInt(args[3]), true);
                    } catch (NumberFormatException e) {
                        menu.renderException(e);
                    }
                } else {
                    if (args[0].equals(PingController.PING_FLAG) && args[2].equals(PingController.MULTITHREADING_FLAG) && args[3].equals(PingController.TIMEOUT_FLAG)) {
                        try {
                            this.ping(Integer.parseInt(args[1]), Integer.parseInt(args[4]), true);
                        } catch (NumberFormatException e) {
                            menu.renderException(e);
                        }
                    } else {
                        menu.renderArgsError();
                    }
                }
                break;
            default:
                menu.renderArgsError();
        }
    }

    /**
     * Method exploring the network and pining all subnet IPs, the IPs that are
     * found online will be displayer in real-time in the view that's been
     * initialized within this class as menu. By default the method will use the
     * first interface and a timeout of1000 ms.
     *
     * @param i no of the interface used for pinging
     * @param sec pinging timeout
     * @param multihreading determines whether multithreading is enabled or not
     * @return count of all found IPs
     */
    public int ping(int i, int sec, boolean multihreading) {
        // Default parameters 
        int threadCounter = 0;
        Integer ipCounter = 0;
        // Check if params were supplied
        try {
            menu.renderInit(this.ips.get(i));
            for (IPv4Address value : this.getSubnetIPs(this.ips.get(i))) {
                if (multihreading) {
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
                                menu.renderException(e);
                            }
                        }
                    }
                } else {
                    try {
                        if (value.isReachable(sec, multihreading)) {
                            menu.displayIP(value);
                        }
                    } catch (IOException e) {
                        menu.renderException(e);
                    }
                }
            }
            menu.renderEnd(ipCounter);
        } catch (IndexOutOfBoundsException e) {
            menu.renderException(e);
        }
        return ipCounter;

    }

    /**
     * Method fetching all IPs of the current machine
     *
     * @return machine's IPs
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
}
