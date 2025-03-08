package dev.lyphium.bossbar;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
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

    public BossBarManager(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();

        Bukkit.getScheduler().runTaskTimer(plugin, this::updateBossBar, period, period);
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        final FileConfiguration config = plugin.getConfig();

        period = config.getInt("Period");
        text = config.getString("Text");
        progress = (float) config.getDouble("Progress");
        color = BossBar.Color.NAMES.valueOrThrow(config.getString("Color", "red"));
        overlay = BossBar.Overlay.NAMES.valueOrThrow(config.getString("Overlay", "progress"));
    }

    public void createBossBar(@NotNull Player player) {
        final Component text = formatBossBarText(player);
        final BossBar bossBar = BossBar.bossBar(text, progress, color, overlay);
        bossBars.put(player.getUniqueId(), bossBar);
        player.showBossBar(bossBar);
    }

    public void removeBossBar(@NotNull Player player) {
        final BossBar bossBar = bossBars.remove(player.getUniqueId());
        player.hideBossBar(bossBar);
    }

    public void updateBossBar() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (!bossBars.containsKey(player.getUniqueId())) {
                createBossBar(player);
            } else {
                final Component text = formatBossBarText(player);
                final BossBar bossBar = bossBars.get(player.getUniqueId());
                bossBar.name(text);
            }
        }
    }

    private @NotNull Component formatBossBarText(@NotNull Player player) {
        String text = this.text;
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            text = PlaceholderAPI.setPlaceholders(player, text);
        }

        return MiniMessage.miniMessage().deserialize(text);
    }
}
