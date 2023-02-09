package com.elach.bedwars.Commands;

import com.elach.bedwars.Arena.ArenaUtils.State;
import com.elach.bedwars.Arena.System.Arena;
import com.elach.bedwars.BedWars;
import com.elach.bedwars.Utils.ScoreboardManager;
import com.elach.bedwars.Utils.Utils;
import me.neznamy.tab.api.TabAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class BedWarsCommand implements CommandExecutor {

    private final BedWars plugin;

    public BedWarsCommand(BedWars plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 2 && args[0].equals("join")) {
                for (Arena arenaCheck : plugin.getArenas()) {
                    if (arenaCheck.getPlayers().contains(((Player) sender).getUniqueId())) {
                        sender.sendMessage(Utils.translateMessage("&cYa te encuentras actualmente en una arena."));
                        return false;
                    }
                }
                List<Arena> result = plugin.getArenas().stream().filter(a -> a.getId().equals(args[1])).collect(Collectors.toList());
                if (result.isEmpty()) {
                    sender.sendMessage(Utils.translateMessage("&cNo hay una arena con el nombre introducido."));
                } else {
                    Arena arena = result.get(0);
                    if (!arena.getState().equals(State.LIVE)) {
                        if (arena.getPlayers().size() < arena.getTeams().size() * arena.getPlayersPerTeam()) {
                            if (!arena.getPlayers().contains(((Player) sender).getUniqueId())) {
                                arena.addPlayer((Player) sender);
                                int size = arena.getPlayers().size();
                                int maxSize = arena.getTeams().size() * arena.getPlayersPerTeam();
                                ScoreboardManager.setWaitingArenaScoreboard((Player) sender, arena.getName(), size, maxSize);
                                ScoreboardManager.updateAllArenaWaitingPlayers(arena.getPlayers(), size, maxSize);
                            } else {
                                sender.sendMessage(Utils.translateMessage("&cYa estas dentro de esta arena."));
                            }
                        } else {
                            sender.sendMessage(Utils.translateMessage("&cNo puedes entrar a esta arena debido a que esta llena."));
                        }
                    } else {
                        sender.sendMessage(Utils.translateMessage("&cNo puedes entrar debido a que el juego ya comenzo."));
                    }
                }
            } else if (args.length == 1 && args[0].equals("leave")) {
                Arena arena = Utils.getPlayerArena(((Player) sender), plugin.getArenas());
                if (arena == null) {
                    sender.sendMessage(Utils.translateMessage("&cNo te encuentras en una arena actualmente."));
                } else {
                    arena.removePlayerArena((Player) sender);
                    arena.sendBroadcast(Utils.translateMessage("&cEl jugador " + sender.getName() + " acaba de abandonar la arena." + (arena.getState().equals(State.LIVE) ? "" : "(" + arena.getPlayers().size() + "/" + arena.getTeams().size() * arena.getPlayersPerTeam() + ")")));
                    sender.sendMessage(Utils.translateMessage("&cSaliendo de la arena actual..."));
                    ScoreboardManager.setLobbyScoreboard((Player) sender);
                    ScoreboardManager.setLobbyPlayerName(TabAPI.getInstance().getPlayer(((Player) sender).getUniqueId()));
                    if (arena.getState().equals(State.WAITING)) ScoreboardManager.updateAllArenaWaitingPlayers(arena.getPlayers(), arena.getPlayers().size(), arena.getTeams().size());
                }
            }
        }
        return false;
    }
}