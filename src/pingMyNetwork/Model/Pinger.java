package pingMyNetwork.Model;

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
    private String mask;
    private ArrayList<IPv4Address> ips;

    /**
     * @return List of IPs
     */
    public ArrayList<IPv4Address> ping() {
        return null;
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
     * @return
     */
    public ArrayList<IPv4Address> getSubnetIPs() {
        // to do
        return null;
    }

}
