package com.github.intangir.Tweaks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class Titles extends Tweak
{
	Titles(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "titles.yml");
		TWEAK_NAME = "Tweak_Titles";
		TWEAK_VERSION = "1.0";

		titles = new HashMap<String, String>();
		
		titles.put("world", "Overworld,Sunshine and Rainbows");
		titles.put("world_nether", "The Nether,Abandon all hope all who enter here");
	}

	private Map<String, String> titles;
	
	public void delayedEnable()
	{
		server.dispatchCommand(server.getConsoleSender(), "gamerule sendCommandFeedback false");
	}
	
	@EventHandler
	public void onWorldChange(PlayerChangedWorldEvent e) {
		setTitle(e.getPlayer(), titles.get(e.getPlayer().getWorld().getName()));
	}

	@EventHandler
	public void onWorldJoin(PlayerJoinEvent e) {
		setTitle(e.getPlayer(), titles.get(e.getPlayer().getWorld().getName()));
	}
	
	public void setTitle(Player player, String title) {
		if(title != null) {
			String[] parts = title.split(",",2);
			if(parts.length > 1) {
				server.dispatchCommand(server.getConsoleSender(), "title " + player.getName() + " subtitle {text:\"" + parts[1] + "\"}");
			}
			server.dispatchCommand(server.getConsoleSender(), "title " + player.getName() + " title {text:\"" + parts[0] + "\"}");
		}
		
	}
}
