package net.azisaba.hopperlagdetector.command;

import net.azisaba.hopperlagdetector.data.HopperLocationData;
import net.azisaba.hopperlagdetector.listener.InventoryListener;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HopperLagDetectorCommand implements TabExecutor {
    private static final String PREFIX = " | " + ChatColor.WHITE + " ";
    private static final int maxEntriesInList = 10;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            int page = 1;
            if (args.length > 1) {
                page = Math.max(1, Integer.parseInt(args[1]));
            }
            Map<Location, Integer> count = new HashMap<>();
            for (HopperLocationData data : InventoryListener.data) {
                count.put(data.getLocation(), count.getOrDefault(data.getLocation(), 0) + 1);
            }
            int maxPage = (int) Math.ceil(count.size() / (double) maxEntriesInList);
            double s5 = Math.round(InventoryListener.data.stream().filter(data -> System.currentTimeMillis() - data.getTimestamp() <= 5000).count() / 5.0 * 10) / 10.0;
            double s10 = Math.round(InventoryListener.data.stream().filter(data -> System.currentTimeMillis() - data.getTimestamp() <= 10000).count() / 10.0 * 10) / 10.0;
            double s60 = Math.round(InventoryListener.data.stream().filter(data -> System.currentTimeMillis() - data.getTimestamp() <= 60000).count() / 60.0 * 10) / 10.0;
            String calls = ChatColor.GOLD.toString() + s5 + ", " + s10 + ", " + s60;
            sender.sendMessage(PREFIX + ChatColor.YELLOW + ChatColor.BOLD + "Event calls/s from last 5s, 10s, 1m: " + calls);
            sender.sendMessage(PREFIX + ChatColor.YELLOW + ChatColor.BOLD + "Data from past 5 minutes:");
            int idx = 0;
            for (Map.Entry<Location, Integer> entry : count.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).collect(Collectors.toList())) {
                if (idx > (page - 1) * maxEntriesInList && idx < page * maxEntriesInList) {
                    String worldName = Objects.requireNonNull(entry.getKey().getWorld()).getName();
                    int blockX = entry.getKey().getBlockX();
                    int blockY = entry.getKey().getBlockY();
                    int blockZ = entry.getKey().getBlockZ();
                    String loc = blockX + ", " + blockY + ", " + blockZ;
                    TextComponent component = new TextComponent(PREFIX);
                    TextComponent locComponent = new TextComponent(ChatColor.GRAY + "(" + worldName + ") " + ChatColor.GOLD + loc);
                    String cmd = "/" + command.getName() + " teleport " + worldName + " " + (blockX + 0.5) + " " + blockY + " " + (blockZ + 0.5);
                    locComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
                    String tooltip = ChatColor.YELLOW.toString() + ChatColor.BOLD + "Click to teleport (" + loc + ")";
                    locComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(tooltip)));
                    component.addExtra(locComponent);
                    component.addExtra(ChatColor.WHITE + ": " + ChatColor.RED + entry.getValue());
                    sender.spigot().sendMessage(component);
                }
                idx++;
            }
            if (count.size() > page * maxEntriesInList) {
                sender.sendMessage(PREFIX + ChatColor.GRAY + "(...and " + (count.size() - page * maxEntriesInList) + " more)");
            }
            sender.sendMessage(PREFIX + ChatColor.GRAY + "Page " + ChatColor.YELLOW + page + ChatColor.GRAY + "/" + ChatColor.YELLOW + maxPage
                    + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY + "Entries: " + ChatColor.YELLOW + count.size());
            TextComponent component = new TextComponent(PREFIX);
            TextComponent prev;
            if (page == 1) {
                prev = new TextComponent("- <<");
                prev.setColor(ChatColor.GRAY.asBungee());
            } else {
                prev = new TextComponent((page - 1) + " <<");
                prev.setColor(ChatColor.YELLOW.asBungee());
                prev.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hld list " + (page - 1)));
                prev.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.YELLOW.toString() + ChatColor.BOLD + "<- Previous page")));
            }
            TextComponent next;
            if (page >= maxPage) {
                next = new TextComponent(">> -");
                next.setColor(ChatColor.GRAY.asBungee());
            } else {
                next = new TextComponent(">> " + (page + 1));
                next.setColor(ChatColor.YELLOW.asBungee());
                next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hld list " + (page + 1)));
                next.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Next page ->")));
            }
            TextComponent refresh = new TextComponent(" " + page + " ");
            refresh.setColor(ChatColor.GREEN.asBungee());
            refresh.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hld list " + page));
            refresh.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GREEN.toString() + ChatColor.BOLD + "♻ Click to refresh")));
            component.addExtra(prev);
            component.addExtra(refresh);
            component.addExtra(next);
            sender.spigot().sendMessage(component);
        } else if (args[0].equalsIgnoreCase("times")) {
            int page = 1;
            if (args.length > 1) {
                page = Math.max(1, Integer.parseInt(args[1]));
            }
            int maxPage = (int) Math.ceil(InventoryListener.times.size() / (double) maxEntriesInList);
            sender.sendMessage(PREFIX + ChatColor.YELLOW + ChatColor.BOLD + "Times from past 5 minutes:");
            int idx = 0;
            for (Map.Entry<Location, Map.Entry<Integer, Double>> entry : InventoryListener.times.entrySet().stream().sorted((a, b) -> Double.compare(b.getValue().getValue(), a.getValue().getValue())).collect(Collectors.toList())) {
                if (idx > (page - 1) * maxEntriesInList && idx < page * maxEntriesInList) {
                    double count = entry.getValue().getKey();
                    String worldName = Objects.requireNonNull(entry.getKey().getWorld()).getName();
                    int blockX = entry.getKey().getBlockX();
                    int blockY = entry.getKey().getBlockY();
                    int blockZ = entry.getKey().getBlockZ();
                    String loc = blockX + ", " + blockY + ", " + blockZ;
                    TextComponent component = new TextComponent(PREFIX);
                    TextComponent locComponent = new TextComponent(ChatColor.GRAY + "(" + worldName + ") " + ChatColor.GOLD + loc);
                    String cmd = "/" + command.getName() + " teleport " + worldName + " " + (blockX + 0.5) + " " + blockY + " " + (blockZ + 0.5);
                    locComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd));
                    String tooltip = ChatColor.YELLOW.toString() + ChatColor.BOLD + "Click to teleport (" + loc + ")";
                    locComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(tooltip)));
                    component.addExtra(locComponent);
                    double avg = (Math.round(entry.getValue().getValue() / count * 100.0) / 100.0);
                    ChatColor avgColor;
                    if (avg < 0.5) {
                        avgColor = ChatColor.GREEN;
                    } else if (avg < 1) {
                        avgColor = ChatColor.YELLOW;
                    } else {
                        avgColor = ChatColor.RED;
                    }
                    component.addExtra(ChatColor.WHITE + ": " + avgColor + (Math.round(entry.getValue().getValue() * 100.0) / 100.0) + " " + ChatColor.YELLOW + "ms" +
                            ChatColor.GRAY + " (avg. " + avgColor + avg + ChatColor.GRAY + " ms per call, called " + count + " times)");
                    sender.spigot().sendMessage(component);
                }
                idx++;
            }
            if (InventoryListener.times.size() > page * maxEntriesInList) {
                sender.sendMessage(PREFIX + ChatColor.GRAY + "(...and " + (InventoryListener.times.size() - page * maxEntriesInList) + " more)");
            }
            sender.sendMessage(PREFIX + ChatColor.GRAY + "Page " + ChatColor.YELLOW + page + ChatColor.GRAY + "/" + ChatColor.YELLOW + maxPage
                    + ChatColor.DARK_GRAY + " | " + ChatColor.GRAY + "Entries: " + ChatColor.YELLOW + InventoryListener.times.size());
            TextComponent component = new TextComponent(PREFIX);
            TextComponent prev;
            if (page == 1) {
                prev = new TextComponent("- <<");
                prev.setColor(ChatColor.GRAY.asBungee());
            } else {
                prev = new TextComponent((page - 1) + " <<");
                prev.setColor(ChatColor.YELLOW.asBungee());
                prev.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hld times " + (page - 1)));
                prev.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.YELLOW.toString() + ChatColor.BOLD + "<- Previous page")));
            }
            TextComponent next;
            if (page >= maxPage) {
                next = new TextComponent(">> -");
                next.setColor(ChatColor.GRAY.asBungee());
            } else {
                next = new TextComponent(">> " + (page + 1));
                next.setColor(ChatColor.YELLOW.asBungee());
                next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hld times " + (page + 1)));
                next.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.YELLOW.toString() + ChatColor.BOLD + "Next page ->")));
            }
            TextComponent refresh = new TextComponent(" " + page + " ");
            refresh.setColor(ChatColor.GREEN.asBungee());
            refresh.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/hld times " + page));
            refresh.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(ChatColor.GREEN.toString() + ChatColor.BOLD + "♻ Click to refresh")));
            component.addExtra(prev);
            component.addExtra(refresh);
            component.addExtra(next);
            sender.spigot().sendMessage(component);
        } else if (args[0].equalsIgnoreCase("clear")) {
            InventoryListener.data.clear();
            InventoryListener.times.clear();
            sender.sendMessage(PREFIX + ChatColor.YELLOW + ChatColor.BOLD + "Cleared the data.");
        } else if (args[0].equalsIgnoreCase("mspt")) {
            List<HopperLocationData> s5 = InventoryListener.data.stream().filter(data -> System.currentTimeMillis() - data.getTimestamp() <= 5000).collect(Collectors.toList());
            List<HopperLocationData> s10 = InventoryListener.data.stream().filter(data -> System.currentTimeMillis() - data.getTimestamp() <= 10000).collect(Collectors.toList());
            List<HopperLocationData> s60 = InventoryListener.data.stream().filter(data -> System.currentTimeMillis() - data.getTimestamp() <= 60000).collect(Collectors.toList());
            Consumer<Predicate<HopperLocationData>> showMspt = (predicate) -> {
                double msptS5 = s5.stream().filter(predicate).mapToDouble(HopperLocationData::getElapsed).sum() / 5;
                double msptS10 = s10.stream().filter(predicate).mapToDouble(HopperLocationData::getElapsed).sum() / 10;
                double msptS60 = s60.stream().filter(predicate).mapToDouble(HopperLocationData::getElapsed).sum() / 60;
                String times = ChatColor.GOLD.toString() + msptS5 + ", " + msptS10 + ", " + msptS60;
                sender.sendMessage(PREFIX + ChatColor.YELLOW + " " + ChatColor.BOLD + " 5s/10s/1m: " + times);
            };
            sender.sendMessage(PREFIX + ChatColor.YELLOW + ChatColor.BOLD + "Global:");
            showMspt.accept(d -> true);
            for (World world : Bukkit.getWorlds()) {
                if (world.getLoadedChunks().length == 0) continue;
                sender.sendMessage(PREFIX + ChatColor.YELLOW + ChatColor.BOLD + "World <" + world.getName() + ">:");
                showMspt.accept(d -> d.getLocation().getWorld() == world);
            }
        } else if (args[0].equalsIgnoreCase("enable-hopper") || args[0].equalsIgnoreCase("disable-hopper")) {
            if (args.length == 1) {
                sender.sendMessage(ChatColor.RED + "/hld " + args[0] + " <world>");
                return true;
            }
            boolean enable = args[0].equalsIgnoreCase("enable-hopper");
            String name = args[1].toLowerCase();
            if (enable) {
                InventoryListener.disableHopperOnWorld.remove(name);
                sender.sendMessage(PREFIX + ChatColor.GREEN + "Enabled hopper on world " + name);
            } else {
                InventoryListener.disableHopperOnWorld.add(name);
                sender.sendMessage(PREFIX + ChatColor.GREEN + "Disabled hopper on world " + name);
            }
        } else if (args[0].equalsIgnoreCase("teleport")) {
            if (args.length <= 4) {
                return true;
            }
            World world = Bukkit.getWorld(args[1]);
            if (world == null) {
                return true;
            }
            double x = Double.parseDouble(args[2]);
            double y = Double.parseDouble(args[3]);
            double z = Double.parseDouble(args[4]);
            if (sender instanceof Player) {
                ((Player) sender).teleport(new Location(world, x, y, z));
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(Stream.of("list", "times", "clear", "mspt", "enable-hopper", "disable-hopper"), args[0]);
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("enable-hopper") || args[0].equalsIgnoreCase("disable-hopper")) {
                return filter(Bukkit.getWorlds().stream().map(World::getName), args[1]);
            }
        }
        return Collections.emptyList();
    }

    private static List<String> filter(@NotNull Stream<String> stream, @NotNull String arg) {
        return stream.filter(s -> s.toLowerCase(Locale.ROOT).startsWith(arg.toLowerCase(Locale.ROOT))).collect(Collectors.toList());
    }
}
