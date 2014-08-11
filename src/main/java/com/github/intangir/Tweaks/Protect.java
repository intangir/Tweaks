package com.github.intangir.Tweaks;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import net.cubespace.Yamler.Config.Comment;
import net.cubespace.Yamler.Config.Comments;

import org.bukkit.Material;
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
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
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
	}
	
	@Comment("stops fire from spreading and consuming blocks over Y")
	private int fireProtectAboveY;
	
	@Comment("stops creepers from blowing up blocks when detonating over Y")
	private int creeperProtectAboveY;
	
	@Comment("stop endermen from stealing blocks")
	private boolean enderManProtect;
	
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
	
	private boolean disableCreatureSpawn;
	
	@EventHandler(ignoreCancelled = true)
	public void onFireSpread(BlockIgniteEvent e) {
		if(e.getCause() != IgniteCause.FLINT_AND_STEEL && e.getBlock().getY() > fireProtectAboveY)
			e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onFireSpread(BlockBurnEvent e) {
		if(e.getBlock().getY() > fireProtectAboveY)
			e.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEnderGrief(EntityChangeBlockEvent e) {
		if(enderManProtect && e.getEntityType() == EntityType.ENDERMAN) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onCreeperExplode(EntityExplodeEvent e) {
		if(e.getEntityType() == EntityType.CREEPER && e.getLocation().getBlockY() > creeperProtectAboveY)
			e.blockList().clear();
	}

	@EventHandler(ignoreCancelled = true)
	public void onCrystalDamage(EntityDamageEvent e) {
		if(enderCrystalProtect && e.getEntityType() == EntityType.ENDER_CRYSTAL)
			e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent e) {
		if(blockPlacePermission != null && !e.getPlayer().hasPermission(blockPlacePermission))
			e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent e) {
		if(blockBreakPermission != null && !e.getPlayer().hasPermission(blockBreakPermission))
			e.setCancelled(true);
	}

	@EventHandler(ignoreCancelled = true)
	public void onFrameBreak(HangingBreakByEntityEvent e) {
		if(blockBreakPermission != null && e.getCause() == RemoveCause.ENTITY &&
			e.getRemover() instanceof Player && !((Player)e.getRemover()).hasPermission(blockBreakPermission)) {

			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageByEntityEvent e) {
		if(entityDamagePermission != null && 
			e.getDamager() != null &&
			e.getDamager() instanceof Player &&
			!((Player)e.getDamager()).hasPermission(entityDamagePermission)) {
			
			e.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent e) {
		if(interactPermission != null &&
			e.getAction() == Action.RIGHT_CLICK_BLOCK &&
			protectInteractions.contains(e.getClickedBlock().getType().toString()) &&
			!e.getPlayer().hasPermission(interactPermission)) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInteractEntity(PlayerInteractEntityEvent e) {
		if(interactPermission != null &&
			protectInteractions.contains(e.getRightClicked().getType().toString()) &&
			!e.getPlayer().hasPermission(interactPermission)) {
			e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onSpawn(CreatureSpawnEvent e) {
		if(disableCreatureSpawn && e.getSpawnReason() == SpawnReason.DEFAULT) {
			e.setCancelled(true);
		}
	}
}
