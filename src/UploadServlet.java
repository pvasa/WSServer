import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet(loadOnStartup = 1, name = "UploadServlet")
@MultipartConfig
public class UploadServlet extends HttpServlet {

    private Logger logger;

    @Override
    public void init() throws ServletException {
        logger = Logger.getLogger(getClass().getName());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String deviceId = request.getParameter(Strings.deviceId);

            String clientId = request.getParameter(Strings.clientId);

            String fileType = request.getParameter(Strings.fileType);

            String fileName = request.getParameter(Strings.fileName);
            String filePath = System.getProperty("jboss.server.data.dir")
                    + "/clientData/" + clientId + File.separator + deviceId + File.separator + fileType + File.separator;

            File file = new File(filePath);
            if ( !file.exists() && !file.mkdirs() )
                logger.severe("CANNOT CREATE DIRECTORY: " + filePath);

            file = new File(filePath + fileName);
            if (file.exists())
                file.delete();
            if ( !file.createNewFile() ) {
                logger.severe("CANNOT CREATE FILE: " + file.getAbsolutePath());
                return;
            }

            BufferedInputStream inputStream = new BufferedInputStream(request.getPart("file").getInputStream());
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            // sends response to client
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print("File received.");

            if ( !fileType.equals(Strings.fileTypeLog) && !fileType.equals(Strings.fileTypeWAKey) ) {
                JsonObject object = new JsonObject();
                object.addProperty(WSServer.jsonType, WSServer.typeFile);
                object.addProperty(Strings.deviceId, deviceId);
                object.addProperty(Strings.fileType, fileType);
                object.addProperty(Strings.fileName, file.getName());
                WSConns.Client client = WSConns.Client.get(clientId);
                if (client != null && client.getSession().isOpen())
                    WSServer.send(client.getSession(), object);
            }
        } catch (Exception e) {
            logger.severe(e.toString());
        }
    }

    @Override
    public void destroy() {
        logger = null;
    }
}
