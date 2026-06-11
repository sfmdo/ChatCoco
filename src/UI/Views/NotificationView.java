package UI.Views;

import ClientServices.*;
import Models.Notifications;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NotificationView extends JPanel {
    private JPanel gridPanel;
    private NotificationClientService notifService;
    private FriendClientService friendService;
    private GroupClientService groupService;
    private AuthClientService authService;

    public NotificationView(NotificationClientService ns, FriendClientService fs, GroupClientService gs, AuthClientService as) {
        this.notifService = ns;
        this.friendService = fs;
        this.groupService = gs;
        this.authService = as;
        
        setLayout(new BorderLayout());
        JButton btnRefresh = new JButton("Actualizar Bandeja");
        btnRefresh.addActionListener(e -> notifService.fetchNotifications());

        gridPanel = new JPanel();
        gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));
        gridPanel.setBackground(new Color(230, 230, 230));

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);

        add(btnRefresh, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void updateList(List<Notifications> list) {
        SwingUtilities.invokeLater(() -> {
            gridPanel.removeAll(); 
            if (list == null || list.isEmpty()) {
                JLabel empty = new JLabel("No tienes notificaciones pendientes.");
                empty.setAlignmentX(Component.CENTER_ALIGNMENT);
                gridPanel.add(empty);
            } else {
                for (Notifications n : list) {
                    gridPanel.add(createCard(n));
                    gridPanel.add(Box.createVerticalStrut(10));
                }
            }
            gridPanel.revalidate();
            gridPanel.repaint();
        });
    }

    private JPanel createCard(Notifications n) {
        JPanel card = new JPanel(new BorderLayout(15, 10));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(500, 100));

        String titulo = n.getType().replace("_", " ");
        titulo += " DE " + n.getFrom_user_id();
        String contenido = n.getContent() != null ? n.getContent() : "Sin descripción";
        
        JLabel infoLabel = new JLabel("<html>" +
            "<body style='color: #222222; font-family: sans-serif;'>" +
            "<b style='font-size: 11px; color: #0056b3;'>" + titulo + "</b><br>" +
            "<span style='font-size: 10px;'>" + contenido + "</span>" +
            "</body></html>");
        
        card.add(infoLabel, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        if (n.getType().equals("FRIEND_REQUEST") || n.getType().equals("GROUP_INVITE")) {
            JButton btnAccept = new JButton("Aceptar");
            btnAccept.setBackground(new Color(40, 167, 69)); // Verde
            btnAccept.setForeground(Color.WHITE);
            
            btnAccept.addActionListener(e -> {
                if (n.getType().equals("FRIEND_REQUEST")) {
                    int miId = Integer.parseInt(authService.getMyId());
                    int requesterId = (n.getFrom_user_id() != miId) ? n.getFrom_user_id() : n.getTarget_user_id();
                    friendService.acceptFriendRequest(requesterId);
                } else {
                    groupService.acceptGroupInvitation(n.getRelated_id());
                }
                removeCard(card); 
            });

            JButton btnDecline = new JButton("Rechazar");
            btnDecline.setBackground(new Color(220, 53, 69)); // Rojo
            btnDecline.setForeground(Color.WHITE);
            
            btnDecline.addActionListener(e -> {
                if (n.getType().equals("FRIEND_REQUEST")) {
                    // related_id contiene el ID de la relación de amistad (friendship_id)
                    friendService.declineFriendRequest(n.getRelated_id());
                } else {
                    // related_id contiene el ID del grupo (group_id)
                    groupService.declineGroupInvitation(n.getRelated_id());
                }
                removeCard(card);
            });

            actions.add(btnAccept);
            actions.add(btnDecline);
        } else {
            JButton btnOk = new JButton("Entendido");
            btnOk.addActionListener(e -> removeCard(card));
            actions.add(btnOk);
        }

        card.add(actions, BorderLayout.EAST);
        return card;
    }

    private void removeCard(JPanel card) {
        gridPanel.remove(card);
        gridPanel.revalidate();
        gridPanel.repaint();
    }
}