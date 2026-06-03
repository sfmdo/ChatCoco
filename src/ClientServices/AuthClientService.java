package ClientServices;

import Messages.MessagePacket;
import Network.Protocol;
import Network.ServerConnection;

public class AuthClientService {
    private ServerConnection connection;
    private String myId;

    public AuthClientService(ServerConnection connection) {
        this.connection = connection;
    }
    
    public String getMyId() {
        return myId;
    }

    public void setMyId(String myId) {
        this.myId = myId;
    }

    public void login(String user, String pass) {
        MessagePacket p = MessagePacket.request(Protocol.LOGIN)
                .add("user", user)
                .add("pass", pass)
                .setToken("LGN-" + System.currentTimeMillis());
        connection.sendPacket(p);
    }

    public void register(String user, String pass) {
        MessagePacket p = MessagePacket.request(Protocol.REGISTER)
                .add("user", user)
                .add("pass", pass);
        connection.sendPacket(p);
    }
}