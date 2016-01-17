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
 * @author Jakub Suchan
 * @version %I%, %G%
 * @since 1.0
 */
public interface ViewInterface {

    /**
     * Displays all interfaces
     * @param interfaces Interfaces to be displayed
     */
    public void renderInterfaces(ArrayList<IPv4Address> interfaces);

    /**
     * Displays a single IP in the view
     * @param ip IP to be displayed
     */
    public void displayIP(IPv4Address ip);

    /**
     * Displays help
     */
    public void renderHelp();

    /**
     * Shows discovery results
     * @param ip
     */
    public void renderInit(IPv4Address ip);

    /**
     * Displays pinging initialization message
     * @param result
     */
    public void renderEnd(int result);

    /**
     * Displays an exception
     * @param e
     */
    public void renderException(Throwable e);

    /**
     * Displays an arguments error
     */
    public void renderArgsError();

    /**
     * Exits the view
     */
    public void exit();

    /**
     * Main view method
     */
    public void main();
}
