package UI.Components;

import ClientServices.ChatGlobalClientService;
import ClientServices.FriendClientService;
import ClientServices.GroupClientService;
import Models.GlobalMessage;
import java.util.List;
import javax.swing.*;
import java.awt.*;

public class ChatPanel extends JPanel {
    private JPanel messagesContainer;
    private JScrollPane scrollPane;
    private JTextField messageField;
    private JButton sendButton;
    private JLabel titleLabel;

    // Estado del chat actual
    private String currentContext = "NONE"; 
    private int targetId = -1;
    private String targetName = "";

    // Servicios inyectados
    private ChatGlobalClientService globalChatService;
    private FriendClientService friendService;
    private GroupClientService groupService;

    public ChatPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("Conversación"));
        initComponents();
    }

    public void setServices(ChatGlobalClientService gc, FriendClientService fs, GroupClientService gs) {
        this.globalChatService = gc;
        this.friendService = fs;
        this.groupService = gs;
    }

    public String getCurrentContext() {
        return this.currentContext; // "GLOBAL", "FRIEND", "GROUP"
    }
    
    private void initComponents() {
        titleLabel = new JLabel("Seleccione un chat para empezar");
        messagesContainer = new JPanel();
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(messagesContainer);
        messageField = new JTextField();
        sendButton = new JButton("Enviar");

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(messageField, BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);

        add(titleLabel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public void setTargetContext(String type, int id, String name) {
        this.currentContext = type;
        this.targetId = id;
        this.targetName = name;
        this.titleLabel.setText(type + ": " + name);
    }
    
    public int getCurrentTargetId() {
        return this.targetId;
    }
    
    public void loadFriendHistory(List<Models.PrivateMessages> history, int myUserId) {
        SwingUtilities.invokeLater(() -> {
            clearMessages(); // Limpiar el chat actual
        
            if (history == null || history.isEmpty()) {
                addBubble("Sistema", "No hay mensajes previos en esta conversación.", false);
                return;
            }

            for (Models.PrivateMessages msg : history) {
                boolean isSelf = (msg.getSenderId() == myUserId);
            
                String displayName = isSelf ? "Yo" : targetName;
            
                addBubble(displayName, msg.getMessage(), isSelf);
            }
        });
    }
    
    public void loadHistory(List<GlobalMessage> history, int myUserId) {
        SwingUtilities.invokeLater(() -> {
            clearMessages();
            for (GlobalMessage msg : history) {
                boolean isSelf = String.valueOf(myUserId).equals(msg.getFromId());
                String senderName = isSelf ? "Yo" : targetName;
                addBubble(senderName, msg.getText(), isSelf);
            }
        });
    }
    
    public void loadGroupHistory(java.util.List<Models.GroupMessages> history, int myUserId) {
        SwingUtilities.invokeLater(() -> {
            clearMessages(); 
        
            if (history == null || history.isEmpty()) {
                addBubble("Sistema", "Este es el inicio de la conversación del grupo.", false);
                return;
            }

            for (Models.GroupMessages msg : history) {
                boolean isSelf = (msg.getSenderId() == myUserId);
                String displayName = isSelf ? "Yo" : "Usuario " + msg.getSenderId();
            
                addBubble(displayName, msg.getMessage(), isSelf);
            }
        });
    }

    public void receiveMessage(int fromId, String text) {
        if (this.targetId == fromId) {
            addBubble(targetName, text, false);
        }
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty() || targetId == -1) return;

        switch (currentContext) {
            case "GLOBAL":
                globalChatService.sendGlobalMessage(targetId, text);
                break;
            case "FRIEND":
                friendService.sendPrivateMessage(targetId, text);
                break;
            case "GROUP":
                groupService.sendGroupMessage(targetId, text);
                break;
        }
        
        addBubble("Yo", text, true);
        messageField.setText("");
    }

    public void clearMessages() {
        messagesContainer.removeAll();
        messagesContainer.revalidate();
        messagesContainer.repaint();
    }
    
    public void addBubble(String sender, String text, boolean isSelf) {
        JPanel wrapper = new JPanel(new FlowLayout(isSelf ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 1)) {
            @Override
            public Dimension getMaximumSize() {
                Dimension pref = getPreferredSize();
                return new Dimension(Integer.MAX_VALUE, pref.height);
            }
        };
        wrapper.setOpaque(false);


        JLabel senderLabel = new JLabel(isSelf ? "Yo" : sender);
        senderLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        senderLabel.setForeground(Color.GRAY);

        JPanel bubble = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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
        if (scrollPane != null) {
            scrollPane.revalidate();
            scrollPane.repaint();
        }

        SwingUtilities.invokeLater(() -> {
            if (scrollPane != null) {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }
}