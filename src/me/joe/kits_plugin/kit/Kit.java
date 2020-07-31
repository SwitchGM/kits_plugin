package me.joe.kits_plugin.kit;

import java.util.List;

public class Kit {

    private String name;
    private String icon;
    private List<String> items;
    private int cooldown;

    public Kit(String name, String icon, List<String> items, int cooldown) {
        this.name = name;
        this.icon = icon;
        this.items = items;
        this.cooldown = cooldown;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public List<String> getItems() {
        return items;
    }

    public int getCooldown() {
        return cooldown;
    }
}
