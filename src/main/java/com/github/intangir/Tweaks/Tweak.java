package com.github.intangir.Tweaks;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class Tweak extends Config implements Listener, CommandExecutor 
{
	public transient Tweaks plugin;
	public transient Server server;
	public transient Logger log;
	public transient String TWEAK_NAME;
	public transient String TWEAK_VERSION;
	private transient Map<Command, Method> commandHandlers;

	public Tweak() {}
	public Tweak(Tweaks plugin) {
		this.plugin = plugin;
		server = plugin.getServer();
		log = plugin.getLog();
		commandHandlers = new HashMap<Command, Method>();
	}
	
	public void enable() {
		log.info("Enabling " + TWEAK_NAME + " v" + TWEAK_VERSION);
		if(CONFIG_FILE != null) {
			init();
		}
		server.getPluginManager().registerEvents(this, plugin);
		registerCommands();
		
	}

	public void disable() {
		log.info("Disabling " + TWEAK_NAME + " v" + TWEAK_VERSION);
	}
	
	@Override
	public void init() {
		try {
			super.init();
		} catch (InvalidConfigurationException e) {
			log.info("Couldn't Load " + CONFIG_FILE);
			e.printStackTrace();
		}
	}

	@Override
	public void save() {
		try {
			super.save();
		} catch (InvalidConfigurationException e) {
			log.info("Couldn't Save " + CONFIG_FILE);
			e.printStackTrace();
		}
	}
	
	// annotation to specify command handler methods
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface CommandHandler {
		String value();
	}
	
	// registers all of the annotated command handlers from its object methods
	public void registerCommands() {
		// search through all of the methods
		for(Method method : this.getClass().getMethods()){

			// for the right parameter types
			Class<?>[] params = method.getParameterTypes();
            if(params.length == 2 && CommandSender.class.isAssignableFrom(params[0]) && String[].class.equals(params[1])) {

            	// and annotation class
            	CommandHandler annotation = method.getAnnotation(CommandHandler.class);
            	if(annotation != null) {

            		// then get command object
            		PluginCommand command = getCommand(annotation.value());
            		
            		if(command != null)
            		{
	            		// and setup the executor and handler
	            		command.setExecutor(this);
	            		commandHandlers.put(command, method);
            		}
            	}
            }
		}
	}
	
	// gets a command or creates it via reflection if it isn't already mapped
	private PluginCommand getCommand(String name) {
		// check if the command already exists (from plugin.yml)
		PluginCommand command = plugin.getCommand(name);
		
		// create it if not using reflection
		if(command == null) {
			try {
				// override PluginCommands protected constructor
    			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
    			c.setAccessible(true);
    	 
    			// create a new PluginCommand object
    			command = c.newInstance(name, plugin);
    			
    			// override PluginManager's protected command map accessor
    			Field f = server.getPluginManager().getClass().getDeclaredField("commandMap");
    			f.setAccessible(true);
    			
    			// now get the commandmap object
    			CommandMap commandMap = (CommandMap) f.get(server.getPluginManager());
    			
    			// finally, register our command
    			commandMap.register(TWEAK_NAME, command);
    			
			} catch (Exception e) {
				log.severe("Error creating command " + name + " in " + this.TWEAK_NAME);
				return null;
			}
		}
		
		return command;
	}
                
	// this is technically the command handler but it delegates to its mapped handlers
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Method method = commandHandlers.get(command);
		if(method != null) {
			try {
				method.invoke(this, sender, args);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			sender.sendMessage(ChatColor.RED + "command not mapped" + command.getLabel() + " " + command.getName());
		}
		sender.sendMessage(ChatColor.RED + "An error occurred while trying to process the command");
		return true;
	}
}
