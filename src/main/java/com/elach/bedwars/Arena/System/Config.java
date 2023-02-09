package com.elach.bedwars.Arena.System;

import com.elach.bedwars.Arena.ArenaUtils.BedLocation;
import com.elach.bedwars.Arena.ArenaUtils.Region;
import com.elach.bedwars.Arena.ArenaUtils.TeamInfo;
import com.elach.bedwars.BedWars;
import com.elach.bedwars.Utils.Utils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Config {

    private final BedWars plugin;

    public Config(BedWars plugin) {
        this.plugin = plugin;
        FileConfiguration config = this.plugin.getConfig();
        plugin.setArenas(getArenas(config));
        plugin.setSpawnLocation(getMainSpawn(config));
    }

    public Location getMainSpawn(FileConfiguration config) {
        return new Location(
                Bukkit.getWorld(config.getString("spawn.location.world")),
                config.getDouble("spawn.location.x"),
                config.getDouble("spawn.location.y"),
                config.getDouble("spawn.location.z"),
                (float) config.getDouble("spawn.location.yaw"),
                (float) config.getDouble("spawn.location.pitch")
        );
    }

    public List<Arena> getArenas(FileConfiguration config) {
        List<Arena> arenas = new ArrayList<>();
        Set<String> arenasList = config.getConfigurationSection("arenas").getKeys(false);
        for (String arena : arenasList) {
            String worldName = config.getString("arenas." + arena + ".world");
            World world = Bukkit.createWorld(new WorldCreator(worldName).generator("VoidGen"));
            world.setTime(1000);
            world.getEntities().removeIf(e -> e.getType().equals(EntityType.VILLAGER));
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            world.setGameRule(GameRule.DO_FIRE_TICK, false);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            Region arenaMap = new Region(
                    world,
                    config.getInt("arenas." + arena + ".pos1.x"),
                    config.getInt("arenas." + arena + ".pos1.y"),
                    config.getInt("arenas." + arena + ".pos1.z"),
                    config.getInt("arenas." + arena + ".pos2.x"),
                    config.getInt("arenas." + arena + ".pos2.y"),
                    config.getInt("arenas." + arena + ".pos2.z")
            );
            Location spawn = new Location(
                    world,
                    config.getDouble("arenas." + arena + ".spawn.x"),
                    config.getDouble("arenas." + arena + ".spawn.y"),
                    config.getDouble("arenas." + arena + ".spawn.z"),
                    (float) config.getDouble("arenas." + arena + ".spawn.yaw"),
                    (float) config.getDouble("arenas." + arena + ".spawn.pitch"));
            Location spectator = new Location(
                    world,
                    config.getDouble("arenas." + arena + ".spectator.x"),
                    config.getDouble("arenas." + arena + ".spectator.y"),
                    config.getDouble("arenas." + arena + ".spectator.z"),
                    (float) config.getDouble("arenas." + arena + ".spectator.yaw"),
                    (float) config.getDouble("arenas." + arena + ".spectator.pitch"));
            String shopDisplayName = config.getString("arenas." + arena + ".villagers.shop.display-name");
            String upgradesDisplayName = config.getString("arenas." + arena + ".villagers.upgrades.display-name");
            HashMap<Location, Integer> generatorsLocations = new HashMap<>();
            Set<String> diamondLocations = config.getConfigurationSection("arenas." + arena + ".generators.diamond").getKeys(false);
            Set<String> emeraldLocations = config.getConfigurationSection("arenas." + arena + ".generators.emerald").getKeys(false);
            for (String diamond : diamondLocations) {
                Location diamondLocation = new Location(
                        world,
                        config.getDouble("arenas." + arena + ".generators.diamond." + diamond + ".x"),
                        config.getDouble("arenas." + arena + ".generators.diamond." + diamond + ".y"),
                        config.getDouble("arenas." + arena + ".generators.diamond." + diamond + ".z")
                );
                generatorsLocations.put(diamondLocation, 0);
            }
            for (String emerald : emeraldLocations) {
                Location emeraldLocation = new Location(
                        world,
                        config.getDouble("arenas." + arena + ".generators.emerald." + emerald + ".x"),
                        config.getDouble("arenas." + arena + ".generators.emerald." + emerald + ".y"),
                        config.getDouble("arenas." + arena + ".generators.emerald." + emerald + ".z")
                );
                generatorsLocations.put(emeraldLocation, 1);
            }
            List<String> diamondText = Utils.translateListMessage(config.getStringList("arenas." + arena + ".generators.display-text.diamond"));
            List<String> emeraldText = Utils.translateListMessage(config.getStringList("arenas." + arena + ".generators.display-text.emerald"));
            int playersPerTeam = config.getInt("arenas." + arena + ".players-per-team");
            List<TeamInfo> teams = new ArrayList<>();
            Set<String> teamsList = config.getConfigurationSection("arenas." + arena + ".teams").getKeys(false);
            for (String team : teamsList) {
                String displayName = Utils.translateMessage(config.getString(Utils.translateMessage("teams." + team + ".display-name")));
                String colorId = config.getString("teams." + team + ".chat-color");
                ChatColor teamChatColor = ChatColor.valueOf(colorId);
                Color color = getColorString(colorId);
                Material woolMaterial = Material.valueOf(config.getString("teams." + team + ".wool-material"));
                Material bedMaterial = Material.valueOf(config.getString("teams." + team + ".bed-material"));
                Location spawnTeam = new Location(
                        world,
                        config.getDouble("arenas." + arena + ".teams." + team + ".locations.spawn.x"),
                        config.getDouble("arenas." + arena + ".teams." + team + ".locations.spawn.y"),
                        config.getDouble("arenas." + arena + ".teams." + team + ".locations.spawn.z"),
                        (float) config.getDouble("arenas." + arena + ".teams." + team + ".locations.spawn.yaw"),
                        (float) config.getDouble("arenas." + arena + ".teams." + team + ".locations.spawn.pitch")
                );
                Location generator = new Location(
                        world,
                        config.getDouble("arenas." + arena + ".teams." + team + ".locations.generator.x"),
                        config.getDouble("arenas." + arena + ".teams." + team + ".locations.generator.y"),
                        config.getDouble("arenas." + arena + ".teams." + team + ".locations.generator.z")
                );
                Region islandRegion = new Region(
                        world,
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.islandRegion.x1"),
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.islandRegion.y1"),
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.islandRegion.z1"),
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.islandRegion.x2"),
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.islandRegion.y2"),
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.islandRegion.z2")
                );
                Region noBuildRegion = new Region(
                        world,
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.noBuildRegion.x1"),
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.noBuildRegion.y1"),
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.noBuildRegion.z1"),
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.noBuildRegion.x2"),
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.noBuildRegion.y2"),
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.noBuildRegion.z2")
                );
                Location villagerShopItems = new Location(
                        world,
                        config.getDouble("arenas." + arena + ".teams." + team + ".locations.villagers.shop-items.x"),
                        config.getDouble("arenas." + arena + ".teams." + team + ".locations.villagers.shop-items.y"),
                        config.getDouble("arenas." + arena + ".teams." + team + ".locations.villagers.shop-items.z"),
                        (float) config.getDouble("arenas." + arena + ".teams." + team + ".locations.villagers.shop-items.yaw"),
                        (float) config.getDouble("arenas." + arena + ".teams." + team + ".locations.villagers.shop-items.pitch")
                );
                Location villagerShopUpgrades = new Location(
                        world,
                        config.getDouble("arenas." + arena + ".teams." + team + ".locations.villagers.shop-upgrades.x"),
                        config.getDouble("arenas." + arena + ".teams." + team + ".locations.villagers.shop-upgrades.y"),
                        config.getDouble("arenas." + arena + ".teams." + team + ".locations.villagers.shop-upgrades.z"),
                        (float) config.getDouble("arenas." + arena + ".teams." + team + ".locations.villagers.shop-upgrades.yaw"),
                        (float) config.getDouble("arenas." + arena + ".teams." + team + ".locations.villagers.shop-upgrades.pitch")
                );
                BedLocation bedLocation = new BedLocation(
                        world,
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.bed.x"),
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.bed.y"),
                        config.getInt("arenas." + arena + ".teams." + team + ".locations.bed.z"),
                        BlockFace.valueOf(config.getString("arenas." + arena + ".teams." + team + ".locations.bed.facing").toUpperCase())
                );
                teams.add(new TeamInfo(plugin, team, displayName, teamChatColor, color, woolMaterial, bedMaterial, spawnTeam, generator, islandRegion, noBuildRegion, villagerShopItems, villagerShopUpgrades, bedLocation));
            }
            String name = config.getString("arenas." + arena + ".arena-name");
            arenas.add(new Arena(plugin, name, arena, playersPerTeam, teams, arenaMap, spawn, spectator, generatorsLocations, diamondText, emeraldText, shopDisplayName, upgradesDisplayName));
        }
        return arenas;
    }

    public Color getColorString(String color) {
        switch (color) {
            case "AQUA":
                return Color.AQUA;
            case "BLACK":
                return Color.BLACK;
            case "BLUE":
                return Color.BLUE;
            case "FUCHSIA":
                return Color.FUCHSIA;
            case "GRAY":
                return Color.GRAY;
            case "GREEN":
                return Color.GREEN;
            case "LIME":
                return Color.LIME;
            case "MAROON":
                return Color.MAROON;
            case "NAVY":
                return Color.NAVY;
            case "OLIVE":
                return Color.OLIVE;
            case "ORANGE":
                return Color.ORANGE;
            case "PURPLE":
                return Color.PURPLE;
            case "RED":
                return Color.RED;
            case "SILVER":
                return Color.SILVER;
            case "TEAL":
                return Color.TEAL;
            case "WHITE":
                return Color.WHITE;
            case "YELLOW":
                return Color.YELLOW;
        }
        return null;
    }
}