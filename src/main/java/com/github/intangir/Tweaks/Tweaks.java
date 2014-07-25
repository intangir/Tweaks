package com.github.intangir.Tweaks;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import lombok.Getter;

import net.cubespace.Yamler.Config.InvalidConfigurationException;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class Tweaks extends JavaPlugin implements Listener
{
    private Logger log;
    private PluginDescriptionFile pdfFile;
    private MainConfig mainConfig;
    List<Tweak> tweaks;
    
	public void onEnable()
	{
		log = getLogger();
		pdfFile = getDescription();

		//Bukkit.getPluginManager().registerEvents(this, this);
		
		tweaks = new ArrayList<Tweak>();

		mainConfig = new MainConfig(this);
        try {
        	mainConfig.init();
		} catch (InvalidConfigurationException e) {
			log.severe("Couldn't Load config.yml");
		}
        
        if(mainConfig.isVehicles())
        	tweaks.add(new Vehicles(this));
        if(mainConfig.isWeather())
        	tweaks.add(new Weather(this));
        if(mainConfig.isTime())
        	tweaks.add(new Time(this));
        if(mainConfig.isCauldrons())
        	tweaks.add(new Cauldrons(this));
        
        for(Tweak tweak : tweaks)
        	tweak.enable();
		
		log.info("v" + pdfFile.getVersion() + " enabled!");
	}
	
	public void onDisable()
	{
        for(Tweak tweak : tweaks)
        	tweak.disable();

        log.info("v" + pdfFile.getVersion() + " disabled.");
	}
}

