package pingMyNetwork.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketException;
import java.sql.SQLException;
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

public class HistoryServlet extends HttpServlet {

    private static final String DEFAULT_MESSAGE = "Most recent results: ";
    private static final String SCANNING_MESSAGE = "Scanning...";
    protected PingController controller;

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.displayResults(req, resp);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }

    protected void renderException(HttpServletRequest req, HttpServletResponse resp, Throwable t) {
        try {
            resp.getWriter().print(t.getMessage());
        } catch (IOException e) {
            this.log(e.getMessage());
        }
    }

    protected void displayResults(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
        try {
            String msg = HistoryServlet.DEFAULT_MESSAGE;
            if (this.controller.isDiscoveryRunning()) {
                resp.setHeader("Refresh", "5");
                msg = HistoryServlet.SCANNING_MESSAGE;
            }
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
        } catch (IOException |SQLException | InvalidIPAddressException e) {
            this.log(e.getMessage());
            this.renderException(req, resp, e);
        }
    }
}
