package com.elach.bedwars.Arena.Game;

import com.elach.bedwars.Arena.ArenaUtils.TeamInfo;
import com.elach.bedwars.Arena.System.Arena;
import com.elach.bedwars.BedWars;
import com.elach.bedwars.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class PlayerChecker extends BukkitRunnable {

    private final BedWars plugin;
    private final Arena arena;

    public PlayerChecker(BedWars plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
    }

    @Override
    public void run() {
        List<UUID> players = arena.getPlayers();
        players.addAll(arena.getSpectators());
        for (UUID playerUUID : players) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null) continue;
            if (player.isFlying()) {
                if (player.getLocation().getY() <= -40) {
                    player.teleport(arena.getSpectatorArena());
                    return;
                }
                if (!arena.getArenaRegion().contains(player.getLocation())) {
                    player.teleport(arena.getSpectatorArena());
                    player.sendMessage(Utils.translateMessage("&cNo puedes salir del mapa."));
                    return;
                }
            }
            TeamInfo playerTeam = arena.getPlayerTeamInfo(playerUUID);
            if (playerTeam == null) return;
            if (playerTeam.getUpgrades().getFastDiggingPotionEffect() != null) {
                player.addPotionEffect(playerTeam.getUpgrades().getFastDiggingPotionEffect());
            }
            for (TeamInfo team : arena.getTeams()) {
                if (team.getIslandRegion().contains(player.getLocation())) {
                    if (!team.getId().equals(playerTeam.getId())) {
                        if (team.getUpgrades().triggerTrap(arena, player, team)) {
                            arena.getRunnables().add(Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                team.getUpgrades().setCooldownTraps(false);
                            }, 3600).getTaskId());
                        }
                    } else if (team.getUpgrades().getRegenerateIsland()) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 140, 1));
                    }
                }
            }
        }
    }
}
