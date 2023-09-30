package net.azisaba.hopperlagdetector.listener;

import net.azisaba.hopperlagdetector.data.HopperLocationData;
import org.bukkit.Location;
import org.bukkit.block.Hopper;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;

public class InventoryListener implements Listener {
    public static final List<HopperLocationData> data = new ArrayList<>();
    public static final Map<Location, Map.Entry<Integer, Double>> times = new HashMap<>();
    public static final Set<String> disableHopperOnWorld = new HashSet<>();
    private static final Map.Entry<Integer, Double> INITIAL_VALUE = new AbstractMap.SimpleImmutableEntry<>(0, 0.0);

    @EventHandler(ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent e) {
        double start = System.nanoTime();
        InventoryHolder holder = e.getInitiator().getHolder();
        double time = (System.nanoTime() - start) / 1_000_000;
        if (holder instanceof Hopper) {
            Hopper hopper = (Hopper) holder;
            Map.Entry<Integer, Double> entry = times.getOrDefault(hopper.getLocation(), INITIAL_VALUE);
            times.put(hopper.getLocation(), new AbstractMap.SimpleImmutableEntry<>(entry.getKey() + 1, entry.getValue() + time));
            data.add(new HopperLocationData(hopper.getLocation(), time));
        }
        if (holder instanceof HopperMinecart) {
            HopperMinecart hopper = (HopperMinecart) holder;
            Map.Entry<Integer, Double> entry = times.getOrDefault(hopper.getLocation(), INITIAL_VALUE);
            times.put(hopper.getLocation(), new AbstractMap.SimpleImmutableEntry<>(entry.getKey() + 1, entry.getValue() + time));
            data.add(new HopperLocationData(hopper.getLocation(), time));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void checkDisableHopper(InventoryMoveItemEvent e) {
        InventoryHolder holder = e.getInitiator().getHolder();
        if (holder instanceof Hopper) {
            Location loc = ((Hopper) holder).getLocation();
            if (disableHopperOnWorld.contains(loc.getWorld().getName().toLowerCase())) {
                e.setCancelled(true);
            }
        }
        if (holder instanceof HopperMinecart) {
            Location loc = ((HopperMinecart) holder).getLocation();
            if (disableHopperOnWorld.contains(loc.getWorld().getName().toLowerCase())) {
                e.setCancelled(true);
            }
        }
    }
}
