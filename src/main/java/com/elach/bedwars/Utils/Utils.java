package com.elach.bedwars.Utils;

import com.elach.bedwars.Arena.System.Arena;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Utils {

    public static String translateMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> translateListMessage(List<String> lines) {
        List<String> translated = new ArrayList<>();
        for (String line : lines) {
            translated.add(translateMessage(line));
        }
        return translated;
    }

    public static UUID spawnNPC(Location location, String displayName, boolean isShop) {
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setProfession(isShop ? Villager.Profession.ARMORER : Villager.Profession.LIBRARIAN);
        villager.setAI(false);
        villager.setCollidable(false);
        villager.setCanPickupItems(false);
        villager.setCustomName(Utils.translateMessage(displayName));
        villager.setCustomNameVisible(true);
        return villager.getUniqueId();
    }

    public static Arena getPlayerArena(Player player, List<Arena> arenas) {
        for (Arena arena : arenas) {
            if (arena.getPlayers().contains(player.getUniqueId())) {
                return arena;
            }
        }
        return null;
    }

    public static Integer getNumberDrops(Location spawn) {
        Block generatorBlock = spawn.getBlock();
        spawn.add(0, -0.25, 0);
        int count = 0;
        for (Entity entity : spawn.getWorld().getEntities().stream().filter(m -> m instanceof Item).collect(Collectors.toList())) {
            Block entityBlock = entity.getLocation().getBlock();
            if (entityBlock.getLocation().getBlockX() == generatorBlock.getLocation().getBlockX() && entityBlock.getLocation().getBlockZ() == generatorBlock.getLocation().getBlockZ()) {
                Item item = (Item) entity;
                count += item.getItemStack().getAmount();
            }
        }
        return count;
    }

    public static ItemStack getConfigItem(FileConfiguration config, String path) {
        ItemStack item = new ItemStack(Material.valueOf(config.getString(path + ".material")));
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Utils.translateMessage(config.getString(path + ".display-name")));
        meta.setLore(Utils.translateListMessage(config.getStringList(path + ".lore")));
        item.setItemMeta(meta);
        return item;
    }
}