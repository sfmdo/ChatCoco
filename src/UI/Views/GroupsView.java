package UI.Views;

import ClientServices.GroupClientService;
import UI.Components.ChatPanel;
import javax.swing.*;
import java.awt.*;

public class GroupsView extends JPanel {
    private DefaultListModel<String> groupsModel;
    private JList<String> groupsList;
    private GroupClientService groupService;
    private ChatPanel chatPanel;

    public GroupsView(GroupClientService service, ChatPanel sharedChat) {
        this.groupService = service;
        this.chatPanel = sharedChat;
        setLayout(new BorderLayout(10, 10));
        
        // Lógica de lista izquierda
        groupsModel = new DefaultListModel<>();
        groupsList = new JList<>(groupsModel);
        
        JButton btnCreate = new JButton("Crear Grupo");
        btnCreate.addActionListener(e -> {
            String name = JOptionPane.showInputDialog("Nombre del grupo:");
            if(name != null) groupService.createGroup(name);
        });

        // Panel Izquierdo
        JPanel left = new JPanel(new BorderLayout());
        left.add(new JScrollPane(groupsList), BorderLayout.CENTER);
        left.add(btnCreate, BorderLayout.SOUTH);
        left.setPreferredSize(new Dimension(250, 0));

        add(left, BorderLayout.WEST);
        // Aquí no añadimos el chatPanel, lo gestionará el MainFrame
    }
    
    public void updateGroups(java.util.List<String> groups) {
        groupsModel.clear();
        groups.forEach(groupsModel::addElement);
    }
}