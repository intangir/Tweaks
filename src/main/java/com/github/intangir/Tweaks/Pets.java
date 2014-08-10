package com.github.intangir.Tweaks;

import java.io.File;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class Pets extends Tweak
{
	Pets(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "pets.yml");
		TWEAK_NAME = "Tweak_Pets";
		TWEAK_VERSION = "1.0";
		
		exclusiveHorses = true;
		leadsTransferable = true;
		leadsTransferOwnership = true;
		dogsStaySeated = true;
	}
	
	private boolean exclusiveHorses;
	private boolean leadsTransferable;
	private boolean leadsTransferOwnership;
	private boolean dogsStaySeated;

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEntityEvent e) {
		Player player = e.getPlayer();
		Entity target = e.getRightClicked();
		
		if(player != null && target != null) {
			
			if(leadsTransferable && target.getType() == EntityType.PLAYER) {
				// they are right clicking another player
				// see if they are holding any leashes
				for(Entity entity : player.getNearbyEntities(5, 5, 5)) {
					if(entity instanceof LivingEntity) {
						LivingEntity animal = (LivingEntity) entity;
						
						// a nearby animal is leashed by player?
						if(animal.isLeashed() && animal.getLeashHolder() == player) {
							// give it to his target
							animal.setLeashHolder(target);
							e.setCancelled(true);
							
							if(leadsTransferOwnership && entity instanceof Tameable) {
								// transfer ownership of tamable animals
								Tameable pet = (Tameable) entity;
								if((pet.isTamed() && pet.getOwner() == player) || player.isOp()) {
									pet.setOwner((Player)target);
								}
							}
						}
					}
				}
			}
			
			// horses can only be interacted with by their owners
			if(exclusiveHorses && target instanceof Horse) {
				Horse horse = (Horse) target;
				if(horse.isTamed() && horse.getOwner() != player && !player.isOp()) {
					e.setCancelled(true);
					player.updateInventory();
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onAnimalTeleport(EntityTeleportEvent e) {
		// im not sure if it might send subclasses in here too, skip them if so
		if(!dogsStaySeated || e instanceof EntityPortalEvent || e instanceof EntityPortalExitEvent)
			return;
		
		Entity entity = e.getEntity();
		
		// attempt to prevent wolves from teleporting to you when you are attacked if you told them to sit
		if(entity instanceof Wolf)
			if(((Wolf) entity).isSitting())
				e.setCancelled(true);
	}
}
