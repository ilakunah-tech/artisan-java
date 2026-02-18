# Artisan Python → Java Architecture

Source: Artisan Python/PyQt application (artisan-for-QQ-ui-test / artisantest-master equivalent).
Target: Java 17, JavaFX 21, Chart-FX 11.2.7, AtlantaFX 2.0.1, jSerialComm 2.10.4.

---

## 1. List of Python Files (One-Line Description)

### Root / entry
| File | Description |
|------|-------------|
| `artisan.py` | Application entry point, launches main window |
| `conftest.py` | Pytest configuration and fixtures |
| `patch_scipy_3120.py` | Compatibility patch for scipy |
| `pylupdate6pro.py` | Qt lupdate/lrelease helper for translations |
| `setup-macos3.py` | macOS build/setup script |

### artisanlib (core)
| File | Description |
|------|-------------|
| `__init__.py` | Package version and build info |
| `acaia.py` | Acaia scale protocol support |
| `aillio_r1.py` | Aillio R1 roaster device driver |
| `aillio_r2.py` | Aillio R2 roaster device driver |
| `alarms.py` | Roast alarms (temperature/time) |
| `async_comm.py` | Asynchronous serial/device communication |
| `atypes.py` | Typed dicts and data types (ProfileData, events, etc.) |
| `autosave.py` | Auto-save roast profiles |
| `axis.py` | Chart axis configuration |
| `background.py` | Background/theme handling |
| `background_events_panel.py` | UI panel for background events |
| `batches.py` | Batch management |
| `ble_port.py` | BLE (Bluetooth Low Energy) port abstraction |
| `bluedot.py` | Bluedot device support |
| `calculator.py` | Roast calculations (e.g. development time) |
| `canvas.py` | Main roast curve canvas (matplotlib-based plot) |
| `colors.py` | Color definitions and palette |
| `colortrack.py` | ColorTrack (color meter) device support |
| `comm.py` | Device communication layer (serial, Phidget, Yocto, etc.) |
| `command_utility.py` | Command encoding/decoding helpers |
| `comparator.py` | Profile comparison logic |
| `cropster.py` | Cropster integration |
| `cup_profile.py` | Cupping profile data |
| `curves.py` | Curves dialog and plotter data UI |
| `designer.py` | Qt designer / form helpers |
| `devices.py` | Device assignment and configuration dialog |
| `dialogs.py` | Base dialog classes (ArtisanDialog, etc.) |
| `events.py` | Event markers dialog (CHARGE, DRY END, FC, DROP, etc.) |
| `event_annotations_help_ru.md` | Help text (RU) for event annotations |
| `event_button_style.py` | Event button styling |
| `filters.py` | Digital filters (LiveFilter, LiveSosFilter, etc.) for RoR/smoothing |
| `giesen.py` | Giesen roaster support |
| `hibean.py` | HiBean device support |
| `hottop.py` | Hottop roaster driver |
| `ikawa.py` | Ikawa roaster driver |
| `kaleido.py` | Kaleido roaster support |
| `large_lcds.py` | Large LCD display widgets |
| `lebrew.py` | LeBrew device support |
| `logs.py` | Logging configuration |
| `loring.py` | Loring roaster support |
| `main.py` | Main application window (ApplicationWindow), UI and orchestration |
| `modbusport.py` | Modbus TCP/serial port |
| `mugma.py` | Mugma device support |
| `notifications.py` | In-app notifications |
| `petroncini.py` | Petroncini roaster support |
| `phases.py` | Roast phase computation (dry, mid, finish, cool) |
| `phases_canvas.py` | Phase visualization on canvas |
| `phidgets.py` | Phidget sensors/actuators (temperature, humidity, etc.) |
| `pid.py` | PID controller logic (scipy.signal, numpy) |
| `pid_control.py` | PID control integration with roast |
| `pid_dialogs.py` | PID configuration dialogs |
| `platformdlg.py` | Platform-specific dialog (e.g. serial permissions) |
| `ports.py` | Port selection and Modbus/Serial scan dialogs |
| `qcheckcombobox.py` | Custom Qt check combobox |
| `qqplus_web.py` | Artisan.plus web integration |
| `qq_theme.py` | Application theme (Qt styles) |
| `qrcode.py` | QR code generation |
| `qtsingleapplication.py` | Single-instance application lock |
| `reference_box_panel.py` | Reference box UI panel |
| `roastlog.py` | Roast log read/write (file format) |
| `roastpath.py` | Roast path / file paths |
| `roast_properties.py` | Roast properties dialog (weight, batch, etc.) |
| `roast_properties_panel.py` | Roast properties panel UI |
| `roest.py` | Roest roaster support |
| `rubasse.py` | Rubasse device support |
| `s7client.py` | Siemens S7 PLC client |
| `s7port.py` | S7 port/connection |
| `sampling.py` | Data sampling and timing |
| `santoker.py` | Santoker roaster (serial) |
| `santoker_r.py` | Santoker R variant |
| `scale.py` | Scale abstraction and supported scales (Acaia, etc.) |
| `simulator.py` | Roast simulator (replay/demo) |
| `slider_style.py` | Slider styling |
| `statistics.py` | Roast statistics |
| `stronghold.py` | Stronghold roaster support |
| `suppress_errors.py` | Context manager to suppress stdout/stderr |
| `time.py` | ArtisanTime (high-resolution elapsed time) |
| `transposer.py` | Profile transposition (time/temp) |
| `util.py` | Utilities (paths, conversions, string/float, RoR, etc.) |
| `weblcds.py` | Web-based LCD displays |
| `wheels.py` | Slider “wheels” UI |
| `widgets.py` | Custom widgets (spinboxes, combos, etc.) |
| `wsport.py` | WebSocket port for devices |

