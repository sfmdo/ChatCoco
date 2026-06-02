package UI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Box;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

public class Content extends JFrame { 
//Esta versión reutiliza el panel de chat en varias pestañas, pero cada pestaña crea su propio chatArea
//convendría tener un solo panel de chat global y cambiar solo la lista izquierda
    private static final String CARD_USERS = "USERS";
    private static final String CARD_FRIENDS = "FRIENDS";
    private static final String CARD_GROUPS = "GROUPS";
    private static final String CARD_NOTIFICATIONS = "NOTIFICATIONS";

    private JPanel headerPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    private JPanel chatPanel;
    private JPanel usersSplitPane;
    private JPanel friendsSplitPane;
    private JPanel groupsSplitPane;

    private JPanel messagesContainer;
    private JScrollPane messagesScrollPane;
    private JTextField messageField;
    private JLabel chatTitleLabel;

    private String selectedChat = "Ningún chat seleccionado";

    private DefaultListModel<String> usersModel;
    private DefaultListModel<String> friendsModel;
    private DefaultListModel<String> groupsModel;
    private DefaultListModel<String> notificationsModel;
    private JPanel notificationsContainer;

    public Content() {
        super("ChatCoco");
        Network.ClientRouter.setUI(this);
        init();
    }

    public void init() {
        setTitle("ChatCoco");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        createHeader();

        // Inicializamos la instancia compartida de chatPanel
        chatPanel = createChatPanel();

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(createUsersPanel(), CARD_USERS);
        contentPanel.add(createFriendsPanel(), CARD_FRIENDS);
        contentPanel.add(createGroupsPanel(), CARD_GROUPS);
        contentPanel.add(createNotificationsPanel(), CARD_NOTIFICATIONS);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        showCard(CARD_USERS);
    }

    private void createHeader() {
        headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        headerPanel.setBackground(new Color(245, 245, 245));

        JButton usersButton = createHeaderButton("Usuarios", CARD_USERS);
        JButton friendsButton = createHeaderButton("Amigos", CARD_FRIENDS);
        JButton groupsButton = createHeaderButton("Grupos", CARD_GROUPS);
        JButton notificationsButton = createHeaderButton("Notificaciones", CARD_NOTIFICATIONS);

        headerPanel.add(usersButton);
        headerPanel.add(friendsButton);
        headerPanel.add(groupsButton);
        headerPanel.add(notificationsButton);
    }

