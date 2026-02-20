# Artisan Java

JavaFX application for coffee roast profiling and machine control.

## New UI Architecture (Cropster RI5–inspired)

The application uses a two-screen layout with a persistent top bar and navigation.

### Screens

1. **Pre-Roast (Setup)** — Default on startup.
   - **Left column:** Roast profile selector, green coffee/lot, batch size, target roast level, reference curve. Buttons: *Start Roast*, *Demo Mode*.
   - **Right column:** Summary card, validation messages, quick curve visibility settings.
   - Validation disables *Start Roast* when batch size is invalid; *Demo Mode* is always available.

2. **Roast (Live)** — Shown when a roast is active or Demo Mode is running.
   - **Center:** Dominant ChartFX roast curve (BT, ET, RoR). Crosshair with tooltip; click on chart to add an event marker (type + note).
   - **Right dock:** Vertical stack of panels, each collapsible and detachable into a separate window:
     - **Curves:** Visibility toggles for BT, ET, RoR, ΔET.
     - **Readouts:** Large numeric tiles (BT, ET, RoR) with size modes S/M/L.
     - **Controls:** Gas/Air/Drum sliders and event buttons (toggle visibility with **C**).
     - **Event Log:** List of events with time and label; filter field.
   - **Bottom:** Status strip (phase, elapsed time, connection status).

### Where to modify

| Area | Package / class |
|------|------------------|
| App shell, top bar, navigation | `org.artisan.ui.AppShell` |
| Pre-Roast layout and validation | `org.artisan.ui.screens.PreRoastScreen` |
| Live screen layout, chart, dock, status | `org.artisan.ui.screens.RoastLiveScreen` |
| Dockable/detachable panel behaviour | `org.artisan.ui.components.DockPanel`, `DetachablePanelManager` |
| Curve toggles | `org.artisan.ui.components.CurveLegendPanel` |
| Readout tiles | `org.artisan.ui.components.ReadoutTile` |
| Event log | `org.artisan.ui.components.EventLogPanel` |
| Bottom status bar | `org.artisan.ui.components.BottomStatusBar` |
| UI state and layout persistence | `org.artisan.ui.state.UIPreferences`, `LayoutState`, `PreferencesStore` |
| View-model for live data | `org.artisan.ui.vm.RoastViewModel` |
| RI5-like theme | `src/main/resources/org/artisan/ui/theme/ri5.css`, `tokens.css` |

### Preference file location

- **UI preferences:** `~/.artisan-java/ui-preferences.json` (user home, `.artisan-java` folder).  
  Persisted: theme, density, readout size (S/M/L), curve visibility (BT, ET, ΔBT, ΔET), main divider position, layout (dock width, panel order, collapsed/detached per panel, detached window bounds), controls panel visibility, and keyboard shortcuts mapping (defaults stored; custom keys optional for future use).  
  Schema version is in the file; unknown keys are ignored; missing keys fall back to defaults.

- **Reset Layout:** Settings → Reset Layout… resets layout to defaults and saves. Restart or switch screens to apply fully.

- **Path:** On Windows: `C:\Users\<username>\.artisan-java\ui-preferences.json`. On macOS/Linux: `~/.artisan-java/ui-preferences.json`.

### Keyboard shortcuts (Roast Live screen)

| Key | Action |
|-----|--------|
| **Space** | Add event marker at current time (opens note dialog) |
| **1** | Charge |
| **2** | Dry end |
| **3** | First crack start |
| **4** | First crack end |
| **5** | Drop |
| **C** | Toggle Controls panel visibility |
| **L** | Toggle Curve Legend panel |
| **E** | Focus Event Log |
| **?** (Shift+/) | Show shortcuts help dialog |

Help → Keyboard Shortcuts (or **F1**) opens the same list in a dialog.

### Developer documentation

#### UI architecture

The new UI follows a view-model pattern. Core roast logic stays in `controller/` and `model/`; the UI subscribes via listeners and bindings.

```
Core (AppController, RoastSession)  →  listeners  →  RoastViewModel (JavaFX properties)
                                                           ↓
                                                   UI components (bindings)
```

- **AppShell** — Main window, top bar, navigation (Pre-Roast ↔ Roast Live). Holds `PreRoastScreen` and `RoastLiveScreen` in a StackPane; only one visible at a time.
- **RoastViewModel** — Exposes `SimpleDoubleProperty` for BT, ET, RoR and `SimpleStringProperty` for phase. Updated from `AppController.addSampleListener` and `addPhaseListener` callbacks on the JavaFX thread via `Platform.runLater`.
- **RoastChartController** — Reused from `view/`; provides ChartFX chart with crosshair, tooltip, click-to-add-event. UI does not modify roast algorithms.

#### Package structure

| Package | Purpose |
|---------|---------|
| `org.artisan.ui` | AppShell, DemoRunner |
| `org.artisan.ui.screens` | PreRoastScreen, RoastLiveScreen |
| `org.artisan.ui.components` | DockPanel, DetachablePanelManager, CurveLegendPanel, ReadoutTile, EventLogPanel, BottomStatusBar, ShortcutHelpDialog |
| `org.artisan.ui.state` | UIPreferences, LayoutState, PreferencesStore |
| `org.artisan.ui.vm` | RoastViewModel |
| `org.artisan.ui.theme` (resources) | ri5.css, tokens.css |

#### Adding a new dock panel

1. Create content `Node` (e.g. `VBox`).
2. Create `DockPanel panel = new DockPanel(panelId, "Title", content)`.
3. Call `panelManager.registerPanel(panel, dockContainer)`.
4. Add `panelId` to `LayoutState.panelOrder` and persist via `PreferencesStore`.
5. In `RoastLiveScreen`, add panel to `addPanelInOrder(panel, layoutState.getPanelOrder())`.

#### Threading

- All UI updates must run on the JavaFX thread.
- Sample and phase callbacks from `AppController` use `Platform.runLater()` before updating view-model or UI.
- Do not block the FX thread; offload heavy work to background threads.

### Build and run

- **Java:** 17+ (JavaFX 21).
- **Build:** `./gradlew build` (Windows: `gradlew.bat build`)
- **Run:** `./gradlew run` (Windows: `gradlew.bat run`)

Existing roast/IO logic (serial, Modbus, HID, CommController, RoastSession, AppController) is unchanged; the UI subscribes to sample and phase updates on the JavaFX thread.
