package io.ib67.meeting.command;

import io.ib67.meeting.MeetingServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandGo extends Command {
    private static final Argument<String> INVITE_CODE = ArgumentType.String("invite-code");
    private final MeetingServer server;

    public CommandGo(MeetingServer server) {
        super("go", "join", "connect");
        this.server = server;
        setDefaultExecutor(this::handleDefault);
        addSyntax(this::handleGo, INVITE_CODE);
    }

    private void handleDefault(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        commandSender.sendMessage(Component.text("You need to specify a invite code, please contact your activity holder.").color(NamedTextColor.RED));
    }

    private void handleGo(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        if (commandSender instanceof Player p) {
            var code = commandContext.get(INVITE_CODE);
            var manager = server.getMeetingManager();
            manager.getMeetingByCode(code)
                    .ifPresentOrElse(meeting -> {
                        if(manager.getMeetingByInstance(p.getInstance()).orElse(null) == meeting){
                            p.sendMessage(Component.text("You're already in the meeting!").color(NamedTextColor.RED));
                        }else{
                            CommandNew.goMeeting(p, meeting, server);
                        }
                    },()->{
                        p.sendMessage(Component.text("The meeting with code `"+code+"` isn't exists or is already ended."));
                    });
        } else {
            commandSender.sendMessage("Must be a player.");
        }
    }
}
