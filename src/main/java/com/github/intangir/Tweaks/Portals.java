package com.github.intangir.Tweaks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import net.cubespace.Yamler.Config.Config;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.util.BlockVector;

public class Portals extends Tweak
{
	public Portals() {}
	public Portals(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "portals.yml");
		TWEAK_NAME = "Tweak_Portals";
		TWEAK_VERSION = "1.0";
		
		specialPortals = new HashMap<String, SpecialPortal>();
		SpecialPortal example = new SpecialPortal("world 0 70 0", "world_example 10 100 10");
		specialPortals.put("example", example);
		
		fudgeRadius = 4;
		beamUpTime = 3;
		beamUpTo = "world 0 70 0";
		operator = "Scotty";
		
		beamDowns = new HashMap<String, BlockVector>();
		
	}
	
	private int fudgeRadius;
	private int beamUpTime;
	private String beamUpTo;
	private Map<String, SpecialPortal> specialPortals;
	private transient Map<String, BlockVector> beamDowns;
	private String operator;

	// this one is called after all of the worlds are loaded
	public void delayedEnable()
	{
		for(Map.Entry<String, SpecialPortal> p : specialPortals.entrySet()) {
			p.getValue().setFrom(parseLocation(p.getValue().getLoc()));
			p.getValue().setDest(parseLocation(p.getValue().getTo()));
		}
	}
	
	public Location parseLocation(String loc) {
		String[] parts = loc.split(" ");
		if(parts.length == 4) {
			return new Location(server.getWorld(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
		}
		return null;
	}
	
	@Getter
	public class SpecialPortal extends Config {
		public SpecialPortal() {}
		public SpecialPortal(String loc, String to) {
			this.loc = loc;
			this.to = to;
		}
		
		private String loc;
		private String to;

		@Setter
		private transient Location from;
		@Setter
		private transient Location dest;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onWarpPortal(PlayerPortalEvent e) {
		// special portals
		for(Map.Entry<String, SpecialPortal> p : specialPortals.entrySet()) {
			Location l = e.getFrom();
			if(l.getWorld().equals(p.getValue().getFrom().getWorld()) && l.distance(p.getValue().getFrom()) < fudgeRadius) {
				log.info(e.getPlayer().getName() + " using gate " + p.getKey());
				if(server.getWorld(p.getValue().getTo()) != null) {

					// test for beam down
					BlockVector beamDown = beamDowns.get(e.getPlayer().getName()); 
					if(beamDown != null) {
						l = Respawn.getValidY_s(server.getWorld(p.getValue().getTo()), beamDown.getBlockX(), beamDown.getBlockZ());
						if(l == null) {
							e.getPlayer().sendMessage(ChatColor.GREEN + "<from " + operator + "> Your previous location was longer reachable, you have been beamed down randomly!");
						}
					} 
					if(beamDown == null || l == null) {
						// random
						l = Respawn.chooseSpawn_s(p.getValue().getTo());
					}
					e.setTo(l.add(0.5, 0.5, 0.5));
					l.getWorld().playEffect(l, Effect.ENDER_SIGNAL, 0);
					return;
				}
				// translate to the new location
				l = l.subtract(p.getValue().getFrom());
				l.setWorld(p.getValue().getDest().getWorld());
				l = l.add(p.getValue().getDest());
				e.setTo(l);
				return;
			}
		}
	}
	
	// beams a player up
	@CommandHandler("beamup")
	public void onCmdBeamUp(CommandSender sender, String[] args) {
		final Player p = (Player) sender;
		final BlockVector ploc = p.getLocation().toVector().toBlockVector();

		// check the location
		if(!p.getWorld().getName().equals("world")) {
			p.sendMessage(ChatColor.GREEN + "<from " + operator + "> I can only beam you off the surface of planet");
		} else if(ploc.getBlockY() < 63) {
			p.sendMessage(ChatColor.GREEN + "<from " + operator + "> You need to be above sea level for me to lock onto you");
		} else {
			final Location SurfaceLoc = Respawn.getValidY_s(p.getWorld(), ploc.getBlockX(), ploc.getBlockZ());
			
			if(SurfaceLoc == null || SurfaceLoc.getBlockY() > p.getLocation().getBlockY()) {
				p.sendMessage(ChatColor.GREEN + "<from " + operator + "> I can't get a lock, make sure you are on the surface on solid ground");
			} else {
				p.sendMessage(ChatColor.GREEN + "<from " + operator + "> I'm attempting to lock onto you now, hold still...");
				sendPortal(SurfaceLoc.clone(), true);
				server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						sendPortal(SurfaceLoc.clone(), false);

						if(ploc.equals(p.getLocation().toVector().toBlockVector())) {
							p.getWorld().playEffect(SurfaceLoc.clone().add(0.5, 0.5, 0.5), Effect.ENDER_SIGNAL, 0);
							p.teleport(parseLocation(beamUpTo).clone().add(0.5, 0.5, 0.5));
							beamDowns.put(p.getName(), ploc);
							p.sendMessage(ChatColor.GREEN + "<from " + operator + "> Welcome back " + p.getName() + ", you can beam down using that teleporter anytime before a system restart.");
						} else {
							p.sendMessage(ChatColor.GREEN + "<from " + operator + "> I couldn't maintain a lock, you must've moved.");
						}
					}
				}, 20* beamUpTime);
			}
		}
	}
	
	// sends block changes for a portal to nearby players
	@SuppressWarnings("deprecation")
	public void sendPortal(Location portalLoc, boolean isOn) {

		Location portalLoc1 = portalLoc.clone().add(0, 1, 0);
		for(Player p : portalLoc.getWorld().getPlayers()) {
			if(p.getLocation().distanceSquared(portalLoc) < 6400) { // 80 block distance squared
				p.sendBlockChange(portalLoc,  isOn ? Material.PORTAL : Material.AIR, (byte) 0);
				p.sendBlockChange(portalLoc1, isOn ? Material.PORTAL : Material.AIR, (byte) 0);
			}
		}
	}
}
