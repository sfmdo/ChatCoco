/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Messages;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sfmdo
 */
public class MessagePacket {
    private String type;
    private String action;
    private String token;
    private Map<String, Object> payload = new HashMap<>();;
    
    public String getParam(String key) {
        Object value = payload.get(key);
        return (value != null) ? value.toString() : null;
    }
    
    public Integer getIntParam(String key) {
        if (payload == null) return -1;
        Object value = payload.get(key);
        if (value == null) return -1;
    
        // Si ya es un número (Gson lo hace Double o Integer)
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
    
        // Si viene como String (importante para evitar errores)
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
    
    public MessagePacket add(String key, Object value) {
        this.payload.put(key, value);
        return this;                  
    }
    
    public static MessagePacket response(String action, String token) {
        MessagePacket p = new MessagePacket();
        p.setType("RESPONSE");
        p.setAction(action);
        p.setToken(token);
        return p;
    }
    
    public static MessagePacket event(String action) {
        MessagePacket p = new MessagePacket();
        p.setType("EVENT");
        p.setAction(action);
        return p;
    }
    
    public static MessagePacket request(String action) {
        MessagePacket p = new MessagePacket();
        p.setType("REQUEST");
        p.setAction(action);
        return p;
    }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getToken() { return token; }
    public MessagePacket setToken(String token) { 
    this.token = token; 
    return this; 
    }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
    
}
