package pingMyNetwork.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import pingMyNetwork.enums.CookieKeys;
import pingMyNetwork.enums.SessionKeys;
import pingMyNetwork.exception.InvalidIPAddressException;
import pingMyNetwork.model.IPv4Address;

/**
 *
 * @author Administrator
 */
public class PingServlet extends HistoryServlet {

    private IPv4Address currentIP;
    private static final String DEFAULT_MESSAGE = "Oh, you didn't!";

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        if (this.controller.isDiscoveryRunning()) {
            Object ip = session.getAttribute(SessionKeys.usedIP.name());
            if (this.currentIP.toString().equals(ip.toString())) {
                this.displayResults(req, resp);
            } else {
                this.busy(req, resp);
            }
        }
        else{
            String address = req.getParameter(SessionKeys.usedIP.name());
            Boolean isWaiting = (Boolean) session.getAttribute(SessionKeys.isWaiting.name());
            if(address != null && isWaiting == false){
                this.ping(req, resp,address);
                resp.addCookie(new Cookie(CookieKeys.lastUsedIP.name(),address));
                resp.addCookie(new Cookie(CookieKeys.lastScan.name(),(new Date()).toString()));
                try {
                    session.setAttribute(SessionKeys.isWaiting.name(), true);
                    session.setAttribute(SessionKeys.usedIP.name(), new IPv4Address(address));
                } catch (InvalidIPAddressException e) {
                    this.renderException(req, resp, e);
                }
            }
            else{
                this.renderForm(req, resp);
                session.setAttribute(SessionKeys.isWaiting.name(), false);
            }
        }

    }

    private void renderForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String lastDate = PingServlet.DEFAULT_MESSAGE;
        String lastIP = PingServlet.DEFAULT_MESSAGE; 
        resp.setContentType("text/html; charset=ISO-8859-2");
        PrintWriter out = resp.getWriter();
        req.getRequestDispatcher("/header.html").include(req,resp);
        Cookie[] cookies = req.getCookies();
        for(Cookie value:cookies){
            try{
            switch(CookieKeys.valueOf(value.getName())){
                case lastScan:
                    lastDate = value.getValue();
                    break;
                case lastUsedIP:
                    lastIP = value.getValue();
                    break;
            }
            }
            catch(IllegalArgumentException e){}
        }
        out.print("<div class='jumbotron'><h1> Hi! :)</h1><p> You last scanned our network at: " 
                + lastDate
                + "<br> and you used the interface with the IP: "
                + lastIP
                + "</p></div>");
        req.getRequestDispatcher("/form_top.html").include(req, resp);
        try{
        for (IPv4Address ip : this.controller.getLocalIPs()) {
            out.print("<option value='" + ip.toString() + "'>" + ip.toString() + "</option>");
        }
        }
        catch(IndexOutOfBoundsException | InvalidIPAddressException | NumberFormatException | SocketException e){
            this.renderException(req, resp, e);
        }
        req.getRequestDispatcher("/form_bottom.html").include(req, resp);
    }

    private void ping(HttpServletRequest req, HttpServletResponse resp, String ip) throws ServletException, IOException {
        try{
        this.currentIP = new IPv4Address(ip);
        }
        catch(InvalidIPAddressException e){
            
        }
        resp.setHeader("Refresh", "1");
        resp.setContentType("text/html; charset=ISO-8859-2");
        try {
            this.controller.ping(new IPv4Address(ip), 1000);

        } catch (InvalidIPAddressException e) {

        }

    }

    private void busy(HttpServletRequest req, HttpServletResponse resp) {
        try {
            resp.getWriter().print("Pinging is currently unavailable");
        } catch (IOException e) {

        }
    }
    
}
