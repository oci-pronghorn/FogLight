package com.ociweb.device.testApps;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

public class PronghornMinecraftPlugin extends JavaPlugin{

	@Override
    public void onEnable() {
        getLogger().info("onEnable has been invoked!");
        
    }
    
    @Override
    public void onDisable() {
    	getLogger().info("onDisable has been invoked!");
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    	if (cmd.getName().equalsIgnoreCase("generatepi")) {
    		
    	    //Method not found so removed for now.
    	    //World world = sender.getWorld(); 
    		
    		Location loc = new Location(null, 0, 0, 0);
    		return true;
    	} else if (cmd.getName().equalsIgnoreCase("pluginLight")) {
    		
    		return true;
    	}
    	return false;
    }
    
    public void onPlayerJoin(PlayerJoinEvent evt) {
        Player player = evt.getPlayer(); // The player who joined
        PlayerInventory inventory = player.getInventory(); // The player's inventory
        ItemStack itemstack = new ItemStack(Material.STICK, 1); 
        inventory.addItem(itemstack);
    }
    
    private void generatePi(Location loc){
    	int x1 = loc.getBlockX(); 
        int y1 = loc.getBlockY();
        int z1 = loc.getBlockZ();

        // Figure out the opposite corner of the cube by taking the corner and adding length to all coordinates.
        int x2 = x1 + 200;
        int y2 = y1 + 100;
        int z2 = z1 + 5;

        World world = loc.getWorld();

        // Loop over the cube in the x dimension.
        for (int xPoint = x1; xPoint <= x2; xPoint++) { 
            // Loop over the cube in the y dimension.
            for (int yPoint = y1; yPoint <= y2; yPoint++) {
                // Loop over the cube in the z dimension.
                for (int zPoint = z1; zPoint <= z2; zPoint++) {
                    // Get the block that we are currently looping over.
                    Block currentBlock = world.getBlockAt(xPoint, yPoint, zPoint);
                    // Set the block to type 57 (Diamond block!)
                    currentBlock.setType(Material.WOOL);
                }
            }
        }
    }
}
