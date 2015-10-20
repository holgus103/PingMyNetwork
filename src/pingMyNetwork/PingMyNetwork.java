/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork;

import java.io.IOException;
import pingMyNetwork.model.Pinger;
import pingMyNetwork.model.IPv4Address;
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
                int i= 0;
                ConsoleOutput menu = new ConsoleOutput();
                Pinger pinger = new Pinger();
                menu.renderInterfaces(pinger.getLocalIPs());
                try{
                i = System.in.read() - 48;
                }
                catch(IOException e){
                    System.out.println(e.getMessage());
                }
                menu.renderInterfaces(pinger.ping(i, 100));
//            switch(args[0]){
//                case "-h":
//                    menu.renderHelp();
//                    break;
//                case "-l":
//                    menu.renderInterfaces(pinger.getLocalIPs());
//                    break;
//                case "-p"
//                    if(args[1].equals("-t")){
//                        menu.renderInterfaces(pinger.ping(, sec));
//                    }
//                    break;
//            }
    }

}
