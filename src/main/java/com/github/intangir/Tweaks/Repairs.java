package com.github.intangir.Tweaks;

import java.io.File;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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
	
	// opening up an anvil
	@EventHandler(ignoreCancelled = true)
	public void onAnvilOpen(InventoryOpenEvent e) {
		if(e.getInventory() instanceof AnvilInventory && e.getPlayer() != null && e.getPlayer().getInventory() != null) {
			// check their inventory for any items over repair cap
			PlayerInventory inv = e.getPlayer().getInventory();
			for(ItemStack i : inv) {
				if(i != null && i.hasItemMeta() && i.getItemMeta() instanceof Repairable) {
					Repairable r = (Repairable) i.getItemMeta();
					// remove previous repair history, treat it like a never before repaired item
					if(r.hasRepairCost() && r.getRepairCost() >= 1) {
						r.setRepairCost(0);
						i.setItemMeta((ItemMeta) r);
					}
				}
			}
		}
	}
}
