/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork.view;

import java.util.ArrayList;
import pingMyNetwork.model.IPv4Address;

/**
 *
 * @author holgus103
 * @version %I%
 */
public class ConsoleOutput {

    /**
     * Displays a list of IPs
     * @param interfaces to display in the console
     */
    public void renderInterfaces(ArrayList<IPv4Address> interfaces) {
        for(IPv4Address value:interfaces)
        System.out.println(value.toString());
    }

    /**
     *  Renders the help instructions 
     */
    public void renderHelp(){
        System.out.println("SYNTAX:");
        System.out.println("-l : lists all available interfaces");
        System.out.println("-t : sets timeout for network discovery");
        System.out.println("-h : displays help");
        System.out.println("-p [i]: starts network discovery using interface no i");
                
    }
}
