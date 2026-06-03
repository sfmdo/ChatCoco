package UI.Views;

import ClientServices.FriendClientService;
import Models.User;
import UI.Components.ChatPanel;
import javax.swing.*;
import java.awt.*;

public class FriendsView extends JPanel {
    private DefaultListModel<User> friendsModel;
    private JList<User> friendsList;
    
    private FriendClientService friendService;
    private ChatPanel sharedChatPanel;

    public FriendsView(FriendClientService friendService, ChatPanel sharedChat) {
        this.friendService = friendService;
        this.sharedChatPanel = sharedChat;
        
        setLayout(new BorderLayout(10, 10));
        initComponents();
    }

    private void initComponents() {
        // 1. Modelo y Lista (Metadata de objetos User)
        friendsModel = new DefaultListModel<>();
        friendsList = new JList<>(friendsModel);
        friendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 2. Botones de acción específicos para Amigos
        JButton btnOpenChat = new JButton("Abrir Chat Privado");
        JButton btnRemoveFriend = new JButton("Eliminar Amigo");
        JButton btnRefresh = new JButton("Actualizar Lista");

        // --- ACCIÓN: ABRIR CHAT PRIVADO ---
        btnOpenChat.addActionListener(e -> {
            User selected = friendsList.getSelectedValue();
            if (selected != null) {
                // 1. Limpiamos y preparamos el panel de chat
                sharedChatPanel.clearMessages();
                
                // 2. Cambiamos el contexto a FRIEND para que el botón enviar sepa a quién va
                sharedChatPanel.setTargetContext("FRIEND", selected.getId(), selected.getUsername());
                
                // 3. Pedimos el historial persistente (SQL) al servidor
                friendService.fetchFriendHistory(selected.getId());
                
                sharedChatPanel.addBubble("Sistema", "Cargando conversación con " + selected.getUsername() + "...", false);
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona un amigo de la lista.");
            }
        });

        // --- ACCIÓN: ELIMINAR AMIGO ---
        btnRemoveFriend.addActionListener(e -> {
            User selected = friendsList.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "¿Estás seguro de eliminar a " + selected.getUsername() + "?\nSe borrará todo el historial de mensajes.", 
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    // Llamamos al servicio (necesitarás implementar removeFriend en el service)
                    // friendService.removeFriend(selected.getId());
                    friendsModel.removeElement(selected);
                }
            }
        });

        // --- ACCIÓN: REFRESCAR LISTA ---
        btnRefresh.addActionListener(e -> {
            friendService.fetchFriendsList();
            btnRefresh.setEnabled(false);
            btnRefresh.setText("Cargando...");
            
            new javax.swing.Timer(2000, evt -> {
                btnRefresh.setText("Actualizar Lista");
                btnRefresh.setEnabled(true);
            }).start();
        });

        // 3. Diseño del panel izquierdo (Espejo de UsersView)
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Mis Amigos"));
        leftPanel.add(new JScrollPane(friendsList), BorderLayout.CENTER);
        
        JPanel buttons = new JPanel(new GridLayout(3, 1, 5, 5));
        buttons.add(btnOpenChat);
        buttons.add(btnRemoveFriend);
        buttons.add(btnRefresh);
        
        leftPanel.add(buttons, BorderLayout.SOUTH);

        leftPanel.setPreferredSize(new Dimension(250, 0));
        add(leftPanel, BorderLayout.WEST);
    }

    // Getters para Handlers y Content
    public JList<User> getFriendsList() { return friendsList; }
    public DefaultListModel<User> getModel() { return friendsModel; }
}