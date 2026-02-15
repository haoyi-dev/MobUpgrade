package org.haoyi.mobUpgrade.hologram;

import org.bukkit.entity.LivingEntity;
import org.haoyi.mobUpgrade.MobUpgrade;

/** Placeholder: Holographics API not integrated yet. */
public class HolographicsProvider extends HologramProvider {

    private final MobUpgrade plugin;

    public HolographicsProvider(MobUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "Holographics";
    }

    @Override
    public void createMobHologram(LivingEntity entity, int day, double currentHp, double maxHp) {
    }

    @Override
    public void updateMobHologram(LivingEntity entity, int day, double currentHp, double maxHp) {
    }

    @Override
    public void removeMobHologram(LivingEntity entity) {
    }

    @Override
    public void cleanup() {
    }
}
