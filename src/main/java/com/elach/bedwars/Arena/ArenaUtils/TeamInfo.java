package com.elach.bedwars.Arena.ArenaUtils;

import com.elach.bedwars.Arena.Game.GeneratorTimer;
import com.elach.bedwars.BedWars;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TeamInfo {

    private final String id;
    private final String displayName;
    private final ChatColor chatColor;
    private final Color color;
    public static List<Material> allBedMaterials;
    public static List<Material> allWoolMaterials;
    private final Material woolMaterial;
    private final Material bedMaterial;
    private Boolean isBed;
    private final Location spawn;
    private final Upgrades upgrades;
    private Inventory inventory;
    private final Location generatorLocation;
    private final Region islandRegion;
    private final Region noBuildRegion;
    private final Location villagerShopItems;
    private final Location villagerShopUpgrades;
    private GeneratorTimer generator;
    private final List<UUID> teamPlayers;
    private final BedLocation bedLocation;

    public TeamInfo(BedWars plugin,
                    String id,
                    String displayName,
                    ChatColor chatColor,
                    Color color,
                    Material woolMaterial,
                    Material bedMaterial,
                    Location spawn,
                    Location generator,
                    Region islandRegion,
                    Region noBuildRegion,
                    Location villagerShopItems,
                    Location villagerShopUpgrades,
                    BedLocation bedLocation)
    {
        this.upgrades = new Upgrades(plugin, this);
        this.id = id;
        this.displayName = displayName;
        this.chatColor = chatColor;
        this.color = color;
        this.woolMaterial = woolMaterial;
        allWoolMaterials.add(woolMaterial);
        this.bedMaterial = bedMaterial;
        allBedMaterials.add(bedMaterial);
        this.spawn = spawn;
        this.generatorLocation = generator;
        this.islandRegion = islandRegion;
        this.noBuildRegion = noBuildRegion;
        this.villagerShopItems = villagerShopItems;
        this.villagerShopUpgrades = villagerShopUpgrades;
        teamPlayers = new ArrayList<>();
        this.bedLocation = bedLocation;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getWoolMaterial() {
        return woolMaterial;
    }

    public Material getBedMaterial() {
        return bedMaterial;
    }

    public Color getColor() {
        return color;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public Location getSpawn() {
        return spawn;
    }

    public Location getGeneratorLocation() {
        return generatorLocation;
    }

    public Region getIslandRegion() {
        return islandRegion;
    }

    public Region getNoBuildRegion() {
        return noBuildRegion;
    }

    public Location getVillagerShopItems() {
        return villagerShopItems;
    }

    public Location getVillagerShopUpgrades() {
        return villagerShopUpgrades;
    }

    public List<UUID> getTeamPlayers() {
        return new ArrayList<>(teamPlayers);
    }

    public BedLocation getBedLocation() {
        return bedLocation;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Boolean isBed() {
        return isBed;
    }

    public void setBedValue(Boolean bed) {
        isBed = bed;
    }

    public void setGenerator(GeneratorTimer generator) {
        this.generator = generator;
    }

    public GeneratorTimer getGenerator() {
        return generator;
    }

    public Upgrades getUpgrades() {
        return upgrades;
    }

    public void addPlayer(Player player) {
        teamPlayers.add(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        teamPlayers.remove(player.getUniqueId());
    }
}
