package com.github.intangir.Tweaks;

import java.io.File;

import org.bukkit.DyeColor;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SheepRegrowWoolEvent;

public class Difficulty extends Tweak
{
	Difficulty(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "difficulty.yml");
		TWEAK_NAME = "Tweak_Difficulty";
		TWEAK_VERSION = "1.0";

		sheepWool = true;
		monsterXpMultiplier = 0.2f;
	}

	private boolean sheepWool;
	private float monsterXpMultiplier;
	
	@EventHandler(ignoreCancelled = true)
	public void onSheepRegrowWool(SheepRegrowWoolEvent e) {
		if(sheepWool) {
		    Sheep sheep = e.getEntity();
		    if(sheep.isSheared()) {
		        sheep.setColor(DyeColor.WHITE);
		    }
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent e) {
		if(monsterXpMultiplier == 1.0f) {
			e.setDroppedExp(Math.round(e.getDroppedExp() * monsterXpMultiplier));
		}
	}

}
