package com.github.intangir.Tweaks;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class Farming extends Tweak
{
	Farming(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "farming.yml");
		TWEAK_NAME = "Tweak_Farming";
		TWEAK_VERSION = "1.0";
		
		weakCropChance = 20;
		lightRequired = 10;
		aridTreeChance = 10; 

		lowLightPenalty = 25;
		aridPenalty = 25;
		unwateredPenalty = 25;
		
		dontMoveWater = true;

		weakCrops = new HashSet<String>();
		weakCrops.add("CROPS");
		weakCrops.add("CARROT");
		weakCrops.add("POTATO");
		
		aridBiomes = new HashSet<String>();
		aridBiomes.add("DESERT");
		aridBiomes.add("DESERT_HILLS");
		aridBiomes.add("DESERT_MOUNTAINS");
		aridBiomes.add("MESA");
		aridBiomes.add("MESA_BRYCE");
		aridBiomes.add("MESA_PLATEAU");
		aridBiomes.add("MESA_PLATEAU_MOUNTAINS");
		aridBiomes.add("HELL");
	}
	
	private boolean dontMoveWater;
	private Integer weakCropChance;
	private Integer lightRequired;
	private Integer aridTreeChance;
	private Integer lowLightPenalty;
	private Integer aridPenalty;
	private Integer unwateredPenalty;
	
	private Set<String> aridBiomes;
	private Set<String> weakCrops;
	
	transient private Set<Biome> aridTypes;
	transient private Set<Material> weakTypes;
	
	@Override
	public void enable() {
		super.enable();

		aridTypes = new HashSet<Biome>();
		for(String name : aridBiomes) {
			aridTypes.add(Biome.valueOf(name));
		}

		weakTypes = new HashSet<Material>();
		for(String name : weakCrops) {
			weakTypes.add(Material.valueOf(name));
		}
	}

    // weak crops
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockGrow(BlockGrowEvent e) {
        if (PlantDies(e.getBlock(), e.getNewState().getData()))
        {
            e.setCancelled(true);
            e.getBlock().setType(Material.DEAD_BUSH); // dead shrub
        }
    }

    // cancel tree and mushroom growth rate
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onStructureGrow(StructureGrowEvent e) {
        Block block = e.getLocation().getBlock();

        if(aridTypes.contains(block.getBiome()) && rand.nextInt(100) > aridTreeChance) {
        	e.setCancelled(true);
        }
    }
    
    // disable moving water source
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerEmptyBucket(PlayerBucketEmptyEvent e) {
        if (dontMoveWater) {
            final Block block = e.getBlockClicked().getRelative(e.getBlockFace());

        	server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@SuppressWarnings("deprecation")
				public void run() {
					if (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER) {
						block.setData((byte) 1);
			        }
				}
			}, 10);
        }
    }

    // evaluates if a plant will die
    @SuppressWarnings("deprecation")
	public boolean PlantDies(Block block, MaterialData newDataValue) {
    	if(weakCropChance > 0 && weakTypes.contains(block.getType()) && newDataValue.getData() == CropState.RIPE.ordinal()) {
            int lossChance = weakCropChance;

            // not enough light
            if(block.getLightFromSky() < lightRequired) {
            	lossChance += lowLightPenalty;
            }

            // is in an arid biome
        	if(aridTypes.contains(block.getBiome())) {
            	lossChance += aridPenalty;
            }

        	// unwatered crops are more likely to die
            Block belowBlock = block.getRelative(BlockFace.DOWN);
            if(belowBlock.getType() == Material.SOIL && belowBlock.getData() == 0)
            {
            	lossChance += unwateredPenalty;
            }
            
            if(rand.nextInt(100) < lossChance) {
            	return true;
            }
        }
        return false;
    }
}

