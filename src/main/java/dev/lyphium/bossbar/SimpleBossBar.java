package dev.lyphium.bossbar;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleBossBar extends JavaPlugin {

    private BossBarManager bossBarManager;

    @Override
    public void onEnable() {
        bossBarManager = new BossBarManager(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(bossBarManager), this);

        getLogger().info("Plugin activated");
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(p -> bossBarManager.removeBossBar(p));

        getLogger().info("Plugin deactivated");
    }
}
