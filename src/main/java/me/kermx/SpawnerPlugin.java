package me.kermx;

import com.palmergames.bukkit.towny.TownyAPI;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;


public final class SpawnerPlugin extends JavaPlugin implements Listener {
    //KEY
    NamespacedKey key = new NamespacedKey(this, "spawnerEntityType");

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

        if (block.getType() == Material.SPAWNER && event.getPlayer().getInventory().getItemInMainHand().toString().contains("_SPAWN_EGG") ) {
            CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
            if (creatureSpawner.getSpawnedType() != null) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                player.sendMessage(ChatColor.RED + "You can only change empty spawners!");
            }
        }
    }


    @EventHandler
    public void onPlayerBreakSpawner(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        if (block.getType() == Material.SPAWNER && itemStack.containsEnchantment(Enchantment.SILK_TOUCH) && !event.isCancelled() && player.hasPermission("spawnerplugin.use")) {

            //make sure player have inv space
            if (player.getInventory().firstEmpty() == -1) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "Empty a space in your inventory to break a spawner!");
            }
            if (player.getInventory().firstEmpty() != -1) {
                // player is in a town AND it is not an empty spawner
                // give player a spawner of the same type that they broke
                boolean inWilderness = TownyAPI.getInstance().isWilderness(player.getLocation());
                EntityType entityType = ((CreatureSpawner) block.getState()).getSpawnedType();

                if (!inWilderness && entityType != null) {

                    ItemStack spawnerSameType = new ItemStack(Material.SPAWNER);
                    ItemMeta itemMeta = spawnerSameType.getItemMeta();

                    itemMeta.setDisplayName(ChatColor.GOLD + entityType.name() + ChatColor.WHITE + " Spawner");
                    itemMeta.setLore(Arrays.asList("", ChatColor.GRAY + "Cannot be Changed With Spawn Egg"));
                    itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, entityType.name());

                    BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;
                    CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();
                    creatureSpawner.setSpawnedType(entityType);
                    blockStateMeta.setBlockState(creatureSpawner);

                    spawnerSameType.setItemMeta(itemMeta);

                    player.getInventory().addItem(spawnerSameType);
                    event.setExpToDrop(0);

                }
                // player is in a town AND the spawner is empty
                // give player an empty spawner
                if (!inWilderness && entityType == null) {
                    ItemStack emptySpawner = new ItemStack(Material.SPAWNER);
                    player.getInventory().addItem(emptySpawner);
                    event.setExpToDrop(0);
                }
                // player is in wilderness AND is not sneaking
                // cancel event and tell the player to try again while crouching
                if (inWilderness && !player.isSneaking()) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Crouch and break the spawner again to receive a spawner fragment!");
                }
                // player is in wildernesss AND is sneaking
                // drop itemsadder item
                if (inWilderness && player.isSneaking()) {
                    CustomStack stack = CustomStack.getInstance("crafting_ingredients:spawner_shard");
                    if (stack != null) {
                        ItemStack customItemStack = stack.getItemStack();
                        player.getInventory().addItem(customItemStack);
                    } else {
                        player.sendMessage(ChatColor.DARK_RED + "Error, Make a Ticket!!!");
                        player.sendMessage(ChatColor.DARK_RED + "Error Code 1");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerPlaceSpawner(BlockPlaceEvent event){
        Block block = event.getBlock();
        if (block.getType() == Material.SPAWNER && event.getItemInHand().getItemMeta() != null){
            PersistentDataContainer placedSpawnerSameType = event.getItemInHand().getItemMeta().getPersistentDataContainer();
            if (placedSpawnerSameType.has(key, PersistentDataType.STRING)){
                String persistentSpawnedType = placedSpawnerSameType.get(key, PersistentDataType.STRING);

                //change spawner type and update the spawner
                CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
                creatureSpawner.setSpawnedType(EntityType.valueOf(persistentSpawnedType));
                creatureSpawner.update();
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