### help
| File | Description |
|------|-------------|
| `alarms_help.py` … `transposer_help.py` | Help content modules for dialogs |

### plus (Artisan.plus cloud)
| File | Description |
|------|-------------|
| `account.py`, `blend.py`, `config.py`, `connection.py`, `controller.py`, `countries.py`, `login.py`, `notifications.py`, `queue.py`, `references.py`, `register.py`, `roast.py`, `schedule.py`, `stock.py`, `sync.py`, `util.py`, `weight.py` | Cloud sync, blends, references, roast upload, etc. |

### proto, uic, misc
| File | Description |
|------|-------------|
| `proto/IkawaCmd_pb2.py` | Protobuf generated for Ikawa commands |
| `uic/*.py` | UI components (EnergyWidget, SetupWidget, dialogs) |
| `misc/KERNpython3.py` | Kernel/script helper |

---

## 2. Module Dependency Scheme (Text)

```
util, atypes, time, suppress_errors
    ↓
filters, sampling, time (used by pid, comm, canvas)
    ↓
pid (uses filters, scipy.signal, numpy)
    ↓
roastlog, roastpath, roast_properties (model/data)
    ↓
phases, statistics, calculator, transposer (depend on atypes, util, curves data)
    ↓
comm, ports, devices (device layer: serial, Phidget, Yocto, Modbus, S7, BLE, etc.)
    ↓
scale, acaia, hottop, aillio_r1, aillio_r2, santoker, kaleido, ... (device drivers)
    ↓
dialogs, widgets (UI building blocks)
    ↓
canvas, axis, colors (plot/visualization data)
    ↓
events, alarms, curves, roast_properties_panel, reference_box_panel (dialogs/panels)
    ↓
main (ApplicationWindow: ties comm, canvas, dialogs, plus, settings)
```

- **Leaf (no internal artisan deps):** `util`, `atypes`, `time`, `suppress_errors`, `filters`.
- **Model / logic:** `atypes`, `util`, `time`, `filters`, `pid`, `phases`, `statistics`, `calculator`, `roastlog`, `roastpath`, `sampling`.
- **Device layer:** `comm`, `ports`, `async_comm`, `modbusport`, `s7port`, `wsport`, `ble_port`, then device-specific: `acaia`, `scale`, `hottop`, `aillio_r1`, `aillio_r2`, `santoker`, `kaleido`, `phidgets`, Yocto (in comm), etc.
- **View / controller:** `dialogs`, `widgets`, `canvas`, `curves`, `events`, `roast_properties`, `devices`, `main`.

---

## 3. External Python Libraries and Java Replacements

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

---

## 4. COM / USB Devices Used by the Code

- **Serial (COM) ports:** Generic serial (PySerial → jSerialComm) for:
  - Fuji PID, thermocouple meters
  - Hottop, Santoker, Kaleido, Giesen, Loring, Roest, Petroncini, Stronghold, Aillio R1/R2, Ikawa, etc.
  - Modbus RTU over serial
  - Scales (e.g. Acaia over serial)
- **USB HID / BLE:** Acaia (BLE), Bluedot, ColorTrack (BLE), Santoker BLE, Mugma.
- **Phidgets (USB):** Temperature, humidity, pressure, voltage, digital I/O, RC servo, stepper, DC/BLDC motors (Phidget22).
- **Yoctopuce (USB):** Voltage, current loop, relay, servo, PWM, generic sensors, power, temperature (yocto_api, yocto_*).
- **Network:** Modbus TCP, Siemens S7 (s7client, s7port), WebSocket (wsport).
- **Single-instance lock:** QLocalSocket (Qt) → file lock or local socket in Java.

---

## 5. Order of Module Migration (Independent → Dependent)

1. **util** – paths, string/float conversions, RoR, constants (no artisan deps).
2. **atypes** – TypedDicts → Java records/DTOs; minimal deps.
3. **time** – ArtisanTime (perf_counter) → System.nanoTime().
4. **suppress_errors** – optional; logging/redirect in Java.
5. **filters** – LiveFilter, LiveSosFilter; use Apache Commons Math or custom.
6. **pid** – PID class; depends on filters; replace scipy/numpy with Java math.
7. **roastpath** – path helpers; depends on util.
8. **roastlog** – read/write roast file format; depends on atypes, util.
9. **phases** – phase computation; depends on atypes, util.
10. **statistics** – roast stats; depends on atypes, util.
11. **calculator** – roast calculations; depends on util, atypes.
12. **sampling** – sampling/timing; depends on time, util.
13. **transposer** – profile transposition; depends on atypes, util.
14. **device/comm** – abstract serial (jSerialComm), then device drivers (acaia, scale, hottop, …) one by one.
15. **dialogs / widgets** – base dialog and custom widgets (JavaFX).
16. **axis, colors** – chart axis and color config (model).
17. **canvas** – main roast curve → Chart-FX; events, zoom, pan.
18. **events, alarms, roast_properties** – dialogs and panels.
19. **curves** – curves dialog and plotter data.
20. **main** – ApplicationWindow; tie model, device, view, controller.

After **Stage 2** (Gradle project and structure), migrate in the order above, one module per branch, with tests and `gradle build` green before proceeding.
