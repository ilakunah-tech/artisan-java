package org.artisan.view;

import javafx.stage.Window;

import java.util.prefs.Preferences;

/**
 * Dialog that remembers size and position (geometry).
 * Analog of ArtisanResizeablDialog / HelpDlg geometry restore in dialogs.py.
 */
public abstract class ArtisanResizeDialog extends ArtisanDialog {

    private static final String PREF_NODE = "org.artisan.view.ArtisanResizeDialog";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";

    private final String geometryKey;

    /**
     * @param owner       owner window (can be null)
     * @param modal       true for modal
     * @param withApply   true to show Apply button
     * @param geometryKey unique key for storing geometry (e.g. "Help", "PortConfig")
     */
    protected ArtisanResizeDialog(Window owner, boolean modal, boolean withApply, String geometryKey) {
        super(owner, modal, withApply);
        this.geometryKey = geometryKey != null ? geometryKey : "default";
    }

    protected ArtisanResizeDialog(Window owner, boolean modal, String geometryKey) {
        this(owner, modal, false, geometryKey);
    }

    @Override
    public void show() {
        restoreGeometry();
        super.show();
        getStage().setOnShown(e -> saveGeometry());
        getStage().setOnHidden(e -> saveGeometry());
    }

    @Override
    public boolean showAndWait() {
        restoreGeometry();
        getStage().setOnShown(e -> saveGeometry());
        getStage().setOnHidden(e -> saveGeometry());
        return super.showAndWait();
    }

    private void restoreGeometry() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE).node(geometryKey);
        double x = prefs.getDouble(KEY_X, Double.NaN);
        double y = prefs.getDouble(KEY_Y, Double.NaN);
        double w = prefs.getDouble(KEY_WIDTH, Double.NaN);
        double h = prefs.getDouble(KEY_HEIGHT, Double.NaN);
        if (!Double.isNaN(w) && w > 0 && !Double.isNaN(h) && h > 0) {
            getStage().setWidth(w);
            getStage().setHeight(h);
        }
        if (!Double.isNaN(x) && !Double.isNaN(y)) {
            getStage().setX(x);
            getStage().setY(y);
        }
    }

    private void saveGeometry() {
        try {
            Preferences prefs = Preferences.userRoot().node(PREF_NODE).node(geometryKey);
            prefs.putDouble(KEY_X, getStage().getX());
            prefs.putDouble(KEY_Y, getStage().getY());
            prefs.putDouble(KEY_WIDTH, getStage().getWidth());
            prefs.putDouble(KEY_HEIGHT, getStage().getHeight());
        } catch (Exception ignored) {
            // ignore storage errors
        }
    }
}
