package dev.lyphium.bossbar;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ReloadBossBarCommand implements CommandExecutor, TabCompleter {

    private final BossBarManager bossBarManager;

    public ReloadBossBarCommand(@NotNull BossBarManager bossBarManager) {
        this.bossBarManager = bossBarManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length != 0) {
            return false;
        }

        // Load new config and restart update task
        bossBarManager.loadConfig();
        bossBarManager.startUpdateTask();

        // Recreate Boss Bar for all players
        Bukkit.getOnlinePlayers().forEach(p -> {
            bossBarManager.removeBossBar(p);
            bossBarManager.createBossBar(p);
        });

        sender.sendActionBar(Component.text("Boss Bar Reloaded", TextColor.color(0x1EFF41)));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }

    /**
     * Set this object as an executor and tab completer for the command.
     *
     * @param command Command to be handled.
     */
    public void register(@NotNull PluginCommand command) {
        command.setExecutor(this);
        command.setTabCompleter(this);
    }
}
