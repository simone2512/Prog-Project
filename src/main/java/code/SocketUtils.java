package code;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * Utility class for reading from and writing to a socket.
 */
public class SocketUtils {
    protected Socket socket;

    /**
     * Constructs a SocketUtils object.
     */
    public SocketUtils() {
        this.socket = null;
    }

    /**
     * Constructs a SocketUtils object with a socket.
     *
     * @param socket the socket to use
     */
    public SocketUtils(Socket socket) {
        this.socket = socket;
    }

    /**
     * Reads from the socket.
     *
     * @return the received message
     */
    protected String readSocket() {
        try {
            InputStream inStream = socket.getInputStream();
            Scanner in = new Scanner(inStream);
            return in.nextLine();
        } catch (IOException e) {
            throw new RuntimeException("Scanner error", e);
        }
    }

    /**
     * Writes to the socket.
     *
     * @param message the message to write
     */
    protected void writeSocket(String message) {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            writer.println(message);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Writer error", e);
        }
    }
}
