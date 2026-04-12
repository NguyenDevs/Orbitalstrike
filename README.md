# 🛰️ OrbitalStrike

**Minecraft Plugin — Experience devastating power from orbit.**

---

![Banner](https://github.com/user-attachments/assets/9b628bd9-f92e-4d21-bf58-3f8039309d89)

## 🌌 Overview
**OrbitalStrike** allows players to establish orbital satellite cannon systems to perform precision strikes or massive bombardments. The plugin brings a modern warfare experience with stunning visual effects and overwhelming power.

---

## 💣 Attack Payloads

The cannon system supports 4 advanced weapon types, each with its own mechanics and purpose:

### 🔴 STAB — Precision Strike
A focused, high-speed attack that drops a continuous column of TNT onto a single point, drilling deep into the ground. Perfect for destroying the most fortified underground bunkers.

### 🟡 NUKE — Area Bombardment
Launches a massive nuclear warhead from orbit, creating concentric explosion rings that flatten everything on the surface.

### 🟣 RECURSION — Chain Reaction
Creates a cascading series of explosions. Each detonation spawns more explosives, causing extreme chaos across the target zone.



### ⚡ EMP — Shockwave
Emits a sequence of expanding electromagnetic pulses that disable Redstone devices, shatter glass, and cripple enemies with Blindness and Weakness.

---

## 🛠️ How It Works

### 🎮 Firing by Command
You can signal the cannon to fire immediately at your crosshair or at specific X, Y, Z coordinates:
- `/cannon fire <name>`
- `/cannon target <name> <x> <y> <z>`

### 🎣 Remote Controller
Use a special Remote Controller (crafted or given) linked to a specific cannon. Simply click with the rod to fire exactly where you are looking. These can be crafted in Survival mode.

---

## 📜 Commands

| Command | Description |
|---|---|
| `/cannon create <name> [payload]` | Establish a new cannon at your current location |
| `/cannon remove <name>` | Decommission and remove a cannon from orbit |
| `/cannon list` | List all available cannons under your control |
| `/cannon info <name>` | View detailed stats (Payload type, location, etc.) |
| `/cannon set <name> <param> <value>` | Customize power, radius, or speed parameters |
| `/cannon reload` | Refresh the plugin configuration |

---

## 🛡️ Region Protection
The plugin is fully compatible with **WorldGuard**. Administrators can control cannon usage via:
- **Flag:** `osc-enable`
- **Default:** `deny` (Protected regions block all orbital strikes by default).

---
© 2026 Developed by **NguyenDevs**
