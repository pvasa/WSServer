import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@WebFilter(filterName = "SSLFilter")
public class SSLFilter implements Filter {

    private Logger logger;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger = Logger.getLogger(getClass().getName());
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        if ( req.isSecure() || ((HttpServletRequest)req).getRequestURI().endsWith("/wss") ) {
            chain.doFilter(req, resp);
        }
        else {
            String requestURL = ((HttpServletRequest)req).getRequestURL().toString();
            ((HttpServletResponse)resp).sendRedirect( requestURL.replace("http", "https") );
        }
    }

    public void destroy() {
        logger = null;
    }
}
