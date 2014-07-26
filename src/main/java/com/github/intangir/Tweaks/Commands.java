package com.github.intangir.Tweaks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

		hideMessage = "Unknown command. Type \"help\" for help.";
		unhiddenCommands = Arrays.asList("lag", "vote");
		hiddenCommands = Arrays.asList("plugins", "kill", "version");
		
		aliases = new TreeMap<String, String>();
		aliases.put("/rules", "/help rules");
		aliases.put("/snitch ", "/ps ");
	}

	private String hideMessage;
	private List<String> unhiddenCommands;
	private List<String> hiddenCommands;
	private TreeMap<String, String> aliases;
	private transient Pattern replPattern;
	
	public void enable()
	{
		super.enable();

		String pattern = join(new ArrayList<String>(aliases.descendingKeySet()), "|");
		log.info(pattern);
		replPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
	}
	
	public String substitute(String message) {
		
		Matcher m = replPattern.matcher(message);
		while(m.find()) {
			message = message.replaceAll(m.group(), aliases.get(m.group().toLowerCase()));
		}
		return message;
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPreprocessCommand(PlayerCommandPreprocessEvent e) {

		log.info("preprocess " + e.getMessage());
	    // replace aliases with the actual commands
	    String msg = substitute(e.getMessage());
	    e.setMessage(msg);

	    // ops can use any command
	    if(e.getPlayer().isOp()) {
	        return;
	    }

	    // get the command
	    String cmd = msg.substring(1).split(" ",2)[0];

		log.info("preprocess cmd " + cmd);

	    
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
	        log.info("plugin name " + pc.getPlugin().getName());
	    	String plug = pc.toString().split(" ")[1];
	    	log.info("plugin split " + plug);
	        if(server.getHelpMap().getIgnoredPlugins().contains(plug)) {
	            e.setCancelled(true);
	            e.getPlayer().sendMessage(hideMessage);
	        }
	    }
	}
}
