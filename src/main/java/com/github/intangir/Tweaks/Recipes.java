package com.github.intangir.Tweaks;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class Recipes extends Tweak
{
	Recipes(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "recipes.yml");
		TWEAK_NAME = "Tweak_Recipes";
		TWEAK_VERSION = "1.0";

		// default examples
		recipes = new HashMap<String, List<String>>();
		recipes.put("STRING,2", Arrays.asList(
			"WOOL"));
		recipes.put("CLAY_BALL,4", Arrays.asList(
				"CLAY"));
		recipes.put("WEB", Arrays.asList(
				"S:STRING",
				"S S",
				" S ",
				"S S"));
	}
	
	private Map<String, List<String>> recipes;
	
	public ItemStack parseItemStack(String item) {
		String[] parts = item.split(",");
		return new ItemStack(
			Material.getMaterial(parts[0]),
			parts.length >= 2 ? Integer.parseInt(parts[1]) : 1,
			parts.length >= 3 ? Short.parseShort(parts[2]) : 0
		);
	}

	@SuppressWarnings("deprecation")
	public void enable()
	{
		super.enable();

		int added = 0;
		for(Map.Entry<String, List<String>> recipe : recipes.entrySet()) {
			
			if(recipe.getValue().get(0).contains("cook:")) {
				// cook means its a furnace recipe
				String[] parts = recipe.getValue().get(0).split(":");
				ItemStack item = parseItemStack(parts[1]);
				FurnaceRecipe newrecipe = new FurnaceRecipe(parseItemStack(recipe.getKey()), item.getType(), item.getDurability());
				server.addRecipe(newrecipe);
				added++;
			} else if(recipe.getValue().get(0).contains(":")) { 
				// otherwise a colon means its an ingredient for a shaped recipe
				ShapedRecipe newrecipe = new ShapedRecipe(parseItemStack(recipe.getKey()));
				List<String> shape = new ArrayList<String>();
				// setting the shape must be done before ingredients are set.. so this has to be done in two passes..
				for(String line : recipe.getValue())
					if(!line.contains(":"))
						shape.add(line);
				newrecipe.shape(shape.toArray(new String[shape.size()]));
				// 2nd pass, set ingredients
				for(String line : recipe.getValue()) {
					if(line.contains(":")) {
						String[] parts = line.split(":");
						ItemStack item = parseItemStack(parts[1]);
						newrecipe.setIngredient(parts[0].charAt(0), item.getType(), item.getDurability());
					}
				}
				server.addRecipe(newrecipe);
				added++;
			} else {
				// shapeless recipes are simple
				ShapelessRecipe newrecipe = new ShapelessRecipe(parseItemStack(recipe.getKey()));
				for(String line : recipe.getValue()) {
					ItemStack item = parseItemStack(line);
					newrecipe.addIngredient(item.getAmount(), item.getType(), item.getDurability());
				}
				server.addRecipe(newrecipe);
				added++;
			}
		}
		log.info("Added " + added + " Recipes.");
	}
}
