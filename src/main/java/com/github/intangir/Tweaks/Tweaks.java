package com.github.intangir.Tweaks;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import lombok.Getter;

import net.cubespace.Yamler.Config.InvalidConfigurationException;

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

		tweaks = new ArrayList<Tweak>();

		mainConfig = new MainConfig(this);
        try {
        	mainConfig.init();
		} catch (InvalidConfigurationException e) {
			log.severe("Couldn't Load config.yml");
		}
        
        tweaks.add(new Fixes(this));
        if(mainConfig.isAdmin())
        	tweaks.add(new Admin(this));
        if(mainConfig.isAnvils())
        	tweaks.add(new Anvils(this));
        if(mainConfig.isBooks())
        	tweaks.add(new Books(this));
        if(mainConfig.isCauldrons())
        	tweaks.add(new Cauldrons(this));
        if(mainConfig.isChat())
        	tweaks.add(new Chat(this));
        if(mainConfig.isCommands())
        	tweaks.add(new Commands(this));
        if(mainConfig.isDifficulty())
        	tweaks.add(new Difficulty(this));
        if(mainConfig.isDisasters())
        	tweaks.add(new Disasters(this));
        if(mainConfig.isDreamers())
        	tweaks.add(new Dreamers(this));
        if(mainConfig.isDrops())
        	tweaks.add(new Drops(this));
        if(mainConfig.isEffects())
        	tweaks.add(new Effects(this));
        if(mainConfig.isFarming())
        	tweaks.add(new Farming(this));
        if(mainConfig.isFirstjoin())
        	tweaks.add(new Firstjoin(this));
        if(mainConfig.isMining())
        	tweaks.add(new Mining(this));
        if(mainConfig.isMobs())
        	tweaks.add(new Mobs(this));
        if(mainConfig.isPets())
        	tweaks.add(new Pets(this));
        if(mainConfig.isPhysics())
        	tweaks.add(new Physics(this));
        if(mainConfig.isPortals())
        	tweaks.add(new Portals(this));
        if(mainConfig.isProtect())
        	tweaks.add(new Protect(this));
        if(mainConfig.isRecipes())
        	tweaks.add(new Recipes(this));
        if(mainConfig.isRepairs())
        	tweaks.add(new Repairs(this));
        if(mainConfig.isRespawn())
        	tweaks.add(new Respawn(this));
        if(mainConfig.isSchedule())
        	tweaks.add(new Schedule(this));
        if(mainConfig.isTime())
        	tweaks.add(new Time(this));
        if(mainConfig.isTitles())
        	tweaks.add(new Titles(this));
        if(mainConfig.isVehicles())
        	tweaks.add(new Vehicles(this));
        if(mainConfig.isWeather())
        	tweaks.add(new Weather(this));
        if(mainConfig.isWorlds())
        	tweaks.add(new Worlds(this));
        if(mainConfig.isXpbottle())
        	tweaks.add(new XpBottle(this));
        
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

