package com.github.intangir.Tweaks;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;

public class Vehicles extends Tweak
{
	Vehicles(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "vehicles.yml");
		TWEAK_NAME = "Tweak_Vehicles";
		TWEAK_VERSION = "1.0";

		fixExitMinecart = true;
		fixExitBoat = true;
		fixExitHorse = true;
		breakSwordInHand = true;
	}

	private boolean fixExitMinecart;
	private boolean fixExitBoat;
	private boolean fixExitHorse;
	private boolean breakSwordInHand;
	
	@EventHandler(ignoreCancelled = true)
	public void onVehicleExit(VehicleExitEvent e) {

		final LivingEntity exiter = e.getExited();
		final Vehicle vehicle = e.getVehicle();
		
		if(exiter instanceof Player) {
			if(	(fixExitMinecart && vehicle instanceof Minecart) || 
				(fixExitBoat	 && vehicle instanceof Boat) || 
				(fixExitHorse	 && vehicle instanceof Horse)) {
				
				final Location fixLoc = exiter.getLocation().add(0, 0.5, 0);
				
				server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	    			public void run() {
	    		    	
	    				exiter.teleport(fixLoc);
	    				ItemStack hand = ((Player)exiter).getItemInHand();
	    		    	
	    				// auto destroy boat/minecart if they are a holding a sword
	    				if(breakSwordInHand && hand != null && hand.getType().toString().contains("SWORD")) {
	    					ItemStack item = null;
	    					
	    					if(vehicle instanceof Minecart) {
	    						item = new ItemStack(Material.MINECART);
	    					} else if(vehicle instanceof Boat) {
	    						item = new ItemStack(Material.BOAT);
	    					}

	    					if(item != null) {
	    						exiter.getWorld().dropItem(fixLoc, item);
	    						vehicle.remove();
	    					}
	    				}
	    			}
	    		}, 2);
			}
		}
	}
}
