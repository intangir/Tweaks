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
	private boolean books;
	private boolean cauldrons;
	private boolean chat;
	private boolean commands;
	private boolean difficulty;
	private boolean drops;
	private boolean mobs;
	private boolean pets;
	private boolean portals;
	private boolean protect;
	private boolean recipes;
	private boolean respawn;
	private boolean schedule;
	private boolean time;
	private boolean vehicles;
	private boolean weather;
	private boolean worlds;
	private boolean xpbottle;


	MainConfig(Tweaks plugin) {
		this.plugin = plugin;
		this.log = plugin.getLog();
		
		CONFIG_FILE = new File(plugin.getDataFolder(), "config.yml");
		
		admin = false;
		anvils = false;
		books = false;
		cauldrons = false;
		chat = false;
		commands = false;
		difficulty = false;
		drops = false;
		mobs = false;
		pets = false;
		portals = false;
		protect = false;
		recipes = false;
		respawn = false;
		schedule = false;
		time = false;
		vehicles = false;
		weather = false;
		worlds = false;
		xpbottle = false;
	}
}
