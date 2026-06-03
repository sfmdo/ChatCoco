package Network;

import Messages.MessagePacket;
import Models.GlobalMessage;
import Models.User;
import UI.Components.ChatPanel;
import UI.Login;
import UI.Content; // Asumiendo que tienes esta clase
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken; // IMPORTANTE
import java.lang.reflect.Type; 
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Handlers {
    private static final Logger LOGGER = System.getLogger(Handlers.class.getName());
    private static Handlers instance;
    private Login loginWindow;
    private Content mainWindow;
    private ServerConnection connection;
    
    private Handlers() {}

    public static synchronized Handlers getInstance() {
        if (instance == null) instance = new Handlers();
        return instance;
    }
    
    public void setConnection(ServerConnection conn) {
        this.connection = conn;
    }

    // --- SETTERS PARA LA UI ---
    public void setLoginWindow(Login login) { this.loginWindow = login; }
    public void setMainWindow(Content content) { this.mainWindow = content; }

    // --- MÉTODOS DE MANEJO ---

    public void handleLoginResponse(MessagePacket packet) {
        if ("success".equals(packet.getParam("status"))) {
            LOGGER.log(Level.INFO, "Login exitoso, cambiando a ventana principal.");
            String userId = packet.getParam("userId");
            java.awt.EventQueue.invokeLater(() -> {
                if (loginWindow != null) {
                    mainWindow = new Content(this.connection);
                    mainWindow.getAuthService().setMyId(userId); 
                    mainWindow.setVisible(true);
                    mainWindow.getUserService().fetchUsers(); 
                    mainWindow.getNotificationService().fetchNotifications();
                    loginWindow.dispose();
                    loginWindow = null;
                }
            });
        } else {
            String reason = packet.getParam("reason");
            LOGGER.log(Level.WARNING, "Login fallido: {0}", reason);
            
            SwingUtilities.invokeLater(() -> {
                if (loginWindow != null) {
                    loginWindow.showErrorMessage(reason);
                }
            });
        }
    }
    
    public void handleRegisterResponse(MessagePacket packet) {
        String status = packet.getParam("status");
    
        if ("success".equals(status)) {
            String serverMsg = packet.getParam("message");
            LOGGER.log(Level.INFO, "Registro exitoso: {0}", serverMsg);
        
            // Actualizamos la UI de forma segura
            javax.swing.SwingUtilities.invokeLater(() -> {
                if (loginWindow != null) {
                    if (loginWindow != null) loginWindow.showSuccessMessage("Cuenta creada, inicia sesion.");
                }
            });
        } else {
            String reason = packet.getParam("reason");
            LOGGER.log(Level.WARNING, "Fallo en registro: {0}", reason);
        
            SwingUtilities.invokeLater(() -> {
                if (loginWindow != null) loginWindow.showErrorMessage(reason); // <--- ERROR
            });
        }
    }

    public void handleIncomingMessage(MessagePacket packet, String action) {
    // 1. Extraer datos básicos y limpiar el .0 de Gson
    String fromId = packet.getParam("from");
    if (fromId != null && fromId.endsWith(".0")) fromId = fromId.substring(0, fromId.length() - 2);
    
    String text = packet.getParam("text");
    String fromName = packet.getParam("fromName") != null ? packet.getParam("fromName") : fromId;

    if (mainWindow == null || mainWindow.getChatPanel() == null) return;
    ChatPanel panel = mainWindow.getChatPanel();

    // 2. Switch para procesar según el tipo de mensaje entrante
    switch (action) {
            case Protocol.GLOBAL_MSG:
                // Regla: ¿Estoy en pestaña GLOBAL y hablando con este ID?
                if ("GLOBAL".equals(panel.getCurrentContext()) && 
                    String.valueOf(panel.getCurrentTargetId()).equals(fromId)) {
                
                    SwingUtilities.invokeLater(() -> panel.addBubble(fromName, text, false));
                } else {
                    LOGGER.log(Level.INFO, "Mensaje Global de {0} recibido en 2do plano.", fromName);
                }
                break;

            case Protocol.FRIEND_MSG:
                // Regla: ¿Estoy en pestaña FRIEND y hablando con este amigo?
                if ("FRIEND".equals(panel.getCurrentContext()) && 
                    String.valueOf(panel.getCurrentTargetId()).equals(fromId)) {
                
                    SwingUtilities.invokeLater(() -> panel.addBubble(fromName, text, false));
                } else {
                    LOGGER.log(Level.INFO, "Mensaje Privado de {0} guardado en SQL.", fromName);
                }
                break;

            case Protocol.GROUP_MSG:
                // Regla: En grupos no comparamos el 'from', sino el 'groupId'
                String msgGroupId = packet.getParam("groupId");
                if (msgGroupId != null && msgGroupId.endsWith(".0")) msgGroupId = msgGroupId.substring(0, msgGroupId.length() - 2);

                if ("GROUP".equals(panel.getCurrentContext()) && 
                    String.valueOf(panel.getCurrentTargetId()).equals(msgGroupId)) {
                
                    // Pintamos el nombre del integrante para saber quién escribió en el grupo
                    SwingUtilities.invokeLater(() -> panel.addBubble(fromName, text, false));
                } else {
                    LOGGER.log(Level.INFO, "Actividad en el grupo {0} detectada.", msgGroupId);
                }
                break;
            
            default:
                LOGGER.log(Level.WARNING, "Contexto de mensaje desconocido: {0}", action);
                break;
        }
    }
    
    public void handleGlobalHistoryResponse(MessagePacket packet) {
        if ("success".equals(packet.getParam("status"))) {
            // Extraemos la lista de objetos GlobalMessage que mandó el servidor
            Object rawData = packet.getPayload().get("history");
        
            // 2. Convertir a la lista real de objetos GlobalMessage
            Gson gson = new Gson();
            String json = gson.toJson(rawData);
            Type listType = new TypeToken<ArrayList<GlobalMessage>>(){}.getType();
            List<GlobalMessage> history = gson.fromJson(json, listType);
        
            if (mainWindow != null) {
                // Le pasamos el historial al chat panel
                // Necesitas obtener tu propia ID del AuthService o Session
                int myId = Integer.parseInt(mainWindow.getAuthService().getMyId()); 
                mainWindow.getChatPanel().loadHistory(history, myId);
            }
        }
    }

    public void handleIncomingGlobalMessage(MessagePacket packet) {
        int fromId = packet.getIntParam("from");
        String text = packet.getParam("text");

        if (mainWindow != null) {
            // El ChatPanel decidirá si lo pinta (si el ID coincide con el abierto)
            mainWindow.getChatPanel().receiveMessage(fromId, text);
        }
    }

    public void handleNotification(MessagePacket packet) {
        Object rawData = packet.getPayload().get("notifications"); // Ajusta el nombre según tu server
        Gson gson = new Gson();
        String json = gson.toJson(rawData);
    
        // Re-parseo a la lista real de modelos
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.ArrayList<Models.Notifications>>(){}.getType();
        java.util.List<Models.Notifications> list = gson.fromJson(json, type);

        if (mainWindow != null) {
            mainWindow.getNotificationView().updateList(list);
        }
    }
    
    public void handleUsersListResponse(MessagePacket packet) {
        Object rawData = packet.getPayload().get("users");

        Gson gson = new Gson();
        String json = gson.toJson(rawData);
    
        Type listType = new TypeToken<ArrayList<Models.User>>(){}.getType();
        List<Models.User> users = gson.fromJson(json, listType);
        LOGGER.log(Level.INFO, "Cargando historial para:");
        // 3. Ahora sí, mandamos la lista de objetos REALES a la UI
        if (mainWindow != null) {
            String myIdStr = mainWindow.getAuthService().getMyId();
        
            if (myIdStr != null) {
                int myId = Integer.parseInt(myIdStr);
            
                // 3. Removemos de la lista al usuario que tenga mi mismo ID
                // El método removeIf es muy eficiente para esto
                users.removeIf(u -> u.getId() == myId);
            }
            mainWindow.updateUsersList(users);
        }
    }
    
    public void handleFriendRequestResponse(MessagePacket packet) {
    String status = packet.getParam("status");
    
        if ("success".equals(status)) {
             LOGGER.log(Level.INFO, "Solicitud de amistad enviada con exito:");
            // Escenario: Todo salió bien
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(mainWindow, "¡Solicitud de amistad enviada con éxito!");
                if  (mainWindow != null) {
                    mainWindow.getUsersView().resetFriendButton();
                }
            });
        } else {
        // Escenario: Error (Duplicado, usuario no existe, etc.)
         
            String reason = packet.getParam("reason");
            LOGGER.log(Level.WARNING, "Solicitud no enviada: {0}", reason);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(mainWindow, 
                    "No se pudo enviar la solicitud: " + reason, 
                    "Error de Amistad", 
                    JOptionPane.WARNING_MESSAGE);
            
                if (mainWindow != null) {
                    mainWindow.getUsersView().resetFriendButton();
                }
            });
        }
    }
    
}