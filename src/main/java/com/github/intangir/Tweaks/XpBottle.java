package com.github.intangir.Tweaks;

import java.io.File;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class XpBottle extends Tweak
{
	XpBottle(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "xpbottle.yml");
		TWEAK_NAME = "Tweak_XpBottle";
		TWEAK_VERSION = "1.0";

		allowBottling = true;
		xpPerBottle = 92;
	}

	private boolean allowBottling;
	private int xpPerBottle;
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onXPBottle(ExpBottleEvent e) {
		e.setExperience(xpPerBottle);
	}
	
	public int getCurrentExp(Player p) {
		int level = p.getLevel();

		// get the xp into current level
		int total = Math.round(p.getExp() * levelToXP(level + 1));
		for(int l = 1; l <= level; l++) {
			// add up all of the XP for each level they have
			total += levelToXP(l);
		}
		return total;
	}
	
	public int levelToXP(int level) {
		return 5 + level * 2 + Math.max(level - 16, 0) * 3 + Math.max(level - 31, 0) * 4; 
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onBottleXp(PlayerInteractEvent e) {
		if(allowBottling && e.getMaterial() == Material.GLASS_BOTTLE && e.getClickedBlock().getType() == Material.ENCHANTMENT_TABLE) {
			e.setCancelled(true);
			
			Player p = e.getPlayer();
			int total = getCurrentExp(p);
			
			if(total < xpPerBottle) {
				p.sendMessage(ChatColor.RED + "Not enough experience to fill a bottle.");
				return;
			}
			
			// take out XP for a bottle
			total -= xpPerBottle;
			
			// remove 1 bottle from hand
			ItemStack stack = p.getItemInHand();
			stack.setAmount(stack.getAmount() - 1);
			p.setItemInHand(stack);
			
			// reset XP to new value
			p.setExp(0);
			p.setLevel(0);
			p.setTotalExperience(0);
			p.giveExp(total);
			
			// add xp bottle to inventory
			Map<Integer, ItemStack> full = p.getInventory().addItem(new ItemStack(Material.EXP_BOTTLE));
			
			// or drop it if they are full
			for(ItemStack i : full.values()) {
				p.getWorld().dropItem(p.getLocation(), i);
			}
			
			p.sendMessage(ChatColor.AQUA + "Filled an experience bottle!");
			p.getWorld().playEffect(p.getEyeLocation(), Effect.ENDER_SIGNAL, 0);
			p.updateInventory();
		}			
	}
}
