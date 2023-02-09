package com.elach.bedwars.Arena.Game;

import com.elach.bedwars.Arena.System.Arena;
import com.elach.bedwars.BedWars;
import com.elach.bedwars.Menus.ItemShop;
import com.elach.bedwars.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class RespawnTimer extends BukkitRunnable {


    private final Arena arena;
    private final Player player;
    private final ItemStack[] armorContents;
    private ItemStack pickAxe;
    private ItemStack axe;
    private final Location spawn;
    private int count = 5;

    public RespawnTimer(BedWars plugin, Arena arena, Player player, Location spawn) {
        this.arena = arena;
        this.player = player;
        armorContents = player.getInventory().getArmorContents().clone();
        player.setAllowFlight(true);
        player.setFlying(true);

        for (UUID playerUUID : arena.getPlayers()) {
            Player target = Bukkit.getPlayer(playerUUID);
            if (target == null) continue;
            target.hidePlayer(player);
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                if (plugin.getMenus().getShop().getAxes().contains(item.getType())) {
                    axe = item;
                } else if (plugin.getMenus().getShop().getPickAxes().contains(item.getType())) {
                    pickAxe = item;
                }
            }
        }

        if (pickAxe != null) {
            pickAxe = new ItemStack(plugin.getMenus().getMaterialInList(player.getInventory(), plugin.getMenus().getShop().getPickAxes()).getBeforeMaterial());
            ItemMeta meta = pickAxe.getItemMeta();
            ItemShop.DoubleResult result = plugin.getMenus().getShop().getEnchantments().get(pickAxe.getType());
            if (result != null) {
                meta.addEnchant(result.getEnchantment(), result.getLevel(), true);
                pickAxe.setItemMeta(meta);
            }
        }

        if (axe != null) {
            axe = new ItemStack(plugin.getMenus().getMaterialInList(player.getInventory(), plugin.getMenus().getShop().getAxes()).getBeforeMaterial());
            ItemMeta meta = axe.getItemMeta();
            ItemShop.DoubleResult result = plugin.getMenus().getShop().getEnchantments().get(axe.getType());
            if (result != null) {
                meta.addEnchant(result.getEnchantment(), result.getLevel(), true);
                axe.setItemMeta(meta);
            }
        }

        player.getInventory().clear();
        player.updateInventory();
        this.spawn = spawn;
    }

    @Override
    public void run() {
        if (arena.getPlayers().contains(player.getUniqueId())) {
            if (count == 0) {
                if (axe != null) {
                    player.getInventory().addItem(axe);
                }
                if (pickAxe != null) {
                    player.getInventory().addItem(pickAxe);
                }
                ItemStack sword = new ItemStack(Material.WOODEN_SWORD);
                if (arena.getPlayerTeamInfo(player.getUniqueId()).getUpgrades().getSharpness()) {
                    ItemMeta meta = sword.getItemMeta();
                    meta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
                    sword.setItemMeta(meta);
                }
                player.getInventory().addItem(sword);
                player.getInventory().setArmorContents(armorContents);
                player.sendTitle(Utils.translateMessage("&aReapareciste!"), Utils.translateMessage("&aTen cuidado!"), 10, 20, 10);
                for (UUID playerUUID : arena.getPlayers()) {
                    Player target = Bukkit.getPlayer(playerUUID);
                    if (target == null) continue;
                    target.showPlayer(player);
                }
                player.teleport(spawn);
                player.setFlying(false);
                player.setAllowFlight(false);
                cancel();
                return;
            }
            player.sendTitle(Utils.translateMessage("&cAcabas de morir."), Utils.translateMessage("&cReaparecerás en &4" + count + "&c segundos..."), 1, 20, 1);
            count--;
        } else {
            cancel();
        }
    }
}