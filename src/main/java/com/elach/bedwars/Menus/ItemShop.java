package com.elach.bedwars.Menus;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ItemShop {

    private final Inventory shopInventory;
    private final List<Integer> shopItemSlots;
    private final HashMap<String, List<ItemOfSection>> shopInventorySections;
    private final List<Material> pickAxes = new ArrayList<>(Arrays.asList(Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE));
    private final List<Material> axes = new ArrayList<>(Arrays.asList(Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE));
    private final List<Material> swords = new ArrayList<>(Arrays.asList(Material.WOODEN_SWORD, Material.STONE_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD));
    private final HashMap<Material, DoubleResult> enchantments = new HashMap<>();

    public class DoubleResult {

        private final Enchantment enchantment;
        private final int level;

        public DoubleResult(Enchantment enchantment, int level) {
            this.enchantment = enchantment;
            this.level = level;
        }

        public Enchantment getEnchantment() {
            return enchantment;
        }

        public int getLevel() {
            return level;
        }
    }

    private final HashMap<Integer, ItemStack> prices = new HashMap<>();
    private final HashMap<Material, ItemStack> armors = new HashMap<>();
    private final HashMap<Integer, String> interactiveSlots;

    public ItemShop(Inventory shopInventory, List<Integer> shopItemSlots, HashMap<String, List<ItemOfSection>> shopInventorySections, HashMap<Integer, String> interactiveSlots) {
        this.shopInventory = shopInventory;
        this.shopItemSlots = shopItemSlots;
        this.shopInventorySections = shopInventorySections;
        armors.put(Material.GOLDEN_BOOTS, new ItemStack(Material.GOLDEN_LEGGINGS));
        armors.put(Material.CHAINMAIL_BOOTS, new ItemStack(Material.CHAINMAIL_LEGGINGS));
        armors.put(Material.IRON_BOOTS, new ItemStack(Material.IRON_LEGGINGS));
        armors.put(Material.DIAMOND_BOOTS, new ItemStack(Material.DIAMOND_LEGGINGS));
        prices.put(0, new ItemStack(Material.IRON_INGOT, 20));
        prices.put(1, new ItemStack(Material.IRON_INGOT, 30));
        prices.put(2, new ItemStack(Material.GOLD_INGOT, 5));
        prices.put(3, new ItemStack(Material.DIAMOND, 2));
        prices.put(4, new ItemStack(Material.EMERALD, 2));
        enchantments.put(Material.GOLDEN_PICKAXE, new DoubleResult(Enchantment.DIG_SPEED, 3));
        enchantments.put(Material.DIAMOND_PICKAXE, new DoubleResult(Enchantment.DIG_SPEED, 3));
        enchantments.put(Material.WOODEN_AXE, new DoubleResult(Enchantment.DIG_SPEED, 1));
        enchantments.put(Material.STONE_AXE, new DoubleResult(Enchantment.DIG_SPEED, 1));
        enchantments.put(Material.IRON_AXE, new DoubleResult(Enchantment.DIG_SPEED, 1));
        enchantments.put(Material.GOLDEN_AXE, new DoubleResult(Enchantment.DIG_SPEED, 2));
        enchantments.put(Material.DIAMOND_AXE, new DoubleResult(Enchantment.DIG_SPEED, 3));
        this.interactiveSlots = interactiveSlots;
    }

    public List<Integer> getShopItemSlots() {
        return shopItemSlots;
    }

    public HashMap<Integer, String> getInteractiveSlots() {
        return interactiveSlots;
    }

    public Inventory getShopInventory() {
        return shopInventory;
    }

    public HashMap<String, List<ItemOfSection>> getShopInventorySections() {
        return shopInventorySections;
    }

    public List<Material> getPickAxes() {
        return pickAxes;
    }

    public List<Material> getAxes() {
        return axes;
    }

    public List<Material> getSwords() {
        return swords;
    }

    public HashMap<Integer, ItemStack> getPrices() {
        return prices;
    }

    public HashMap<Material, DoubleResult> getEnchantments() {
        return enchantments;
    }

    public HashMap<Material, ItemStack> getArmors() {
        return armors;
    }
}
