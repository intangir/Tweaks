package com.github.intangir.Tweaks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class Time extends Tweak
{
	Time(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "time.yml");
		TWEAK_NAME = "Tweak_Time";
		TWEAK_VERSION = "1.0";

		worlds = new HashMap<String, Integer>();
		
		worlds.put("world_space", 17000);
		worlds.put("world_desert", 8000);
		
		resetticks = 600;
	}

	private Map<String, Integer> worlds;
	private int resetticks;
	
	@Override
	public void enable()
	{
		super.enable();
		
		for(World w : server.getWorlds()) {
			setTime(w);
		}
		
		server.getScheduler().scheduleSyncRepeatingTask(plugin, new FixWorlds(), 0, resetticks);
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		setTime(e.getWorld());
	}
	
	class FixWorlds implements Runnable {
		public void run() {
			for(World w : server.getWorlds()) {
				setTime(w);
			}
		}
	}
	
	void setTime(World w) {
		Integer time = worlds.get(w.getName());
		if(time != null) {
			w.setTime(time);
		}
	}
	
	@EventHandler
	public void onLeaveBed(PlayerBedLeaveEvent e) {
		setTime(e.getPlayer().getWorld());
	}
}
