/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TestConexionServer;

import ClientServices.AuthClientService;
import ClientServices.FriendClientService;
import ClientServices.GroupClientService;
import Network.ClientRouter;
import Network.ServerConnection;
import java.io.IOException;

/**
 *
 * @author sfmdo
 */
public class TestConexionServer {
    private static final System.Logger LOGGER = System.getLogger("TestClient");
    private static ServerConnection connection;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
            try {
            LOGGER.log(System.Logger.Level.INFO, "=== INICIANDO PRUEBA DE CICLO COMPLETO ===");

            // --- CLIENTE 1 (Usuario 5) ---
            ServerConnection c1 = new ServerConnection("localhost", 6767, null);
            new Thread(c1).start();
            AuthClientService auth1 = new AuthClientService(c1);
            GroupClientService groups1 = new GroupClientService(c1);
            FriendClientService social1 = new FriendClientService(c1);

            // --- CLIENTE 2 (Usuario 7) ---
            ServerConnection c2 = new ServerConnection("localhost", 6767, null);
            new Thread(c2).start();
            AuthClientService auth2 = new AuthClientService(c2);
            GroupClientService groups2 = new GroupClientService(c2);
            FriendClientService social2 = new FriendClientService(c2);

            // 1. Logins
            auth1.login("tester1", "clave123");
            auth2.login("tester2", "clave123");
            esperar(2000);

            // 2. Crear Grupo e Invitar (Usuario 5)
            groups1.createGroup("Grupo Final");
            esperar(2000);
            int groupId = ClientRouter.ultimoGroupIdRecibido;
            groups1.sendGroupInvitation(groupId, 7);
            groups1.sendGroupInvitation(groupId, 9); // Invitamos al 9 aunque no esté en este hilo para cumplir la regla de 3
            esperar(1000);

            // 3. Aceptar Grupo (Usuario 7)
            LOGGER.log(System.Logger.Level.INFO, "Usuario 7 aceptando invitación al grupo {0}...", groupId);
            groups2.acceptGroupInvitation(groupId);
            esperar(1500);

            // 4. Chat de Grupo (Ahora Usuario 7 ya es miembro oficial)
            groups1.sendGroupMessage(groupId, "¡Hola Tester2! Bienvenido al grupo.");
            esperar(1000);

            // 5. Flujo de Amistad (5 pide a 7)
            social1.sendFriendRequest(7);
            esperar(2000);
            
            // 6. Aceptar Amistad (Usuario 7 usa la ID que llegó por notificación)
            int fId = ClientRouter.ultimaNotificacionFriendshipId;
            int reqId = ClientRouter.ultimaNotificacionRequesterId;
            if (fId != -1) {
                LOGGER.log(System.Logger.Level.INFO, "Usuario 7 aceptando amistad {0} de usuario {1}...", fId, reqId);
                social2.acceptFriendRequest(fId, reqId);
            }
            esperar(1500);

            // 7. Mensaje Privado (Ahora que son amigos)
            social1.sendPrivateMessage(7, "Este es un mensaje privado secreto");
            esperar(1000);

            // 8. Carga de Historial (Usuario 7 pide ver el historial del grupo)
            LOGGER.log(System.Logger.Level.INFO, "Usuario 7 solicitando historial del grupo...");
            groups2.fetchHistory(groupId);
            esperar(2000);
            
            LOGGER.log(System.Logger.Level.INFO, "Paso 9: Usuario 7 solicitando historial de chat privado con usuario 5...");
            social2.fetchFriendHistory(5);
            esperar(2000);

            LOGGER.log(System.Logger.Level.INFO, "=== FIN DEL SUPER TEST ===");

        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.ERROR, "Fallo en el test: ", e);
        }
    }

    private static void esperar(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}
