package com.github.intangir.Tweaks;

import java.io.File;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

public class Repairs extends Tweak
{
	Repairs(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "repairs.yml");
		TWEAK_NAME = "Tweak_Repairs";
		TWEAK_VERSION = "1.0";
	}
	
	private int priorPenaltyMin = 0;
	private int priorPenaltyMax = 32;
	
	// opening up an anvil
	@EventHandler(ignoreCancelled = true)
	public void onAnvilPickupItem(InventoryClickEvent e) {
		if(e.getInventory() instanceof AnvilInventory) {
			ItemStack i = e.getCurrentItem();
			if(i != null && i.hasItemMeta() && i.getItemMeta() instanceof Repairable) {
				Repairable r = (Repairable) i.getItemMeta();
				// force repair history into configurable range
				debug("Adjusting repair cost for " + e.getCurrentItem().getType().toString() + " " + r.getRepairCost() + " between " + priorPenaltyMin + " and " + priorPenaltyMax);
				int cost = r.getRepairCost(); 
				if(cost > priorPenaltyMax) {
					cost = priorPenaltyMax; 
				} else if(cost < priorPenaltyMin) {
					cost = priorPenaltyMin;
				}
				if(cost != r.getRepairCost()) {
					debug("Adjusted to repair " + cost);
					r.setRepairCost(cost);
					i.setItemMeta((ItemMeta) r);
				}
			}
		}
	}
}
