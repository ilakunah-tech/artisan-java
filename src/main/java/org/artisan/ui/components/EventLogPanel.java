package org.artisan.ui.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.artisan.model.EventEntry;
import org.artisan.model.EventType;

import java.util.List;
import java.util.function.Consumer;

/**
 * Event log: quick-add input, compact list, optional filter.
 */
public final class EventLogPanel extends VBox {

    private final ListView<EventEntry> listView;
    private final TextField quickAddField;
    private final TextField filterField;
    private final ObservableList<EventEntry> items = FXCollections.observableArrayList();
    private List<Double> timex;
    private Consumer<String> onQuickAdd;
    private Consumer<EventEntry> onEventSelected;

    public EventLogPanel() {
        setSpacing(6);
        setPadding(new Insets(0));
        setMinHeight(120);

        quickAddField = new TextField();
        quickAddField.setPromptText("Add event (Enter)…");
        quickAddField.getStyleClass().add("ri5-event-log-quickadd");
        quickAddField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String t = quickAddField.getText();
                if (t != null && !t.isBlank() && onQuickAdd != null) {
                    onQuickAdd.accept(t.trim());
                    quickAddField.clear();
                }
            }
        });

        listView = new ListView<>(items);
        listView.getStyleClass().add("ri5-event-log-list");
        listView.setMinHeight(140);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(EventEntry e, boolean empty) {
                super.updateItem(e, empty);
                if (empty || e == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    double timeSec = timeAt(e.getTimeIndex());
                    int m = (int) (timeSec / 60);
                    int s = (int) (timeSec % 60);
                    String timeStr = String.format("%d:%02d", m, s);
                    double temp = e.getValue();
                    String label = e.getLabel() != null ? e.getLabel() : e.getType().name();
                    String text = String.format("%s  %.1f°C  %s", timeStr, temp, label);
                    setText(text);
                    if (isFixedEvent(e.getType())) {
                        Label check = new Label("\u2713");
                        check.getStyleClass().add("ri5-event-status-icon");
                        setGraphic(check);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        listView.setOnMouseClicked(ev -> {
            EventEntry sel = listView.getSelectionModel().getSelectedItem();
            if (sel != null && onEventSelected != null) {
                onEventSelected.accept(sel);
            }
        });
        VBox.setVgrow(listView, Priority.ALWAYS);

        filterField = new TextField();
        filterField.setPromptText("Filter…");
        filterField.textProperty().addListener((a, b, c) -> filterList(c));

        getChildren().addAll(quickAddField, listView, filterField);
    }

    public void setOnQuickAdd(Consumer<String> callback) {
        this.onQuickAdd = callback;
    }

    /** Called when user selects an event in the list; use to center chart and highlight. */
    public void setOnEventSelected(Consumer<EventEntry> callback) {
        this.onEventSelected = callback;
    }

    /** Scroll list to the given event and select it. */
    public void scrollToEvent(EventEntry entry) {
        if (entry == null || !items.contains(entry)) return;
        listView.getSelectionModel().select(entry);
        listView.scrollTo(entry);
    }

    private static boolean isFixedEvent(EventType type) {
        return type == EventType.CHARGE || type == EventType.DRY_END || type == EventType.FC_START
            || type == EventType.FC_END || type == EventType.DROP;
    }

    private double timeAt(int index) {
        if (timex != null && index >= 0 && index < timex.size()) return timex.get(index);
        return 0;
    }

    private void filterList(String filter) {
        listView.setItems(items);
        if (filter != null && !filter.isBlank()) {
            ObservableList<EventEntry> filtered = FXCollections.observableArrayList();
            String f = filter.toLowerCase();
            for (EventEntry e : items) {
                if (e.getLabel() != null && e.getLabel().toLowerCase().contains(f))
                    filtered.add(e);
            }
            listView.setItems(filtered);
        }
    }

    public void setTimex(List<Double> timex) {
        this.timex = timex;
        listView.refresh();
    }

    public void setEvents(List<EventEntry> events) {
        items.clear();
        if (events != null) items.addAll(events);
    }

    public ObservableList<EventEntry> getItems() {
        return items;
    }

    /** Focus the quick-add field (e.g. for keyboard shortcut E). */
    public void requestFocusFilter() {
        javafx.application.Platform.runLater(() -> quickAddField.requestFocus());
    }
}
