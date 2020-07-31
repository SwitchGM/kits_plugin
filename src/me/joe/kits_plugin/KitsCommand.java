package me.joe.kits_plugin;

import me.joe.bundle_me.gui_me.GUIMePlugin;
import me.joe.bundle_me.gui_me.element.Button;
import me.joe.bundle_me.gui_me.element.Element;
import me.joe.bundle_me.gui_me.gui.GUI;
import me.joe.bundle_me.gui_me.gui.GUIManager;
import me.joe.bundle_me.gui_me.gui.GUIType;
import me.joe.bundle_me.item_me.items.CustomItem;
import me.joe.bundle_me.item_me.items.CustomItemManager;
import me.joe.kits_plugin.kit.Kit;
import me.joe.kits_plugin.kit.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class KitsCommand implements CommandExecutor {

    private KitManager manager;
    private CustomItemManager customItemManager;
    private GUIManager guiManager;

    private GUI kitsMenu;

    public KitsCommand(KitManager manager, CustomItemManager customItemManager) {
        this.manager = manager;
        this.customItemManager = customItemManager;
        this.guiManager = new GUIManager((GUIMePlugin) Bukkit.getPluginManager().getPlugin("GUIMe"));
        this.kitsMenu = this.createKitsMenu();

    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 0) {
            this.openKitsMenu((Player) commandSender);
            return true;
        }

        String subCommand = strings[0];
        switch (subCommand) {
            case "give":
                this.giveKit(commandSender, strings);
                break;
            case "show":
                this.showKit(commandSender, strings);
                break;
            case "save":
                this.saveKit(commandSender, strings);
            default:
                this.openKitsMenu((Player) commandSender);
        }
        return false;
    }

    private void giveKit(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof Player)) {
            System.out.println("you must be a player to run this command!");
            return;
        }

        Player player = (Player) commandSender;

        if (args.length == 1) {
            this.sendError(player, "You didn't specify a kit!");
            return;
        }

        if (!this.manager.getKits().containsKey(args[1])) {
            this.sendError(player, "That kit does not exist!");
            return;
        }

        if (!player.hasPermission("kits.give." + args[1])) {
            this.sendError(player, "You don't have permission to get this kit!");
            return;
        }

        if (args.length == 3 && player.hasPermission("kits.give.other")) {
            Player targetPlayer = Bukkit.getServer().getPlayerExact(args[2]);
            if (targetPlayer == null) {
                this.sendError(player, "The target player was not found!");
                return;
            }

            this.manager.giveKit(args[1], targetPlayer);
            return;
        }

        this.manager.giveKit(args[1], player);
    }

    private void showKit(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof Player)) {
            System.out.println("you must be a player to run this command!");
            return;
        }

        Player player = (Player) commandSender;

        if (args.length == 1) {
            this.sendError(player, "You didn't specify a kit!");
            return;
        }

        if (!this.manager.getKits().containsKey(args[1])) {
            this.sendError(player, "That kit does not exist!");
            return;
        }

        if (!player.hasPermission("kits.show." + args[1])) {
            this.sendError(player, "You don't have permission to view this kit!");
            return;
        }

        if (args.length == 3 && player.hasPermission("kits.show.other")) {
            Player targetPlayer = Bukkit.getServer().getPlayerExact(args[2]);
            if (targetPlayer == null) {
                this.sendError(player, "The target player was not found!");
                return;
            }

            this.displayKit(args[1], targetPlayer);
            return;
        }

        this.displayKit(args[1], player);
    }

    private void saveKit(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof Player)) {
            System.out.println("you must be a player to run this command!");
            return;
        }

        Player player = (Player) commandSender;

        if (!player.hasPermission("kits.save")) {
            this.sendError(player, "You don't have permission to save kits!");
            return;
        }

        if (args.length == 1) {
            this.sendError(player, "You didn't specify a kit!");
            return;
        }

        if (this.manager.getKits().containsKey(args[1])) {
            this.sendError(player, "That kit already exists!");
            return;
        }

        this.manager.giveKit(args[1], player);
    }

    private void openKitsMenu(Player player) {
        this.guiManager.openGUI(this.kitsMenu, player);
    }

    private void displayKit(String id, Player player) {
        Kit kit = this.manager.getKit(id);

        List<String> items = kit.getItems();

        float rawKitSize = (float) items.size() / 9f;
        int kitSize = (int) Math.ceil(rawKitSize);

        GUI kitDisplay = new GUI(GUIType.MENU, kitSize);

        for (String rawItem : items) {
            String[] itemInfo = rawItem.split(",");
            ItemStack kitItem;

            if (this.customItemManager.getCustomItem(itemInfo[0]) != null) {
                kitItem = this.customItemManager.getCustomItem(itemInfo[0]).getAmountOfItem(Integer.parseInt(itemInfo[1]));

            } else {
                kitItem = new ItemStack(Material.valueOf(itemInfo[0]), Integer.parseInt(itemInfo[1]));
            }

            kitDisplay.addElement(new Element(kitItem));
        }

        this.guiManager.openGUI(kitDisplay, player);
    }

    private GUI createKitsMenu() {
        float rawKitSize = (float) manager.getKits().keySet().size() / 9f;
        int kitSize = (int) Math.ceil(rawKitSize);

        GUI gui = new GUI(GUIType.MENU, kitSize);

        HashMap<String, Kit> kits = this.manager.getKits();
        for (String id : kits.keySet()) {
            Kit kit = kits.get(id);

            String rawIcon = kit.getIcon();

            ItemStack kitItem;

            if (this.customItemManager.getCustomItem(rawIcon) != null) {
                kitItem = this.customItemManager.getCustomItem(rawIcon).getItem();

            } else {
                kitItem = new ItemStack(Material.valueOf(rawIcon));
            }

            CustomItem item = new CustomItem(kitItem).setName(kit.getName()).setLore(Collections.singletonList("&7Do &c/kits show " + id + "&7 to view the kit items!"));

            String command = "kits give " + id;

            gui.addElement(new Button(item.getItem(), Collections.singletonList(command)));
        }

        return gui;
    }

    private void sendError(Player player, String error) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + error));
    }

    private void sendSuccess(Player player, String sucess) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a" + sucess));
    }
}
