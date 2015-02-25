package com.github.intangir.Tweaks;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Effects extends Tweak
{
	Effects(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "effects.yml");
		TWEAK_NAME = "Tweak_Effects";
		TWEAK_VERSION = "1.0";

		worlds = new HashMap<String, Map<String, Integer>>();
		Map<String, Integer> exampleeffects = new HashMap<String, Integer>();
	
		exampleeffects.put("NIGHT_VISION", 1);
		worlds.put("world_twilight", exampleeffects);
		
		effects = new HashMap<World, Collection<PotionEffect>>();

		resetticks = 600;
	}
	
	private Map<String, Map<String, Integer>> worlds;
	private transient Map<World, Collection<PotionEffect>> effects;
	private int resetticks;

	@Override
	public void enable()
	{
		super.enable();

		for(World w : server.getWorlds()) {
			prepareEffects(w);
		}
		
		server.getScheduler().scheduleSyncRepeatingTask(plugin, new ApplyEffects(), 0, resetticks);

	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent e) {
		prepareEffects(e.getWorld());
	}

	class ApplyEffects implements Runnable {
		public void run() {
			for(World w : server.getWorlds()) {
				applyEffects(w);
			}
		}
	}

	void prepareEffects(World w) {
		
		Collection<PotionEffect> neweffects = new ArrayList<PotionEffect>();
		
		if(worlds.containsKey(w.getName())) {
			for(String effect : worlds.get(w.getName()).keySet()) {
				neweffects.add(new PotionEffect(PotionEffectType.getByName(effect), resetticks * 2, worlds.get(w.getName()).get(effect), true, false));
			}
			effects.put(w, neweffects);
		}
	}

	void applyEffects(World w) {

		if(effects.containsKey(w)) {
			for(Player p : w.getPlayers()) {
				applyEffects(p);
			}
		}
	}
	
	void applyEffects(Player p) {
		if(effects.containsKey(p.getWorld())) {
			for(PotionEffect effect : effects.get(p.getWorld())) {
				p.addPotionEffect(effect, true);
			}
		}
	}
	
	void removeEffects(World w, Player p) {
		if(effects.containsKey(w)) {
			for(PotionEffect effect : effects.get(w)) {
				p.removePotionEffect(effect.getType());
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		
		final Player p = e.getPlayer();
		
		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				applyEffects(p);
			}
		}, 2);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		
		final Player p = e.getPlayer();
		
		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				applyEffects(p);
			}
		}, 2);
	}

	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {
		final Player p = e.getPlayer();
		final World old = e.getFrom();

		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				removeEffects(old, p);
				applyEffects(p);
			}
		}, 2);
	}
}
