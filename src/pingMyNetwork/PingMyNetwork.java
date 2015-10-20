/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork;

import pingMyNetwork.Model.Pinger;
import pingMyNetwork.Model.IPv4Address;

/**
 *
 * @author holgus103
 * @version %I%
 */
public class PingMyNetwork {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Pinger pinger = new Pinger();
//        pinger.getLocalIPs();
        // TODO code application logic here
        IPv4Address test = new IPv4Address("192.168.18.101", 24);
        for (IPv4Address value : test.generateSubnetIPs()) {
            System.out.println(value.toString());
        }
    }

}
