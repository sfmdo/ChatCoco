package ClientServices;

import Messages.MessagePacket;
import Models.PrivateMessages;
import Models.User;
import Network.ClientRouter;
import Network.ServerConnection;
import UI.Content;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class FriendClientService {
    
    private static final System.Logger LOGGER = System.getLogger(GroupClientService.class.getName());
    private static final String ACTION_REQ     = "FRIEND_REQ";
    private static final String ACTION_ACCEPT  = "FRIEND_ACCEPT";
    private static final String ACTION_MSG     = "FRIEND_MSG";
    private static final String ACTION_HISTORY = "FRIEND_HISTORY";
    private static final String ACTION_DECLINE = "FRIEND_DECLINE"; 
    private static final String ACTION_LIST    = "FRIEND_LIST";
    private static final String EVENT_UPDATE   = "FRIEND_LIST_UPDATE"; 

    private final ServerConnection connection;
    private Content mainWindow;
    private final Gson gson = new Gson();

    public FriendClientService(ServerConnection connection) {
        this.connection = connection;
        ClientRouter.addRoute(ACTION_LIST,    this::handleFriendListResponse);
        ClientRouter.addRoute(ACTION_MSG,     this::handleIncomingPrivateMsg);
        ClientRouter.addRoute(ACTION_HISTORY, this::handleFriendHistoryResponse);
        ClientRouter.addRoute(ACTION_REQ,     this::handleFriendRequestResponse);
        ClientRouter.addRoute(ACTION_ACCEPT,  this::handleFriendAcceptResponse);
        ClientRouter.addRoute(ACTION_DECLINE, this::handleFriendDeclineResponse);
        ClientRouter.addRoute(EVENT_UPDATE,   p -> fetchFriendsList());
        
    }

    public void setMainWindow(Content window) {
        this.mainWindow = window;
    }

    public void fetchFriendsList() {
        connection.sendPacket(MessagePacket.request(ACTION_LIST));
    }

    public void sendFriendRequest(int targetUserId) {
        connection.sendPacket(MessagePacket.request(ACTION_REQ).add("targetUserId", targetUserId));
    }

    public void acceptFriendRequest(int targetUserId) {
        connection.sendPacket(MessagePacket.request(ACTION_ACCEPT).add("targetUserId", targetUserId));
    }
    
    public void declineFriendRequest(int friendshipId) {
        connection.sendPacket(MessagePacket.request(ACTION_DECLINE).add("friendshipId", friendshipId));
    }


    public void sendPrivateMessage(int targetUserId, String text) {
        connection.sendPacket(MessagePacket.request(ACTION_MSG)
                .add("targetUserId", targetUserId)
                .add("text", text));
    }

    public void fetchFriendHistory(int targetUserId) {
        connection.sendPacket(MessagePacket.request(ACTION_HISTORY).add("targetUserId", targetUserId));
    }

    // --- MANEJADORES DE ENTRADA (RESPUESTAS Y EVENTOS) ---

    private void handleFriendListResponse(MessagePacket packet) {
        if ("success".equals(packet.getParam("status"))) {
            Object rawData = packet.getPayload().get("friends");
            Type listType = new TypeToken<ArrayList<User>>(){}.getType();
            List<User> friends = gson.fromJson(gson.toJson(rawData), listType);
            
            SwingUtilities.invokeLater(() -> {
                if (mainWindow != null) mainWindow.updateFriendsList(friends);
            });
        }
    }

    private void handleIncomingPrivateMsg(MessagePacket packet) {
        if (!"EVENT".equals(packet.getType())) return;

        String fromId = packet.getParam("from");
        if (fromId != null && fromId.endsWith(".0")) fromId = fromId.substring(0, fromId.length() - 2);
        
        String text = packet.getParam("text");

        final String finalFromId = fromId;
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null && mainWindow.getChatPanel() != null) {
                if ("FRIEND".equals(mainWindow.getChatPanel().getCurrentContext()) && 
                    String.valueOf(mainWindow.getChatPanel().getCurrentTargetId()).equals(finalFromId)) {
                    
                    mainWindow.getChatPanel().addBubble(finalFromId, text, false);
                }
            }
        });
    }

    private void handleFriendHistoryResponse(MessagePacket packet) {
        if ("success".equals(packet.getParam("status"))) {
            Object rawData = packet.getPayload().get("history");
            Type listType = new TypeToken<ArrayList<PrivateMessages>>(){}.getType();
            List<PrivateMessages> history = gson.fromJson(gson.toJson(rawData), listType);

            SwingUtilities.invokeLater(() -> {
                if (mainWindow != null) {
                    int myId = Integer.parseInt(mainWindow.getAuthService().getMyId());
                    mainWindow.getChatPanel().loadFriendHistory(history, myId);
                }
            });
        }
    }

    private void handleFriendRequestResponse(MessagePacket packet) {
        String status = packet.getParam("status");
        SwingUtilities.invokeLater(() -> {
            if ("success".equals(status)) {
                JOptionPane.showMessageDialog(mainWindow, "¡Solicitud de amistad enviada!");
                if (mainWindow != null) mainWindow.getUsersView().resetFriendButton();
            } else {
                JOptionPane.showMessageDialog(mainWindow, "Error: " + packet.getParam("reason"), 
                        "Amistad", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private void handleFriendAcceptResponse(MessagePacket packet) {
        if ("EVENT".equals(packet.getType())) {
            SwingUtilities.invokeLater(() -> {
                System.out.println("SISTEMA: Un usuario ha aceptado tu solicitud de amistad.");
            });
        }
    }
    
    private void handleFriendDeclineResponse(MessagePacket packet) {
        String status = packet.getParam("status");
        if ("success".equals(status)) {
            System.out.println("SISTEMA: Solicitud de amistad rechazada con éxito.");
        } else {
            LOGGER.log(System.Logger.Level.WARNING, "Error al rechazar amistad: {0}", packet.getParam("reason"));
        }
    }
}