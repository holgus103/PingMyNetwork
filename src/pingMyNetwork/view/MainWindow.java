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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import pingMyNetwork.model.IPv4Address;
import pingMyNetwork.enums.Flags;

/**
 *
 * @author Lab
 */
public class MainWindow implements ViewInterface {
    private JTree ipTree;
    private ActionListener menuListener;
    private TreeSelectionListener treeListener;
    private JFrame frame;
        
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
        JOptionPane.showMessageDialog(null, "This application also proviced it's CLI, run with -h for more information!","Help", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void renderInit(IPv4Address ip) {
        
    }

    @Override
    public void renderEnd(int result) {
        
    }

    @Override
    public void renderException(Throwable e) {
        JOptionPane.showMessageDialog(null, e.getMessage(), "Error",JOptionPane.ERROR_MESSAGE);
    }
    
    @Override
    public void renderArgsError() {
        JOptionPane.showMessageDialog(null, "Invalid arguments", "Error", JOptionPane.ERROR_MESSAGE);
    }
    @Override
    public void exit() {
        System.exit(0);
    }
    private void createAndShowGUI() {
        this.frame = new JFrame("PingMyNetwork");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JComponent component = (JComponent) this.frame.getContentPane();
        component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));
        component.add(this.buildMenuBar());
        component.add(this.buildToolBar());
        component.add(this.buildTree());
        this.frame.pack();
        this.frame.setVisible(true);
    }

    private void main() {
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
        JMenuBar commandMenu = new JMenuBar();
        JMenu commandsMenu = new JMenu("Commands");

        JMenuItem item = new JMenuItem("List IPs");
        item.setActionCommand(Flags.LIST_FLAG.name());
        item.addActionListener(this.menuListener);
        commandsMenu.add(item);

        item = new JMenuItem("Help");
        item.setActionCommand(Flags.HELP_FLAG.name());
        item.addActionListener(this.menuListener);
        commandsMenu.add(item);

        item = new JMenuItem("Ping");
        item.setActionCommand(Flags.PING_FLAG.name());
        item.addActionListener(this.menuListener);
        
        commandsMenu.add(item);

        item = new JMenuItem("Exit");
        item.addActionListener(this.menuListener);
        item.setActionCommand(Flags.EXIT_FLAG.name());
        commandsMenu.add(item);

        commandMenu.add(commandsMenu);
        return commandMenu;
        //Display the window.
    }
    private JPanel buildToolBar(){
        JToolBar commandsToolBar = new JToolBar();
        
        JButton button = new JButton("List IP");
        button.setActionCommand(Flags.LIST_FLAG.name());
        button.addActionListener(this.menuListener);
        commandsToolBar.add(button);
        button = new JButton("Ping");
        button.setActionCommand(Flags.PING_FLAG.name());
        button.addActionListener(this.menuListener);
        commandsToolBar.add(button);
        
        JPanel toolBarPanel = new JPanel();
        toolBarPanel.add(commandsToolBar);
        return toolBarPanel;
    }
    
    private JPanel buildTree(){
        DefaultMutableTreeNode top =
        new DefaultMutableTreeNode("IP");
        this.ipTree = new JTree(top);
        this.ipTree.addTreeSelectionListener(this.treeListener);
        JPanel treePanel = new JPanel();
        treePanel.add(this.ipTree);
        return treePanel;
    }
    public MainWindow(ActionListener listener, TreeSelectionListener treeListener){
        this.treeListener = treeListener;
        this.menuListener = listener;
        this.main();
    }

    
}
