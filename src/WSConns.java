import javax.websocket.Session;
import java.util.HashSet;
import java.util.logging.Logger;

class WSConns {

    private Logger logger = Logger.getLogger(getClass().getName());

    static final String typeClient = "client";
    static final String typeDevice = "device";

    private static HashSet<Device> devices = new HashSet<>();
    static class Device {
        private String id;
        private Session session;
        private String name;
        private String clientId;

        Device (String id, Session session, String name, String clientId) {
            Device device;
            if ( (device = get(id)) != null )
                devices.remove(device);
            this.id = id;
            this.session = session;
            this.name = name;
            this.clientId = clientId;
            devices.add(this);
        }

        static Device get(String id) {
            for (Device device : devices) {
                if (device.getId().equals(id))
                    return device;
            }
            return null;
        }

        static Device get(Session session) {
            for (Device device : devices) {
                if (device.getSession() == session)
                    return device;
            }
            return null;
        }

        static HashSet<Device> getDevicesOf(String clientId, Boolean online) {
            HashSet<Device> devices = new HashSet<>();
            for (Device device : WSConns.devices) {
                if (device.getClientID().equals(clientId) &&
                        device.getSession().isOpen() == online)
                    devices.add(device);
            }
            return devices;
        }

        String getId() {
            return id;
        }
        Session getSession() {
            return session;
        }
        String getName() {
            return name;
        }
        String getClientID() {
            return clientId;
        }
    }

    private static HashSet<Client> clients = new HashSet<>();
    static class Client {
        private String id;
        private Session session;
        private String name;

        Client (String id, Session session, String name) {
            Client client;
            if ( (client = get(id)) != null )
                clients.remove(client);
            this.id = id;
            this.session = session;
            this.name = name;
            clients.add(this);
        }

        static Client get(String id) {
            for (Client client : clients) {
                if (client.getId().equals(id))
                    return client;
            }
            return null;
        }

        static Client get(Session session) {
            for (Client client : clients) {
                if (client.getSession() == session)
                    return client;
            }
            return null;
        }

        String getId() {
            return id;
        }
        Session getSession() {
            return session;
        }
        String getName() {
            return name;
        }
    }
}
