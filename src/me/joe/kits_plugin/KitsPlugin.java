package me.joe.kits_plugin;

import me.joe.bundle_me.item_me.ItemMePlugin;
import me.joe.bundle_me.item_me.items.CustomItemManager;
import me.joe.kits_plugin.kit.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class KitsPlugin extends JavaPlugin {

    private CustomItemManager customItemManager;
    private KitManager manager;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.customItemManager = new CustomItemManager((ItemMePlugin) Bukkit.getPluginManager().getPlugin("Item_Me"));
        this.manager = new KitManager(this, this.customItemManager);

        this.getCommand("kits").setExecutor(new KitsCommand(this.manager, this.customItemManager));
    }
}
