package com.github.intangir.Tweaks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.world.WorldLoadEvent;

public class Weather extends Tweak
{
	Weather(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "weather.yml");
		TWEAK_NAME = "Tweak_Weather";
		TWEAK_VERSION = "1.0";

		worlds = new HashMap<String, String>();
		
		worlds.put("world_space", "clear");
		worlds.put("world_jungle", "raining");
	}

	private Map<String, String> worlds;
	
	@Override
	public void enable()
	{
		super.enable();
		
		for(World w : server.getWorlds()) {
			setWeather(w);
		}
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		setWeather(e.getWorld());
	}
	
	void setWeather(World w) {
		String weather = worlds.get(w.getName());
		if(weather == null)
			return;
		
		if(weather.equals("raining")) {
			log.info("Setting raining weather on " + w.getName());
			w.setStorm(true);
		} else if(weather.equals("clear")) {
			log.info("Setting clear weather on " + w.getName());
			w.setStorm(false);
		}
		
		w.setWeatherDuration(2000000000);
		w.setThundering(false);
		w.setThunderDuration(2000000000);
	}
	
	@EventHandler
	public void onLeaveBed(PlayerBedLeaveEvent e) {

		// this event screws up the weather, so reapply the settings on all worlds
		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				
				for(World w : server.getWorlds()) {
					setWeather(w);
				}
			}
		}, 40);
	}
}
