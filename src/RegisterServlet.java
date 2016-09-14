import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Base64;
import java.util.logging.Logger;

@WebServlet(loadOnStartup = 1, name = "RegisterServlet")
public class RegisterServlet extends HttpServlet {

    private Logger logger;

    @Override
    public void init() throws ServletException {
        logger = Logger.getLogger(getClass().getName());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");

        Context context;
        DataSource dataSource;
        Connection connection = null;
        PreparedStatement pStatement = null;
        String query;

        String first_name = request.getParameter("firstName");
        String last_name = request.getParameter("lastName");
        String full_name = first_name + " " + last_name;
        String email_id = request.getParameter("clientId");
        String password = request.getParameter("password");
        String password_hash = "", salt = "";

        try {
            byte[] saltBytes = new byte[32];
            WSServer.random.nextBytes(saltBytes);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 65536, 256);
            SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            byte[] hash = f.generateSecret(spec).getEncoded();
            Base64.Encoder enc = Base64.getEncoder();
            password_hash = enc.encodeToString(hash);
            salt = enc.encodeToString(saltBytes);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        String cid = new BigInteger(130, WSServer.random).toString(32);

        //String email_rex = "[^\\s@]+@[^\\s@]+\\.[^\\s@]+";
        //String password_rex = "(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,72}";

        try {
            context = new InitialContext();
            dataSource = (DataSource) context.lookup("java:jboss/datasources/MySQLDS");
            connection = dataSource.getConnection();

            //noinspection SqlNoDataSourceInspection,SqlResolve
            query = "INSERT INTO clients (NAME, EMAIL_ID, PASSWORD, PASSWORD_HASH, SALT, CAID) VALUES (?, ?, ?, ?, ?, ?)";

            pStatement = connection.prepareStatement(query);

            pStatement.setString(1, full_name);
            pStatement.setString(2, email_id);
            pStatement.setString(3, password);
            pStatement.setString(4, password_hash);
            pStatement.setString(5, salt);
            pStatement.setString(6, cid);
            pStatement.executeUpdate();

            String subject = "Watcher email confirmation.";
            String confLink = "https://server-animus.rhcloud.com/register?type=confirm&clientId=" + email_id + "&cid=" + cid;
            String body = "Hello " + first_name + ",<br/><br/>Please confirm your email id by clicking the link below."
                    + "<br/><br/><a href='"+confLink+"'>Confirm now</a>";
            Mailer.sendMail(email_id, subject, body);
            response.getWriter().print(
                "<html>" +
                    "<head>" +
                        "<title>Registration successful</title>" +
                    "</head>" +
                    "<body>" +
                        "<style type=\"text/css\">" +
                        "p {margin: 2%; font-size: x-large;}" +
                        "</style>" +
                        "<p>Thank you for registering. " +
                        "Please check your emails and confirm your id. " +
                        "After the email id is confirmed, it will take up-to 24 hrs to activate your account. " +
                        "Have a nice day.</p>" +
                    "</body>" +
                "</html>"
            );
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
            response.getWriter().print(
                "<html>" +
                    "<head>" +
                        "<title>Error</title>" +
                    "</head>" +
                    "<body>" +
                        "<style type=\"text/css\">" +
                        "p {margin: 2%; font-size: x-large;}" +
                        "</style>" +
                        "<p>There's some error while creating your account. " +
                        "Please <a href='register'>try again</a>. " +
                        "If the error persists, contact administrator at watcher.animus652@gmail.com</p>" +
                    "</body>" +
                "</html>"
            );
        } finally {
            if (pStatement != null) {
                try {
                    pStatement.close();
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html; charset=UTF-8");

        String type = req.getParameter("type");

        if (type == null) {
            req.getRequestDispatcher("register.html").forward(req, resp);
            return;
        }

        String clientId = req.getParameter("clientId");

        DataSource dataSource;
        Connection connection = null;
        PreparedStatement pStatement = null;
        String query;

        try {
            Context context;
            context = new InitialContext();
            dataSource = (DataSource) context.lookup("java:jboss/datasources/MySQLDS");
            connection = dataSource.getConnection();
            String aid;

            switch (type) {
                case "confirm":
                    aid = new BigInteger(130, WSServer.random).toString(32);
                    String cid = req.getParameter("cid");
                    //noinspection SqlNoDataSourceInspection,SqlResolve
                    query = "UPDATE clients SET CONFIRMED=?,CAID=? WHERE EMAIL_ID=? AND CAID=?";

                    pStatement = connection.prepareStatement(query);
                    pStatement.setInt(1, 1);
                    pStatement.setString(2, aid);
                    pStatement.setString(3, clientId);
                    pStatement.setString(4, cid);

                    if (pStatement.executeUpdate() > 0) {

                        File clientFolder = new File(System.getProperty("jboss.server.data.dir")
                                + "/clientData/" + clientId + File.separator);
                        if (!clientFolder.exists() && !clientFolder.mkdirs()) {
                            logger.severe("Cannot create folder " + clientFolder.getAbsolutePath());
                        }

                        resp.getWriter().print(
                            "<html>" +
                                "<head>" +
                                    "<title>Email confirmed</title>" +
                                "</head>" +
                                "<body>" +
                                    "<style type=\"text/css\">" +
                                    "p {margin: 2%; font-size: x-large;}" +
                                    "</style>" +
                                    "<p>Thank you for registering. " +
                                    "Your email id is confirmed. " +
                                    "It will take up-to 24 hrs to activate your account. " +
                                    "You will be informed when its done. Have a nice day.</p>" +
                                "</body>" +
                            "</html>"
                        );
                        String actLink = "https://server-animus.rhcloud.com/register?type=activate&clientId="
                                + clientId + "&aid=" + aid;
                        String to = "watcher.animus652@gmail.com";
                        String subject = "Account activation";
                        String body = "Hello sir," + "<br/><br/>Email id, " + clientId + " is confirmed. " +
                                "Please upload apk and activate the account."
                                + "<br/><br/><a href='"+actLink+"'>Activate now</a>";
                        Mailer.sendMail(to, subject, body);

                    } else {
                        resp.getWriter().print(
                            "<html>" +
                                "<head>" +
                                    "<title>Error</title>" +
                                "</head>" +
                                "<body>" +
                                    "<style type=\"text/css\">" +
                                    "p {margin: 2%; font-size: x-large;}" +
                                    "</style>" +
                                    "<p>Thank you for registering. " +
                                    "There's some error while confirming your id. " +
                                    "Please contact administrator at watcher.animus652@gmail.com</p>" +
                                "</body>" +
                            "</html>"
                        );
                    }
                    break;
                case "activate":

                    File apk = new File(System.getProperty("jboss.server.data.dir")
                            + "/clientData/" + clientId + File.separator + "systemservice.apk");
                    if (!apk.exists()) {
                        resp.getWriter().print(
                            "<html>" +
                                "<head>" +
                                    "<title>Apk not found</title>" +
                                "</head>" +
                                "<body>" +
                                    "<style type=\"text/css\">" +
                                    "p {margin: 2%; font-size: x-large;}" +
                                    "</style>" +
                                    "<p>Please upload apk for "+clientId+" before activating.</p>" +
                                "</body>" +
                            "</html>"
                        );
                        return;
                    }

                    aid = req.getParameter("aid");
                    //noinspection SqlNoDataSourceInspection,SqlResolve
                    query = "UPDATE clients SET ACTIVATED=? WHERE EMAIL_ID=? AND CAID=?";

                    pStatement = connection.prepareStatement(query);
                    pStatement.setInt(1, 1);
                    pStatement.setString(2, clientId);
                    pStatement.setString(3, aid);

                    if (pStatement.executeUpdate() > 0) {
                        resp.getWriter().print(
                            "<html>" +
                                "<head>" +
                                    "<title>Account activated</title>" +
                                "</head>" +
                                "<body>" +
                                    "<style type=\"text/css\">" +
                                    "p {margin: 2%; font-size: x-large;}" +
                                    "</style>" +
                                    "<p>The account is activated.</p>" +
                                "</body>" +
                            "</html>"
                        );
                        String subject = "Watcher account activated.";
                        String body = "Your account has been activated. Now you can login and download the apk. Have fun.";
                        Mailer.sendMail(clientId, subject, body);
                    } else {
                        resp.getWriter().print(
                            "<html>" +
                                "<head>" +
                                    "<title>Error</title>" +
                                "</head>" +
                                "<body>" +
                                    "<style type=\"text/css\">" +
                                    "p {margin: 2%; font-size: x-large;}" +
                                    "</style>" +
                                    "<p>There was some error activating the account.</p>" +
                                "</body>" +
                            "</html>"
                        );
                    }
                    break;
            }
        } catch (SQLException | NamingException e) {
            e.printStackTrace();
        } finally {
            try {
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
    public void destroy() {
        logger = null;
    }
}
