import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import javax.swing.JOptionPane;

//TODO STUDENT: edit the class header so that ChatServer can run in a thread
public class ChatServer implements Runnable {
    private String name;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    ChatServer(int port) {
        CHAT_ROOM_PORT = port;
    }

    private static int CHAT_ROOM_PORT;

    private static HashSet<String> names = new HashSet<String>();

    private static HashSet<PrintWriter> writers = new HashSet<PrintWriter>();

    public void run() {
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(CHAT_ROOM_PORT);
            System.out.println("The chat server is running.");

            while (true) {
                clientSocket = listener.accept();
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                name = null;
                while (name == null) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name.equals("")) name = null;
                    else if (name.equals("null")) name = null;
                    else if (names.contains(name)) {
                        out.println("WRONGNAME");
                        Thread.sleep(100);
                        name = null;
                    }
                    else names.add(name);
                }

                out.println("NAMEACCEPTED");
                writers.add(out);
                ServerThreadForClient svrForClient = new ServerThreadForClient(in, out, name);

                Thread t = new Thread(svrForClient);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ServerThreadForClient implements Runnable {
        BufferedReader in;
        PrintWriter out;
        String name;

        ServerThreadForClient(BufferedReader in, PrintWriter out, String name) {
            this.in = in;
            this.out = out;
            this.name = name;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String input;
                    try {
                        input = in.readLine();

                        if (input == null) {
                            return;
                        }
                        for (PrintWriter writer : writers) {
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            finally {
                if (name != null) {
                    names.remove(name);
                }
                if (out != null) {
                    writers.remove(out);
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
