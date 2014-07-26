package com.github.intangir.Tweaks;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class Cauldrons extends Tweak
{
	Cauldrons(Tweaks plugin) {
		super(plugin);
		TWEAK_NAME = "Tweak_Cauldrons";
		TWEAK_VERSION = "1.0";
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public void onCauldronUse(PlayerInteractEvent e) {
		if(e.getClickedBlock().getType() == Material.CAULDRON && e.getMaterial() == Material.GLASS_BOTTLE && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = e.getClickedBlock();
			if(b.getData() > 0) {
				b.setData((byte) (b.getData()+1));
			}
		}
	}
}
