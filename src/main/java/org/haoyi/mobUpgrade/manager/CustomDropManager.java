package org.haoyi.mobUpgrade.manager;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.haoyi.mobUpgrade.MobUpgrade;
import org.haoyi.mobUpgrade.config.CustomDropConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomDropManager {

    private final MobUpgrade plugin;
    private final File file;
    private final List<CustomDropConfig> drops = new ArrayList<>();

    public CustomDropManager(MobUpgrade plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "custom-drops.yml");
    }

    public void load() {
        drops.clear();
        if (!file.exists()) return;
        YamlConfiguration c = YamlConfiguration.loadConfiguration(file);
        List<?> items = c.getList("items");
        if (items == null) return;
        for (Object o : items) {
            if (!(o instanceof java.util.Map)) continue;
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) o;
            String matName = String.valueOf(map.getOrDefault("material", "STONE"));
            Material mat = Material.matchMaterial(matName);
            if (mat == null || !mat.isItem()) continue;
            double chance = ((Number) map.getOrDefault("chance", 0.1)).doubleValue();
            int minA = ((Number) map.getOrDefault("min-amount", 1)).intValue();
            int maxA = ((Number) map.getOrDefault("max-amount", 1)).intValue();
            int minD = ((Number) map.getOrDefault("min-day", 0)).intValue();
            drops.add(new CustomDropConfig(mat, chance, minA, maxA, minD));
        }
    }

    public void save() {
        plugin.getDataFolder().mkdirs();
        YamlConfiguration c = new YamlConfiguration();
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        for (CustomDropConfig d : drops) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("material", d.material().name());
            map.put("chance", d.chance());
            map.put("min-amount", d.minAmount());
            map.put("max-amount", d.maxAmount());
            map.put("min-day", d.minDay());
            list.add(map);
        }
        c.set("items", list);
        try {
            c.save(file);
        } catch (Throwable ignored) {
        }
    }

    public List<CustomDropConfig> getDrops() {
        return new ArrayList<>(drops);
    }

    public boolean addDrop(CustomDropConfig drop) {
        if (drop == null || drop.material() == null || !drop.material().isItem()) return false;
        drops.add(drop);
        save();
        return true;
    }

    public CustomDropConfig removeDrop(int index) {
        if (index < 0 || index >= drops.size()) return null;
        CustomDropConfig removed = drops.remove(index);
        save();
        return removed;
    }
}
