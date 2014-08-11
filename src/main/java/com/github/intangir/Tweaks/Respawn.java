package com.github.intangir.Tweaks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@SuppressWarnings("deprecation")
public class Respawn extends Tweak
{
	Respawn(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "respawn.yml");
		TWEAK_NAME = "Tweak_Respawn";
		TWEAK_VERSION = "1.0";

		instance = this;
		
		defaultWorld = "world";
		
		worldRespawns =new HashMap<String, String>();
		worldRespawns.put("world", "1400");
		worldRespawns.put("world_nether", "world");
		worldRespawns.put("world_the_end", "world");
		
		overrides = new HashMap<String, String>();
		overrides.put("world_prison", "tweak.respawn.prisoner");
		
		spawnEffects = new HashMap<String, Integer>();
		spawnEffects.put("DAMAGE_RESISTANCE", 60);
		spawnEffects.put("WEAKNESS", 60);
		spawnEffects.put("CONFUSION", 6);
		spawnEffects.put("BLINDNESS", 3);
		
		noSpawnIds = new HashSet<Integer>(Arrays.asList(
			Material.WATER.getId(),
			Material.STATIONARY_WATER.getId(),
			Material.LAVA.getId(),
			Material.STATIONARY_LAVA.getId(),
			Material.LEAVES.getId(),
			Material.LEAVES_2.getId(),
			Material.VINE.getId(),
			Material.FIRE.getId(),
			Material.CACTUS.getId(),
			Material.OBSIDIAN.getId()
		));
		
		effects = new ArrayList<PotionEffect>();
		
		random = new Random();
	}
	
	public void enable()
	{
		super.enable();

		for(String effect : spawnEffects.keySet())
			effects.add(new PotionEffect(PotionEffectType.getByName(effect), spawnEffects.get(effect) * 20, 5));
	}
	

	private String defaultWorld;
	private Map<String, String> worldRespawns;
	private Map<String, String> overrides;
	private Map<String, Integer> spawnEffects;
	private Set<Integer> noSpawnIds;
	private transient Collection<PotionEffect> effects;
	private transient Random random;
	private static Respawn instance = null; 
	
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		
		final Player p = e.getPlayer();
		String wname = p.getWorld().getName();
		log.info("player respawn from " + wname);

		// apply spawn buffs (delayed)
		server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				p.addPotionEffects(effects);
			}
		}, 2);

		// override respawn world for some
		boolean overridden = false;
		for(Map.Entry<String, String> override : overrides.entrySet()) {
			log.info("checking " + p.getName() + " for " + override.getValue());
			if(p.hasPermission(override.getValue())) {
				wname = override.getKey();
				log.info("overriding respawn of " + p.getName() + " to " + wname);
				overridden = true;
			}
		}
				
		if(!overridden) {
			if(e.isBedSpawn()) {
				log.info("respawning " + p.getName() + " at bed: " + p.getBedSpawnLocation());
				return;
			} else {
				wname = defaultWorld;
			}
		}
		
		// get the actual spawn location now
		Location l = chooseSpawn(wname);
		log.info("chose spawn location for " + p.getName() + " at " + l);
		p.sendBlockChange(l, Material.BEACON, (byte) 0); // helps make it so they don't just instantly fall into the ground
		e.setRespawnLocation(l.add(0.5, 0.5, 0.5));
	}
	
	public static Location chooseSpawn_s(String world) {
		return instance.chooseSpawn(world);
	}
	
	// chooses a spawn point for the world
	public Location chooseSpawn(String world) {
		String r = worldRespawns.get(world);
		// if its a redirect to another world
		
		if(worldRespawns.containsKey(r) && server.getWorld(r) != null)
			return chooseSpawn(r);

		// otherwise it should be a number indicating the random spawn radius
		Integer radius = Integer.parseInt(r);
		
		World w = server.getWorld(world);
		
		Location l = w.getSpawnLocation(); 

		if(radius == 0)
			return l;
		
		while(true) {
			int x = l.getBlockX() + (random.nextInt(radius * 2) - radius);
			int z = l.getBlockZ() + (random.nextInt(radius * 2) - radius);
			Location spawn = getValidY(w, x, z);
			if(spawn != null)
				return spawn;
		}
	}

	public static Location getValidY_s(World w, int x, int z) {
		return instance.getValidY(w, x, z);
	}

	// finds the lowest spawn valid location at tha x and z
	public Location getValidY(World w, int x, int z) {
		w.loadChunk(x >> 4, z >> 4 );
		
		for(int y = 255; y > 0; y--) {
			Block block = w.getBlockAt(x, y, z);
			
			// go until you find not air
			if(block.getTypeId() != 0) {
				if(noSpawnIds.contains(block.getTypeId()))
					return null;
				return block.getLocation().add(0, 1, 0);
			}
		}
		return null;
	}
	
	// remove the immunity buffs granted on respawn if they attack anything
	@EventHandler(ignoreCancelled = true)
	public void onAttack(EntityDamageByEntityEvent e) {
		if(e.getDamager() != null && e.getDamager() instanceof Player) {
			Player attacker = (Player) e.getDamager();
			for(PotionEffect effect : effects) {
				attacker.removePotionEffect(effect.getType());
			}
		}
	}

	
}
