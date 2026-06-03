package ClientServices;

import Messages.MessagePacket;
import Network.Protocol;
import Network.ServerConnection;

public class GroupClientService {
    private ServerConnection connection;

    public GroupClientService(ServerConnection connection) {
        this.connection = connection;
    }

    public void createGroup(String name) {
        MessagePacket p = MessagePacket.request(Protocol.GROUP_CREATE)
                .add("groupName", name);
        connection.sendPacket(p);
    }

    public void sendGroupMessage(int groupId, String text) {
        MessagePacket p = MessagePacket.request(Protocol.GROUP_MSG)
                .add("groupId", groupId)
                .add("text", text);
        connection.sendPacket(p);
    }
    
    public void fetchMyGroups() {
        MessagePacket p = MessagePacket.request(Protocol.GROUP_LIST);
        connection.sendPacket(p);
    }
    public void fetchHistory(int groupId) {
        MessagePacket p = MessagePacket.request(Protocol.GROUP_HISTORY)
                .add("groupId", groupId);
        connection.sendPacket(p);
    }
    
    public void sendGroupInvitation(int groupId, int targetUserId) {
        MessagePacket p = MessagePacket.request(Protocol.GROUP_INVITE)
                .add("groupId", groupId)
                .add("targetUserId", targetUserId);
        connection.sendPacket(p);
    }
    
    public void acceptGroupInvitation(int groupId) {
        MessagePacket p = MessagePacket.request(Protocol.GROUP_INVITE_ACCEPT)
                .add("groupId", groupId);
        connection.sendPacket(p);
    }

    public void declineGroupInvitation(int groupId) {
        MessagePacket p = MessagePacket.request(Protocol.GROUP_INVITE_DECLINE)
                .add("groupId", groupId);
        connection.sendPacket(p);
    }
}