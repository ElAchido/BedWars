package com.elach.bedwars.Arena.Game;

import com.elach.bedwars.BedWars;
import com.elach.bedwars.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GlobalGenerator extends BukkitRunnable {

    private final BedWars plugin;
    private int blockTaskId;

    private final Location spawnItemLocation;
    private ItemStack item;
    private int count = 60;
    private final List<UUID> hologramUUIDs;
    private String message;

    public GlobalGenerator(BedWars plugin, Location spawnItemLocation, Material block, ItemStack item, List<String> hologramText) {
        this.plugin = plugin;
        this.spawnItemLocation = spawnItemLocation.add(0.5, 0.5, 0.5);
        this.item = item;
        hologramUUIDs = new ArrayList<>();
        generateHolograms(hologramText);
        generateBlockHologram(block);
    }

    @Override
    public void run() {
        if (count == 0) {
            count = 30;
            Location spawn = spawnItemLocation.clone();
            if (Utils.getNumberDrops(spawn) <= 6) spawnItemLocation.getWorld().dropItem(spawn.add(0, 1.25, 0), item).setVelocity(new Vector(0, 0, 0));
        }
        ArmorStand armorStand = (ArmorStand) Bukkit.getEntity(hologramUUIDs.get(0));
        armorStand.setCustomName(message.replace("%seconds%", String.valueOf(count)));
        count--;
    }

    class BlockAnimation extends BukkitRunnable {

        private final ArmorStand block;

        private boolean isUp = true;
        private final float maxY;
        private final float minY;
        private final int maxYaw = 180;

        private final int minYaw = maxYaw * -1;
        private final int maxPitch = 90;
        private final int minPitch = maxPitch * -1;
        private boolean addYaw;
        private boolean addPitch;

        public BlockAnimation(ArmorStand block) {
            this.block = block;
            maxY = block.getLocation().getBlockY() + 0.5f;
            minY = maxY - 1;
            blockTaskId = runTaskTimer(plugin, 2, 2).getTaskId();
        }

        @Override
        public void run() {
            Location location = block.getLocation();
            location.setY(isUp ? location.getY() + 0.025 : location.getY() - 0.025);
            block.teleport(location);
            if (isUp && location.getY() >= maxY) isUp = false;
            else if (location.getY() <= minY) isUp = true;
            location.setYaw(addYaw ? location.getYaw() + 2 : location.getYaw() - 2);
            location.setPitch(addPitch ? location.getPitch() + 1 : location.getPitch() - 1);
            block.setRotation(location.getYaw(), location.getPitch());
            if (addYaw && location.getYaw() == maxYaw) addYaw = false;
            else if (location.getYaw() == minYaw) addYaw = true;
            if (addPitch && location.getPitch() == maxPitch) addPitch = false;
            else if (location.getPitch() == minPitch) addPitch = true;
        }
    }

    public void generateBlockHologram(Material block) {
        Location hologramLocation = spawnItemLocation.clone();
        hologramLocation.setY(hologramLocation.getY() + 1);
        ArmorStand armorStand = (ArmorStand) hologramLocation.getWorld().spawnEntity(hologramLocation.add(0, 0.3, 0), EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setInvulnerable(true);
        armorStand.setGravity(false);
        armorStand.setHelmet(new ItemStack(block));
        armorStand.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        hologramUUIDs.add(armorStand.getUniqueId());
        new BlockAnimation(armorStand);
    }

    public void generateHolograms(List<String> hologramText) {
        Location hologramLocation = spawnItemLocation.clone();
        hologramLocation.setY(hologramLocation.getY() + 1);
        message = hologramText.get(hologramText.size() - 1);
        for (int i = hologramText.size() - 1; i >= 0; i--) {
            ArmorStand armorStand = (ArmorStand) hologramLocation.getWorld().spawnEntity(hologramLocation.add(0, 0.3, 0), EntityType.ARMOR_STAND);
            armorStand.setVisible(false);
            armorStand.setInvulnerable(true);
            armorStand.setGravity(false);
            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName(hologramText.get(i));
            hologramUUIDs.add(armorStand.getUniqueId());
        }
    }

    public void removeHolograms() {
        Bukkit.getScheduler().cancelTask(blockTaskId);
        for (UUID hologramUUID : hologramUUIDs) {
            Bukkit.getEntity(hologramUUID).remove();
        }
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }
}
