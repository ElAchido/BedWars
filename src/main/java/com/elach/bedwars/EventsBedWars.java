package com.elach.bedwars;

import com.elach.bedwars.Arena.ArenaUtils.State;
import com.elach.bedwars.Arena.ArenaUtils.TeamInfo;
import com.elach.bedwars.Arena.System.Arena;
import com.elach.bedwars.Utils.ScoreboardManager;
import com.elach.bedwars.Utils.Utils;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;
import me.neznamy.tab.api.team.UnlimitedNametagManager;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftVillager;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.*;
import java.util.stream.Collectors;

public class EventsBedWars implements Listener {

    private final BedWars plugin;

    public EventsBedWars(BedWars plugin) {
        this.plugin = plugin;
        TabAPI api = TabAPI.getInstance();

        api.getEventBus().register(PlayerLoadEvent.class, event -> {
            TabPlayer tabPlayer = event.getPlayer();
            if (api.getTeamManager() instanceof UnlimitedNametagManager) {
                ScoreboardManager.setLobbyPlayerName(tabPlayer);
            }
        });
    }

    @EventHandler
    public void onSpawn(PlayerSpawnLocationEvent e) {
        e.setSpawnLocation(plugin.getSpawnLocation());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.setJoinMessage(null);
        Player player = e.getPlayer();
        player.getInventory().clear();
        player.getEnderChest().clear();
        player.setHealth(20);
        player.setFoodLevel(20);

        player.sendMessage(Utils.translateMessage("&aBienvenido al BedWars " + e.getPlayer().getName() + ", esperemos que lo disfrutes."));
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(1024.0D);
        player.saveData();

        for (Arena arena : plugin.getArenas()) {
            for (UUID playerUUID : arena.getPlayers()) {
                Player arenaPlayer = Bukkit.getPlayer(playerUUID);
                arenaPlayer.hidePlayer(plugin, player);
                player.hidePlayer(plugin, arenaPlayer);
            }
        }

        ScoreboardManager.setLobbyScoreboard(player);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Arena arena = Utils.getPlayerArena(e.getPlayer(), plugin.getArenas());
        if (arena != null) {
            List<Entity> villagers = e.getPlayer().getNearbyEntities(15, 15, 15).stream().filter(en -> en.getType().equals(EntityType.VILLAGER)).collect(Collectors.toList());
            ServerPlayer player = ((CraftPlayer) e.getPlayer()).getHandle();
            ServerGamePacketListenerImpl sp = player.connection;
            for (Entity entity : villagers) {
                Location location = entity.getLocation().clone().setDirection(e.getPlayer().getLocation().subtract(entity.getLocation()).toVector());
                sp.send(new ClientboundRotateHeadPacket(((CraftVillager) entity).getHandle(), (byte) ((location.getYaw() % 360) * 256 / 360)));
                sp.send(new ClientboundMoveEntityPacket.Rot(entity.getEntityId(), (byte) ((location.getYaw() % 360) * 256 / 360), (byte) ((location.getPitch() %360.)*256/360), false));
            }
        }
    }

