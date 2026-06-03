package UI;

import ClientServices.*;
import Network.Handlers;
import Network.ServerConnection;
import UI.Components.ChatPanel;

import Models.User;
import UI.Views.*;

import java.util.List; // IMPORTANTE: Usa este para las listas
import javax.swing.*;
import java.awt.*;

public class Content extends JFrame {
    // Componentes principales
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private ChatPanel sharedChatPanel;
    
    private UsersView usersView;
    private NotificationView notificationView;
    private FriendsView friendsView;
    private GroupsView groupsView;

    // Servicios necesarios para esta fase
    private AuthClientService authService;
    private ChatGlobalClientService globalChatService;
    private FriendClientService friendService;
    private UserClientService userService;
    private NotificationClientService notificationService;
    private GroupClientService groupService;
    
    public Content(ServerConnection conn) {
        super("ChatCoco - Global");
        
        // 1. Inicializar Servicios (Los necesitamos para pasarlos a las vistas)
        this.authService = new AuthClientService(conn);
        this.globalChatService = new ChatGlobalClientService(conn);
        this.friendService = new FriendClientService(conn);
        this.userService = new UserClientService(conn);
        this.notificationService = new NotificationClientService(conn);
        this.groupService = new GroupClientService(conn);
        
        // 2. Registrar en Handlers para que el servidor pueda "hablarle" a esta ventana
        Handlers.getInstance().setMainWindow(this);
        
        init();
    }

    private void init() {
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));
        
        JPanel navBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        navBar.setBackground(new Color(45, 45, 45)); // Oscuro elegante
        
        JButton btnGlobal = createNavButton("Chat Global", "USERS");
        JButton btnFriends = createNavButton("Amigos", "FRIENDS");
        JButton btnGroups = createNavButton("Grupos", "GROUP");
        JButton btnNotif = createNavButton("Notificaciones", "NOTIFICATIONS");
        
        navBar.add(btnGlobal);
        navBar.add(btnFriends);
        navBar.add(btnGroups);
        navBar.add(btnNotif);

        // 2. Contenedor Principal (CardLayout)
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        // --- PARTE DERECHA: PANEL DE CHAT COMPARTIDO ---
        sharedChatPanel = new ChatPanel();
        // Inyectamos los servicios al panel de chat para que el botón "Enviar" funcione
        sharedChatPanel.setServices(globalChatService, friendService, groupService); 
        sharedChatPanel.setPreferredSize(new Dimension(500, 0));

        // --- PARTE IZQUIERDA: VISTA DE USUARIOS ---
        // Pasamos servicios y el panel de chat para que UsersView pueda mandar comandos
        usersView = new UsersView(globalChatService, friendService, sharedChatPanel, userService);
        usersView.getUsersList().setCellRenderer(universalRenderer);
        notificationView = new NotificationView(notificationService, friendService, groupService, authService);
        friendsView = new FriendsView(friendService, sharedChatPanel);
        friendsView.getFriendsList().setCellRenderer(universalRenderer);
        groupsView = new GroupsView(groupService, sharedChatPanel, friendService);
        groupsView.getGroupsList().setCellRenderer(universalRenderer);
        groupsView.getFriendsList().setCellRenderer(universalRenderer);
        
        mainContainer.add(usersView, "USERS");
        mainContainer.add(notificationView, "NOTIFICATIONS");
        mainContainer.add(friendsView, "FRIENDS");
        mainContainer.add(groupsView, "GROUP");

        // Ensamblado
        add(navBar, BorderLayout.NORTH);
        add(mainContainer, BorderLayout.CENTER);
        add(sharedChatPanel, BorderLayout.EAST);
    }

    // --- MÉTODOS PARA ACTUALIZACIÓN DESDE HANDLERS ---

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
            // 1. Actualizar la pestaña de AMIGOS
            DefaultListModel<User> friendsTabModel = friendsView.getModel();
            friendsTabModel.clear();
        
            // 2. Actualizar la lista de invitación en la pestaña de GRUPOS
            DefaultListModel<User> groupsInviteModel = groupsView.getFriendsModel();
            groupsInviteModel.clear();
    
            if (friends != null) {
                for (User f : friends) {
                    // Añadimos al amigo a ambos modelos
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
    
    

    // Getters para que Handlers acceda a los componentes
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
    
        // --- ESTILO DEL BOTÓN ---
        btn.setPreferredSize(new Dimension(160, 35));
        btn.setFocusPainted(false); // Quita el borde de foco al hacer clic
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cambia el mouse a una "manita"
    
        // Colores oscuros para combinar con el navBar (opcional)
        btn.setBackground(new Color(60, 60, 60));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));

        // --- ACCIÓN ---
        // Al presionar el botón, el CardLayout muestra la vista solicitada
        btn.addActionListener(e -> {
            cardLayout.show(mainContainer, cardName);
        
            // Efecto visual: resetear bordes de otros botones (opcional)
            // System.out.println("Cambiando a la vista: " + cardName);
        });

        // Efecto Hover (Cambio de color al pasar el mouse)
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
    
    // --- RENDERER DE METADATA ---
    private final ListCellRenderer<Object> universalRenderer = new DefaultListCellRenderer() {
    @Override
     public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // Dejamos que la superclase gestione el fondo azul cuando se selecciona
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            // CASO 1: Es un objeto Usuario (Pestaña Global o Amigos)
            if (value instanceof Models.User u) {
                setText(u.getUsername() + " [" + u.getStatus() + "]");
            
                // Color verde si está online, gris si no
                if ("ONLINE".equalsIgnoreCase(u.getStatus())) {
                    setForeground(new Color(0, 120, 0)); 
                } else {
                    setForeground(Color.GRAY);
                }
            } 
            // CASO 2: Es un objeto Grupo (Pestaña Grupos)
            else if (value instanceof Models.Group) {
                Models.Group g = (Models.Group) value;
                // Podemos poner un emoji o icono para diferenciar
                setText("👥 " + g.getGroupName()); 
                setForeground(Color.BLACK);
            }

            return this;
        }
    };
    
    
}