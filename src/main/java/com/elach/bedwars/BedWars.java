package com.elach.bedwars;

import com.elach.bedwars.Arena.ArenaUtils.TeamInfo;
import com.elach.bedwars.Arena.Database.MySQL;
import com.elach.bedwars.Arena.System.Arena;
import com.elach.bedwars.Commands.BedWarsCommand;
import com.elach.bedwars.Arena.System.Config;
import com.elach.bedwars.Menus.Menus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class BedWars extends JavaPlugin {

    private MySQL mySQL;
    private List<Arena> arenas;
    private Location spawnLocation;
    public boolean isDisabled;
    private Menus menus;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        mySQL = new MySQL(getConfig());
        arenas = new ArrayList<>();
        menus = new Menus(this);
        TeamInfo.allBedMaterials = new ArrayList<>();
        TeamInfo.allWoolMaterials = new ArrayList<>();
        new Config(this);
        Bukkit.getPluginManager().registerEvents(new EventsBedWars(this), this);
        Bukkit.getPluginManager().registerEvents(menus, this);
        getCommand("bedwars").setExecutor(new BedWarsCommand(this));
    }

    @Override
    public void onDisable() {
        isDisabled = true;
        for (Arena arena : arenas) {
            arena.stop();
        }
    }

    public void setArenas(List<Arena> arenas) {
        this.arenas = arenas;
    }

    public List<Arena> getArenas() {
        return arenas;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public Menus getMenus() {
        return menus;
    }

    public MySQL getMySQL() {
        return mySQL;
    }
}
