package pingMyNetwork.view;

import java.util.ArrayList;
import pingMyNetwork.model.IPv4Address;

/**
 *
 * @author Jakub Suchan
 * @version     %I%, %G%
 * @since       1.0
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
     * Renders the IP address provided in the view
     * @param ip IP to be rendered
     */
    public void displayIP(IPv4Address ip){
        System.out.println(ip.toString());
    }
    /**
     *  Renders the help instructions 
     */
    public void renderHelp(){
        System.out.println("SYNTAX: -l | -p | -h [interface] [-t | -m] ");
        System.out.println("-l : lists all available interfaces");
        System.out.println("-t : sets timeout for network discovery");
        System.out.println("-h : displays help");
        System.out.println("-p [i]: starts network discovery using interface no i");
                
    }
    
    /**
     * In case the used has provided invalid arguments 
     */
    public void renderArgsError(){
        System.out.println("Invalid arguments! Use -h for help");
    }
    
    public void renderInit(IPv4Address ip){
        System.out.println("Starting discovery using: " + ip.toString());
    }
    
    public void renderEnd(int result){
        System.out.println("Discovery finished. Finds: " + result);
    }
    /**
     *
     * @param e Exception to be logged
     */
    public void renderException(Throwable e){
        System.out.println(e.getMessage());
    }
}
