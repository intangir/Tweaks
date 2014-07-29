package com.github.intangir.Tweaks;

import java.io.File;

import net.cubespace.Yamler.Config.Comment;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class Protect extends Tweak
{
	Protect(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "protect.yml");
		TWEAK_NAME = "Tweak_Protect";
		TWEAK_VERSION = "1.0";
		
		fireProtectAboveY = 0;
		
		enderProtect = true;
	}
	
	private int fireProtectAboveY;
	
	@Comment("stop endermen from stealing blocks")
	private boolean enderProtect;

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
		if(enderProtect && e.getEntityType() == EntityType.ENDERMAN) {
			e.setCancelled(true);
		}
	}
}
