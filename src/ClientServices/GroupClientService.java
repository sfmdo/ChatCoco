package ClientServices;

import Messages.MessagePacket;
import Models.Group;
import Models.GroupMessages;
import Network.ClientRouter;
import Network.ServerConnection;
import UI.Content;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class GroupClientService {
    private static final Logger LOGGER = System.getLogger(GroupClientService.class.getName());
    private static final String ACTION_CREATE  = "GROUP_CREATE";
    private static final String ACTION_MSG     = "GROUP_MSG";
    private static final String ACTION_LIST    = "GROUP_LIST";
    private static final String ACTION_HISTORY = "GROUP_HISTORY";
    private static final String ACTION_INVITE  = "GROUP_INVITE";
    private static final String ACTION_ACCEPT  = "GROUP_ACCEPT";
    private static final String ACTION_DECLINE = "GROUP_DECLINE";
    private static final String EVENT_UPDATE   = "GROUP_LIST_UPDATE"; // Evento reactivo

    private final ServerConnection connection;
    private Content mainWindow;
    private final Gson gson = new Gson();
    public static volatile int ultimoGroupIdRecibido = -1;

    public GroupClientService(ServerConnection connection) {
        this.connection = connection;
        ClientRouter.addRoute(ACTION_LIST,    this::handleGroupListResponse);
        ClientRouter.addRoute(ACTION_HISTORY, this::handleGroupHistoryResponse);
        ClientRouter.addRoute(ACTION_MSG,     this::handleIncomingGroupMsg);
        ClientRouter.addRoute(ACTION_CREATE,  this::handleCreateResponse);
        ClientRouter.addRoute(ACTION_ACCEPT,  this::handleGenericSuccess);
        ClientRouter.addRoute(ACTION_DECLINE, this::handleGenericSuccess);
        ClientRouter.addRoute(EVENT_UPDATE,   p -> fetchMyGroups());
    }

    public void setMainWindow(Content window) {
        this.mainWindow = window;
    }
    
    public void fetchMyGroups() {
        connection.sendPacket(MessagePacket.request(ACTION_LIST));
    }

    public void createGroup(String name) {
        connection.sendPacket(MessagePacket.request(ACTION_CREATE).add("groupName", name));
    }

    public void sendGroupMessage(int groupId, String text) {
        connection.sendPacket(MessagePacket.request(ACTION_MSG)
                .add("groupId", groupId)
                .add("text", text));
    }

    public void fetchHistory(int groupId) {
        connection.sendPacket(MessagePacket.request(ACTION_HISTORY).add("groupId", groupId));
    }

    public void sendGroupInvitation(int groupId, int targetUserId) {
        connection.sendPacket(MessagePacket.request(ACTION_INVITE)
                .add("groupId", groupId)
                .add("targetUserId", targetUserId));
    }

    public void acceptGroupInvitation(int groupId) {
        connection.sendPacket(MessagePacket.request(ACTION_ACCEPT).add("groupId", groupId));
    }

    public void declineGroupInvitation(int groupId) {
        connection.sendPacket(MessagePacket.request(ACTION_DECLINE).add("groupId", groupId));
    }

    private void handleGroupListResponse(MessagePacket packet) {
        if ("success".equals(packet.getParam("status"))) {
            Object rawData = packet.getPayload().get("groups");
            Type listType = new TypeToken<ArrayList<Group>>(){}.getType();
            List<Group> groups = gson.fromJson(gson.toJson(rawData), listType);

            SwingUtilities.invokeLater(() -> {
                if (mainWindow != null && mainWindow.getGroupsView() != null) {
                    mainWindow.updateUIList(mainWindow.getGroupsView().getGroupsModel(), groups);
                }
            });
        }
    }

    private void handleIncomingGroupMsg(MessagePacket packet) {
        if (!"EVENT".equals(packet.getType())) return;

        String msgGroupId = packet.getParam("groupId");
        if (msgGroupId != null && msgGroupId.endsWith(".0")) {
            msgGroupId = msgGroupId.substring(0, msgGroupId.length() - 2);
        }

        final String finalGroupId = msgGroupId;
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null && mainWindow.getChatPanel() != null) {
                String currentOpenId = String.valueOf(mainWindow.getChatPanel().getCurrentTargetId());
                
                if ("GROUP".equals(mainWindow.getChatPanel().getCurrentContext()) && currentOpenId.equals(finalGroupId)) {
                    fetchHistory(Integer.parseInt(finalGroupId));
                } else {
                    LOGGER.log(Level.INFO, "Nuevo mensaje en grupo {0} (segundo plano)", finalGroupId);
                }
            }
        });
    }

    private void handleGroupHistoryResponse(MessagePacket packet) {
        if ("success".equals(packet.getParam("status"))) {
            Object rawData = packet.getPayload().get("history");
            Type listType = new TypeToken<ArrayList<GroupMessages>>(){}.getType();
            List<GroupMessages> history = gson.fromJson(gson.toJson(rawData), listType);

            SwingUtilities.invokeLater(() -> {
                if (mainWindow != null) {
                    int myId = Integer.parseInt(mainWindow.getAuthService().getMyId());
                    mainWindow.getChatPanel().loadGroupHistory(history, myId);
                }
            });
        }
    }

    private void handleCreateResponse(MessagePacket packet) {
        if ("success".equals(packet.getParam("status"))) {
            ultimoGroupIdRecibido = packet.getIntParam("groupId");
            LOGGER.log(Level.INFO, "Grupo creado con ID: {0}", ultimoGroupIdRecibido);
            // El servidor enviará un GROUP_LIST_UPDATE, así que la lista se refrescará sola
        }
    }

    private void handleGenericSuccess(MessagePacket packet) {
        if ("success".equals(packet.getParam("status"))) {
            LOGGER.log(Level.INFO, "Acción de grupo {0} completada con éxito", packet.getAction());
        }
    }
}