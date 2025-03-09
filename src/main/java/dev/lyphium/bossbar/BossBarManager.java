package dev.lyphium.bossbar;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.util.Tuple;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BossBarManager {

    /**
     * Saving delay.
     */
    public static final int SAVE_DELAY = 20 * 2;

    private final JavaPlugin plugin;

    private int period;
    private boolean async;

    private String text;
    private float progress;
    private BossBar.Color color;
    private BossBar.Overlay overlay;

    private final Map<UUID, BossBar> bossBars = new ConcurrentHashMap<>();
    private final Map<UUID, Tuple<String, Component>> cachedMessages = new ConcurrentHashMap<>();
    private final Set<UUID> hiddenBossBars = new HashSet<>();

    private BukkitTask saveTask, updateTask;

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
        async = config.getBoolean("Asynchronously", true);
        text = config.getString("Text", "");
        progress = (float) config.getDouble("Progress", 1.0);
        color = BossBar.Color.NAMES.valueOr(config.getString("Color", "red"), BossBar.Color.RED);
        overlay = BossBar.Overlay.NAMES.valueOr(config.getString("Overlay", "progress"), BossBar.Overlay.PROGRESS);

        hiddenBossBars.clear();
        final YamlConfiguration hiddenConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "hidden.yml"));
        for (final String hidden : hiddenConfig.getStringList("Hidden")) {
            hiddenBossBars.add(UUID.fromString(hidden));
        }
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

        if (async) {
            // Run Update task in the background
            updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateBossBar, period, period);
        } else {
            // Run Update task in the foreground
            updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateBossBar, period, period);
        }
    }

    /**
     * Set whether a player can see the Boss Bar.
     *
     * @param player Player to change the state
     * @param show   New state of the Boss Bar for the player.
     */
    public void setShowBossBar(@NotNull Player player, boolean show) {
        // Check if new state is equal old -> skip
        if (show != hiddenBossBars.contains(player.getUniqueId()))
            return;

        // Update hidden list and change Boss Bar status
        if (show) {
            hiddenBossBars.remove(player.getUniqueId());
            createBossBar(player);
        } else {
            hiddenBossBars.add(player.getUniqueId());
            removeBossBar(player);
        }

        // Cancel scheduled task
        if (saveTask != null)
            saveTask.cancel();

        // Delay saving, if multiple edits are made
        // Run saving asynchronously
        saveTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            final YamlConfiguration hiddenConfig = new YamlConfiguration();
            hiddenConfig.set("Hidden", hiddenBossBars.stream().map(UUID::toString).toList());

            try {
                hiddenConfig.save(new File(plugin.getDataFolder(), "hidden.yml"));
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save hidden config");
            }
        }, SAVE_DELAY);
    }

    /**
     * Check whether the Boss Bar is visible for the player.
     *
     * @param player Player to check
     * @return {@code true} if Boss Bar is shown.
     */
    public boolean showingBossBar(@NotNull Player player) {
        return !hiddenBossBars.contains(player.getUniqueId());
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
        cachedMessages.remove(player.getUniqueId());

        if (bossBar != null)
            player.hideBossBar(bossBar);
    }

    /**
     * Update the Boss Bars for all players.
     */
    public void updateBossBar() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            final BossBar bossBar = bossBars.getOrDefault(player.getUniqueId(), null);
            if (bossBar == null)
                continue;

            bossBar.name(formatBossBarText(player));
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
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            text = PlaceholderAPI.setPlaceholders(player, text);

        final Tuple<String, Component> tuple = cachedMessages.getOrDefault(player.getUniqueId(), null);
        if (tuple != null && tuple.getA().equals(text))
            return tuple.getB();

        final Component message = MiniMessage.miniMessage().deserialize(text);
        cachedMessages.put(player.getUniqueId(), new Tuple<>(text, message));
        return message;
    }
}
