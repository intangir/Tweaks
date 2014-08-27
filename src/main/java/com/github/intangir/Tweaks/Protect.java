package com.github.intangir.Tweaks;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Comments;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Protect extends Tweak
{
	Protect(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "protect.yml");
		TWEAK_NAME = "Tweak_Protect";
		TWEAK_VERSION = "1.0";
		
		fireProtectAboveY = 0;
		creeperProtectAboveY = 62;
		enderManProtect = true;
		enderCrystalProtect = false;
		dropsProtect = true;
		
		blockBreakPermission = null;
		blockPlacePermission = null;
		entityDamagePermission = null;
		interactPermission = null;
		
		disableCreatureSpawn = false;
		
		protectInteractions = new HashSet<String>();
		protectInteractions.add(Material.LEVER.toString());
		protectInteractions.add(Material.COMMAND.toString());
		protectInteractions.add(Material.BEACON.toString());
		protectInteractions.add(EntityType.ITEM_FRAME.toString());
		
		protectExplosions = new HashSet<String>();
		protectExplosions.add(Material.BEDROCK.toString());
		
		
	}
	
	public void enable()
	{
		super.enable();
		
		if(dropsProtect) {
			excludeDropped = new HashSet<UUID>();

			// nms reflection stuff for flagging invulnerable drops
			try {
				// get the field for flagging an entity invulnerable in the NMS class, and set it to accessible for later
				isInvulnerable = getVersionedClass("net.minecraft.server.Entity").getDeclaredField("invulnerable");
				isInvulnerable.setAccessible(true);

				// get the method for getting underlying NMS entity object handle from a craftbukkit entity object
				getEntityHandle = getVersionedClass("org.bukkit.craftbukkit.entity.CraftEntity").getMethod("getHandle");

			} catch(Exception ex) {
				log.severe("Failed to access dropsProtect methods");
				ex.printStackTrace();
				dropsProtect = false;
			}
		}
	}
	
	private transient Method getEntityHandle;
	private transient Field isInvulnerable;
	
	@Comment("stops fire from spreading and consuming blocks over Y")
	private int fireProtectAboveY;
	
	@Comment("stops creepers from blowing up blocks when detonating over Y")
	private int creeperProtectAboveY;
	
	@Comment("stop endermen from stealing blocks")
	private boolean enderManProtect;
	
	@Comment("protect dropped items on death or explosions from being destroyed by fire, lava, or explosion")
	private boolean dropsProtect;
	
	
	@Comment("stop endercrystals from breaking, useful for decoration, ruins dragon encounter")
	private boolean enderCrystalProtect;

	private String blockBreakPermission;
	private String blockPlacePermission;
	private String entityDamagePermission;
	private String interactPermission;
	
	@Comments({
		"The following permission settings are available",
		"blockBreakPermission",
		"blockPlacePermission",
		"entityDamagePermission",
		"interactPermission",
		"when set they will be restricted only to those with that permission"
	})
	private Set<String> protectInteractions;
	private Set<String> protectExplosions;
	private boolean disableCreatureSpawn;

	private transient Set<UUID> excludeDropped;
	
	// prevent fires from spreading over Y
	@EventHandler(ignoreCancelled = true)
	public void onFireSpread(BlockIgniteEvent e) {
		if(e.getCause() != IgniteCause.FLINT_AND_STEEL && e.getBlock().getY() > fireProtectAboveY)
			e.setCancelled(true);
	}

	// prevent fires from burning blocks over Y
	@EventHandler(ignoreCancelled = true)
	public void onFireSpread(BlockBurnEvent e) {
		if(e.getBlock().getY() > fireProtectAboveY)
			e.setCancelled(true);
	}
	
	// prevent endermen from stealing blocks
	@EventHandler(ignoreCancelled = true)
	public void onEnderGrief(EntityChangeBlockEvent e) {
		if(enderManProtect && e.getEntityType() == EntityType.ENDERMAN) {
			e.setCancelled(true);
		}
	}

	// prevent explosions over Y, and prevent them from exploding protected blocks
	@EventHandler(ignoreCancelled = true)
	public void onExplosion(EntityExplodeEvent e) {
		if(e.getEntity() != null && e.getEntityType() == EntityType.CREEPER && e.getLocation().getBlockY() > creeperProtectAboveY) {
			e.blockList().clear();

		} else if(protectExplosions.size() > 0) {
			List<Block> remove = new ArrayList<Block>();
			for(Block b : e.blockList()) {
				if(protectExplosions.contains(b.getType().toString())) {
					remove.add(b);
				}
			}
			e.blockList().removeAll(remove);
		}
	}
	
	// exclude intentionally dropped items from drops Protection
	@EventHandler(ignoreCancelled = true)
	public void onItemDrop(PlayerDropItemEvent e) {
		if(dropsProtect) {
			excludeDropped.add(e.getItemDrop().getUniqueId());
			debug("excluding " + e.getItemDrop().getUniqueId());
		}
	}

	// protect dropped items from being burned blown up or melted
	@EventHandler(ignoreCancelled = true)
	public void onItemSpawn(ItemSpawnEvent e) {
		if(dropsProtect) {
			if(excludeDropped.contains(e.getEntity().getUniqueId())) {
				excludeDropped.remove(e.getEntity().getUniqueId());
			} else {
				debug("protecting " + e.getEntity().getItemStack().getType() + " " + e.getEntity().getUniqueId());
				try {
					isInvulnerable.set(getEntityHandle.invoke(e.getEntity()), true);
				} catch (Exception ex) {
					log.severe("failed to protect spawned item");
					ex.printStackTrace();
				}
			}
		}
	}

	// prevent endercrystals from being destroyed (for decorative ones, defaults off)
	@EventHandler(ignoreCancelled = true)
	public void onCrystalDamage(EntityDamageEvent e) {
		if(enderCrystalProtect && e.getEntityType() == EntityType.ENDER_CRYSTAL)
			e.setCancelled(true);
	}

	// exclude anyone from placing if the permission is set and they don't have it
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		if(blockPlacePermission != null && !e.getPlayer().hasPermission(blockPlacePermission))
			e.setCancelled(true);
	}

	// exclude anyone from breaking if the permission is set and they don't have it
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		if(blockBreakPermission != null && !e.getPlayer().hasPermission(blockBreakPermission))
			e.setCancelled(true);
	}

	// exclude anyone from breaking frames if the permission is set and they don't have it
	@EventHandler(ignoreCancelled = true)
	public void onFrameBreak(HangingBreakByEntityEvent e) {
		if(blockBreakPermission != null && e.getCause() == RemoveCause.ENTITY &&
			e.getRemover() instanceof Player && !((Player)e.getRemover()).hasPermission(blockBreakPermission)) {

			e.setCancelled(true);
		}
	}

	// exclude anyone from harming entities if the permission is set and they don't have it
	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageByEntityEvent e) {
		if(entityDamagePermission != null && 
			e.getDamager() != null &&
			e.getDamager() instanceof Player &&
			!((Player)e.getDamager()).hasPermission(entityDamagePermission)) {
			
			e.setCancelled(true);
		}
	}
	
	// disable interactions with listed blocks if the permission is set and they don't have it
	@EventHandler(ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent e) {
		if(interactPermission != null &&
			e.getAction() == Action.RIGHT_CLICK_BLOCK &&
			protectInteractions.contains(e.getClickedBlock().getType().toString()) &&
			!e.getPlayer().hasPermission(interactPermission)) {
			e.setCancelled(true);
		}
	}

	// disable interactions with listed entities if the permission is set and they don't have it 
	@EventHandler(ignoreCancelled = true)
	public void onInteractEntity(PlayerInteractEntityEvent e) {
		if(interactPermission != null &&
			protectInteractions.contains(e.getRightClicked().getType().toString()) &&
			!e.getPlayer().hasPermission(interactPermission)) {
			e.setCancelled(true);
		}
	}

	// disable natural creature spawn (defaults off)
	@EventHandler(ignoreCancelled = true)
	public void onSpawn(CreatureSpawnEvent e) {
		if(disableCreatureSpawn && e.getSpawnReason() == SpawnReason.DEFAULT) {
			e.setCancelled(true);
		}
	}
}
