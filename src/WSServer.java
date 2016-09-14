import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
@ServerEndpoint(value = "/wss")
public class WSServer {

    private static final Logger logger = Logger.getLogger(WSServer.class.getName());

    static SecureRandom random = new SecureRandom();

    static final String jsonType = "type";

    static final String typeCommand = "command";

    static final String typeCheck = "check";
    static final String typeStatus = "status";

    static final String typeError = "error";

    static final String typeState = "state";
    static final String typeResult = "result";
    static final String typeLive = "live";
    static final String typeFile = "file";
    static final String typeFileManager = "fileManager";

    @OnMessage
    public void onMessage (Session session, String jsonMessage) throws IOException {

        JsonObject jsonMessageObject = new JsonParser().parse(jsonMessage).getAsJsonObject();
        String clientId, deviceId, name;
        WSConns.Client client;

        switch (jsonMessageObject.get(jsonType).getAsString()) {

            case typeCheck:
                clientId = jsonMessageObject.get(Strings.clientId).getAsString();
                client = WSConns.Client.get(clientId);
                String status = "";
                JsonObject statusJsonObject = new JsonObject();
                statusJsonObject.addProperty(jsonType, typeStatus);

                statusJsonObject.addProperty(typeStatus, status);
                if (client != null && client.getSession().isOpen())
                    send(client.getSession(), statusJsonObject);
                break;

            case WSConns.typeDevice:
                deviceId = jsonMessageObject.get(Strings.deviceId).getAsString();
                clientId = jsonMessageObject.get(Strings.clientId).getAsString();
                name = jsonMessageObject.get("name").getAsString();

                new WSConns.Device(deviceId, session, name, clientId);

                logger.info("\n#--DEVICE CONNECTED--#" +
                        "\nSESSION ID: " + session.getId() +
                        "\nDEVICE ID: " + deviceId +
                        "\nNAME: " + name +
                        "\nCLIENT ID: " + clientId);
                break;

            case WSConns.typeClient:
                clientId = jsonMessageObject.get(Strings.clientId).getAsString();
                name = jsonMessageObject.get("name").getAsString();

                new WSConns.Client(clientId, session, name);

                logger.info( "\n#--CLIENT CONNECTED--#" +
                        "\nSESSION ID: " + session.getId() +
                        "\nCLIENT ID: " + clientId +
                        "\nNAME: " + name);
                break;

            case typeCommand:
                WSConns.Device device;
                deviceId = jsonMessageObject.get(Strings.deviceId).getAsString();
                device = WSConns.Device.get(deviceId);
                jsonMessageObject.remove(jsonType);
                if (device != null && device.getSession().isOpen()) {
                    jsonMessageObject.remove(Strings.deviceId);
                    send(device.getSession(), jsonMessageObject);
                } else {
                    jsonMessageObject.addProperty(jsonType, typeError);
                    jsonMessageObject.addProperty(typeError, deviceId + " is offline.");
                    send(session, jsonMessageObject);
                }
                break;

            case typeState:
            case typeResult:
            case typeError:
            case typeLive:
            case typeFileManager:
                clientId = jsonMessageObject.get(Strings.clientId).getAsString();
                client = WSConns.Client.get(clientId);
                jsonMessageObject.remove(Strings.clientId);
                if (client != null && client.getSession().isOpen())
                    send(client.getSession(), jsonMessageObject);
                break;
        }
    }

    @OnClose
    public void onClose (Session session, CloseReason reason) throws IOException {
        WSConns.Device device;
        WSConns.Client client;

        if ( (client = WSConns.Client.get(session)) != null )
            logger.info("\n#--CLIENT DISCONNECTED--#" +
                    "\nSESSION ID: " + session.getId() +
                    "\nCLIENT ID: " + client.getId());
        else if ( (device = WSConns.Device.get(session)) != null )
            logger.info("\n#--DEVICE DISCONNECTED--#" +
                    "\nSESSION ID: " + session.getId() +
                    "\nDEVICE ID: " + device.getId());
        logger.info("\nREASON: " + reason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable e) {
        WSConns.Device device;
        WSConns.Client client;
        if ( (client = WSConns.Client.get(session)) != null )
            logger.severe("\nCLIENT ERROR: " + client.getId() + ": " + e.toString());
        else if ( (device = WSConns.Device.get(session)) != null )
            logger.severe("\nDEVICE ERROR: " + device.getId() + ": " + e.toString());
    }

    static void send(Session session, JsonObject jsonMessageObject) throws IOException {
        session.getBasicRemote().sendText(jsonMessageObject.toString());
    }

    /*private String getKeyByValue (HashMap<String, Session> map, Session value) {
        for (Map.Entry<String, Session> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }*/
}
