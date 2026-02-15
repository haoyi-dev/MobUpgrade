package org.haoyi.mobUpgrade.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.haoyi.mobUpgrade.MobUpgrade;

import java.io.File;
import java.util.List;

public class LangConfig {

    private final MobUpgrade plugin;
    private File file;
    private FileConfiguration config;

    public LangConfig(MobUpgrade plugin) {
        this.plugin = plugin;
    }

    public void load() {
        String lang = plugin.getMainConfig().getLanguage();
        file = new File(plugin.getDataFolder(), "lang" + File.separator + lang + ".yml");
        String resourcePath = "lang/" + lang + ".yml";
        if (!file.exists()) {
            plugin.saveResource(resourcePath, false);
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public String get(String path) {
        String s = config.getString(path, path);
        return s.replace("&", "\u00a7");
    }

    public String get(String path, String... replacements) {
        String s = get(path);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            s = s.replace(replacements[i], replacements[i + 1]);
        }
        return s;
    }

    public List<String> getList(String path) {
        return config.getStringList(path);
    }
}
