package ClientServices;

import Messages.MessagePacket;
import Network.Protocol;
import Network.ServerConnection;

public class NotificationClientService {
    private ServerConnection connection;

    public NotificationClientService(ServerConnection connection) {
        this.connection = connection;
    }

    //Pide al servidor la lista de notificaciones pendientes (solicitudes de amistad/grupo).
     
    public void fetchNotifications() {
        MessagePacket p = MessagePacket.request(Protocol.FETCH_NOTIFICATIONS);
        connection.sendPacket(p);
    }
}