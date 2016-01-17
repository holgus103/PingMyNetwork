/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import pingMyNetwork.enums.Flags;
import pingMyNetwork.exception.InvalidIPAddressException;
import pingMyNetwork.exception.InvalidServerResponseException;
import pingMyNetwork.model.IPv4Address;
import pingMyNetwork.view.ConsoleOutput;
import pingMyNetwork.view.MainWindow;
import pingMyNetwork.view.ViewInterface;

/**
 * @version     %I%, %G%
 * @since       1.0
 * @author Jakub Suchan
 * Main controller of the clien program
 */
public class ClientController implements ControllerConst {
    /**
     * Localhost string constant
     */
    private static final String LOCALHOST = "127.0.0.1";
    /**
     * The default interface id used for pinging
     */
    private static final int DEFAULT_INTERFACE = 0;
    /**
     * The default timeout used for pinging
     */
    private static final int DEFAULT_TIMEOUT = 1000;
    /**
     * Selected interface address
     */
    private String currentInterface;
    /**
     * Blocks multiple discoveries at a time
     */
    private boolean isDiscoveryRunning;
    /**
     * Reference to this controllers view
     */
    private ViewInterface menu;
    /**
     *  Socket used for connections with the server
     */
    private Socket socket;
    /**
     * Stream for reading data from the server
     */
    private ObjectInputStream inObj;
    /**
     *  Stream for sending data to the server
     */
    private ObjectOutputStream outObj;
    /**
     * @version     %I%, %G%
     * @since       1.0
     * @author Jakub Suchan
     * Private class used to update the UI asynchronously whenever a IP gets discovered
     */
    private class AsynchUpdates extends SwingWorker<Void, IPv4Address> {
        /**
         * Background job waiting for new IPs from the discovery
         * @return null
         * @throws Exception 
         */
        @Override
        protected Void doInBackground() throws Exception {
            try {
                IPv4Address temp;
                do {
                    temp = (IPv4Address) receiveResponse();
                    if (temp != null) {
                        publish(temp);
                    }
                } while (temp != null);
            } catch (ClassCastException | InvalidServerResponseException e) {
                menu.renderException(e);
            }
            return null;
        }
        /**
         * Unblocks the discovery for the client
         */
        @Override
        protected void done() {
            super.done();
            isDiscoveryRunning = false;
        }
        /**
         * Pushes new IPs to the view
         * @param ips List of IPs to display
         */
        @Override
        protected void process(List<IPv4Address> ips) {
            for (IPv4Address value : ips) {
                menu.displayIP(value);
            }
        }

    }

