package com.github.intangir.Tweaks;

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
		TWEAK_NAME = "Tweak_Admin";
		TWEAK_VERSION = "1.0";
		server.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
	}
	
	// teleports you to a world (tp command is a bit lacking)
	@CommandHandler("world")
	public void onCmdWorldTp(CommandSender sender, String[] args) {
		if(!sender.isOp()) return;
		World w = server.getWorld(args[0]);
		if(w != null) {
			server.getPlayer(sender.getName()).teleport(w.getSpawnLocation());
		}
	}

	// sets the worlds spawn
	@CommandHandler("setspawn")
	public void onCmdSetSpawn(CommandSender sender, String[] args) {
		if(!sender.isOp()) return;
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
		if(!sender.isOp()) return;
		Player p = server.getPlayer(args[0]);
		if(p != null) {
			Location l = p.getLocation();
			sender.sendMessage(String.format("%s is at %s, %s %s %s", args[0], l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ()));
		}
	}

	// tells bungeecord proxy to move a player to another server
	@CommandHandler("changeserver")
	public void onCmdChangeServer(CommandSender sender, String[] args) {
		if(!sender.isOp()) return;
		Player p = server.getPlayer(args[0]);
		if(p != null) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("ConnectOther");
	        out.writeUTF(args[0]);
	        out.writeUTF(args[1]);
	        sender.sendMessage("sending player " + args[0] +  " to server " + args[1]);
	        p.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
		}
	}

	// randomly teleports a player somewhere
	@SuppressWarnings("deprecation")
	@CommandHandler("randomteleport")
	public void onCmdRandomSpawn(CommandSender sender, String[] args) {
		if(!sender.isOp()) return;
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
