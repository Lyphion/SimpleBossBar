package dev.lyphium.bossbar;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class BossBarCommand {

    public static final String DESCRIPTION = "Manage the boss bar";

    private final BossBarManager bossBarManager;

    public BossBarCommand(BossBarManager bossBarManager) {
        this.bossBarManager = bossBarManager;
    }

    public LiteralCommandNode<CommandSourceStack> construct() {
        return Commands.literal("bossbar")
                .then(Commands.literal("reload")
                        .requires(s -> s.getSender().hasPermission("bossbar.admin"))
                        .executes(ctx -> {
                            final CommandSender executor = ctx.getSource().getExecutor() == null ? ctx.getSource().getSender() : ctx.getSource().getExecutor();

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

                            executor.sendActionBar(Component.text("Plugin reloaded", TextColor.color(0x1EFF41)));
                            return Command.SINGLE_SUCCESS;
                        })
                ).then(Commands.literal("toggle")
                        .requires(s -> s.getExecutor() instanceof Player)
                        .executes(ctx -> {
                            final Player player = (Player) ctx.getSource().getExecutor();
                            if (player == null)
                                return Command.SINGLE_SUCCESS;

                            // Toggle Boss Bar for player
                            bossBarManager.setShowBossBar(player, !bossBarManager.showingBossBar(player));
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();
    }
}
