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
	
	private boolean admin;
	private boolean anvils;
	private boolean cauldrons;
	private boolean time;
	private boolean vehicles;
	private boolean weather;

	MainConfig(Tweaks plugin) {
		this.plugin = plugin;
		this.log = plugin.getLog();
		
		CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
		
		admin = false;
		anvils = false;
		cauldrons = false;
		time = false;
		vehicles = false;
		weather = false;
	}
	
	
}
