package ClientServices;

import Messages.MessagePacket;
import Models.GlobalMessage;
import Network.ClientRouter;
import Network.ServerConnection;
import UI.Content;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class ChatGlobalClientService {
    
    // --- ACCIONES LOCALES ---
    private static final String ACTION_MSG = "GLOBAL_MSG";
    private static final String ACTION_HISTORY = "GLOBAL_FETCH_HISTORY";

    private final ServerConnection connection;
    private Content mainWindow;
    private final Gson gson = new Gson();

    public ChatGlobalClientService(ServerConnection connection) {
        this.connection = connection;
        
        // REGISTRO DE RUTAS EN EL ROUTER DEL CLIENTE
        ClientRouter.addRoute(ACTION_MSG, this::handleIncomingMessage);
        ClientRouter.addRoute(ACTION_HISTORY, this::handleHistoryResponse);
    }

    public void setMainWindow(Content window) {
        this.mainWindow = window;
    }

    // --- PETICIONES (SALIDA) ---

    public void sendGlobalMessage(int targetUserId, String text) {
        MessagePacket p = MessagePacket.request(ACTION_MSG)
                .add("targetUserId", String.valueOf(targetUserId))
                .add("text", text);
        connection.sendPacket(p);
    }

    public void fetchHistory(int targetUserId) {
        MessagePacket p = MessagePacket.request(ACTION_HISTORY)
                .add("targetUserId", String.valueOf(targetUserId));
        connection.sendPacket(p);
    }

    // --- MANEJADORES DE ENTRADA ---

    /**
     * Maneja tanto la confirmación de envío como los mensajes entrantes de otros.
     */
    private void handleIncomingMessage(MessagePacket packet) {
        // Si es un EVENT, es un mensaje que alguien nos envió
        if ("EVENT".equals(packet.getType())) {
            String fromId = packet.getParam("from");
            String text = packet.getParam("text");
            
            // Limpieza de IDs (por si GSON lo convirtió a Double como 12.0)
            if (fromId != null && fromId.endsWith(".0")) {
                fromId = fromId.substring(0, fromId.length() - 2);
            }

            final String finalFromId = fromId;
            SwingUtilities.invokeLater(() -> {
                if (mainWindow != null && mainWindow.getChatPanel() != null) {
                    // Solo lo mostramos si el usuario tiene abierto el chat con esa persona en contexto GLOBAL
                    if ("GLOBAL".equals(mainWindow.getChatPanel().getCurrentContext()) && 
                        String.valueOf(mainWindow.getChatPanel().getCurrentTargetId()).equals(finalFromId)) {
                        
                        mainWindow.getChatPanel().addBubble(finalFromId, text, false);
                    }
                }
            });
        } 
        // Si es una RESPONSE con status error, podríamos mostrar un aviso
        else if ("error".equals(packet.getParam("status"))) {
            System.err.println("Error enviando mensaje global: " + packet.getParam("reason"));
        }
    }

    /**
     * Procesa la lista de mensajes históricos recibida de la RAM del servidor.
     */
    private void handleHistoryResponse(MessagePacket packet) {
        if ("success".equals(packet.getParam("status"))) {
            Object rawData = packet.getPayload().get("history");
            
            // Convertimos el payload a una lista de objetos GlobalMessage
            Type listType = new TypeToken<ArrayList<GlobalMessage>>(){}.getType();
            List<GlobalMessage> history = gson.fromJson(gson.toJson(rawData), listType);
        
            SwingUtilities.invokeLater(() -> {
                if (mainWindow != null && mainWindow.getChatPanel() != null) {
                    int myId = Integer.parseInt(mainWindow.getAuthService().getMyId()); 
                    mainWindow.getChatPanel().loadHistory(history, myId);
                }
            });
        }
    }
}