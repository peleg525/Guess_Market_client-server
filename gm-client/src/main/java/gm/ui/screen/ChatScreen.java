package gm.ui.screen;

import gm.engine.dto.ChatMessageDto;
import gm.ui.AppContext;
import gm.ui.util.Alerts;
import gm.ui.util.Async;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bonus: a system-wide chat room, shared by every connected user. New messages are picked up by
 * {@link #poll()}, which {@link MarketApp} calls on the same timer it uses to refresh events/users
 * (the assignment's "pull" model - see the README).
 */
public class ChatScreen {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private final AppContext context;
    private final ListView<ChatMessageDto> messageList = new ListView<>();
    private final TextField inputField = new TextField();
    private final BorderPane root = new BorderPane();
    private final AtomicLong lastSeenSequence = new AtomicLong(0);
    private boolean pollInFlight = false;

    public ChatScreen(AppContext context) {
        this.context = context;

        messageList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ChatMessageDto message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                } else {
                    String time = TIME_FORMAT.format(Instant.ofEpochMilli(message.getSentAtEpochMillis()));
                    setText("[" + time + "] " + message.getUsername() + ": " + message.getText());
                }
            }
        });

        inputField.setPromptText("Type a message and press Enter...");
        Button sendButton = new Button("Send");
        sendButton.setDefaultButton(true);
        Runnable send = this::sendMessage;
        sendButton.setOnAction(e -> send.run());
        inputField.setOnAction(e -> send.run());

        HBox inputBar = new HBox(6, inputField, sendButton);
        HBox.setHgrow(inputField, Priority.ALWAYS);
        inputBar.setPadding(new Insets(6));

        root.setCenter(messageList);
        root.setBottom(inputBar);
        root.setPadding(new Insets(6));
    }

    public javafx.scene.Node getRoot() {
        return root;
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        inputField.setDisable(true);
        Async.run(
                () -> context.engine().postChatMessage(context.actingUsername(), text),
                () -> {
                    inputField.clear();
                    inputField.setDisable(false);
                    poll();
                },
                error -> {
                    inputField.setDisable(false);
                    Alerts.error("Could not send message", String.valueOf(error.getMessage()));
                });
    }

    /** Fetches any chat messages posted since the last poll and appends them. Safe to call repeatedly; skips overlapping calls. */
    public void poll() {
        if (pollInFlight) {
            return;
        }
        pollInFlight = true;
        Async.call(
                () -> context.engine().getChatMessages(lastSeenSequence.get()),
                (List<ChatMessageDto> newMessages) -> {
                    pollInFlight = false;
                    if (newMessages.isEmpty()) {
                        return;
                    }
                    messageList.getItems().addAll(newMessages);
                    lastSeenSequence.set(newMessages.get(newMessages.size() - 1).getSequence());
                    messageList.scrollTo(messageList.getItems().size() - 1);
                },
                error -> pollInFlight = false);
    }
}
