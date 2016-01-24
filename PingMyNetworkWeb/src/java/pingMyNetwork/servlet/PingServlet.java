package pingMyNetwork.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InterfaceAddress;
import java.net.SocketException;
import java.sql.SQLException;
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
 * @author Jakub Suchan
 * @version %I%, %G%
 * @since 1.0
 */
public class PingServlet extends HistoryServlet {

    private IPv4Address currentIP;
    private static final String DEFAULT_MESSAGE = "Oh, you didn't!";
    private static final String SCANNING_MESSAGE = "Scanning...";

    /**
     * Services a GET request
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        if (this.controller.isDiscoveryRunning()) {
            Object ip = session.getAttribute(SessionKeys.usedIP.name());
            if (ip != null && this.currentIP.toString().equals(ip.toString())) {
                this.displayResults(req, resp);
            } else {
                this.busy(req, resp);
            }
        } else {
            String address = req.getParameter(SessionKeys.usedIP.name());
            Boolean isWaiting = (Boolean) session.getAttribute(SessionKeys.isWaiting.name());
            if (address != null && isWaiting != null && isWaiting == false) {
                this.ping(req, resp, address);
                resp.addCookie(new Cookie(CookieKeys.lastUsedIP.name(), address));
                resp.addCookie(new Cookie(CookieKeys.lastScan.name(), (new Date()).toString()));
                try {
                    session.setAttribute(SessionKeys.isWaiting.name(), true);
                    session.setAttribute(SessionKeys.usedIP.name(), new IPv4Address(address));
                } catch (InvalidIPAddressException e) {
                    this.log(e.getMessage());
                    this.renderException(req, resp, e);
                }
            } else {
                this.renderForm(req, resp);
                session.setAttribute(SessionKeys.isWaiting.name(), false);
            }
        }

    }
    /**
     * Renders the pinging form
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException 
     */
    private void renderForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String lastDate = PingServlet.DEFAULT_MESSAGE;
        String lastIP = PingServlet.DEFAULT_MESSAGE;
        resp.setContentType("text/html; charset=ISO-8859-2");
        PrintWriter out = resp.getWriter();
        req.getRequestDispatcher("/header.html").include(req, resp);
        Cookie[] cookies = req.getCookies();
        for (Cookie value : cookies) {
            try {
                switch (CookieKeys.valueOf(value.getName())) {
                    case lastScan:
                        lastDate = value.getValue();
                        break;
                    case lastUsedIP:
                        lastIP = value.getValue();
                        break;
                }
            } catch (IllegalArgumentException e) {
            }
        }
        out.print("<div class='jumbotron'><h1> Hi! :)</h1><p> You last scanned our network at: "
                + lastDate
                + "<br> and you used the interface with the IP: "
                + lastIP
                + "</p></div>");
        req.getRequestDispatcher("/form_top.html").include(req, resp);
        try {
            for (IPv4Address ip : this.controller.getLocalIPs()) {
                out.print("<option value='" + ip.toString() + "'>" + ip.toString() + "</option>");
            }
        } catch (IndexOutOfBoundsException | InvalidIPAddressException | NumberFormatException | SocketException e) {
            this.log(e.getMessage());
            this.renderException(req, resp, e);
        }
        req.getRequestDispatcher("/form_bottom.html").include(req, resp);
    }
    /**
     * Starts the discovery
     * @param req
     * @param resp
     * @param ip Interface IP
     * @throws ServletException
     * @throws IOException 
     */
    private void ping(HttpServletRequest req, HttpServletResponse resp, String ip) throws ServletException, IOException {
        try {
            for (IPv4Address value : this.controller.getLocalIPs()) {
                if (ip.equals(value.toString())) {
                    this.currentIP = value;
                    break;
                }
            }
        } catch (InvalidIPAddressException e) {
            this.log(e.getMessage());
            this.renderException(req, resp, e);
        }
        resp.setHeader("Refresh", "1");
        resp.setContentType("text/html; charset=ISO-8859-2");
        this.controller.ping(this.currentIP, 1000);
    }
    /**
     * Displays a busy message
     * @param req
     * @param resp 
     */
    private void busy(HttpServletRequest req, HttpServletResponse resp) {
        try {
            req.getRequestDispatcher("/header.html").include(req, resp);
            req.getRequestDispatcher("/busy.html").include(req, resp);
        } catch (ServletException | IOException e) {
            this.log(e.getMessage());
            this.renderException(req, resp, e);
        }
    }

    /**
     * Displays the results of the currently running discovery
     * @param req
     * @param resp
     * @throws ServletException
     */
    @Override
    protected void displayResults(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            String msg = PingServlet.SCANNING_MESSAGE;
            resp.setHeader("Refresh", "5");
            PrintWriter out = resp.getWriter();
            req.getRequestDispatcher("/header.html").include(req, resp);
            out.print("<div class='container'>"
                    + "<div class='jumbotron'><h1>" + msg + "</h1></div>");
            req.getRequestDispatcher("/results_top.html").include(req, resp);
            for (IPv4Address value : this.controller.getResults()) {
                out.println("<tr><td>"
                        + value.toString() + "</td></tr>");

            }
            req.getRequestDispatcher("/results_bottom.html").include(req, resp);
        } catch (IOException | SQLException | InvalidIPAddressException e) {
            this.log(e.getMessage());
            this.renderException(req, resp, e);
        }
    }

}
