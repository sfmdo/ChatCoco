package Network;

import ClientServices.*;
import UI.Login;
import UI.Content;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

public class Handlers {
    private static final Logger LOGGER = System.getLogger(Handlers.class.getName());
    private static Handlers instance;
    
    private ServerConnection connection;
    
    private Login loginWindow;
    private Content mainWindow;

    private AuthClientService authService;
    private ChatGlobalClientService chatGlobalService;
    private FriendClientService friendService;
    private GroupClientService groupService;
    private NotificationClientService notificationService;
    private UserClientService userService;

    private Handlers() {}

    public static synchronized Handlers getInstance() {
        if (instance == null) instance = new Handlers();
        return instance;
    }

    public void init(ServerConnection conn) {
        this.connection = conn;

        this.authService = new AuthClientService(conn);
        this.chatGlobalService = new ChatGlobalClientService(conn);
        this.friendService = new FriendClientService(conn);
        this.groupService = new GroupClientService(conn);
        this.notificationService = new NotificationClientService(conn);
        this.userService = new UserClientService(conn);
        
        LOGGER.log(Level.INFO, "Servicios de cliente inicializados y rutas registradas.");
    }


    public void setLoginWindow(Login login) {
        this.loginWindow = login;
        if (authService != null) {
            authService.setLoginWindow(login);
        }
    }

    public void setMainWindow(Content content) {
        this.mainWindow = content;

        if (chatGlobalService != null) chatGlobalService.setMainWindow(content);
        if (friendService != null) friendService.setMainWindow(content);
        if (groupService != null) groupService.setMainWindow(content);
        if (notificationService != null) notificationService.setMainWindow(content);
        if (userService != null) userService.setMainWindow(content);
        
        LOGGER.log(Level.INFO, "Ventana principal vinculada a los servicios.");
    }


    public AuthClientService getAuthService() { return authService; }
    public ChatGlobalClientService getChatGlobalService() { return chatGlobalService; }
    public FriendClientService getFriendService() { return friendService; }
    public GroupClientService getGroupService() { return groupService; }
    public NotificationClientService getNotificationService() { return notificationService; }
    public UserClientService getUserService() { return userService; }

    public ServerConnection getConnection() { return connection; }
}