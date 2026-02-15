package org.haoyi.mobUpgrade.model;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.haoyi.mobUpgrade.MobUpgrade;

public final class MutantData {

    public static final NamespacedKey KEY_DAY = key("mutant_day");
    public static final NamespacedKey KEY_MUTANT = key("mutant");

    private static NamespacedKey key(String k) {
        return new NamespacedKey(MobUpgrade.getInstance(), k);
    }

    public static void setDay(LivingEntity entity, int day) {
        entity.getPersistentDataContainer().set(KEY_MUTANT, PersistentDataType.BYTE, (byte) 1);
        entity.getPersistentDataContainer().set(KEY_DAY, PersistentDataType.INTEGER, Math.max(1, day));
    }

    public static int getDay(LivingEntity entity) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(KEY_DAY, PersistentDataType.INTEGER)) {
            return 0;
        }
        return pdc.getOrDefault(KEY_DAY, PersistentDataType.INTEGER, 0);
    }

    public static boolean isMutant(LivingEntity entity) {
        return entity.getPersistentDataContainer().has(KEY_MUTANT, PersistentDataType.BYTE);
    }
}
