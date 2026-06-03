package UI;

import ClientServices.AuthClientService;
import Network.Handlers; // Importamos los handlers
import javax.swing.*;
import java.awt.Component;

/**
 * Ventana de acceso al sistema.
 * Implementa protección contra spam y delegación de respuesta a Handlers.
 */
public final class Login extends JFrame {

    private JLabel usernameLabel, passwordLabel, messageLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton;
    private AuthClientService authService;

    public Login(AuthClientService authService) {
        super();
        this.authService = authService;
        
        // VINCULACIÓN CRÍTICA: Le decimos a Handlers que esta es la ventana activa
        Handlers.getInstance().setLoginWindow(this);
        
        init();
    }
    
  
    // Bloquea o desbloquea la interfaz mientras se espera respuesta del servidor.
    // Previene que el usuario envíe 800 peticiones.
 
    public void setLoading(boolean isLoading) {
        loginButton.setEnabled(!isLoading);
        registerButton.setEnabled(!isLoading);
        usernameField.setEnabled(!isLoading);
        passwordField.setEnabled(!isLoading);
    
        if (isLoading) {
            messageLabel.setText("Connecting to server, please wait...");
        }
    }
    
    
    // Método para mostrar mensajes que vienen desde el servidor (vía Handlers).
    
    public void showMessage(String msg, boolean isError) {
        messageLabel.setText(msg);
        if (isError) {
            messageLabel.setForeground(java.awt.Color.RED);
        } else {
            messageLabel.setForeground(new java.awt.Color(0, 150, 0)); // Verde para éxito
        }
        setLoading(false); // Siempre desbloqueamos al recibir respuesta
    }
    
    public void showErrorMessage(String msg) {
        messageLabel.setForeground(java.awt.Color.RED); // Ponemos el texto en rojo
        messageLabel.setText(msg);
        setLoading(false); // IMPORTANTE: Volvemos a habilitar la interfaz
    }
    
    public void showSuccessMessage(String msg) {
        messageLabel.setForeground(new java.awt.Color(0, 150, 0)); // Ponemos el texto en verde
        messageLabel.setText(msg);
        setLoading(false); // Volvemos a habilitar para que el usuario pueda loguearse
    }

    public void init() {
        setTitle("Messenger - Login & Register");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);

        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        messageLabel = new JLabel("", SwingConstants.CENTER); // Centrado

        // Posicionamiento de componentes
        setPosition(usernameLabel, 50, 50, 100, 30);
        setPosition(usernameField, 150, 50, 180, 30);
        setPosition(passwordLabel, 50, 100, 100, 30);
        setPosition(passwordField, 150, 100, 180, 30);

        setPosition(loginButton, 80, 160, 110, 30);
        setPosition(registerButton, 210, 160, 110, 30);
        setPosition(messageLabel, 25, 220, 350, 30);

        add(usernameLabel); add(usernameField);
        add(passwordLabel); add(passwordField);
        add(loginButton); add(registerButton);
        add(messageLabel);

        // --- LÓGICA DE BOTÓN: LOGIN ---
        loginButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword());
    
            if(user.isEmpty() || pass.isEmpty()) {
                showMessage("Please enter all credentials.", true);
                return;
            }

            setLoading(true); 
            authService.login(user, pass); // La respuesta llegará a Handlers.handleLoginResponse
        });

        // --- LÓGICA DE BOTÓN: REGISTER ---
        registerButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword());
            
            if(user.isEmpty() || pass.isEmpty()) {
                showMessage("Username and Password are required.", true);
                return;
            }

            setLoading(true); 
            authService.register(user, pass); // La respuesta llegará a Handlers.handleRegisterResponse
        });
    }
    
    private void setPosition(Component component, int x, int y, int width, int height) {
        component.setBounds(x, y, width, height);
    }
}