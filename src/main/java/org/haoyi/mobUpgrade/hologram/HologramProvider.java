package org.haoyi.mobUpgrade.hologram;

import org.bukkit.entity.LivingEntity;
import org.haoyi.mobUpgrade.MobUpgrade;

/** Hologram above mutant mobs; supports DecentHolograms, FancyHolograms, Holographics via reflection. */
public abstract class HologramProvider {

    public abstract String getName();

    public abstract void createMobHologram(LivingEntity entity, int day, double currentHp, double maxHp);

    public abstract void updateMobHologram(LivingEntity entity, int day, double currentHp, double maxHp);

    public abstract void removeMobHologram(LivingEntity entity);

    public abstract void cleanup();

    public static HologramProvider detect(MobUpgrade plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("DecentHolograms") != null) {
            try {
                return new DecentHologramsProvider(plugin);
            } catch (Throwable ignored) {
            }
        }
        if (plugin.getServer().getPluginManager().getPlugin("FancyHolograms") != null) {
            try {
                return new FancyHologramsProvider(plugin);
            } catch (Throwable ignored) {
            }
        }
        if (plugin.getServer().getPluginManager().getPlugin("Holographics") != null) {
            try {
                return new HolographicsProvider(plugin);
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    protected String buildBar(double current, double max, int length, String filled, String empty) {
        if (max <= 0) max = 1;
        int filledCount = (int) Math.round((current / max) * length);
        filledCount = Math.max(0, Math.min(length, filledCount));
        return filled.repeat(filledCount) + empty.repeat(length - filledCount);
    }
}
