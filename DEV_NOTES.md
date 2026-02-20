# DEV NOTES

## New workspace layout structure

- Live workspace root is `BorderPane` in `src/main/java/org/artisan/ui/screens/RoastLiveScreen.java`.
- `top`: fixed App Bar in `AppShell` (`ri5-topbar`, fixed height 56).
- `center`: `SplitPane(HORIZONTAL)` with:
  - left = chart container (`ri5-chart-container`, grow priority always),
  - right = dock area (`ScrollPane` -> `VBox` of cards), min dock width enforced via `LayoutState.MIN_DOCK_WIDTH`.
- `bottom`: fixed bars (`PhasesCanvasPanel` + `BottomStatusBar`) in `bottomBars`.

## Theme location

- Single active theme file: `src/main/resources/org/artisan/ui/theme/ri5.css`.
- It defines:
  - palette tokens (`-ri5-primary`, `-ri5-primary-alt`, `-ri5-primary-soft`, `-ri5-accent-soft`, `-ri5-accent-strong`),
  - surface/border/text/shadow tokens,
  - spacing/radius tokens,
  - density tokens (`-ri5-card-pad`, chip paddings).
- Theme is loaded in `MainWindow` via `/org/artisan/ui/theme/ri5.css`.

## How to add a new dock card

1. Create content node (prefer responsive pane, avoid absolute positioning).
2. Wrap it in `DockPanel`:
   - `new DockPanel("<panelId>", "<title>", contentNode[, optionalHeaderExtras...])`.
3. Add via `addPanelInOrder(...)` in `RoastLiveScreen`.
4. Register with `DetachablePanelManager.registerPanel(...)`.
5. Add panel ID constant in `LayoutState` if it should persist order/collapsed/detached state.

Card behavior included by default:
- header with title + collapse + `...` placeholder + detach,
- smooth collapse/expand animation,
- internal body scroll (`dock-card-body-scroll`),
- compatible with dock-column scrolling.

## Density mode behavior

- Density is controlled by root style classes:
  - `ri5-density-compact`
  - `ri5-density-comfortable`
- Pre-Roast density selector updates `UIPreferences` and reapplies root class live.
- Theme maps density to token values (card/chip paddings), so layout geometry remains stable while control density changes.

## UI sanity checklist run

- [x] Build compiles (`gradlew compileJava`).
- [x] Top app bar uses fixed height and remains in `BorderPane.top`.
- [x] Live workspace center uses `SplitPane` left chart/right dock.
- [x] Right dock is `ScrollPane` + `VBox` of cards.
- [x] Dock cards support collapse/expand animation.
- [x] Event Log list has internal scroll + bottom filter field.
- [x] Bottom phase/status area is in fixed `BorderPane.bottom`.
- [x] Stage minimum size set to `1280x720` to prevent clipping baseline regressions.
