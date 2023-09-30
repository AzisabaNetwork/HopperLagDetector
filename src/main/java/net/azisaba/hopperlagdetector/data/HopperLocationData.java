package net.azisaba.hopperlagdetector.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class HopperLocationData {
    private final int tick;
    private final long timestamp;
    private final Location location;
    private final double elapsed;

    public HopperLocationData(@NotNull Location location, double elapsed) {
        this(Bukkit.getCurrentTick(), System.currentTimeMillis(), location, elapsed);
    }

    public HopperLocationData(int tick, long timestamp, @NotNull Location location, double elapsed) {
        this.tick = tick;
        this.timestamp = timestamp;
        this.location = location;
        this.elapsed = elapsed;
    }

    public int getTick() {
        return tick;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public @NotNull Location getLocation() {
        return location;
    }

    public double getElapsed() {
        return elapsed;
    }
}
