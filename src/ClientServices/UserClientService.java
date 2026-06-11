package ClientServices;

import Messages.MessagePacket;
import Models.User;
import Network.ClientRouter;
import Network.ServerConnection;
import UI.Content;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class UserClientService {
    
    // --- ACCIÓN LOCAL ---
    private static final String ACTION_FETCH = "FETCH_USERS";

    private final ServerConnection connection;
    private Content mainWindow;
    private final Gson gson = new Gson();

    public UserClientService(ServerConnection connection) {
        this.connection = connection;
        ClientRouter.addRoute(ACTION_FETCH, this::handleUsersListResponse);
    }

    public void setMainWindow(Content window) {
        this.mainWindow = window;
    }

    public void fetchUsers() {
        MessagePacket p = MessagePacket.request(ACTION_FETCH);
        connection.sendPacket(p);
    }

    private void handleUsersListResponse(MessagePacket packet) {
        if (!"success".equals(packet.getParam("status"))) return;

        Object rawData = packet.getPayload().get("users");
        if (rawData == null) return;
        Type listType = new TypeToken<ArrayList<User>>(){}.getType();
        List<User> users = gson.fromJson(gson.toJson(rawData), listType);

        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null) {
                String myIdStr = mainWindow.getAuthService().getMyId();
                
                if (myIdStr != null) {
                    int myId = Integer.parseInt(myIdStr);
                    users.removeIf(u -> u.getId() == myId);
                }
                mainWindow.updateUsersList(users);
            }
        });
    }
}