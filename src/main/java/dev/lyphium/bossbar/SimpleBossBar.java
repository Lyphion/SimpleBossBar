package dev.lyphium.bossbar;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.UnknownNullability;

public final class SimpleBossBar extends JavaPlugin {

    @UnknownNullability
    private BossBarManager bossBarManager;

    @Override
    public void onEnable() {
        bossBarManager = new BossBarManager(this);

        Bukkit.getOnlinePlayers().stream()
                .filter(bossBarManager::showingBossBar)
                .forEach(bossBarManager::createBossBar);
        getServer().getPluginManager().registerEvents(new PlayerListener(bossBarManager), this);

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            final Commands registrar = commands.registrar();

            final BossBarCommand command = new BossBarCommand(bossBarManager);
            registrar.register(command.construct(), BossBarCommand.DESCRIPTION);
        });
    }

    @Override
    public void onDisable() {
        Bukkit.getOnlinePlayers().forEach(bossBarManager::removeBossBar);
    }
}
