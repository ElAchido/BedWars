package com.elach.bedwars.Menus;

import org.bukkit.inventory.ItemStack;

public class ItemOfSection {

    private final ItemStack displayItem;
    private final ItemStack requiredItem;
    private final int slot;

    public ItemOfSection(ItemStack displayItem, ItemStack requiredItem, int slot) {
        this.displayItem = displayItem;
        this.requiredItem = requiredItem;
        this.slot = slot;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public ItemStack getRequiredItem() {
        return requiredItem;
    }
}
