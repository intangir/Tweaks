package com.github.intangir.Tweaks;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.cubespace.Yamler.Config.Config;
import net.cubespace.Yamler.Config.InvalidConfigurationException;

public class BeamDowns extends Config 
{
	public BeamDowns() {}
	public BeamDowns(Tweaks plugin) {
		this.plugin = plugin;
		this.log = plugin.getLog();
		
		CONFIG_FILE = new File(plugin.getDataFolder(), "beamdowns.yml");
		
		beamDowns = new HashMap<String, String>();
		
	}

	transient Tweaks plugin;
	transient Logger log;
	
	private Map<String, String> beamDowns;
	
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

	public boolean contains(String name) {
		return beamDowns.containsKey(name);
	}
	
	public void put(String name, String loc) {
		beamDowns.put(name, loc);
		save();
	}
	
	public String get(String name) {
		return beamDowns.get(name);
	}
}
