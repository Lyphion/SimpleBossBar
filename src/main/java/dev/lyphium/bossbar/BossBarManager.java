package dev.lyphium.bossbar;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BossBarManager {

    private final JavaPlugin plugin;

    private int period;

    private String text;
    private float progress;
    private BossBar.Color color;
    private BossBar.Overlay overlay;

    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private BukkitTask updateTask;

    public BossBarManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;

        loadConfig();
        startUpdateTask();
    }

    /**
     * Load config values.
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        final FileConfiguration config = plugin.getConfig();

        period = config.getInt("Period", 20);
        text = config.getString("Text", "");
        progress = (float) config.getDouble("Progress", 1.0);
        color = BossBar.Color.NAMES.valueOr(config.getString("Color", "red"), BossBar.Color.RED);
        overlay = BossBar.Overlay.NAMES.valueOr(config.getString("Overlay", "progress"), BossBar.Overlay.PROGRESS);
    }

    /**
     * Start or restart the update task.
     */
    public void startUpdateTask() {
        if (updateTask != null)
            updateTask.cancel();

        // No update if period is zero or negative
        if (period <= 0)
            return;

        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateBossBar, period, period);
    }

    /**
     * Create a new Boss Bar for a player.
     *
     * @param player Player for whom a Boss Bar should be created.
     */
    public void createBossBar(@NotNull Player player) {
        final Component text = formatBossBarText(player);
        final BossBar bossBar = BossBar.bossBar(text, progress, color, overlay);
        bossBars.put(player.getUniqueId(), bossBar);
        player.showBossBar(bossBar);
    }

    /**
     * Remove a Boss Bar for a player.
     *
     * @param player Player for whom a Boss Bar should be removed.
     */
    public void removeBossBar(@NotNull Player player) {
        final BossBar bossBar = bossBars.remove(player.getUniqueId());
        player.hideBossBar(bossBar);
    }

    /**
     * Update the Boss Bars for all players.
     */
    public void updateBossBar() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (!bossBars.containsKey(player.getUniqueId()))
                continue;

            final Component text = formatBossBarText(player);
            final BossBar bossBar = bossBars.get(player.getUniqueId());
            bossBar.name(text);
        }
    }

    /**
     * Format the Boss Bar for a player, and replace Placeholders.
     *
     * @param player Player for whom the Boss Bar to format.
     * @return Formated Boss Bar.
     */
    private @NotNull Component formatBossBarText(@NotNull Player player) {
        String text = this.text;
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        return MiniMessage.miniMessage().deserialize(text);
    }
}
