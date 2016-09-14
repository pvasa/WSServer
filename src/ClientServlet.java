import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
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
import java.util.HashSet;
import java.util.logging.Logger;

@WebServlet(name = "ClientServlet")
public class ClientServlet extends HttpServlet {

    private Logger logger;

    @Override
    public void init() throws ServletException {
        logger = Logger.getLogger(getClass().getName());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String clientId = request.getSession(false).getAttribute(Strings.clientId).toString();
        String newPassword;
        if ( (newPassword = request.getParameter("newPassword")) != null) {

            String oldPassword = request.getParameter("password");
            String salt = "", oldPassword_hash, newPassword_hash;

            Connection connection = null;
            PreparedStatement pStatement = null;
            ResultSet resultSet;
            String query;

            try {
                DataSource dataSource = (DataSource) new InitialContext().lookup("java:jboss/datasources/MySQLDS");
                connection = dataSource.getConnection();

                //noinspection SqlNoDataSourceInspection,SqlResolve
                query = "SELECT SALT FROM clients WHERE EMAIL_ID=?";
                pStatement = connection.prepareStatement(query);
                pStatement.setString(1, clientId);
                resultSet = pStatement.executeQuery();
                while (resultSet.next()) {
                    salt = resultSet.getString("SALT");
                }
                byte[] saltBytes = Base64.getDecoder().decode(salt);
                KeySpec spec = new PBEKeySpec(oldPassword.toCharArray(), saltBytes, 65536, 256);
                SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
                byte[] hash = f.generateSecret(spec).getEncoded();
                Base64.Encoder enc = Base64.getEncoder();
                oldPassword_hash = enc.encodeToString(hash);

                spec = new PBEKeySpec(newPassword.toCharArray(), saltBytes, 65536, 256);
                f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
                hash = f.generateSecret(spec).getEncoded();
                enc = Base64.getEncoder();
                newPassword_hash = enc.encodeToString(hash);

                //noinspection SqlNoDataSourceInspection,SqlResolve
                query = "UPDATE clients SET PASSWORD=?,PASSWORD_HASH=? WHERE EMAIL_ID=? AND PASSWORD_HASH=?";
                pStatement = connection.prepareStatement(query);
                pStatement.setString(1, newPassword);
                pStatement.setString(2, newPassword_hash);
                pStatement.setString(3, clientId);
                pStatement.setString(4, oldPassword_hash);

                if (pStatement.executeUpdate() <= 0)
                    request.setAttribute("type", "updateError");
                else request.setAttribute("type", "updateSuccess");

                request.getRequestDispatcher("logout").forward(request, response);

            } catch (SQLException | NamingException | NoSuchAlgorithmException | InvalidKeySpecException e) {
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
        } else {
            StringBuilder deviceList = new StringBuilder();
            HashSet<WSConns.Device> devices = WSConns.Device.getDevicesOf(clientId, false);
            if (!devices.isEmpty()) {
                for (WSConns.Device device : devices) {
                    deviceList.append(device.getId()).append("-").append(device.getName()).append(",");
                }
                deviceList.deleteCharAt(deviceList.lastIndexOf(","));
            }
            deviceList.append('|');
            devices = WSConns.Device.getDevicesOf(clientId, true);
            if (!devices.isEmpty()) {
                for (WSConns.Device device : devices) {
                    deviceList.append(device.getId()).append("-").append(device.getName()).append(",");
                }
                deviceList.deleteCharAt(deviceList.lastIndexOf(","));
            }
            request.setAttribute("devices", deviceList.toString());
            request.getRequestDispatcher("client.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute(Strings.clientId) != null)
            doPost(req, resp);
    }

    @Override
    public void destroy() {
        logger = null;
    }
}
