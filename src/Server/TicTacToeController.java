package Server;

import AI.AIPlayer;
import Structure.Message;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class TicTacToeController implements Initializable {
    private int size;
    private String ip;
    private int port;

    public GridPane gridPane;
    private ArrayList<Label> labels;

    private int depth;
    private AIPlayer aiPlayer;
    private int requiredSymbols;

    private String mySymbol;
    private String myColor;

    private String opponentSymbol;
    private String opponentColor;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        labels = new ArrayList<>();
        gridPane.setGridLinesVisible(true);
    }

    public void loadConnect(int size, String ip, int port) {
        load(size, ip, port);

        Thread thread = new Thread(() -> {
            try {
                logicClient();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }

    public void loadHost(int size, String ip, int port) {
        load(size, ip, port);

        Thread thread = new Thread(() -> {
            try {
                logicServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }

    private void load(int size, String ip, int port) {
        this.size = size;
        this.ip = ip;
        this.port = port;

        switch (size) {
            case 3:
                depth = -1;
                requiredSymbols = 3;
                break;
            case 4:
                depth = 8;
                requiredSymbols = 4;
                break;
            default:
                depth = 3;
                requiredSymbols = 5;
        }
    }

    private void setup() {
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                Label label = new Label("");
                label.setMinSize(32, 32);
                label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                label.setAlignment(Pos.CENTER);
                label.setFont(Font.font("Verdana", FontWeight.NORMAL, 20));

                gridPane.add(label, j, i);
                GridPane.setFillWidth(label, true);
                GridPane.setFillHeight(label, true);

                labels.add(label);
            }
        }
    }

    private void logicServer() throws IOException {
        ServerSocket server = new ServerSocket(port);
        System.out.println("Waiting for client on port [" + server.getLocalPort() + "]...");

        Socket client = server.accept();
        System.out.println("Client connected");

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        DataOutputStream dOut = new DataOutputStream(client.getOutputStream());

        ObjectMapper objectMapper = new ObjectMapper();
        Message startMessage = new Message("start", Math.random(), 0);
        String messageAsString = objectMapper.writeValueAsString(startMessage);

        dOut.writeBytes(messageAsString + "\n");

        String json = bufferedReader.readLine();
        System.out.println(json);
        JsonNode clientMessage = objectMapper.readTree(json);

        double myNumber = startMessage.getP1();
        double opponentNumber = clientMessage.get("p1").asDouble();
        boolean iStart = myNumber > opponentNumber;

        if (iStart) {
            mySymbol = "X";
            myColor = "red";

            opponentSymbol = "O";
            opponentColor = "blue";

            dOut.writeBytes(objectMapper.writeValueAsString(new Message("size", size, size)) + "\n");
            dOut.writeBytes(objectMapper.writeValueAsString(new Message("length", requiredSymbols, 0d)) + "\n");
        } else {
            mySymbol = "O";
            myColor = "blue";

            opponentSymbol = "X";
            opponentColor = "red";

            json = bufferedReader.readLine();
            System.out.println(json);
            clientMessage = objectMapper.readTree(json);
            size = (int) clientMessage.get("p1").asDouble();

            json = bufferedReader.readLine();
            System.out.println(json);
            clientMessage = objectMapper.readTree(json);
            requiredSymbols = (int) clientMessage.get("p1").asDouble();
        }

        Platform.runLater(this::setup);
        aiPlayer = new AIPlayer(size, requiredSymbols, mySymbol, opponentSymbol);
        loop(iStart, objectMapper, bufferedReader, dOut);

        client.close();
        server.close();
    }

    private void logicClient() throws IOException {
        Socket socket = new Socket(ip, port);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

        ObjectMapper objectMapper = new ObjectMapper();
        Message startMessage = new Message("start", Math.random(), 0);
        String messageAsString = objectMapper.writeValueAsString(startMessage);

        dOut.writeBytes(messageAsString + "\n");

        String json = bufferedReader.readLine();
        System.out.println(json);
        JsonNode serverMessage = objectMapper.readTree(json);

        double myNumber = startMessage.getP1();
        double opponentNumber = serverMessage.get("p1").asDouble();
        boolean iStart = myNumber > opponentNumber;

        if (iStart) {
            mySymbol = "X";
            myColor = "red";

            opponentSymbol = "O";
            opponentColor = "blue";

            dOut.writeBytes(objectMapper.writeValueAsString(new Message("size", size, size)) + "\n");
            dOut.writeBytes(objectMapper.writeValueAsString(new Message("length", requiredSymbols, 0d)) + "\n");
        } else {
            mySymbol = "O";
            myColor = "blue";

            opponentSymbol = "X";
            opponentColor = "red";

            json = bufferedReader.readLine();
            System.out.println(json);
            serverMessage = objectMapper.readTree(json);
            size = (int) serverMessage.get("p1").asDouble();

            json = bufferedReader.readLine();
            System.out.println(json);
            serverMessage = objectMapper.readTree(json);
            requiredSymbols = (int) serverMessage.get("p1").asDouble();
        }

        Platform.runLater(this::setup);
        aiPlayer = new AIPlayer(size, requiredSymbols, mySymbol, opponentSymbol);
        loop(iStart, objectMapper, bufferedReader, dOut);

        socket.close();
    }

    private void loop(boolean iStart, ObjectMapper objectMapper, BufferedReader bufferedReader, DataOutputStream dOut) throws IOException {
        if (iStart) {
            int[] move = aiPlayer.next(depth);
            updateBoard(move[0], move[1], false);
            dOut.writeBytes(objectMapper.writeValueAsString(new Message("move", move[1], move[0])) + "\n");
        }

        do {
            // opponent's turn
            String json = bufferedReader.readLine();
            System.out.println(json);
            JsonNode clientMessage = objectMapper.readTree(json);

            int x = (int) clientMessage.get("p2").asDouble();
            int y = (int) clientMessage.get("p1").asDouble();
            updateBoard(x, y, true);

            if (gameOver()) {
                break;
            }

            // my turn
            int[] move = aiPlayer.next(depth);
            updateBoard(move[0], move[1], false);
            dOut.writeBytes(objectMapper.writeValueAsString(new Message("move", move[1], move[0])) + "\n");

            if (gameOver()) {
                break;
            }
        } while (true);
    }

    private void updateBoard(int x, int y, boolean opponent) {
        int index = x * size + y;

        Platform.runLater(() -> {
            if (opponent) {
                labels.get(index).setTextFill(Paint.valueOf(opponentColor));
                labels.get(index).setText(opponentSymbol);
            } else {
                labels.get(index).setTextFill(Paint.valueOf(myColor));
                labels.get(index).setText(mySymbol);
            }
        });

        if (opponent) {
            aiPlayer.updateBoard(x, y, opponentSymbol);
        } else {
            aiPlayer.updateBoard(x, y, mySymbol);
        }
    }

    private boolean gameOver() {
        int result = aiPlayer.gameOver();

        if (result == 42) {
            return false;
        }

        if (result == 0) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Keep calm...");
                alert.setContentText("It's a Draw!");
                alert.showAndWait();
                closeWindow();
            });
        } else {
            if ((result == 1 && mySymbol.equals("X")) || (result == -1 && mySymbol.equals("O"))) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("Congratulations!");
                    alert.setContentText("You Win!");
                    alert.showAndWait();
                    closeWindow();
                });
            } else {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("The game you just can't win.");
                    alert.setContentText("You Lose!");
                    alert.showAndWait();
                    closeWindow();
                });
            }
        }

        return true;
    }

    private void closeWindow() {
        Stage stage = (Stage) gridPane.getScene().getWindow();
        stage.close();
    }
}
