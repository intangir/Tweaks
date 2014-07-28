package com.github.intangir.Tweaks;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import net.cubespace.Yamler.Config.Config;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class Recipes extends Tweak
{
	public Recipes() {} 
	public Recipes(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "recipes.yml");
		TWEAK_NAME = "Tweak_Recipes";
		TWEAK_VERSION = "1.0";

		// default examples
		shapelessRecipes = Arrays.asList(
			new RecipeShapeless(
					new Item(Material.STRING, 2, 0),
					Arrays.asList(new Item(Material.WOOL, 1, 0))),
			new RecipeShapeless(
					new Item(Material.CLAY_BALL, 4, 0),
					Arrays.asList(new Item(Material.CLAY, 1, 0)))
		);
		
		RecipeShaped web = new RecipeShaped(new Item(Material.WEB));
		web.setShape(Arrays.asList("S S", " S ", "S S"));
		Map<String, Item> ingredients = new HashMap<String, Item>();
		ingredients.put("S", new Item(Material.STRING));
		web.setIngredients(ingredients);

		shapedRecipes = Arrays.asList(web);
	}
	
	private List<RecipeShapeless> shapelessRecipes;
	private List<RecipeShaped> shapedRecipes;
	
	public class Item extends Config {
		public Item() {
			count = 1;
			data = 0;
		}
		public Item(Material mat) {
			material = mat.toString();
			this.count = 1;
			this.data = 0;
		}
		public Item(Material mat, int count, int data) {
			material = mat.toString();
			this.count = count;
			this.data = (short) data;
		}
		public ItemStack getItemStack() {
			return new ItemStack(Material.getMaterial(material), count, data);
		}
		public Material getMaterial() {
			return Material.getMaterial(material);
		}
		public String material;
		public int count;
		public short data;
		
	}
	
	@Getter
	public class RecipeShapeless extends Config {
		public RecipeShapeless() {}
		public RecipeShapeless(Item result, List<Item> ingredients) {
			this.result = result;
			this.ingredients = ingredients;
		}
		private Item result;
		private List<Item> ingredients;
	}

	@Getter
	@Setter
	public class RecipeShaped extends Config {
		public RecipeShaped() {}
		public RecipeShaped(Item result) {
			this.result = result;
		}
		private Item result;
		private List<String> shape;
		private Map<String, Item> ingredients;
	}

	@SuppressWarnings("deprecation")
	public void enable()
	{
		super.enable();

		int added = 0;
		for(RecipeShaped recipe : shapedRecipes) {
			ShapedRecipe newrecipe = new ShapedRecipe(recipe.getResult().getItemStack());
			newrecipe.shape(recipe.getShape().toArray(new String[recipe.getShape().size()]));
			for(Map.Entry<String, Item> i : recipe.getIngredients().entrySet()) {
				newrecipe.setIngredient(i.getKey().charAt(0), i.getValue().getMaterial(), i.getValue().data);
			}
			server.addRecipe(newrecipe);
			added++;
		}
		for(RecipeShapeless recipe : shapelessRecipes) {
			ShapelessRecipe newrecipe = new ShapelessRecipe(recipe.getResult().getItemStack());
			for(Item i : recipe.getIngredients()) {
				newrecipe.addIngredient(i.count, i.getMaterial(), i.data);
			}
			server.addRecipe(newrecipe);
			added++;
		}
		log.info("Added " + added + " Recipes.");
	}
}
