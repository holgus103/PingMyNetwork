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
public class PingController implements ControllerConst {

    private static final int PORT = 9999;
    /**
     * Blocks multiple discoveries at a time
     */
    private boolean isDiscoveryRunning;
    private long lastRefresh;
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
        private Socket clientSocket;
        private ObjectInputStream in;
        private ObjectOutputStream out;

        private boolean handShake() {
            try {
                this.out.writeInt(ControllerConst.handShakeVal);
                this.out.flush();
                if (ControllerConst.handShakeVal == this.in.readInt()) {
                    return true;
                }
                return false;
            } catch (IOException e) {
                return false;
            }
        }

        private void ping(IPv4Address ip, int sec) {
            isDiscoveryRunning = true;
            try {
                onlineIPs.add(new IPv4Address("192.168.18.100"));
                this.sendResponse(onlineIPs.get(0), isDiscoveryRunning);
                onlineIPs.add(new IPv4Address("192.168.18.102"));
                this.sendResponse(onlineIPs.get(1), true);
//            for(IPv4Address val:getSubnetIPs(ip)){
//                if(val.isReachable(sec)){
//                    onlineIPs.add(val);
//                    this.sendResponse(val, true);
//                }
//            }
                this.sendResponse(null, true);
            isDiscoveryRunning = false;
            lastRefresh = System.currentTimeMillis() / 1000L;
            } catch (IndexOutOfBoundsException | InvalidIPAddressException e) {
                this.sendResponse(e, false);
                logException(e);
            }

        }

        public void sendResponse(Object obj, boolean success) {
            try {
                if (success) {
                    this.out.writeInt(ControllerConst.successVal);
                } else {
                    this.out.writeInt(ControllerConst.failureVal);
                }
                this.out.writeObject(obj);
                this.out.flush();
            } catch (IOException e) {
                logException(e);
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
            super.done();
            try{
            this.out.close();
            this.in.close();
            this.clientSocket.close();
            }
            catch(IOException e){
                logException(e);
            }
        }

        @Override
        public Void doInBackground() {
            boolean isOnline = true;
            while (isOnline) {
                try {
                    String command = this.in.readUTF();
                    Flags f = Flags.valueOf(command);
                    if (!this.handShake()) {
                        isOnline = false;
                        break;
                    }
                    switch (f) {
                        case LIST_FLAG:
                            try {
                                this.sendResponse(getLocalIPs(), true);
                            } catch (IndexOutOfBoundsException | InvalidIPAddressException | SocketException e) {
                                this.sendResponse(e, false);
                            }
                            break;
                        case PING_FLAG:
                            if (((lastRefresh == 0 || lastRefresh - System.currentTimeMillis() / 1000L > 10000)) && !isDiscoveryRunning) {
                                isDiscoveryRunning = true;
                                IPv4Address ip = (IPv4Address) in.readObject();
                                int timeout = in.readInt();
                                this.ping(ip, timeout);
                            } else {
                                while (isDiscoveryRunning) {
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException e) {
                                        this.sendResponse(e, false);
                                    }

                                }
                                for(IPv4Address val: onlineIPs){
                                    this.sendResponse(val, true);
                                }
                                this.sendResponse(null, true);
                            }
                            break;
                        case EXIT_FLAG:
                            isOnline = false;
                            break;

                    }
                } catch (IOException | ClassNotFoundException e) {
                    logException(e);
                }
            }
            return null;
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
     * Method fetching all IPs of the current machine
     *
     * @return machine's IPs
     */
    private ArrayList<IPv4Address> getLocalIPs() throws SocketException, IndexOutOfBoundsException, InvalidIPAddressException {
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

    private void logException(Throwable e) {
        Logger.getLogger(PingController.class.getName()).log(Level.SEVERE, null, e);
    }
}
