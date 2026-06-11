package chatcoco;

import Network.Handlers;
import Network.ServerConnection;
import UI.Login;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.swing.UIManager;

public class ChatCoco {
    private static final Logger LOGGER = System.getLogger("ClientMain");

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo establecer el LookAndFeel del sistema.");
        }

        try {
            LOGGER.log(Level.INFO, "Iniciando conexión con el servidor...");
            ServerConnection connection = new ServerConnection("localhost", 6767);
            Handlers.getInstance().init(connection);

            Thread networkThread = new Thread(connection);
            networkThread.setDaemon(true); 
            networkThread.start();

            java.awt.EventQueue.invokeLater(() -> {
                Login loginUI = new Login(Handlers.getInstance().getAuthService());
                Handlers.getInstance().setLoginWindow(loginUI);
                
                loginUI.setVisible(true);
                LOGGER.log(Level.INFO, "Interfaz de Login desplegada correctamente.");
            });

        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "Error fatal: No se pudo conectar con el servidor. ¿Está encendido?", e);
            javax.swing.JOptionPane.showMessageDialog(null, 
                "Error crítico: No se pudo conectar con el servidor.\n" + e.getMessage(), 
                "Error de Conexión", 
                javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}