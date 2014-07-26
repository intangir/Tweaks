package com.github.intangir.Tweaks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

import net.cubespace.Yamler.Config.Config;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.util.Vector;

public class Anvils extends Tweak
{
	public Anvils() {}
	public Anvils(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "anvils.yml");
		TWEAK_NAME = "Tweak_Anvil";
		TWEAK_VERSION = "1.0";

		smashable = new HashMap<String, Smashable>();
		
		smashable.put(Material.COBBLESTONE.toString(),  new Smashable(Material.SAND,         0));
		smashable.put(Material.HARD_CLAY.toString(),    new Smashable(Material.SAND,         1));
		smashable.put(Material.SMOOTH_BRICK.toString(), new Smashable(Material.SMOOTH_BRICK, 2));
		smashable.put(Material.STONE.toString(),        new Smashable(Material.COBBLESTONE,  0));
	}

	private Map<String, Smashable> smashable;
	
	@Getter
	public class Smashable extends Config {
		public Smashable() {}
		public Smashable(Material material, int data) {
			this.material = material.toString();
			this.data = (byte) data;
		}
		private String material;
		private byte data;
	}
	
	// can lift up an anvil when piston retracts (so that it can fall again)
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onLiftAnvil(BlockPistonRetractEvent e) {
		Block block = e.getRetractLocation().getBlock();
		if(block.getType() == Material.ANVIL && e.getDirection() == BlockFace.DOWN && e.isSticky() == true) {
			
			FallingBlock fall = block.getLocation().getWorld().spawnFallingBlock(block.getLocation(), block.getType(), block.getData());
			Vector velocity = new Vector(0.0, 1, 0.0);
			fall.setVelocity(velocity);
			block.setType(Material.AIR);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onAnvilLand(EntityChangeBlockEvent e) {
		if(e.getTo() == Material.ANVIL) {
			Block smashed = e.getBlock().getRelative(BlockFace.DOWN);
			
			Smashable into = smashable.get(smashed.getType().toString());
			if(into != null) {
				smashed.setType(Material.getMaterial(into.getMaterial()));
				smashed.setData(into.getData());
			}
		}
	}
}
