package ClientServices;

import Messages.MessagePacket;
import Network.Protocol;
import Network.ServerConnection;

public class UserClientService {
    private ServerConnection connection;

    public UserClientService(ServerConnection connection) {
        this.connection = connection;
    }

    public void fetchUsers() {
        MessagePacket p = MessagePacket.request(Protocol.FETCH_USERS);
        connection.sendPacket(p);
    }
}