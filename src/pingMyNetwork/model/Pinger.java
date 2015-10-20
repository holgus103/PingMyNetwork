package pingMyNetwork.model;

import java.net.Inet4Address;
import java.net.InetAddress;
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

    /**
     * Array of already discovered IPs
     */
    private ArrayList<IPv4Address> ips;

    public Pinger() {
        this.ips = this.getLocalIPs();
    }

    /**
     * @return List of online IPs within the subnet
     */
    public ArrayList<IPv4Address> ping(int i, int sec) {
        ArrayList<IPv4Address> onlineNodes = new ArrayList<IPv4Address>();
        try {
            ArrayList<IPv4Address> subnet = this.ips.get(i).generateSubnetIPs();
            for (IPv4Address value : subnet) {
                if (value.isReachable(sec)) {
                    onlineNodes.add(value);
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
