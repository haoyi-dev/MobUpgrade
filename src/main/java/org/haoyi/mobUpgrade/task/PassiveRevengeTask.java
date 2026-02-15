package org.haoyi.mobUpgrade.task;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.haoyi.mobUpgrade.MobUpgrade;
import org.haoyi.mobUpgrade.listener.MobUpgradeListener;
import org.haoyi.mobUpgrade.model.MutantData;
import org.haoyi.mobUpgrade.scheduler.FoliaCompat;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class PassiveRevengeTask implements Runnable {

    private final MobUpgrade plugin;
    private final MobUpgradeListener listener;
    private Object scheduledTask;

    public PassiveRevengeTask(MobUpgrade plugin, MobUpgradeListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }

    public void start() {
        if (scheduledTask != null) return;
        if (!plugin.getMainConfig().isPassiveRevengeEnabled()) return;
        long interval = plugin.getMainConfig().getPassiveRevengeIntervalTicks();
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
        if (!plugin.getMainConfig().isPassiveRevengeEnabled()) return;
        Map<UUID, MobUpgradeListener.RevengeEntry> map = listener.getPassiveRevengeTargets();
        if (map.isEmpty()) return;
        long currentTick = System.currentTimeMillis() / 50;
        double range = plugin.getMainConfig().getPassiveRevengeRange();
        double damagePerDay = plugin.getMainConfig().getPassiveDamagePerDay();

        Iterator<Map.Entry<UUID, MobUpgradeListener.RevengeEntry>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, MobUpgradeListener.RevengeEntry> e = it.next();
            UUID entityUuid = e.getKey();
            MobUpgradeListener.RevengeEntry entry = e.getValue();
            if (currentTick >= entry.expireAtTick) {
                it.remove();
                continue;
            }
            World world = Bukkit.getWorld(entry.worldName);
            if (world == null) {
                it.remove();
                continue;
            }
            LivingEntity mob = findEntity(world, entityUuid);
            if (mob == null || !mob.isValid() || mob.isDead() || !MutantData.isMutant(mob)) {
                it.remove();
                continue;
            }
            Player damager = Bukkit.getPlayer(entry.damagerUuid);
            if (damager == null || !damager.isOnline() || !damager.getWorld().equals(world)) continue;
            if (damager.getLocation().distance(mob.getLocation()) > range) continue;
            int day = MutantData.getDay(mob);
            double damage = Math.max(0.5, damagePerDay * day);
            Runnable deal = () -> {
                if (mob.isValid() && !mob.isDead() && damager.isOnline())
                    damager.damage(damage, mob);
            };
            FoliaCompat.runAtEntity(plugin, mob, deal);
        }
    }

    private LivingEntity findEntity(World world, UUID uuid) {
        for (Entity entity : world.getEntities()) {
            if (entity.getUniqueId().equals(uuid) && entity instanceof LivingEntity le)
                return le;
        }
        return null;
    }
}
