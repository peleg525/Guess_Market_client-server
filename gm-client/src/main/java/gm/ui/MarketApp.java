package gm.ui;

import gm.engine.dto.LoadResultDto;
import gm.engine.exception.GmException;
import gm.ui.component.CreateEventDialog;
import gm.ui.net.GmHttpEngine;
import gm.ui.screen.ChatScreen;
import gm.ui.screen.EventsScreen;
import gm.ui.screen.LoginScreen;
import gm.ui.screen.UsersScreen;
import gm.ui.util.Alerts;
import gm.ui.util.Animations;
import gm.ui.util.Async;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Root of the JavaFX application. Exercise 2 had no login concept; here the app shows
 * {@link LoginScreen} first, then builds the same top toolbar (file loading, skin + animation
 * toggles, new-event bonus) and Events/Account/Chat navigation, per the layout sketched in
 * targil01/ex3/ex 3 scetch.pptx (the "Account" tab replaces Exercise 2's "Users" tab).
 * <p>
 * Because every engine call is now a network round-trip to the server, this class also owns the
 * "pull" refresh loop: a background timer calls {@link #refreshAll()} and polls chat every second
 * (within the assignment's 0.5-2s guidance), keeping every connected client's view of shared state -
 * other users, other events, chat - up to date without the user having to do anything.
 */
public class MarketApp extends Application {

    private static final String[] SKIN_NAMES = {"Default", "Dark", "Ocean"};
    private static final String[] SKIN_FILES = {"default.css", "dark.css", "ocean.css"};
    private static final long PULL_INTERVAL_MILLIS = 1000;

    private final ScheduledExecutorService pullScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "gm-pull");
        t.setDaemon(true);
        return t;
    });

    private AppContext context;
    private GmHttpEngine engine;
    private String username;

    private Label filePathLabel;
    private Label loggedInLabel;
    private ProgressBar progressBar;
    private Scene scene;

    private EventsScreen eventsScreen;
    private UsersScreen usersScreen;
    private ChatScreen chatScreen;
    private StackPane centerArea;

    @Override
    public void start(Stage stage) {
        LoginScreen.show(stage, (loggedInEngine, loggedInUsername) -> {
            this.engine = loggedInEngine;
            this.username = loggedInUsername;
            buildMainUi(stage);
        });
    }

    private void buildMainUi(Stage stage) {
        context = new AppContext(engine, username);

        eventsScreen = new EventsScreen(context);
        usersScreen = new UsersScreen(context);
        chatScreen = new ChatScreen(context);
        context.setOpenEventDetail(eventId -> {
            showScreen(eventsScreen.getRoot());
            eventsScreen.selectEvent(eventId);
        });
        context.setRefreshAll(this::refreshAll);

        centerArea = new StackPane(eventsScreen.getRoot(), usersScreen.getRoot(), chatScreen.getRoot());

        BorderPane root = new BorderPane();
        root.setTop(new javafx.scene.layout.VBox(buildToolBar(stage), buildNavBar()));
        root.setCenter(centerArea);

        scene = new Scene(root, 1100, 720);
        applySkin(SKIN_FILES[0]);

        stage.setScene(scene);
        stage.setTitle("Guess Market - " + username);
        stage.setMinWidth(480);
        stage.setMinHeight(360);
        stage.setOnCloseRequest(e -> {
            pullScheduler.shutdownNow();
            Async.run(() -> engine.logout(username), () -> { }, error -> { });
        });
        stage.show();

        showScreen(eventsScreen.getRoot());
        refreshAll();
        startPulling();
    }

    private void startPulling() {
        pullScheduler.scheduleWithFixedDelay(() -> {
            refreshAll();
            chatScreen.poll();
        }, PULL_INTERVAL_MILLIS, PULL_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
    }

    private HBox buildToolBar(Stage stage) {
        filePathLabel = new Label("No file uploaded yet.");
        filePathLabel.setMaxWidth(300);

        Button loadButton = new Button("Upload events file...");
        loadButton.setOnAction(e -> onUploadFile(stage));

        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setManaged(false);

        loggedInLabel = new Label("Logged in as: " + username);
        loggedInLabel.setStyle("-fx-font-weight: bold;");

        ComboBox<String> skinCombo = new ComboBox<>();
        skinCombo.getItems().addAll(SKIN_NAMES);
        skinCombo.setValue(SKIN_NAMES[0]);
        skinCombo.setOnAction(e -> {
            int index = skinCombo.getSelectionModel().getSelectedIndex();
            applySkin(SKIN_FILES[index]);
        });

        CheckBox animationsCheck = new CheckBox("Enable animations");
        animationsCheck.selectedProperty().bindBidirectional(context.animationsEnabledProperty());

        Button newEventButton = new Button("+ New Event");
        newEventButton.setOnAction(e -> new CreateEventDialog(context).showAndCreate());

        HBox box = new HBox(10, filePathLabel, loadButton, progressBar, spacer(), loggedInLabel,
                newEventButton, new Label("Skin:"), skinCombo, animationsCheck);
        box.getStyleClass().add("toolbar-bar");
        box.setPadding(new Insets(6));
        return box;
    }

    private HBox buildNavBar() {
        ToggleGroup group = new ToggleGroup();
        ToggleButton eventsBtn = new ToggleButton("Events");
        ToggleButton accountBtn = new ToggleButton("Account");
        ToggleButton chatBtn = new ToggleButton("Chat");
        for (ToggleButton btn : new ToggleButton[]{eventsBtn, accountBtn, chatBtn}) {
            btn.getStyleClass().add("nav-button");
            btn.setToggleGroup(group);
        }
        eventsBtn.setSelected(true);

        eventsBtn.setOnAction(e -> showScreen(eventsScreen.getRoot()));
        accountBtn.setOnAction(e -> {
            usersScreen.refresh();
            showScreen(usersScreen.getRoot());
        });
        chatBtn.setOnAction(e -> {
            chatScreen.poll();
            showScreen(chatScreen.getRoot());
        });

        HBox box = new HBox(6, eventsBtn, accountBtn, chatBtn);
        box.getStyleClass().add("toolbar-bar");
        box.setPadding(new Insets(4, 6, 8, 6));
        return box;
    }

    private void showScreen(javafx.scene.Node node) {
        node.setVisible(true);
        node.setManaged(true);
        for (javafx.scene.Node sibling : centerArea.getChildren()) {
            if (sibling != node) {
                sibling.setVisible(false);
                sibling.setManaged(false);
            }
        }
        Animations.fadeIn(node, context.animationsEnabled());
    }

    private javafx.scene.layout.Region spacer() {
        javafx.scene.layout.Region region = new javafx.scene.layout.Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private void onUploadFile(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a Guess Market events file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File file = chooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        Task<LoadResultDto> task = new Task<>() {
            @Override
            protected LoadResultDto call() throws Exception {
                byte[] content = Files.readAllBytes(file.toPath());
                return context.engine().uploadEventsFile(context.actingUsername(), file.getName(), content);
            }
        };

        progressBar.progressProperty().unbind();
        progressBar.setProgress(-1);
        progressBar.setVisible(true);
        progressBar.setManaged(true);

        task.setOnSucceeded(e -> {
            progressBar.setVisible(false);
            progressBar.setManaged(false);
            LoadResultDto result = task.getValue();
            filePathLabel.setText(file.getAbsolutePath());
            filePathLabel.setTooltip(new javafx.scene.control.Tooltip(file.getAbsolutePath()));
            refreshAll();
            Alerts.info("File uploaded", "Added " + result.getAddedEventCount() + " event(s). The system now has "
                    + result.getTotalEventCount() + " event(s) in total.");
        });

        task.setOnFailed(e -> {
            progressBar.setVisible(false);
            progressBar.setManaged(false);
            Throwable ex = task.getException();
            String message = ex instanceof GmException ? ex.getMessage() : "Unexpected error: " + ex;
            Alerts.error("Could not upload file", message);
        });

        Thread thread = new Thread(task, "gm-file-uploader");
        thread.setDaemon(true);
        thread.start();
    }

    private void refreshAll() {
        eventsScreen.refresh();
        usersScreen.refresh();
    }

    private void applySkin(String cssFile) {
        scene.getStylesheets().setAll(getClass().getResource("/gm/ui/css/" + cssFile).toExternalForm());
    }
}
