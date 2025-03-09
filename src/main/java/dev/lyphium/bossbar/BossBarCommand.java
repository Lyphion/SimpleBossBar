package dev.lyphium.bossbar;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class BossBarCommand implements CommandExecutor, TabCompleter {

    private final BossBarManager bossBarManager;

    public BossBarCommand(@NotNull BossBarManager bossBarManager) {
        this.bossBarManager = bossBarManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length != 1)
            return false;

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("bossbar.admin")) {
                sender.sendMessage(Bukkit.getServer().permissionMessage());
                return true;
            }

            // Load new config and restart update task
            bossBarManager.loadConfig();
            bossBarManager.startUpdateTask();

            // Recreate Boss Bar for all players
            for (final Player p : Bukkit.getOnlinePlayers()) {
                if (bossBarManager.showingBossBar(p)) {
                    bossBarManager.removeBossBar(p);
                    bossBarManager.createBossBar(p);
                }
            }

            sender.sendActionBar(Component.text("Boss Bar Reloaded", TextColor.color(0x1EFF41)));
            return true;
        } else if (args[0].equalsIgnoreCase("toggle")) {
            // Only player can execute the command
            if (!(sender instanceof Player player)) {
                sender.sendMessage("You must be a player to use this command!");
                return true;
            }

            // Toggle Boss Bar for player
            bossBarManager.setShowBossBar(player, !bossBarManager.showingBossBar(player));
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length != 1)
            return List.of();

        final String name = args[0].toLowerCase();

        final List<String> completions = new ArrayList<>(2);
        if (sender.hasPermission("bossbar.admin"))
            completions.add("reload");
        if (sender instanceof Player)
            completions.add("toggle");

        return completions.stream()
                .filter(s -> s.startsWith(name))
                .toList();
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
