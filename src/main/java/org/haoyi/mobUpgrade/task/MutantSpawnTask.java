package org.haoyi.mobUpgrade.task;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.haoyi.mobUpgrade.MobUpgrade;
import org.haoyi.mobUpgrade.scheduler.FoliaCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MutantSpawnTask implements Runnable {

    private final MobUpgrade plugin;
    private final Random random = new Random();
    private Object scheduledTask;

    public MutantSpawnTask(MobUpgrade plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (scheduledTask != null) return;
        if (!plugin.getMainConfig().isSpawnEnabled()) return;
        long interval = plugin.getMainConfig().getSpawnIntervalTicks();
        scheduledTask = FoliaCompat.runGlobalTimer(plugin, this, interval, interval);
    }

    public void cancel() {
        if (scheduledTask != null) {
            FoliaCompat.cancelGlobalTask(scheduledTask);
            scheduledTask = null;
        }
    }

    @Override
    public void run() {
        if (!plugin.getMainConfig().isSpawnEnabled()) return;
        Set<World> worlds = plugin.getMutantMobManager().getSpawnWorlds();
        List<String> types = plugin.getMainConfig().getMobTypes();
        if (worlds.isEmpty() || types.isEmpty()) return;

        if (FoliaCompat.isFolia()) {
            List<? extends Player> online = new ArrayList<>(plugin.getServer().getOnlinePlayers());
            int max = Math.min(3, online.size());
            for (int i = 0; i < max; i++) {
                Player p = online.get(random.nextInt(online.size()));
                FoliaCompat.runAtEntity(plugin, p, () -> trySpawnNearPlayer(p, worlds, types));
            }
            return;
        }

        int day = pickSpawnDay();
        int attempts = plugin.getMainConfig().getPerChunkAttempts();
        int maxSpawnsPerRun = plugin.getMainConfig().getMaxSpawnsPerRun();
        int minDist = Math.max(0, plugin.getMainConfig().getMinPlayerDistance());
        int spawned = 0;

        for (int run = 0; run < maxSpawnsPerRun; run++) {
            World world = plugin.getMutantMobManager().getRandomSpawnWorld(random);
            if (world == null) break;
            List<Player> players = world.getPlayers();
            if (players.isEmpty()) continue;
            if (!plugin.getMutantMobManager().canSpawnInWorld(world)) continue;
            int tries = Math.min(3, players.size());
            for (int i = 0; i < tries; i++) {
                Player p = players.get(random.nextInt(players.size()));
                Chunk chunk = p.getLocation().getChunk();
                int cx = chunk.getX() + random.nextInt(5) - 2;
                int cz = chunk.getZ() + random.nextInt(5) - 2;
                Chunk target = world.getChunkAt(cx, cz);
                if (!target.isLoaded() || !plugin.getMutantMobManager().canSpawnInChunk(target)) continue;
                for (int a = 0; a < attempts; a++) {
                    String typeName = types.get(random.nextInt(types.size()));
                    EntityType type;
                    try {
                        type = EntityType.valueOf(typeName);
                    } catch (IllegalArgumentException ignored) {
                        continue;
                    }
                    if (!type.isAlive() || type == EntityType.PLAYER) continue;
                    int x = target.getX() * 16 + random.nextInt(16);
                    int z = target.getZ() * 16 + random.nextInt(16);
                    int y = world.getHighestBlockYAt(x, z) + 1;
                    if (y <= world.getMinHeight() + 1) continue;
                    Location loc = new Location(world, x + 0.5, y, z + 0.5);
                    if (minDist > 0 && world.getPlayers().stream().noneMatch(pl -> pl.getLocation().distance(loc) <= minDist)) continue;
                    if (loc.getBlock().getLightLevel() > 7 && type != EntityType.ZOMBIE && type != EntityType.SKELETON) continue;
                    if (plugin.getMutantMobManager().spawnMutant(type, loc, day) != null) spawned++;
                    break;
                }
                break;
            }
        }
    }

    private void trySpawnNearPlayer(Player p, Set<World> worlds, List<String> types) {
        World world = p.getWorld();
        if (!worlds.contains(world) || !plugin.getMutantMobManager().canSpawnInWorld(world)) return;
        if (types.isEmpty()) return;
        int day = pickSpawnDay();
        int minDist = Math.max(0, plugin.getMainConfig().getMinPlayerDistance());
        int attempts = plugin.getMainConfig().getPerChunkAttempts();
        Chunk chunk = p.getLocation().getChunk();
        int cx = chunk.getX() + random.nextInt(5) - 2;
        int cz = chunk.getZ() + random.nextInt(5) - 2;
        Chunk target = world.getChunkAt(cx, cz);
        if (!target.isLoaded() || !plugin.getMutantMobManager().canSpawnInChunk(target)) return;
        for (int a = 0; a < attempts; a++) {
            String typeName = types.get(random.nextInt(types.size()));
            EntityType type;
            try {
                type = EntityType.valueOf(typeName);
            } catch (IllegalArgumentException ignored) {
                continue;
            }
            if (!type.isAlive() || type == EntityType.PLAYER) continue;
            int x = target.getX() * 16 + random.nextInt(16);
            int z = target.getZ() * 16 + random.nextInt(16);
            final int fx = x;
            final int fz = z;
            final EntityType fType = type;
            final int fDay = day;
            final World fWorld = world;
            final int fMinDist = minDist;
            Runnable doSpawn = () -> {
                int y = fWorld.getHighestBlockYAt(fx, fz) + 1;
                if (y <= fWorld.getMinHeight() + 1) return;
                Location loc = new Location(fWorld, fx + 0.5, y, fz + 0.5);
                if (fMinDist > 0) {
                    boolean anyNear = false;
                    for (Player pl : fWorld.getPlayers()) {
                        if (pl.getWorld().equals(fWorld) && pl.getLocation().distance(loc) <= fMinDist) {
                            anyNear = true;
                            break;
                        }
                    }
                    if (!anyNear) return;
                }
                if (loc.getBlock().getLightLevel() > 7 && fType != EntityType.ZOMBIE && fType != EntityType.SKELETON) return;
                plugin.getMutantMobManager().spawnMutant(fType, loc, fDay);
            };
            FoliaCompat.runAtRegion(plugin, world, target.getX(), target.getZ(), doSpawn);
            break;
        }
    }

    private int pickSpawnDay() {
        var cfg = plugin.getMainConfig();
        if ("real_world".equalsIgnoreCase(cfg.getTimeMode())) {
            return plugin.getCurrentRealTimeDay();
        }
        int day;
        if (cfg.isSpawnDayCustomEnabled()) {
            int min = Math.min(cfg.getSpawnDayMin(), cfg.getSpawnDayMax());
            int max = Math.max(cfg.getSpawnDayMin(), cfg.getSpawnDayMax());
            day = min == max ? min : min + random.nextInt(max - min + 1);
        } else {
            day = cfg.getDefaultSpawnDay();
        }
        return Math.max(1, Math.min(day, cfg.getMaxDayCap()));
    }
}
