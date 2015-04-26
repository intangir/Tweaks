package com.github.intangir.Tweaks;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class Cauldrons extends Tweak
{
	Cauldrons(Tweaks plugin) {
		super(plugin);
		TWEAK_NAME = "Tweak_Cauldrons";
		TWEAK_VERSION = "1.0";
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public void onCauldronUse(PlayerInteractEvent e) {
		if(e.getClickedBlock().getType() == Material.CAULDRON && e.getMaterial() == Material.GLASS_BOTTLE && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = e.getClickedBlock();
			if(b.getData() == 3) {
				e.setCancelled(true);
				
				Player p = e.getPlayer();
			
				// remove 1 bottle from hand
				ItemStack stack = p.getItemInHand();
				stack.setAmount(stack.getAmount() - 1);
				p.setItemInHand(stack);
				
				// add water bottle to inventory
				Map<Integer, ItemStack> full = p.getInventory().addItem(new ItemStack(Material.POTION));
				
				// or drop it if they are full
				for(ItemStack i : full.values()) {
					p.getWorld().dropItem(p.getLocation(), i);
				}
				
				p.updateInventory();
			}
		}
	}
}
