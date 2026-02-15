package org.haoyi.mobUpgrade.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.haoyi.mobUpgrade.MobUpgrade;

import java.io.File;
import java.io.IOException;

public class RealTimeData {

    private static final String FILE_NAME = "realtime.yml";
    private static final String KEY_FIRST_RUN = "first-run-timestamp-millis";

    private final MobUpgrade plugin;
    private File file;
    private FileConfiguration config;
    private Long firstRunTimestamp;

    public RealTimeData(MobUpgrade plugin) {
        this.plugin = plugin;
    }

    public void load() {
        file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create " + FILE_NAME + ": " + e.getMessage());
                return;
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
        firstRunTimestamp = config.contains(KEY_FIRST_RUN) ? config.getLong(KEY_FIRST_RUN) : null;
    }

    public long getOrSetFirstRunTimestamp(long configStartMillis) {
        if (configStartMillis > 0) {
            return configStartMillis;
        }
        if (firstRunTimestamp != null) {
            return firstRunTimestamp;
        }
        long now = System.currentTimeMillis();
        firstRunTimestamp = now;
        config.set(KEY_FIRST_RUN, now);
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save " + FILE_NAME + ": " + e.getMessage());
        }
        return now;
    }

    public void setStartTimestamp(long millis) {
        firstRunTimestamp = millis;
        if (config != null) {
            config.set(KEY_FIRST_RUN, millis);
            try {
                config.save(file);
            } catch (IOException ignored) {
            }
        }
    }
}
