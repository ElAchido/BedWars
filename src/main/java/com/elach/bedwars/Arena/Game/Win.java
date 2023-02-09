package com.elach.bedwars.Arena.Game;

import com.elach.bedwars.Arena.ArenaUtils.State;
import com.elach.bedwars.Arena.System.Arena;
import com.elach.bedwars.BedWars;
import com.elach.bedwars.Utils.ScoreboardManager;
import me.neznamy.tab.api.TabAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.UUID;

public class Win extends BukkitRunnable {

    private final BedWars plugin;

    private Integer amount;
    private final List<UUID> winners;
    private final Arena arena;
    private final Color color;

    public Win(BedWars plugin, Integer amount, Arena arena, List<UUID> winners, Color teamColor) {
        this.plugin = plugin;
        this.winners = winners;
        this.amount = amount;
        this.arena = arena;
        this.color = teamColor;
        for (UUID winnerUUID : winners) {
            Player winner = Bukkit.getPlayer(winnerUUID);
            winner.setHealth(20);
            winner.setFoodLevel(20);
            winner.getInventory().clear();
            winner.getEnderChest().clear();
            winner.setInvulnerable(true);
            winner.playSound(winner, Sound.ENTITY_ENDER_DRAGON_DEATH, 1, 1);
        }
        this.runTaskTimer(plugin, 20, 20);
    }

    @Override
    public void run() {
        if (amount == 0) {
            arena.stop();
            cancel();
            return;
        }
        for (UUID winnerUUID : winners) {
            Player winner = Bukkit.getPlayer(winnerUUID);
            if (winner == null) winners.remove(winnerUUID);
            Firework firework = winner.getWorld().spawn(winner.getLocation(), Firework.class);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.setPower(1);
            meta.addEffect(FireworkEffect.builder().withColor(color).with(FireworkEffect.Type.BALL_LARGE).build());
            firework.setFireworkMeta(meta);
        }
        amount--;
    }
}