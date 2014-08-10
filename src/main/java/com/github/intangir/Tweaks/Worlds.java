package com.github.intangir.Tweaks;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.Difficulty;

import lombok.Getter;
import lombok.Setter;

import net.cubespace.Yamler.Config.Config;

public class Worlds extends Tweak
{
	public Worlds() {}
	public Worlds(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "worlds.yml");
		TWEAK_NAME = "Tweak_Worlds";
		TWEAK_VERSION = "1.0";
		
		WorldSettings example = new WorldSettings();
		example.setName("world_example");
		example.setStructs(false);
		example.setType(WorldType.LARGE_BIOMES.toString());
		example.setSeed(12345);
		example.setEnv(Environment.NORMAL.toString());
		example.setPvp(true);
		example.setMonsters(true);
		example.setAnimals(true);
		example.setPreload(false);
		example.setDiff(Difficulty.HARD.toString());
		worlds = Arrays.asList(example);

	}

	public List<WorldSettings> worlds;

	@Getter
	@Setter
	public class WorldSettings extends Config {
		public WorldSettings() {
			seed = 0;
			gen = null;
		}
		private String name;

		private boolean structs;
		private String type;
		private int seed;
		private String env;
		private String gen;

		private boolean pvp;
		private boolean monsters;
		private boolean animals;
		private boolean preload;
		private String diff;
	}
	
	public void enable()
	{
		super.enable();

		for(WorldSettings set : worlds) {
			World w = server.getWorld(set.getName());
			if(w == null) {
				log.info("Creating/Loading " + set.getName());
				WorldCreator wc = new WorldCreator(set.getName());
				wc.generateStructures(set.isStructs());
				wc.type(WorldType.valueOf(set.getType()));
				wc.seed(set.getSeed());
				wc.environment(Environment.valueOf(set.getEnv()));
				if(set.getGen() != null && !set.getGen().isEmpty())
					wc.generator(set.getGen());
				w = server.createWorld(wc);
			}
			
			log.info("Settings for " + set.getName());
			w.setDifficulty(Difficulty.valueOf(set.getDiff()));
			w.setPVP(set.isPvp());
			w.setSpawnFlags(set.isMonsters(), set.isAnimals());
			w.setKeepSpawnInMemory(set.isPreload());
		}
	}
}
