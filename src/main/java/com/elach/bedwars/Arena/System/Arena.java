package com.elach.bedwars.Arena.System;

import com.elach.bedwars.Arena.ArenaUtils.*;
import com.elach.bedwars.Arena.Game.*;
import com.elach.bedwars.BedWars;
import com.elach.bedwars.Utils.ScoreboardManager;
import com.elach.bedwars.Utils.Utils;
import me.neznamy.tab.api.TabAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.*;
import java.util.stream.Collectors;

public class Arena {

    private final BedWars plugin;

    private State state;
    private final String name;
    private final String id;
    private final int playersPerTeam;
    private final Region arena;
    private final Location spawnArena;
    private final Location spectatorArena;
    private final List<UUID> players;
    private final List<UUID> spectators;
    private final List<TeamInfo> teams;
    private final List<Integer> runnables;
    private final HashMap<Location, Integer> generatorLocations;
    private final List<String> diamondText;
    private final List<String> emeraldText;
    private final List<GlobalGenerator> generators;
    private final List<UUID> npcs;
    private final String shopDisplayName;
    private final String upgradeDisplayName;
    private final BossBar bossBar;

    public Arena(BedWars plugin,
                 String name,
                 String id,
                 int playersPerTeam,
                 List<TeamInfo> teamInfo,
                 Region arena,
                 Location spawnArena,
                 Location spectatorArena,
                 HashMap<Location, Integer> generatorLocations,
                 List<String> diamondText,
                 List<String> emeraldText,
                 String shopDisplayName,
                 String upgradeDisplayName) {
        this.plugin = plugin;
        this.name = name;
        this.id = id;
        this.playersPerTeam = playersPerTeam;
        this.arena = arena;
        this.teams = teamInfo;
        this.shopDisplayName = shopDisplayName;
        this.upgradeDisplayName = upgradeDisplayName;
        bossBar = Bukkit.createBossBar(Utils.translateMessage("&cZona de espera..."), BarColor.RED, BarStyle.SOLID);
        runnables = new ArrayList<>();
        generators = new ArrayList<>();
        npcs = new ArrayList<>();
        this.generatorLocations = generatorLocations;
        for (TeamInfo team : teamInfo) team.setBedValue(null);
        players = new ArrayList<>();
        spectators = new ArrayList<>();
        this.spawnArena = spawnArena;
        this.spectatorArena = spectatorArena;
        this.diamondText = diamondText;
        this.emeraldText = emeraldText;
        state = State.WAITING;
    }

    public void start() {
        List<Player> nonArenaPlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        nonArenaPlayers.removeIf(p -> players.contains(p.getUniqueId()));

        List<UUID> listPlayers = getPlayers();
        for (TeamInfo teamInformation : teams) {
            teamInformation.setInventory(Bukkit.createInventory(null, InventoryType.CHEST, teamInformation.getDisplayName()));

            teamInformation.setBedValue(true);

            teamInformation.getUpgrades().startArena();

            BedLocation bed = teamInformation.getBedLocation();
            Block block = bed.getBlock();
            for (Bed.Part part : Bed.Part.values()) {
                block.setBlockData(Bukkit.createBlockData(teamInformation.getBedMaterial(), (data) -> {
                    ((Bed) data).setPart(part);
                    ((Bed) data).setFacing(bed.getFacing());
                }));
                block.setMetadata("team", new FixedMetadataValue(plugin, teamInformation.getId()));
                block = block.getRelative(bed.getFacing().getOppositeFace());
            }

            Location villagerShop = teamInformation.getVillagerShopItems();
            Location villagerUpgrades = teamInformation.getVillagerShopUpgrades();
            npcs.add(Utils.spawnNPC(villagerShop, shopDisplayName, true));
            npcs.add(Utils.spawnNPC(villagerUpgrades, upgradeDisplayName, false));

            Random random = new Random();
            for (int player = 0; player < playersPerTeam; player++) {
                int index = random.nextInt(listPlayers.size());
                Player playerTeam = Bukkit.getPlayer(listPlayers.get(index));
                teamInformation.addPlayer(playerTeam);
                ScoreboardManager.setArenaScoreboard(playerTeam, teamInformation, teams);

                ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
                LeatherArmorMeta meta1 = (LeatherArmorMeta) Bukkit.getItemFactory().getItemMeta(boots.getType());
                meta1.setColor(teamInformation.getColor());
                boots.setItemMeta(meta1);
                playerTeam.getInventory().setBoots(boots);

                ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
                LeatherArmorMeta meta2 = (LeatherArmorMeta) Bukkit.getItemFactory().getItemMeta(leggings.getType());
                meta2.setColor(teamInformation.getColor());
                leggings.setItemMeta(meta2);
                playerTeam.getInventory().setLeggings(leggings);

                ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
                LeatherArmorMeta meta3 = (LeatherArmorMeta) Bukkit.getItemFactory().getItemMeta(chestplate.getType());
                meta3.setColor(teamInformation.getColor());
                chestplate.setItemMeta(meta3);
                playerTeam.getInventory().setChestplate(chestplate);

                ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
                LeatherArmorMeta meta4 = (LeatherArmorMeta) Bukkit.getItemFactory().getItemMeta(helmet.getType());
                meta4.setColor(teamInformation.getColor());
                helmet.setItemMeta(meta4);
                playerTeam.getInventory().setHelmet(helmet);

                playerTeam.getInventory().addItem(new ItemStack(Material.WOODEN_SWORD));

                playerTeam.teleport(teamInformation.getSpawn());
                listPlayers.remove(index);
            }
        }
        runnables.add(new TimerGame(this, plugin).runTaskTimer(plugin, 0, 20).getTaskId());
        runnables.add(new PlayerChecker(plugin, this).runTaskTimer(plugin, 0, 20).getTaskId());
        startGenerators();
        bossBar.setTitle(Utils.translateMessage("&fJugando en &6&nmc.hytacraft.com"));
        bossBar.setColor(BarColor.YELLOW);
        sendBroadcast(Utils.translateMessage("Comenzando el minijuego..."));
        state = State.LIVE;
    }

