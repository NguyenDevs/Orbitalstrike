# 🛰️ OrbitalStrike

Minecraft plugin — Orbital cannon system

---
![image](https://github.com/user-attachments/assets/9b628bd9-f92e-4d21-bf58-3f8039309d89)
## Cannon System

Players can establish virtual **cannons** in orbit. Each cannon is assigned a unique name and loaded with a specific type of ammunition (payload). Cannons can be fired by command or via a special Fishing Rod targeting tool.

---

## Attack Payloads

### 🔴 STAB — Precision Strike
A focused attack that drops a continuous column of TNT onto a single point, drilling deep into the ground. Ideal for breaching bunkers or reinforced bases.

### 🟡 NUKE — Area Bombardment
A massive area-of-effect strike. Drops a main warhead from high altitude with expanding rings of TNT to carpet-bomb a large area. Ideal for surface annihilation.

### 🟣 RECURSION — Chain Reaction
Creates devastating chain-detonation effects across the target zone for cascading destruction.

---

## Firing Mechanisms

### 🔵 By Command
Fire immediately at the crosshair location or at specific X, Y, Z coordinates using `/cannon fire` or `/cannon target`.

### 🟢 By Fishing Rod
A special Fishing Rod linked to a specific cannon. Clicking with the rod fires at the location the player is looking at. Can be crafted in survival mode if recipes are configured.

---
![image](https://github.com/user-attachments/assets/31b13b0c-d8e2-4634-813e-280ae6d081d7)
## Commands

| Command | Description |
|---|---|
| `/cannon create <name> [payload]` | Create a cannon at your location |
| `/cannon remove <name>` | Remove a cannon |
| `/cannon list` | List all cannons |
| `/cannon fire <cannon>` | Fire payload at crosshair |
| `/cannon info <cannon>` | Show info for a cannon |
| `/cannon target <cannon> <x> <y> <z>` | Fire at specific coordinates |
| `/cannon give <player> <cannon>` | Give targeting tool to a player |
| `/cannon set <cannon> <param> <value>` | Set a parameter for a cannon |
| `/cannon reload` | Reload configuration |

---

## Permissions

| Node | Description |
|---|---|
| `orbitalstrike.use.<cannon>` | Use a specific orbital strike cannon |
| `orbitalstrike.admin` | Create and manage cannons |

---

## WorldGuard Integration

### Flag: `osc-enable`

Controls whether OrbitalStrike cannons are allowed to fire within a WorldGuard region.

> **Default: `deny`** — Regions that have **not** explicitly set this flag will block all orbital strikes by default.

| Value | Effect |
|---|---|
| `allow` | Orbital strikes are permitted in this region |
| `deny` | Orbital strikes are blocked (default for all regions) |

Set to `allow` to enable strikes in a specific region — useful for designated PvP or war zones.
