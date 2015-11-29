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
 * @author Administrator
 */
public interface ViewInterface {
    public void renderInterfaces(ArrayList<IPv4Address> interfaces);
    public void displayIP(IPv4Address ip);
    public void renderHelp();
    public void renderInit(IPv4Address ip);
    public void renderEnd(int result);
    public void renderException(Throwable e);
    public void renderArgsError();
}
