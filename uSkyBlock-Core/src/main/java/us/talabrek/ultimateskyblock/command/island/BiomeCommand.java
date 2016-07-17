package us.talabrek.ultimateskyblock.command.island;

import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import us.talabrek.ultimateskyblock.Settings;
import us.talabrek.ultimateskyblock.async.Callback;
import us.talabrek.ultimateskyblock.island.IslandInfo;
import us.talabrek.ultimateskyblock.menu.SkyBlockMenu;
import us.talabrek.ultimateskyblock.player.PlayerInfo;
import us.talabrek.ultimateskyblock.uSkyBlock;

import java.util.HashMap;
import java.util.Map;

import static dk.lockfuglsang.minecraft.po.I18nUtil.marktr;
import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class BiomeCommand extends RequireIslandCommand {
    public static final Map<String, Biome> BIOMES = new HashMap<String, Biome>() {
        {
            put("ocean", Biome.OCEAN);
            put("jungle", Biome.JUNGLE);
            put("hell", Biome.HELL);
            put("sky", Biome.SKY);
            put("mushroom", Biome.MUSHROOM_ISLAND);
            put("swampland", Biome.SWAMPLAND);
            put("taiga", Biome.TAIGA);
            put("desert", Biome.DESERT);
            put("forest", Biome.FOREST);
            put("plains", Biome.PLAINS);
            put("extreme_hills", Biome.EXTREME_HILLS);
            put("deep_ocean", Biome.DEEP_OCEAN);
        }
    };
    private final SkyBlockMenu menu;

    public BiomeCommand(uSkyBlock plugin, SkyBlockMenu menu) {
        super(plugin, "biome|b", null, "biome", tr("change the biome of the island"));
        this.menu = menu;
        addFeaturePermission("usb.exempt.cooldown.biome", tr("exempt player from biome-cooldown"));
        for (String biome : BIOMES.keySet()) {
            addFeaturePermission("usb.biome." + biome, tr("Let the player change their islands biome to {0}", biome.toUpperCase()));
        }
    }

    @Override
    protected boolean doExecute(String alias, final Player player, PlayerInfo pi, final IslandInfo island, Map<String, Object> data, String... args) {
        if (args.length == 0) {
            if (!island.hasPerm(player, "canChangeBiome")) {
                player.sendMessage(tr("\u00a7cYou do not have permission to change the biome of your current island."));
            } else {
                player.openInventory(menu.displayBiomeGUI(player)); // Weird, that we show the UI
            }
        }
        if (args.length == 1) {
            final String biome = args[0];
            if (!island.hasPerm(player, "canChangeBiome")) {
                player.sendMessage(tr("\u00a74You do not have permission to change the biome of this island!"));
                return true;
            }
            if (!plugin.playerIsOnOwnIsland(player)) {
                player.sendMessage(tr("\u00a7eYou must be on your island to change the biome!"));
                return true;
            }
            if (!biomeExists(biome)) {
                player.sendMessage(tr("\u00a7cYou have misspelled the biome name. Must be one of {0}", BIOMES.keySet()));
                return true;
            }
            int cooldown = plugin.getCooldownHandler().getCooldown(player, "biome");
            if (cooldown > 0) {
                player.sendMessage(tr("\u00a7eYou can change your biome again in {0,number,#} minutes.", cooldown / 60));
                return true;
            }
            plugin.changePlayerBiome(player, biome, new Callback<Boolean>() {
                @Override
                public void run() {
                    if (getState()) {
                        player.sendMessage(tr("\u00a7aYou have changed your island''s biome to {0}", biome.toUpperCase()));
                        player.sendMessage(tr("\u00a7aYou may need to go to spawn, or relog, to see the changes."));
                        island.sendMessageToIslandGroup(true, marktr("{0} changed the island biome to {1}"), player.getName(), biome.toUpperCase());
                        plugin.getCooldownHandler().resetCooldown(player, "biome", Settings.general_biomeChange);
                    } else {
                        player.sendMessage(tr("\u00a7cYou do not have permission to change your biome to that type."));
                    }
                }
            });
        }
        return true;
    }

    public static boolean biomeExists(String biomeName) {
        if (biomeName == null) {
            return false;
        }
        return BIOMES.containsKey(biomeName.toLowerCase());
    }
}
