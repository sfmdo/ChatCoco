package Network;

import Messages.MessagePacket;
import Messages.Comprimir; 
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

public class ServerConnection implements Runnable {
    private static final Logger LOGGER = System.getLogger(ServerConnection.class.getName());
    private static final Gson GSON = new GsonBuilder().create();
    
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private final Comprimir compresor = new Comprimir();
    private volatile boolean isRunning = false;
    
    public ServerConnection(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.isRunning = true;
    }

    @Override
    public void run() {
        try {
            String lineaRecibida;
            while (isRunning && (lineaRecibida = reader.readLine()) != null) {
                processIncomingLine(lineaRecibida);
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Conexión cerrada por el servidor.");
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "Error en el bucle de recepción: {0}", e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void processIncomingLine(String linea) {
        try {
            byte[] data = Base64.getDecoder().decode(linea);
            char[] comprimido = bytesToChars(data);
            String json = Comprimir.descomprimir(comprimido);
            MessagePacket packet = GSON.fromJson(json, MessagePacket.class);
            
            if (packet != null) {
                LOGGER.log(Level.INFO, "Paquete recibido: {0}", packet.getAction());
                ClientRouter.route(packet);
            }
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "Error al procesar paquete entrante: {0}", e.getMessage());
        }
    }

    public synchronized void sendPacket(MessagePacket packet) {
        try {
            String json = GSON.toJson(packet);
            char[] tokens = compresor.compresion(json);
            byte[] bytes = charsToBytes(tokens);
            String base64 = Base64.getEncoder().encodeToString(bytes);

            writer.println(base64);
        } catch (Exception e) {
            LOGGER.log(Level.ERROR, "Error al enviar paquete: {0}", e.getMessage());
        }
    }

    public void disconnect() {
        isRunning = false;
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && !socket.isClosed()) socket.close();
            LOGGER.log(Level.INFO, "Desconectado del servidor.");
        } catch (IOException e) {
            LOGGER.log(Level.ERROR, "Error al desconectar: {0}", e.getMessage());
        }
    }

    private byte[] charsToBytes(char[] chars) {
        ByteBuffer bb = ByteBuffer.allocate(chars.length * 2);
        for (char c : chars) bb.putChar(c);
        return bb.array();
    }

    private char[] bytesToChars(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        char[] chars = new char[bytes.length / 2];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = bb.getChar();
        }
        return chars;
    }
}