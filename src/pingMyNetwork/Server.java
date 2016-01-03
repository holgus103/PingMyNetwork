
package pingMyNetwork;

import java.io.IOException;
import pingMyNetwork.controller.PingController;

/**
 * @author Jakub Suchan
 * @version     %I%, %G%
 * @since       1.0
 * Main class of the server program
 */
public class Server {
     /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        PingController pinger = new PingController();
        try{
            pinger.run();
        }
        catch(IOException e){
            System.out.println("Failed to run");
        }
    }
}
