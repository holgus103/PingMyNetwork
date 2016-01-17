package pingMyNetwork.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import pingMyNetwork.controller.PingController;
import pingMyNetwork.enums.SessionKeys;
import pingMyNetwork.model.IPv4Address;

public class HistoryServlet extends HttpServlet {

    protected PingController controller;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = this.getServletContext();
        try{
            this.controller =  (PingController) context.getAttribute(SessionKeys.PingController.name());
        }
        catch(ClassCastException e){
            this.log(e.getMessage());
        }
        if(this.controller == null){
            this.controller = new PingController();
            context.setAttribute(SessionKeys.PingController.name(), this.controller);
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
    
    protected void renderException(HttpServletRequest req, HttpServletResponse resp, Throwable t){
        try{
            resp.getWriter().print(t.getMessage());
        }
        catch(IOException e){
            this.log(e.getMessage());
        }
    }
    
    protected void displayResults(HttpServletRequest req, HttpServletResponse resp) throws ServletException{
        try {
            resp.setHeader("Refresh", "5");
            PrintWriter out = resp.getWriter();
            req.getRequestDispatcher("/header.html").include(req,resp);
            req.getRequestDispatcher("/results_top.html").include(req,resp);
            for (IPv4Address value : this.controller.getResults()) {
                out.println("<tr><td>"
                        + value.toString() + "</td></tr>");

            }
            req.getRequestDispatcher("/results_bottom.html").include(req,resp);
        } catch (IOException e) {

        }
    }
}
