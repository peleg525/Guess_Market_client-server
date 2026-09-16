package gm.ui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

public final class Alerts {

    private Alerts() {
    }

    public static void error(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (message.length() > 200 || message.contains("\n")) {
            TextArea area = new TextArea(message);
            area.setEditable(false);
            area.setWrapText(true);
            area.setPrefSize(480, 200);
            alert.getDialogPane().setContent(area);
        } else {
            alert.setContentText(message);
        }
        alert.showAndWait();
    }

    public static void info(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void warning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
