package UI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

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

    private JTextArea chatArea;
    private JTextField messageField;
    private JLabel chatTitleLabel;

    private String selectedChat = "Ningún chat seleccionado";

    private DefaultListModel<String> usersModel;
    private DefaultListModel<String> friendsModel;
    private DefaultListModel<String> groupsModel;
    private DefaultListModel<String> notificationsModel;

    public Content() {
        super("ChatCoco");
        init();
    }

    public void init() {
        setTitle("ChatCoco");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        createHeader();

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
        JPanel chatPanel = createChatPanel();

        return createMainSplitPanel(leftPanel, chatPanel);
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
        JPanel chatPanel = createChatPanel();

        return createMainSplitPanel(leftPanel, chatPanel);
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

            groupsModel.addElement(groupName.trim());
            notificationsModel.addElement("Grupo creado: " + groupName.trim());
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

            notificationsModel.addElement("Invitaste a " + userName.trim() + " al grupo " + selectedGroup);
            JOptionPane.showMessageDialog(this, "Invitación enviada a " + userName.trim());
        });

        JPanel leftPanel = createLeftPanel("Mis grupos", groupsList, openGroupButton, createGroupButton, inviteButton);
        JPanel chatPanel = createChatPanel();

        return createMainSplitPanel(leftPanel, chatPanel);
    }

    private JPanel createNotificationsPanel() {
        notificationsModel = new DefaultListModel<>();
        notificationsModel.addElement("Bienvenido a ChatCoco");
        notificationsModel.addElement("No tienes solicitudes pendientes");

        JList<String> notificationsList = createList(notificationsModel);

        JButton markReadButton = new JButton("Marcar como leída");
        JButton clearButton = new JButton("Limpiar notificaciones");

        markReadButton.addActionListener(e -> {
            String selected = notificationsList.getSelectedValue();

            if (selected == null) {
                showWarning("Selecciona una notificación primero.");
                return;
            }

            int index = notificationsList.getSelectedIndex();
            notificationsModel.set(index, "[Leída] " + selected);
        });

        clearButton.addActionListener(e -> notificationsModel.clear());

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createTitledBorder("Notificaciones"));

        panel.add(new JScrollPane(notificationsList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(markReadButton);
        buttonPanel.add(clearButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
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

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        messageField = new JTextField();

        JButton sendButton = new JButton("Enviar");

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        panel.add(chatTitleLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
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

        if (chatArea != null) {
            chatArea.setText("");
            chatArea.append("Sistema: Abriste " + selectedChat + "\n");
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

        chatArea.append("Yo: " + text + "\n");
        messageField.setText("");

        notificationsModel.addElement("Mensaje enviado en " + selectedChat);
    }

    private String getNameOnly(String text) {
        if (text.contains("-")) {
            return text.substring(0, text.indexOf("-")).trim();
        }

        return text.trim();
    }

    private void showCard(String card) {
        cardLayout.show(contentPanel, card);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
