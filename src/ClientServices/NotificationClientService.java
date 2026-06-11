package ClientServices;

import Messages.MessagePacket;
import Models.Notifications;
import Network.ClientRouter;
import Network.ServerConnection;
import UI.Content;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class NotificationClientService {

    private static final String ACTION_FETCH = "NOTIF_REQ";    
    private static final String EVENT_NOTIF  = "NOTIFICATION";

    private final ServerConnection connection;
    private Content mainWindow;
    private final Gson gson = new Gson();

    public NotificationClientService(ServerConnection connection) {
        this.connection = connection;
        ClientRouter.addRoute(ACTION_FETCH, this::handleNotificationIncoming);
        ClientRouter.addRoute(EVENT_NOTIF,  this::handleNotificationIncoming);
    }

    public void setMainWindow(Content window) {
        this.mainWindow = window;
    }

    public void fetchNotifications() {
        MessagePacket p = MessagePacket.request(ACTION_FETCH);
        connection.sendPacket(p);
    }

    private void handleNotificationIncoming(MessagePacket packet) {
        // Extraemos la lista del payload
        Object rawData = packet.getPayload().get("notifications");
        
        if (rawData == null) return;
        
        Type listType = new TypeToken<ArrayList<Notifications>>(){}.getType();
        List<Notifications> list = gson.fromJson(gson.toJson(rawData), listType);
        SwingUtilities.invokeLater(() -> {
            if (mainWindow != null && mainWindow.getNotificationView() != null) {
                mainWindow.getNotificationView().updateList(list);
            }
        });
    }
}