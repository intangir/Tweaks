package com.github.intangir.Tweaks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Mining extends Tweak
{
	Mining(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "mining.yml");
		TWEAK_NAME = "Tweak_Mining";
		TWEAK_VERSION = "1.0";

		fatigueWorlds = new HashMap<String, List<Integer>>();
		// setup a normal world profile
		List<Integer> normalWorld = new ArrayList<Integer>();
		normalWorld.add(40);
		normalWorld.add(20);
		fatigueWorlds.put("world", normalWorld);
		// setup a nether world profile
		List<Integer> netherWorld = new ArrayList<Integer>();
		netherWorld.add(256);
		netherWorld.add(256);
		fatigueWorlds.put("world_nether", netherWorld);
		
		slowEffects = new ArrayList<PotionEffect>();
		//weakEffects = new ArrayList<PotionEffect>();
		miningEffects = new ArrayList<PotionEffect>();
		currentLevels = new HashMap<Player, Integer>();
		currentTimers = new HashMap<Player, Long>();
		emptyLevels = new ArrayList<Integer>();
		checkseconds = 4;
		durationseconds = 600;
	}
	
	private Map<String, List<Integer>> fatigueWorlds;
	private transient List<PotionEffect> slowEffects;
	private transient List<PotionEffect> miningEffects;
	//private transient List<PotionEffect> weakEffects;
	private transient Map<Player, Integer> currentLevels;
	private transient Map<Player, Long> currentTimers;
	private transient List<Integer> emptyLevels;
	private int checkseconds;
	private int durationseconds;

	@Override
	public void enable()
	{
		super.enable();
		
		for(int level = 0; level < 3; level++) {
			miningEffects.add(new PotionEffect(PotionEffectType.SLOW_DIGGING, durationseconds * 20, level, true, false));
			slowEffects.add(new PotionEffect(PotionEffectType.SLOW, durationseconds * 20, level, true, false));
			//weakEffects.add(new PotionEffect(PotionEffectType.WEAKNESS, durationseconds * 20, level, true, false));
		}

		server.getScheduler().scheduleSyncRepeatingTask(plugin, new ApplyEffects(), 0, checkseconds * 20);

	}

	class ApplyEffects implements Runnable {
		public void run() {
			for(World w : server.getWorlds()) {
				applyEffects(w);
			}
		}
	}

	void applyEffects(World w) {
		if(fatigueWorlds.containsKey(w.getName().toString())) {
			for(Player p : w.getPlayers()) {
				applyEffects(p, fatigueWorlds.get(w.getName().toString()));
			}
		}
	}
	
	void applyEffects(Player p) {
		if(fatigueWorlds.containsKey(p.getWorld().getName().toString())) {
			applyEffects(p, fatigueWorlds.get(p.getWorld().getName().toString()));
		} else {
			applyEffects(p, emptyLevels);
		}
	}
	
	void applyEffects(Player p, List<Integer> levels) {
		int applyLevel = -1;
		for(Integer level : levels) {
			if(p.getLocation().getBlockY() <= level) {
				applyLevel++;
			}
		}
		
		if(applyLevel > 0) {
			for(PotionEffect effect : p.getActivePotionEffects()) {
				//debug("checking potion level " + effect.getType().toString() + " at " + effect.getAmplifier());
				if(effect.getType().equals(PotionEffectType.INCREASE_DAMAGE) && effect.getAmplifier() == 1) {
					applyLevel--;
					break;
				}
			}
		}
		
		if(!currentLevels.containsKey(p) || currentLevels.get(p) != applyLevel || !currentTimers.containsKey(p) || currentTimers.get(p) < System.currentTimeMillis()) {
			debug("applying fatigue level " + applyLevel + " to " + p.getName());
			currentLevels.put(p, applyLevel);
			currentTimers.put(p, (System.currentTimeMillis() + (durationseconds * 1000)) - (checkseconds * 2000) );
			if(applyLevel >= 0) {
				p.addPotionEffect(miningEffects.get(applyLevel), true);
				p.addPotionEffect(slowEffects.get(applyLevel), true);
				//p.addPotionEffect(weakEffects.get(applyLevel), true);
			} else {
				p.removePotionEffect(PotionEffectType.SLOW_DIGGING);
				p.removePotionEffect(PotionEffectType.SLOW);
				//p.removePotionEffect(PotionEffectType.WEAKNESS);
			}
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		
		final Player p = e.getPlayer();
		currentTimers.put(p, System.currentTimeMillis());
		
		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				applyEffects(p);
			}
		}, 2);
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		
		final Player p = e.getPlayer();
		currentTimers.put(p, System.currentTimeMillis());
		
		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				applyEffects(p);
			}
		}, 2);
	}

	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent e) {

		final Player p = e.getPlayer();
		currentTimers.put(p, System.currentTimeMillis());
		
		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				applyEffects(p);
			}
		}, 2);
	}

	@EventHandler
	public void onPlayerConsume(PlayerItemConsumeEvent e) {
		if(e.getItem().getType() != Material.MILK_BUCKET) {
			return;
		}
		
		final Player p = e.getPlayer();
		currentTimers.put(p, System.currentTimeMillis());
		
		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				applyEffects(p);
			}
		}, 2);
	}
}
