package Network;

import Messages.MessagePacket;
import UI.Content;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientRouter {

    // Definimos el Logger para el cliente
    private static final Logger LOGGER = System.getLogger(ClientRouter.class.getName());

    // Referencia estática a la interfaz de usuario activa
    private static Content activeUI;
    // Referencia estática a la conexión del servidor
    private static ServerConnection serverConnection;

    public static void setUI(Content ui) {
        activeUI = ui;
    }

    public static Content getUI() {
        return activeUI;
    }

    public static void setServerConnection(ServerConnection conn) {
        serverConnection = conn;
    }

    public static ServerConnection getServerConnection() {
        return serverConnection;
    }

    public static void route(MessagePacket packet) {
        String action = packet.getAction();
        String type = packet.getType(); // "RESPONSE" o "EVENT"

        // Log informativo de lo que llega
        LOGGER.log(Level.INFO, "Paquete recibido: Tipo={0}, Acción={1}", type, action);

        // 1. Manejo de Errores Globales
        if (packet.getParam("status") != null && packet.getParam("status").equals("error")) {
            String reason = packet.getParam("reason");
            LOGGER.log(Level.WARNING, "El servidor reportó un error en {0}: {1}", action, reason);
            if (activeUI != null) {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    javax.swing.JOptionPane.showMessageDialog(activeUI, "Error en " + action + ": " + reason, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                });
            }
            return;
        }

        // 2. Enrutamiento según la acción
        switch (action) {
            
            case Protocol.LOGIN:
                handleLoginResponse(packet);
                break;

            case Protocol.FRIEND_MSG:
                handleIncomingMessage(packet, "PRIVADO");
                break;

            case Protocol.GROUP_MSG:
                handleIncomingMessage(packet, "GRUPO");
                break;

            case Protocol.NOTIFICATION:
                handleNotification(packet);
                break;

            case Protocol.FRIEND_REQUEST:
                LOGGER.log(Level.INFO, "Nueva solicitud de amistad de: {0}", packet.getParam("fromName"));
                handleFriendRequest(packet);
                break;

            case Protocol.GROUP_HISTORY:
                handleGroupHistory(packet);
                break;

            default:
                LOGGER.log(Level.INFO, "Acción no manejada específicamente en el cliente: {0}", action);
                break;
        }
    }

    private static void handleLoginResponse(MessagePacket packet) {
        if ("success".equals(packet.getParam("status"))) {
            String user = packet.getParam("username");
            LOGGER.log(Level.INFO, "¡Bienvenido {0}! Login exitoso.", user);
            // Si el login fue exitoso, ya abrimos la ventana principal en Login.java
        }
    }

    private static void handleIncomingMessage(MessagePacket packet, String context) {
        String from = packet.getParam("from");
        String text = packet.getParam("text");
        
        LOGGER.log(Level.INFO, "[{0}] Mensaje de {1}: {2}", context, from, text);
        
        if (activeUI != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                activeUI.appendIncomingMessage(from, text);
            });
        }
    }

    private static void handleNotification(MessagePacket packet) {
        String content = packet.getParam("content");
        String notifType = packet.getParam("type");
        String relatedId = packet.getParam("relatedId");
        
        LOGGER.log(Level.INFO, "Notificación Push ({0}): {1}", notifType, content);
        
        if (activeUI != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                activeUI.addNotification(content, relatedId, notifType);
            });
        }
    }

    private static void handleFriendRequest(MessagePacket packet) {
        String fromName = packet.getParam("fromName");
        String requestId = packet.getParam("requestId"); // ID de la solicitud
        
        if (activeUI != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                activeUI.addNotification("Solicitud de amistad de: " + fromName, requestId, "FRIEND_REQUEST");
            });
        }
    }

    private static void handleGroupHistory(MessagePacket packet) {
        // Asumiendo que el historial de mensajes viene en un parámetro o payload estructurado
        // Si viene estructurado, podemos pasarlo a la UI
        if (activeUI != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                // Aquí llamamos al método para cargar el arreglo de mensajes
                // Por ejemplo, decodificando una lista del payload
                java.util.List<MessagePacket> messages = new ArrayList<>();
                Object msgsObj = packet.getPayload().get("messages");
                if (msgsObj instanceof java.util.List) {
                    java.util.List<?> rawList = (java.util.List<?>) msgsObj;
                    for (Object obj : rawList) {
                        if (obj instanceof MessagePacket) {
                            messages.add((MessagePacket) obj);
                        } else if (obj instanceof java.util.Map) {
                            // Si Gson lo deserializó como mapa
                            @SuppressWarnings("unchecked")
                            java.util.Map<String, Object> map = (java.util.Map<String, Object>) obj;
                            MessagePacket p = new MessagePacket();
                            p.setPayload(map);
                            messages.add(p);
                        }
                    }
                }
                activeUI.loadConversationMessages(messages);
            });
        }
    }
}