    /**
     * Main method of the controller that analyzes user input and fires up the
     * corresponding methods.
     *
     * @param args Command line arguments
     */
    public void run(String[] args) {
        if (args.length == 0) {
            this.menu = new MainWindow(
                    new ActionListener() {
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
                    },
                    new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent e) {
                            if(socket!= null && socket.isConnected())
                            sendExitMessage();
                        }
                    });
            this.menu.main();
            this.connect();
        } else {
            this.menu = new ConsoleOutput();
            if (!this.connect()) {
                return;
            }

            switch (args.length) {
                case 1:
                    if (args.length > 0) {
                        switch (Flags.getEnum(args[0])) {
                            case HELP_FLAG:
                                menu.renderHelp();
                                break;
                            case LIST_FLAG:
                                menu.renderInterfaces(this.getInterfaces());
                                break;
                            case PING_FLAG:
                                this.getOnlineIPs(ClientController.DEFAULT_INTERFACE, ClientController.DEFAULT_TIMEOUT);
                                break;
                            default:
                                menu.renderArgsError();

                        }
                    }
                    break;
                case 2:
                    if (Flags.PING_FLAG.isEqual(args[0])) {
                        try {
                            this.getOnlineIPs(Integer.parseInt(args[1]), ClientController.DEFAULT_TIMEOUT);
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
                                this.getOnlineIPs(ClientController.DEFAULT_INTERFACE, Integer.parseInt(args[1]));
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
                                this.getOnlineIPs(Integer.parseInt(args[1]), Integer.parseInt(args[3]));
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
            while (this.isDiscoveryRunning) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    menu.renderException(e);
                }
            }
            this.sendExitMessage();
        }
    }

    /**
     * Method for action handling
     * @param e ActionEvent to be handled
     */
    private void actions(ActionEvent e) {
        Flags command = Flags.valueOf(e.getActionCommand());
        switch (command) {
            case LIST_FLAG:
                this.menu.renderInterfaces(this.getInterfaces());
                break;
            case PING_FLAG:
                this.getOnlineIPs(-1, ClientController.DEFAULT_TIMEOUT);
                break;
            case HELP_FLAG:
                this.menu.renderHelp();
                break;
            case EXIT_FLAG:
                this.sendExitMessage();
                this.menu.exit();

            default:

        }
    }

    /**
     * Updates the pinging interface
     * @param e TreeSelectionEvent
     */
    private void selectInterface(TreeSelectionEvent e) {
        Object selection = ((JTree) e.getSource()).getLastSelectedPathComponent();
        if (selection != null) {
            this.currentInterface = selection.toString();
        }
    }
    /**
     * Reads the interfaces available on the server machine
     * @return ArrayList of interfaces available on the server
     */
    private ArrayList<IPv4Address> getInterfaces() {
        try {
            this.outObj.writeUTF(Flags.LIST_FLAG.toString());
            this.outObj.flush();
            if (!this.handShake()) {
                return null;
            }
            return (ArrayList< IPv4Address>) this.receiveResponse();
        } catch (IOException | InvalidServerResponseException e) {
            this.menu.renderException(e);
            return null;
        }
    }
    /**
     * Performs a handshake between the server and the client to check if both are still present
     * @return Whether the handshake was successful or not 
     */
    private boolean handShake() {
        try {
            if (ClientController.handShakeVal == this.inObj.readInt()) {
                this.outObj.writeInt(ControllerConst.handShakeVal);
                this.outObj.flush();
                return true;
            }
            return false;
        } catch (IOException e) {
            this.menu.renderException(e);
            return false;
        }

    }
   /**
    * Requests available nodes from the server
    * @param id Interface id
    * @param timeout Tiemout used for pinging
    * @return Whether the request was successful or not
    */
    private boolean getOnlineIPs(int id, int timeout) {
        if (this.currentInterface == null) {
            this.currentInterface = this.getInterfaces().get(id >= 0 ? id : 0).toString();
        }
        if (this.isDiscoveryRunning) {
            return false;
        }
        this.isDiscoveryRunning = true;
        try {
            this.outObj.writeUTF(Flags.PING_FLAG.toString());
            this.outObj.flush();
            if (!this.handShake()) {
                return false;
            }
            this.outObj.writeObject(new IPv4Address(this.currentInterface));
            this.outObj.writeInt(timeout);
            this.outObj.flush();
            new AsynchUpdates().execute();
            return true;
        } catch (IOException | IndexOutOfBoundsException | InvalidIPAddressException e) {
            this.menu.renderException(e);
        }
        return false;
    }
    /**
     * Sends an exit message to the server
     */
    private void sendExitMessage() {
        try {
            this.outObj.writeUTF(Flags.EXIT_FLAG.toString());
            this.outObj.flush();
            this.handShake();
            this.outObj.close();
            this.inObj.close();
            this.socket.close();
        } catch (IOException e) {
            this.menu.renderException(e);
        }
    }
    /**
     * Receives a formated server response with header
     * @return Object to be received from the server
     * @throws InvalidServerResponseException 
     */
    private Object receiveResponse() throws InvalidServerResponseException {
        try {
            switch (this.inObj.readInt()) {
                case ControllerConst.successVal:
                    return this.inObj.readObject();
                case ControllerConst.failureVal:
                    Throwable e = (Throwable) this.inObj.readObject();
                    this.menu.renderException(e);
                    return e;
                default:
                    throw new InvalidServerResponseException("The server response is invalid");
            }
        } catch (IOException | ClassNotFoundException e) {
            this.menu.renderException(e);
        }
        return null;
    }
    /**
     * Connects to the server
     * @return Whether the connection occurred
     */
    private boolean connect() {
        try {
            this.socket = new Socket(ClientController.LOCALHOST, ControllerConst.PORT);
            if (!socket.isConnected()) {
                return false;
            }
            this.inObj = new ObjectInputStream(new BufferedInputStream(this.socket.getInputStream()));
            this.outObj = new ObjectOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
            this.outObj.flush();
            return true;
        } catch (IOException e) {
            this.menu.renderException(e);
            return false;
        }
    }
}
