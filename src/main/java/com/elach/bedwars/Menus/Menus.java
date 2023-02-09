package com.elach.bedwars.Menus;

import com.elach.bedwars.Arena.ArenaUtils.TeamInfo;
import com.elach.bedwars.Arena.ArenaUtils.Upgrades;
import com.elach.bedwars.Arena.System.Arena;
import com.elach.bedwars.BedWars;
import com.elach.bedwars.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

public class Menus implements Listener {

    private final BedWars plugin;
    private final String shopTitleMenu;
    private final ItemShop shop;
    private final String upgradesTitleMenu;
    private final ItemUpgrades upgrades;

    public Menus(BedWars plugin) {
        this.plugin = plugin;

        shopTitleMenu = Utils.translateMessage(plugin.getConfig().getString("menus.shop-title"));
        shop = createShop(plugin.getConfig());

        upgradesTitleMenu = Utils.translateMessage(plugin.getConfig().getString("menus.upgrades-title"));
        upgrades = createUpgrades(plugin.getConfig());
    }

    // INVENTORIES

    public ItemUpgrades createUpgrades(FileConfiguration config) {
        HashMap<Integer, ItemUpgrades.UpgradeItem> upgrades = new HashMap<>();
        Set<String> upgradeItems = config.getConfigurationSection("menus.upgrades-menus.upgrade-items").getKeys(false);
        for (String upgrade : upgradeItems) {
            String displayName = Utils.translateMessage(config.getString("menus.upgrades-menus.upgrade-items." + upgrade + ".display-name"));
            HashMap<Integer, List<String>> loreLevels = new HashMap<>();
            HashMap<Integer, ItemStack> requiredLevels = new HashMap<>();
            Set<String> levels = config.getConfigurationSection("menus.upgrades-menus.upgrade-items." + upgrade + ".levels").getKeys(false);
            for (String level : levels) {
                List<String> lore = Utils.translateListMessage(config.getStringList("menus.upgrades-menus.upgrade-items." + upgrade + ".levels." + level + ".lore"));
                ItemStack requiredItem = new ItemStack(Material.valueOf(config.getString("menus.upgrades-menus.upgrade-items." + upgrade + ".levels." + level + ".price.material-of-item")), config.getInt("menus.upgrades-menus.upgrade-items." + upgrade + ".levels." + level + ".price.amount-needed"));
                Integer levelId = Integer.parseInt(level);
                loreLevels.put(levelId, lore);
                requiredLevels.put(levelId, requiredItem);
            }
            List<String> purchasedLore = Utils.translateListMessage(config.getStringList("menus.upgrades-menus.upgrade-items." + upgrade + ".purchased"));
            Material itemMaterial = Material.valueOf(config.getString("menus.upgrades-menus.upgrade-items." + upgrade + ".material"));
            Integer slot = config.getInt("menus.upgrades-menus.upgrade-items." + upgrade + ".slot");
            upgrades.put(slot, new ItemUpgrades.UpgradeItem(upgrade, displayName, loreLevels, requiredLevels, purchasedLore, itemMaterial));
        }
        HashMap<Integer, ItemUpgrades.TrapItem> traps = new HashMap<>();
        Set<String> trapItems = config.getConfigurationSection("menus.upgrades-menus.trap-items").getKeys(false);
        for (String trap : trapItems) {
            String displayName = Utils.translateMessage(config.getString("menus.upgrades-menus.trap-items." + trap + ".display-name"));
            List<String> buyLore = Utils.translateListMessage(config.getStringList("menus.upgrades-menus.trap-items." + trap + ".lore"));
            List<String> useLore = Utils.translateListMessage(config.getStringList("menus.upgrades-menus.trap-items." + trap + ".in-use"));
            Material material = Material.valueOf(config.getString("menus.upgrades-menus.trap-items." + trap + ".material"));
            Integer slot = config.getInt("menus.upgrades-menus.trap-items." + trap + ".slot");
            ItemStack requiredItem = new ItemStack(Material.valueOf(config.getString("menus.upgrades-menus.trap-items." + trap + ".price.material-of-item")), config.getInt("menus.upgrades-menus.trap-items." + trap + ".price.amount-needed"));
            traps.put(slot, new ItemUpgrades.TrapItem(trap, displayName, buyLore, useLore, material, requiredItem));
        }
        return new ItemUpgrades(traps, upgrades);
    }

