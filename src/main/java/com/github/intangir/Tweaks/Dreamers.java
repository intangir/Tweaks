package com.github.intangir.Tweaks;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.cubespace.Yamler.Config.Comment;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class Dreamers extends Tweak
{
	Dreamers(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "dreamers.yml");
		TWEAK_NAME = "Tweak_Dreamers";
		TWEAK_VERSION = "1.0";
		
		dreamticks = 1200;
		dreamMessage = "You feel yourself settle into a deep theta brainwave state, you feel as if you could project into the /astral realms";
		cantDreamMessage = "You failed to project";
		cantWakeMessage = "You aren't asleep!";
		
		canDream = false;
		isDream = false;
		dreamServer = "astral";
		dreamerGroup = "Dreamer";
		
		sleepers = new HashMap<String, String>();
		deepsleepers = new HashSet<String>();
	}
	
	@Comment("how frequently it checks if someone is dreaming")
	private Integer dreamticks;
	
	@Comment("message sent to those who can enter dream state")
	private String dreamMessage;

	@Comment("message sent to those who cant enter dream state")
	private String cantDreamMessage;

	@Comment("message sent to those who cant wake up")
	private String cantWakeMessage;

	@Comment("if players can dream to another server from this server")
	private boolean canDream;
	
	@Comment("If this server IS a dream")
	private boolean isDream;
	
	@Comment("server players are sent to who go into deep sleep")
	private String dreamServer;

	@Comment("group of permissions giving to people when in a dream world they are allowed to edit")
	private String dreamerGroup;

	transient private Map<String, String> sleepers;
	transient private Set<String> deepsleepers;
	
	@Override
	public void enable()
	{
		super.enable();

		if(canDream) {
			debug("starting sleep check task");
			server.getScheduler().scheduleSyncRepeatingTask(plugin, new FindDreamers(), dreamticks, dreamticks);
		}

	}

	class FindDreamers implements Runnable {
		public void run() {
			debug("checking for sleepers");
			for(World w : plugin.getServer().getWorlds()) {
				for(Player p : w.getPlayers()) {
					if(p.hasMetadata("NPC")) {
						continue;
					}
					if(p.isSleeping() || p.isInsideVehicle()) {
						debug("found sleeper " + p.getName());
						if(sleepers.containsKey(p.getName()) && sleepers.get(p.getName()).equals(p.getLocation().toString())) {
							// set them as deep sleeping, and send dream invite message (and blank lines to avoid leave bed button)
							if(!deepsleepers.contains(p.getName())) {
								p.sendMessage(" ");
								p.sendMessage(dreamMessage);
								p.sendMessage(" ");
								p.sendMessage(" ");
								deepsleepers.add(p.getName());
							}
							debug("found deep sleeper " + p.getName());
						} else {
							// save their new location
							debug("resetting sleep status " + p.getName());
							sleepers.put(p.getName(), p.getLocation().toString());
							deepsleepers.remove(p.getName());
						}
					} else {
						debug("removing sleep status " + p.getName());
						sleepers.remove(p.getName());
						deepsleepers.remove(p.getName());
					}
				}
			}
		}
	}

	// tells bungeecord proxy to move a player to another server
	@CommandHandler("astral")
	public void onCmdAstral(CommandSender sender, String[] args) {
		if(sender instanceof Player &&
				deepsleepers.contains(sender.getName()) && 
				sleepers.containsKey(sender.getName()) &&
				sleepers.get(sender.getName()).equals(((Player) sender).getLocation().toString())) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("ConnectOther");
	        out.writeUTF(sender.getName());
	        out.writeUTF(dreamServer);
	        ((Player)sender).sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
		} else {
			sender.sendMessage(cantDreamMessage);
			sleepers.remove(sender.getName());
			deepsleepers.remove(sender.getName());
		}

	}
	
	@CommandHandler("wake")
	public void onCmdWake(CommandSender sender, String[] args) {
		if(sender instanceof Player && isDream) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("ConnectOther");
	        out.writeUTF(sender.getName());
	        out.writeUTF("last");
	        ((Player)sender).sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
		} else {
			sender.sendMessage(cantWakeMessage);
		}
	}
	
	@CommandHandler("allow")
	public void onCmdAllow(CommandSender sender, String[] args) {
		if(isDream && sender instanceof Player) {
			Player owner = (Player) sender;
			if(owner.isOp() || owner.getName().equals(owner.getWorld().getName())) {
				if(args.length == 1) {
					Player friend = server.getPlayer(args[0]);
					if(friend != null) {
						server.dispatchCommand(server.getConsoleSender(), "pex group " + dreamerGroup + " user add " + friend.getName() + " " + owner.getWorld().getName());
						owner.sendMessage(friend.getName() + " is now allowed to shape your dream world.");
					} else {
						owner.sendMessage(ChatColor.RED + args[0] + " is not in the astral realm.");
					}
				} else {
					owner.sendMessage(ChatColor.RED + "Usage: /allow <name>");
				}
			}
		}
	}

	@CommandHandler("disallow")
	public void onCmdDisallow(CommandSender sender, String[] args) {
		if(isDream && sender instanceof Player) {
			Player owner = (Player) sender;
			if(owner.isOp() || owner.getName().equals(owner.getWorld().getName())) {
				if(args.length == 1) {
					Player friend = server.getPlayer(args[0]);
					if(friend != null) {
						server.dispatchCommand(server.getConsoleSender(), "pex group " + dreamerGroup + " user remove " + friend.getName() + " " + owner.getWorld().getName());
						owner.sendMessage(friend.getName() + " is no longer allowed to shape your dream world.");
					} else {
						owner.sendMessage(ChatColor.RED + args[0] + " is not in the astral realm.");
					}
				} else {
					owner.sendMessage(ChatColor.RED + "Usage: /allow <name>");
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	void onEntityInteract(PlayerInteractEntityEvent event) {
		if (isDream && event.getRightClicked() instanceof Player) {
			final Player dreamer = (Player)event.getRightClicked();
			if (dreamer.hasMetadata("NPC")) {
				// see if they have a dream world
				final World dreamWorld = server.getWorld(dreamer.getName());
				if(dreamWorld != null) {
					final Player player = event.getPlayer();
					player.sendMessage("<" + dreamer.getName() + "> " + ChatColor.GRAY + "Take a look and you'll see...");
					server.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						public void run() {
							player.teleport(dreamWorld.getSpawnLocation());
							server.dispatchCommand(server.getConsoleSender(), "title " + player.getName() + " subtitle {text:\"In a world of pure imagination\"}");
							server.dispatchCommand(server.getConsoleSender(), "title " + player.getName() + " title {text:\"" + dreamer.getName() + "'s Dream\"}");
						}
					}, 20);
				}
			}
		}
	}
}
