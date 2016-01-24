package pingMyNetwork.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Statement;
import java.sql.Timestamp;
import pingMyNetwork.exception.InvalidIPAddressException;

/**
 * @author Jakub Suchan
 * @version %I%, %G%
 * @since 1.0
 */
public class Scan {
    /**
     * Array of discovered IPs
     */
    private ArrayList<IPv4Address> ips;
    /**
     * Scan timestamp
     */
    private Date date;
    /**
     * IP mask
     */
    private int mask;
    /**
     * Scan id
     */
    private int id;
    /**
     * Used interface
     */
    private IPv4Address dev;
    /**
     *  Table name
     */
    public static final String TABLE_NAME = "scans";

    /**
     * Constructor
     * @param id Scan id
     * @param date Timestamp
     * @param mask IP mask
     * @param dev Interface used
     */
    private Scan(int id, Date date, int mask, IPv4Address dev) {
        this.id = id;
        this.date = date;
        this.mask = mask;
        this.dev = dev;
        this.ips = new ArrayList<IPv4Address>();
    }
    
    /**
     * Creates a table for Scan objects in the database
     * @param con Database connection
     * @throws ClassNotFoundException
     * @throws SQLException 
     */
    private void createTable(Connection con) throws ClassNotFoundException, SQLException {
        Statement statement = con.createStatement();
        statement.executeUpdate("CREATE TABLE " + Scan.TABLE_NAME + "("
                + "scanId INTEGER not null PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                + "mask INTEGER, "
                + "date TIMESTAMP,"
                + "interface VARCHAR(15))");
    }
    
    /**
     * @param date Timestamp
     * @param mask IP mask
     * @param dev Used interface
     */
    public Scan(Date date, int mask, IPv4Address dev) {
        this(-1, date, mask, dev);
    }
    
    /**
     * @param con Database connection
     * @param id Scan id
     * @throws SQLException
     * @throws InvalidIPAddressException
     */
    public Scan(Connection con, int id) throws SQLException, InvalidIPAddressException{
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("Select * from " + Scan.TABLE_NAME + " where scanID = " + id);
        rs.next();
        this.id = id;
        this.ips = new ArrayList<IPv4Address>();
        this.mask = rs.getInt("mask");
        this.date = rs.getTimestamp("date");
        this.dev = new IPv4Address(rs.getString("interface"),this.mask);
        rs = statement.executeQuery("Select * from " + IPv4Address.TABLE_NAME + " where scanID = " + this.id);
        while (rs.next()) {
            this.ips.add(new IPv4Address(rs.getString("address"),this.mask));
        }
    }

    /**
     * Returns an array of subnet IPs
     * @return IPs
     */
    public ArrayList<IPv4Address> getSubnetIPs() {
        ArrayList<IPv4Address> addressList = new ArrayList<>();
        int count = (int) Math.pow(IPv4Address.BINARY_BASE, IPv4Address.IPv4_BITS - this.mask);
        int prefix = this.dev.getRawIP() & 0xFFFFFFFF << (IPv4Address.IPv4_BITS - this.mask);
        for (int i = 0; i < count; i++) {
            addressList.add(new IPv4Address(prefix | i));
        }
        return addressList;
    }

    /**
     * Returns an array of unpopulated Scan objects
     * @param con Database connection
     * @return Unpopulated Scan objects
     * @throws SQLException
     * @throws InvalidIPAddressException
     */
    public static ArrayList<Scan> getIndex(Connection con) throws SQLException, InvalidIPAddressException {
        ArrayList<Scan> res = new ArrayList<Scan>();
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("Select * from " + Scan.TABLE_NAME);
        while (rs.next()) {
            res.add(new Scan(rs.getInt("scanId"), rs.getDate("date"), rs.getInt("mask"), new IPv4Address(rs.getString("interface"))));
        }
        return res;
    }
    
    /**
     * Saves the model in the database
     * @param con Database connection
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void save(Connection con) throws ClassNotFoundException, SQLException {
        try {
            this.createTable(con);
        } catch (SQLException e) {
            System.out.println("Table already exists");
        }
        Statement statement = con.createStatement();
        statement.executeUpdate("INSERT INTO " + Scan.TABLE_NAME + "(mask,date,interface) values(" + this.mask + ",'" + new Timestamp(this.date.getTime()).toString() + "','" + this.dev.toString() + "')", Statement.RETURN_GENERATED_KEYS );
        ResultSet rs = statement.getGeneratedKeys();
        rs.next();
        this.id = rs.getInt(1);
        for (IPv4Address value : this.ips) {
            value.save(con, id);
        }

    }

    /**
     * Adds a IP to the result list
     * @param ip IP to add
     */
    public void add(IPv4Address ip) {
        this.ips.add(ip);
    }

    /**
     * Returns online nodes
     * @return Array of online nodes
     */
    public ArrayList<IPv4Address> getOnlineNodes() {
        return this.ips;
    }
    
    /**
     * @return ID
     */
    public int getID(){
        return this.id;
    }
    
    /**
     * @return Timestamp
     */
    public Date getDate(){
        return this.date;
    }
    
    /**
     * @return IP
     */
    public IPv4Address getIP(){
        return this.dev;
    }

}
