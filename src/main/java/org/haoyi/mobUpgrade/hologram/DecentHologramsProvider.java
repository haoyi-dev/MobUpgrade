package org.haoyi.mobUpgrade.hologram;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.haoyi.mobUpgrade.MobUpgrade;
import org.haoyi.mobUpgrade.config.MainConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DecentHologramsProvider extends HologramProvider {

    private final MobUpgrade plugin;
    private final MainConfig config;
    private final Map<UUID, String> hologramIds = new ConcurrentHashMap<>();
    private static final String PREFIX = "mobupgrade_";

    public DecentHologramsProvider(MobUpgrade plugin) {
        this.plugin = plugin;
        this.config = plugin.getMainConfig();
    }

    @Override
    public String getName() {
        return "DecentHolograms";
    }

    @Override
    public void createMobHologram(LivingEntity entity, int day, double currentHp, double maxHp) {
        removeMobHologram(entity);
        String id = PREFIX + entity.getUniqueId();
        hologramIds.put(entity.getUniqueId(), id);
        Location loc = entity.getLocation().add(0, config.getHologramHeightAbove(), 0);
        List<String> lines = buildLines(entity, day, currentHp, maxHp);
        try {
            var dhapi = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
            var create = dhapi.getMethod("createHologram", String.class, Location.class, List.class);
            create.invoke(null, id, loc, lines);
        } catch (Exception e) {
            plugin.getLogger().warning("DecentHolograms create failed: " + e.getMessage());
        }
    }

    @Override
    public void updateMobHologram(LivingEntity entity, int day, double currentHp, double maxHp) {
        String id = hologramIds.get(entity.getUniqueId());
        if (id == null) return;
        try {
            var dhapi = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
            var getHologram = dhapi.getMethod("getHologram", String.class);
            Object h = getHologram.invoke(null, id);
            if (h != null) {
                Location loc = entity.getLocation().add(0, config.getHologramHeightAbove(), 0);
                var move = dhapi.getMethod("moveHologram", h.getClass(), Location.class);
                move.invoke(null, h, loc);
                List<String> lines = buildLines(entity, day, currentHp, maxHp);
                var setLines = dhapi.getMethod("setHologramLines", h.getClass(), List.class);
                setLines.invoke(null, h, lines);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void removeMobHologram(LivingEntity entity) {
        String id = hologramIds.remove(entity.getUniqueId());
        if (id == null) return;
        try {
            var dhapi = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
            var remove = dhapi.getMethod("removeHologram", String.class);
            remove.invoke(null, id);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void cleanup() {
        for (String id : hologramIds.values()) {
            try {
                var dhapi = Class.forName("eu.decentsoftware.holograms.api.DHAPI");
                var remove = dhapi.getMethod("removeHologram", String.class);
                remove.invoke(null, id);
            } catch (Exception ignored) {
            }
        }
        hologramIds.clear();
    }

    private List<String> buildLines(LivingEntity entity, int day, double currentHp, double maxHp) {
        List<String> lines = new ArrayList<>();
        String bar = color("&c" + buildBar(currentHp, maxHp, config.getBarLength(), config.getBarCharFilled(), config.getBarCharEmpty()));
        lines.add(bar);
        if (config.isShowName()) {
            lines.add(color("&f" + (entity.getCustomName() != null ? entity.getCustomName() : entity.getType().name())));
        }
        if (config.isShowDay()) {
            lines.add(color("&7Day " + day));
        }
        return lines;
    }

    private static String color(String s) {
        return s == null ? "" : s.replace("&", "\u00a7");
    }
}
