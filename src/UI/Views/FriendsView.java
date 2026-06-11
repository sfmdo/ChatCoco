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
        friendsModel = new DefaultListModel<>();
        friendsList = new JList<>(friendsModel);
        friendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton btnOpenChat = new JButton("Abrir Chat Privado");
        JButton btnRemoveFriend = new JButton("Eliminar Amigo");
        JButton btnRefresh = new JButton("Actualizar Lista");

        btnOpenChat.addActionListener(e -> {
            User selected = friendsList.getSelectedValue();
            if (selected != null) {
                sharedChatPanel.clearMessages();
                sharedChatPanel.setTargetContext("FRIEND", selected.getId(), selected.getUsername());
                friendService.fetchFriendHistory(selected.getId());
                
                sharedChatPanel.addBubble("Sistema", "Cargando conversación con " + selected.getUsername() + "...", false);
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona un amigo de la lista.");
            }
        });

        btnRemoveFriend.addActionListener(e -> {
            User selected = friendsList.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "¿Estás seguro de eliminar a " + selected.getUsername() + "?\nSe borrará todo el historial de mensajes.", 
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    friendsModel.removeElement(selected);
                }
            }
        });

        btnRefresh.addActionListener(e -> {
            friendService.fetchFriendsList();
            btnRefresh.setEnabled(false);
            btnRefresh.setText("Cargando...");
            
            new javax.swing.Timer(2000, evt -> {
                btnRefresh.setText("Actualizar Lista");
                btnRefresh.setEnabled(true);
            }).start();
        });

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

    public JList<User> getFriendsList() { return friendsList; }
    public DefaultListModel<User> getModel() { return friendsModel; }
}