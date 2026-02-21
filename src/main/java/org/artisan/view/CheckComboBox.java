package org.artisan.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Multi-select ComboBox — parity with Python artisanlib/qcheckcombobox.py CheckComboBox.
 *
 * <p>Displays a comma-separated summary of selected items as the combo label.
 * The dropdown shows checkboxes for each item.
 * Fires {@link #setOnFlagChanged} callback when an item is toggled.
 *
 * <p>Usage:
 * <pre>
 *   CheckComboBox combo = new CheckComboBox();
 *   combo.getItems().addAll("Air", "Drum", "Power", "Temp");
 *   combo.setChecked(0, true); // check first item
 *   combo.setOnFlagChanged((index, checked) -&gt; System.out.println(index + " -&gt; " + checked));
 * </pre>
 */
public final class CheckComboBox extends ComboBox<String> {

    private final BitSet checkedState;
    private BiConsumer<Integer, Boolean> onFlagChanged;
    private String placeholderText = "Select...";

    public CheckComboBox() {
        this.checkedState = new BitSet();
        setCellFactory(buildCellFactory());
        setButtonCell(buildButtonCell());
        getItems().addListener((javafx.collections.ListChangeListener<String>) change -> {
            checkedState.clear(getItems().size(), checkedState.size());
            updateButtonText();
        });
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Returns true if item at index is checked. */
    public boolean isChecked(int index) {
        return index >= 0 && index < getItems().size() && checkedState.get(index);
    }

    /** Sets the checked state of item at index. */
    public void setChecked(int index, boolean checked) {
        if (index < 0 || index >= getItems().size()) return;
        if (checked) checkedState.set(index); else checkedState.clear(index);
        updateButtonText();
        if (onFlagChanged != null) onFlagChanged.accept(index, checked);
    }

    /** Returns a list of checked item indices. */
    public List<Integer> getCheckedIndices() {
        List<Integer> result = new ArrayList<>();
        for (int i = checkedState.nextSetBit(0); i >= 0; i = checkedState.nextSetBit(i + 1)) {
            if (i < getItems().size()) result.add(i);
        }
        return result;
    }

    /** Returns a list of checked item labels. */
    public List<String> getCheckedItems() {
        List<String> result = new ArrayList<>();
        for (int idx : getCheckedIndices()) result.add(getItems().get(idx));
        return result;
    }

    /**
     * Callback fired when an item's checked state changes.
     * Arguments: (itemIndex, newCheckedState).
     * Parity with Python flagChanged signal.
     */
    public void setOnFlagChanged(BiConsumer<Integer, Boolean> handler) {
        this.onFlagChanged = handler;
    }

    public String getPlaceholderText() { return placeholderText; }
    public void setPlaceholderText(String text) {
        this.placeholderText = text;
        updateButtonText();
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void updateButtonText() {
        List<String> checked = getCheckedItems();
        String label = checked.isEmpty() ? placeholderText : String.join(", ", checked);
        setValue(label);
    }

    private Callback<ListView<String>, ListCell<String>> buildCellFactory() {
        return lv -> new ListCell<>() {
            private final CheckBox checkBox = new CheckBox();

            {
                checkBox.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx >= 0 && idx < getItems().size()) {
                        setChecked(idx, checkBox.isSelected());
                        // keep popup open (JavaFX closes it by default on selection; workaround)
                        getListView().getSelectionModel().clearSelection();
                    }
                });
                setGraphic(checkBox);
                setText(null);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox.setText(item);
                    checkBox.setSelected(isChecked(getIndex()));
                    setGraphic(checkBox);
                }
            }
        };
    }

    private ListCell<String> buildButtonCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(placeholderText);
                } else {
                    setText(item);
                }
                setGraphic(null);
            }
        };
    }
}
