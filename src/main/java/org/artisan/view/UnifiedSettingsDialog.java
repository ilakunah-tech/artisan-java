package org.artisan.view;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;

/**
 * Unified Settings dialog using the ArtisanDialog styling.
 * Wraps SettingsDialog content and delegates apply/ok to it.
 */
public final class UnifiedSettingsDialog extends ArtisanDialog {

    private final SettingsDialog embedded;

    public UnifiedSettingsDialog(SettingsContext ctx) {
        super(ctx != null ? ctx.getOwner() : null, true, true);
        Window owner = ctx != null ? ctx.getOwner() : null;
        this.embedded = new SettingsDialog(ctx);
        getStage().setTitle("Settings");
        getStage().setResizable(true);
        if (owner != null) getStage().initOwner(owner);
    }

    @Override
    protected Node buildContent() {
        Node content = embedded.getDialogPane().getContent();
        StackPane wrapper = new StackPane(content);
        wrapper.addEventHandler(ScrollEvent.ANY, Event::consume);
        wrapper.setPrefSize(780, 600);
        return wrapper;
    }

    @Override
    protected void onApply(ActionEvent e) {
        embedded.applyFromUI();
    }

    @Override
    protected void onOk(ActionEvent e) {
        embedded.applyFromUI();
        super.onOk(e);
    }
}
