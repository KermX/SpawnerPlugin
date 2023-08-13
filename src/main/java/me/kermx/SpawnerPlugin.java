package me.kermx;

import com.palmergames.bukkit.towny.TownyAPI;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.java.JavaPlugin;


public final class SpawnerPlugin extends JavaPlugin implements Listener {

    @EventHandler
    //this part is done and shouldn't need to be changed at all
    public void onPlayerInteractSpawner(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        if (block.getType() == Material.SPAWNER) {
            CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
            if (creatureSpawner.getSpawnedType() != null) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                player.sendMessage(ChatColor.RED + "You can only change empty spawners!");
            }
        }
    }

    @EventHandler
    // I was having trouble getting the correct spawner type to drop or working out a system to set up the
    //item meta so the type can be set correctly on placement
    public void onPlayerBreakSpawner(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        if (block.getType() == Material.SPAWNER && itemStack.containsEnchantment(Enchantment.SILK_TOUCH) && !event.isCancelled()) {
            boolean inWilderness = TownyAPI.getInstance().isWilderness(player.getLocation());
            CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
            EntityType entityType = creatureSpawner.getSpawnedType();
            // player is in a town AND it is not an empty spawner
            // give player a spawner of the same type that they broke
            if (!inWilderness && entityType != null) {
                ItemStack spawnerSameType = new ItemStack(Material.SPAWNER);
                BlockStateMeta blockStateMeta = (BlockStateMeta) spawnerSameType.getItemMeta();
                CreatureSpawner creatureSpawnerDrop = (CreatureSpawner) blockStateMeta.getBlockState();
                creatureSpawnerDrop.setSpawnedType(creatureSpawner.getSpawnedType());
                blockStateMeta.setBlockState(creatureSpawnerDrop);
                spawnerSameType.setItemMeta(blockStateMeta);
                //add to player inv
                player.getInventory().addItem(spawnerSameType);
            }
            // player is in a town AND the spawner is empty
            // give player an empty spawner
            if (!inWilderness && entityType == null){
                ItemStack emptySpawner = new ItemStack(Material.SPAWNER);
                player.getInventory().addItem(emptySpawner);
            }
            // player is in wilderness AND is not sneaking
            // cancel event and tell the player to try again while crouching
            if (inWilderness && !player.isSneaking()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Crouch and break the spawner again to receive a spawner fragment!");
            }
            // player is in wilder AND is sneaking
            // drop itemsadder item
            if (inWilderness && player.isSneaking()){
                CustomStack stack = CustomStack.getInstance("crafting_ingredients:spawner_shard");
                if (stack != null){
                    ItemStack customItemStack = stack.getItemStack();
                    player.getInventory().addItem(customItemStack);
                }else{
                    player.sendMessage(ChatColor.DARK_RED + "Error, Contact Kerm!!!");
                }
            }
        }
    }
@Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this,this);
        Bukkit.getLogger().info("Silly lil spawner plugin started");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getLogger().info("Silly lil spawner plugin disabled");
    }
}
