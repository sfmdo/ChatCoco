package UI;

import ClientServices.*;
import Network.Handlers;
import Network.ServerConnection;
import UI.Components.ChatPanel;

import Models.User;
import UI.Views.*;

import java.util.List; 
import javax.swing.*;
import java.awt.*;

public class Content extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private ChatPanel sharedChatPanel;
    
    private UsersView usersView;
    private NotificationView notificationView;
    private FriendsView friendsView;
    private GroupsView groupsView;

    private AuthClientService authService;
    private ChatGlobalClientService globalChatService;
    private FriendClientService friendService;
    private UserClientService userService;
    private NotificationClientService notificationService;
    private GroupClientService groupService;
    
    public Content(ServerConnection conn) {
        super("ChatCoco - Global");
        
    
        this.authService         = Handlers.getInstance().getAuthService();
        this.globalChatService   = Handlers.getInstance().getChatGlobalService();
        this.friendService       = Handlers.getInstance().getFriendService();
        this.userService         = Handlers.getInstance().getUserService();
        this.notificationService = Handlers.getInstance().getNotificationService();
        this.groupService        = Handlers.getInstance().getGroupService();
        
        Handlers.getInstance().setMainWindow(this);
        
        init();
    }

    private void init() {
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));
        
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(new Color(45, 45, 45)); 
        
        JPanel leftNav = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        leftNav.setOpaque(false);
        
        JButton btnGlobal = createNavButton("Chat Global", "USERS");
        JButton btnFriends = createNavButton("Amigos", "FRIENDS");
        JButton btnGroups = createNavButton("Grupos", "GROUP");
        JButton btnNotif = createNavButton("Notificaciones", "NOTIFICATIONS");
        
        leftNav.add(btnGlobal);
        leftNav.add(btnFriends);
        leftNav.add(btnGroups);
        leftNav.add(btnNotif);
        
        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        rightNav.setOpaque(false);
    
        JButton btnLogout = new JButton("Cerrar Sesión");
        styleLogoutButton(btnLogout);
    
        btnLogout.addActionListener(e -> performLogout());
        rightNav.add(btnLogout);

        navBar.add(leftNav, BorderLayout.WEST);
        navBar.add(rightNav, BorderLayout.EAST);
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        sharedChatPanel = new ChatPanel();
        sharedChatPanel.setServices(globalChatService, friendService, groupService); 
        sharedChatPanel.setPreferredSize(new Dimension(500, 0));

        usersView = new UsersView(globalChatService, friendService, sharedChatPanel, userService);
        usersView.getUsersList().setCellRenderer(universalRenderer);
        notificationView = new NotificationView(notificationService, friendService, groupService, authService);
        friendsView = new FriendsView(friendService, sharedChatPanel);
        friendsView.getFriendsList().setCellRenderer(universalRenderer);
        groupsView = new GroupsView(groupService, sharedChatPanel, friendService);
        
        mainContainer.add(usersView, "USERS");
        mainContainer.add(notificationView, "NOTIFICATIONS");
        mainContainer.add(friendsView, "FRIENDS");
        mainContainer.add(groupsView, "GROUP");

        add(navBar, BorderLayout.NORTH);
        add(mainContainer, BorderLayout.CENTER);
        add(sharedChatPanel, BorderLayout.EAST);
    }


    public void updateUsersList(List<User> users) {
        SwingUtilities.invokeLater(() -> {
            DefaultListModel<User> model = usersView.getModel();
            model.clear();
            for (User u : users) {
                model.addElement(u);
            }
        });
    }
    
    public void updateFriendsList(List<User> friends) {
        SwingUtilities.invokeLater(() -> {
            DefaultListModel<User> friendsTabModel = friendsView.getModel();
            friendsTabModel.clear();
        
            DefaultListModel<User> groupsInviteModel = groupsView.getFriendsModel();
            groupsInviteModel.clear();
    
            if (friends != null) {
                for (User f : friends) {
                    friendsTabModel.addElement(f);
                    groupsInviteModel.addElement(f);
                }
            }
        
        });
    }
    
    public <T> void updateUIList(DefaultListModel<T> model, java.util.List<T> data) {
        if (model == null) return;
    
        SwingUtilities.invokeLater(() -> {
            model.clear();
            if (data != null) {
                for (T item : data) {
                    model.addElement(item);
                }
            }
        });
    }
    
    

    public ChatPanel getChatPanel() { return sharedChatPanel; }
    public UsersView getUsersView() { return usersView; }
    public NotificationView getNotificationView() { return notificationView; }
    public FriendsView getFriendsView() { return friendsView; }
    public GroupsView getGroupsView() { return groupsView; }
    public AuthClientService getAuthService() { return authService; }
    public UserClientService getUserService() { return userService; }
    public ChatGlobalClientService getChatGlobalService() { return globalChatService; }
    public NotificationClientService getNotificationService() { return notificationService; }
    public FriendClientService getFriendService() { return friendService; }
    public GroupClientService getGroupService() { return groupService; }
    
    private JButton createNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
    
        btn.setPreferredSize(new Dimension(160, 35));
        btn.setFocusPainted(false); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
    
        btn.setBackground(new Color(60, 60, 60));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));


        btn.addActionListener(e -> {
            cardLayout.show(mainContainer, cardName);
        });

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(80, 80, 80));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(60, 60, 60));
            }
        });

        return btn;
    }
    
    private final ListCellRenderer<Object> universalRenderer = new DefaultListCellRenderer() {
    @Override
     public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof Models.User u) {
                setText(u.getUsername() + " [" + u.getStatus() + "]");

                if ("ONLINE".equalsIgnoreCase(u.getStatus())) {
                    setForeground(new Color(0, 120, 0)); 
                } else {
                    setForeground(Color.GRAY);
                }
            } 
            else if (value instanceof Models.Group) {
                Models.Group g = (Models.Group) value;
             
                setText("👥 " + g.getGroupName()); 
                setForeground(Color.BLACK);
            }

            return this;
        }
    };
    
    private void styleLogoutButton(JButton btn) {
        btn.setPreferredSize(new Dimension(130, 35));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBackground(new Color(150, 40, 40)); 
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setBorder(BorderFactory.createLineBorder(new Color(100, 30, 30)));
    }

private void performLogout() {
    int confirm = JOptionPane.showConfirmDialog(this, 
        "¿Estás seguro de que deseas cerrar sesión?", 
        "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
        
    if (confirm == JOptionPane.YES_OPTION) {
        authService.logout();
        this.dispose();
            Handlers.getInstance().setMainWindow(null);
        }
    }
    
}