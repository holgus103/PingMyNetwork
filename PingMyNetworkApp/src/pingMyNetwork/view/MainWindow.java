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
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import pingMyNetwork.model.IPv4Address;
import pingMyNetwork.enums.Flags;
/**
 *
 * @author Jakub Suchan
 * @version %I%, %G%
 * @since 1.0
 */
public class MainWindow implements ViewInterface {
    /**
     * View's JTree for displaying IPs
     */
    private JTree ipTree;
    /**
     * ActionListener to be added to the buttons
     */
    private final ActionListener menuListener;
    /**
     * TheSelectionListener to change the Interface ID
     */
    private final TreeSelectionListener treeListener;
    /**
     * The WindowAdapter used for closing the connection with the server when the window is closing
     */
    private final WindowAdapter windowAdapter;
    /**
     * Main JFrame of the view
     */
    private JFrame frame;
    /**
     * Views TabbedPane
     */
    private JTabbedPane tabPane;
    /**
     * Enables the user to input an IP address
     */
    private JTextField textField;
    /**
     * Boolean value for JTree refreshing
     */
    private boolean isShowingInterfaces;
        
    /**
     * Displays all interfaces
     * @param ips Interfaces to be displayed
     */
    @Override
    public void renderInterfaces(ArrayList<IPv4Address> ips) {
        this.isShowingInterfaces = true;
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)this.ipTree.getModel().getRoot();
        root.removeAllChildren();
        for(IPv4Address value: ips){
            root.add(new DefaultMutableTreeNode(value.toString()));
        }
        ((DefaultTreeModel)this.ipTree.getModel()).reload(root);
    }

    /**
     * Displays a single IP in the view
     * @param ip IP to be displayed
     */
    @Override
    public void displayIP(IPv4Address ip) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)this.ipTree.getModel().getRoot();
        if(this.isShowingInterfaces){
            root.removeAllChildren();
            this.isShowingInterfaces = false;
        }
        root.add(new DefaultMutableTreeNode(ip.toString()));
        ((DefaultTreeModel)this.ipTree.getModel()).reload(root);
    }

     /**
     * Displays help
     */
    @Override
    public void renderHelp() {
        JOptionPane.showMessageDialog(null, "This application also proviced it's CLI, run with -h for more information!","Help", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows discovery results
     * @param ip
     */
    @Override
    public void renderInit(IPv4Address ip) {
        JOptionPane.showMessageDialog(null, "Scanning network","Discovery started", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays pinging initialization message
     * @param result
     */
    @Override
    public void renderEnd(int result) {
        JOptionPane.showMessageDialog(null, result + " IPs found!","REsults", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays an exception
     * @param e
     */
    @Override
    public void renderException(Throwable e) {
        JOptionPane.showMessageDialog(null, e.getMessage(), "Error",JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Displays an arguments error
     */
    @Override
    public void renderArgsError() {
        JOptionPane.showMessageDialog(null, "Invalid arguments", "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Exits the view
     */
    @Override
    public void exit() {
        System.exit(0);
    }
    /**
     * Creates and displays the GUI
     */
    private void createAndShowGUI() {
        this.frame = new JFrame("PingMyNetwork");
        this.frame.addWindowListener(this.windowAdapter);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JComponent component = (JComponent) this.frame.getContentPane();
        this.frame.setPreferredSize(new Dimension(800,600));
        this.frame.setJMenuBar(this.buildMenuBar());
        component.add(this.buildToolBar());
        component.add(this.buildTabs());
        component.setLayout(new BoxLayout(component, BoxLayout.Y_AXIS));
        this.frame.pack();
        this.frame.setVisible(true);
    }
    
    /**
     * Main view method
     */
    @Override
    public void main() {
          try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createAndShowGUI();
            }
        });
    }
    /**
     * Builds the menu bad
     * @return New menu bar
     */
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
    }
    /**
     * Builds tool bar
     * @return New tool bar
     */
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
    /**
     * Builds a new tree
     * @return New tree
     */
    private JPanel buildTree(){
        DefaultMutableTreeNode top =
        new DefaultMutableTreeNode("IP");
        this.ipTree = new JTree(top);
        this.ipTree.addTreeSelectionListener(this.treeListener);
        JPanel treePanel = new JPanel();
        treePanel.add(this.ipTree);
        return treePanel;
    }
    /**
     * Builds the tabbed pane
     * @return New tabbed pane
     */
    private JTabbedPane buildTabs(){
        this.tabPane = new JTabbedPane();
        this.tabPane.add("IPs",this.buildTree());
        return this.tabPane;
    }
    
    /**
     * Creates a new window
     * @param listener ActionListener for the buttons
     * @param treeListener TreeSelectionListener that changes the interface ID 
     * @param windowAdapter WindowAdapter used to handle WindowEvents
     */
    public MainWindow(ActionListener listener, TreeSelectionListener treeListener, WindowAdapter windowAdapter){
        this.treeListener = treeListener;
        this.menuListener = listener;
        this.windowAdapter = windowAdapter;
        this.isShowingInterfaces = false;
    }

    
}
