<div align="center">

# ✦ SellGUIPlus ✦
### Premium Sell Chest Plugin for Spigot / Paper 1.16+

A fast, modern **inventory-based sell chest system**

</div>

---

## What is SellGUIPlus?

SellGUIPlus is a lightweight and optimized **sell chest plugin**.

Players simply **drag items into a chest GUI**, close it, and get paid automatically.

❌ Not a shop  
❌ Not click-to-sell  
✅ Deposit-style selling chest  

---

## Features

- Inventory-based sell chest
- Vault economy support
- Async sell calculations (lag free)
- Admin editor GUI with paging
- SignBoard search input (no chat spam)
- Fully configurable YAML files
- Lightweight and optimized performance

---

## Requirements

- Java 8+
- Spigot or Paper 1.16+
- Vault + Economy plugin

---

## Commands

| Command | Description |
|--------|-------------|
| `/sellgui` | Open sell chest |
| `/sell handitem` | Sell item in hand |
| `/sell inventory` | Sell full inventory |
| `/sellguiadmin config` | Open admin editor |
| `/sellguiadmin reload` | Reload configs |

---

## Permissions

| Permission | Description |
|------------|-------------|
| `sellgui.use` | Player access |
| `sellgui.admin` | Admin access |
| `sellgui.multiplier.2` | Sell bonus multiplier |

---

## How It Works

1. Run `/sellgui`
2. Drag items into chest
3. Close GUI
4. Get paid instantly

Unsellable items are safely returned.

---

## Admin GUI Controls

- Left click → Enable/Disable
- Right click → Change price
- Shift click → Change slot
- Middle click → Reset price
- Search using SignBoard input

---

## Config Example

```yml
items:
  DIAMOND:
    enabled: true
    price: 10
  DIRT:
    enabled: false
    price: 0
