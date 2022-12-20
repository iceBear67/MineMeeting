package io.ib67.meeting.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandHelp extends Command {
    public CommandHelp() {
        super("help", "wat", "info");
        setDefaultExecutor((this::handleDefault));
    }

    private void handleDefault(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        commandSender.sendMessage(Component.text(" MineMeeting ").color(NamedTextColor.AQUA));
        commandSender.sendMessage(Component.text("---------------------").color(NamedTextColor.GRAY));
        commandSender.sendMessage(Component.text(" /new [title]").color(NamedTextColor.GREEN).append(Component.text(" to create a new meeting")));
        commandSender.sendMessage(Component.text(" /go <inviteCode>").color(NamedTextColor.GREEN).append(Component.text(" to join a meeting.")));
    }
}
