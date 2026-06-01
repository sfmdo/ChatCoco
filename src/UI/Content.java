package UI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class Content extends JFrame {
    private static final String CARD_USERS = "USERS";
    private static final String CARD_FRIENDS = "FRIENDS";
    private static final String CARD_GROUPS = "GROUPS";

    private JPanel headerPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public Content() {
        super("ChatCoco");
        init();
    }

    public void init() {
        setTitle("ChatCoco");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        headerPanel.setBackground(new Color(245, 245, 245));

        JButton usersButton = createHeaderButton("Usuarios", CARD_USERS);
        JButton friendsButton = createHeaderButton("Amigos", CARD_FRIENDS);
        JButton groupsButton = createHeaderButton("Grupos", CARD_GROUPS);

        headerPanel.add(usersButton);
        headerPanel.add(friendsButton);
        headerPanel.add(groupsButton);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        contentPanel.add(createUsersPanel(), CARD_USERS);
        contentPanel.add(createFriendsPanel(), CARD_FRIENDS);
        contentPanel.add(createGroupsPanel(), CARD_GROUPS);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);

        showCard(CARD_USERS);
    }

    private JButton createHeaderButton(String text, String cardName) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 32));
        button.addActionListener(e -> showCard(cardName));
        return button;
    }

    private JPanel createUsersPanel() {
        JPanel panel = createPanelWithTitle("Usuarios");
        panel.add(createUserCard("Oscar", true));
        panel.add(createUserCard("Lucia", false));
        panel.add(createMessageCard("Bienvenido a ChatCoco", "01/06/2026"));
        return wrapInScroll(panel);
    }

    private JPanel createFriendsPanel() {
        JPanel panel = createPanelWithTitle("Amigos");
        panel.add(createUserCard("Amigo 1", true));
        panel.add(createUserCard("Amigo 2", true));
        panel.add(createMessageCard("Chat con amigos activado", "01/06/2026"));
        return wrapInScroll(panel);
    }

    private JPanel createGroupsPanel() {
        JPanel panel = createPanelWithTitle("Grupos");
        panel.add(createGroupCard("Grupo de Estudio", "Miembros: 12"));
        panel.add(createGroupCard("Grupo de Trabajo", "Miembros: 8"));
        panel.add(createMessageCard("Selecciona un grupo para ver mensajes", "01/06/2026"));
        return wrapInScroll(panel);
    }

    private JPanel createPanelWithTitle(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private JPanel wrapInScroll(JPanel panel) {
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    public void CrearVisualUsuario(String nombre, boolean estado) {
        JPanel userPanel = createUserCard(nombre, estado);
        contentPanel.add(userPanel, CARD_USERS);
    }

    public void crearVisualMensaje(String mensaje, String fecha) {
        JPanel messagePanel = createMessageCard(mensaje, fecha);
        contentPanel.add(messagePanel, CARD_USERS);
    }

    public void visualGrupos() {
        showCard(CARD_GROUPS);
    }

    public void visualAmigos() {
        showCard(CARD_FRIENDS);
    }

    private JPanel createUserCard(String nombre, boolean online) {
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        userPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        userPanel.setBackground(new Color(250, 250, 250));

        JLabel nameLabel = new JLabel("Nombre: " + nombre);
        JLabel statusLabel = new JLabel("Estado: " + (online ? "En línea" : "Desconectado"));
        userPanel.add(nameLabel);
        userPanel.add(statusLabel);

        return userPanel;
    }

    private JPanel createGroupCard(String nombre, String descripcion) {
        JPanel groupPanel = new JPanel();
        groupPanel.setLayout(new BoxLayout(groupPanel, BoxLayout.Y_AXIS));
        groupPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        groupPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        groupPanel.setBackground(new Color(250, 250, 250));

        JLabel nameLabel = new JLabel("Grupo: " + nombre);
        JLabel descLabel = new JLabel(descripcion);
        groupPanel.add(nameLabel);
        groupPanel.add(descLabel);

        return groupPanel;
    }

    private JPanel createMessageCard(String mensaje, String fecha) {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        messagePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        messagePanel.setBackground(new Color(250, 250, 250));

        JLabel messageLabel = new JLabel(mensaje);
        JLabel dateLabel = new JLabel("Fecha: " + fecha);
        messagePanel.add(messageLabel);
        messagePanel.add(dateLabel);

        return messagePanel;
    }

    private void showCard(String card) {
        cardLayout.show(contentPanel, card);
    }
}
