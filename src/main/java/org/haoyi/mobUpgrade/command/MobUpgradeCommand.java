package org.haoyi.mobUpgrade.command;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.haoyi.mobUpgrade.MobUpgrade;
import org.haoyi.mobUpgrade.config.CustomDropConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MobUpgradeCommand implements CommandExecutor, TabCompleter {

    private final MobUpgrade plugin;

    public MobUpgradeCommand(MobUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            send(sender, "usage");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("mobupgrade.reload")) {
                    send(sender, "no-permission");
                    return true;
                }
                plugin.reload();
                send(sender, "reloaded");
                return true;
            }
            case "spawn" -> {
                if (!sender.hasPermission("mobupgrade.spawn")) {
                    send(sender, "no-permission");
                    return true;
                }
                send(sender, "spawn-use-mutant");
                return true;
            }
            case "list" -> {
                if (!sender.hasPermission("mobupgrade.use")) {
                    send(sender, "no-permission");
                    return true;
                }
                String types = String.join(", ", plugin.getMainConfig().getMobTypes());
                sender.sendMessage(plugin.getLangConfig().get("list-mobs") + ": " + types);
                return true;
            }
            case "info" -> {
                if (!sender.hasPermission("mobupgrade.use")) {
                    send(sender, "no-permission");
                    return true;
                }
                send(sender, "info-version", "<version>", plugin.getDescription().getVersion());
                sender.sendMessage(plugin.getLangConfig().get("info-worlds") + ": " + String.join(", ", plugin.getMainConfig().getSpawnWorlds()));
                return true;
            }
            case "customdrop" -> {
                handleCustomDrop(sender, args);
                return true;
            }
            default -> {
                send(sender, "usage");
                return true;
            }
        }
    }

    private void handleCustomDrop(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mobupgrade.customdrop")) {
            send(sender, "no-permission");
            return;
        }
        if (plugin.getCustomDropManager() == null) {
            sender.sendMessage(plugin.getLangConfig().get("customdrop-not-available"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(plugin.getLangConfig().get("customdrop-usage"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "add" -> {
                if (args.length < 3) {
                    sender.sendMessage(plugin.getLangConfig().get("customdrop-usage"));
                    return;
                }
                Material mat = Material.matchMaterial(args[2].toUpperCase());
                if (mat == null || !mat.isItem()) {
                    sender.sendMessage(plugin.getLangConfig().get("customdrop-invalid-material"));
                    return;
                }
                double chance = args.length > 3 ? parseDouble(args[3], 0.1) : 0.1;
                int minA = args.length > 4 ? parseInt(args[4], 1) : 1;
                int maxA = args.length > 5 ? parseInt(args[5], 1) : 1;
                int minDay = args.length > 6 ? parseInt(args[6], 0) : 0;
                CustomDropConfig drop = new CustomDropConfig(mat, chance, minA, maxA, minDay);
                if (plugin.getCustomDropManager().addDrop(drop)) {
                    sender.sendMessage(plugin.getLangConfig().get("customdrop-added")
                            .replace("<material>", mat.name())
                            .replace("<chance>", String.valueOf(chance)));
                } else {
                    send(sender, "customdrop-add-failed");
                }
            }
            case "list" -> {
                List<CustomDropConfig> drops = plugin.getCustomDropManager().getDrops();
                if (drops.isEmpty()) {
                    sender.sendMessage(plugin.getLangConfig().get("customdrop-list-empty"));
                    return;
                }
                sender.sendMessage(plugin.getLangConfig().get("customdrop-list-header"));
                for (int i = 0; i < drops.size(); i++) {
                    CustomDropConfig d = drops.get(i);
                    sender.sendMessage("  " + i + ": " + d.material().name() + " chance=" + d.chance() + " min=" + d.minAmount() + " max=" + d.maxAmount() + " min-day=" + d.minDay());
                }
            }
            case "remove" -> {
                if (args.length < 3) {
                    sender.sendMessage(plugin.getLangConfig().get("customdrop-usage"));
                    return;
                }
                int idx = parseInt(args[2], -1);
                CustomDropConfig removed = plugin.getCustomDropManager().removeDrop(idx);
                if (removed != null) {
                    sender.sendMessage(plugin.getLangConfig().get("customdrop-removed").replace("<material>", removed.material().name()));
                } else {
                    sender.sendMessage(plugin.getLangConfig().get("customdrop-remove-failed"));
                }
            }
            default -> sender.sendMessage(plugin.getLangConfig().get("customdrop-usage"));
        }
    }

    private static double parseDouble(String s, double def) {
        try { return Double.parseDouble(s); } catch (NumberFormatException e) { return def; }
    }
    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String a = args[0].toLowerCase();
            for (String sub : List.of("reload", "spawn", "list", "info", "customdrop")) {
                if (sub.startsWith(a)) out.add(sub);
            }
        } else if (args.length == 2 && "customdrop".equalsIgnoreCase(args[0]) && sender.hasPermission("mobupgrade.customdrop")) {
            String a = args[1].toLowerCase();
            for (String sub : List.of("add", "list", "remove")) {
                if (sub.startsWith(a)) out.add(sub);
            }
        } else if (args.length == 3 && "customdrop".equalsIgnoreCase(args[0]) && "add".equalsIgnoreCase(args[1])) {
            String a = args[2].toUpperCase();
            out.addAll(Arrays.stream(Material.values()).filter(Material::isItem).map(Material::name).filter(n -> n.startsWith(a)).limit(30).collect(Collectors.toList()));
        }
        return out;
    }

    private void send(CommandSender sender, String key, String... placeholders) {
        String msg = plugin.getLangConfig().get(key);
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            msg = msg.replace(placeholders[i], placeholders[i + 1]);
        }
        sender.sendMessage(msg);
    }
}
