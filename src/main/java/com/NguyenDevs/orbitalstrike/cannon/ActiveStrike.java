package com.NguyenDevs.orbitalstrike.cannon;

import com.NguyenDevs.orbitalstrike.utils.StrikeData;
import org.bukkit.Location;

public class ActiveStrike {
    private final Cannon cannon;
    private final StrikeData data;
    private final Location target;
    private long startTime;

    public ActiveStrike(Cannon cannon, StrikeData data, Location target) {
        this.cannon = cannon;
        this.data = data;
        this.target = target;
        this.startTime = System.currentTimeMillis();
    }

    public Cannon getCannon() {
        return cannon;
    }

    public StrikeData getData() {
        return data;
    }

    public Location getTarget() {
        return target;
    }
}
