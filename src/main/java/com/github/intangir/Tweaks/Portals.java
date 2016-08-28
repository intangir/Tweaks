package com.github.intangir.Tweaks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

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
		
		adjustPortal = "0.0 2.0 0.0";
		adjustTeleport = "0.5 0.5 0.5";
		
		warpedDimensions = new HashMap<String, WarpedDimension>();
		WarpedDimension example2 = new WarpedDimension("second_world", "second_nether", 10);
		warpedDimensions.put("secondary nether", example2);
		
		fudgeRadius = 4;
		beamUpTime = 3;
		beamUpTo = "world 0 70 0";
		operator = "Scotty";
		seaLevel = 63;
		
		beamDowns = new BeamDowns(plugin);
		
		portalPermission = "tweak.portals.use";
		beamupPermission = "tweak.portals.beamup";
		
	}
	
	private int fudgeRadius;
	private int beamUpTime;
	private String beamUpTo;
	private Map<String, SpecialPortal> specialPortals;
	private Map<String, WarpedDimension> warpedDimensions;
	private transient BeamDowns beamDowns;
	private String beamupPermission;
	private String portalPermission;
	private String operator;
	private int seaLevel;
	private String adjustPortal;
	private String adjustTeleport;

	// this one is called after all of the worlds are loaded
	public void delayedEnable()
	{
		beamDowns.init();
		
		for(Map.Entry<String, SpecialPortal> p : specialPortals.entrySet()) {
			p.getValue().setFrom(parseLocation(p.getValue().getLoc()));
			p.getValue().setDest(parseLocation(p.getValue().getTo()));
		}
		
		Commands.unhideCommand("beamup");
	}
	
	public void disable() {
		super.disable();
		beamDowns.save();
	}
	
	public Location parseLocation(String loc) {
		if(loc != null) {
			String[] parts = loc.split(" ");
			if(parts.length == 4) {
				return new Location(server.getWorld(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
			}
		}
		return null;
	}

	public Vector parseVector(String vec) {
		if(vec != null) {
			String[] parts = vec.split(" ");
			if(parts.length == 3) {
				return new Vector(Float.parseFloat(parts[0]), Float.parseFloat(parts[1]), Float.parseFloat(parts[2]));
			}
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

	@Getter
	public class WarpedDimension extends Config {
		public WarpedDimension() {}
		public WarpedDimension(String world, String warped, Integer factor) {
			this.world = world;
			this.warped = warped;
			this.factor = factor;
		}
		
		private String world;
		private String warped;
		private Integer factor;
	}

	// create a generalized portal event class i can use to handle both players and entities
	@Getter
	@Setter
	public class GeneralPortalEvent {
		public GeneralPortalEvent(Location from, Player player) {
			this.from = from;
			this.player = player;
			to = null;
			cancelled = false;
			travelAgent = null;
		}
		private Location from;
		private Location to;
		private Boolean cancelled;
		private Player player;
		private Boolean travelAgent;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerPortal(PlayerPortalEvent e) {
		// make sure player has permission
		if(!e.getPlayer().hasPermission(portalPermission)) {
			log.info("Blocking " + e.getPlayer().getName() + " from portalling");
			e.setCancelled(true);
			return;
		}

		// transfer to a generalized object
		GeneralPortalEvent g = new GeneralPortalEvent(e.getFrom(), e.getPlayer()); 
		
		// handle it generally
		onWarpPortal(g);
		
		// transfer back
		if(g.getTo() != null)
			e.setTo(g.getTo());
		if(g.getTravelAgent() != null)
			e.useTravelAgent(g.getTravelAgent());
		if(g.getCancelled())
			e.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onEntityPortal(EntityPortalEvent e) {
		// transfer to a generalized object
		GeneralPortalEvent g = new GeneralPortalEvent(e.getFrom(), null);
		
		// handle it generally
		onWarpPortal(g);

		// transfer back
		if(g.getTo() != null)
			e.setTo(g.getTo());
		if(g.getTravelAgent() != null)
			e.useTravelAgent(g.getTravelAgent());
		if(g.getCancelled())
			e.setCancelled(true);

	}

	public void onWarpPortal(GeneralPortalEvent e) {

		// special portals for players only
		if(e.getPlayer() != null) {
			// special portals
			for(Entry<String, SpecialPortal> p : specialPortals.entrySet()) {
				Location l = e.getFrom();
				if(l.getWorld().equals(p.getValue().getFrom().getWorld()) && l.distance(p.getValue().getFrom()) < fudgeRadius) {

					log.info(e.getPlayer().getName() + " using gate " + p.getKey());

					// test for beam down (destination world only)
					if(server.getWorld(p.getValue().getTo()) != null) {

						// beam down to previous beamup location
						BlockVector beamDown = null;
						if(beamDowns.contains(e.getPlayer().getName())) {
							beamDown = parseLocation(beamDowns.get(e.getPlayer().getName())).toVector().toBlockVector();
						}
						// check if beamdown location is still valid
						if(beamDown != null) {
							l = Respawn.getValidY_s(server.getWorld(p.getValue().getTo()), beamDown.getBlockX(), beamDown.getBlockZ());
							if(l == null) {
								e.getPlayer().sendMessage(ChatColor.GREEN + "<from " + operator + "> Your previous location was longer reachable, you have been beamed down randomly!");
							}
						} 
						// beamdown to a new random location
						if(beamDown == null || l == null) {
							// random
							l = Respawn.chooseSpawn_s(p.getValue().getTo());
						}
						// teleport instead of portal if its in the same world
						if(e.getFrom().getWorld().equals(l.getWorld())) {
							e.getPlayer().teleport(l.add(parseVector(adjustTeleport)));
							debug("beamdown teleporting " + e.getPlayer().getName() + " to " + l.toString());
							e.setCancelled(true);
						// set the portal exit point for beamdown portal
						} else {
							e.setTo(l.add(parseVector(adjustPortal)));
							e.setTravelAgent(false);
							debug("beamdown portalling " + e.getPlayer().getName() + " to " + l.toString());
						}
						l.getWorld().playEffect(l, Effect.ENDER_SIGNAL, 0);
						return;
					}

					// portal is a special portal - translate to the new world/location
					l = l.subtract(p.getValue().getFrom());
					l.setWorld(p.getValue().getDest().getWorld());
					l = l.add(p.getValue().getDest());
					l = l.add(parseVector(adjustPortal));
					e.setTo(l);
					e.setTravelAgent(false);
					debug("special portalling " + e.getPlayer().getName() + " to " + l.toString());
					return;
				}
			}
		}

		// custom portal dimensions (link to and from a world besides world_nether)
		Location l = customPortalDestination(e.getFrom());
		if(l != null) {
			e.setTo(l);
			e.setTravelAgent(true);
		}
	}
	
	@EventHandler
	public void onPlayerFall(PlayerMoveEvent e) {
		// falling out of the world
		if(e.getTo().getY() < -20) {
			// falling out of custom portal dimension
			Location l = customPortalDestination(e.getTo());
			if(l != null) {
				e.getPlayer().teleport(l);
			}
		}
	}

	@EventHandler
	public void onVehicleFall(VehicleMoveEvent e) {
		// falling out of the world
		if(e.getTo().getY() < -20) {
			// falling out of custom portal dimension
			Location l = customPortalDestination(e.getTo());
			if(l != null) {
				e.getVehicle().teleport(l);
			}
		}
	}

	public Location customPortalDestination(Location l) {
		if(warpedDimensions.isEmpty()) {
			return null;
		}
		
		for(Entry<String, WarpedDimension> p : warpedDimensions.entrySet()) {
			if(l.getWorld().getName().equals(p.getValue().getWorld())) {
				l.setWorld(server.getWorld(p.getValue().getWarped()));
				l.setX(l.getX() / p.getValue().getFactor());
				l.setZ(l.getZ() / p.getValue().getFactor());
				return l;
			} else if(l.getWorld().getName().equals(p.getValue().getWarped())) {
				l.setWorld(server.getWorld(p.getValue().getWorld()));
				l.setX(l.getX() * p.getValue().getFactor());
				l.setZ(l.getZ() * p.getValue().getFactor());
				// handle fall out
				if(l.getY() < -20) {
					l.setY(300);
				}
				return l;
			}
		}
		return null;
	}
	
	// beams a player up
	@CommandHandler("beamup")
	public void onCmdBeamUp(CommandSender sender, String[] args) {
		if(!sender.hasPermission(beamupPermission)) return;
		final Player p = (Player) sender;
		final BlockVector ploc = p.getLocation().toVector().toBlockVector();

		// check the location
		if(!p.getWorld().getName().equals("world")) {
			p.sendMessage(ChatColor.GREEN + "<from " + operator + "> I can only beam you off the surface of the world");
		} else if(ploc.getBlockY() < seaLevel) {
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
							beamDowns.put(p.getName(), String.format("world %d %d %d", ploc.getBlockX(), ploc.getBlockY(), ploc.getBlockZ()));
							p.sendMessage(ChatColor.GREEN + "<from " + operator + "> Welcome back " + p.getName() + ", you can beam back down using that teleporter whenever you like.");
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
