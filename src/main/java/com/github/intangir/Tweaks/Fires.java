package com.github.intangir.Tweaks;

import java.io.File;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;

public class Fires extends Tweak
{
	Fires(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "fires.yml");
		TWEAK_NAME = "Tweak_Fires";
		TWEAK_VERSION = "1.0";
		
		fireProtectAboveY = 0;
	}
	
	private int fireProtectAboveY;

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
}
