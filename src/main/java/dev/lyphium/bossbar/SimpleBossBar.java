package dev.lyphium.bossbar;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SimpleBossBar extends JavaPlugin {

    private BossBarManager bossBarManager;

    @Override
    public void onEnable() {
        bossBarManager = new BossBarManager(this);

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> bossBarManager.showingBossBar(p))
                .forEach(p -> bossBarManager.createBossBar(p));
        getServer().getPluginManager().registerEvents(new PlayerListener(bossBarManager), this);

        new BossBarCommand(bossBarManager).register(Objects.requireNonNull(getCommand("bossbar")));

        getLogger().info("Plugin activated");
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(p -> bossBarManager.removeBossBar(p));

        getLogger().info("Plugin deactivated");
    }
}
