package org.haoyi.mobUpgrade.config;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public record CustomDropConfig(Material material, double chance, int minAmount, int maxAmount, int minDay) {

    public ItemStack createDrop(int day, java.util.Random random) {
        if (day < minDay || random.nextDouble() >= chance) return null;
        int amount = minAmount >= maxAmount ? minAmount : minAmount + random.nextInt(maxAmount - minAmount + 1);
        if (amount <= 0 || material == null || material.isAir()) return null;
        return new ItemStack(material, Math.min(amount, material.getMaxStackSize()));
    }
}