    public ItemShop createShop(FileConfiguration config) {
        Inventory shopInventory = Bukkit.createInventory(null, 54, shopTitleMenu);
        List<Integer> shopItemSlots = config.getIntegerList("menus.shop-menus.menu-template.shop-items-slots");
        HashMap<Integer, String> interactiveSlots = new HashMap<>();
        Set<String> interactiveItems = config.getConfigurationSection("menus.shop-menus.menu-template.interactive-items").getKeys(false);
        for (String interactiveItem : interactiveItems) {
            String displayName = Utils.translateMessage(config.getString("menus.shop-menus.menu-template.interactive-items." + interactiveItem + ".display-name"));
            List<String> lore = Utils.translateListMessage(config.getStringList("menus.shop-menus.menu-template.interactive-items." + interactiveItem + ".lore"));
            Material material = Material.valueOf(config.getString("menus.shop-menus.menu-template.interactive-items." + interactiveItem + ".material"));
            int amount = config.getInt("menus.shop-menus.menu-template.interactive-items." + interactiveItem + ".amount");
            int slot = config.getInt("menus.shop-menus.menu-template.interactive-items." + interactiveItem + ".slot");
            String sectionId = config.getString("menus.shop-menus.menu-template.interactive-items." + interactiveItem + ".section");
            ItemStack itemStack = new ItemStack(material, amount);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemStack.setItemMeta(meta);
            shopInventory.setItem(slot, itemStack);
            interactiveSlots.put(slot, sectionId);
        }
        Set<String> fills = config.getConfigurationSection("menus.shop-menus.menu-template.fill").getKeys(false);
        for (String fillData : fills) {
            String displayName = Utils.translateMessage(config.getString("menus.shop-menus.menu-template.fill." + fillData + ".display-name"));
            List<String> lore = Utils.translateListMessage(config.getStringList("menus.shop-menus.menu-template.fill." + fillData + ".lore"));
            Material material = Material.valueOf(config.getString("menus.shop-menus.menu-template.fill." + fillData + ".material"));
            int amount = config.getInt("menus.shop-menus.menu-template.fill." + fillData +  ".amount");
            List<Integer> slots = config.getIntegerList("menus.shop-menus.menu-template.fill." + fillData +  ".slots");
            ItemStack itemStack = new ItemStack(material, amount);
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(displayName);
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            for (Integer slot : slots) {
                shopInventory.setItem(slot, itemStack);
            }
        }
        Set<String> sections = config.getConfigurationSection("menus.shop-menus.sections").getKeys(false);
        HashMap<String, List<ItemOfSection>> sectionHashMap = new HashMap<>();
        for (String section : sections) {
            Set<String> items = config.getConfigurationSection("menus.shop-menus.sections." + section).getKeys(false);
            List<ItemOfSection> itemsValues = new ArrayList<>();
            for (String item : items) {
                String displayName = Utils.translateMessage(config.getString("menus.shop-menus.sections." + section + "." + item + ".display-name"));
                List<String> lore = Utils.translateListMessage(config.getStringList("menus.shop-menus.sections." + section + "." + item + ".lore"));
                Material material = Material.valueOf(config.getString("menus.shop-menus.sections." + section + "." + item + ".material"));
                int amount = config.getInt("menus.shop-menus.sections." + section + "." + item + ".amount");
                int slot = config.getInt("menus.shop-menus.sections." + section + "." + item + ".slot");
                Material requiredMaterial = Material.valueOf(config.getString("menus.shop-menus.sections." + section + "." + item + ".price.material-of-item"));
                int requiredAmount = config.getInt("menus.shop-menus.sections." + section + "." + item + ".price.amount-needed");
                List<String> enchants = config.getStringList("menus.shop-menus.sections." + section + "." + item + ".enchantments");
                String effect = config.getString("menus.shop-menus.sections." + section + "." + item + ".effect");
                ItemStack displayItem = new ItemStack(material, amount);
                ItemMeta meta = displayItem.getItemMeta();
                meta.setDisplayName(displayName);
                meta.setLore(lore);
                if (!enchants.isEmpty()) {
                    for (String splitString : enchants) {
                        String[] arrays = splitString.split(":");
                        String enchantName = arrays[0];
                        int enchantNumber = Integer.parseInt(arrays[1]);
                        Enchantment enchant = Enchantment.getByName(enchantName);
                        meta.addEnchant(enchant, enchantNumber, true);
                    }
                }
                displayItem.setItemMeta(meta);
                if (effect != null) {
                    PotionMeta potionMeta = (PotionMeta) meta;
                    String[] arrays = effect.split(":");
                    potionMeta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(arrays[0]), Integer.parseInt(arrays[2]) * 20, Integer.parseInt(arrays[1])), false);
                    displayItem.setItemMeta(potionMeta);
                }
                ItemStack requiredItem = new ItemStack(requiredMaterial, requiredAmount);
                itemsValues.add(new ItemOfSection(displayItem, requiredItem, slot));
            }
            sectionHashMap.put(section, itemsValues);
        }
        return new ItemShop(shopInventory, shopItemSlots, sectionHashMap, interactiveSlots);
    }

    @EventHandler
    public void onClickMenu(InventoryClickEvent e) {
        if (e.getCurrentItem() != null) {
            if (e.getView().getTitle().equals(shopTitleMenu)) {
                e.setCancelled(true);
                Arena arena = Utils.getPlayerArena((Player) e.getWhoClicked(), plugin.getArenas());
                if (arena != null) {
                    ItemStack selectedItem = e.getCurrentItem().clone();
                    int slot = e.getRawSlot();
                    Player player = (Player) e.getWhoClicked();
                    if (shop.getShopItemSlots().contains(slot)) {
                        String section = getSection(shop, e.getClickedInventory());
                        ItemOfSection item = shop.getShopInventorySections().get(section).stream().filter(i -> i.getSlot() == slot).collect(Collectors.toList()).get(0);
                        ItemStack required = item.getRequiredItem();
                        Material type = selectedItem.getType();
                        ItemMeta meta = selectedItem.getItemMeta();
                        String name = meta.getDisplayName();
                        List<String> lore = meta.getLore();
                        meta.setLore(null);
                        meta.setDisplayName(null);
                        selectedItem.setItemMeta(meta);
                        if (shop.getArmors().containsKey(type)) {
                            if (!checkBuy(required, player)) return;
                            ItemStack leggings = shop.getArmors().get(type);
                            TeamInfo team = arena.getPlayerTeamInfo(player.getUniqueId());
                            if (team.getUpgrades().getArmorIndex() != 0) {
                                meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, team.getUpgrades().getArmorIndex(), true);
                                leggings.setItemMeta(meta);
                            }
                            player.getInventory().setItem(EquipmentSlot.FEET, selectedItem);
                            player.getInventory().setItem(EquipmentSlot.LEGS, leggings);
                        } else if (shop.getAxes().contains(type) || shop.getPickAxes().contains(type)) {
                            boolean value = shop.getAxes().contains(type);
                            MaterialResult result = getMaterialInList(player.getInventory(), value ? shop.getAxes() : shop.getPickAxes());
                            Integer index = result.getIndex();
                            required = shop.getPrices().get(result.getIndexList());
                            if (!checkBuy(required, player)) return;
                            if (index != null) {
                                player.getInventory().setItem(index, selectedItem);
                            } else {
                                player.getInventory().addItem(selectedItem);
                            }
                            selectedItem.setType(result.nextMaterial);
                            meta.setLore(lore);
                            if (getShop().getEnchantments().containsKey(selectedItem.getType())) {
                                ItemShop.DoubleResult resultEnchants = getShop().getEnchantments().get(selectedItem.getType());
                                meta.addEnchant(resultEnchants.getEnchantment(), resultEnchants.getLevel(), true);
                            }
                            selectedItem.setItemMeta(meta);
                            e.getClickedInventory().setItem(slot, selectedItem);
                        } else if (plugin.getMenus().getShopSwords().contains(type)) {
                            if (!checkBuy(required, player)) return;
                            meta.setDisplayName(name);
                            if (player.getInventory().contains(Material.WOODEN_SWORD)) {
                                for (int slotItem = 0; slotItem < player.getInventory().getSize(); slotItem++) {
                                    ItemStack itemStack = player.getInventory().getItem(slotItem);
                                    if (itemStack != null && itemStack.getType().equals(Material.WOODEN_SWORD)) {
                                        if (arena.getPlayerTeamInfo(player.getUniqueId()).getUpgrades().getSharpness()) meta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
                                        selectedItem.setItemMeta(meta);
                                        player.getInventory().setItem(slotItem, selectedItem);
                                        return;
                                    }
                                }
                            }
                            selectedItem.setItemMeta(meta);
                            player.getInventory().addItem(selectedItem);
                        } else {
                            if (!checkBuy(required, player)) return;
                            if (!plugin.getMenus().containsBlock(type) && !TeamInfo.allWoolMaterials.contains(type)) {
                                meta.setDisplayName(name);
                                selectedItem.setItemMeta(meta);
                            }
                            player.getInventory().addItem(selectedItem);
                        }
                        player.sendMessage(Utils.translateMessage("&cAcabas de comprar x" + selectedItem.getAmount() + " " + name + "&c."));
                    } else if (shop.getInteractiveSlots().containsKey(slot)) {
                        changeSection(shop, slot, e.getClickedInventory(), e.getWhoClicked().getInventory(), shop.getInteractiveSlots().get(slot), arena.getPlayerTeamInfo(player.getUniqueId()));
                    }
                }
            } else if (e.getView().getTitle().equals(upgradesTitleMenu)) {
                e.setCancelled(true);
                Player player = (Player) e.getWhoClicked();
                Arena arena = Utils.getPlayerArena(player, plugin.getArenas());
                TeamInfo team = arena.getPlayerTeamInfo(player.getUniqueId());
                int slot = e.getRawSlot();
                if (upgrades.getUpgrades().containsKey(slot)){
                    ItemUpgrades.UpgradeItem upgradeItem = upgrades.getUpgrades().get(slot);
                    Upgrades teamUpgrades = team.getUpgrades();
                    switch (upgradeItem.getId()) {
                        case "generator":
                            if (teamUpgrades.getGeneratorIndex() == 4) {
                                player.sendMessage(Utils.translateMessage("&cYa tienes al nivel maximo esta mejora."));
                            } else if (checkBuy(upgradeItem.getRequiredLevels().get(teamUpgrades.getGeneratorIndex() + 1), player)) {
                                teamUpgrades.changeGeneratorLevel();
                                e.getClickedInventory().setItem(slot, itemUpgradeLevel(upgradeItem, team));
                            }
                            break;
                        case "fast-digging":
                            if (teamUpgrades.getFastDiggingIndex() == 2) {
                                player.sendMessage(Utils.translateMessage("&cYa tienes al nivel maximo esta mejora."));
                            } else if (checkBuy(upgradeItem.getRequiredLevels().get(teamUpgrades.getFastDiggingIndex() + 1), player)) {
                                teamUpgrades.changeFastDigging();
                                e.getClickedInventory().setItem(slot, itemUpgradeLevel(upgradeItem, team));
                            }
                            break;
                        case "regeneration":
                            if (teamUpgrades.getRegenerateIsland()) {
                                player.sendMessage(Utils.translateMessage("&cYa tienes al nivel maximo esta mejora."));
                            } else if (checkBuy(upgradeItem.getRequiredLevels().get(1), player)) {
                                teamUpgrades.changeRegeneration();
                                e.getClickedInventory().setItem(slot, itemUpgradeLevel(upgradeItem, team));
                            }
                            break;
                        case "sharpness":
                            if (teamUpgrades.getSharpness()) {
                                player.sendMessage(Utils.translateMessage("&cYa tienes al nivel maximo esta mejora."));
                            } else if (checkBuy(upgradeItem.getRequiredLevels().get(1), player)) {
                                teamUpgrades.changeSharpness();
                                e.getClickedInventory().setItem(slot, itemUpgradeLevel(upgradeItem, team));
                            }
                            break;
                        case "armor":
                            if (teamUpgrades.getArmorIndex() == 4) {
                                player.sendMessage(Utils.translateMessage("&cYa tienes al nivel maximo esta mejora."));
                            } else if (checkBuy(upgradeItem.getRequiredLevels().get(teamUpgrades.getArmorIndex() + 1), player)) {
                                teamUpgrades.changeArmorEnchant();
                                e.getClickedInventory().setItem(slot, itemUpgradeLevel(upgradeItem, team));
                            }
                    }
                } else if (upgrades.getTraps().containsKey(slot)) {
                    ItemUpgrades.TrapItem trapItem = upgrades.getTraps().get(slot);
                    ItemStack item = new ItemStack(trapItem.getMaterialOfItem());
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(trapItem.getDisplayName());
                    if (team.getUpgrades().getTraps().get(trapItem.getId())) {
                        player.sendMessage(Utils.translateMessage("&cEsta trampa todavía no fue usada, por lo cual no puedes comprar otra."));
                    } else if (checkBuy(trapItem.getRequired(), player)) {
                        player.sendMessage(Utils.translateMessage("&cAcabas de comprar la siguiente trampa: " + trapItem.getDisplayName()));
                        team.getUpgrades().getTraps().put(trapItem.getId(), true);
                    }
                }
            } else if (e.getSlotType().equals(InventoryType.SlotType.ARMOR)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onVillagerInteract(PlayerInteractEntityEvent e) {
        Arena arena = Utils.getPlayerArena(e.getPlayer(), plugin.getArenas());
        if (arena != null && e.getRightClicked().getType().equals(EntityType.VILLAGER)) {
            Villager villager = (Villager) e.getRightClicked();
            if (villager.getProfession().equals(Villager.Profession.ARMORER)) {
                e.setCancelled(true);
                openShopMenu(e.getPlayer(), arena.getPlayerTeamInfo(e.getPlayer().getUniqueId()), arena.getPlayersPerTeam());
            } else if (villager.getProfession().equals(Villager.Profession.LIBRARIAN)) {
                e.setCancelled(true);
                openUpgrades(e.getPlayer(), arena.getPlayerTeamInfo(e.getPlayer().getUniqueId()));
            }
        }
    }

    public boolean checkBuy(ItemStack required, Player player) {
        boolean value = player.getInventory().contains(required.getType(), required.getAmount());
        if (value) {
            int count = 0;
            for (ItemStack playerItem : player.getInventory().getContents()) {
                if (playerItem != null && playerItem.getType().equals(required.getType())) {
                    if (playerItem.getAmount() + count - required.getAmount() >= 0) {
                        playerItem.setAmount(playerItem.getAmount() - required.getAmount());
                        break;
                    } else if (playerItem.getAmount() - required.getAmount() < 0) {
                        count += playerItem.getAmount();
                        playerItem.setAmount(0);
                    }
                }
            }
        } else {
            player.sendMessage(Utils.translateMessage("&cNo tienes los suficientes articulos para comprar esto."));
        }
        return value;
    }

    public void openShopMenu(Player player, TeamInfo teamInfo, int playersPerTeam) {
        Inventory copy = Bukkit.createInventory(null, 54, shopTitleMenu);
        ItemStack[] content = shop.getShopInventory().getContents();
        copy.setContents(content);
        changeSection(shop, 0, copy, player.getInventory(), "fastbuy", teamInfo);
        player.openInventory(copy);
    }

    public String getSection(ItemShop shop, Inventory inventory) {
        for (Integer slot : shop.getInteractiveSlots().keySet()) {
            if (inventory.getItem(slot).getItemMeta().hasEnchants()) return shop.getInteractiveSlots().get(slot);
        }
        return null;
    }

    public void changeSection(ItemShop shop, Integer slotSection, Inventory inventory, Inventory playerInventory, String section, TeamInfo playerTeam) {
        for (Integer slotItem : shop.getShopItemSlots()) {
            ItemStack item = inventory.getItem(slotItem);
            if (item != null) inventory.removeItem(item);
        }
        for (ItemOfSection slot : shop.getShopInventorySections().get(section)) inventory.setItem(slot.getSlot(), slot.getDisplayItem());
        Enchantment enchant = Enchantment.DAMAGE_ALL;
        ItemStack itemStack = inventory.getItem(slotSection);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addEnchant(enchant, 1, true);
        itemStack.setItemMeta(itemMeta);
        List<Integer> slots = new ArrayList<>(shop.getInteractiveSlots().keySet());
        slots.remove(slotSection);
        for (Integer slot : shop.getShopItemSlots()) {
            ItemStack item = inventory.getItem(slot);
            if (item != null) {
                Material material = item.getType();
                if (material.equals(Material.WHITE_WOOL)) {
                    item.setType(playerTeam.getWoolMaterial());
                } else if (material.equals(Material.WOODEN_PICKAXE) || material.equals(Material.WOODEN_AXE)) {
                    boolean value = material == Material.WOODEN_PICKAXE;
                    MaterialResult info = getMaterialInList(playerInventory, value ? shop.getPickAxes() : shop.getAxes());
                    if (info.getIndex() != null) {
                        item.setType(info.getMaterial());
                    } else {
                        item.setType(value ? Material.WOODEN_PICKAXE : Material.WOODEN_AXE);
                    }
                    if (getShop().getEnchantments().containsKey(item.getType())) {
                        ItemMeta meta = item.getItemMeta();
                        ItemShop.DoubleResult result = getShop().getEnchantments().get(item.getType());
                        meta.addEnchant(result.getEnchantment(), result.getLevel(), true);
                        item.setItemMeta(meta);
                    }
                } else if (section.equals("armors")) {
                    if (playerTeam.getUpgrades().getArmorIndex() != 0) item.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, playerTeam.getUpgrades().getArmorIndex());
                } else if (section.equals("swords")) {
                    if (playerTeam.getUpgrades().getSharpness()) item.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                }
            }
        }
        for (Integer slot : slots) {
            ItemStack item = inventory.getItem(slot);
            ItemMeta meta = item.getItemMeta();
            if (meta.getEnchants().containsKey(enchant)) {
                meta.removeEnchant(enchant);
                item.setItemMeta(meta);
            }
        }
    }

    public MaterialResult getMaterialInList(Inventory inventory, List<Material> materials) {
        int size = materials.size();
        int index = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null) {
                for (int x = 0; x < size; x++) {
                    if (materials.get(x).equals(item.getType())) {
                        if (x == 0) {
                            return new MaterialResult(materials.get(x + 1), materials.get(x + 2), materials.get(x), index, x + 1);
                        } else if (x < size - 2) {
                            return new MaterialResult(materials.get(x + 1), materials.get(x + 2), materials.get(x - 1), index, x + 1);
                        } else if (x == size - 2) {
                            return new MaterialResult(materials.get(x + 1), materials.get(x + 1), materials.get(x - 1), index, x);
                        } else if (x == size - 1) {
                            return new MaterialResult(materials.get(x), materials.get(x), materials.get(x - 1), index, x);
                        }
                    }
                }
            }
            index++;
        }
        return new MaterialResult(materials.get(0), materials.get(1), materials.get(0), null, 0);
    }

    public static class MaterialResult {

        private final Material material;
        private final Material nextMaterial;
        private final Material beforeMaterial;
        private final Integer index;
        private final Integer indexList;

        public MaterialResult(Material material, Material nextMaterial, Material beforeMaterial, Integer index, Integer indexList) {
            this.material = material;
            this.nextMaterial = nextMaterial;
            this.beforeMaterial = beforeMaterial;
            this.index = index;
            this.indexList = indexList;
        }

        public Material getBeforeMaterial() {
            return beforeMaterial;
        }

        public Material getMaterial() {
            return material;
        }

        public Material getNextMaterial() {
            return nextMaterial;
        }

        public Integer getIndex() {
            return index;
        }

        public Integer getIndexList() {
            return indexList;
        }
    }

    public List<Material> getShopMaterials() {
        List<Material> materials = new ArrayList<>();
        for (ItemOfSection item : shop.getShopInventorySections().get("blocks")) {
            materials.add(item.getDisplayItem().getType());
        }
        materials.add(Material.TNT);
        return materials;
    }

    public boolean containsBlock(Material material) {
        return getShopMaterials().contains(material);
    }

    public List<Material> getShopSwords() {
        List<Material> materials = new ArrayList<>();
        for (ItemOfSection item : shop.getShopInventorySections().get("swords")) {
            materials.add(item.getDisplayItem().getType());
        }
        materials.add(Material.WOODEN_SWORD);
        return materials;
    }


    public ItemShop getShop() {
        return shop;
    }

    public void openUpgrades(Player player, TeamInfo team) {
        Inventory upgradesInventory = Bukkit.createInventory(null, 54, upgradesTitleMenu);
        for (Integer slotUpgrades : upgrades.getUpgrades().keySet()) {
            upgradesInventory.setItem(slotUpgrades, itemUpgradeLevel(upgrades.getUpgrades().get(slotUpgrades), team));
        }
        for (Integer slotTraps : upgrades.getTraps().keySet()) {
            upgradesInventory.setItem(slotTraps, itemTrap(upgrades.getTraps().get(slotTraps), team));
        }
        player.openInventory(upgradesInventory);
    }

    public ItemStack itemTrap(ItemUpgrades.TrapItem trapItem, TeamInfo teamInfo) {
        ItemStack item = new ItemStack(trapItem.getMaterialOfItem());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(trapItem.getDisplayName());
        if (teamInfo.getUpgrades().getTraps().get(trapItem.getId())) {
            meta.setLore(trapItem.getUseLore());
        } else {
            meta.setLore(trapItem.getBuyLore());
        }
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack itemUpgradeLevel(ItemUpgrades.UpgradeItem upgradeItem, TeamInfo teamPlayerInfo) {
        Upgrades teamUpgrades = teamPlayerInfo.getUpgrades();
        ItemStack item = new ItemStack(upgradeItem.getMaterialOfItem());
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(upgradeItem.getDisplayName());
        switch (upgradeItem.getId()) {
            case "generator":
                if (teamUpgrades.getGeneratorIndex() == 4) {
                    meta.setLore(upgradeItem.getPurchasedLore());
                } else {
                    meta.setLore(upgradeItem.getLoreLevels().get(teamUpgrades.getGeneratorIndex() + 1));
                }
                break;
            case "fast-digging":
                if (teamUpgrades.getFastDiggingIndex() == 2) {
                    meta.setLore(upgradeItem.getPurchasedLore());
                } else {
                    meta.setLore(upgradeItem.getLoreLevels().get(teamUpgrades.getFastDiggingIndex() + 1));
                }
                break;
            case "regeneration":
                if (teamUpgrades.getRegenerateIsland()) {
                    meta.setLore(upgradeItem.getPurchasedLore());
                } else {
                    meta.setLore(upgradeItem.getLoreLevels().get(1));
                }
                break;
            case "sharpness":
                if (teamUpgrades.getSharpness()) {
                    meta.setLore(upgradeItem.getPurchasedLore());
                } else {
                    meta.setLore(upgradeItem.getLoreLevels().get(1));
                }
                break;
            case "armor":
                if (teamUpgrades.getArmorIndex() == 4) {
                    meta.setLore(upgradeItem.getPurchasedLore());
                } else {
                    meta.setLore(upgradeItem.getLoreLevels().get(teamUpgrades.getArmorIndex() + 1));
                }
        }
        item.setItemMeta(meta);
        return item;
    }
}