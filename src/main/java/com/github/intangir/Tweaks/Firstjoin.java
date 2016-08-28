package com.github.intangir.Tweaks;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public class Firstjoin extends Tweak
{
	public Firstjoin() {}
	public Firstjoin(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "firstjoin.yml");
		TWEAK_NAME = "Tweak_Firstjoin";
		TWEAK_VERSION = "1.0";
		
		applySpawnStatus = true;

		commands =	Arrays.asList(
			"say Welcome %player%",
			"givebook %player% manual");
	}
	
	public List<String> commands;
	private boolean applySpawnStatus;

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onFirstJoin(PlayerJoinEvent e) {
		if(!e.getPlayer().hasPlayedBefore()) {
			for(String command : commands) {
				server.dispatchCommand(server.getConsoleSender(), command.replaceAll("%player%", e.getPlayer().getName()));
			}
			if(applySpawnStatus && plugin.getMainConfig().isRespawn()) {
				Respawn.applySpawnStatus_s(e.getPlayer());
			}
		}
	}
}
