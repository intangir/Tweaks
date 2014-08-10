package com.github.intangir.Tweaks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.PluginCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;


public class Commands extends Tweak
{
	Commands(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "commands.yml");
		TWEAK_NAME = "Tweak_Commands";
		TWEAK_VERSION = "1.0";
		
		instance = this;

		hideMessage = "Unknown command. Type \"help\" for help.";
		unhiddenCommands = new ArrayList<String>(Arrays.asList("lag", "vote"));
		hiddenCommands = Arrays.asList("plugins", "kill", "version", "me");
		
		aliases = new TreeMap<String, String>();
		aliases.put("/rules", "/help rules");
		aliases.put("/snitch ", "/ps ");
		
		replPattern = null;
	}

	private String hideMessage;
	private List<String> unhiddenCommands;
	private List<String> hiddenCommands;
	private TreeMap<String, String> aliases;
	private transient Pattern replPattern;
	private static Commands instance = null;
	
	public String substitute(String message) {
		
		if(replPattern == null) {
			String pattern = join(new ArrayList<String>(aliases.descendingKeySet()), "|");
			replPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		}
		
		Matcher m = replPattern.matcher(message);
		while(m.find()) {
			message = message.replaceAll(m.group(), aliases.get(m.group().toLowerCase()));
		}
		return message;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPreprocessCommand(PlayerCommandPreprocessEvent e) {

	    // replace aliases with the actual commands
	    String msg = substitute(e.getMessage());
	    e.setMessage(msg);

	    // ops can use any command
	    if(e.getPlayer().isOp()) {
	        return;
	    }

	    // get the command
	    String cmd = msg.substring(1).split(" ",2)[0];

	    // check if its specifically allowed (regardless of being in a hidden plugin)
	    if(unhiddenCommands.contains(cmd.toLowerCase())) {
	        return;
	    }

	    // check if it is specifically hidden
	    if(hiddenCommands.contains(cmd.toLowerCase())) {
	        e.setCancelled(true);
	        e.getPlayer().sendMessage(hideMessage);
	        return;
	    }

	    // check if its in an hidden plugin
	    PluginCommand pc = server.getPluginCommand(cmd);
	    if(pc != null) {
	    	String plug = pc.getPlugin().getName();
	        if(server.getHelpMap().getIgnoredPlugins().contains(plug)) {
	            e.setCancelled(true);
	            e.getPlayer().sendMessage(hideMessage);
	        }
	    }
	}

	public static void unhideCommands(List<String> commands) {
		if(instance != null) {
			for(String command : commands) {
				instance.unhiddenCommands.add(command);
			}
		}
	}

	public static void unhideCommand(String command) {
		if(instance != null) {
			instance.unhiddenCommands.add(command);
		}
	}

	public static void addAlias(String alias, String cmd) {
		if(instance != null) {
			instance.aliases.put(alias, cmd);
			instance.replPattern = null;
		}
	}
}
