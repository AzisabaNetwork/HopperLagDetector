package net.azisaba.hopperlagdetector;

import net.azisaba.hopperlagdetector.command.HopperLagDetectorCommand;
import net.azisaba.hopperlagdetector.listener.InventoryListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class HopperLagDetector extends JavaPlugin {
    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
        Bukkit.getScheduler().runTaskTimer(this, () ->
                InventoryListener.data.removeIf(data -> System.currentTimeMillis() - data.getTimestamp() > 1000 * 60 * 5), 20, 20);
        Objects.requireNonNull(Bukkit.getPluginCommand("hopperlagdetector")).setExecutor(new HopperLagDetectorCommand());
    }
}