    private JButton createHeaderButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(140, 32));
        button.addActionListener(e -> showCard(cardName));
        return button;
    }

    private JPanel createUsersPanel() {
        usersModel = new DefaultListModel<>();
        usersModel.addElement("Oscar - En línea");
        usersModel.addElement("Lucia - Desconectado");
        usersModel.addElement("Carlos - En línea");
        usersModel.addElement("Mariana - En línea");

        JList<String> usersList = createList(usersModel);

        JButton messageButton = new JButton("Enviar mensaje");
        JButton addFriendButton = new JButton("Agregar amigo");

        messageButton.addActionListener(e -> {
            String selected = usersList.getSelectedValue();

            if (selected == null) {
                showWarning("Selecciona un usuario primero.");
                return;
            }

            openChat("Chat con " + getNameOnly(selected));
        });

        addFriendButton.addActionListener(e -> {
            String selected = usersList.getSelectedValue();

            if (selected == null) {
                showWarning("Selecciona un usuario primero.");
                return;
            }

            String name = getNameOnly(selected);

            if (!friendsModel.contains(name + " - En línea")) {
                friendsModel.addElement(name + " - En línea");
            }

            notificationsModel.addElement("Solicitud enviada a " + name);
            JOptionPane.showMessageDialog(this, "Solicitud de amistad enviada a " + name);
        });

        JPanel leftPanel = createLeftPanel("Usuarios disponibles", usersList, messageButton, addFriendButton);
        usersSplitPane = createMainSplitPanel(leftPanel, chatPanel);
        return usersSplitPane;
    }

    private JPanel createFriendsPanel() {
        friendsModel = new DefaultListModel<>();
        friendsModel.addElement("Amigo 1 - En línea");
        friendsModel.addElement("Amigo 2 - En línea");

        JList<String> friendsList = createList(friendsModel);

        JButton openChatButton = new JButton("Abrir chat");
        JButton removeFriendButton = new JButton("Eliminar amigo");

        openChatButton.addActionListener(e -> {
            String selected = friendsList.getSelectedValue();

            if (selected == null) {
                showWarning("Selecciona un amigo primero.");
                return;
            }

            openChat("Chat privado con " + getNameOnly(selected));
        });

        removeFriendButton.addActionListener(e -> {
            String selected = friendsList.getSelectedValue();

            if (selected == null) {
                showWarning("Selecciona un amigo primero.");
                return;
            }

            friendsModel.removeElement(selected);
            notificationsModel.addElement("Eliminaste a " + getNameOnly(selected) + " de amigos");
        });

        JPanel leftPanel = createLeftPanel("Mis amigos", friendsList, openChatButton, removeFriendButton);
        friendsSplitPane = createMainSplitPanel(leftPanel, chatPanel);
        return friendsSplitPane;
    }

    private JPanel createGroupsPanel() {
        groupsModel = new DefaultListModel<>();
        groupsModel.addElement("Grupo de Estudio");
        groupsModel.addElement("Grupo de Trabajo");

        JList<String> groupsList = createList(groupsModel);

        JButton openGroupButton = new JButton("Abrir chat");
        JButton createGroupButton = new JButton("Crear grupo");
        JButton inviteButton = new JButton("Invitar persona");

        openGroupButton.addActionListener(e -> {
            String selected = groupsList.getSelectedValue();

            if (selected == null) {
                showWarning("Selecciona un grupo primero.");
                return;
            }

            openChat("Chat del grupo: " + selected);
        });

        createGroupButton.addActionListener(e -> {
            String groupName = JOptionPane.showInputDialog(this, "Nombre del grupo:");

            if (groupName == null || groupName.trim().isEmpty()) {
                return;
            }

            String trimmedGroupName = groupName.trim();
            groupsModel.addElement(trimmedGroupName);

            Network.ServerConnection conn = Network.ClientRouter.getServerConnection();
            if (conn != null) {
                Messages.MessagePacket packet = Messages.MessagePacket.event(Network.Protocol.GROUP_CREATE);
                packet.add("groupName", trimmedGroupName);
                conn.sendPacket(packet);
            }
            
            addNotification("Grupo creado localmente: " + trimmedGroupName, "", "SYSTEM");
        });

        inviteButton.addActionListener(e -> {
            String selectedGroup = groupsList.getSelectedValue();

            if (selectedGroup == null) {
                showWarning("Selecciona un grupo primero.");
                return;
            }

            String userName = JOptionPane.showInputDialog(this, "Nombre del usuario a invitar:");

            if (userName == null || userName.trim().isEmpty()) {
                return;
            }

            String trimmedUserName = userName.trim();
            Network.ServerConnection conn = Network.ClientRouter.getServerConnection();
            if (conn != null) {
                Messages.MessagePacket packet = Messages.MessagePacket.event(Network.Protocol.GROUP_INVITE);
                packet.add("groupName", selectedGroup);
                packet.add("invitee", trimmedUserName);
                conn.sendPacket(packet);
            }

            addNotification("Invitaste a " + trimmedUserName + " al grupo " + selectedGroup, "", "SYSTEM");
            JOptionPane.showMessageDialog(this, "Invitación enviada a " + trimmedUserName);
        });

        JPanel leftPanel = createLeftPanel("Mis grupos", groupsList, openGroupButton, createGroupButton, inviteButton);
        groupsSplitPane = createMainSplitPanel(leftPanel, chatPanel);
        return groupsSplitPane;
    }

    private JPanel createNotificationsPanel() {
        notificationsModel = new DefaultListModel<>();
        notificationsContainer = new JPanel();
        notificationsContainer.setLayout(new BoxLayout(notificationsContainer, BoxLayout.Y_AXIS));
        notificationsContainer.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(notificationsContainer);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JButton clearButton = new JButton("Limpiar notificaciones");
        clearButton.addActionListener(e -> {
            notificationsContainer.removeAll();
            notificationsContainer.revalidate();
            notificationsContainer.repaint();
        });

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Notificaciones"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Notificación de bienvenida inicial
        addNotification("Bienvenido a ChatCoco", "", "SYSTEM");

        return panel;
    }

    public void addNotification(String content, String relatedId, String type) {
        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        JLabel label = new JLabel(content);
        card.add(label, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        actionPanel.setBackground(Color.WHITE);

        if ("FRIEND_REQUEST".equals(type) || "GROUP_INVITE".equals(type)) {
            JButton acceptButton = new JButton("Aceptar");
            JButton declineButton = new JButton("Rechazar");

            acceptButton.addActionListener(e -> {
                sendAcceptRequest(type, relatedId);
                notificationsContainer.remove(card);
                notificationsContainer.revalidate();
                notificationsContainer.repaint();
                JOptionPane.showMessageDialog(this, "Solicitud aceptada.");
            });

            declineButton.addActionListener(e -> {
                sendDeclineRequest(type, relatedId);
                notificationsContainer.remove(card);
                notificationsContainer.revalidate();
                notificationsContainer.repaint();
                JOptionPane.showMessageDialog(this, "Solicitud rechazada.");
            });

            actionPanel.add(acceptButton);
            actionPanel.add(declineButton);
        } else {
            JButton dismissButton = new JButton("Eliminar");
            dismissButton.addActionListener(e -> {
                notificationsContainer.remove(card);
                notificationsContainer.revalidate();
                notificationsContainer.repaint();
            });
            actionPanel.add(dismissButton);
        }

        card.add(actionPanel, BorderLayout.EAST);
        notificationsContainer.add(card);
        notificationsContainer.revalidate();
        notificationsContainer.repaint();
    }

    private void sendAcceptRequest(String type, String relatedId) {
        Network.ServerConnection conn = Network.ClientRouter.getServerConnection();
        if (conn == null) {
            System.out.println("No hay conexión activa con el servidor.");
            return;
        }
        Messages.MessagePacket packet;
        if ("FRIEND_REQUEST".equals(type)) {
            packet = Messages.MessagePacket.event(Network.Protocol.FRIEND_ACCEPT);
            packet.add("requestId", relatedId);
        } else {
            packet = Messages.MessagePacket.event(Network.Protocol.GROUP_INVITE_ACCEPT);
            packet.add("inviteId", relatedId);
        }
        conn.sendPacket(packet);
    }

    private void sendDeclineRequest(String type, String relatedId) {
        Network.ServerConnection conn = Network.ClientRouter.getServerConnection();
        if (conn == null) {
            System.out.println("No hay conexión activa con el servidor.");
            return;
        }
        Messages.MessagePacket packet;
        if ("FRIEND_REQUEST".equals(type)) {
            packet = Messages.MessagePacket.event(Network.Protocol.FRIEND_DECLINE);
            packet.add("requestId", relatedId);
        } else {
            packet = Messages.MessagePacket.event(Network.Protocol.GROUP_INVITE_DECLINE);
            packet.add("inviteId", relatedId);
        }
        conn.sendPacket(packet);
    }

    public void loadConversationMessages(java.util.List<Messages.MessagePacket> messages) {
        messagesContainer.removeAll();
        addMessageBubble("Sistema", "Historial de " + selectedChat + " cargado.", false);
        for (Messages.MessagePacket msg : messages) {
            String from = msg.getParam("from");
            String text = msg.getParam("text");
            boolean isSelf = "Yo".equalsIgnoreCase(from);
            addMessageBubble(from != null ? from : "Desconocido", text, isSelf);
        }
        messagesContainer.revalidate();
        messagesContainer.repaint();
    }

    public void appendIncomingMessage(String from, String text) {
        if (messagesContainer != null) {
            boolean isSelf = "Yo".equalsIgnoreCase(from);
            addMessageBubble(from != null ? from : "Desconocido", text, isSelf);
        }
    }

    private JPanel createMainSplitPanel(JPanel leftPanel, JPanel rightPanel) {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        leftPanel.setPreferredSize(new Dimension(280, 600));

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);

        return mainPanel;
    }

    private JPanel createLeftPanel(String title, JList<String> list, JButton... buttons) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder(title));

        panel.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(buttons.length, 1, 5, 5));

        for (JButton button : buttons) {
            buttonPanel.add(button);
        }

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Conversación"));

        chatTitleLabel = new JLabel(selectedChat);
        chatTitleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        messagesContainer = new JPanel();
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBackground(Color.WHITE);

        messagesScrollPane = new JScrollPane(messagesContainer);
        messagesScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        messagesScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        messageField = new JTextField();

        JButton sendButton = new JButton("Enviar");

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        panel.add(chatTitleLabel, BorderLayout.NORTH);
        panel.add(messagesScrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JList<String> createList(DefaultListModel<String> model) {
        JList<String> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return list;
    }

    private void openChat(String title) {
        selectedChat = title;

        if (chatTitleLabel != null) {
            chatTitleLabel.setText(selectedChat);
        }

        if (messagesContainer != null) {
            messagesContainer.removeAll();
            addMessageBubble("Sistema", "Abriste " + selectedChat, false);
            messagesContainer.revalidate();
            messagesContainer.repaint();
            if (messagesScrollPane != null) {
                messagesScrollPane.revalidate();
                messagesScrollPane.repaint();
            }
        }

        Network.ServerConnection conn = Network.ClientRouter.getServerConnection();
        if (conn != null) {
            Messages.MessagePacket packet;
            if (selectedChat.startsWith("Chat del grupo:")) {
                String groupName = selectedChat.replace("Chat del grupo: ", "").trim();
                packet = Messages.MessagePacket.event(Network.Protocol.GROUP_HISTORY);
                packet.add("groupName", groupName);
            } else {
                String username = getNameOnly(selectedChat.replace("Chat privado con ", "").replace("Chat con ", "").trim());
                packet = Messages.MessagePacket.event(Network.Protocol.FRIEND_MSG);
                packet.setAction("FETCH_HISTORY"); // O según el protocolo del servidor
                packet.add("username", username);
            }
            conn.sendPacket(packet);
        }
    }

    private void sendMessage() {
        String text = messageField.getText().trim();

        if (selectedChat.equals("Ningún chat seleccionado")) {
            showWarning("Primero abre un chat.");
            return;
        }

        if (text.isEmpty()) {
            return;
        }

        Network.ServerConnection conn = Network.ClientRouter.getServerConnection();
        if (conn != null) {
            Messages.MessagePacket packet;
            if (selectedChat.startsWith("Chat del grupo:")) {
                String groupName = selectedChat.replace("Chat del grupo: ", "").trim();
                packet = Messages.MessagePacket.event(Network.Protocol.GROUP_MSG);
                packet.add("groupName", groupName);
                packet.add("text", text);
            } else {
                String username = getNameOnly(selectedChat.replace("Chat privado con ", "").replace("Chat con ", "").trim());
                packet = Messages.MessagePacket.event(Network.Protocol.FRIEND_MSG);
                packet.add("toUser", username);
                packet.add("text", text);
            }
            conn.sendPacket(packet);
        }

        addMessageBubble("Yo", text, true);
        messageField.setText("");
    }

    private void addMessageBubble(String sender, String text, boolean isSelf) {
        JPanel wrapper = new JPanel(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 1)) {
            @Override
            public Dimension getMaximumSize() {
                Dimension pref = getPreferredSize();
                return new Dimension(Integer.MAX_VALUE, pref.height);
            }
        };
        wrapper.setOpaque(false);

        // Sender label
        JLabel senderLabel = new JLabel(isSelf ? "Yo" : sender);
        senderLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        senderLabel.setForeground(Color.GRAY);

        // Bubble shape panel
        JPanel bubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Si es propio: azul suave; si es ajeno o sistema: gris suave
                if ("Sistema".equalsIgnoreCase(sender)) {
                    g2.setColor(new Color(245, 245, 245));
                } else {
                    g2.setColor(isSelf ? new Color(195, 235, 255) : new Color(230, 230, 230));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bubble.setLayout(new BorderLayout());

        // Text inside bubble
        JTextArea textArea = new JTextArea(text);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        if ("Sistema".equalsIgnoreCase(sender)) {
            textArea.setFont(new Font("SansSerif", Font.ITALIC, 11));
            textArea.setForeground(Color.DARK_GRAY);
        }
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setBackground(new Color(0,0,0,0));
        textArea.setColumns(25);
        bubble.add(textArea, BorderLayout.CENTER);

        // Si es propio (isSelf), primero va la burbuja y luego la etiqueta "Yo" a la derecha
        // Si es ajeno, primero va la etiqueta con el nombre del remitente y luego la burbuja a la derecha
        if (isSelf) {
            wrapper.add(bubble);
            wrapper.add(Box.createHorizontalStrut(5));
            wrapper.add(senderLabel);
        } else {
            if (!"Sistema".equalsIgnoreCase(sender)) {
                wrapper.add(senderLabel);
                wrapper.add(Box.createHorizontalStrut(5));
            }
            wrapper.add(bubble);
        }

        messagesContainer.add(wrapper);
        messagesContainer.add(Box.createVerticalStrut(2));
        
        messagesContainer.revalidate();
        messagesContainer.repaint();
        if (messagesScrollPane != null) {
            messagesScrollPane.revalidate();
            messagesScrollPane.repaint();
        }

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            if (messagesScrollPane != null) {
                JScrollBar vertical = messagesScrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    private String getNameOnly(String text) {
        if (text.contains("-")) {
            return text.substring(0, text.indexOf("-")).trim();
        }

        return text.trim();
    }

    private void showCard(String card) {
        cardLayout.show(contentPanel, card);
        if (CARD_USERS.equals(card) && usersSplitPane != null) {
            usersSplitPane.add(chatPanel, BorderLayout.CENTER);
            usersSplitPane.revalidate();
            usersSplitPane.repaint();
        } else if (CARD_FRIENDS.equals(card) && friendsSplitPane != null) {
            friendsSplitPane.add(chatPanel, BorderLayout.CENTER);
            friendsSplitPane.revalidate();
            friendsSplitPane.repaint();
        } else if (CARD_GROUPS.equals(card) && groupsSplitPane != null) {
            groupsSplitPane.add(chatPanel, BorderLayout.CENTER);
            groupsSplitPane.revalidate();
            groupsSplitPane.repaint();
        }
        revalidate();
        repaint();
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
