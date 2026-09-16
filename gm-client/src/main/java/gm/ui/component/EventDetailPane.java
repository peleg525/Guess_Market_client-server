package gm.ui.component;

import gm.engine.dto.EventDetailDto;
import gm.engine.dto.LmsrEventDetailDto;
import gm.engine.dto.OptionStatusDto;
import gm.engine.dto.OrderBookEventDetailDto;
import gm.engine.dto.ParticipantHoldingDto;
import gm.engine.dto.TradeDto;
import gm.engine.exception.GmException;
import gm.engine.model.EventStatus;
import gm.ui.AppContext;
import gm.ui.util.Alerts;
import gm.ui.util.Animations;
import gm.ui.util.Async;
import gm.ui.util.Money;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

/**
 * Renders the full detail of one selected event: header, open/close controls, and the LMSR or
 * order-book trading panel. Every engine call here now goes through {@link Async} - the engine is
 * an HTTP client (see {@link gm.ui.net.GmHttpEngine}), so a synchronous call would freeze the
 * window for the round-trip.
 */
public class EventDetailPane {

    private final AppContext context;
    private final VBox root = new VBox(10);
    private final Label headerLabel = new Label("Select an event to see its details.");
    private final Label subHeaderLabel = new Label();
    private final VBox bodyBox = new VBox(10);

    private Integer currentEventId;

    public EventDetailPane(AppContext context) {
        this.context = context;
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        subHeaderLabel.setWrapText(true);
        root.setPadding(new Insets(10));
        root.getStyleClass().add("detail-pane");
        root.getChildren().addAll(headerLabel, subHeaderLabel, bodyBox);
    }

    public VBox getRoot() {
        return root;
    }

    public void show(int eventId) {
        this.currentEventId = eventId;
        Async.call(
                () -> context.engine().getEventDetail(eventId),
                this::render,
                error -> { });
    }

    private void render(EventDetailDto detail) {
        headerLabel.setText("#" + detail.getId() + " - " + detail.getName() + "  [" + detail.getStatus() + "]");
        subHeaderLabel.setText(detail.getDescription() + "\nCommission: " + detail.getCommissionPercent() + "% ("
                + detail.getCommissionType() + ")   Market maker: " + detail.getMarketMakerUsername()
                + "   Total commission collected: " + Money.format(detail.getTotalCommissionCollected())
                + (detail.getWinningOptionName() != null ? "\nWinning option: " + detail.getWinningOptionName() : ""));

        bodyBox.getChildren().clear();
        bodyBox.getChildren().add(buildLifecycleControls(detail));
        if (detail instanceof LmsrEventDetailDto lmsr) {
            bodyBox.getChildren().add(buildLmsrBody(lmsr));
        } else if (detail instanceof OrderBookEventDetailDto ob) {
            bodyBox.getChildren().add(buildOrderBookBody(ob));
        }
    }

    private HBox buildLifecycleControls(EventDetailDto detail) {
        Button openButton = new Button("Open event");
        openButton.setDisable(!canManage(detail) || detail.getStatus() != EventStatus.NOT_STARTED);
        openButton.setOnAction(e -> {
            openButton.setDisable(true);
            Async.run(
                    () -> context.engine().openEvent(context.actingUsername(), detail.getId()),
                    this::refreshAfterAction,
                    error -> {
                        openButton.setDisable(false);
                        Alerts.error("Could not open event", messageOf(error));
                    });
        });

        Button closeButton = new Button("Close event");
        closeButton.setDisable(!canManage(detail) || detail.getStatus() != EventStatus.ACTIVE);
        closeButton.setOnAction(e -> onCloseEvent(detail));

        return new HBox(10, openButton, closeButton);
    }

    private boolean canManage(EventDetailDto detail) {
        String acting = context.actingUsername();
        return acting != null && acting.equals(detail.getMarketMakerUsername());
    }

