/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import pingMyNetwork.enums.Flags;
import pingMyNetwork.model.IPv4Address;
import pingMyNetwork.view.ConsoleOutput;
import pingMyNetwork.view.MainWindow;
import pingMyNetwork.view.ViewInterface;

/**
 *
 * @author Administrator
 */
public class ClientController {
    private static final int handShakeVal = 13378888;
    private static final String localhost = "127.0.0.1"; 
    private static final int PORT = 9999;
    /**
     * The default interface used for pinging
     */
    private static final int DEFAULT_INTERFACE = 0;
    /**
     * The default timeout used for pinging
     */
    private static final int DEFAULT_TIMEOUT = 1000;
     /**
     * Selected interface
     */
    private int currentInterfaceId;
    /**
     * Blocks multiple discoveries at a time
     */
    private boolean isDiscoveryRunning;
    /**
     * Reference to this controllers view
     */
    private ViewInterface menu;
    /**
     * 
     */
    private Socket socket;
    /**
     * 
     */
    private ObjectInputStream inObj;
    /**
     * 
     */
    private ObjectOutputStream outObj;

    public ClientController() {
        
    }
    
    /**
     * Main method of the controller that analyzes user input and fires up the
     * corresponding methods.
     *
     * @param args
     */
    public void run(String[] args) {
        if (args.length == 0) {
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
            this.connect();
        } else {
            this.menu = new ConsoleOutput();
            if(!this.connect())
                return;
            
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
        }
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
                this.menu.renderInterfaces(this.getInterfaces());
                break;
            case PING_FLAG:
                if (!this.isDiscoveryRunning) {
                    this.getOnlineIPs(this.currentInterfaceId,ClientController.DEFAULT_TIMEOUT);
                }
                break;
            case HELP_FLAG:
                this.menu.renderHelp();
                break;
            case EXIT_FLAG:
                this.sentExitMessage();
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
   
    private ArrayList<IPv4Address> getInterfaces(){
        try{
        this.outObj.writeUTF(Flags.LIST_FLAG.toString());
        this.outObj.flush();
        if(!this.handShake())
            return null;
        return (ArrayList < IPv4Address >) inObj.readObject();
        }
        catch(IOException | ClassNotFoundException e){
            this.menu.renderException(e);
            return null;
        }
    }
    
    private boolean handShake(){
        try{
            if(ClientController.handShakeVal == this.inObj.readInt()){
                this.outObj.writeInt(ClientController.handShakeVal);
                this.outObj.flush();
                return true;
            }
            return false;
        }
        catch(IOException e){
            this.menu.renderException(e);
            return false;
        }
                
    }
    private boolean getOnlineIPs(int id, int timeout){
        try{
        this.outObj.writeUTF(Flags.PING_FLAG.toString());
        this.outObj.flush();
        if(!this.handShake())
            return false;
        this.outObj.writeInt(id);
        this.outObj.writeInt(timeout);
        this.outObj.flush();
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    IPv4Address temp; 
                    do{
                        temp = (IPv4Address)inObj.readObject();
                        menu.displayIP(temp);
                    }
                    while(temp!= null);
                }
                catch(ClassNotFoundException | IOException | ClassCastException e){
                    menu.renderException(e);
                }
            } 
        }).start();
        return true;
        }
        catch(IOException e){
            this.menu.renderException(e);
        }
        return false;
    }
    
    private void sentExitMessage(){
        try{
          this.outObj.writeChars(Flags.EXIT_FLAG.toString());
          this.outObj.flush();
          this.handShake();
        }
        catch(IOException e){
            this.menu.renderException(e);
        }
    }
    
    private boolean connect(){
        try{
        this.socket = new Socket(ClientController.localhost,ClientController.PORT);
        if(!socket.isConnected())
            return false;
        this.inObj = new ObjectInputStream(new BufferedInputStream(this.socket.getInputStream()));
        this.outObj = new ObjectOutputStream(new BufferedOutputStream(this.socket.getOutputStream()));
        this.outObj.flush();
        return true;
        }
        catch(IOException e){
            this.menu.renderException(e);
            return false;
        }
    }
}
