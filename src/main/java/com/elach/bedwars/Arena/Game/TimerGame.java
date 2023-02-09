package com.elach.bedwars.Arena.Game;

import com.elach.bedwars.Arena.ArenaUtils.TeamInfo;
import com.elach.bedwars.Arena.System.Arena;
import com.elach.bedwars.BedWars;
import com.elach.bedwars.Utils.ScoreboardManager;
import com.elach.bedwars.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class TimerGame extends BukkitRunnable {

    private final BedWars plugin;

    private int count = 2100;
    private int eventId = 1;
    private int countScoreBoard;
    private final HashMap<Integer, String> eventNames;
    private final Arena arena;

    public TimerGame(Arena arena, BedWars plugin) {
        this.plugin = plugin;
        this.arena = arena;
        eventNames = new HashMap<>();
        eventNames.put(2, Utils.translateMessage("&fDiamante II &b"));
        eventNames.put(3, Utils.translateMessage("&fDiamante III &b"));
        eventNames.put(4, Utils.translateMessage("&fEsmeralda I &2"));
        eventNames.put(5, Utils.translateMessage("&fEsmeralda II &2"));
        eventNames.put(6, Utils.translateMessage("&fEsmeralda III &2"));
        countScoreBoard = 300;
    }

    @Override
    public void run() {
        ScoreboardManager.updateTimeArena(arena.getPlayers(), null, getTimeWithSeconds(countScoreBoard), false);
        countScoreBoard--;
        count--;
        switch (count) {
            case 1800:
            case 1500:
            case 1200:
                for (GlobalGenerator generator : arena.getGenerators().stream().filter(g -> g.getItem().getType().equals(Material.DIAMOND)).collect(Collectors.toList())) {
                    generator.getItem().setAmount(generator.getItem().getAmount() + 1);
                }
                eventId += 1;
                arena.sendBroadcast(Utils.translateMessage("&fLos generadores fueron cambiados a " + eventNames.get(eventId) + "..."));
                countScoreBoard = 300;
                ScoreboardManager.updateTimeArena(arena.getPlayers(), eventNames.get(eventId), getTimeWithSeconds(countScoreBoard), true);
                break;
            case 900:
            case 600:
            case 300:
                for (GlobalGenerator generator : arena.getGenerators().stream().filter(g -> g.getItem().getType().equals(Material.EMERALD)).collect(Collectors.toList())) {
                    generator.getItem().setAmount(generator.getItem().getAmount() + 1);
                }
                eventId += 1;
                arena.sendBroadcast(Utils.translateMessage("&fLos generadores fueron cambiados a " + eventNames.get(eventId) + "..."));
                countScoreBoard = 300;
                ScoreboardManager.updateTimeArena(arena.getPlayers(), eventNames.get(eventId), getTimeWithSeconds(countScoreBoard), true);
                break;
            case 0:
                arena.sendBroadcast(Utils.translateMessage("&cMuerte subita, las camas fueron destruidas..."));
                for (TeamInfo teamInfo : arena.getTeams()) {
                    if (teamInfo.isBed().equals(true)) {
                        Location bedLocation = teamInfo.getBedLocation();
                        bedLocation.getBlock().setType(Material.AIR);
                        bedLocation.getWorld().createExplosion(bedLocation, 2, false, false);
                    }
                    for (UUID playerTeamUUID : teamInfo.getTeamPlayers()) Bukkit.getPlayer(playerTeamUUID).sendTitle(Utils.translateMessage("&4&lMUERTE SUBITA"), Utils.translateMessage("&cLas camas fueron destruidas."));
                }
                ScoreboardManager.updateTimeArena(arena.getPlayers(), "&fMuerte súbita &4&k", "asd", true);
                arena.getRunnables().add(Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if (arena.getPlayers().isEmpty()) cancel();
                    for (UUID playerUUID : arena.getPlayers()) {
                        Player player = Bukkit.getPlayer(playerUUID);
                        if (player.getHealth() - 2 <= 0) {
                            arena.killPlayer(player);
                        } else {
                            player.setHealth(player.getHealth() - 2);
                        }
                    }
                }, 20, 20).getTaskId());
                cancel();
        }
    }

    public String getTimeWithSeconds(int counter) {
        int minutes = counter / 60;
        int seconds = counter - minutes * 60;
        return minutes + ":" + (String.valueOf(seconds).length() == 1 ? "0" + seconds : seconds);
    }
}
