package Network;

import Messages.MessagePacket;
import java.util.HashMap;
import java.util.Map;

public class ClientRouter {
    private static final Map<String, java.util.function.Consumer<MessagePacket>> routes = new HashMap<>();
    private static final System.Logger LOGGER = System.getLogger(ClientRouter.class.getName());

    public static void addRoute(String action, java.util.function.Consumer<MessagePacket> handler) {
        routes.put(action, handler);
    }

    public static void route(MessagePacket packet) {
        String action = packet.getAction();
        java.util.function.Consumer<MessagePacket> handler = routes.get(action);
        System.out.println("[DEBUG] Router recibio accion: " + action);

        if (handler != null) {
            LOGGER.log(System.Logger.Level.INFO, "Ruteando acción: {0}", action);
            handler.accept(packet);
        } else {
            LOGGER.log(System.Logger.Level.WARNING, "Acción desconocida recibida del servidor: {0}", action);
        }
    }
}