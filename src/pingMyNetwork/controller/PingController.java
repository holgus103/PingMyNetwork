package pingMyNetwork.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import jdk.nashorn.internal.runtime.regexp.joni.ast.ConsAltNode;
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
    /**
     * Array of references to the above mentioned threads
     */
    private final Thread[] threads;
    /**
     * Reference to this controllers view
     */
    private ViewInterface menu;
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
        this.threads = new Thread[PingController.MAX_THREADS];
    }

    /**
     * Main method of the controller that analyzes user input and fires up the
     * corresponding methods.
     *
     * @param args
     */
    public void run(String[] args) {
        if (args.length == 0) {
            this.menu = new MainWindow(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    actions(e);
                }
            });
        } else {
            this.menu = new ConsoleOutput();
            switch (args.length) {
                case 1:
                    if (args.length > 0) {
                        switch (Flags.getEnum(args[0])) {
                            case HELP_FLAG:
                                menu.renderHelp();
                                break;
                            case LIST_FLAG:
                                menu.renderInterfaces(this.ips);
                                break;
                            case PING_FLAG:
                                this.ping(PingController.DEFAULT_INTERFACE, PingController.DEFAULT_TIMEOUT, PingController.DEFAULT_MULTITHREADING);
                                break;
                            case MULTITHREADING_FLAG:
                                this.ping(PingController.DEFAULT_INTERFACE, PingController.DEFAULT_TIMEOUT, true);
                            default:
                                menu.renderArgsError();

                        }
                    }
                    break;
                case 2:
                    if (Flags.PING_FLAG.isEqual(args[0])) {
                        if (Flags.MULTITHREADING_FLAG.isEqual(args[1])) {
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
                    if (Flags.PING_FLAG.isEqual(args[0])) {
                        if (Flags.TIMEOUT_FLAG.isEqual(args[1])) {
                            try {
                                this.ping(PingController.DEFAULT_INTERFACE, Integer.parseInt(args[1]), PingController.DEFAULT_MULTITHREADING);
                            } catch (NumberFormatException e) {
                                menu.renderException(e);
                            }
                        } else {
                            if (Flags.MULTITHREADING_FLAG.isEqual(args[2])) {
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
                    if (Flags.PING_FLAG.isEqual(args[0])) {
                        if (Flags.TIMEOUT_FLAG.isEqual(args[1]) && Flags.MULTITHREADING_FLAG.isEqual(args[3])) {
                            try {
                                this.ping(PingController.DEFAULT_INTERFACE, Integer.parseInt(args[2]), true);
                            } catch (NumberFormatException e) {
                                menu.renderException(e);
                            }
                        } else {
                            if (Flags.TIMEOUT_FLAG.isEqual(args[2])) {
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
                    if (Flags.PING_FLAG.isEqual(args[0]) && Flags.TIMEOUT_FLAG.isEqual(args[2]) && Flags.MULTITHREADING_FLAG.isEqual(args[4])) {
                        try {
                            this.ping(Integer.parseInt(args[1]), Integer.parseInt(args[3]), true);
                        } catch (NumberFormatException e) {
                            menu.renderException(e);
                        }
                    } else {
                        if (Flags.PING_FLAG.isEqual(args[0]) && Flags.MULTITHREADING_FLAG.isEqual(args[2]) && Flags.TIMEOUT_FLAG.isEqual(args[3])) {
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
    private int ping(int i, int sec, boolean multihreading) {
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

    //ActionListeners

    private void actions(ActionEvent e) {
        Flags command = Flags.valueOf(e.getActionCommand());
        switch (command) {
            case LIST_FLAG:
                this.menu.renderInterfaces(ips);
                break;
            default:

        }
    }
}
