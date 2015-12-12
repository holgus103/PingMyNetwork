/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import pingMyNetwork.enums.Flags;
import pingMyNetwork.view.ConsoleOutput;
import pingMyNetwork.view.MainWindow;
import pingMyNetwork.view.ViewInterface;

/**
 *
 * @author Administrator
 */
public class ClientController {
     /**
     * Reference to this controllers view
     */
    private ViewInterface menu;
    /**
     * Is this a CLI session
     */
    private boolean isCLISession;
    /**
     * 
     */
    private Socket socket;
    /**
     * 
     */
    private BufferedReader in;
    /**
     * 
     */
    private PrintWriter out;
    /**
     * Main method of the controller that analyzes user input and fires up the
     * corresponding methods.
     *
     * @param args
     */
    public void run(String[] args) {
        if (args.length == 0) {
            this.isCLISession = false;
            this.menu = new MainWindow(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    actions(e);
                }
            },
                    new TreeSelectionListener() {
                        @Override
                        public void valueChanged(TreeSelectionEvent e) {
                            selectInterface(e);
                        }
                    });
            this.menu.main();
        } else {
            this.isCLISession = true;
            this.menu = new ConsoleOutput();
            switch (args.length) {
                case 1:
                    if (args.length > 0) {
                        switch (Flags.getEnum(args[0])) {
                            case HELP_FLAG:
                                menu.renderHelp();
                                break;
                            case LIST_FLAG:
                                menu.renderInterfaces(this.ips);
                                break;
                            case PING_FLAG:
                                this.ping(PingController.DEFAULT_INTERFACE, PingController.DEFAULT_TIMEOUT);
                                break;
                            default:
                                menu.renderArgsError();

                        }
                    }
                    break;
                case 2:
                    if (Flags.PING_FLAG.isEqual(args[0])) {
                        try {
                            this.ping(Integer.parseInt(args[1]), PingController.DEFAULT_TIMEOUT);
                        } catch (NumberFormatException e) {
                            menu.renderException(e);
                        }
                    } else {
                        menu.renderArgsError();
                    }
                    break;
                case 3:
                    if (Flags.PING_FLAG.isEqual(args[0])) {
                        if (Flags.TIMEOUT_FLAG.isEqual(args[1])) {
                            try {
                                this.ping(PingController.DEFAULT_INTERFACE, Integer.parseInt(args[1]));
                            } catch (NumberFormatException e) {
                                menu.renderException(e);
                            }
                        }
                    } else {
                        menu.renderArgsError();
                    }
                    break;
                case 4:
                    if (Flags.PING_FLAG.isEqual(args[0])) {
                        if (Flags.TIMEOUT_FLAG.isEqual(args[2])) {
                            try {
                                this.ping(Integer.parseInt(args[1]), Integer.parseInt(args[3]));
                            } catch (NumberFormatException e) {
                                menu.renderException(e);
                            }
                        } else {
                            menu.renderArgsError();
                        }

                    } else {
                        menu.renderArgsError();
                    }
                    break;
                default:
                    menu.renderArgsError();
            }
        }
    }
    /**
     * 
     * @param i
     * @param sec 
     */
    private void ping(int i, int sec){
        this.socket.connect(null);
    }
    /**
     * Method for action handling
     *
     * @param e ActionEvent to be handled
     */
    private void actions(ActionEvent e) {
        Flags command = Flags.valueOf(e.getActionCommand());
        switch (command) {
            case LIST_FLAG:
                this.menu.renderInterfaces(ips);
                break;
            case PING_FLAG:
                if (!this.isDiscoveryRunning) {
                    this.ping(this.currentInterfaceId, PingController.DEFAULT_TIMEOUT);
                }
                break;
            case HELP_FLAG:
                this.menu.renderHelp();
                break;
            case EXIT_FLAG:
                this.menu.exit();

            default:

        }
    }

    /**
     * Updates the pinging interface
     *
     * @param e
     */
    private void selectInterface(TreeSelectionEvent e) {
        this.currentInterfaceId = ((JTree) e.getSource()).getMinSelectionRow() - 1;
    }
}