    private void onCloseEvent(EventDetailDto detail) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(detail.getOptions().get(0), detail.getOptions());
        dialog.setTitle("Close event");
        dialog.setHeaderText("Which option won?");
        Optional<String> choice = dialog.showAndWait();
        choice.ifPresent(winner -> {
            int winnerNumber = detail.getOptions().indexOf(winner) + 1;
            Async.call(
                    () -> context.engine().closeEvent(context.actingUsername(), detail.getId(), winnerNumber),
                    result -> {
                        if (result.isMarketMakerBlocked()) {
                            Alerts.warning("Market maker blocked", "Closing this event pushed the market maker's "
                                    + "balance negative; their account is now blocked from further actions.");
                        }
                        refreshAfterAction();
                    },
                    error -> Alerts.error("Could not close event", messageOf(error)));
        });
    }

    private VBox buildLmsrBody(LmsrEventDetailDto detail) {
        VBox box = new VBox(10);

        GridPane pricesGrid = new GridPane();
        pricesGrid.setHgap(20);
        pricesGrid.setVgap(4);
        int row = 0;
        for (OptionStatusDto option : detail.getOptionStatuses()) {
            pricesGrid.addRow(row++, new Label(option.getOptionName() + ":"),
                    new Label("price " + Money.format(option.getCurrentPrice())
                            + "   shares bought " + Money.formatShares(option.getTotalSharesBought())));
        }
        box.getChildren().add(pricesGrid);

        if (detail.getStatus() == EventStatus.ACTIVE) {
            box.getChildren().add(buildLmsrBuyForm(detail));
        }

        TableView<TradeDto> table = new TableView<>();
        table.setPrefHeight(180);
        TableColumn<TradeDto, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getOwnerUsername()));
        TableColumn<TradeDto, String> optionCol = new TableColumn<>("Option");
        optionCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getOptionName()));
        TableColumn<TradeDto, String> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(Money.formatShares(c.getValue().getQuantity())));
        TableColumn<TradeDto, String> priceCol = new TableColumn<>("Price paid");
        priceCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(Money.format(c.getValue().getPricePaid())));
        TableColumn<TradeDto, String> commissionCol = new TableColumn<>("Commission");
        commissionCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(Money.format(c.getValue().getCommissionPaid())));
        table.getColumns().addAll(List.of(userCol, optionCol, qtyCol, priceCol, commissionCol));
        table.setItems(FXCollections.observableArrayList(detail.getTradeHistoryNewestFirst()));

        box.getChildren().addAll(new Label("Trade history (most recent first):"), table);
        box.getChildren().add(buildPriceCharts(detail.getId(), detail.getOptions()));
        return box;
    }

    private HBox buildLmsrBuyForm(LmsrEventDetailDto detail) {
        ComboBox<String> optionBox = new ComboBox<>(FXCollections.observableArrayList(detail.getOptions()));
        optionBox.setValue(detail.getOptions().get(0));
        TextField qtyField = new TextField();
        qtyField.setPromptText("Quantity");
        qtyField.setPrefWidth(90);
        Button buyButton = new Button("Buy shares");

        buyButton.setOnAction(e -> {
            String acting = context.actingUsername();
            Double qty = parse(qtyField.getText());
            if (qty == null || qty <= 0) {
                Alerts.warning("Invalid input", "Quantity must be a positive number.");
                return;
            }
            int optionNumber = detail.getOptions().indexOf(optionBox.getValue()) + 1;
            buyButton.setDisable(true);
            Async.call(
                    () -> context.engine().buyLmsrShares(acting, detail.getId(), optionNumber, qty),
                    result -> {
                        buyButton.setDisable(false);
                        Alerts.info("Purchase completed", "Shares cost: " + Money.format(result.getSharesCost())
                                + "\nCommission: " + Money.format(result.getCommissionPaid())
                                + "\nTotal paid: " + Money.format(result.getTotalPaid()));
                        refreshAfterAction();
                    },
                    error -> {
                        buyButton.setDisable(false);
                        Alerts.error("Purchase failed", messageOf(error));
                    });
        });

        return new HBox(8, new Label("Buy:"), optionBox, qtyField, buyButton);
    }

    /** Builds the chart row immediately (empty) and fills each chart in asynchronously once its price history arrives. */
    private HBox buildPriceCharts(int eventId, List<String> options) {
        HBox charts = new HBox(10);
        for (int i = 0; i < options.size(); i++) {
            String optionName = options.get(i);
            int optionNumber = i + 1;
            PriceChart chart = new PriceChart(optionName + " price history");
            HBox.setHgrow(chart, javafx.scene.layout.Priority.ALWAYS);
            charts.getChildren().add(chart);
            Async.call(
                    () -> context.engine().getPriceHistory(eventId, optionNumber),
                    points -> chart.render(optionName, points),
                    error -> { });
        }
        return charts;
    }

    private VBox buildOrderBookBody(OrderBookEventDetailDto detail) {
        VBox box = new VBox(10);
        box.getChildren().add(new Label("Base value (d): " + detail.getD() + "   Initial buy-in: " + detail.getInitial()
                + "   Mint allowed: " + detail.isAllowMint() + "   Event account balance: "
                + Money.format(detail.getEventAccountBalance())));

        boolean canTrade = detail.getStatus() == EventStatus.ACTIVE && context.actingUsername() != null;
        HBox booksRow = new HBox(10);
        for (int i = 0; i < detail.getOptionBooks().size(); i++) {
            int optionNumber = i + 1;
            OrderBookPanel panel = new OrderBookPanel(context);
            panel.render(detail.getOptionBooks().get(i), optionNumber, () -> currentEventId, this::refreshAfterAction, canTrade);
            javafx.scene.layout.HBox.setHgrow(panel, javafx.scene.layout.Priority.ALWAYS);
            booksRow.getChildren().add(panel);
        }
        box.getChildren().add(booksRow);

        TableView<ParticipantHoldingDto> participantsTable = new TableView<>();
        participantsTable.setPrefHeight(150);
        TableColumn<ParticipantHoldingDto, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUsername()));
        participantsTable.getColumns().add(userCol);
        for (int i = 0; i < detail.getOptions().size(); i++) {
            final int idx = i;
            TableColumn<ParticipantHoldingDto, String> col = new TableColumn<>(detail.getOptions().get(i) + " held");
            col.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                    Money.formatShares(c.getValue().getQuantityPerOption().get(idx)) + " (paid "
                            + Money.format(c.getValue().getAmountPaidPerOption().get(idx)) + ")"));
            participantsTable.getColumns().add(col);
        }
        participantsTable.setItems(FXCollections.observableArrayList(detail.getParticipants()));
        box.getChildren().addAll(new Label("Participants:"), participantsTable);

        box.getChildren().add(buildPriceCharts(detail.getId(), detail.getOptions()));
        return box;
    }

    private Double parse(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String messageOf(Throwable error) {
        return (error instanceof GmException) ? error.getMessage() : String.valueOf(error.getMessage());
    }

    private void refreshAfterAction() {
        if (currentEventId != null) {
            show(currentEventId);
        }
        context.refreshAll();
    }
}
