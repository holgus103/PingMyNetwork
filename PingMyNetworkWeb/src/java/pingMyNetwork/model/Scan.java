package pingMyNetwork.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Statement;
import java.sql.Timestamp;
import pingMyNetwork.exception.InvalidIPAddressException;
import pingMyNetwork.model.IPv4Address;

public class Scan {

    private ArrayList<IPv4Address> ips;
    private Date date;
    private int mask;
    private int id;
    public static final String TABLE_NAME = "scans";

    private Scan(int id, Date date, int mask) {
        this.id = id;
        this.date = date;
        this.mask = mask;
        this.ips = new ArrayList<IPv4Address>();
    }
    
    
    private void createTable(Connection con) throws ClassNotFoundException, SQLException {
        Statement statement = con.createStatement();
        statement.executeUpdate("CREATE TABLE " + Scan.TABLE_NAME + "("
                + "scanId INTEGER not null PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                + "mask INTEGER, "
                + "date TIMESTAMP");
    }

    private void populate(Connection con) throws SQLException, InvalidIPAddressException {
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("Select * from " + IPv4Address.TABLE_NAME + " where scanID = " + this.id);
        while (rs.next()) {
            this.ips.add(new IPv4Address(rs.getString("address")));
        }

    }
    
    public Scan(Date date, int mask) {
        this.id = -1;
        this.date = date;
        this.mask = mask;
        this.ips = new ArrayList<IPv4Address>();
    }

    public ArrayList<IPv4Address> getSubnetIPs(IPv4Address address) {
        ArrayList<IPv4Address> addressList = new ArrayList<>();
        int count = (int) Math.pow(IPv4Address.BINARY_BASE, IPv4Address.IPv4_BITS - this.mask);
        int prefix = address.getRawIP() & 0xFFFFFFFF << (IPv4Address.IPv4_BITS - this.mask);
        for (int i = 0; i < count; i++) {
            addressList.add(new IPv4Address(prefix | i));
        }
        return addressList;
    }

    public static ArrayList<Scan> getIndex(Connection con) throws SQLException {
        ArrayList<Scan> res = new ArrayList<Scan>();
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery("Select * from " + Scan.TABLE_NAME);
        while (rs.next()) {
            res.add(new Scan(rs.getInt("scanId"), rs.getDate("date"), rs.getInt("mask")));
        }
        return res;
    }

    public void save(Connection con) throws ClassNotFoundException, SQLException {
        try {
            this.createTable(con);
        } catch (SQLException e) {
            System.out.println("Table already exists");
        }
        Statement statement = con.createStatement();
        statement.executeUpdate("INSERT INTO " + Scan.TABLE_NAME + "(mask,date) values(" + this.mask + ",'" + new Timestamp(this.date.getTime()).toString() + "')", Statement.RETURN_GENERATED_KEYS );
        ResultSet rs = statement.getGeneratedKeys();
        rs.next();
        this.id = rs.getInt(1);
        for (IPv4Address value : this.ips) {
            value.save(con, id);
        }

    }

    public void add(IPv4Address ip) {
        this.ips.add(ip);
    }

    public ArrayList<IPv4Address> getOnlineNodes(Connection con)throws SQLException,InvalidIPAddressException {
        if(this.ips.isEmpty())
            this.populate(con);
        return this.ips;
    }

}
