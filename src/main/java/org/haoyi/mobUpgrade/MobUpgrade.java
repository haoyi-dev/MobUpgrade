package org.haoyi.mobUpgrade;

import org.haoyi.mobUpgrade.command.MobUpgradeCommand;
import org.haoyi.mobUpgrade.command.MutantCommand;
import org.haoyi.mobUpgrade.config.MainConfig;
import org.haoyi.mobUpgrade.config.LangConfig;
import org.haoyi.mobUpgrade.config.RealTimeData;
import org.haoyi.mobUpgrade.hologram.HologramProvider;
import org.haoyi.mobUpgrade.listener.MobUpgradeListener;
import org.haoyi.mobUpgrade.manager.CustomDropManager;
import org.haoyi.mobUpgrade.manager.MutantMobManager;
import org.haoyi.mobUpgrade.task.MutantSpawnTask;
import org.haoyi.mobUpgrade.task.MutantUpgradeTask;
import org.haoyi.mobUpgrade.task.PassiveRevengeTask;
import org.bukkit.plugin.java.JavaPlugin;

public final class MobUpgrade extends JavaPlugin {

    private static MobUpgrade instance;
    private MainConfig mainConfig;
    private LangConfig langConfig;
    private MutantMobManager mutantMobManager;
    private CustomDropManager customDropManager;
    private HologramProvider hologramProvider;
    private MobUpgradeListener mobUpgradeListener;
    private MutantSpawnTask spawnTask;
    private MutantUpgradeTask upgradeTask;
    private PassiveRevengeTask passiveRevengeTask;
    private RealTimeData realTimeData;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        loadConfigs();
        mutantMobManager = new MutantMobManager(this);
        customDropManager = new CustomDropManager(this);
        customDropManager.load();
        hologramProvider = HologramProvider.detect(this);
        if (hologramProvider != null) {
            getLogger().info("Hologram support: " + hologramProvider.getName());
        }
        registerCommands();
        mobUpgradeListener = new MobUpgradeListener(this);
        getServer().getPluginManager().registerEvents(mobUpgradeListener, this);
        spawnTask = new MutantSpawnTask(this);
        spawnTask.start();
        upgradeTask = new MutantUpgradeTask(this);
        upgradeTask.start();
        passiveRevengeTask = new PassiveRevengeTask(this, mobUpgradeListener);
        passiveRevengeTask.start();
        printStartupInfo();
    }

    private void printStartupInfo() {
        String v = getDescription().getVersion();
        String authors = String.join(", ", getDescription().getAuthors());
        String[] lines = {
                " ",
                "  §c§lMobUpgrade §7v" + v,
                "  §7" + getDescription().getDescription(),
                "  §fAuthors: §e" + authors,
                "  §fWebsite: §ehaoyi.dev",
                "  §fSupport: §7Paper 1.20 - 1.21.11",
                " "
        };
        for (String line : lines) {
            getLogger().info(consoleColor(line));
        }
    }

    private static String consoleColor(String text) {
        if (text == null) return "";
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                char c = text.charAt(i + 1);
                i++;
                String ansi = switch (Character.toLowerCase(c)) {
                    case '0' -> "\u001b[30m";
                    case '1' -> "\u001b[34m";
                    case '2' -> "\u001b[32m";
                    case '3' -> "\u001b[36m";
                    case '4' -> "\u001b[31m";
                    case '5' -> "\u001b[35m";
                    case '6' -> "\u001b[33m";
                    case '7' -> "\u001b[90m";
                    case '8' -> "\u001b[90m";
                    case '9' -> "\u001b[34;1m";
                    case 'a' -> "\u001b[32;1m";
                    case 'b' -> "\u001b[36;1m";
                    case 'c' -> "\u001b[31;1m";
                    case 'd' -> "\u001b[35;1m";
                    case 'e' -> "\u001b[33;1m";
                    case 'f' -> "\u001b[37m";
                    case 'l' -> "\u001b[1m";
                    case 'm' -> "\u001b[9m";
                    case 'n' -> "\u001b[4m";
                    case 'o' -> "\u001b[3m";
                    case 'r' -> "\u001b[0m";
                    default -> "";
                };
                out.append(ansi);
            } else {
                out.append(text.charAt(i));
            }
        }
        return out.append("\u001b[0m").toString();
    }

    @Override
    public void onDisable() {
        if (spawnTask != null) spawnTask.cancel();
        if (upgradeTask != null) upgradeTask.cancel();
        if (passiveRevengeTask != null) passiveRevengeTask.cancel();
        if (hologramProvider != null) {
            hologramProvider.cleanup();
        }
        getLogger().info("MobUpgrade disabled.");
    }

    private void loadConfigs() {
        mainConfig = new MainConfig(this);
        mainConfig.load();
        realTimeData = new RealTimeData(this);
        realTimeData.load();
        langConfig = new LangConfig(this);
        langConfig.load();
    }

    private void registerCommands() {
        var mu = getCommand("mobupgrade");
        if (mu != null) {
            mu.setExecutor(new MobUpgradeCommand(this));
            mu.setTabCompleter(new MobUpgradeCommand(this));
        }
        var mut = getCommand("mutant");
        if (mut != null) {
            mut.setExecutor(new MutantCommand(this));
            mut.setTabCompleter(new MutantCommand(this));
        }
    }

    public void reload() {
        reloadConfig();
        loadConfigs();
        if (customDropManager != null) customDropManager.load();
        mutantMobManager.reload();
        if (spawnTask != null) {
            spawnTask.cancel();
            spawnTask = new MutantSpawnTask(this);
            spawnTask.start();
        }
        if (upgradeTask != null) {
            upgradeTask.cancel();
            upgradeTask = new MutantUpgradeTask(this);
            upgradeTask.start();
        }
    }

    public int getCurrentRealTimeDay() {
        if (!"real_world".equalsIgnoreCase(mainConfig.getTimeMode())) {
            return mainConfig.getDefaultSpawnDay();
        }
        long start = realTimeData.getOrSetFirstRunTimestamp(mainConfig.getRealWorldStartTimestampMillis());
        long now = System.currentTimeMillis();
        long elapsedDays = (now - start) / (24L * 60 * 60 * 1000);
        int day = (int) Math.max(1, Math.round(elapsedDays * mainConfig.getRealWorldDaysPerRealDay()));
        return Math.min(day, mainConfig.getMaxDayCap());
    }

    public RealTimeData getRealTimeData() {
        return realTimeData;
    }

    public static MobUpgrade getInstance() {
        return instance;
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public LangConfig getLangConfig() {
        return langConfig;
    }

    public MutantMobManager getMutantMobManager() {
        return mutantMobManager;
    }

    public CustomDropManager getCustomDropManager() {
        return customDropManager;
    }

    public HologramProvider getHologramProvider() {
        return hologramProvider;
    }
}
