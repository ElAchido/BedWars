package com.elach.bedwars.Arena.System;

import com.elach.bedwars.Arena.ArenaUtils.State;
import com.elach.bedwars.BedWars;
import com.elach.bedwars.Utils.Utils;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class Starting extends BukkitRunnable {

    private final Arena arena;
    private int count = 5;

    public Starting(BedWars plugin, Arena arena) {
        this.arena = arena;
        arena.setState(State.STARTING);
        arena.getRunnables().add(this.runTaskTimer(plugin, 20, 20).getTaskId());
    }

    @Override
    public void run() {
        List<UUID> players = arena.getPlayers();
        if (players.size() != arena.getTeams().size() * arena.getPlayersPerTeam()) {
            arena.sendBroadcast(Utils.translateMessage("&cNo se puede iniciar el minijuego debido a que no hay suficientes jugadores para iniciar."));
            arena.setState(State.WAITING);
            cancel();
            return;
        }
        if (count % 5 == 0 && count != 0) {
            arena.sendTitle("&cQuedan " + count + " segundos.", "&c¡Preparate!", 5, 20, 5);
            arena.sendBroadcast(Utils.translateMessage("&cQuedan " + count + " segundos para iniciar."));
        } else if (count > 0 && count < 5){
            arena.sendTitle("&cQuedan " + count + " segundos.", "&c¡Preparate!", 5, 20, 5);
            arena.sendBroadcast(Utils.translateMessage("&cQuedan " + count + " segundos para iniciar."));
        } else if (count == 0) {
            arena.start();
            cancel();
            return;
        }
        count--;
    }
}
