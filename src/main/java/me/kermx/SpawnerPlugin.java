package me.kermx;

import com.palmergames.bukkit.towny.TownyAPI;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;


public final class SpawnerPlugin extends JavaPlugin implements Listener {

    @EventHandler
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
    public void onPlayerBreakSpawner(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        if (block.getType() == Material.SPAWNER && itemStack.containsEnchantment(Enchantment.SILK_TOUCH) && !event.isCancelled()) {
            boolean inWilderness = TownyAPI.getInstance().isWilderness(player.getLocation());
            CreatureSpawner creatureSpawner = (CreatureSpawner) block.getState();
            EntityType entityType = creatureSpawner.getSpawnedType();
            if (!inWilderness && entityType != null) {
                player.sendMessage("drop correct spawner type");
            }
            if (!inWilderness && entityType == null){
                player.sendMessage("drop empty spawner");
            } else {
                player.sendMessage("drop a spawner fragment");
            }
        }
    }

    @EventHandler
    public void onPlayerPlaceSpawner(BlockPlaceEvent event){
        if (event.getBlock().getType() == Material.SPAWNER){
            ItemStack itemStack = event.getItemInHand();
            ItemMeta itemMeta = itemStack.getItemMeta();
            Player player = event.getPlayer();
            PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
            if (persistentDataContainer.has(new NamespacedKey(this, "entityType"), PersistentDataType.STRING)){
                player.sendMessage("place correct spawner type? unsure if this is needed");
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
