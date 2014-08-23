package com.github.intangir.Tweaks;

import java.io.File;

import org.bukkit.PortalType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEvent;
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
	}
	
	private boolean golemRegeneration;
	private boolean enderPortalCreation;
	
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
}
