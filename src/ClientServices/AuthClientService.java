package ClientServices;

import Messages.MessagePacket;
import Network.ClientRouter;
import Network.ServerConnection;
import Network.Handlers;
import UI.Login;
import UI.Content;
import javax.swing.SwingUtilities;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

public class AuthClientService {
    private static final Logger LOGGER = System.getLogger(AuthClientService.class.getName());
    
    private static final String ACTION_LOGIN = "LOGIN";
    private static final String ACTION_REGISTER = "REGISTER";

    private final ServerConnection connection;
    private String myId;
    private Login loginWindow;

    public AuthClientService(ServerConnection connection) {
        this.connection = connection;

        ClientRouter.addRoute(ACTION_LOGIN, this::handleLoginResponse);
        ClientRouter.addRoute(ACTION_REGISTER, this::handleRegisterResponse);
    }

    public void setLoginWindow(Login loginWindow) {
        this.loginWindow = loginWindow;
    }

    public String getMyId() { return myId; }

    public void login(String user, String pass) {
        MessagePacket p = MessagePacket.request(ACTION_LOGIN)
                .add("user", user)
                .add("pass", pass)
                .setToken("LGN-" + System.currentTimeMillis());
        connection.sendPacket(p);
    }

    public void register(String user, String pass) {
        MessagePacket p = MessagePacket.request(ACTION_REGISTER)
                .add("user", user)
                .add("pass", pass);
        connection.sendPacket(p);
    }

    private void handleLoginResponse(MessagePacket packet) {
        String status = packet.getParam("status");
        
        if ("success".equals(status)) {
            this.myId = packet.getParam("userId");
            String username = packet.getParam("username");
            LOGGER.log(Level.INFO, "Login exitoso para: {0}", username);

            SwingUtilities.invokeLater(() -> {
                if (loginWindow != null) {
                    Content mainWindow = new Content(connection);
                    mainWindow.setVisible(true);
                    triggerInitialData(mainWindow);
                    
                    loginWindow.dispose();
                }
            });
        } else {
            String reason = packet.getParam("reason");
            SwingUtilities.invokeLater(() -> {
                if (loginWindow != null) loginWindow.showErrorMessage(reason);
            });
        }
    }

    private void handleRegisterResponse(MessagePacket packet) {
        if ("success".equals(packet.getParam("status"))) {
            SwingUtilities.invokeLater(() -> {
                if (loginWindow != null) loginWindow.showSuccessMessage("Cuenta creada. Inicie sesión.");
            });
        } else {
            String reason = packet.getParam("reason");
            SwingUtilities.invokeLater(() -> {
                if (loginWindow != null) loginWindow.showErrorMessage(reason);
            });
        }
    }

    public void logout() {
        MessagePacket p = MessagePacket.request("LOGOUT");
        connection.sendPacket(p);
        this.myId = null;
        SwingUtilities.invokeLater(() -> {
            Login newLogin = new Login(this);
            Handlers.getInstance().setLoginWindow(newLogin);
            newLogin.setVisible(true);
        });
    }

    private void triggerInitialData(Content mainWindow) {
        mainWindow.getUserService().fetchUsers(); 
        mainWindow.getNotificationService().fetchNotifications();
        mainWindow.getGroupService().fetchMyGroups();
        mainWindow.getFriendService().fetchFriendsList();
    }
}