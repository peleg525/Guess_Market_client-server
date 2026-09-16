package gm.ui.screen;

import gm.engine.dto.EventFilter;
import gm.engine.dto.EventSummaryDto;
import gm.engine.dto.TradeMethod;
import gm.engine.model.EventStatus;
import gm.ui.AppContext;
import gm.ui.component.EventDetailPane;
import gm.ui.util.Async;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Events tab: a filter row, an events table, and a detail pane for whichever event is selected -
 * following the layout in the assignment's UI sketch (filter line above a list, event details and
 * trade panel alongside it).
 */
public class EventsScreen {

    private final AppContext context;
    private final BorderPane root = new BorderPane();
    private final TableView<EventSummaryDto> table = new TableView<>();
    private final EventDetailPane detailPane;

    private final CheckBox lmsrBox = new CheckBox("LMSR");
    private final CheckBox orderBookBox = new CheckBox("Order Book");
    private final CheckBox notStartedBox = new CheckBox("Not started");
    private final CheckBox activeBox = new CheckBox("Active");
    private final CheckBox closedBox = new CheckBox("Closed");
    private final CheckBox onPurchaseBox = new CheckBox("On purchase");
    private final CheckBox onCloseBox = new CheckBox("On close");

    public EventsScreen(AppContext context) {
        this.context = context;
        this.detailPane = new EventDetailPane(context);

        buildTable();
        VBox left = new VBox(8, buildFilterRow(), table);
        left.setPadding(new Insets(8));
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        SplitPane split = new SplitPane(wrapScroll(left), wrapScroll(detailPane.getRoot()));
        split.setDividerPositions(0.42);
        root.setCenter(split);
    }

    public javafx.scene.Node getRoot() {
        return root;
    }

    public void refresh() {
        EventFilter filter = currentFilter();
        Async.call(
                () -> context.engine().getEvents(filter),
                (List<EventSummaryDto> events) -> {
                    EventSummaryDto selected = table.getSelectionModel().getSelectedItem();
                    table.setItems(FXCollections.observableArrayList(events));
                    if (selected != null) {
                        events.stream().filter(e -> e.getId() == selected.getId()).findFirst()
                                .ifPresent(e -> table.getSelectionModel().select(e));
                    }
                },
                error -> { });
    }

    public void selectEvent(int eventId) {
        for (EventSummaryDto e : table.getItems()) {
            if (e.getId() == eventId) {
                table.getSelectionModel().select(e);
                detailPane.show(eventId);
                return;
            }
        }
        detailPane.show(eventId);
    }

    private EventFilter currentFilter() {
        Set<TradeMethod> methods = new HashSet<>();
        if (lmsrBox.isSelected()) methods.add(TradeMethod.LMSR);
        if (orderBookBox.isSelected()) methods.add(TradeMethod.ORDER_BOOK);

        Set<EventStatus> statuses = EnumSet.noneOf(EventStatus.class);
        if (notStartedBox.isSelected()) statuses.add(EventStatus.NOT_STARTED);
        if (activeBox.isSelected()) statuses.add(EventStatus.ACTIVE);
        if (closedBox.isSelected()) statuses.add(EventStatus.CLOSED);

        Set<String> commissionTypes = new HashSet<>();
        if (onPurchaseBox.isSelected()) commissionTypes.add("on-purchase");
        if (onCloseBox.isSelected()) commissionTypes.add("on-close");

        return new EventFilter(methods, statuses, commissionTypes);
    }

    private VBox buildFilterRow() {
        ChangeListener<Boolean> refreshOnChange = (obs, was, is) -> refresh();
        for (CheckBox box : List.of(lmsrBox, orderBookBox, notStartedBox, activeBox, closedBox, onPurchaseBox, onCloseBox)) {
            box.selectedProperty().addListener(refreshOnChange);
        }

        HBox row = new HBox(16,
                labeledGroup("Method (none checked = all):", lmsrBox, orderBookBox),
                new javafx.scene.control.Separator(Orientation.VERTICAL),
                labeledGroup("Status (none checked = all):", notStartedBox, activeBox, closedBox),
                new javafx.scene.control.Separator(Orientation.VERTICAL),
                labeledGroup("Commission (none checked = all):", onPurchaseBox, onCloseBox));
        return new VBox(row);
    }

    private VBox labeledGroup(String caption, CheckBox... boxes) {
        HBox boxRow = new HBox(8, boxes);
        VBox box = new VBox(2, new Label(caption), boxRow);
        return box;
    }

    @SuppressWarnings("unchecked")
    private void buildTable() {
        TableColumn<EventSummaryDto, Number> idCol = new TableColumn<>("#");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        idCol.setPrefWidth(40);

        TableColumn<EventSummaryDto, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        nameCol.setPrefWidth(160);

        TableColumn<EventSummaryDto, String> methodCol = new TableColumn<>("Method");
        methodCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMethod().name()));

        TableColumn<EventSummaryDto, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus().name()));

        TableColumn<EventSummaryDto, String> commissionCol = new TableColumn<>("Commission");
        commissionCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getCommissionPercent() + "% (" + c.getValue().getCommissionType() + ")"));

        TableColumn<EventSummaryDto, String> mmCol = new TableColumn<>("Market Maker");
        mmCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMarketMakerUsername()));

        table.getColumns().addAll(List.of(idCol, nameCol, methodCol, statusCol, commissionCol, mmCol));
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                detailPane.show(selected.getId());
            }
        });
    }

    private ScrollPane wrapScroll(javafx.scene.Node node) {
        ScrollPane scroll = new ScrollPane(node);
        scroll.setFitToWidth(true);
        return scroll;
    }
}