    @EventHandler
    public void onPickUp(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            if (player.getAllowFlight()) {
                e.setCancelled(true);
                return;
            }
            Item item = e.getItem();
            if (item.getCustomName() != null && item.getCustomName().equals("generatorDrop")) {
                item.setCustomName(null);
                List<Entity> players = player.getNearbyEntities(0.75, 0.75, 0.75).stream().filter(en -> en.getType().equals(EntityType.PLAYER)).collect(Collectors.toList());
                for (Entity entity : players) {
                    Player target = (Player) entity;
                    target.getInventory().addItem(item.getItemStack());
                    target.playSound(target, Sound.ENTITY_ITEM_PICKUP, 1, 1);
                }
            }
        }
    }

    @EventHandler
    public void rightClickEvent(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Arena arena = Utils.getPlayerArena(e.getPlayer(), plugin.getArenas());
            if (arena != null) {
                if (e.getClickedBlock().getType().equals(Material.CHEST)) {
                    e.getPlayer().openInventory(arena.getPlayerTeamInfo(e.getPlayer().getUniqueId()).getInventory());
                    e.setCancelled(true);
                } else if (e.getItem() != null) {
                    if (e.getItem().getType().equals(Material.COMPASS)) {
                        // NUEVO MATCH
                    } else if (e.getItem().getType().equals(Material.RED_BED)) {
                        arena.removePlayerArena(e.getPlayer());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent e) {
        if (e.getItem().getType().equals(Material.POTION)) {
            Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> e.getPlayer().setItemInHand(new ItemStack(Material.AIR)), 1L);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        Arena arena = Utils.getPlayerArena(e.getPlayer(), plugin.getArenas());
        if (arena != null) {
            if (e.getPlayer().getAllowFlight()) {
                e.setCancelled(true);
                return;
            }
            if (arena.getSpectators().contains(e.getPlayer().getUniqueId())) {
                e.setCancelled(true);
                return;
            }
            List<Material> pickAxes = plugin.getMenus().getShop().getPickAxes();
            List<Material> axes = plugin.getMenus().getShop().getAxes();
            Material typeItemInUse = e.getItemDrop().getItemStack().getType();
            if (e.getPlayer().getFallDistance() != 0) {
                e.setCancelled(true);
            } else if (pickAxes.contains(typeItemInUse) || axes.contains(typeItemInUse)) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(Utils.translateMessage("&cNo puedes tirar este item."));
            }
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Arena arena = Utils.getPlayerArena(e.getPlayer(), plugin.getArenas());
        if (arena != null && arena.getState().equals(State.LIVE)) {
            if (e.getPlayer().getAllowFlight()) {
                e.setCancelled(true);
                return;
            }
            if (e.getBlock().hasMetadata("team")) {
                String teamId = e.getBlock().getMetadata("team").get(0).asString();
                TeamInfo team = arena.getTeams().stream().filter(t -> t.getId().equals(teamId)).collect(Collectors.toList()).get(0);
                if (team.isBed()) {
                    if (team.getId().equals(arena.getPlayerTeamInfo(e.getPlayer().getUniqueId()).getId())) {
                        e.setCancelled(true);
                        e.getPlayer().sendMessage(Utils.translateMessage("&cNo puedes romper tu propia cama."));
                    } else {
                        e.setDropItems(false);
                        arena.breakBed(team, team.getTeamPlayers(), e.getPlayer());
                        arena.sendBroadcast(Utils.translateMessage("&cEl jugador " + e.getPlayer().getName() + " acaba de romper la cama del equipo " + team.getDisplayName()));
                    }
                }
            } else if (!TeamInfo.allWoolMaterials.contains(e.getBlock().getType()) && !plugin.getMenus().containsBlock(e.getBlock().getType())) {
                e.getPlayer().sendMessage(Utils.translateMessage("&cNo puedes romper la parte original del mapa."));
                e.setCancelled(true);
            }
        } else {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Arena arena = Utils.getPlayerArena(e.getPlayer(), plugin.getArenas());
        if (arena != null) {
            if (e.getPlayer().getAllowFlight()) {
                e.setCancelled(true);
                return;
            }
            List<UUID> players = arena.getPlayers();
            if (players.contains(e.getPlayer().getUniqueId())) {
                if (plugin.getMenus().containsBlock(e.getBlock().getType()) || TeamInfo.allWoolMaterials.contains(e.getBlock().getType())) {
                    for (TeamInfo teamInfo : arena.getTeams()) {
                        if (teamInfo.getNoBuildRegion().contains(e.getBlock().getLocation())) {
                            e.getPlayer().sendMessage(Utils.translateMessage("&cNo puedes construir en el spawn de una isla."));
                            e.setCancelled(true);
                            return;
                        }
                    }
                    if (!arena.getArenaRegion().contains(e.getBlock().getLocation())) {
                        e.getPlayer().sendMessage(Utils.translateMessage("&cNo puedes construir fuera del mapa."));
                        e.setCancelled(true);
                    } else if (e.getBlock().getType().equals(Material.TNT)) {
                        Location location = e.getBlock().getLocation();
                        e.getBlock().setType(Material.AIR);
                        TNTPrimed tnt = (TNTPrimed) location.getWorld().spawnEntity(location.add(0, 0.5, 0), EntityType.PRIMED_TNT, false);
                        tnt.teleport(e.getBlock().getLocation().add(0, 0.5, 0));
                        tnt.setCustomName(arena.getId());
                        tnt.setFuseTicks(80);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        if (e.getEntity().getType() == EntityType.PRIMED_TNT) {
            e.getLocation().getWorld().createExplosion(e.getLocation(), 5, false, false);
            List<Block> destroyedBlocks = e.blockList();
            List<Block> glasses = destroyedBlocks.stream().filter(m -> m.getType().equals(Material.GLASS)).collect(Collectors.toList());
            List<Material> indestructibleMaterials = TeamInfo.allBedMaterials;
            List<Material> destructibleMaterials = new ArrayList<>();
            destructibleMaterials.addAll(TeamInfo.allWoolMaterials);
            destructibleMaterials.addAll(plugin.getMenus().getShopMaterials());
            destroyedBlocks.removeIf(m -> indestructibleMaterials.contains(m.getType()) || !destructibleMaterials.contains(m.getType()));
            for (Block block : glasses) {
                destroyedBlocks.remove(block);
                Location location = block.getLocation().clone();
                checkExplosion(destructibleMaterials, destroyedBlocks, location, 0, -1, 0);
                checkExplosion(destructibleMaterials, destroyedBlocks, location, -1, 0, 0);
                checkExplosion(destructibleMaterials, destroyedBlocks, location, 1, 0, 0);
                checkExplosion(destructibleMaterials, destroyedBlocks, location, 0, 0, -1);
                checkExplosion(destructibleMaterials, destroyedBlocks, location, 0, 0, 1);
            }
        }
    }

    public void checkExplosion(List<Material> destructibleMaterials, List<Block> destroyedBlocks, Location location, int x, int y, int z) {
        while (true) {
            Block blockCheck = location.getWorld().getBlockAt(location.add(x, y, z));
            if (destructibleMaterials.contains(blockCheck.getType())) {
                if (destroyedBlocks.contains(blockCheck)) {
                    destroyedBlocks.remove(blockCheck);
                    continue;
                }
            }
            break;
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        Arena arena = Utils.getPlayerArena(e.getPlayer(), plugin.getArenas());
        if (arena != null) {
            arena.removePlayerArena(e.getPlayer());
            if (!arena.getState().equals(State.LIVE)) {
                ScoreboardManager.updateAllArenaWaitingPlayers(arena.getPlayers(), arena.getPlayers().size(), arena.getTeams().size() * arena.getPlayersPerTeam());
            }
        }
    }

    @EventHandler (priority = EventPriority.NORMAL)
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) return;
            Player player = (Player) e.getEntity();
            if (player.getAllowFlight()) {
                e.setCancelled(true);
                return;
            }
            Arena arena = Utils.getPlayerArena(player, plugin.getArenas());
            if (arena != null) {
                List<UUID> players = arena.getPlayers();
                if (players.contains(player.getUniqueId())) {
                    EntityDamageEvent.DamageCause cause = e.getCause();
                    if (arena.getState().equals(State.LIVE)) {
                        if (cause.equals(EntityDamageEvent.DamageCause.VOID)) {
                            e.setDamage(0);
                            arena.killPlayer((Player) e.getEntity());
                        } else if (cause.equals(EntityDamageEvent.DamageCause.FALL) || cause.equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) {
                            if (cause.equals(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)) e.setDamage(e.getDamage() * 0.1);
                            if (cause.equals(EntityDamageEvent.DamageCause.FALL)) e.setDamage(e.getDamage() * 0.5);
                            if (player.getHealth() - e.getDamage() <= 0) {
                                e.setCancelled(true);
                                List<ItemStack> items = new ArrayList<>(Arrays.asList(player.getInventory().getContents()));
                                items.removeIf(item -> item == null ||!Arrays.asList(Material.IRON_INGOT, Material.GOLD_INGOT, Material.EMERALD, Material.DIAMOND).contains(item.getType()));
                                for (ItemStack item : items) player.getLocation().getWorld().dropItem(player.getLocation(), item);
                                arena.killPlayer((Player) e.getEntity());
                                for (PotionEffect potionEffect : ((Player) e.getEntity()).getActivePotionEffects()) {
                                    ((Player) e.getEntity()).removePotionEffect(potionEffect.getType());
                                }
                            }
                        }
                    }
                }
            } else {
                e.setCancelled(true);
            }
        } else if (e.getEntity() instanceof Villager) {
            e.setCancelled(false);
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onPlayerAttack(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player) e.getEntity();
            Arena arena = Utils.getPlayerArena(player, plugin.getArenas());
            if (arena != null && arena.getState().equals(State.LIVE)) {
                if (e.getDamager() instanceof Player) {
                    Player damager = (Player) e.getDamager();
                    if (damager.getAllowFlight()) {
                        e.setCancelled(true);
                    } else if (arena.getPlayerTeamInfo(player.getUniqueId()).getId().equals(arena.getPlayerTeamInfo(damager.getUniqueId()).getId())) {
                        e.setCancelled(true);
                    } else if (damager.getPotionEffect(PotionEffectType.INVISIBILITY) != null) {
                        ((Player) e.getDamager()).removePotionEffect(PotionEffectType.INVISIBILITY);
                    }
                }
                if (player.getHealth() - e.getDamage() <= 0) {
                    e.setCancelled(true);
                    List<ItemStack> items = new ArrayList<>(Arrays.asList(player.getInventory().getContents()));
                    items.removeIf(item -> item == null ||!Arrays.asList(Material.IRON_INGOT, Material.GOLD_INGOT, Material.EMERALD, Material.DIAMOND).contains(item.getType()));
                    if (e.getDamager() instanceof Player) for (ItemStack item : items) ((Player) e.getDamager()).getInventory().addItem(item);
                    for (PotionEffect potionEffect : ((Player) e.getEntity()).getActivePotionEffects()) {
                        ((Player) e.getEntity()).removePotionEffect(potionEffect.getType());
                    }
                    arena.killPlayer((Player) e.getEntity());
                }
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFeed(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemDamage(PlayerItemDamageEvent e) {
        e.setCancelled(true);
    }
}
