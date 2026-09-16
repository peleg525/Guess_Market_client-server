package gm.ui.screen;

import gm.engine.dto.UserSummaryDto;
import gm.engine.exception.GmException;
import gm.ui.net.GmHttpEngine;
import gm.ui.util.Async;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The very first thing the user sees: pick a server and a username. There are no passwords or
 * sign-up in this exercise - the server accepts any username that isn't currently in use, and
 * rejects it (with a message shown right here) otherwise, letting the user try again.
 */
public final class LoginScreen {

    private static final String DEFAULT_API_BASE_URL = "http://localhost:8080/gm-server/api";

    public interface Callback {
        void onLoggedIn(GmHttpEngine engine, String username);
    }

    public static void show(Stage stage, Callback callback) {
        Label title = new Label("Guess Market");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField serverField = new TextField(DEFAULT_API_BASE_URL);
        serverField.setPrefWidth(320);
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username...");
        usernameField.setPrefWidth(320);

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(320);
        statusLabel.setStyle("-fx-text-fill: #b00020;");

        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(20, 20);
        progress.setVisible(false);
        progress.setManaged(false);

        Button connectButton = new Button("Connect");
        connectButton.setDefaultButton(true);
        connectButton.setPrefWidth(320);

        Runnable attempt = () -> {
            String serverUrl = serverField.getText().trim();
            String username = usernameField.getText().trim();
            statusLabel.setText("");
            if (serverUrl.isEmpty()) {
                statusLabel.setText("Please enter the server's address.");
                return;
            }
            if (username.isEmpty()) {
                statusLabel.setText("Please choose a username.");
                return;
            }

            connectButton.setDisable(true);
            progress.setVisible(true);
            progress.setManaged(true);
            GmHttpEngine engine = new GmHttpEngine(serverUrl);

            Async.call(
                    () -> engine.login(username),
                    (UserSummaryDto user) -> callback.onLoggedIn(engine, user.getName()),
                    error -> {
                        connectButton.setDisable(false);
                        progress.setVisible(false);
                        progress.setManaged(false);
                        String message = (error instanceof GmException) ? error.getMessage() : String.valueOf(error.getMessage());
                        statusLabel.setText(message);
                    });
        };
        connectButton.setOnAction(e -> attempt.run());
        usernameField.setOnAction(e -> attempt.run());

        VBox box = new VBox(10,
                title,
                new Label("Server address:"), serverField,
                new Label("Username:"), usernameField,
                connectButton, progress, statusLabel);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));

        Scene scene = new Scene(box, 480, 420);
        stage.setScene(scene);
        stage.setTitle("Guess Market - Login");
        stage.show();
    }

    private LoginScreen() {
    }
}
