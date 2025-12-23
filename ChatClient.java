import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import javax.swing.JOptionPane;

/**
 * A single chat client window.
 * It connects to the server, negotiates a unique screen name,
 * then sends/receives messages using the simple text protocol.
 */
public class ChatClient implements Runnable, ChatClientInterface {

    private final int CHAT_ROOM_PORT;

    private BufferedReader in;
    private PrintWriter out;

    private final BorderPane frame = new BorderPane();
    private final TextField textField = new TextField();
    private final TextArea messageArea = new TextArea();
    private final Stage stage;

    private String myScreenName = "";

    public ChatClient(int port) {
        this.CHAT_ROOM_PORT = port;

        stage = new Stage();
        stage.setScene(new Scene(frame, 500, 200));
        stage.setX(ChatClientExec.getClientX());
        stage.setY(ChatClientExec.getClientY());
        stage.setTitle("Chat Client");
        stage.show();

        // User can't type until the server accepts the name
        textField.setEditable(false);
        messageArea.setEditable(false);

        frame.setTop(textField);
        frame.setCenter(messageArea);

        // When user hits Enter, send the message
        textField.setOnAction(event -> {
            if (out != null) {
                out.println(textField.getText());
                textField.setText("");
            }
        });

        // Close cleanly if window is closed
        stage.setOnCloseRequest(e -> {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
            } catch (IOException ignored) { }
        });
    }

    private String getServerAddress() {
        // Lab assumes everything runs on the same machine
        return "localhost";
    }

    @Override
    public String getName() {
        return JOptionPane.showInputDialog(
                null,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(getServerAddress(), CHAT_ROOM_PORT)) {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            while (true) {
                String line = in.readLine();
                if (line == null) break;

                if (line.startsWith("SUBMITNAME")) {
                    myScreenName = getName();
                    if (myScreenName == null) myScreenName = "";
                    out.println(myScreenName);

                } else if (line.startsWith("NAMEACCEPTED")) {
                    Platform.runLater(() -> textField.setEditable(true));

                } else if (line.startsWith("WRONGNAME")) {
                    JOptionPane.showMessageDialog(null,
                            "Screen Name "" + myScreenName + "" is already in use. Pick another one.");

                } else if (line.startsWith("MESSAGE")) {
                    final String msg = line.substring(8);
                    Platform.runLater(() -> messageArea.appendText(msg + "\n"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getServerPort() {
        return CHAT_ROOM_PORT;
    }
}
