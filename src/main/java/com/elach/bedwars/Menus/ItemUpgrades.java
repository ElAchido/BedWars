package com.elach.bedwars.Menus;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class ItemUpgrades {

    private final HashMap<Integer, TrapItem> traps;
    private final HashMap<Integer, UpgradeItem> upgrades;

    public ItemUpgrades(HashMap<Integer, TrapItem> traps, HashMap<Integer, UpgradeItem> upgrades) {
        this.traps = traps;
        this.upgrades = upgrades;
    }

    static class TrapItem {
        private final String id;
        private final String displayName;
        private final List<String> buyLore;
        private final List<String> useLore;
        private final Material materialOfItem;
        private final ItemStack required;


        public TrapItem(String id,
                        String displayName,
                        List<String> buyLore,
                        List<String> userLore,
                        Material materialOfItem,
                        ItemStack required) {
            this.id = id;
            this.displayName = displayName;
            this.buyLore = buyLore;
            this.useLore = userLore;
            this.materialOfItem = materialOfItem;
            this.required = required;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public ItemStack getRequired() {
            return required;
        }

        public List<String> getBuyLore() {
            return buyLore;
        }

        public List<String> getUseLore() {
            return useLore;
        }

        public Material getMaterialOfItem() {
            return materialOfItem;
        }
    }

    static class UpgradeItem {
        private final String id;
        private final String displayName;
        private final HashMap<Integer, List<String>> loreLevels;
        private final HashMap<Integer, ItemStack> requiredLevels;
        private final List<String> purchasedLore;
        private final Material materialOfItem;


        public UpgradeItem(String id,
                        String displayName,
                        HashMap<Integer, List<String>> loreLevels,
                        HashMap<Integer, ItemStack> requiredLevels,
                        List<String> purchasedLore,
                        Material materialOfItem) {
            this.id = id;
            this.displayName = displayName;
            this.loreLevels = loreLevels;
            this.requiredLevels = requiredLevels;
            this.purchasedLore = purchasedLore;
            this.materialOfItem = materialOfItem;
        }

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public HashMap<Integer, ItemStack> getRequiredLevels() {
            return requiredLevels;
        }

        public HashMap<Integer, List<String>> getLoreLevels() {
            return loreLevels;
        }

        public List<String> getPurchasedLore() {
            return purchasedLore;
        }

        public Material getMaterialOfItem() {
            return materialOfItem;
        }
    }

    public HashMap<Integer, UpgradeItem> getUpgrades() {
        return upgrades;
    }

    public HashMap<Integer, TrapItem> getTraps() {
        return traps;
    }
}
