import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet(name = "LogoutServlet")
public class LogoutServlet extends HttpServlet {

    private Logger logger;

    @Override
    public void init() throws ServletException {
        logger = Logger.getLogger(getClass().getName());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null)
            session.invalidate();
        resp.sendRedirect(req.getContextPath());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session != null)
            session.invalidate();

        String type = request.getAttribute("type").toString();
        request.removeAttribute("type");
        switch (type) {
            case "updateError":
                response.getWriter().print(
                    "<html>" +
                        "<head>" +
                            "<title>Update error</title>" +
                        "</head>" +
                        "<body>" +
                            "<style type=\"text/css\">" +
                            "p {margin: 2%; font-size: x-large;}" +
                            "</style>" +
                            "<p>Invalid current password. " +
                            "Please <a href='login'>try again</a>.</p>" +
                        "</body>" +
                    "</html>"
                );
                break;
            case "updateSuccess":
                response.getWriter().print(
                    "<html>" +
                        "<head>" +
                            "<title>Update success</title>" +
                        "</head>" +
                        "<body>" +
                            "<style type=\"text/css\">" +
                            "p {margin: 2%; font-size: x-large;}" +
                            "</style>" +
                            "<p>Password changed successfully. " +
                            "Please <a href='login'>login again</a> to continue.</p>" +
                        "</body>" +
                    "</html>"
                );
                break;
        }
    }

    @Override
    public void destroy() {
        logger = null;
    }
}
