package Network;

import Messages.MessagePacket;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

public class ClientRouter {

    // Definimos el Logger para el cliente
    private static final Logger LOGGER = System.getLogger(ClientRouter.class.getName());

    public static void route(MessagePacket packet) {
        String action = packet.getAction();
        String type = packet.getType(); // "RESPONSE" o "EVENT"

        // Log informativo de lo que llega
        LOGGER.log(Level.INFO, "Paquete recibido: Tipo={0}, Acción={1}", type, action);

        // 1. Manejo de Errores Globales
        if (packet.getParam("status") != null && packet.getParam("status").equals("error")) {
            String reason = packet.getParam("reason");
            LOGGER.log(Level.WARNING, "El servidor reportó un error en {0}: {1}", action, reason);
            // Aquí llamarías a una función de la UI para mostrar un cuadro de error
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
                // UI.showFriendRequestDialog(packet);
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
            // UI.openMainWindow();
        }
    }

    private static void handleIncomingMessage(MessagePacket packet, String context) {
        String from = packet.getParam("from");
        String text = packet.getParam("text");
        
        LOGGER.log(Level.INFO, "[{0}] Mensaje de {1}: {2}", context, from, text);
        
        // Aquí es donde actualizas tu JList o JTextArea del Chat
        // ChatWindow.appendMessage(from, text);
    }

    private static void handleNotification(MessagePacket packet) {
        String content = packet.getParam("content");
        String notifType = packet.getParam("type");
        
        LOGGER.log(Level.INFO, "Notificación Push ({0}): {1}", notifType, content);
        
        // UI.showSystemTrayIcon(content);
    }
}