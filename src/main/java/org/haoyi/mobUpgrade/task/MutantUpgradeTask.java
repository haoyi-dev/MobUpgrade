package org.haoyi.mobUpgrade.task;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.haoyi.mobUpgrade.MobUpgrade;
import org.haoyi.mobUpgrade.model.MutantData;
import org.haoyi.mobUpgrade.scheduler.FoliaCompat;

import java.util.Set;

public class MutantUpgradeTask implements Runnable {

    private final MobUpgrade plugin;
    private Object scheduledTask;

    public MutantUpgradeTask(MobUpgrade plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (scheduledTask != null) return;
        long interval = plugin.getMainConfig().getUpgradeExistingIntervalTicks();
        if (interval <= 0) return;
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
        if (!"real_world".equalsIgnoreCase(plugin.getMainConfig().getTimeMode())) return;
        int currentDay = plugin.getCurrentRealTimeDay();
        Set<World> worlds = plugin.getMutantMobManager().getSpawnWorlds();
        if (FoliaCompat.isFolia()) {
            for (World world : worlds) {
                for (Player player : world.getPlayers()) {
                    FoliaCompat.runAtEntity(plugin, player, () -> {
                        Chunk chunk = player.getLocation().getChunk();
                        int cx = chunk.getX();
                        int cz = chunk.getZ();
                        for (int dx = -2; dx <= 2; dx++) {
                            for (int dz = -2; dz <= 2; dz++) {
                                int fx = cx + dx;
                                int fz = cz + dz;
                                FoliaCompat.runAtRegion(plugin, world, fx, fz, () -> upgradeChunk(world, fx, fz, currentDay));
                            }
                        }
                    });
                }
            }
        } else {
            for (World world : worlds) {
                world.getLivingEntities().stream()
                        .filter(MutantData::isMutant)
                        .filter(e -> MutantData.getDay(e) < currentDay)
                        .forEach(e -> plugin.getMutantMobManager().applyMutant(e, currentDay));
            }
        }
    }

    private void upgradeChunk(World world, int chunkX, int chunkZ, int currentDay) {
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof LivingEntity le && MutantData.isMutant(le) && MutantData.getDay(le) < currentDay) {
                plugin.getMutantMobManager().applyMutant(le, currentDay);
            }
        }
    }
}
