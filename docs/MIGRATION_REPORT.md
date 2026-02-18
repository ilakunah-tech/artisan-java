# Artisan Python → Java Migration Report

## 1. Migrated Modules and Branch Names

| Module / area | Branch | Description |
|---------------|--------|-------------|
| util | feature/util | Paths, conversions, RoR, constants |
| atypes | feature/atypes | TypedDicts → DTOs, ProfileData, events |
| time | feature/time | ArtisanTime (nanoTime) |
| filters | feature/filters | LiveFilter, smoothing |
| pid | feature/pid | PID controller logic |
| roastpath | feature/roastpath | Path helpers |
| roastlog | feature/roastlog | .alog read/write |
| phases | feature/phases | Phase computation |
| statistics | feature/statistics | Roast stats |
| calculator | feature/calculator | Roast calculations |
| sampling | feature/sampling | Sampling timer |
| transposer | feature/transposer | Profile transposition |
| device/comm | feature/comm-base, device branches | Serial, Modbus, devices (Hottop, Aillio, etc.) |
| dialogs / widgets | feature/dialogs-widgets | ArtisanDialog, spinboxes, combos |
| axis, colors | feature/axis-colors | AxisConfig, ColorConfig |
| canvas | feature/canvas | CanvasData, RorCalculator |
| events | feature/events | EventList, EventEntry, EventType |
| alarms | feature/alarms | Alarm, AlarmList, AlarmCondition, AlarmAction |
| roast_properties | feature/roast-properties | RoastProperties, RoastPropertiesValidator |
| curves | feature/curves | CurveDefinition, CurveSet, CurveStyle, BackgroundProfile |
| main | feature/main-window | RoastState, RoastSession, AppController, AppSettings, MainWindow |

## 2. Python Libraries and Java Replacements

(Copied from ARCHITECTURE.md section 3.)

| Python | Java replacement |
|--------|------------------|
| Python 3 | Java 17 |
| PyQt6 (Qt widgets, core, gui, network, print) | JavaFX 21 (controls, FXML) |
| numpy | No direct equivalent; use primitive arrays, `List<Double>`, or Apache Commons Math / minimal math in model |
| scipy.signal (IIR filter, etc.) | Apache Commons Math (IIRFilter-style) or custom implementation |
| matplotlib | Chart-FX 11.2.7 |
| matplotlib colors | Chart-FX / AtlantaFX / javafx.scene.paint.Color |
| requests | Java 11+ HttpClient or OkHttp |
| PySerial | jSerialComm 2.10.4 |
| Phidget22 | Phidget Java library (if needed) or abstract behind device interface |
| Yoctopuce (yocto_*) | Yoctopuce Java library or abstract behind device interface |
| PIL (Pillow) | ImageIO / Java BufferedImage |
| arabic_reshaper / bidi | ICU4J or equivalent for RTL/bidi |
| dateutil | java.time |
| PyQt6.QtWebEngineWidgets | JavaFX WebView or separate browser control |
| yaml | SnakeYAML or Jackson YAML |
| zlib | java.util.zip |
| protobuf (Ikawa) | protobuf-java |
| (logging) | java.util.logging or SLF4J + Logback |

## 3. Known Limitations / TODO

- **BLE stub:** BLE port (e.g. Acaia scale) is stubbed; no real BLE connection.
- **S7 port stub:** Siemens S7 PLC client/port not ported; stub if present.
- **Canvas event markers:** Chart event markers (CHARGE, DRY END, FC, DROP) not yet wired to canvas overlay; event list and indices are in model.
- **Device connect/disconnect:** Main window uses StubDevice; real device selection and connect/disconnect UI not fully wired.
- **RoR on load:** Loaded profiles do not recompute delta (RoR) arrays; chart shows loaded temps only unless RoR is recomputed.
- **Preferences scope:** AppSettings uses JVM user prefs; no per-profile or machine-wide separation beyond node.

## 4. Counts

- **Java source files (main):** 77  
- **Java test files:** 50  

(Counts as of final migration step.)
