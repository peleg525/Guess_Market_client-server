package gm.ui.component;

import gm.engine.dto.OptionBookDto;
import gm.engine.dto.OrderRowDto;
import gm.engine.exception.GmException;
import gm.engine.model.OrderSide;
import gm.ui.AppContext;
import gm.ui.util.Alerts;
import gm.ui.util.Async;
import gm.ui.util.Money;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.IntSupplier;

/** One option's order book: bids/asks tables, LAST/BID/ASK/MID/SPREAD stats, and an order-entry form. */
public class OrderBookPanel extends VBox {

    private final AppContext context;
    private final TableView<OrderRowDto> bidsTable = new TableView<>();
    private final TableView<OrderRowDto> asksTable = new TableView<>();
    private final Label statsLabel = new Label();
    private final Label titleLabel = new Label();

    public OrderBookPanel(AppContext context) {
        this.context = context;
        setSpacing(6);
        setPadding(new Insets(6));
        getStyleClass().add("detail-pane");

        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        buildTable(bidsTable, "Bids (buy)");
        buildTable(asksTable, "Asks (sell)");

        HBox books = new HBox(10, labeled("Bids", bidsTable), labeled("Asks", asksTable));
        getChildren().addAll(titleLabel, statsLabel, books);
    }

    public void render(OptionBookDto book, int optionNumber, IntSupplier eventId, Runnable onOrderPlaced, boolean canTrade) {
        titleLabel.setText(book.getOptionName());
        bidsTable.setItems(FXCollections.observableArrayList(book.getBids()));
        asksTable.setItems(FXCollections.observableArrayList(book.getAsks()));

        var stats = book.getStats();
        statsLabel.setText(String.format("LAST %s   BID %s   ASK %s   MID %s   SPREAD %s",
                fmt(stats.getLast()), fmt(stats.getBid()), fmt(stats.getAsk()), fmt(stats.getMid()), fmt(stats.getSpread())));

        getChildren().removeIf(n -> "order-form".equals(n.getId()));
        if (canTrade) {
            javafx.scene.Node form = buildOrderForm(optionNumber, eventId, onOrderPlaced);
            form.setId("order-form");
            getChildren().add(form);
        }
    }

    private javafx.scene.Node buildOrderForm(int optionNumber, IntSupplier eventId, Runnable onOrderPlaced) {
        ComboBox<OrderSide> sideBox = new ComboBox<>(FXCollections.observableArrayList(OrderSide.values()));
        sideBox.setValue(OrderSide.BUY);
        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");
        qtyField.setPrefWidth(80);
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        priceField.setPrefWidth(80);
        Button submit = new Button("Place order");

        submit.setOnAction(e -> {
            String actingUser = context.actingUsername();
            Double qty = parse(qtyField.getText());
            Double price = parse(priceField.getText());
            if (qty == null || price == null) {
                Alerts.warning("Invalid input", "Quantity and price must be numbers.");
                return;
            }
            submit.setDisable(true);
            Async.call(
                    () -> context.engine().placeOrder(actingUser, eventId.getAsInt(), optionNumber,
                            sideBox.getValue(), qty, price),
                    result -> {
                        submit.setDisable(false);
                        qtyField.clear();
                        priceField.clear();
                        if (!result.getNewlyBlockedUsernames().isEmpty()) {
                            Alerts.warning("Account blocked", "This order pushed the following account(s) negative; "
                                    + "they are now blocked from further actions: " + result.getNewlyBlockedUsernames());
                        }
                        onOrderPlaced.run();
                    },
                    error -> {
                        submit.setDisable(false);
                        String message = (error instanceof GmException) ? error.getMessage() : String.valueOf(error.getMessage());
                        Alerts.error("Order rejected", message);
                    });
        });

        GridPane grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(4);
        grid.addRow(0, new Label("Side:"), sideBox, new Label("Qty:"), qtyField, new Label("Price:"), priceField, submit);
        return grid;
    }

    private Double parse(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String fmt(Double value) {
        return value == null ? "-" : Money.format(value);
    }

    private void buildTable(TableView<OrderRowDto> table, String tooltip) {
        table.setTooltip(new Tooltip(tooltip));
        table.setPrefHeight(140);
        TableColumn<OrderRowDto, String> ownerCol = new TableColumn<>("User");
        ownerCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getOwnerUsername()));
        TableColumn<OrderRowDto, String> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(Money.formatShares(c.getValue().getQuantity())));
        TableColumn<OrderRowDto, String> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(Money.format(c.getValue().getPrice())));
        table.getColumns().addAll(List.of(ownerCol, qtyCol, priceCol));
    }

    private VBox labeled(String caption, TableView<OrderRowDto> table) {
        VBox box = new VBox(2, new Label(caption), table);
        HBox.setHgrow(box, javafx.scene.layout.Priority.ALWAYS);
        return box;
    }
}
