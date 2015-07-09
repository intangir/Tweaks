package com.github.intangir.Tweaks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.material.Torch;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Physics extends Tweak
{
	Physics(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "physics.yml");
		TWEAK_NAME = "Tweak_Trees";
		TWEAK_VERSION = "1.0";

		fallingTrees = true;
		treeHeight = 40;
		netherRackIgniteChance = 20;
		fallingBlockDamage = 2;
		
		fallingBlocks = new HashSet<String>();
		fallingBlocks.add("DIRT");
		fallingBlocks.add("GRASS");
		fallingBlocks.add("MYCEL");
		fallingBlocks.add("COBBLESTONE");
		
		logBlocks = new HashSet<String>();
		logBlocks.add("LOG");
		logBlocks.add("LOG_2");
		
		leafBlocks = new HashSet<String>();
		leafBlocks.add("LEAVES");
		leafBlocks.add("LEAVES_2");
		
		oreBlocks = new HashSet<String>();
		oreBlocks.add("DIAMOND_ORE");
		oreBlocks.add("EMERALD_ORE");
		oreBlocks.add("REDSTONE_ORE");
		oreBlocks.add("GLOWING_REDSTONE_ORE");
		oreBlocks.add("GOLD_ORE");
		oreBlocks.add("IRON_ORE");
		oreBlocks.add("LAPIS_ORE");

		allfaces = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH, BlockFace.UP, BlockFace.DOWN};
		
		obsidianGeneration = true;
		permaFrost = false;
		iceCoolsLavaRange = 1;
		packedIceCoolsLavaRange = 1;

		loosenOres = true;
		
		softTorchCancel = true;
		deepTorchCancel = 30;
		igniteWebs = true;

	}
	
	private boolean fallingTrees;
	private Integer treeHeight;
	private Integer netherRackIgniteChance;
	private Integer fallingBlockDamage;
	
	private Set<String> fallingBlocks;
	private Set<String> logBlocks;
	private Set<String> leafBlocks;
	private Set<String> oreBlocks;
	
	private transient Set<Material> fallingTypes;
	private transient Set<Material> logTypes;
	private transient Set<Material> leafTypes;
	private transient Set<Material> treeTypes;
	private transient Set<Material> oreTypes;
	private transient BlockFace[] allfaces;
	
	private boolean obsidianGeneration;
	private boolean permaFrost;
	private Integer iceCoolsLavaRange;
	private Integer packedIceCoolsLavaRange;
	
	private boolean loosenOres;
	
	private boolean softTorchCancel;
	private Integer deepTorchCancel;
	private boolean igniteWebs;


	@Override
	public void enable() {
		super.enable();

		fallingTypes = new HashSet<Material>();
		for(String blockName : fallingBlocks) {
			fallingTypes.add(Material.valueOf(blockName));
		}
		
		logTypes = new HashSet<Material>();
		for(String blockName : logBlocks) {
			logTypes.add(Material.valueOf(blockName));
		}

		leafTypes = new HashSet<Material>();
		for(String blockName : leafBlocks) {
			leafTypes.add(Material.valueOf(blockName));
		}

		oreTypes = new HashSet<Material>();
		for(String blockName : oreBlocks) {
			oreTypes.add(Material.valueOf(blockName));
		}

		treeTypes = new HashSet<Material>();
		treeTypes.addAll(leafTypes);
		treeTypes.addAll(logTypes);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockEvent(BlockFromToEvent e) {
		// lava burning redstone while cooling creates obsidian
		if(obsidianGeneration && (e.getBlock().getType() == Material.LAVA || e.getBlock().getType() == Material.STATIONARY_LAVA)) {
			if(e.getToBlock().getType() == Material.REDSTONE_WIRE) {
				for(BlockFace f : BlockFace.values()) {
					if(e.getToBlock().getRelative(f).getType() == Material.WATER || e.getToBlock().getRelative(f).getType() == Material.STATIONARY_WATER) {
						e.setCancelled(true);
						e.getToBlock().setType(Material.OBSIDIAN);
						e.getBlock().getWorld().playSound(e.getBlock().getLocation(), Sound.FIZZ, 1, 1);
					}
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onMelt(BlockFadeEvent e) {
		if(permaFrost && e.getBlock().getType() == Material.ICE) {
		    e.setCancelled(true);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent e) {
		Block block = e.getBlock();
		Material blockType = block.getType();
		
    	// falling blocks/caveins
		if(fallingTypes.contains(blockType) && !block.getRelative(BlockFace.DOWN).getType().isSolid()) {
    		dropBlock(block);
    	}
		cascade(block, fallingTypes, 10);
		
		// ice cools lava
		int coolRange = 0;
		if(blockType == Material.ICE) {
			coolRange = iceCoolsLavaRange;
		}
		if(blockType == Material.PACKED_ICE) {
			coolRange = packedIceCoolsLavaRange;
		}
		
		if(coolRange > 0) {
			boolean cooled = false;
			for(int x = block.getX() - coolRange; x <= block.getX() + coolRange; x++) {
				for(int y = block.getY() - coolRange; y <= block.getY() + coolRange; y++) {
					for(int z = block.getZ() - coolRange; z <= block.getZ() + coolRange; z++) {
						Block near = block.getWorld().getBlockAt(x, y, z);
						if(near.getType() == Material.STATIONARY_LAVA || near.getType() == Material.LAVA) {
							if(near.getData() == 0) {
								near.setType(Material.OBSIDIAN);
							} else {
								near.setType(Material.COBBLESTONE);
							}
							cooled = true;
						}
					}
				}
			}
			
			if(cooled && e.getBlock().getType() == Material.ICE) {
				e.getPlayer().sendMessage("The ice was melted while cooling nearby lava.");
				e.getBlock().setType(Material.STATIONARY_WATER);
			}
		}
		
		if(softTorchCancel && blockType == Material.TORCH) {
			Torch torch = new Torch(Material.TORCH, block.getData());
			Material against = block.getRelative(torch.getAttachedFace()).getType();
			if(against == Material.SAND || against == Material.DIRT || against == Material.GRAVEL || against == Material.GRASS) {
				e.getPlayer().sendMessage(ChatColor.RED + "Too soft there to fasten a torch there");
				block.breakNaturally();
			}
		}
		
		if(deepTorchCancel > e.getBlock().getY() && e.getBlock().getWorld().getEnvironment() == World.Environment.NORMAL) {
			if(blockType == Material.TORCH || blockType == Material.JACK_O_LANTERN || blockType == Material.FIRE) {
				e.getPlayer().sendMessage(ChatColor.RED + "Not enough oxygen for flames down here");
				block.getWorld().playSound(block.getLocation(), Sound.FIZZ, 1, 1);
				block.breakNaturally();
			}
		}
		
		if(igniteWebs && blockType == Material.FIRE) {
			for(BlockFace face : allfaces) {
				Block near = block.getRelative(face);
				if(near.getType() == Material.WEB) {
					near.breakNaturally();
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent e)
    {
        Block block = e.getBlock();

        // falling trees
        if(fallingTrees && logTypes.contains(block.getType())) {
            boolean isTree = false;
            List<Block> tree = new ArrayList<Block>();
            for (int i = 1; i < treeHeight; i++)
            {
                Block upBlock = block.getRelative(BlockFace.UP, i);
            	Material upType = upBlock.getType();
                
                if(treeTypes.contains(upType)) {
                	if(leafTypes.contains(upType)) {
                		// leaves make it a tree
                		isTree = true;
                	}
                	tree.add(upBlock);
                } else {
                	// it should either be a tree or not by now
                	break;
                }
            }
            
            if(isTree) {
            	cascade(tree.get(tree.size() - 1), treeTypes, 6);
            	for(Block b : tree) {
	            	dropBlock(b);
	            }
            }
        }
        
        // falling blocks, caveins
    	cascade(block, fallingTypes, 10);
        
        // netherrack
        if(netherRackIgniteChance > 0 && 
	    		block.getType() == Material.NETHERRACK && 
	    		block.getRelative(BlockFace.DOWN).getType() == Material.NETHERRACK &&
	    		rand.nextInt(100) < netherRackIgniteChance) {
            block.setType(Material.FIRE);
        }
        
        // loosen ores
        if(loosenOres && oreTypes.contains(block.getType())) {
        	for(BlockFace face : allfaces) {
        		Block near = block.getRelative(face);
        		
        		if(near.getType() == Material.STONE && near.getData() == 0) {
        			e.setCancelled(true);
        			near.getWorld().playSound(near.getLocation(), Sound.SILVERFISH_WALK, (float)1, (float)0.5);
        			near.setType(Material.COBBLESTONE);
        			return;
        		}
        	}
        }
    }
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockLand(EntityChangeBlockEvent e)
    {
		
		if(e.getEntity() instanceof FallingBlock) {
			cascade(e.getBlock(), fallingTypes, 10);
			
			if(fallingBlockDamage > 0 && e.getTo().isOccluding()) {
	            List<Entity> entities = e.getEntity().getNearbyEntities(0, 1, 0);
	            for (Entity ent : entities)
	            {
	                if (ent instanceof LivingEntity)
	                {
	                	((LivingEntity) ent).damage(fallingBlockDamage, e.getEntity());
	                    ((LivingEntity) ent).addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 140, 10));
	                }
	            }
			}
		}
    }
	
    @SuppressWarnings("deprecation")
	public void dropBlock(Block block)
    {
    	if(block.getType() != Material.AIR) {
			block.getLocation().getWorld().spawnFallingBlock(block.getLocation(), block.getType(), block.getData());
			block.setType(Material.AIR);
		}
    }
    
    public void cascade(final Block block, final Set<Material> types, final int spread) {
		if(spread > 0) {
	    	server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				public void run() {
					for(BlockFace face : allfaces) {
	
						Block near = block.getRelative(face);
						
				    	if(!types.contains(near.getType()) || near.getRelative(BlockFace.DOWN).getType().isSolid()) {
				    		continue;
				    	}
				    	dropBlock(near);
						cascade(near, types, spread - 1);
					}
				}
			}, 4);
		}
    }
}

