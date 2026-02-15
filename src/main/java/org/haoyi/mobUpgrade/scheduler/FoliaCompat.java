package org.haoyi.mobUpgrade.scheduler;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;

/** Paper + Folia scheduler compatibility via reflection. */
public final class FoliaCompat {

    private static final Boolean FOLIA = detectFolia();
    private static final MethodHandle GET_GLOBAL_SCHEDULER;
    private static final MethodHandle GLOBAL_RUN_AT_FIXED_RATE;
    private static final MethodHandle GLOBAL_CANCEL;
    private static final MethodHandle GET_REGION_SCHEDULER;
    private static final MethodHandle REGION_RUN;
    private static final MethodHandle ENTITY_GET_SCHEDULER;
    private static final MethodHandle ENTITY_RUN;

    static {
        MethodHandle g1 = null, g2 = null, g3 = null, r1 = null, r2 = null, e1 = null, e2 = null;
        if (Boolean.TRUE.equals(detectFolia())) {
            try {
                var lookup = MethodHandles.publicLookup();
                Class<?> serverClass = Class.forName("org.bukkit.Server");
                Class<?> scheduledTaskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
                g1 = lookup.findVirtual(serverClass, "getGlobalRegionScheduler", MethodType.methodType(Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler")));
                Class<?> globalClass = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
                g2 = lookup.findVirtual(globalClass, "runAtFixedRate", MethodType.methodType(scheduledTaskClass, Plugin.class, Consumer.class, long.class, long.class));
                g3 = lookup.findVirtual(scheduledTaskClass, "cancel", MethodType.methodType(void.class));
                r1 = lookup.findVirtual(serverClass, "getRegionScheduler", MethodType.methodType(Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler")));
                Class<?> regionClass = Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
                r2 = lookup.findVirtual(regionClass, "run", MethodType.methodType(scheduledTaskClass, Plugin.class, World.class, int.class, int.class, Runnable.class));
                e1 = lookup.findVirtual(Entity.class, "getScheduler", MethodType.methodType(Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler")));
                Class<?> entitySched = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
                e2 = lookup.findVirtual(entitySched, "run", MethodType.methodType(scheduledTaskClass, Plugin.class, Runnable.class, Runnable.class));
            } catch (Throwable ignored) {
            }
        }
        GET_GLOBAL_SCHEDULER = g1;
        GLOBAL_RUN_AT_FIXED_RATE = g2;
        GLOBAL_CANCEL = g3;
        GET_REGION_SCHEDULER = r1;
        REGION_RUN = r2;
        ENTITY_GET_SCHEDULER = e1;
        ENTITY_RUN = e2;
    }

    private static Boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isFolia() {
        return Boolean.TRUE.equals(FOLIA);
    }

    public static Object runGlobalTimer(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        if (isFolia() && GET_GLOBAL_SCHEDULER != null && GLOBAL_RUN_AT_FIXED_RATE != null) {
            try {
                Object global = GET_GLOBAL_SCHEDULER.invoke(plugin.getServer());
                Consumer<?> consumer = st -> runnable.run();
                return GLOBAL_RUN_AT_FIXED_RATE.invoke(global, plugin, consumer, delayTicks, periodTicks);
            } catch (Throwable t) {
                if (isFolia()) return null;
                return fallbackGlobalTimer(plugin, runnable, delayTicks, periodTicks);
            }
        }
        if (isFolia()) return null;
        return fallbackGlobalTimer(plugin, runnable, delayTicks, periodTicks);
    }

    private static Object fallbackGlobalTimer(Plugin plugin, Runnable runnable, long delayTicks, long periodTicks) {
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks);
        return task != null ? task.getTaskId() : null;
    }

    public static void cancelGlobalTask(Object task) {
        if (task == null) return;
        if (isFolia() && GLOBAL_CANCEL != null && !(task instanceof Integer)) {
            try {
                GLOBAL_CANCEL.invoke(task);
                return;
            } catch (Throwable ignored) {
            }
        }
        if (task instanceof Integer id) {
            org.bukkit.Bukkit.getScheduler().cancelTask(id);
        }
    }

    public static void runAtRegion(Plugin plugin, World world, int chunkX, int chunkZ, Runnable runnable) {
        if (isFolia() && GET_REGION_SCHEDULER != null && REGION_RUN != null) {
            try {
                Object region = GET_REGION_SCHEDULER.invoke(plugin.getServer());
                REGION_RUN.invoke(region, plugin, world, chunkX, chunkZ, runnable);
                return;
            } catch (Throwable t) {
                plugin.getServer().getScheduler().runTask(plugin, runnable);
                return;
            }
        }
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    public static void runAtEntity(Plugin plugin, Entity entity, Runnable runnable) {
        if (isFolia() && entity != null && ENTITY_GET_SCHEDULER != null && ENTITY_RUN != null) {
            try {
                Object scheduler = ENTITY_GET_SCHEDULER.invoke(entity);
                ENTITY_RUN.invoke(scheduler, plugin, runnable, null);
                return;
            } catch (Throwable t) {
                plugin.getServer().getScheduler().runTask(plugin, runnable);
                return;
            }
        }
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    public static void runGlobal(Plugin plugin, Runnable runnable) {
        if (isFolia() && GET_GLOBAL_SCHEDULER != null) {
            try {
                Object global = GET_GLOBAL_SCHEDULER.invoke(plugin.getServer());
                Class<?> c = global.getClass();
                var run = MethodHandles.publicLookup().findVirtual(c, "run", MethodType.methodType(void.class, Plugin.class, Runnable.class));
                run.invoke(global, plugin, runnable);
                return;
            } catch (Throwable t) {
                plugin.getServer().getScheduler().runTask(plugin, runnable);
                return;
            }
        }
        plugin.getServer().getScheduler().runTask(plugin, runnable);
    }
}
