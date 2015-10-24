package pingMyNetwork.model;

import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 *
 * @author holgus103
 * @version %I%
 */
public class Pinger {
    
    private static int MAX_THREADS = 256;
    private Thread[] threads;
    /**
     * Array of already discovered IPs
     */
    private ArrayList<IPv4Address> ips;
    // Private class used for pinging asychronously
    
    private class PingIP extends Thread{
        // Private properties
        // IP the thread is going to ping
        private IPv4Address ip;
        private ArrayList<IPv4Address> onlineNodes;
        // Ping timeout
        private final int timeout;
        PingIP(IPv4Address ip, int timeout,ArrayList<IPv4Address> onlineNodes){
            this.ip = ip;
            this.timeout = timeout;
            this.onlineNodes = onlineNodes;
        }
        public void run(){
            if(this.ip.isReachable(timeout));
                onlineNodes.add(this.ip);
        }
    }
    
    public Pinger() {
        this.ips = this.getLocalIPs();
        this.threads = new Thread[this.MAX_THREADS];
    }

    /**
     * 
     * @return List of online IPs within the subnet
     */
    public ArrayList<IPv4Address> ping(int... args) {
        // Default parameters 
        int i = 0, sec = 1000, counter = 0;
        // Check if params were supplied
        if(args.length>0){
            i = args[0];
            if(args.length>1){
                sec = args[1];
            }
        }
        ArrayList<IPv4Address> onlineNodes = new ArrayList<IPv4Address>();
        try {
            ArrayList<IPv4Address> subnet = this.ips.get(i).generateSubnetIPs();
            for (IPv4Address value : subnet) {
                if(counter<this.MAX_THREADS){
                this.threads[counter] = new PingIP(value, sec, onlineNodes);
                this.threads[counter].start();
                }
                else{
                    for(Thread worker:this.threads){
                        try{
                        worker.join();
                        }
                        catch(InterruptedException e){
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
        }
        return onlineNodes;

    }

    /**
     * @return Machine's interfaces' IPs
     */
    public ArrayList<IPv4Address> getLocalIPs() {
        ArrayList<IPv4Address> validIPs = new ArrayList<IPv4Address>();
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
        } catch (SocketException e) {
            System.out.printf(e.getMessage());
        }
        return validIPs;
    }

    /**
     * 
     * @return a set of IPs from the machine's subnet
     */
    private ArrayList<IPv4Address> getSubnetIPs(int i) {
        return this.ips.get(i).generateSubnetIPs();
    }

}
