package org.haoyi.mobUpgrade.model;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.haoyi.mobUpgrade.MobUpgrade;

import java.util.List;
import java.util.Random;

/** Custom model: vanilla MATERIAL:CMD, oraxen:, itemadder:, nexo:. Uses reflection (no hard dependency). */
public class CustomModelProvider {

    private final MobUpgrade plugin;
    private final List<String> itemIds;
    private final Random random = new Random();

    public CustomModelProvider(MobUpgrade plugin, List<String> itemIds) {
        this.plugin = plugin;
        this.itemIds = itemIds == null || itemIds.isEmpty() ? List.of() : itemIds;
    }

    public ItemStack getRandomItem() {
        if (itemIds.isEmpty()) return null;
        String entry = itemIds.get(random.nextInt(itemIds.size())).trim();
        if (entry.isEmpty()) return null;
        return parseItem(entry);
    }

    private ItemStack parseItem(String entry) {
        if (entry.toLowerCase().startsWith("oraxen:")) {
            return getOraxenItem(entry.substring(7).trim());
        }
        if (entry.toLowerCase().startsWith("itemadder:")) {
            return getItemAdderItem(entry.substring(10).trim());
        }
        if (entry.toLowerCase().startsWith("nexo:")) {
            return getNexoItem(entry.substring(5).trim());
        }
        return getVanillaItem(entry);
    }

    private ItemStack getVanillaItem(String entry) {
        String[] parts = entry.split(":", 2);
        Material mat = Material.matchMaterial(parts[0].trim());
        if (mat == null) {
            try {
                mat = Material.valueOf(parts[0].trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        if (!mat.isItem()) return null;
        ItemStack item = new ItemStack(mat);
        if (parts.length >= 2) {
            try {
                int cmd = Integer.parseInt(parts[1].trim());
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setCustomModelData(cmd);
                    item.setItemMeta(meta);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return item;
    }

    private ItemStack getOraxenItem(String id) {
        try {
            Class<?> c = Class.forName("io.th0rgal.oraxen.items.OraxenItems");
            java.lang.reflect.Method m = c.getMethod("getItemById", String.class);
            Object builder = m.invoke(null, id);
            if (builder == null) return null;
            if (builder instanceof ItemStack stack) return stack;
            java.lang.reflect.Method build = builder.getClass().getMethod("build");
            Object result = build.invoke(builder);
            return result instanceof ItemStack s ? s : null;
        } catch (Throwable t) {
            plugin.getLogger().fine("Oraxen item '" + id + "': " + t.getMessage());
            return null;
        }
    }

    private ItemStack getItemAdderItem(String id) {
        try {
            Class<?> customStack = Class.forName("dev.lone.itemsadder.api.CustomStack");
            java.lang.reflect.Method m = customStack.getMethod("getInstance", String.class);
            Object instance = m.invoke(null, id);
            if (instance == null) return null;
            java.lang.reflect.Method getItem = customStack.getMethod("getItemStack");
            Object stack = getItem.invoke(instance);
            return stack instanceof ItemStack s ? s : null;
        } catch (Throwable t1) {
            try {
                Class<?> api = Class.forName("dev.lone.itemsadder.api.ItemsAdder");
                java.lang.reflect.Method m = api.getMethod("getCustomItem", String.class);
                Object stack = m.invoke(null, id);
                return stack instanceof ItemStack s ? s : null;
            } catch (Throwable t2) {
                plugin.getLogger().fine("ItemAdder item '" + id + "': " + t2.getMessage());
                return null;
            }
        }
    }

    private ItemStack getNexoItem(String id) {
        try {
            Class<?> c = Class.forName("com.nexomedia.nexoplugins.api.NexoAPI");
            java.lang.reflect.Method getItem = c.getMethod("getItem", String.class);
            Object stack = getItem.invoke(null, id);
            return stack instanceof ItemStack s ? s : null;
        } catch (Throwable t) {
            plugin.getLogger().fine("Nexo item '" + id + "': " + t.getMessage());
            return null;
        }
    }
}
