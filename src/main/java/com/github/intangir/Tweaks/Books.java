package com.github.intangir.Tweaks;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class Books extends Tweak
{
	Books(Tweaks plugin) {
		super(plugin);
		CONFIG_FILE = new File(plugin.getDataFolder(), "books.yml");
		TWEAK_NAME = "Tweak_Books";
		TWEAK_VERSION = "1.0";
		booksPermission = "tweak.books.give";
	}
	
	private String booksPermission;

	@CommandHandler("givebook")
	public void onGiveBook(CommandSender sender, String[] args) {
		if(!sender.isOp() && !sender.hasPermission(booksPermission)) return;
		
		if(args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Usage: /givebook <player> <book>");
			return;
		}

		Player p = server.getPlayer(args[0]);
		String bookName = args[1];

		if(p == null) {
			sender.sendMessage(ChatColor.RED + args[0] + " is not online");
			return;
		}
		
		// retreive book data
		String bookData; 
		try {
			byte[] contents;
			Path bookPath = Paths.get(plugin.getDataFolder() + "/books/" + bookName + ".txt");
			contents = Files.readAllBytes(bookPath);
			bookData = new String(contents);
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "Unable to read book " + plugin.getDataFolder() + "/books/" + bookName + ".txt");
			return;
		}
		
		// create book
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bm = (BookMeta) book.getItemMeta();

		String[] bookSections = bookData.split("\n", 3);
		
	    bm.setTitle(translateColors(bookSections[0].trim()));
	    bm.setAuthor(translateColors(bookSections[1].trim()));

	    String[] pages = bookSections[2].split("\n/np\n");

	    for(String page : pages) {
	    	bm.addPage(translateColors(page));
	    }
	    
	    book.setItemMeta(bm);
	    p.getInventory().addItem(book);
	}
	
	public String translateColors(String line) {
	    return ChatColor.translateAlternateColorCodes('&', line);
	}
}
