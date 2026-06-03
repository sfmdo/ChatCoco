package ClientServices;

import Messages.MessagePacket;
import Network.Protocol;
import Network.ServerConnection;

public class FriendClientService {
    private ServerConnection connection;

    public FriendClientService(ServerConnection connection) {
        this.connection = connection;
    }
    
    public void fetchFriendHistory(int targetUserId) {
        MessagePacket p = MessagePacket.request(Protocol.FRIEND_HISTORY)
                .add("targetUserId", targetUserId);
        
        connection.sendPacket(p);
    }
    
    public void sendFriendRequest(int targetUserId) {
        MessagePacket p = MessagePacket.request(Protocol.FRIEND_REQUEST)
                .add("targetUserId", targetUserId);
        connection.sendPacket(p);
    }

    public void sendPrivateMessage(int targetUserId, String text) {
        MessagePacket p = MessagePacket.request(Protocol.FRIEND_MSG)
                .add("targetUserId", targetUserId)
                .add("text", text);
        connection.sendPacket(p);
    }
    
    public void acceptFriendRequest(int targetUserId) {
        MessagePacket p = MessagePacket.request(Protocol.FRIEND_ACCEPT)
                .add("targetUserId", targetUserId); // ID del que envió la solicitud
        connection.sendPacket(p);
    }

    public void declineFriendRequest(int friendshipId) {
        MessagePacket p = MessagePacket.request(Protocol.FRIEND_DECLINE)
                .add("friendshipId", friendshipId);
        connection.sendPacket(p);
    }
}