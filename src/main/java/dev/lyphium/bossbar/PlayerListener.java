package dev.lyphium.bossbar;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerListener implements Listener {

    private final BossBarManager bossBarManager;

    public PlayerListener(BossBarManager bossBarManager) {
        this.bossBarManager = bossBarManager;
    }

    @EventHandler
    private void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (bossBarManager.showingBossBar(player))
            bossBarManager.createBossBar(player);
    }

    @EventHandler
    private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        bossBarManager.removeBossBar(event.getPlayer());
    }
}
