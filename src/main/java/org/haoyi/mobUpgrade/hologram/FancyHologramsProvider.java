package org.haoyi.mobUpgrade.hologram;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.haoyi.mobUpgrade.MobUpgrade;
import org.haoyi.mobUpgrade.config.MainConfig;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FancyHologramsProvider extends HologramProvider {

    private final MobUpgrade plugin;
    private final MainConfig config;
    private final Map<UUID, Object> holograms = new ConcurrentHashMap<>();
    private static final String PREFIX = "mobupgrade_";

    public FancyHologramsProvider(MobUpgrade plugin) {
        this.plugin = plugin;
        this.config = plugin.getMainConfig();
    }

    @Override
    public String getName() {
        return "FancyHolograms";
    }

    @Override
    public void createMobHologram(LivingEntity entity, int day, double currentHp, double maxHp) {
        removeMobHologram(entity);
        try {
            Location loc = entity.getLocation().add(0, config.getHologramHeightAbove(), 0);
            List<String> lines = buildLines(entity, day, currentHp, maxHp);
            Object hologram = createHologram(PREFIX + entity.getUniqueId(), loc, lines);
            if (hologram != null) {
                holograms.put(entity.getUniqueId(), hologram);
                addToManager(hologram);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("FancyHolograms create failed: " + e.getMessage());
        }
    }

    @Override
    public void updateMobHologram(LivingEntity entity, int day, double currentHp, double maxHp) {
        Object h = holograms.get(entity.getUniqueId());
        if (h == null) return;
        try {
            Location loc = entity.getLocation().add(0, config.getHologramHeightAbove(), 0);
            setLocation(h, loc);
            List<String> lines = buildLines(entity, day, currentHp, maxHp);
            setText(h, lines);
            updateHologram(h);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void removeMobHologram(LivingEntity entity) {
        Object h = holograms.remove(entity.getUniqueId());
        if (h != null) {
            try {
                removeFromManager(h);
                deleteHologram(h);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void cleanup() {
        for (Object h : holograms.values()) {
            try {
                removeFromManager(h);
                deleteHologram(h);
            } catch (Exception ignored) {
            }
        }
        holograms.clear();
    }

    private List<String> buildLines(LivingEntity entity, int day, double currentHp, double maxHp) {
        String bar = color("&c" + buildBar(currentHp, maxHp, config.getBarLength(), config.getBarCharFilled(), config.getBarCharEmpty()));
        String name = config.isShowName() ? color("&f" + (entity.getCustomName() != null ? entity.getCustomName() : entity.getType().name())) : "";
        String dayLine = config.isShowDay() ? color("&7Day " + day) : "";
        return List.of(bar, name, dayLine).stream().filter(s -> !s.isEmpty()).toList();
    }

    private Object createHologram(String name, Location loc, List<String> lines) throws Exception {
        Class<?> dataClass = Class.forName("de.oliver.fancyholograms.hologram.data.TextHologramData");
        var ctor = dataClass.getConstructor(String.class, org.bukkit.Location.class);
        Object data = ctor.newInstance(name, loc);
        var setText = dataClass.getMethod("setText", List.class);
        setText.invoke(data, lines);
        var pluginClass = Class.forName("de.oliver.fancyholograms.FancyHolograms");
        var getInstance = pluginClass.getMethod("get");
        Object fancyPlugin = getInstance.invoke(null);
        var getManager = fancyPlugin.getClass().getMethod("getHologramManager");
        Object manager = getManager.invoke(fancyPlugin);
        var create = manager.getClass().getMethod("create", dataClass);
        return create.invoke(manager, data);
    }

    private void addToManager(Object hologram) throws Exception {
        var pluginClass = Class.forName("de.oliver.fancyholograms.FancyHolograms");
        var getInstance = pluginClass.getMethod("get");
        Object fancyPlugin = getInstance.invoke(null);
        var getManager = fancyPlugin.getClass().getMethod("getHologramManager");
        Object manager = getManager.invoke(fancyPlugin);
        var add = manager.getClass().getMethod("addHologram", hologram.getClass().getSuperclass());
        add.invoke(manager, hologram);
    }

    private void setLocation(Object h, Location loc) throws Exception {
        h.getClass().getMethod("setLocation", Location.class).invoke(h, loc);
    }

    private void setText(Object h, List<String> lines) throws Exception {
        var data = h.getClass().getMethod("getData").invoke(h);
        data.getClass().getMethod("setText", List.class).invoke(data, lines);
    }

    private void updateHologram(Object h) throws Exception {
        h.getClass().getMethod("updateHologram").invoke(h);
    }

    private void removeFromManager(Object hologram) throws Exception {
        var pluginClass = Class.forName("de.oliver.fancyholograms.FancyHolograms");
        var getInstance = pluginClass.getMethod("get");
        Object fancyPlugin = getInstance.invoke(null);
        var getManager = fancyPlugin.getClass().getMethod("getHologramManager");
        Object manager = getManager.invoke(fancyPlugin);
        var remove = manager.getClass().getMethod("removeHologram", hologram.getClass().getSuperclass());
        remove.invoke(manager, hologram);
    }

    private void deleteHologram(Object h) {
    }

    private static String color(String s) {
        return s == null ? "" : s.replace("&", "\u00a7");
    }
}
