package com.github.intangir.Tweaks;

import java.io.File;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class Ice extends Tweak
{
	Ice(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "ice.yml");
		TWEAK_NAME = "Tweak_Ice";
		TWEAK_VERSION = "1.0";

		permaFrost = false;
		iceCoolsLavaRange = 1;
		packedIceCoolsLavaRange = 1;
		obsidianGeneration = true;
	}

	private boolean permaFrost;
	private int iceCoolsLavaRange;
	private int packedIceCoolsLavaRange;
	private boolean obsidianGeneration;
	
	@EventHandler(ignoreCancelled = true)
	public void onMelt(BlockFadeEvent e) {
		if(permaFrost && e.getBlock().getType() == Material.ICE) {
		    e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlaceIce(BlockPlaceEvent e) {
		int coolRange = 0;
		if(e.getBlock().getType() == Material.ICE) {
			coolRange = iceCoolsLavaRange;
		}
		if(e.getBlock().getType() == Material.PACKED_ICE) {
			coolRange = packedIceCoolsLavaRange;
		}
		
		boolean cooled = false;
		for(int x = e.getBlock().getX() - coolRange; x <= e.getBlock().getX() + coolRange; x++) {
			for(int y = e.getBlock().getY() - coolRange; y <= e.getBlock().getY() + coolRange; y++) {
				for(int z = e.getBlock().getZ() - coolRange; z <= e.getBlock().getZ() + coolRange; z++) {
					Block block = e.getBlock().getWorld().getBlockAt(x, y, z);
					if(block.getType() == Material.STATIONARY_LAVA || block.getType() == Material.LAVA) {
						if(block.getData() == 0) {
							block.setType(Material.OBSIDIAN);
						} else {
							block.setType(Material.COBBLESTONE);
						}
						cooled = true;
					}
				}
			}
		}
		
		if(cooled && e.getBlock().getType() == Material.ICE) {
			e.getPlayer().sendMessage("The ice was melted while cooling nearby lava.");
			e.getBlock().setType(Material.STATIONARY_WATER);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBlockEvent(BlockFromToEvent e) {
		if(obsidianGeneration && (e.getBlock().getType() == Material.LAVA || e.getBlock().getType() == Material.STATIONARY_LAVA)) {
			if(e.getToBlock().getType() == Material.REDSTONE_WIRE) {
				for(BlockFace f : BlockFace.values()) {
					if(e.getToBlock().getRelative(f).getType() == Material.WATER || e.getToBlock().getRelative(f).getType() == Material.STATIONARY_WATER) {
						e.setCancelled(true);
						e.getToBlock().setType(Material.OBSIDIAN);
						e.getBlock().getWorld().playSound(e.getBlock().getLocation(), Sound.FIZZ, 1, 1);
					}
				}
			}
		}
	}


}