package Network;

import Messages.MessagePacket;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

public class ClientRouter {

    // Definimos el Logger para el cliente
    private static final Logger LOGGER = System.getLogger(ClientRouter.class.getName());
    public static volatile int ultimoGroupIdRecibido = -1;
    public static volatile int ultimaNotificacionFriendshipId = -1;
    public static volatile int ultimaNotificacionRequesterId = -1;
    public static volatile int ultimaNotificacionGroupId = -1;

    public static void route(MessagePacket packet) {
        try{
            String action = packet.getAction();
            String type = packet.getType(); // "RESPONSE" o "EVENT"
            Handlers h = Handlers.getInstance();
            // Log informativo de lo que llega
            LOGGER.log(Level.INFO, "Paquete recibido: Tipo={0}, Acción={1}", type, action);

            // 1. Manejo de Errores Globales
            if (packet.getParam("status") != null && packet.getParam("status").equals("error")) {
                String reason = packet.getParam("reason");
                LOGGER.log(Level.WARNING, "El servidor reportó un error en {0}: {1}", action, reason);
                
            }
        
            if (Protocol.NOTIFICATION.equals(action)) {
                String subType = packet.getParam("type"); // "FRIEND_REQUEST" o "GROUP_INVITE"
            
                if ("FRIEND_REQUEST".equals(subType)) {
                    ultimaNotificacionFriendshipId = packet.getIntParam("relatedId");
                    ultimaNotificacionRequesterId = packet.getIntParam("from");
                    LOGGER.log(Level.INFO, "Capturada Solicitud Amistad: ID {0} del usuario {1}", 
                        ultimaNotificacionFriendshipId, ultimaNotificacionRequesterId);
                
                } else if ("GROUP_INVITE".equals(subType)) {
                // CAPTURAMOS EL ID DEL GRUPO AL QUE NOS INVITARON
                    ultimaNotificacionGroupId = packet.getIntParam("groupId");
                    LOGGER.log(Level.INFO, "Capturada Invitación a Grupo: ID {0}", ultimaNotificacionGroupId);
                }
            
                h.handleNotification(packet);
                return;
            }

            // 2. Enrutamiento según la acción
            switch (action) {
            
                case Protocol.LOGIN:
                    h.handleLoginResponse(packet);
                    break;
                case Protocol.REGISTER:
                    h.handleRegisterResponse(packet);
                    break;
                case Protocol.FETCH_USERS: h.handleUsersListResponse(packet); break;
                
                case Protocol.FRIEND_MSG:
                case Protocol.GROUP_MSG:
                case Protocol.GLOBAL_MSG:
                    if ("EVENT".equals(type)) {
                        h.handleIncomingMessage(packet, action);
                    } else {
                        LOGGER.log(Level.INFO, "Confirmación de envío recibida para: {0}", action);
                    }
                    break;

                case Protocol.NOTIFICATION:
                    h.handleNotification(packet);
                    break;

                case Protocol.FRIEND_REQUEST:
                    h.handleFriendRequestResponse(packet);
                    break;
                    
                case Protocol.GROUP_CREATE:
                    if ("success".equals(packet.getParam("status"))) {
                        // CAPTURAMOS EL ID REAL AQUÍ
                        ultimoGroupIdRecibido = packet.getIntParam("groupId");
                        LOGGER.log(Level.INFO, "ID de grupo capturado dinámicamente: {0}", ultimoGroupIdRecibido);
                    }
                    break;
            
                case Protocol.GROUP_INVITE:
                    // Capturamos los datos para el log usando getIntParam que es más robusto
                    int idG = packet.getIntParam("groupId");
                    if (idG == -1) idG = packet.getIntParam("relatedId");
    
                    // El servidor manda el ID del invitador en "from"
                    String invitador = packet.getParam("from");
                    if (invitador != null && invitador.endsWith(".0")) {
                        invitador = invitador.substring(0, invitador.length() - 2);
                    }

                    LOGGER.log(Level.INFO, "INVITACIÓN RECIBIDA: El usuario {0} te invitó al grupo {1}", invitador, idG);
                    break;

                case Protocol.GROUP_INVITE_ACCEPT:
                    if ("success".equals(packet.getParam("status"))) {
                        LOGGER.log(Level.INFO, "SISTEMA: Has aceptado la invitación al grupo {0} con éxito.", packet.getParam("groupId"));
                    }
                    break;

                case Protocol.GROUP_HISTORY:
                    // Simplemente avisar que el historial llegó (o pasarlo a la UI)
                    LOGGER.log(Level.INFO, "Historial de grupo recibido satisfactoriamente.");
                    break;
                    
                case Protocol.FRIEND_HISTORY:
                    if ("success".equals(packet.getParam("status"))) {
                    // El servidor manda una lista en el campo "history"
                        Object historyObj = packet.getPayload().get("history");
                        LOGGER.log(Level.INFO, "Historial privado recibido de usuario {0}. Datos: {1}", 
                        packet.getParam("targetUserId"), historyObj);
        
                        // Aquí llamarías a la UI para dibujar los mensajes en la ventana de chat
                    }
                    break;
                
                case Protocol.GLOBAL_FETCH_HISTORY:
                    h.handleGlobalHistoryResponse(packet);
                case Protocol.FETCH_NOTIFICATIONS:
                    LOGGER.log(Level.INFO, "Sincronización de notificaciones finalizada.");
                    break;
                    
                default:
                    LOGGER.log(Level.INFO, "Acción no manejada específicamente en el cliente: {0}", action);
                    break;
             
            
                }
            }catch (Exception e) {
                LOGGER.log(Level.ERROR, "Error interno en el Router al procesar paquete: ", e);
            }
        }

    
}