package pingMyNetwork;

import java.io.IOException;
import pingMyNetwork.model.Pinger;
import pingMyNetwork.view.ConsoleOutput;

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
        ConsoleOutput menu = new ConsoleOutput();
        Pinger pinger = new Pinger();
        switch (args.length) {
            case 1:
                if (args.length > 0) {
                    switch (args[0]) {
                        case "-h":
                            menu.renderHelp();
                            break;
                        case "-l":
                            menu.renderInterfaces(pinger.getLocalIPs());
                            break;
                        case "-p":
                            menu.renderInterfaces(pinger.ping());
                            break;
                        default:
                            menu.renderError();

                    }
                }
                break;
            case 2:
                if (args[0].equals("-p")) {
                    try {
                        menu.renderInterfaces(pinger.ping(Integer.parseInt(args[1])));
                    } catch (NumberFormatException e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    menu.renderError();
                }
                break;
            case 3:
                if (args[0].equals("-p")) {
                    if (args[1].equals("-t")) {
                        try {
                            menu.renderInterfaces(pinger.ping(0, Integer.parseInt(args[1])));
                        } catch (NumberFormatException e) {
                            System.out.println(e.getMessage());
                        }
                    } else {
                        menu.renderError();
                    }
                } else {
                    menu.renderError();
                }
                break;
            case 4:
                if (args[0].equals("-p")) {
                    if (args[2].equals("-t")) {
                        try {
                            menu.renderInterfaces(pinger.ping(Integer.parseInt(args[3]), Integer.parseInt(args[1])));
                        } catch (NumberFormatException e) {
                            System.out.println(e.getMessage());
                        }
                    } else {
                        menu.renderError();
                    }
                } else {
                    menu.renderError();
                }
                break;
            default:
                menu.renderError();
        }
    }

}
