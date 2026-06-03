package ClientServices;

import Messages.MessagePacket;
import Network.Protocol;
import Network.ServerConnection;

public class ChatGlobalClientService {
    private ServerConnection connection;

    public ChatGlobalClientService(ServerConnection connection) {
        this.connection = connection;
    }

    public void sendGlobalMessage(int targetUserId, String text) {
        MessagePacket p = MessagePacket.request(Protocol.GLOBAL_MSG)
                .add("targetUserId", String.valueOf(targetUserId))
                .add("text", text);
        connection.sendPacket(p);
    }

    /**
     * Pide la "Tabla RAM" del servidor para una conversación específica.
     */
    public void fetchHistory(int targetUserId) {
        MessagePacket p = MessagePacket.request(Protocol.GLOBAL_FETCH_HISTORY)
                .add("targetUserId", String.valueOf(targetUserId));
        connection.sendPacket(p);
    }
}