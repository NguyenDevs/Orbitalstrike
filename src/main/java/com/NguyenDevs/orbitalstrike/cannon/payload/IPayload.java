package com.NguyenDevs.orbitalstrike.cannon.payload;

import com.NguyenDevs.orbitalstrike.cannon.Cannon;
import org.bukkit.Location;
import org.bukkit.World;

public interface IPayload {
    void execute(World world, Location target, Cannon cannon);
}
