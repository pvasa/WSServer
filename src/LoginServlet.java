import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Logger;

@WebServlet(loadOnStartup = 1, name = "LoginServlet")
public class LoginServlet extends HttpServlet {

    private Logger logger;

    @Override
    public void init() throws ServletException {
        logger = Logger.getLogger(getClass().getName());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");

        String formClientId = request.getParameter("clientId");
        String password = request.getParameter("password");
        String password_hash, salt = "";

        Connection connection = null;
        PreparedStatement pStatement = null;
        ResultSet resultSet = null;
        String query;

        try {
            DataSource dataSource = (DataSource) new InitialContext().lookup("java:jboss/datasources/MySQLDS");

            connection = dataSource.getConnection();

            //noinspection SqlNoDataSourceInspection,SqlResolve
            query = "SELECT SALT FROM clients WHERE EMAIL_ID=?";
            pStatement = connection.prepareStatement(query);
            pStatement.setString(1, formClientId);
            resultSet = pStatement.executeQuery();
            while (resultSet.next()) {
                salt = resultSet.getString("SALT");
            }
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 65536, 256);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            byte[] hash = f.generateSecret(spec).getEncoded();
            Base64.Encoder enc = Base64.getEncoder();
            password_hash = enc.encodeToString(hash);

            //noinspection SqlNoDataSourceInspection,SqlResolve
            query = "SELECT EMAIL_ID,CONFIRMED,ACTIVATED FROM clients WHERE PASSWORD_HASH=?";
            pStatement = connection.prepareStatement(query);
            pStatement.setString(1, password_hash);
            resultSet = pStatement.executeQuery();

            int count = 0, confirmed = 0, activated = 0;
            String clientId = "";
            while (resultSet.next()) {
                count++;
                clientId = resultSet.getString("EMAIL_ID");
                confirmed = resultSet.getInt("CONFIRMED");
                activated = resultSet.getInt("ACTIVATED");
            }

            if (count == 1 && formClientId.equals(clientId) && confirmed == 1 && activated == 1) {
                request.getSession().setAttribute("clientId", formClientId);
                response.sendRedirect("client");
            } else if (count < 1) {
                response.getWriter().print(
                    "<html>" +
                        "<head>" +
                            "<title>Error</title>" +
                        "</head>" +
                        "<body>" +
                            "<style type=\"text/css\">" +
                                "p {margin: 2%; font-size: x-large;}" +
                            "</style>" +
                            "<p>Invalid email id or password. " +
                            "Please try to <a href='login'>login again</a>" +
                            " or <a href='register'>register</a>.</p>" +
                        "</body>" +
                    "</html>"
                );
            } else if (confirmed == 0) {
                response.getWriter().print(
                    "<html>" +
                        "<head>" +
                            "<title>Error</title>" +
                        "</head>" +
                        "<body>" +
                            "<style type=\"text/css\">" +
                                "p {margin: 2%; font-size: x-large;}" +
                            "</style>" +
                            "<p>Your email id is not confirmed. Please check your mail.</p>" +
                        "</body>" +
                    "</html>"
                );
            } else if (activated == 0) {
                response.getWriter().print(
                    "<html>" +
                        "<head>" +
                            "<title>Error</title>" +
                        "</head>" +
                        "<body>" +
                            "<style type=\"text/css\">" +
                                "p {margin: 2%; font-size: x-large;}" +
                            "</style>" +
                            "<p>Your account is not yet activated. " +
                            "It takes up-to 24 hrs after you confirm your email id. " +
                            "If it has been more than that, please contact administrator at watcher.animus652@gmail.com</p>" +
                        "</body>" +
                    "</html>"
                );
            }

        } catch (SQLException | NamingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
                if (pStatement != null)
                    pStatement.close();
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("login.html").forward(request, response);
    }

    @Override
    public void destroy() {
        logger = null;
    }
}
