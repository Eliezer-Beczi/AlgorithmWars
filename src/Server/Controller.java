package Server;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {
    public TextField sizeField;
    public TextField ipField;
    public TextField portField;
    public Button hostButton;
    public Button connectButton;

    public void hostTicTacToe(ActionEvent actionEvent) {
        if (sizeField.getText().isEmpty() || portField.getText().isEmpty()) {
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("ticTacToe.fxml"));

        try {
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setMinWidth(500);
            stage.setMinHeight(500);

            TicTacToeController controller = loader.getController();
            controller.loadHost(Integer.parseInt(sizeField.getText()), ipField.getText(), Integer.parseInt(portField.getText()));

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connectTicTacToe(ActionEvent actionEvent) {
        if (sizeField.getText().isEmpty() || ipField.getText().isEmpty() || portField.getText().isEmpty()) {
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("ticTacToe.fxml"));

        try {
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setMinWidth(500);
            stage.setMinHeight(500);

            TicTacToeController controller = loader.getController();
            controller.loadConnect(Integer.parseInt(sizeField.getText()), ipField.getText(), Integer.parseInt(portField.getText()));

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
