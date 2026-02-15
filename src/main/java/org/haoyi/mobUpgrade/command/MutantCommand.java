package org.haoyi.mobUpgrade.command;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.haoyi.mobUpgrade.MobUpgrade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MutantCommand implements CommandExecutor, TabCompleter {

    private final MobUpgrade plugin;

    public MutantCommand(MobUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("mobupgrade.admin")) {
            sender.sendMessage(plugin.getLangConfig().get("no-permission"));
            return true;
        }
        if (args.length < 1 || !args[0].equalsIgnoreCase("spawn")) {
            sender.sendMessage(plugin.getLangConfig().get("mutant-usage"));
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(plugin.getLangConfig().get("mutant-specify-type"));
            return true;
        }
        EntityType type;
        try {
            type = EntityType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.getLangConfig().get("invalid-mob-type"));
            return true;
        }
        if (!type.isAlive() || type == EntityType.PLAYER) {
            sender.sendMessage(plugin.getLangConfig().get("invalid-mob-type"));
            return true;
        }
        int day;
        if (args.length >= 3) {
            day = parseInt(args[2], plugin.getMainConfig().getDefaultSpawnDay());
        } else {
            day = plugin.getCurrentRealTimeDay();
        }
        day = Math.max(1, Math.min(day, plugin.getMainConfig().getMaxDayCap()));

        Location loc;
        World world;
        if (sender instanceof Player player) {
            loc = player.getLocation();
            world = player.getWorld();
        } else {
            sender.sendMessage(plugin.getLangConfig().get("player-only"));
            return true;
        }

        if (args.length >= 4) {
            World w = plugin.getServer().getWorld(args[3]);
            if (w != null) world = w;
        }

        loc = new Location(world, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        LivingEntity entity = plugin.getMutantMobManager().spawnMutant(type, loc, day);
        if (entity != null) {
            sender.sendMessage(plugin.getLangConfig().get("mutant-spawned")
                    .replace("<type>", type.name())
                    .replace("<day>", String.valueOf(day)));
        } else {
            sender.sendMessage(plugin.getLangConfig().get("spawn-failed"));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> out = new ArrayList<>();
        if (!sender.hasPermission("mobupgrade.admin")) return out;
        if (args.length == 1) {
            if ("spawn".startsWith(args[0].toLowerCase())) out.add("spawn");
            return out;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            String prefix = args[1].toUpperCase();
            return Arrays.stream(EntityType.values())
                    .filter(EntityType::isAlive)
                    .filter(t -> t != EntityType.PLAYER)
                    .map(Enum::name)
                    .filter(n -> n.startsWith(prefix))
                    .collect(Collectors.toList());
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("spawn")) {
            out.add("1");
            out.add("10");
            out.add("50");
            return out;
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("spawn")) {
            plugin.getServer().getWorlds().stream()
                    .map(World::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[3].toLowerCase()))
                    .forEach(out::add);
            return out;
        }
        return out;
    }

    private static int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
