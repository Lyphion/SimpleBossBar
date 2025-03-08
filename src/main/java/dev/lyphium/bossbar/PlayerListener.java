package dev.lyphium.bossbar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public final class PlayerListener implements Listener {

    private final BossBarManager bossBarManager;

    public PlayerListener(BossBarManager bossBarManager) {
        this.bossBarManager = bossBarManager;
    }

    @EventHandler
    private void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        bossBarManager.removeBossBar(event.getPlayer());
    }

}
