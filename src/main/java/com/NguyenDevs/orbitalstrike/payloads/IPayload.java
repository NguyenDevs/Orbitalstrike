package com.NguyenDevs.orbitalstrike.payloads;

import com.NguyenDevs.orbitalstrike.models.Cannon;
import org.bukkit.Location;
import org.bukkit.World;

public interface IPayload {
    void execute(World world, Location target, Cannon cannon);
}
