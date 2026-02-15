package org.haoyi.mobUpgrade.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.haoyi.mobUpgrade.MobUpgrade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MainConfig {

    private final MobUpgrade plugin;
    private String language;
    private double hpPerDay;
    private double damagePerDay;
    private int maxDayCap;
    private String timeMode;
    private long realWorldStartTimestampMillis;
    private double realWorldDaysPerRealDay;
    private long upgradeExistingIntervalTicks;
    private boolean hologramEnabled;
    private double hologramHeightAbove;
    private int barLength;
    private String barCharFilled;
    private String barCharEmpty;
    private boolean showName;
    private boolean showDay;
    private long spawnIntervalTicks;
    private int perChunkAttempts;
    private int maxSpawnsPerRun;
    private int maxMutantsPerWorld;
    private int maxMutantsPerChunk;
    private int minPlayerDistance;
    private boolean spawnEnabled;
    private int hologramUpdateIntervalTicks;
    private int hologramMaxViewDistance;
    private boolean lootMultiplierEnabled;
    private double lootBonusPerDay;
    private double lootMaxMultiplier;
    private boolean spawnEffect;
    private boolean deathEffect;
    private int rewardBroadcastDayThreshold;
    private boolean xpMultiplierEnabled;
    private double xpBonusPerDay;
    private double xpMaxMultiplier;

    private List<String> spawnWorlds;
    private List<String> mobTypes;
    private boolean allMobs;
    private boolean allHostileMobs;
    private boolean excludeTheEnd;
    private int defaultSpawnDay;
    private boolean spawnDayCustomEnabled;
    private int spawnDayMin;
    private int spawnDayMax;
    private String nameFormat;
    private boolean customModelEnabled;
    private String customModelSlot;
    private List<String> customModelItems;
    private boolean customModelDropOnDeath;
    private boolean customDropsEnabled;
    private List<CustomDropConfig> customDrops;
    private boolean customCommandsEnabled;
    private List<String> customCommands;
    private Map<String, Integer> spawnWorldWeights;
    private boolean includePassiveMobs;
    private boolean passiveRevengeEnabled;
    private double passiveDamagePerDay;
    private double passiveRevengeRange;
    private long passiveRevengeIntervalTicks;
    private long passiveRevengeDurationTicks;

    public MainConfig(MobUpgrade plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.reloadConfig();
        FileConfiguration c = plugin.getConfig();
        language = c.getString("language", "en");
        hpPerDay = c.getDouble("scaling.hp-per-day", 0.5);
        damagePerDay = c.getDouble("scaling.damage-per-day", 0.3);
        maxDayCap = c.getInt("scaling.max-day-cap", 100);
        timeMode = c.getString("scaling.time-mode", "minecraft_day");
        realWorldStartTimestampMillis = c.getLong("real-world.start-timestamp-millis", 0L);
        realWorldDaysPerRealDay = c.getDouble("real-world.days-per-real-day", 1.0);
        upgradeExistingIntervalTicks = c.getLong("real-world.upgrade-existing-interval-ticks", 0L);
        hologramEnabled = c.getBoolean("hologram.enabled", true);
        hologramHeightAbove = c.getDouble("hologram.height-above", 0.8);
        barLength = c.getInt("hologram.bar-length", 10);
        barCharFilled = c.getString("hologram.bar-char-filled", "|");
        barCharEmpty = c.getString("hologram.bar-char-empty", " ");
        showName = c.getBoolean("hologram.show-name", true);
        showDay = c.getBoolean("hologram.show-day", true);
        hologramUpdateIntervalTicks = c.getInt("hologram.update-interval-ticks", 20);
        hologramMaxViewDistance = c.getInt("hologram.max-view-distance", 0);
        spawnEnabled = c.getBoolean("spawn.enabled", true);
        spawnIntervalTicks = c.getLong("spawn.interval-ticks", 6000L);
        perChunkAttempts = c.getInt("spawn.per-chunk-attempts", 2);
        maxSpawnsPerRun = c.getInt("spawn.max-spawns-per-run", 5);
        maxMutantsPerWorld = c.getInt("spawn.max-mutants-per-world", 80);
        maxMutantsPerChunk = c.getInt("spawn.max-mutants-per-chunk", 2);
        minPlayerDistance = c.getInt("spawn.min-player-distance", 24);
        spawnWorlds = c.getStringList("spawn.worlds");
        mobTypes = c.getStringList("spawn.mob-types");
        allMobs = c.getBoolean("spawn.all-mobs", false);
        allHostileMobs = c.getBoolean("spawn.all-hostile-mobs", false);
        excludeTheEnd = c.getBoolean("spawn.exclude-the-end", true);
        defaultSpawnDay = c.getInt("default-spawn-day", 1);
        spawnDayCustomEnabled = c.getBoolean("spawn-day-custom-enabled", false);
        spawnDayMin = c.getInt("spawn-day-min", 1);
        spawnDayMax = c.getInt("spawn-day-max", 10);
        nameFormat = c.getString("name-format", "&c&l[MUTANT] &f{name} &7(Day {day})");
        customModelEnabled = c.getBoolean("custom-model.enabled", false);
        customModelSlot = c.getString("custom-model.slot", "head");
        customModelItems = c.getStringList("custom-model.items");
        customModelDropOnDeath = c.getBoolean("custom-model.drop-on-death", false);
        customDropsEnabled = c.getBoolean("custom-drops.enabled", false);
        customDrops = loadCustomDrops(c);
        customCommandsEnabled = c.getBoolean("custom-commands.enabled", false);
        List<String> cmdList = c.getStringList("custom-commands.commands");
        customCommands = cmdList != null ? cmdList : List.of();
        spawnWorldWeights = loadWorldWeights(c.getConfigurationSection("spawn.world-weights"));
        includePassiveMobs = c.getBoolean("spawn.include-passive-mobs", false);
        passiveRevengeEnabled = c.getBoolean("passive-revenge.enabled", true);
        passiveDamagePerDay = c.getDouble("passive-revenge.damage-per-day", 0.2);
        passiveRevengeRange = c.getDouble("passive-revenge.range-blocks", 4.0);
        passiveRevengeIntervalTicks = c.getLong("passive-revenge.interval-ticks", 20L);
        passiveRevengeDurationTicks = c.getLong("passive-revenge.duration-ticks", 200L);
        lootMultiplierEnabled = c.getBoolean("features.loot-multiplier-enabled", true);
        lootBonusPerDay = c.getDouble("features.loot-bonus-per-day", 0.02);
        lootMaxMultiplier = c.getDouble("features.loot-max-multiplier", 3.0);
        spawnEffect = c.getBoolean("features.spawn-effect", true);
        deathEffect = c.getBoolean("features.death-effect", true);
        rewardBroadcastDayThreshold = c.getInt("features.reward-broadcast-day-threshold", 50);
        xpMultiplierEnabled = c.getBoolean("features.xp-multiplier-enabled", true);
        xpBonusPerDay = c.getDouble("features.xp-bonus-per-day", 0.01);
        xpMaxMultiplier = c.getDouble("features.xp-max-multiplier", 2.0);
    }

    public String getLanguage() { return language; }
    public double getHpPerDay() { return hpPerDay; }
    public double getDamagePerDay() { return damagePerDay; }
    public int getMaxDayCap() { return maxDayCap; }
    public String getTimeMode() { return timeMode; }
    public long getRealWorldStartTimestampMillis() { return realWorldStartTimestampMillis; }
    public double getRealWorldDaysPerRealDay() { return realWorldDaysPerRealDay; }
    public long getUpgradeExistingIntervalTicks() { return upgradeExistingIntervalTicks; }
    public boolean isHologramEnabled() { return hologramEnabled; }
    public double getHologramHeightAbove() { return hologramHeightAbove; }
    public int getBarLength() { return barLength; }
    public String getBarCharFilled() { return barCharFilled; }
    public String getBarCharEmpty() { return barCharEmpty; }
    public boolean isShowName() { return showName; }
    public boolean isShowDay() { return showDay; }
    public boolean isSpawnEnabled() { return spawnEnabled; }
    public long getSpawnIntervalTicks() { return spawnIntervalTicks; }
    public int getPerChunkAttempts() { return perChunkAttempts; }
    public int getMaxSpawnsPerRun() { return maxSpawnsPerRun; }
    public int getMaxMutantsPerWorld() { return maxMutantsPerWorld; }
    public int getMaxMutantsPerChunk() { return maxMutantsPerChunk; }
    public int getMinPlayerDistance() { return minPlayerDistance; }
    public int getHologramUpdateIntervalTicks() { return hologramUpdateIntervalTicks; }
    public int getHologramMaxViewDistance() { return hologramMaxViewDistance; }
    public boolean isLootMultiplierEnabled() { return lootMultiplierEnabled; }
    public double getLootBonusPerDay() { return lootBonusPerDay; }
    public double getLootMaxMultiplier() { return lootMaxMultiplier; }
    public boolean isSpawnEffect() { return spawnEffect; }
    public boolean isDeathEffect() { return deathEffect; }
    public int getRewardBroadcastDayThreshold() { return rewardBroadcastDayThreshold; }
    public boolean isXpMultiplierEnabled() { return xpMultiplierEnabled; }
    public double getXpBonusPerDay() { return xpBonusPerDay; }
    public double getXpMaxMultiplier() { return xpMaxMultiplier; }
    public List<String> getSpawnWorlds() { return spawnWorlds; }
    public List<String> getMobTypes() {
        if (allMobs) return getAllMobTypeNames();
        List<String> base = allHostileMobs ? getHostileMobTypeNames() : new ArrayList<>(mobTypes != null ? mobTypes : List.of());
        if (includePassiveMobs) {
            List<String> passive = getPassiveMobTypeNames();
            for (String p : passive) { if (!base.contains(p)) base.add(p); }
        }
        return base;
    }
    public boolean isAllMobs() { return allMobs; }
    public boolean isAllHostileMobs() { return allHostileMobs; }
    public boolean isExcludeTheEnd() { return excludeTheEnd; }

    private static final Set<EntityType> HOSTILE_TYPES = Arrays.stream(EntityType.values())
            .filter(EntityType::isAlive)
            .filter(t -> t != EntityType.PLAYER && t != EntityType.ARMOR_STAND)
            .filter(t -> {
                String n = t.name();
                if (n.contains("VILLAGER") && !n.startsWith("ZOMBIE")) return false;
                if (n.equals("IRON_GOLEM") || n.equals("SNOWMAN") || n.equals("DOLPHIN")) return false;
                if (n.startsWith("AXOLOTL") || n.equals("BAT") || n.equals("CAT") || n.equals("CHICKEN")) return false;
                if (n.equals("COD") || n.equals("COW") || n.equals("DONKEY") || n.equals("FOX")) return false;
                if (n.equals("FROG") || n.equals("HORSE") || n.equals("MUSHROOM_COW") || n.equals("MULE")) return false;
                if (n.equals("OCELOT") || n.equals("PARROT") || n.equals("PIG") || n.equals("POLAR_BEAR")) return false;
                if (n.equals("PUFFERFISH") || n.equals("RABBIT") || n.equals("SALMON") || n.equals("SHEEP")) return false;
                if (n.equals("SKELETON_HORSE") || n.equals("SQUID") || n.equals("STRIDER") || n.equals("TADPOLE")) return false;
                if (n.equals("TROPICAL_FISH") || n.equals("TURTLE") || n.equals("WOLF") || n.equals("ZOMBIE_HORSE")) return false;
                if (n.equals("CAMEL") || n.equals("SNIFFER") || n.equals("ALLAY")) return false;
                return isHostileByName(n);
            })
            .collect(Collectors.toSet());

    private static boolean isHostileByName(String n) {
        return n.equals("ZOMBIE") || n.equals("SKELETON") || n.equals("CREEPER") || n.equals("SPIDER") || n.equals("CAVE_SPIDER")
                || n.equals("ENDERMAN") || n.equals("ZOMBIE_VILLAGER") || n.equals("WITCH") || n.equals("WITHER_SKELETON")
                || n.equals("BLAZE") || n.equals("PIGLIN") || n.equals("PIGLIN_BRUTE") || n.equals("HOGLIN") || n.equals("ZOGLIN")
                || n.equals("DROWNED") || n.equals("HUSK") || n.equals("STRAY") || n.equals("PHANTOM") || n.equals("GHAST")
                || n.equals("MAGMA_CUBE") || n.equals("SLIME") || n.equals("SILVERFISH") || n.equals("ENDERMITE")
                || n.equals("VEX") || n.equals("EVOKER") || n.equals("VINDICATOR") || n.equals("ILLUSIONER") || n.equals("PILLAGER")
                || n.equals("RAVAGER") || n.equals("WARDEN") || n.equals("GUARDIAN") || n.equals("ELDER_GUARDIAN")
                || n.equals("SHULKER") || n.equals("BOGGED") || n.equals("BREEZE") || n.equals("GIANT");
    }
    private static List<String> getHostileMobTypeNames() {
        return HOSTILE_TYPES.stream().map(EntityType::name).sorted().collect(Collectors.toList());
    }

    private static List<String> getAllMobTypeNames() {
        return Arrays.stream(EntityType.values())
                .filter(EntityType::isAlive)
                .filter(t -> t != EntityType.PLAYER && t != EntityType.ARMOR_STAND)
                .map(EntityType::name)
                .sorted()
                .collect(Collectors.toList());
    }

    private static final Set<EntityType> PASSIVE_TYPES = Arrays.stream(EntityType.values())
            .filter(EntityType::isAlive)
            .filter(t -> t != EntityType.PLAYER && t != EntityType.ARMOR_STAND)
            .filter(t -> {
                String n = t.name();
                return n.equals("COW") || n.equals("PIG") || n.equals("SHEEP") || n.equals("CHICKEN")
                        || n.equals("RABBIT") || n.equals("BAT") || n.equals("COD") || n.equals("SALMON")
                        || n.equals("TROPICAL_FISH") || n.equals("PUFFERFISH") || n.equals("SQUID") || n.equals("GLOW_SQUID")
                        || n.equals("WOLF") || n.equals("CAT") || n.equals("HORSE") || n.equals("DONKEY") || n.equals("MULE")
                        || n.equals("LLAMA") || n.equals("TRADER_LLAMA") || n.equals("PARROT") || n.equals("FOX")
                        || n.equals("BEE") || n.equals("POLAR_BEAR") || n.equals("TURTLE") || n.equals("DOLPHIN")
                        || n.equals("PANDA") || n.equals("OCELOT") || n.equals("MUSHROOM_COW") || n.equals("IRON_GOLEM")
                        || n.equals("SNOW_GOLEM") || n.equals("VILLAGER") || n.equals("WANDERING_TRADER")
                        || n.equals("AXOLOTL") || n.equals("GOAT") || n.equals("FROG") || n.equals("TADPOLE")
                        || n.equals("CAMEL") || n.equals("SNIFFER") || n.equals("ALLAY") || n.equals("STRIDER");
            })
            .collect(Collectors.toSet());

    @SuppressWarnings("unchecked")
    private static List<CustomDropConfig> loadCustomDrops(FileConfiguration c) {
        List<CustomDropConfig> list = new ArrayList<>();
        List<?> items = c.getList("custom-drops.items");
        if (items == null) return list;
        for (Object o : items) {
            if (!(o instanceof Map)) continue;
            Map<String, Object> map = (Map<String, Object>) o;
            String matName = String.valueOf(map.getOrDefault("material", "STONE"));
            Material mat = Material.matchMaterial(matName);
            if (mat == null || !mat.isItem()) continue;
            double chance = ((Number) map.getOrDefault("chance", 0.1)).doubleValue();
            int minA = ((Number) map.getOrDefault("min-amount", 1)).intValue();
            int maxA = ((Number) map.getOrDefault("max-amount", 1)).intValue();
            int minD = ((Number) map.getOrDefault("min-day", 0)).intValue();
            list.add(new CustomDropConfig(mat, chance, minA, maxA, minD));
        }
        return list;
    }

    private static Map<String, Integer> loadWorldWeights(ConfigurationSection section) {
        Map<String, Integer> map = new HashMap<>();
        if (section == null) return map;
        for (String key : section.getKeys(false)) {
            int w = section.getInt(key, 1);
            if (w > 0) map.put(key, w);
        }
        return map;
    }

    public boolean isCustomDropsEnabled() { return customDropsEnabled; }
    public List<CustomDropConfig> getCustomDrops() { return customDrops; }
    public boolean isCustomCommandsEnabled() { return customCommandsEnabled; }
    public List<String> getCustomCommands() { return customCommands; }
    public Map<String, Integer> getSpawnWorldWeights() { return spawnWorldWeights; }
    public boolean isIncludePassiveMobs() { return includePassiveMobs; }
    public boolean isPassiveRevengeEnabled() { return passiveRevengeEnabled; }
    public double getPassiveDamagePerDay() { return passiveDamagePerDay; }
    public double getPassiveRevengeRange() { return passiveRevengeRange; }
    public long getPassiveRevengeIntervalTicks() { return passiveRevengeIntervalTicks; }
    public long getPassiveRevengeDurationTicks() { return passiveRevengeDurationTicks; }
    public List<String> getPassiveMobTypeNames() {
        return PASSIVE_TYPES.stream().map(EntityType::name).sorted().collect(Collectors.toList());
    }

    public int getDefaultSpawnDay() { return defaultSpawnDay; }
    public boolean isSpawnDayCustomEnabled() { return spawnDayCustomEnabled; }
    public int getSpawnDayMin() { return spawnDayMin; }
    public int getSpawnDayMax() { return spawnDayMax; }
    public String getNameFormat() { return nameFormat; }
    public boolean isCustomModelEnabled() { return customModelEnabled; }
    public String getCustomModelSlot() { return customModelSlot; }
    public List<String> getCustomModelItems() { return customModelItems; }
    public boolean isCustomModelDropOnDeath() { return customModelDropOnDeath; }
}
