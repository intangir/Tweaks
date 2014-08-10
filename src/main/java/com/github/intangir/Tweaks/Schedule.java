package com.github.intangir.Tweaks;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Server;

import lombok.Getter;

import net.cubespace.Yamler.Config.Config;

public class Schedule extends Tweak
{
	public Schedule() {}
	public Schedule(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "schedule.yml");
		TWEAK_NAME = "Tweak_Schedule";
		TWEAK_VERSION = "1.0";
		
		schedule =
			Arrays.asList(
				new ScheduleCommand("say Saving in 10 seconds", 590, 1200),
				new ScheduleCommand("save-all", 600, 1200));
	}
	
	public List<ScheduleCommand> schedule;
	
	@Getter
	public class ScheduleCommand extends Config implements Runnable {
		public ScheduleCommand() {}
		public ScheduleCommand(String command, int after, int interval) {
			this.command = command;
			this.after = after;
			this.interval = interval;
		}
		
		private String command;
		private int after;
		private int interval;
		private transient Server server; 

		public void schedule(Server server, Tweaks plugin) {
			this.server = server;
			if(interval > 0) {
				server.getScheduler().scheduleSyncRepeatingTask(plugin, this, after * 20, interval * 20);
			} else {
				server.getScheduler().scheduleSyncDelayedTask(plugin, this, after * 20);    
			}
		}
		
		@Override
		public void run() {
			server.dispatchCommand(server.getConsoleSender(), command);
		}
	}
	
	public void delayedEnable()
	{
		for(ScheduleCommand command : schedule) {
			command.schedule(server, plugin);
		}
	}
}
