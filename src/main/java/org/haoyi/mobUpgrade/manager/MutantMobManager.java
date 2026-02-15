package org.haoyi.mobUpgrade.manager;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.inventory.ItemStack;
import org.haoyi.mobUpgrade.MobUpgrade;
import org.haoyi.mobUpgrade.config.MainConfig;
import org.haoyi.mobUpgrade.model.CustomModelProvider;
import org.haoyi.mobUpgrade.model.MutantData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MutantMobManager {

    private final MobUpgrade plugin;
    private final MainConfig config;
    private CustomModelProvider customModelProvider;
    private Set<World> cachedSpawnWorlds;
    private final java.util.Map<UUID, World> mutantWorldByUuid = new ConcurrentHashMap<>();
    private final java.util.Map<World, AtomicInteger> mutantCountByWorld = new ConcurrentHashMap<>();

    public MutantMobManager(MobUpgrade plugin) {
        this.plugin = plugin;
        this.config = plugin.getMainConfig();
        this.customModelProvider = new CustomModelProvider(plugin, config.getCustomModelItems());
    }

    public void reload() {
        cachedSpawnWorlds = null;
        customModelProvider = new CustomModelProvider(plugin, config.getCustomModelItems());
    }

    private Set<World> getSpawnWorldsCached() {
        if (cachedSpawnWorlds == null) {
            cachedSpawnWorlds = config.getSpawnWorlds().stream()
                    .map(w -> plugin.getServer().getWorld(w))
                    .filter(Objects::nonNull)
                    .filter(w -> !config.isExcludeTheEnd() || w.getEnvironment() != Environment.THE_END)
                    .collect(Collectors.toSet());
        }
        return cachedSpawnWorlds;
    }

    public int getMutantCountInWorld(World world) {
        AtomicInteger c = mutantCountByWorld.get(world);
        return c == null ? 0 : c.get();
    }

    public int countMutantsInChunk(Chunk chunk) {
        int n = 0;
        for (var entity : chunk.getEntities()) {
            if (entity instanceof LivingEntity le && MutantData.isMutant(le)) n++;
        }
        return n;
    }

    public boolean canSpawnInWorld(World world) {
        int max = config.getMaxMutantsPerWorld();
        if (max <= 0) return true;
        return getMutantCountInWorld(world) < max;
    }

    public boolean canSpawnInChunk(Chunk chunk) {
        int max = config.getMaxMutantsPerChunk();
        if (max <= 0) return true;
        return countMutantsInChunk(chunk) < max;
    }

    public void registerMutant(LivingEntity entity, World world) {
        mutantWorldByUuid.put(entity.getUniqueId(), world);
        mutantCountByWorld.computeIfAbsent(world, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public void unregisterMutant(LivingEntity entity) {
        World w = mutantWorldByUuid.remove(entity.getUniqueId());
        if (w != null) {
            AtomicInteger c = mutantCountByWorld.get(w);
            if (c != null) c.decrementAndGet();
        }
    }

    @SuppressWarnings("deprecation")
    public void applyMutant(LivingEntity entity, int day) {
        if (day < 1) day = 1;
        int cap = config.getMaxDayCap();
        day = Math.min(day, cap);
        MutantData.setDay(entity, day);

        var maxHpAttr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHpAttr == null) return;
        double baseMaxHp = maxHpAttr.getValue();
        double hpBonus = baseMaxHp * config.getHpPerDay() * day;
        double newMaxHp = baseMaxHp + hpBonus;
        maxHpAttr.setBaseValue(newMaxHp);
        entity.setHealth(Math.min(entity.getHealth(), newMaxHp));

        var damageAttr = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        if (damageAttr != null) {
            double baseDmg = damageAttr.getValue();
            double dmgBonus = baseDmg * config.getDamagePerDay() * day;
            damageAttr.setBaseValue(baseDmg + dmgBonus);
        }

        String name = formatName(entity, day, newMaxHp);
        entity.setCustomName(name);
        boolean showNameAbove = !(plugin.getHologramProvider() != null && config.isHologramEnabled());
        entity.setCustomNameVisible(showNameAbove);

        if (plugin.getHologramProvider() != null && config.isHologramEnabled()) {
            plugin.getHologramProvider().createMobHologram(entity, day, newMaxHp, newMaxHp);
        }

        if (config.isCustomModelEnabled() && entity instanceof Mob mob && customModelProvider != null) {
            ItemStack modelItem = customModelProvider.getRandomItem();
            if (modelItem != null && !modelItem.getType().isAir()) {
                String slot = config.getCustomModelSlot();
                if ("head".equalsIgnoreCase(slot)) {
                    mob.getEquipment().setHelmet(modelItem);
                    mob.getEquipment().setHelmetDropChance(config.isCustomModelDropOnDeath() ? 1.0f : 0f);
                }
            }
        }
    }

    public String formatName(LivingEntity entity, int day, double maxHp) {
        String raw = entity.getType().name().toLowerCase().replace("_", " ");
        String name = raw.substring(0, 1).toUpperCase() + raw.substring(1);
        return color(config.getNameFormat()
                .replace("{name}", name)
                .replace("{day}", String.valueOf(day))
                .replace("{hp}", String.format("%.0f", entity.getHealth()))
                .replace("{max_hp}", String.format("%.0f", maxHp)));
    }

    private static String color(String s) {
        return s == null ? "" : s.replace("&", "§");
    }

    public LivingEntity spawnMutant(EntityType type, Location loc, int day) {
        if (loc.getWorld() == null || !type.isAlive() || type == EntityType.PLAYER) return null;
        World world = loc.getWorld();
        if (!canSpawnInWorld(world)) return null;
        if (!canSpawnInChunk(loc.getChunk())) return null;

        LivingEntity entity = (LivingEntity) world.spawnEntity(loc, type);
        if (entity instanceof Mob mob) mob.setRemoveWhenFarAway(true);
        applyMutant(entity, day);
        registerMutant(entity, world);
        if (config.isSpawnEffect()) {
            world.spawnParticle(Particle.HAPPY_VILLAGER, loc.add(0, 1, 0), 12, 0.4, 0.5, 0.4, 0.1);
            world.playSound(loc, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 0.4f, 1.2f);
        }
        return entity;
    }

    public void updateHologram(LivingEntity entity) {
        if (!MutantData.isMutant(entity)) return;
        int day = MutantData.getDay(entity);
        double maxHp = Objects.requireNonNull(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
        if (plugin.getHologramProvider() != null && config.isHologramEnabled()) {
            plugin.getHologramProvider().updateMobHologram(entity, day, entity.getHealth(), maxHp);
        }
    }

    public List<String> getAllowedMobTypes() {
        return config.getMobTypes();
    }

    public boolean isAllowedType(EntityType type) {
        return config.getMobTypes().contains(type.name());
    }

    public boolean isPassiveType(EntityType type) {
        return config.getPassiveMobTypeNames().contains(type.name());
    }

    public Set<World> getSpawnWorlds() {
        return getSpawnWorldsCached();
    }

    public World getRandomSpawnWorld(java.util.Random random) {
        List<World> worlds = new ArrayList<>(getSpawnWorldsCached());
        if (worlds.isEmpty()) return null;
        var weights = config.getSpawnWorldWeights();
        if (weights == null || weights.isEmpty()) {
            return worlds.get(random.nextInt(worlds.size()));
        }
        int total = 0;
        for (World w : worlds) {
            total += weights.getOrDefault(w.getName(), 1);
        }
        if (total <= 0) return worlds.get(random.nextInt(worlds.size()));
        int r = random.nextInt(total);
        for (World w : worlds) {
            int wgt = weights.getOrDefault(w.getName(), 1);
            if (r < wgt) return w;
            r -= wgt;
        }
        return worlds.get(worlds.size() - 1);
    }
}
