package com.elach.bedwars.Utils;

import com.elach.bedwars.Arena.ArenaUtils.TeamInfo;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.team.UnlimitedNametagManager;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ScoreboardManager {

    public static void setLobbyScoreboard(Player player) {
        Scoreboard lobby = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = lobby.registerNewObjective("sidebar", Criteria.DUMMY, Utils.translateMessage("&6&lBEDWARS"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = Utils.translateListMessage(Arrays.asList("&6&nmc.hytacraft.com", "&c", "&fRango: &e&kasd", "&b", "&fNombre: " + player.getName(), "&a"));
        int count = 1;
        for (String line : lines) {
            obj.getScore(line).setScore(count);
            count++;
        }
        player.setScoreboard(lobby);
    }

    public static void setLobbyPlayerName(TabPlayer player) {
        String playerName = Utils.translateMessage("&6&lRANGO &e");
        TabAPI.getInstance().getTablistFormatManager().setName(player, playerName + player.getName());
        TabAPI.getInstance().getTeamManager().setPrefix(player, playerName);
    }
    public static void setWaitingArenaScoreboard(Player player, String arenaName, int countWaitingPlayers, int maxPlayersArena) {
        Scoreboard waitingLobby = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective obj = waitingLobby.registerNewObjective("waiting", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(Utils.translateMessage("&6&lBEDWARS"));

        String playerName = Utils.translateMessage("&7" + player.getName());
        TabPlayer playerTab = TabAPI.getInstance().getPlayer(player.getUniqueId());
        TabAPI.getInstance().getTablistFormatManager().setName(playerTab, playerName);
        TabAPI.getInstance().getTeamManager().setPrefix(playerTab, "&7");

        List<String> lines = Utils.translateListMessage(Arrays.asList("&c", "&fArena: &c" + arenaName, "&b", "&fNombre: " + player.getName(), "&a"));
        int count = 4;
        for (String line : lines) {
            obj.getScore(line).setScore(count);
            count++;
        }

        Team waitingCount = waitingLobby.registerNewTeam("waitingCount");
        waitingCount.addEntry(ChatColor.YELLOW.toString());
        waitingCount.setPrefix(Utils.translateMessage("&fJugadores: "));
        waitingCount.setSuffix(Utils.translateMessage("&c" + countWaitingPlayers + "/" + maxPlayersArena));
        obj.getScore(ChatColor.YELLOW.toString()).setScore(3);

        obj.getScore(Utils.translateMessage("&d")).setScore(2);
        obj.getScore(Utils.translateMessage("&6&nmc.hytacraft.com")).setScore(1);

        player.setScoreboard(waitingLobby);
    }

    public static void setArenaScoreboard(Player player, TeamInfo playerTeam, List<TeamInfo> teams) {
        Scoreboard arena = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = arena.registerNewObjective("arena", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName(Utils.translateMessage("&6&lBEDWARS"));

        String prefix = Utils.translateMessage(playerTeam.getDisplayName() + playerTeam.getChatColor() + " ");
        TabPlayer playerTab = TabAPI.getInstance().getPlayer(player.getUniqueId());
        TabAPI.getInstance().getTablistFormatManager().setName(playerTab, prefix + player.getName());
        TabAPI.getInstance().getTeamManager().setPrefix(playerTab, prefix);

        int count = 1;
        obj.getScore(Utils.translateMessage("&6&nmc.hytacraft.com")).setScore(count++);
        obj.getScore(Utils.translateMessage(" ")).setScore(count++);

        player.setPlayerListName(playerTeam.getDisplayName() + playerTeam.getChatColor() + " " +  player.getName());

        for (TeamInfo team : teams) {
            Team teamValue = arena.registerNewTeam(team.getDisplayName());
            String teamColorEntry = team.getChatColor().toString();
            teamValue.addEntry(teamColorEntry);
            teamValue.setPrefix(Utils.translateMessage((playerTeam.getDisplayName().equals(team.getDisplayName()) ? "&6" : "&f") + "Equipo " + team.getDisplayName() + "&7: "));
            teamValue.setSuffix(Utils.translateMessage("&2&l✔"));
            obj.getScore(teamColorEntry).setScore(count++);
        }

        obj.getScore(Utils.translateMessage("  ")).setScore(count++);

        Team time = arena.registerNewTeam("time");
        String timeEntry = ChatColor.BOLD.toString();
        time.addEntry(timeEntry);
        time.setPrefix(Utils.translateMessage("&fDiamante I &b"));
        time.setSuffix(" ");
        obj.getScore(timeEntry).setScore(count++);

        obj.getScore(Utils.translateMessage("   ")).setScore(count++);
        obj.getScore(Utils.translateMessage("&fNombre: " + player.getName())).setScore(count++);
        obj.getScore(Utils.translateMessage("    ")).setScore(count);

        player.setScoreboard(arena);
    }

    // UPDATE VALUES (WAITING)

    public static void updateAllArenaWaitingPlayers(List<UUID> players, int playerWaitingCount, int maxPlayersArena) {
        for (UUID playerUUID : players) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null) continue;
            player.getScoreboard().getTeam("waitingCount").setSuffix(Utils.translateMessage("&c" + playerWaitingCount + "/" + maxPlayersArena));
        }
    }

    // UPDATE VALUES (ARENA)

    public static void updateAllArenaLivePlayers(List<UUID> players, TeamInfo team, List<UUID> teamPlayers) {
        String suffix = teamPlayers.isEmpty() ? Utils.translateMessage("&c&l✘") : Utils.translateMessage("&a" + teamPlayers.size());
        for (UUID playerUUID : players) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null) continue;
            player.getScoreboard().getTeam(team.getDisplayName()).setSuffix(suffix);
        }
    }

    public static void updateTimeArena(List<UUID> players, String prefix, String suffix, boolean changePrefix) {
        for (UUID playerUUID : players) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null) continue;
            Team team = player.getScoreboard().getTeam("time");
            team.setSuffix(Utils.translateMessage(suffix));
            if (changePrefix) team.setPrefix(Utils.translateMessage(prefix));
        }
    }
}
