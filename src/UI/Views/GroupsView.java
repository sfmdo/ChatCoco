package UI.Views;

import ClientServices.FriendClientService;
import ClientServices.GroupClientService;
import Models.Group;
import Models.User;
import UI.Components.ChatPanel;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class GroupsView extends JPanel {
    private DefaultListModel<Group> groupsModel;
    private JList<Group> groupsList;
    
    private DefaultListModel<User> friendsModel; 
    private JList<User> friendsList;

    private GroupClientService groupService;
    private ChatPanel sharedChatPanel;
    private FriendClientService friendsService;

    // Colores para el tema oscuro
    private final Color DARK_BG = new Color(45, 45, 45);
    private final Color SELECTION_COLOR = new Color(70, 70, 70);

    public GroupsView(GroupClientService service, ChatPanel sharedChat, FriendClientService friendsService) {
        this.groupService = service;
        this.sharedChatPanel = sharedChat;
        this.friendsService = friendsService;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(DARK_BG);
        initComponents();
    }

    private void initComponents() {
        // --- 1. CONFIGURACIÓN DE LISTAS Y MODELOS ---
        groupsModel = new DefaultListModel<>();
        groupsList = new JList<>(groupsModel);
        setupListStyle(groupsList, "GROUP");

        friendsModel = new DefaultListModel<>();
        friendsList = new JList<>(friendsModel);
        setupListStyle(friendsList, "USER");

        // --- 2. BOTONES DE ACCIÓN ---
        JButton btnOpenGroup = createStyledButton("Abrir Chat", new Color(60, 60, 60));
        JButton btnCreateGroup = createStyledButton("Nuevo Grupo", new Color(40, 100, 40));
        JButton btnLeaveGroup = createStyledButton("Salir del Grupo", new Color(150, 40, 40));
        JButton btnInvite = createStyledButton("Enviar Invitación", new Color(40, 60, 150));

        // --- 3. LÓGICA DE BOTONES ---

        // Abrir Chat
        btnOpenGroup.addActionListener(e -> {
            Group selected = groupsList.getSelectedValue();
            if (selected != null) {
                sharedChatPanel.clearMessages();
                sharedChatPanel.setTargetContext("GROUP", selected.getId(), selected.getGroupName());
                groupService.fetchHistory(selected.getId());
            }
        });

        // Crear Grupo
        btnCreateGroup.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Nombre del nuevo grupo:");
            if (name != null && !name.trim().isEmpty()) {
                groupService.createGroup(name.trim());
            }
        });

        // SALIR DEL GRUPO (Reemplaza a los de actualizar)
        btnLeaveGroup.addActionListener(e -> {
            Group selected = groupsList.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "¿Estás seguro de que quieres salir de: " + selected.getGroupName() + "?", 
                    "Confirmar Salida", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    groupService.declineGroupInvitation(selected.getId());
                    // Si el chat abierto es ese, lo cerramos
                    if ("GROUP".equals(sharedChatPanel.getCurrentContext()) && 
                        sharedChatPanel.getCurrentTargetId() == selected.getId()) {
                        sharedChatPanel.clearMessages();
                        sharedChatPanel.setTargetContext("NONE", -1, "");
                    }
                }
            }
        });

        // Invitar
        btnInvite.addActionListener(e -> {
            Group selectedGroup = groupsList.getSelectedValue();
            User selectedFriend = friendsList.getSelectedValue();
            if (selectedGroup != null && selectedFriend != null) {
                groupService.sendGroupInvitation(selectedGroup.getId(), selectedFriend.getId());
                JOptionPane.showMessageDialog(this, "Invitación enviada.");
            }
        });

        // --- 4. DISEÑO DE PANELES (Layout) ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(DARK_BG);
        leftPanel.setPreferredSize(new Dimension(300, 0));

        // Panel de Grupos
        JPanel pnlMyGroups = createSectionPanel("MIS GRUPOS");
        pnlMyGroups.add(new JScrollPane(groupsList), BorderLayout.CENTER);
        JPanel groupBtns = new JPanel(new GridLayout(3, 1, 2, 2));
        groupBtns.setOpaque(false);
        groupBtns.add(btnOpenGroup);
        groupBtns.add(btnCreateGroup);
        groupBtns.add(btnLeaveGroup);
        pnlMyGroups.add(groupBtns, BorderLayout.SOUTH);

        // Panel de Invitar
        JPanel pnlInvite = createSectionPanel("INVITAR AMIGOS");
        pnlInvite.add(new JScrollPane(friendsList), BorderLayout.CENTER);
        pnlInvite.add(btnInvite, BorderLayout.SOUTH);

        leftPanel.add(pnlMyGroups);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(pnlInvite);

        add(leftPanel, BorderLayout.CENTER);
    }

    // --- MÉTODOS DE UTILIDAD PARA UI ---

    private void setupListStyle(JList<?> list, String type) {
        list.setBackground(new Color(35, 35, 35));
        list.setForeground(Color.WHITE);
        list.setSelectionBackground(SELECTION_COLOR);
        list.setSelectionForeground(Color.CYAN);
        list.setFixedCellHeight(30);

        // ESTO SOLUCIONA EL Models.Group@...
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (type.equals("GROUP") && value instanceof Group g) {
                    label.setText("  👥  " + g.getGroupName());
                } else if (type.equals("USER") && value instanceof User u) {
                    label.setText("  👤  " + u.getUsername());
                    if ("ONLINE".equals(u.getStatus())) label.setForeground(new Color(100, 255, 100));
                }
                return label;
            }
        });
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(bg.brighter()));
        return btn;
    }

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBackground(DARK_BG);
        TitledBorder border = BorderFactory.createTitledBorder(title);
        border.setTitleColor(Color.LIGHT_GRAY);
        p.setBorder(border);
        return p;
    }

    // Getters necesarios para el Handlers
    public JList<Group> getGroupsList() { return groupsList; }
    public JList<User> getFriendsList() { return friendsList; }
    public DefaultListModel<Group> getGroupsModel() { return groupsModel; }
    public DefaultListModel<User> getFriendsModel() { return friendsModel; }
}