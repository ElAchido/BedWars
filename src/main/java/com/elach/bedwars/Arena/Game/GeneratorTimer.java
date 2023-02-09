package com.elach.bedwars.Arena.Game;

import com.elach.bedwars.Utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class GeneratorTimer extends BukkitRunnable {

    private final Location spawnItemLocation;
    private ItemStack iron;
    private ItemStack gold;
    private Integer counterGold = 5;
    private ItemStack emerald;
    private Integer counterEmerald = null;

    public GeneratorTimer(Location spawnItemLocation, ItemStack iron, ItemStack gold, ItemStack emerald) {
        this.iron = iron;
        this.gold = gold;
        this.emerald = emerald;
        this.spawnItemLocation = spawnItemLocation.add(0.5, 0.5, 0.5);
    }

    @Override
    public void run() {
        Location location = spawnItemLocation.clone();
        if (Utils.getNumberDrops(location) <= 128) {
            location.add(0, 1.25, 0);
            spawnItem(iron);
            if (counterGold == 0) {
                spawnItem(gold);
                counterGold = 5;
            }
            if (counterEmerald != null) {
                counterEmerald--;
                if (counterEmerald == 0) {
                    spawnItem(emerald);
                    counterEmerald = 10;
                }
            }
            counterGold--;
        }
    }

    public void spawnItem(ItemStack item) {
        Location location = spawnItemLocation.clone();
        Entity entity = spawnItemLocation.getWorld().dropItem(location.add(0, 1, 0), item);
        entity.setCustomName("generatorDrop");
        entity.setVelocity(new Vector(0, 0, 0));
    }

    public void setCounterEmerald(Integer counterEmerald) {
        this.counterEmerald = counterEmerald;
    }

    public ItemStack getEmerald() {
        return emerald;
    }

    public ItemStack getGold() {
        return gold;
    }

    public ItemStack getIron() {
        return iron;
    }

    public void setIron(ItemStack iron) {
        this.iron = iron;
    }

    public void setGold(ItemStack gold) {
        this.gold = gold;
    }

    public void setEmerald(ItemStack emerald) {
        this.emerald = emerald;
    }
}
