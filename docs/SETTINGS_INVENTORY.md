# Python Artisan — полный список вкладок и диалогов настроек

## Config menu (main.py create_config_menu)

- Machine (menu)
- **Device** → Device Assignment dialog
- **Comm port** → Port Configuration (ports.py)
- Calibrate delay
- **Curves** → Curves dialog
- **Events** → Events dialog
- **Alarm** → Alarms dialog
- **Phases** → Roast Phases dialog
- Statistics, Window config
- **Colors** → Colors dialog
- Theme (menu)
- **Autosave** → Autosave dialog
- **Batch** → Batch dialog
- Temperature config (menu), Language (menu), UI Mode (menu)

---

## 1. Device Assignment (devices.py)

**Window title:** "Device Assignment"

**Tabs:**
- ET/BT
- Extra Devices
- Symb ET/BT
- Phidgets
- Yoctopuce
- Ambient
- Networks
- Batch Manager

---

## 2. Port Configuration (ports.py)

**Window title:** "Port Configuration"

**Tabs:**
- ET/BT
- Extra
- Modbus
- S7
- WebSocket

---

## 3. Curves (curves.py)

**Window title:** "Curves"

**Tabs:**
- RoR
- Filters
- Plotter
- Math
- Analyze
- UI

---

## 4. Events (events.py)

**Window title:** "Events"

**Tabs:**
- Config
- Buttons
- Sliders
- Quantifiers
- Palettes
- Style
- Annotations

---

## 5. Colors (colors.py)

**Window title:** "Colors"

**Tabs:**
- Curves
- Graph
- LCDs

---

## 6. Roast Phases (phases.py)

**Window title:** "Roast Phases"

Single panel (no tabs): Drying/Maillard/Finishing min–max, Auto Adjusted, From Background, Watermarks, Phases LCDs, Auto DRY, Auto FCs, LCD modes.

---

## 7. Autosave (autosave.py)

**Window title:** "Autosave"

Single panel: Autosave toggle, Add to recent file list, File name prefix, Path, Save also (e.g. PDF).

---

## 8. Batch (batches.py)

**Window title:** "Batch"

Single panel: batch counter / list.

---

## 9. Axes (axis.py)

**Window title:** "Axes"

Single panel: Time axis, Temperature axis, Legend, Grid, Delta axis.

---

## 10. Alarms (alarms.py)

**Window title:** "Alarms"

**Tabs:**
- Alarm Table
- Alarm Sets

---

## 11. Profile Background (background.py)

**Window title:** "Profile Background"

**Tabs:**
- Config
- Events
- Data

---

## 12. Sampling

From main.py / sampling: sampling interval, etc. (separate dialog in Python).

---

## 13. Import

Not a single dialog; menu **Import** with actions (Artisan URL, CSV, JSON, Giesen CSV, Loring CSV, Petroncini CSV, etc.). Can be represented as one “Import” tab with buttons/links for each import type.
