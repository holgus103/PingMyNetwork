package pingMyNetwork.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import pingMyNetwork.controller.PingController;
import pingMyNetwork.enums.SessionKeys;
import pingMyNetwork.exception.InvalidIPAddressException;
import pingMyNetwork.model.IPv4Address;
import pingMyNetwork.model.Scan;

/**
 * @author Jakub Suchan
 * @version %I%, %G%
 * @since 1.0
 */
public class HistoryServlet extends HttpServlet {
    
    /**
     * Default message
     */
    private static final String DEFAULT_MESSAGE = "Most recent results: ";
    /**
     * App controller
     */
    protected PingController controller;
    /**
     * Initializes the Servlet
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            Context env = (Context) new InitialContext().lookup("java:comp/env");

            ServletContext context = this.getServletContext();
            try {
                this.controller = (PingController) context.getAttribute(SessionKeys.PingController.name());
            } catch (ClassCastException e) {
                this.log(e.getMessage());
            }
            if (this.controller == null) {
                try {
                    this.controller = new PingController((String) env.lookup("dbUrl"), (String) env.lookup("dbUser"), (String) env.lookup("dbPassword"));
                } catch (InvalidIPAddressException | SocketException | IndexOutOfBoundsException | NumberFormatException | SQLException | ClassNotFoundException e) {
                    this.log(e.getMessage());
                }
                context.setAttribute(SessionKeys.PingController.name(), this.controller);
            }
        } catch (NamingException e) {
            this.log(e.getMessage());
        }
    }

    /**
     * Services a GET request
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.displayResults(req, resp);

    }

    /**
     * Services a POST request
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }

    /**
     * Displays a exception message
     * @param req
     * @param resp
     * @param t
     */
    protected void renderException(HttpServletRequest req, HttpServletResponse resp, Throwable t) {
        try {
            resp.getWriter().print(t.getMessage());
        } catch (IOException e) {
            this.log(e.getMessage());
        }
    }

    /**
     * Displays the scanning results
     * @param req
     * @param resp
     * @throws ServletException
     */
    protected void displayResults(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            PrintWriter out = resp.getWriter();
            req.getRequestDispatcher("/header.html").include(req, resp);
//            out.print("<div class='container'>"
//                    + "<div class='jumbotron'><h1>" + msg + "</h1></div>");
            int id;
            try {
                id = Integer.parseInt(req.getParameter("id"));
            } catch (NumberFormatException e) {
                id = 0;
            }
            if (id == 0) //            String msg = HistoryServlet.DEFAULT_MESSAGE;
            {
                req.getRequestDispatcher("/results_top.html").include(req, resp);
                for (Scan value : this.controller.getScanIndex()) {
                    out.println("<tr><td><a href='history?id=" + value.getID() + "'>"
                            + value.getDate() + "</a></td></tr>");

                }
            }
            else{
                Scan scan = this.controller.loadScan(id);
                out.println("<h3>" + scan.getDate() + "</h1>");
                out.println("<h3>" + scan.getIP().toString() + "</h3>");
                req.getRequestDispatcher("/results_top.html").include(req, resp);    
                for(IPv4Address value:scan.getOnlineNodes()){
                    out.println("<tr><td>" + value.toString() + "</td></tr>");
                }
            }   
            req.getRequestDispatcher("/results_bottom.html").include(req, resp);
        } catch (IOException | SQLException | InvalidIPAddressException e) {
            this.log(e.getMessage());
            this.renderException(req, resp, e);
        }
    }
}
