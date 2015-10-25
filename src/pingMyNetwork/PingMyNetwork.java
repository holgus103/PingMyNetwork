package pingMyNetwork;

import pingMyNetwork.controller.PingController;

/**
 *
 * @author Jakub Suchan
 * @version %I%
 */
public class PingMyNetwork {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        PingController pinger = new PingController();
        pinger.run(args);
    }

}
