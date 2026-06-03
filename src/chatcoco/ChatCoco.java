/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package chatcoco;

import ClientServices.*;
import Network.Handlers;
import Network.ServerConnection;
import UI.Login;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.swing.UIManager;

/**
 *
 * @author sfmdo
 */
public class ChatCoco {
    private static final Logger LOGGER = System.getLogger("ClientMain");
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignorar si falla
        }

        try {
            LOGGER.log(Level.INFO, "Conectando al servidor...");

            // 2. Iniciar la conexión de red (Capa 1)
            // Usamos el puerto 6767 que definimos en el ServerCore
            ServerConnection connection = new ServerConnection("localhost", 6767, null);
            Handlers.getInstance().setConnection(connection); 
            // 3. Arrancar el hilo que escucha al servidor en segundo plano
            Thread networkThread = new Thread(connection);
            networkThread.setDaemon(true); // Se cierra automáticamente si la app se cierra
            networkThread.start();

            // 4. Inicializar el Servicio de Autenticación
            AuthClientService authService = new AuthClientService(connection);

            // 5. Lanzar la Interfaz Gráfica en el EDT (Event Dispatch Thread)
            java.awt.EventQueue.invokeLater(() -> {
                // Creamos la ventana pasándole su servicio
                Login loginUI = new Login(authService);
                
                // IMPORTANTE: Registramos la ventana en los Handlers 
                // para que el Router sepa a quién mandarle las respuestas
                Handlers.getInstance().setLoginWindow(loginUI);
                
                // Mostrar la ventana
                loginUI.setVisible(true);
                
                LOGGER.log(Level.INFO, "Interfaz de Login desplegada.");
            });

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "No se pudo iniciar el cliente. ¿Está el servidor encendido?", e);
            javax.swing.JOptionPane.showMessageDialog(null, 
                "Error: No se pudo conectar con el servidor.\n" + e.getMessage(), 
                "Error de Conexión", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
}