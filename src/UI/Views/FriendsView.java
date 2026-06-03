package UI.Views;

import ClientServices.FriendClientService;
import Models.User;
import UI.Components.ChatPanel;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FriendsView extends JPanel {
    private DefaultListModel<User> friendsModel;
    private JList<User> friendsList;
    private FriendClientService friendService;
    private ChatPanel chatPanel;

    public FriendsView(FriendClientService service, ChatPanel sharedChat) {
        this.friendService = service;
        this.chatPanel = sharedChat;
        setLayout(new BorderLayout(10, 10));
        initComponents();
    }

    private void initComponents() {
        // 1. Lista de Amigos
        friendsModel = new DefaultListModel<>();
        friendsList = new JList<>(friendsModel);
        friendsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Personalizar cómo se ve cada amigo en la lista
        friendsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof User) {
                    User user = (User) value;
                    setText(user.getUsername() + " (" + user.getStatus() + ")");
                    // Cambiar color según estado
                    if ("ONLINE".equalsIgnoreCase(user.getStatus())) {
                        setForeground(new Color(0, 150, 0)); // Verde
                    } else {
                        setForeground(Color.GRAY);
                    }
                }
                return this;
            }
        });

        // 2. Botones de Acción
        JButton openChatBtn = new JButton("Abrir Chat Privado");
        JButton addFriendBtn = new JButton("Nueva Solicitud");
        JButton removeBtn = new JButton("Eliminar Amigo");

        // --- Lógica: Abrir Chat ---
        openChatBtn.addActionListener(e -> {
            User selected = friendsList.getSelectedValue();
            if (selected != null) {
                chatPanel.clearMessages();
                //chatPanel.setChatTitle("Chat con: " + selected.getUsername());
                // Pedimos el historial al servidor
                friendService.fetchFriendHistory(selected.getId());
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona un amigo de la lista.");
            }
        });

        // --- Lógica: Enviar Solicitud ---
        addFriendBtn.addActionListener(e -> {
            String targetName = JOptionPane.showInputDialog(this, "Escribe el nombre del usuario:");
            if (targetName != null && !targetName.trim().isEmpty()) {
                // Mandamos el NOMBRE al servidor, el servidor buscará la ID
                //friendService.sendFriendRequestByName(targetName.trim());
                JOptionPane.showMessageDialog(this, "Solicitud enviada a " + targetName);
            }
        });

        // --- Lógica: Eliminar ---
        removeBtn.addActionListener(e -> {
            User selected = friendsList.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(this, "¿Eliminar a " + selected.getUsername() + "?");
                if (confirm == JOptionPane.YES_OPTION) {
                    // El usuario seleccionó un nombre, pero el código manda la ID
                    //friendService.removeFriend(selected.getId()); 
                    friendsModel.removeElement(selected);
                }
            }
        });

        // 3. Ensamblado de Paneles
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Mis Amigos"));
        leftPanel.add(new JScrollPane(friendsList), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        btnPanel.add(openChatBtn);
        btnPanel.add(addFriendBtn);
        btnPanel.add(removeBtn);
        leftPanel.add(btnPanel, BorderLayout.SOUTH);

        leftPanel.setPreferredSize(new Dimension(250, 0));
        add(leftPanel, BorderLayout.WEST);
        
        // El centro queda vacío para que Content.java coloque el ChatPanel compartido
    }

    /**
     * Método para que los Handlers actualicen la lista cuando el servidor responda
     */
    public void setFriendsList(List<User> friends) {
        SwingUtilities.invokeLater(() -> {
            friendsModel.clear();
            for (User u : friends) {
                friendsModel.addElement(u);
            }
        });
    }
}