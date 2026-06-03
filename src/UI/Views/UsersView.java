package UI.Views;

import ClientServices.ChatGlobalClientService;
import ClientServices.FriendClientService;
import ClientServices.UserClientService;
import Models.User;
import UI.Components.ChatPanel;
import javax.swing.*;
import java.awt.*;

public class UsersView extends JPanel {
    private DefaultListModel<User> usersModel;
    private JList<User> usersList;
    private ChatGlobalClientService globalChatService;
    private FriendClientService friendService;
    private ChatPanel sharedChatPanel;
    private UserClientService userService;
    private JButton btnAddFriend = new JButton("Enviar Solicitud Amistad");

    public UsersView(ChatGlobalClientService globalChat, FriendClientService friendService, ChatPanel sharedChat,UserClientService sharedUser) {
        this.globalChatService = globalChat;
        this.friendService = friendService;
        this.sharedChatPanel = sharedChat;
        this.userService = sharedUser;
        setLayout(new BorderLayout(10, 10));
        initComponents();
    }

    private void initComponents() {
        usersModel = new DefaultListModel<>();
        usersList = new JList<>(usersModel);
        usersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton btnOpenChat = new JButton("Abrir Chat Global");
        JButton btnRefresh = new JButton("Actualizar Usuarios");
        btnRefresh.addActionListener(e -> {
            // Llamamos al servicio para pedir la lista de nuevo
            // Asumiendo que pasaste el userClientService al constructor
            userService.fetchUsers(); 
            btnRefresh.setText("Cargando...");
            btnRefresh.setEnabled(false);
        
            // Timer pequeño para reactivar el botón (opcional)
            new javax.swing.Timer(2000, evt -> {
                btnRefresh.setText("Actualizar Usuarios");
                btnRefresh.setEnabled(true);
            }).start();
        });
        // --- ACCIÓN: ABRIR CHAT (Usa Metadata) ---
        btnOpenChat.addActionListener(e -> {
            User selected = usersList.getSelectedValue();
            if (selected != null) {
                // 1. Cambiamos el contexto visual
                sharedChatPanel.clearMessages();
                sharedChatPanel.setTargetContext("GLOBAL", selected.getId(), selected.getUsername());
        
                // 2. PEDIMOS EL HISTORIAL AL SERVIDOR
                // Esto llamará a GLOBAL_FETCH_HISTORY en el servidor
                globalChatService.fetchHistory(selected.getId());
        
                sharedChatPanel.addBubble("Sistema", "Cargando historial desde el servidor...", false);
            }
        });

        // --- ACCIÓN: AMISTAD (Usa Metadata) ---
        btnAddFriend.addActionListener(e -> {
            User selected = usersList.getSelectedValue();
            if (selected != null) {
                // 1. Bloqueamos el botón para evitar clics repetidos
                btnAddFriend.setEnabled(false);
                btnAddFriend.setText("Enviando...");

            // 2. Enviamos la petición
            friendService.sendFriendRequest(selected.getId());
        
            // El feedback final lo dará el Handler más tarde
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona un usuario de la lista.");
            }
        });

        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.add(new JScrollPane(usersList), BorderLayout.CENTER);
        
        JPanel buttons = new JPanel(new GridLayout(3, 1, 5, 5));
        buttons.add(btnOpenChat);
        buttons.add(btnAddFriend);
        buttons.add(btnRefresh);
        
        leftPanel.add(buttons, BorderLayout.SOUTH);

        leftPanel.setPreferredSize(new Dimension(250, 0));
        add(leftPanel, BorderLayout.WEST);
    }
    
    public void resetFriendButton() {
        SwingUtilities.invokeLater(() -> {
            btnAddFriend.setEnabled(true);
            btnAddFriend.setText("Agregar a mis Amigos");
        });
    }

    public JList<User> getUsersList() { return usersList; }
    public DefaultListModel<User> getModel() { return usersModel; }
}