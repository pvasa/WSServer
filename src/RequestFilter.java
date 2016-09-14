import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

@WebFilter(filterName = "RequestFilter")
public class RequestFilter implements Filter {

    private Logger logger;

    public void init(FilterConfig config) throws ServletException {
        logger = Logger.getLogger(getClass().getName());
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        HttpSession session = request.getSession(false);

        String contextPath = request.getContextPath();
        String requestedURI = request.getRequestURI();

        String indexURI = contextPath + "/";
        String loginURI = contextPath + "/login";
        String registerURI = contextPath + "/register";
        String clientURI = contextPath + "/client";
        String downloadURI = contextPath + "/download";

        if (requestedURI.endsWith("index.html")) {
            response.sendRedirect(indexURI);
            return;
        }
        else if (requestedURI.endsWith("login.html")) {
            response.sendRedirect(loginURI);
            return;
        }
        else if (requestedURI.endsWith("register.html")) {
            response.sendRedirect(registerURI);
            return;
        }
        else if (requestedURI.endsWith("client.jsp")) {
            response.sendRedirect(clientURI);
            return;
        }

        /*logger.info("\nREQUEST: " + requestedURI +
                "\nINDEX: " + indexURI +
                "\nLOGIN: " + loginURI +
                "\nCLIENT: " + clientURI);*/

        boolean loggedIn = session != null && session.getAttribute(Strings.clientId) != null;
        //boolean indexRequest = requestedURI.equals(indexURI);
        boolean loginRequest = requestedURI.equals(loginURI);
        boolean registerRequest = requestedURI.equals(registerURI);
        boolean clientRequest = requestedURI.equals(clientURI);
        boolean downloadRequest = requestedURI.equals(downloadURI);

        if ( loggedIn && (loginRequest || registerRequest) ) {
            response.sendRedirect(clientURI);
        }
        else if (!loggedIn && (clientRequest || downloadRequest)) {
            response.sendRedirect(loginURI);
        }
        else {
            chain.doFilter(request, response);
        }
    }

    public void destroy() {
        logger = null;
    }
}
