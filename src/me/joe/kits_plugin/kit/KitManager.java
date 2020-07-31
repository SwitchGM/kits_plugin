package me.joe.kits_plugin.kit;

import me.joe.bundle_me.item_me.items.CustomItemManager;
import me.joe.kits_plugin.KitsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class KitManager {

    private KitsPlugin plugin;
    private ConfigurationSection config;
    private CustomItemManager customItemManager;

    private HashMap<String, Kit> kits = new HashMap<>();
    private HashMap<String, HashMap<Player, Long>> cooldowns = new HashMap<>();

    public KitManager(KitsPlugin plugin, CustomItemManager customItemManager) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.customItemManager = customItemManager;

        this.kits = this.loadKits();
        this.cooldowns = this.loadCooldowns();
    }

    public HashMap<String, Kit> loadKits() {
        ConfigurationSection rawKits = this.config.getConfigurationSection("kits");

        if (rawKits == null) {
            return new HashMap<>();
        }

        HashMap<String, Kit> kits = new HashMap<>();
        for (String id : rawKits.getKeys(false)) {
            kits.put(id, this.loadKit(rawKits.getConfigurationSection(id)));
        }

        return kits;
    }

    public Kit loadKit(ConfigurationSection rawKit) {
        String name = rawKit.getString("name");
        String icon = rawKit.getString("icon");
        List<String> items = rawKit.getStringList("items");
        int cooldown = rawKit.getInt("cooldown");

        return new Kit(name, icon, items, cooldown);
    }

    public void giveKit(String id, Player player) {
        Kit kit = this.getKit(id);

        if (this.getPlayerKitCooldown(player, id) >= System.currentTimeMillis() || !player.hasPermission("kits.bypass")) {
            int remainingTime = (int) (this.getPlayerKitCooldown(player, id) - System.currentTimeMillis());
            player.sendMessage(
                    ChatColor.translateAlternateColorCodes('&',
                            "&cYou must wait &f" + (remainingTime/1000) + "seconds &c before claiming this kit again!")
            );
            return;
        }

        List<ItemStack> items = this.getKitItems(kit);

        boolean dropped = false;

        for (ItemStack item : items) {
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                dropped = true;
            }

            player.getInventory().addItem(item);

        }

        if (dropped) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&aSome of your kit was dropped because your inventory was full!")
            );
        }

        this.setCooldown(player, id);
    }

    public List<ItemStack> getKitItems(Kit kit) {
        ArrayList<ItemStack> items = new ArrayList<>();
        for (String rawitem : kit.getItems()) {
            List<String> itemInfo = Arrays.asList(rawitem.split(","));
            String item = itemInfo.get(0);
            int amount = Integer.parseInt(itemInfo.get(1));

            ItemStack kitItem;

            if (this.customItemManager.getCustomItem(item) != null) {
                kitItem = this.customItemManager.getCustomItem(item).getAmountOfItem(amount);
            } else {
                kitItem = new ItemStack(Material.valueOf(item), amount);
            }

            items.add(kitItem);
        }

        return items;
    }

    public boolean saveKit(String id, String icon, String name, Inventory inventory, int cooldown) {
        List<String> items = new ArrayList<>();
        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                continue;
            }
            items.add(item.getType().toString());
        }

        Kit kit = new Kit(name, icon, items, cooldown);
        this.kits.put(id, kit);
        return this.saveKitToConfig(id, kit);
    }

    public boolean saveKitToConfig(String id, Kit kit) {
        ConfigurationSection kits = this.config.getConfigurationSection("kits");
        if (kits.getKeys(false).contains(id)) {
            // TODO - already exists
            return false;
        }

        this.config.set(id + ".name", kit.getName());
        this.config.set(id + ".icon", kit.getIcon());
        this.config.set(id + ".items", kit.getItems());
        this.config.set(id + ".cooldown", kit.getCooldown());

        this.plugin.saveConfig();
        return true;
    }

    private HashMap<String, HashMap<Player, Long>> loadCooldowns() {
        ConfigurationSection rawCooldowns = this.config.getConfigurationSection("cooldowns");

        if (rawCooldowns == null) {
            return new HashMap<>();
        }

        HashMap<String, HashMap<Player, Long>> cooldowns = new HashMap<>();
        for (String kit : rawCooldowns.getKeys(false)) {
            HashMap<Player, Long> kitCooldown = new HashMap<>();
            for (String playerUUID : rawCooldowns.getConfigurationSection(kit).getKeys(false)) {
                Player player = Bukkit.getPlayer(playerUUID);
                Long cooldown = rawCooldowns.getLong(playerUUID);

                kitCooldown.put(player, cooldown);
            }
            cooldowns.put(kit, kitCooldown);
        }

        return cooldowns;
    }

    public long getPlayerKitCooldown(Player player, String kit) {
        if (!this.cooldowns.containsKey(kit)) {
            return 0;
        }

        HashMap<Player, Long> kitCooldown = this.cooldowns.get(kit);

        if (!kitCooldown.containsKey(player)) {
            return 0;
        }

        System.out.println(kitCooldown.get(player));
        System.out.println(System.currentTimeMillis());

        return kitCooldown.get(player);
    }

    public void setCooldown(Player player, String id) {
        HashMap<Player, Long> kitCooldown = this.cooldowns.get(id);
        if (kitCooldown == null) {
            this.cooldowns.put(id, new HashMap<>());
        }

        this.cooldowns.get(id).put(player, System.currentTimeMillis() + this.getKit(id).getCooldown());
    }

    public Kit getKit(String id) {
        return this.kits.get(id);
    }

    public HashMap<String, Kit> getKits() {
        return this.kits;
    }
}
