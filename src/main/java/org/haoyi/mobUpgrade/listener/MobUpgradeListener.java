package org.haoyi.mobUpgrade.listener;

import org.bukkit.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.haoyi.mobUpgrade.MobUpgrade;
import org.haoyi.mobUpgrade.config.CustomDropConfig;
import org.haoyi.mobUpgrade.config.MainConfig;
import org.haoyi.mobUpgrade.model.MutantData;
import org.haoyi.mobUpgrade.scheduler.FoliaCompat;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MobUpgradeListener implements Listener {

    private final MobUpgrade plugin;
    private final MainConfig config;
    private final Map<UUID, Long> lastHologramUpdate = new ConcurrentHashMap<>();
    private final Map<UUID, RevengeEntry> passiveRevengeTargets = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public Map<UUID, RevengeEntry> getPassiveRevengeTargets() { return passiveRevengeTargets; }

    public static final class RevengeEntry {
        public final UUID damagerUuid;
        public final long expireAtTick;
        public final String worldName;

        public RevengeEntry(UUID damagerUuid, long expireAtTick, String worldName) {
            this.damagerUuid = damagerUuid;
            this.expireAtTick = expireAtTick;
            this.worldName = worldName;
        }
    }

    public MobUpgradeListener(MobUpgrade plugin) {
        this.plugin = plugin;
        this.config = plugin.getMainConfig();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof LivingEntity entity)) return;
        if (!MutantData.isMutant(entity)) return;
        if (config.isPassiveRevengeEnabled() && plugin.getMutantMobManager().isPassiveType(entity.getType())) {
            if (e instanceof EntityDamageByEntityEvent byEntity && byEntity.getDamager() instanceof Player damager) {
                long expire = (System.currentTimeMillis() / 50) + config.getPassiveRevengeDurationTicks();
                passiveRevengeTargets.put(entity.getUniqueId(), new RevengeEntry(damager.getUniqueId(), expire, entity.getWorld().getName()));
            }
        }
        if (plugin.getHologramProvider() == null || !config.isHologramEnabled()) return;

        long now = System.currentTimeMillis();
        int intervalMs = config.getHologramUpdateIntervalTicks() * 50;
        lastHologramUpdate.compute(entity.getUniqueId(), (uuid, last) -> {
            if (last != null && (now - last) < intervalMs) return last;
            FoliaCompat.runAtEntity(plugin, entity, () -> {
                if (entity.isValid() && !entity.isDead()) {
                    plugin.getMutantMobManager().updateHologram(entity);
                }
            });
            return now;
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(EntityDeathEvent e) {
        LivingEntity entity = e.getEntity();
        if (!MutantData.isMutant(entity)) return;

        int day = MutantData.getDay(entity);
        plugin.getMutantMobManager().unregisterMutant(entity);
        if (plugin.getHologramProvider() != null) {
            plugin.getHologramProvider().removeMobHologram(entity);
        }
        lastHologramUpdate.remove(entity.getUniqueId());
        passiveRevengeTargets.remove(entity.getUniqueId());

        if (config.isCustomDropsEnabled()) {
            java.util.List<CustomDropConfig> allDrops = new java.util.ArrayList<>();
            if (config.getCustomDrops() != null) allDrops.addAll(config.getCustomDrops());
            if (plugin.getCustomDropManager() != null) allDrops.addAll(plugin.getCustomDropManager().getDrops());
            for (CustomDropConfig drop : allDrops) {
                ItemStack item = drop.createDrop(day, random);
                if (item != null && !item.getType().isAir()) {
                    entity.getWorld().dropItemNaturally(entity.getLocation(), item);
                }
            }
        }

        Player killer = entity.getKiller();
        if (killer != null) {
            if (config.isLootMultiplierEnabled() && day > 0) {
                double mult = Math.min(config.getLootMaxMultiplier(), 1.0 + day * config.getLootBonusPerDay());
                for (ItemStack drop : e.getDrops()) {
                    if (drop.getType().isAir()) continue;
                    int extra = (int) Math.floor(drop.getAmount() * (mult - 1.0));
                    if (extra > 0) {
                        drop.setAmount(Math.min(drop.getMaxStackSize(), drop.getAmount() + extra));
                    }
                }
            }
            if (config.isXpMultiplierEnabled() && day > 0) {
                int base = e.getDroppedExp();
                double xpMult = Math.min(config.getXpMaxMultiplier(), 1.0 + day * config.getXpBonusPerDay());
                e.setDroppedExp((int) Math.ceil(base * xpMult));
            }
            if (config.getRewardBroadcastDayThreshold() > 0 && day >= config.getRewardBroadcastDayThreshold()) {
                String msg = plugin.getLangConfig().get("broadcast-kill")
                        .replace("<player>", killer.getName())
                        .replace("<day>", String.valueOf(day))
                        .replace("<mob>", entity.getType().name());
                Bukkit.broadcastMessage(msg);
            }
            if (config.isCustomCommandsEnabled() && config.getCustomCommands() != null && !config.getCustomCommands().isEmpty()) {
                Location loc = entity.getLocation();
                String worldName = entity.getWorld().getName();
                for (String cmd : config.getCustomCommands()) {
                    if (cmd == null || cmd.isBlank()) continue;
                    String run = cmd.replace("<player>", killer.getName())
                            .replace("<day>", String.valueOf(day))
                            .replace("<mob>", entity.getType().name())
                            .replace("<world>", worldName)
                            .replace("<x>", String.valueOf(loc.getBlockX()))
                            .replace("<y>", String.valueOf(loc.getBlockY()))
                            .replace("<z>", String.valueOf(loc.getBlockZ()));
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), run);
                }
            }
        }

        if (config.isDeathEffect()) {
            World w = entity.getWorld();
            Location loc = entity.getLocation();
            w.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1, 0.2, 0.2, 0.2, 0);
            w.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.8f);
        }
    }

}
