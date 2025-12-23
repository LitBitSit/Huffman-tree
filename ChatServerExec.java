import java.io.PrintWriter;
import java.util.HashSet;

public class ChatServerExec {

    public ChatServerExec(int port) {
        CHAT_ROOM_PORT = port;
    }

    private static int CHAT_ROOM_PORT;

    public void startServer() {
        ChatServer server = new ChatServer(CHAT_ROOM_PORT);
        Thread t = new Thread(server);
        t.start();
    }
}
