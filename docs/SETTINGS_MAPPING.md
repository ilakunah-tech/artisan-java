# Сопоставление Python → Java (настройки)

| Python (вкладка/диалог) | Java класс / статус | Что добавить |
|-------------------------|---------------------|--------------|
| **Device Assignment** (ET/BT, Extra, Symb, Phidgets, Yoctopuce, Ambient, Networks, Batch Manager) | DevicesDialog — только выбор устройства + Serial/Modbus/Simulator/Aillio R1 | Встроить контент в таб "Device"; добавить заглушки или подтабы для Extra, Symb, Phidgets, Yoctopuce, Ambient, Networks, Batch Manager |
| **Port Configuration** (ET/BT, Extra, Modbus, S7, WebSocket) | PortsDialog — Serial, Modbus, BLE | Встроить в таб "Ports"; добавить S7, WebSocket, ET/BT, Extra (или заглушки) |
| **Curves** (RoR, Filters, Plotter, Math, Analyze, UI) | SettingsDialog.buildCurvesTab() — только цвета/толщина/сглаживание/видимость | В табе "Curves" добавить внутренний TabPane: RoR, Filters, Plotter, Math, Analyze, UI |
| **Events** (Config, Buttons, Sliders, Quantifiers, Palettes, Style, Annotations) | EventButtonsDialog — только таблица кнопок (max 4) | В табе "Events" добавить TabPane: Config, Buttons, Sliders, Quantifiers, Palettes, Style, Annotations; расширить до полного набора опций |
| **Colors** (Curves, Graph, LCDs) | ColorsDialog — уже Curves/Graph/LCDs; SettingsDialog.buildColorsTab() — палитра графика | Встроить контент ColorsDialog в таб "Colors" (три подтаба) |
| **Phases** | PhasesDialog | Встроить контент в таб "Phases" |
| **Sampling** | SamplingDialog | Встроить контент в таб "Sampling" |
| **Autosave** | AutoSave (controller); нет отдельного диалога в Java | Добавить таб "Autosave", создать UI (или открыть AutoSaveDialog как поддиалог) |
| **Batch** | BatchesDialog | Встроить контент или ссылку в таб "Batch" |
| **Axes** | AxesDialog (view) | Встроить контент в таб "Axes" |
| **Alarms** | AlarmsDialog | Встроить контент в таб "Alarms" |
| **Profile Background** | BackgroundDialog | Встроить контент в таб "Background" |
| **Import** | Разные импорты в MainWindow (file menus) | Таб "Import" с кнопками/ссылками на импорт форматов |

## Модели и persistence (Java)

- AppSettings — device, sampling rate, theme, axis, roast
- DisplaySettings — palette, line widths, smoothing, visibility, AUC, time guide
- AxisConfig — temp/RoR min–max, auto scale
- PhasesSettings / PhasesConfig — dry end, FCs, auto DRY/FCs, LCD modes
- SamplingConfig — interval, oversampling, spike filter
- DeviceConfig, SerialPortConfig, ModbusPortConfig, BlePortConfig, S7Config
- EventButtonConfigPersistence — event buttons (max 4)
- AutoSave — Preferences "autosave.*"
- BackgroundSettings — Preferences "background.*"
- AlarmListPersistence, Batches (model)

Все перечисленные сущности нужно связать с единым окном настроек при Apply/OK.

## Проверка соответствия (unified Settings)

Единое окно Settings реализовано в `SettingsDialog(SettingsContext ctx)`. Порядок вкладок: Device → Ports → Graph → Curves (Line & visibility + RoR & Filters placeholder) → Roast → Events → Colors → Phases → Sampling → Autosave → Batch → Axes → Alarms → Background → Import.

- **OK/Apply**: вызывается `applyInlineChangesOnly()` (Graph, Curves, Roast), затем `applyFromUI()` у каждого встроенного диалога (Device, Ports, Events, Colors, Phases, Sampling, Autosave, Batch, Axes, Alarms, Background), затем `onSettingsApplied` (обновление графика, темы, панелей, событий, будильников).
- **Сохранение**: каждый встроенный диалог сохраняет свои настройки в своём `applyFromUI()` (Preferences/JSON); inline-вкладки пишут в `appSettings`/`displaySettings`/`axisConfig`/`roastStateMachine` и вызывают `appSettings.save()`.
- **Не перенесённые подтабы** (заглушки или будущая доработка): Device (Extra, Symb, Phidgets, Yoctopuce, Ambient, Networks), Ports (S7, WebSocket), Curves (RoR/Filters — подтаб-заглушка), Events (Sliders, Quantifiers, Palettes, Style, Annotations). Import — список форматов, импорт через File menu.