    public void stop() {
        resetMap(arena);
        for (Integer taskId : runnables) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        for (GlobalGenerator task : generators) {
            task.removeHolograms();
            task.cancel();
        }
        runnables.clear();
        generators.clear();
        for (TeamInfo team : teams) {
            team.getUpgrades().stopArena();
            team.setInventory(null);
            team.setBedValue(null);
            team.getTeamPlayers().clear();
        }
        List<UUID> players = getPlayers();
        players.addAll(spectators);
        for (UUID playerUUID : players) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) removePlayerArena(player);
        }
        spectators.clear();
        for (UUID npcUUID : npcs) {
            Bukkit.getEntity(npcUUID).remove();
        }
        npcs.clear();
        for (Entity entity : spawnArena.getWorld().getEntities()) {
            if (entity instanceof Item) {
                entity.remove();
            }
        }
        bossBar.removeAll();
        bossBar.setTitle(Utils.translateMessage("&cZona de espera..."));
        bossBar.setColor(BarColor.RED);
        state = State.WAITING;
    }

    public void addPlayer(Player player) {
        for (Player onlinePlayer : new ArrayList<>(Bukkit.getOnlinePlayers()).stream().filter(p -> !players.contains(player.getUniqueId())).collect(Collectors.toList())) {
            onlinePlayer.hidePlayer(plugin, player);
            player.hidePlayer(plugin, onlinePlayer);
        }
        for (UUID playerUUID : getPlayers()) {
            Player arenaPlayer = Bukkit.getPlayer(playerUUID);
            arenaPlayer.showPlayer(plugin, player);
            player.showPlayer(plugin, arenaPlayer);
        }
        players.add(player.getUniqueId());
        player.getInventory().clear();
        player.teleport(spawnArena);
        bossBar.addPlayer(player);
        sendBroadcast(Utils.translateMessage("&cEl jugador " + player.getName() + " acaba de unirse. (" + players.size() + "/" + teams.size() * playersPerTeam + ")"));
        if (players.size() == teams.size() * playersPerTeam) {
            new Starting(plugin, this);
        }
    }

    public void addSpectatorPlayer(Player player) {
        player.setPlayerListName(ChatColor.GRAY + player.getName());
        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        players.remove(player.getUniqueId());
        spectators.add(player.getUniqueId());
        for (UUID playerUUID : getPlayers()) {
            Player target = Bukkit.getPlayer(playerUUID);
            if (target != null) target.hidePlayer(player);
        }
        player.setAllowFlight(true);
        player.setFlying(true);
        TeamInfo team = getPlayerTeamInfo(player.getUniqueId());
        if (team.getTeamPlayers().isEmpty()) {
            breakBed(team, team.getTeamPlayers(), null);
            ScoreboardManager.updateAllArenaLivePlayers(getPlayers(), team, team.getTeamPlayers());
        }
        team.removePlayer(player);
        player.getEnderChest().clear();
        player.setHealth(20);
        player.setFoodLevel(20);
        ItemStack leave = Utils.getConfigItem(plugin.getConfig(), "items.spectator.leave");
        ItemStack next = Utils.getConfigItem(plugin.getConfig(), "items.spectator.match");
        inventory.setItem(2, next);
        inventory.setItem(6, leave);

        List<TeamInfo> remainingTeams = teams.stream().filter(t -> t.getTeamPlayers().size() != 0).collect(Collectors.toList());
        if (remainingTeams.size() == 1) {
            state = State.WIN;
            TeamInfo winnerTeam = remainingTeams.get(0);
            sendBroadcast("&cEl equipo " + winnerTeam.getDisplayName() + " &cacaba de ganar la partida.");
            if (!plugin.isDisabled) new Win(plugin, 5, this, new ArrayList<>(winnerTeam.getTeamPlayers()), winnerTeam.getColor());
        }
    }

    public void removePlayerArena(Player player) {
        if (!plugin.isDisabled) {
            for (Player playerOnline : Bukkit.getOnlinePlayers()) {
                for (Arena arena : plugin.getArenas()) {
                    if (arena.getPlayers().contains(playerOnline.getUniqueId()) && arena.getSpectators().contains(playerOnline.getUniqueId())) {
                        playerOnline.hidePlayer(plugin, player);
                        player.hidePlayer(plugin, playerOnline);
                    } else {
                        player.showPlayer(plugin, playerOnline);
                        playerOnline.showPlayer(plugin, player);
                    }
                }
            }
        }
        spectators.remove(player.getUniqueId());
        player.setFlying(false);
        player.setAllowFlight(false);
        bossBar.removePlayer(player);
        ScoreboardManager.setLobbyScoreboard(player);
        ScoreboardManager.setLobbyPlayerName(TabAPI.getInstance().getPlayer(player.getUniqueId()));
        player.teleport(plugin.getSpawnLocation());
    }

    public void killPlayer(Player player) {
        TeamInfo team = getPlayerTeamInfo(player.getUniqueId());
        if (team.isBed()) {
            player.setFoodLevel(20);
            player.setHealth(20);
            runnables.add(new RespawnTimer(plugin, this, player, getPlayerTeamInfo(player.getUniqueId()).getSpawn()).runTaskTimer(plugin, 0, 20).getTaskId());
        } else {
            player.sendMessage(Utils.translateMessage("&cAcabas de ser eliminado."));
            addSpectatorPlayer(player);
        }
        player.teleport(spectatorArena);
    }

    public void breakBed(TeamInfo team, List<UUID> teamPlayers, Player trigger) {
        if (trigger != null) trigger.playSound(trigger, Sound.ENTITY_CAT_HURT, 1, 1);
        team.setBedValue(false);
        team.getBedLocation().getWorld().strikeLightningEffect(team.getBedLocation());
        ScoreboardManager.updateAllArenaLivePlayers(getPlayers(), team, teamPlayers);
        for (UUID playerUUID : new ArrayList<>(team.getTeamPlayers())) {
            Player player = Bukkit.getPlayer(playerUUID);
            player.sendTitle(Utils.translateMessage("&c&lCAMA DESTRUIDA"), Utils.translateMessage("&cNo volveras a aparecer."));
            player.playSound(player, Sound.ENTITY_WITHER_DEATH, 1, 1);
        }
    }

    public void startGenerators() {
        for (TeamInfo teamInformation : teams) {
            GeneratorTimer generatorTimer = new GeneratorTimer(teamInformation.getGeneratorLocation().clone(), new ItemStack(Material.IRON_INGOT, 1), new ItemStack(Material.GOLD_INGOT, 1), new ItemStack(Material.EMERALD, 1));
            teamInformation.setGenerator(generatorTimer);
            runnables.add((generatorTimer).runTaskTimer(plugin, 20, 20).getTaskId());
        }
        for (Location location : generatorLocations.keySet()) {
            boolean isDiamond = generatorLocations.get(location) == 0;
            List<String> hologramText = isDiamond ? diamondText : emeraldText;
            ItemStack item = isDiamond ? new ItemStack(Material.DIAMOND) : new ItemStack(Material.EMERALD);
            Material material = isDiamond ? Material.DIAMOND_BLOCK : Material.EMERALD_BLOCK;
            GlobalGenerator generator = new GlobalGenerator(plugin, location.clone(), material, item.clone(), hologramText);
            generator.runTaskTimer(plugin, 20, 20);
            generators.add(generator);
        }
    }

    public void resetMap(Region map) {
        List<Material> wools = TeamInfo.allWoolMaterials;
        List<Material> beds = TeamInfo.allBedMaterials;
        List<Material> shopBlocks = plugin.getMenus().getShopMaterials();
        for (Block block : map) {
            if (wools.contains(block.getType()) || shopBlocks.contains(block.getType()) || beds.contains(block.getType())) block.setType(Material.AIR);
        }
    }

    public void sendBroadcast(String message) {
        for (UUID playerUUID : getPlayers()) {
            Bukkit.getPlayer(playerUUID).sendMessage(Utils.translateMessage(message));
        }
    }

    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (UUID playerUUID : getPlayers()) {
            Bukkit.getPlayer(playerUUID).sendTitle(Utils.translateMessage(title), Utils.translateMessage(subtitle), fadeIn, stay, fadeOut);
        }
    }

    public List<TeamInfo> getTeams() {
        return teams;
    }

    public List<UUID> getPlayers() {
        return new ArrayList<>(players);
    }

    public List<UUID> getSpectators() {
        return spectators;
    }

    public TeamInfo getPlayerTeamInfo(UUID playerUUID) {
        for (TeamInfo teamInfo : teams) {
            if (teamInfo.getTeamPlayers().contains(playerUUID)) return teamInfo;
        }
        return null;
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public List<Integer> getRunnables() {
        return runnables;
    }

    public Location getSpectatorArena() {
        return spectatorArena;
    }

    public List<GlobalGenerator> getGenerators() { return generators; }

    public Region getArenaRegion() {
        return arena;
    }

    public String getName() { return name; }

    public String getId() {
        return id;
    }

    public int getPlayersPerTeam() {
        return playersPerTeam;
    }
}