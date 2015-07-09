package com.github.intangir.Tweaks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;

public class Fixes extends Tweak
{
	Fixes(Tweaks plugin) {
		super(plugin);
		TWEAK_NAME = "Tweak_Fixes";
		TWEAK_VERSION = "1.0";
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public void onSignChange(SignChangeEvent e) {
		int chars = 0;
		for(String line : e.getLines()) {
			chars += line.length();
		}
		debug("sign placed lines " + chars );
		if(chars > 200) {
			e.getPlayer().kickPlayer("hacking");
			e.getPlayer().setBanned(true);
			e.setCancelled(true);
			log.info(e.getPlayer().getName() + " attempted to place a hacked sign");
		}
	}
}
