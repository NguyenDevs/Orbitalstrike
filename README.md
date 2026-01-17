![image](https://github.com/user-attachments/assets/9b628bd9-f92e-4d21-bf58-3f8039309d89)

## OrbitalStrike Plugin Features

**Cannon system:**  
> Players can establish virtual "cannons" in orbit. Each cannon is assigned a unique name and loaded with a specific type of ammunition (payload).

**Attack Modes (Payloads):**  
> **STAB (Precision Strike):** A focused attack that drops a continuous column of TNT onto a single point, drilling deep into the ground (ideal for breaching bunkers or reinforced bases).  
>  
> **NUKE (Area Bombardment):** A massive area-of-effect strike. It drops a main warhead from high altitude, accompanied by expanding rings of TNT to carpet-bomb a large area (ideal for surface annihilation).
>
> **RECURSION (Chain Reaction):** Creates devastating chain-detonation effects 

**Firing Mechanisms**  
> **By Command:** Fire immediately at the crosshair location or at specific X, Y, Z coordinates.  
>  
> **By Fishing Rod:** Players use a special Fishing Rod that is linked to a specific cannon. Using the rod (clicking) will signal the cannon to fire at the location the player is looking at or interacting with.

**Crafting**  
> Players can craft these Remote Controllers (Targeting Tools) using a Crafting Table if recipes are configured. This allows survival players to access orbital weaponry without needing admin commands.

![image](https://github.com/user-attachments/assets/31b13b0c-d8e2-4634-813e-280ae6d081d7)

## Commands
- `/cannon create <name> [payload]` - Create a cannon at your location
- `/cannon remove <name>` - Remove a cannon
- `/cannon list` - List all cannons
- `/cannon fire <cannon>` - Fire payload at crosshair
- `/cannon info <cannon>` - Show info for a cannon
- `/cannon target <cannon> <x> <y> <z>` - Fire at coordinates
- `/cannon give <player> <cannon>` - Give targeting tool for a cannon
- `/cannon set <cannon> <parameter> <value>` - Set a parameter for a cannon
- `/cannon reload` - Reload configuration

## Permissions
- `orbitalstrike.use.<cannon>`: Use orbital strike cannons
- `orbitalstrike.admin`: Create and manage cannon
