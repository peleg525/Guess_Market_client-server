package gm.ui.component;

import gm.engine.dto.CreateEventSpecDto;
import gm.engine.dto.TradeMethod;
import gm.engine.exception.GmException;
import gm.ui.AppContext;
import gm.ui.util.Alerts;
import gm.ui.util.Async;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.Optional;

/** Bonus: lets the acting user create a brand-new event, becoming its market maker. */
public class CreateEventDialog {

    private final AppContext context;

    public CreateEventDialog(AppContext context) {
        this.context = context;
    }

    public void showAndCreate() {
        String actingUser = context.actingUsername();

        Dialog<CreateEventSpecDto> dialog = new Dialog<>();
        dialog.setTitle("Create a new event");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField();
        TextField descriptionField = new TextField();
        TextField option1Field = new TextField("Yes");
        TextField option2Field = new TextField("No");
        TextField commissionField = new TextField("5");
        ComboBox<String> commissionTypeBox = new ComboBox<>(FXCollections.observableArrayList("on-purchase", "on-close"));
        commissionTypeBox.setValue("on-purchase");
        ComboBox<TradeMethod> methodBox = new ComboBox<>(FXCollections.observableArrayList(TradeMethod.values()));
        methodBox.setValue(TradeMethod.LMSR);

        TextField bField = new TextField("100");
        TextField dField = new TextField("1");
        TextField initialField = new TextField("100");
        CheckBox allowMintBox = new CheckBox("Allow mint");
        allowMintBox.setSelected(true);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        int row = 0;
        grid.addRow(row++, new Label("Name:"), nameField);
        grid.addRow(row++, new Label("Description:"), descriptionField);
        grid.addRow(row++, new Label("Option 1:"), option1Field);
        grid.addRow(row++, new Label("Option 2:"), option2Field);
        grid.addRow(row++, new Label("Commission %:"), commissionField);
        grid.addRow(row++, new Label("Commission type:"), commissionTypeBox);
        grid.addRow(row++, new Label("Method:"), methodBox);
        grid.addRow(row++, new Label("LMSR b:"), bField);
        grid.addRow(row++, new Label("Order book d:"), dField);
        grid.addRow(row++, new Label("Order book initial:"), initialField);
        grid.addRow(row++, allowMintBox);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            try {
                int commission = Integer.parseInt(commissionField.getText().trim());
                return new CreateEventSpecDto(nameField.getText(), descriptionField.getText(), commission,
                        commissionTypeBox.getValue(), option1Field.getText(), option2Field.getText(),
                        methodBox.getValue(), Integer.parseInt(bField.getText().trim()),
                        Integer.parseInt(dField.getText().trim()), Integer.parseInt(initialField.getText().trim()),
                        allowMintBox.isSelected());
            } catch (NumberFormatException e) {
                Alerts.warning("Invalid input", "Commission, b, d and initial must be whole numbers.");
                return null;
            }
        });

        Optional<CreateEventSpecDto> spec = dialog.showAndWait();
        spec.ifPresent(s -> Async.call(
                () -> context.engine().createEvent(actingUser, s),
                created -> {
                    context.refreshAll();
                    Alerts.info("Event created", "Event '" + s.getName() + "' was created. Open it from the Events tab to start trading.");
                },
                error -> {
                    String message = (error instanceof GmException) ? error.getMessage() : String.valueOf(error.getMessage());
                    Alerts.error("Could not create event", message);
                }));
    }
}
