package com.github.intangir.Tweaks;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LargeFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

public class Disasters extends Tweak
{
	Disasters(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "disasters.yml");
		TWEAK_NAME = "Tweak_Disasters";
		TWEAK_VERSION = "1.0";
		
		meteorTicks = 20;
		meteorPitch = 88;
		meteorYaw = 90;
		meteorTime = 22500;
		meteorPeakChancePerChunk = 0.02;
		meteorTimeRange = 1000;
		meteorYield = 2;
		
		meteorWorlds = new HashSet<String>();
		meteorWorlds.add("world_astroid");
	}
	
	private Integer meteorTicks;
	private Integer meteorPitch;
	private Integer meteorYaw;
	private Integer meteorTime;
	private Integer meteorTimeRange;
	private Integer meteorYield;
	private Double meteorPeakChancePerChunk;
	
	private Set<String> meteorWorlds;
	
	
	@Override
	public void enable()
	{
		super.enable();

		server.getScheduler().scheduleSyncRepeatingTask(plugin, new MeteorShower(), meteorTicks, meteorTicks);

	}
	
	class MeteorShower implements Runnable {
		public void run() {
			for(String w : meteorWorlds) {
				final World world = server.getWorld(w);
				if(world == null) {
					return;
				}
				Location l = new Location(world, 0, 250, 0);
				Chunk[] chunks = world.getLoadedChunks();
				
				int timing = (int) (Math.abs(world.getTime() - meteorTime) % 24000);
				if(timing < meteorTimeRange) {
					int frequency = (int)((((double)(meteorTimeRange - timing)) / meteorTimeRange) * meteorPeakChancePerChunk * chunks.length) ;
					debug("time: " + world.getTime() + " timing: " + timing + " frequency: " + frequency);
					
					for(int i = 0 ; i < frequency; i++) {
						Chunk chunk = chunks[rand.nextInt(chunks.length)];
						l.setX((chunk.getX() << 4) + rand.nextInt(16));
						l.setZ((chunk.getZ() << 4) + rand.nextInt(16));
						l.setYaw(meteorYaw);
						l.setPitch(meteorPitch);
						final Location meteorLoc = l.clone();
						
						//debug("set fireball spawn " + meteorLoc);
						
						server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							public void run() {
								LargeFireball fireball = (LargeFireball) world.spawnEntity(meteorLoc, EntityType.FIREBALL);
								fireball.setIsIncendiary(true);
								fireball.setYield(meteorYield);
								world.playSound(meteorLoc, Sound.AMBIENCE_THUNDER, (float)100, (float)0.5);
								world.playSound(meteorLoc, Sound.FIREWORK_LAUNCH, (float)100, (float)0.5);
								//debug("spawned fireball " + fireball.getLocation());
							}
						}, rand.nextInt(meteorTicks));
					}
				}
			}
		}
	}
	
	
    @EventHandler
    public void onExplosion(EntityExplodeEvent e) {
    	//debug("explosion " + e.getEntityType() + " " + e.getEntity().getType() + " " + e.getLocation().getWorld().getName());
    	if(e.getEntity() != null && e.getEntityType() == EntityType.FIREBALL && meteorWorlds.contains(e.getLocation().getWorld().getName())) {
    		//debug("explosion cancelled");
    		e.setYield(0);
    	}
    }
}
