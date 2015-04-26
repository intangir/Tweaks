package com.github.intangir.Tweaks;

import java.io.File;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class Admin extends Tweak
{
	Admin(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "admin.yml");
		TWEAK_NAME = "Tweak_Admin";
		TWEAK_VERSION = "1.0";
		server.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
		worldPermission = "tweak.admin.world";
		setSpawnPermission = "tweak.admin.setspawn";
		wherePermission = "tweak.admin.where";
		changeServerPermission = "tweak.admin.changeserver";
		randomTeleportPermission = "tweak.admin.randomteleport";
	}
	
	private String worldPermission;
	private String setSpawnPermission;
	private String wherePermission;
	private String changeServerPermission;
	private String randomTeleportPermission;
	
	// teleports you to a world (tp command is a bit lacking)
	@CommandHandler("world")
	public void onCmdWorldTp(CommandSender sender, String[] args) {
		if(!sender.isOp() && !sender.hasPermission(worldPermission)) return;
		if(args.length == 1) {
			World w = server.getWorld(args[0]);
			if(w != null) {
				server.getPlayer(sender.getName()).teleport(w.getSpawnLocation());
			}
		}
	}

	// sets the worlds spawn
	@CommandHandler("setspawn")
	public void onCmdSetSpawn(CommandSender sender, String[] args) {
		if(!sender.isOp() && !sender.hasPermission(setSpawnPermission)) return;
		Player p = server.getPlayer(sender.getName());
		if(p != null) {
			World w = p.getWorld();
			Location l = p.getLocation();
			w.setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
		}
	}

	// tells you were the player is
	@CommandHandler("where")
	public void onCmdWhere(CommandSender sender, String[] args) {
		if(!sender.isOp() && !sender.hasPermission(wherePermission)) return;
		Player p = server.getPlayer(args[0]);
		if(p != null) {
			Location l = p.getLocation();
			sender.sendMessage(String.format("%s is at %s, %s %s %s", args[0], l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ()));
		}
	}

	// tells bungeecord proxy to move a player to another server
	@CommandHandler("changeserver")
	public void onCmdChangeServer(CommandSender sender, String[] args) {
		if(!sender.isOp() && !sender.hasPermission(changeServerPermission)) return;
		String name;
		String servername;
		Location tpLoc = null;
		if(args.length == 1) {
			name = sender.getName();
			servername = args[0];
		} else if(args.length == 2) {
			name = args[0];
			servername = args[1];
		} else if(args.length == 6) {
			name = args[0];
			servername = args[1];
			tpLoc = new Location(server.getWorld(args[2]), new Float(args[3]), new Float(args[4]), new Float(args[5]));
		} else {
			return;
		}
		Player p = server.getPlayer(name);
		if(p != null) {
			if(tpLoc != null) {
				tpLoc.setYaw(p.getLocation().getYaw());
				tpLoc.setPitch(p.getLocation().getPitch());
				p.teleport(tpLoc);
			}
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("ConnectOther");
	        out.writeUTF(name);
	        out.writeUTF(servername);
	        //sender.sendMessage("sending player " + name +  " to server " + servername);
	        p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
		}
	}

	// randomly teleports a player somewhere
	@SuppressWarnings("deprecation")
	@CommandHandler("randomteleport")
	public void onCmdRandomSpawn(CommandSender sender, String[] args) {
		if(!sender.isOp() && !sender.hasPermission(randomTeleportPermission)) return;
		Player p = server.getPlayer(args[0]);
		
		if(p != null)
		{
			Location l = Respawn.chooseSpawn_s("world");
			log.info("Randomly teleporting " + p.getName() + " to " + l);
			p.sendBlockChange(l, Material.BEACON, (byte) 0);
			p.teleport(l.add(0.5, 0.5, 0.5));
		}
	}

}
