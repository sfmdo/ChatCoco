package Network;

import Messages.MessagePacket;
import Messages.Comprimir; 
import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;


//Esta clase corre en el CLIENTE y gestiona la comunicación con el Servidor.
public class ServerConnection implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Gson gson = new Gson();
    private Comprimir compresor = new Comprimir();
    private boolean isRunning = false;
    
    private static final Logger LOGGER = System.getLogger(ServerConnection.class.getName());

    // Interfaz para que tu UI (Swing/JavaFX) se entere de los mensajes
    public interface OnMessageReceived {
        void onIncomingPacket(MessagePacket packet);
    }
    private OnMessageReceived callback;

    //Constructor para conectar al servidor.

    public ServerConnection(String host, int port, OnMessageReceived callback) throws IOException {
        this.socket = new Socket(host, port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.callback = callback;
        this.isRunning = true;
    }

    @Override
    public void run() {
        try {
            String lineaRecibida;
            // El cliente se queda escuchando eventos del servidor (Mensajes, Notificaciones, etc.)
            while (isRunning && (lineaRecibida = reader.readLine()) != null) {
                // 1. Decodificar y Descomprimir
                byte[] dataData = Base64.getDecoder().decode(lineaRecibida);
                char[] tokensComprimidos = bytesToChars(dataData);
                String jsonDescomprimido = Comprimir.descomprimir(tokensComprimidos);

                // 2. Convertir JSON a Objeto
                MessagePacket packet = gson.fromJson(jsonDescomprimido, MessagePacket.class);
                    
                ClientRouter.route(packet); 
                // 3. Entregar el paquete a la UI o al controlador
                if (callback != null) {
                    callback.onIncomingPacket(packet);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "Conexión con el servidor perdida.");
        } finally {
            disconnect();
        }
    }

    //Envía un paquete al servidor siguiendo el pipeline:
    //Objeto -> JSON -> Comprimir -> Base64

    public synchronized void sendPacket(MessagePacket packet) {
        try {
            String json = gson.toJson(packet);
            char[] tokens = compresor.compresion(json);
            byte[] bytesParaEnviar = charsToBytes(tokens);
            String stringBase64 = Base64.getEncoder().encodeToString(bytesParaEnviar);

            writer.println(stringBase64);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "Error al enviar paquete al servidor", e);
        }
    }

    public void disconnect() {
        isRunning = false;
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- Métodos Auxiliares de Conversión ---

    private byte[] charsToBytes(char[] chars) {
        byte[] b = new byte[chars.length * 2];
        for (int i = 0; i < chars.length; i++) {
            b[i * 2] = (byte) (chars[i] >> 8);
            b[i * 2 + 1] = (byte) (chars[i]);
        }
        return b;
    }

    private char[] bytesToChars(byte[] bytes) {
        char[] c = new char[bytes.length / 2];
        for (int i = 0; i < c.length; i++) {
            c[i] = (char) ((bytes[i * 2] << 8) | (bytes[i * 2 + 1] & 0xFF));
        }
        return c;
    }
}