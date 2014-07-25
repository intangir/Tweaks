package com.github.intangir.Tweaks;

import java.io.File;
import java.util.logging.Logger;

import lombok.Getter;

import net.cubespace.Yamler.Config.Config;

@Getter
public class MainConfig extends Config 
{
	transient Tweaks plugin;
	transient Logger log;
	
	private boolean vehicles;
	private boolean cauldrons;
	private boolean weather;
	private boolean time;

	MainConfig(Tweaks plugin) {
		this.plugin = plugin;
		this.log = plugin.getLog();
		
		CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
		
		vehicles = false;
		cauldrons = false;
		weather = false;
		time = false;
	}
	
	
}
