package com.elach.bedwars.Arena.ArenaUtils;

import com.elach.bedwars.Arena.Game.GeneratorTimer;
import com.elach.bedwars.Arena.System.Arena;
import com.elach.bedwars.BedWars;
import com.elach.bedwars.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Upgrades {

    private final HashMap<String, Boolean> traps;
    private boolean cooldownTraps;
    private Boolean regenerateIsland;
    private Boolean sharpness;
    private Integer generatorIndex;
    private Integer fastDiggingIndex;
    private PotionEffect fastDiggingPotionEffect;
    private Integer armorIndex;

    private final TeamInfo team;
    private final BedWars plugin;

    public Upgrades(BedWars plugin, TeamInfo team) {
        this.plugin = plugin;
        this.team = team;
        traps = new HashMap<>();
    }

    public void startArena() {
        traps.put("blindness", false);
        traps.put("invisible", false);
        traps.put("golem", false);
        regenerateIsland = false;
        sharpness = false;
        generatorIndex = 0;
        fastDiggingIndex = 0;
        fastDiggingPotionEffect = null;
        armorIndex = 0;
    }

    public void stopArena() {
        traps.clear();
        regenerateIsland = null;
        sharpness = null;
        generatorIndex = null;
        fastDiggingIndex = null;
        fastDiggingPotionEffect = null;
        armorIndex = null;
    }

    public void changeArmorEnchant() {
        armorIndex += 1;
        for (UUID playerUUID : team.getTeamPlayers()) {
            Player playerTeam = Bukkit.getPlayer(playerUUID);
            PlayerInventory inventory = playerTeam.getInventory();
            inventory.getItem(EquipmentSlot.FEET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, armorIndex);
            inventory.getItem(EquipmentSlot.LEGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, armorIndex);
            inventory.getItem(EquipmentSlot.CHEST).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, armorIndex);
            inventory.getItem(EquipmentSlot.HEAD).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, armorIndex);
            playerTeam.sendMessage(Utils.translateMessage("&cSe acaba de mejorar la armadura a la etapa " + armorIndex + "."));
        }
    }

    public void changeRegeneration() {
        for (UUID playerUUID : team.getTeamPlayers()) {
            Player playerTeam = Bukkit.getPlayer(playerUUID);
            playerTeam.sendMessage(Utils.translateMessage("&cSe acaba adquirir la mejora de regeneración de isla."));
            regenerateIsland = true;
        }
    }

    public void changeSharpness() {
        for (UUID playerUUID : team.getTeamPlayers()) {
            Player playerTeam = Bukkit.getPlayer(playerUUID);
            List<Material> swords = plugin.getMenus().getShopSwords();
            for (ItemStack itemInventory : playerTeam.getInventory().getContents()) {
                if (itemInventory != null && swords.contains(itemInventory.getType())) {
                    itemInventory.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                }
            }
            playerTeam.sendMessage(Utils.translateMessage("&cSe mejoró la espada con encantamiento de filo I."));
        }
        sharpness = true;
    }

    public void changeFastDigging() {
        if (fastDiggingIndex == 0) fastDiggingIndex += 1;
        else if (fastDiggingIndex == 1) fastDiggingIndex = 2;
        fastDiggingPotionEffect = new PotionEffect(PotionEffectType.FAST_DIGGING, 22, fastDiggingIndex);
        for (UUID playerTeam : team.getTeamPlayers()) Bukkit.getPlayer(playerTeam).sendMessage(Utils.translateMessage("&cSe mejoró la prisa minera al nivel " + (fastDiggingIndex == 1 ? "I" : "II")));
    }

    public void changeGeneratorLevel() {
        GeneratorTimer generator = team.getGenerator();
        switch (generatorIndex) {
            case 1:
                for (UUID playerTeam : team.getTeamPlayers()) {
                    Bukkit.getPlayer(playerTeam).sendMessage(Utils.translateMessage("&cSe mejoro al nivel de generador 50%+."));
                }
                ItemStack iron = new ItemStack(Material.IRON_INGOT, generator.getIron().getAmount() + 1);
                ItemStack gold = new ItemStack(Material.GOLD_INGOT, generator.getGold().getAmount() + 1);
                generator.setIron(iron);
                generator.setGold(gold);
            case 2:
                for (UUID playerTeam : team.getTeamPlayers()) {
                    Bukkit.getPlayer(playerTeam).sendMessage(Utils.translateMessage("&cSe mejoro al nivel de generador 100%+."));
                }
                ItemStack iron1 = new ItemStack(Material.IRON_INGOT, generator.getIron().getAmount() + 1);
                ItemStack gold1 = new ItemStack(Material.GOLD_INGOT, generator.getGold().getAmount() + 1);
                generator.setIron(iron1);
                generator.setGold(gold1);
            case 3:
                for (UUID playerTeam : team.getTeamPlayers()) {
                    Bukkit.getPlayer(playerTeam).sendMessage(Utils.translateMessage("&cSe añadió esmeralda al generador."));
                }
                generator.setCounterEmerald(10);
                break;
            case 4:
                for (UUID playerTeam : team.getTeamPlayers()) {
                    Bukkit.getPlayer(playerTeam).sendMessage(Utils.translateMessage("&cSe mejoro al nivel de generador 200%+."));
                }
                ItemStack iron3 = new ItemStack(Material.IRON_INGOT, generator.getIron().getAmount() + 1);
                ItemStack gold3 = new ItemStack(Material.GOLD_INGOT, generator.getGold().getAmount() + 1);
                ItemStack emerald = new ItemStack(Material.EMERALD, generator.getEmerald().getAmount() + 1);
                generator.setIron(iron3);
                generator.setGold(gold3);
                generator.setEmerald(emerald);
                break;
        }
    }

    public boolean triggerTrap(Arena arena, Player trigger, TeamInfo teamAttacked) {
        if (cooldownTraps) return false;
        if (traps.get("blindness")) {
            trigger.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 400, 1));
            trigger.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 400, 1));
            trigger.sendTitle(Utils.translateMessage("&c&lTRAMPA ACTIVADA"), Utils.translateMessage("&cEl equipo fue alertado."));
            for (UUID playerUUID : teamAttacked.getTeamPlayers()) {
                Bukkit.getPlayer(playerUUID).sendTitle(Utils.translateMessage("&4&lALERTA ENEMIGA"), Utils.translateMessage("&cEl equipo " + Utils.getPlayerArena(trigger, plugin.getArenas()).getPlayerTeamInfo(trigger.getUniqueId()).getDisplayName() + " &cactivó trampa de ceguera."));
            }
            traps.put("blindness", false);
            cooldownTraps = true;
        } else if (traps.get("invisible") && trigger.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            trigger.removePotionEffect(PotionEffectType.INVISIBILITY);
            trigger.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 400, 1));
            trigger.sendTitle(Utils.translateMessage("&c&lTRAMPA ACTIVADA"), Utils.translateMessage("&cInvisibilidad removida. El equipo fue alertado."));
            for (UUID playerUUID : teamAttacked.getTeamPlayers()) {
                Bukkit.getPlayer(playerUUID).sendTitle(Utils.translateMessage("&4&lALERTA ENEMIGA"), Utils.translateMessage("&cEl equipo " + Utils.getPlayerArena(trigger, plugin.getArenas()).getPlayerTeamInfo(trigger.getUniqueId()).getDisplayName() + " &cactivó trampa de invisibilidad."));
            }
            traps.put("invisible", false);
            cooldownTraps = true;
        } else if (traps.get("golem")) {
            IronGolem golem = teamAttacked.getSpawn().getWorld().spawn(teamAttacked.getSpawn(), IronGolem.class);
            List<TeamInfo> teams = new ArrayList<>(arena.getTeams());
            teams.removeIf(t -> t.getId().equals(teamAttacked.getId()));
            golem.setTarget(trigger);
            golem.setAI(true);
            golem.setCustomName(Utils.translateMessage("&fGolem Equipo " + teamAttacked.getDisplayName()));
            trigger.sendTitle(Utils.translateMessage("&c&lTRAMPA ACTIVADA"), Utils.translateMessage("&cUn golem apareció, equipo enemigo avisado."));
            for (UUID playerUUID : teamAttacked.getTeamPlayers()) {
                Bukkit.getPlayer(playerUUID).sendTitle(Utils.translateMessage("&4&lALERTA ENEMIGA"), Utils.translateMessage("&cEl equipo " + Utils.getPlayerArena(trigger, plugin.getArenas()).getPlayerTeamInfo(trigger.getUniqueId()).getDisplayName() + " &cactivó trampa de golem."));
            }
            traps.put("golem", false);
            cooldownTraps = true;
        }
        return cooldownTraps;
    }

    public Boolean getRegenerateIsland() {
        return regenerateIsland;
    }

    public Boolean getSharpness() {
        return sharpness;
    }

    public HashMap<String, Boolean> getTraps() {
        return traps;
    }

    public Integer getArmorIndex() {
        return armorIndex;
    }

    public Integer getFastDiggingIndex() {
        return fastDiggingIndex;
    }

    public Integer getGeneratorIndex() {
        return generatorIndex;
    }

    public void setCooldownTraps(boolean cooldownTraps) {
        this.cooldownTraps = cooldownTraps;
    }

    public PotionEffect getFastDiggingPotionEffect() {
        return fastDiggingPotionEffect;
    }
}