/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;
import pingMyNetwork.model.IPv4Address;
import pingMyNetwork.enums.Flags;

/**
 *
 * @author Lab
 */
public class MainWindow implements ViewInterface {

    @Override
    public void renderInterfaces(ArrayList<IPv4Address> ips) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)this.ipTree.getModel().getRoot();
        for(IPv4Address value: ips){
            root.add(new DefaultMutableTreeNode(value.toString()));
        }
    }

    @Override
    public void displayIP(IPv4Address ip) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)this.ipTree.getModel().getRoot();
        root.add(new DefaultMutableTreeNode(ip.toString()));
    }

    @Override
    public void renderHelp() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void renderInit(IPv4Address ip) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void renderEnd(int result) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void renderException(Throwable e) {
        JOptionPane.showMessageDialog(null, "Error", e.getMessage(), JOptionPane.ERROR_MESSAGE);
    }

    private JTree ipTree;
    
    private static void createAndShowGUI() {
        JFrame frame = new JFrame("PingMyNetwork");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        MainWindow win = new MainWindow();
        JComponent component = (JComponent) frame.getContentPane();
        component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));
        component.add(win.buildMenuBar());
        component.add(win.buildToolBar());
        component.add(win.buildTree());
//        frame.setJMenuBar(win.buildMenuBar());
//        frame.add(win.buildToolBar());
//        frame.add(win.buildTree());
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
          try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private JMenuBar buildMenuBar() {
        //Create and set up the window.

        //Add the ubiquitous "Hello World" label.
        JMenuBar menuBar = new JMenuBar();
        JMenu commandsMenu = new JMenu("Commands");

        JMenuItem item = new JMenuItem("List IPs");
        item.setActionCommand(Flags.LIST_FLAG.name());
//        item.addActionListener(this);
        commandsMenu.add(item);

        item = new JMenuItem("Help");
        item.setActionCommand(Flags.HELP_FLAG.name());
//        item.addActionListener(this);
        commandsMenu.add(item);

        item = new JMenuItem("Ping");
        item.setActionCommand(Flags.PING_FLAG.name());
//        item.addActionListener(this);
        
        commandsMenu.add(item);

        item = new JMenuItem("Exit");
//        item.addActionListener(this);
        item.setActionCommand(Flags.EXIT_FLAG.name());
        commandsMenu.add(item);

        menuBar.add(commandsMenu);
        return menuBar;
        //Display the window.
    }
    private JPanel buildToolBar(){
        JToolBar commandsToolBar = new JToolBar();
        
        JButton button = new JButton("List IP");
        button.setActionCommand(Flags.LIST_FLAG.name());
        commandsToolBar.add(button);
        
        button = new JButton("Ping");
        button.setActionCommand(Flags.PING_FLAG.name());
//        button.addActionListener(this);
        commandsToolBar.add(button);
        
        JPanel toolBarPanel = new JPanel();
        toolBarPanel.add(commandsToolBar);
        return toolBarPanel;
    }
    
    private JPanel buildTree(){
        DefaultMutableTreeNode top =
        new DefaultMutableTreeNode("IP");
        this.ipTree = new JTree(top);
        JPanel treePanel = new JPanel();
        treePanel.add(this.ipTree);
        return treePanel;
    }
    
}
