package UI.Views;

import ClientServices.FriendClientService;
import ClientServices.GroupClientService;
import Models.Group;
import Models.User;
import UI.Components.ChatPanel;
import javax.swing.*;
import java.awt.*;

public class GroupsView extends JPanel {
    private DefaultListModel<Group> groupsModel;
    private JList<Group> groupsList;
    
    private DefaultListModel<User> friendsModel; 
    private JList<User> friendsList;

    private GroupClientService groupService;
    private ChatPanel sharedChatPanel;
    private FriendClientService friendsService;

    public GroupsView(GroupClientService service, ChatPanel sharedChat, FriendClientService friendsService) {
        this.groupService = service;
        this.sharedChatPanel = sharedChat;
        this.friendsService = friendsService;
        setLayout(new BorderLayout(10, 10));
        initComponents();
    }

    private void initComponents() {
        // --- 1. PANEL DE GRUPOS (ARRIBA) ---
        groupsModel = new DefaultListModel<>();
        groupsList = new JList<>(groupsModel);
        groupsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JButton btnOpenGroup = new JButton("Abrir Chat de Grupo");
        JButton btnCreateGroup = new JButton("Crear Nuevo Grupo");
        JButton btnRefreshGroups = new JButton("Actualizar mis Grupos");
        
        btnRefreshGroups.addActionListener(e -> {
            groupService.fetchMyGroups(); 
            btnRefreshGroups.setEnabled(false);
            new javax.swing.Timer(2000, evt -> btnRefreshGroups.setEnabled(true)).start();
        });

        btnOpenGroup.addActionListener(e -> {
            Group selected = groupsList.getSelectedValue();
            if (selected != null) {
                sharedChatPanel.clearMessages();
                sharedChatPanel.setTargetContext("GROUP", selected.getId(), selected.getGroupName());
                groupService.fetchHistory(selected.getId());
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona un grupo primero.");
            }
        });

        btnCreateGroup.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Nombre del nuevo grupo:");
            if (name != null && !name.trim().isEmpty()) {
                groupService.createGroup(name.trim());
            }
        });

        // --- 2. PANEL DE INVITACIONES (ABAJO) ---
        friendsModel = new DefaultListModel<>();
        friendsList = new JList<>(friendsModel);
        
        JButton btnInvite = new JButton("Invitar Amigo al Grupo");
        JButton btnRefreshFriends = new JButton("Actualizar Lista Amigos");
        
        btnInvite.addActionListener(e -> {
            Group selectedGroup = groupsList.getSelectedValue();
            User selectedFriend = friendsList.getSelectedValue();

            if (selectedGroup == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un Grupo arriba.");
                return;
            }
            if (selectedFriend == null) {
                JOptionPane.showMessageDialog(this, "Selecciona un Amigo abajo.");
                return;
            }

            groupService.sendGroupInvitation(selectedGroup.getId(), selectedFriend.getId());
            JOptionPane.showMessageDialog(this, "Invitación enviada a " + selectedFriend.getUsername());
        });
        
        btnRefreshFriends.addActionListener(e -> {
            friendsService.fetchFriendsList();
            btnRefreshFriends.setEnabled(false);
            new javax.swing.Timer(2000, evt -> btnRefreshFriends.setEnabled(true)).start();
        });

        // --- 3. CONSTRUCCIÓN DE LA INTERFAZ ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setPreferredSize(new Dimension(280, 0));

        // Sub-panel Mis Grupos
        JPanel pnlMyGroups = new JPanel(new BorderLayout(5, 5));
        pnlMyGroups.setBorder(BorderFactory.createTitledBorder("Mis Grupos"));
        pnlMyGroups.add(new JScrollPane(groupsList), BorderLayout.CENTER);
        
        JPanel groupBtns = new JPanel(new GridLayout(3, 1, 2, 2));
        groupBtns.add(btnOpenGroup);
        groupBtns.add(btnCreateGroup);
        groupBtns.add(btnRefreshGroups);
        pnlMyGroups.add(groupBtns, BorderLayout.SOUTH);

        // Sub-panel Invitaciones (CORREGIDO: Usando un panel intermedio para los botones)
        JPanel pnlInvite = new JPanel(new BorderLayout(5, 5));
        pnlInvite.setBorder(BorderFactory.createTitledBorder("Invitar Amigos"));
        pnlInvite.add(new JScrollPane(friendsList), BorderLayout.CENTER);
        
        // Contenedor para los dos botones de abajo
        JPanel inviteBtnContainer = new JPanel(new GridLayout(2, 1, 2, 2));
        inviteBtnContainer.add(btnInvite);
        inviteBtnContainer.add(btnRefreshFriends);
        pnlInvite.add(inviteBtnContainer, BorderLayout.SOUTH);

        leftPanel.add(pnlMyGroups);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(pnlInvite);

        add(leftPanel, BorderLayout.WEST);
    }
    
    // --- GETTERS ---
    public JList<Group> getGroupsList() { return groupsList; }
    public JList<User> getFriendsList() { return friendsList; }
    public DefaultListModel<Group> getGroupsModel() { return groupsModel; }
    public DefaultListModel<User> getFriendsModel() { return friendsModel; }
}