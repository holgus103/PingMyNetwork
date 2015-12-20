package pingMyNetwork;

import pingMyNetwork.controller.ClientController;

/**
 *
 * @author Jakub Suchan
 * @version     %I%, %G%
 * @since       1.0
 */
public class Client {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        ClientController controller = new ClientController();
        controller.run(args);
    }

}
