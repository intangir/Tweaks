package com.github.intangir.Tweaks;

import java.io.File;

import net.cubespace.Yamler.Config.Comment;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class Protect extends Tweak
{
	Protect(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "protect.yml");
		TWEAK_NAME = "Tweak_Protect";
		TWEAK_VERSION = "1.0";
		
		fireProtectAboveY = 0;
		creeperProtectAboveY = 62;
		enderManProtect = true;
		enderCrystalProtect = false;
	}
	
	@Comment("stops fire from spreading and consuming blocks over Y")
	private int fireProtectAboveY;
	
	@Comment("stops creepers from blowing up blocks when detonating over Y")
	private int creeperProtectAboveY;
	
	@Comment("stop endermen from stealing blocks")
	private boolean enderManProtect;
	
	@Comment("stop endercrystals from breaking, useful for decoration, ruins dragon encounter")
	private boolean enderCrystalProtect;

	@EventHandler(ignoreCancelled = true)
	public void onFireSpread(BlockIgniteEvent e) {
		if(e.getCause() != IgniteCause.FLINT_AND_STEEL && e.getBlock().getY() > fireProtectAboveY)
			e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onFireSpread(BlockBurnEvent e) {
		if(e.getBlock().getY() > fireProtectAboveY)
			e.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEnderGrief(EntityChangeBlockEvent e) {
		if(enderManProtect && e.getEntityType() == EntityType.ENDERMAN) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onCreeperExplode(EntityExplodeEvent e) {
		if(e.getEntityType() == EntityType.CREEPER && e.getLocation().getBlockY() > creeperProtectAboveY)
			e.blockList().clear();
	}

	@EventHandler(ignoreCancelled = true)
	public void onCrystalDamage(EntityDamageEvent e) {
		if(enderCrystalProtect && e.getEntityType() == EntityType.ENDER_CRYSTAL)
			e.setCancelled(true);
	}
}
