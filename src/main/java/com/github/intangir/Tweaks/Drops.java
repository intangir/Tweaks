package com.github.intangir.Tweaks;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class Drops extends Tweak
{
	Drops(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "drops.yml");
		TWEAK_NAME = "Tweak_Drops";
		TWEAK_VERSION = "1.0";
		
		fallingGravelDrops = true;
		fallingGravelChances = new HashMap<String, Integer>();
		fallingGravelChances.put("FLINT", 14);
		fallingGravelChances.put("QUARTZ", 14);
		fallingGravelChances.put("SULPHUR", 7);

		breakingGravelChances = new HashMap<String, List<Integer>>();
		breakingGravelChances.put("QUARTZ", Arrays.asList(10, 14, 25, 50));
		breakingGravelChances.put("SULPHUR", Arrays.asList(5, 7, 12, 25));
		
		deadBushDropsSelf = true;
		netherQuartzDropsGlowstone = true;
		itemsDropInPlace = true;
		endermanExtraDropChance = 50;
		pigmenNetherwartDropChance = 25;
		villagerDomesticDropChance = 10;
		spongeDropChance = 10;
		
		domesticItems = Arrays.asList("CARROT_ITEM", "POTATO_ITEM", "PUMPKIN_SEEDS", "MELON_SEEDS", "NETHER_STALK", "BOOK", "SEEDS");
	}
	
	private boolean fallingGravelDrops;
	private boolean breakingGravelDrops;
	private boolean deadBushDropsSelf;
	private boolean netherQuartzDropsGlowstone;
	private boolean itemsDropInPlace;
	private int endermanExtraDropChance;
	private int villagerDomesticDropChance;
	private int pigmenNetherwartDropChance;
	private int spongeDropChance;
	private Map<String, Integer> fallingGravelChances;
	private Map<String, List<Integer>> breakingGravelChances;
	private List<String> domesticItems;

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onFallingItemBreak(ItemSpawnEvent e) {
		if(!fallingGravelDrops)
			return;
		
		ItemStack item = e.getEntity().getItemStack();
		// find out of its gravel item spawning
		if(item.getType() == Material.GRAVEL && e.getLocation().getBlock().getType() != Material.AIR) {
	        // check if its breaking from a falling block
	        for(Entity n : e.getEntity().getNearbyEntities(1,1,1)) {
	            if(n.getType() == EntityType.FALLING_BLOCK) {
	            	// determine if another item should fall
	            	for(Map.Entry<String, Integer> chance : fallingGravelChances.entrySet()) {
	            		if(rand.nextInt(100) < chance.getValue()) {
	            			item.setType(Material.getMaterial(chance.getKey()));
	            			return;
	            		}
	            	}
	            }
	        }
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent e) {
	    
		if(breakingGravelDrops && e.getBlock().getType() == Material.GRAVEL) {
	        int fortune = getDropLevel(e.getPlayer());
	        
        	for(Map.Entry<String, List<Integer>> chance : breakingGravelChances.entrySet()) {
        		if(rand.nextInt(100) < chance.getValue().get(fortune)) {
        			// break it ourselves and drop our own drop
        			e.setCancelled(true);
        			e.getBlock().setType(Material.AIR);
        			e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), new ItemStack(Material.getMaterial(chance.getKey()), 1));
        			return;
        		}
        	}
		}

		if(deadBushDropsSelf && e.getBlock().getType() == Material.DEAD_BUSH) {
			e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), new ItemStack(Material.DEAD_BUSH, 1));
			return;
		}
		
	    if(netherQuartzDropsGlowstone && e.getBlock().getType() == Material.QUARTZ_ORE && e.getExpToDrop() > 0) {
	        for(int i = 0; i < 4; i++) {
	            if(rand.nextInt(100) < 25) {
	                e.getBlock().getWorld().dropItem(e.getBlock().getLocation(), new ItemStack(Material.GLOWSTONE_DUST , 1));
	                return;
	            }
	        }
	    }
	}
	
	// determines fortune level for player
	public int getDropLevel(Player player) {
	    if(player == null) {
	        return 0;
	    }

	    ItemStack inHand = player.getItemInHand();
	    if(inHand == null) {
	        return 0;
	    }
	    
	    if(inHand.getType().toString().contains("SPADE") == false) {
	        return 0;
	    }
	    return inHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent e) {

		// drop items in place instead of "naturally" (where they teleport through walls)
	    if(itemsDropInPlace) {
			List<ItemStack> drops = e.getDrops();
		    for(ItemStack item : drops) {
		        e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), item);
		    }
		    drops.clear();
	    }
	    
	    // extra drop chance in 'world' only
	    if(endermanExtraDropChance > 0 && e.getEntity().getType() == EntityType.ENDERMAN && e.getEntity().getWorld().getName() == "world")
	        if(rand.nextInt(100) < endermanExtraDropChance)
	            e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), new ItemStack(Material.ENDER_PEARL, 1));

	    if(pigmenNetherwartDropChance > 0 && e.getEntity().getType() == EntityType.PIG_ZOMBIE)
	        if(rand.nextInt(100) < pigmenNetherwartDropChance)
	            e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), new ItemStack(Material.NETHER_STALK, 1));
	    
	    if(villagerDomesticDropChance > 0 &&
	    	((e.getEntity().getType() == EntityType.ZOMBIE && ((Zombie)e.getEntity()).isVillager()) ||
	    	e.getEntity().getType() == EntityType.VILLAGER)) {
	    	if(rand.nextInt(100) < villagerDomesticDropChance)
	    		e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), new ItemStack(Material.getMaterial(domesticItems.get(rand.nextInt(domesticItems.size()))), 1));
	    }
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onFish(PlayerFishEvent e) {
		if(spongeDropChance > 0 && e.getState() == PlayerFishEvent.State.CAUGHT_FISH && e.getCaught() instanceof Item) {
			Item caught = (Item) e.getCaught();
			if(caught.getItemStack().getType() == Material.RAW_FISH) {
				caught.setItemStack(new ItemStack(Material.SPONGE, 1, (short) 1));
			}
		}
	}
}
