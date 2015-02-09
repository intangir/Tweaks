package com.github.intangir.Tweaks;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.cubespace.Yamler.Config.Comment;

import org.bukkit.PortalType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Mobs extends Tweak
{
	Mobs(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "mobs.yml");
		TWEAK_NAME = "Tweak_Mobs";
		TWEAK_VERSION = "1.0";
		
		golemRegeneration = true;
		enderPortalCreation = true;
		agroRanges = new HashMap<String, Integer>();
		//agroRanges.put("PIG_ZOMBIE", 16);
		//agroRanges.put("ZOMBIE", 32);
		agroRanges.put("BLAZE", 20);
		agroRanges.put("IRON_GOLEM", 20);
		
		nerfSpawnerAI = new HashSet<String>();
		nerfSpawnerAI.add("ZOMBIE");
		nerfSpawnerAI.add("SPIDER");
		nerfSpawnerAI.add("CAVE_SPIDER");
		nerfSpawnerAI.add("SKELETON");
		
		fromMobSpawner = null;
	}
	
	private boolean golemRegeneration;
	private boolean enderPortalCreation;
	private Map<String, Integer> agroRanges;

	@Comment("nerf specific mob spawners instead of all of them, only works with spigot")
	private Set<String> nerfSpawnerAI;
	
	private transient Method getEntityHandle;
	private transient Field fromMobSpawner;

	public void enable()
	{
		super.enable();

		// nms reflection stuff for nerfing spawner mob ai
		try {
			// get the field for flagging if an entity spawned from a spawner (this is only set if the ai nerf is on in spigot normally)
			fromMobSpawner = getVersionedClass("net.minecraft.server.Entity").getDeclaredField("fromMobSpawner");
			fromMobSpawner.setAccessible(true);

			// get the method for getting underlying NMS entity object handle from a craftbukkit entity object
			getEntityHandle = getVersionedClass("org.bukkit.craftbukkit.entity.CraftEntity").getMethod("getHandle");

		} catch(Exception ex) {
			log.severe("Failed to access dropsProtect methods");
			ex.printStackTrace();
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onGolemDamage(EntityDamageEvent e) {
		if(golemRegeneration && e.getEntity() != null && e.getEntityType() == EntityType.IRON_GOLEM) {
			IronGolem golem = (IronGolem) e.getEntity();
			golem.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 2400, 1));
		}
	}

	// stop the damn dragon from escaping the end..
	@EventHandler(ignoreCancelled = true)
	public void onEntityPortal(EntityPortalEvent e) {
		if(e.getEntity() != null && e.getEntityType() == EntityType.ENDER_DRAGON)
			e.setCancelled(true);
	}
	
	// cancel create ender portal
	@EventHandler(ignoreCancelled = true) 
	public void onCreatePortal(EntityCreatePortalEvent e) {
		if(!enderPortalCreation && e.getPortalType() == PortalType.ENDER) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onTargetEntity(final EntityTargetLivingEntityEvent e) {
		if(e.getTarget() != null ) {
			Integer range = agroRanges.get(e.getEntityType().toString());
			if(range != null) {
				if(range * range < e.getEntity().getLocation().distanceSquared(e.getTarget().getLocation())) {
					e.setCancelled(true);
					/*server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							debug("canceling agro from " + e.getEntityType() + " reason " + e.getReason().toString() + " toward " + e.getTarget().getType());
							if(e.getEntity() instanceof PigZombie) {
								PigZombie pig = (PigZombie) e.getEntity();
								int anger = pig.getAnger(); 
								pig.setAnger(0);
								pig.setAngry(false);
								debug("set anger to " + pig.getAnger() + " from " + anger);
							}
							e.setTarget(null);
						}
					}, 1);*/
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onSpawnerSpawn(final CreatureSpawnEvent e) {
		if(fromMobSpawner != null && e.getSpawnReason() == SpawnReason.SPAWNER && nerfSpawnerAI.contains(e.getEntityType().toString())) {
			debug("nerfing ai on " + e.getEntityType() + " " + e.getEntity().getUniqueId());
			try {
				fromMobSpawner.set(getEntityHandle.invoke(e.getEntity()), true);
			} catch (Exception ex) {
				log.severe("failed to nerf mobs ai");
				ex.printStackTrace();
			}
		}
	}
}
