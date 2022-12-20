package io.ib67.meeting.command;

import io.ib67.meeting.MeetingServer;
import io.ib67.meeting.data.MeetingSession;
import io.ib67.meeting.util.RandomStrings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CommandNew extends Command {
    private static final Argument<String> TITLE = ArgumentType.String("meeting-title");
    private final MeetingServer server;

    public CommandNew(MeetingServer server) {
        super("new", "create", "begin");
        Objects.requireNonNull(this.server = server);
        setDefaultExecutor(this::handleDefault);
        addSyntax(this::handleCreate, TITLE);
    }

    private void handleCreate(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        var title = commandContext.get(TITLE);
        createMeeting(commandSender, title);
    }

    private void handleDefault(@NotNull CommandSender commandSender, @NotNull CommandContext commandContext) {
        if (commandSender instanceof Player p) {
            createMeeting(p, p.getUsername() + "'s meeting");
        } else {
            createMeeting(commandSender, "Untitled meeting");
        }
    }

    private void createMeeting(CommandSender commandSender, String title) {
        var code = RandomStrings.randomString(6);

        var meeting = server.getMeetingManager().createMeeting(false, code, title);
        if (commandSender instanceof Player p) {
            goMeeting(p, meeting,server);
            p.sendMessage(
                    Component.text("You just created a new meeting!").color(TextColor.color(NamedTextColor.GREEN))
            );
            p.sendMessage("Below are two ways to join your meeting:");
            p.sendMessage(
                    Component.text(" - ")
                            .append(Component.text("Join "))
                            .append(
                                    Component.text(meeting.inviteCode()+"."+server.getMeetingConfig().getServerHost())
                                            .clickEvent(ClickEvent.copyToClipboard(meeting.inviteCode()+"."+server.getMeetingConfig().getServerHost()))
                                            .color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD).decoration(TextDecoration.BOLD,false)
                            ).appendSpace().append(Component.text("directly."))
            );
            p.sendMessage(
                    Component.text(" - Type ").append(
                            Component.text("/go "+meeting.inviteCode()+" ").clickEvent(ClickEvent.copyToClipboard("/go "+meeting.inviteCode()))
                                    .color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD).decoration(TextDecoration.BOLD,false)
                    ).append(Component.text("in the server."))
            );
            p.sendMessage(" ");
            if(!meeting.persistent()) p.sendMessage(Component.text("The meeting room will be destroyed if nobody inside.").color(NamedTextColor.GRAY));
        } else {
            commandSender.sendMessage("A new meeting is created!");
            commandSender.sendMessage("Use /go " + meeting.inviteCode() + " or join " + meeting.inviteCode() + "." + server.getMeetingConfig().getServerHost());
        }
    }

    public static void goMeeting(Player p, MeetingSession meeting, MeetingServer server) {
        server.getMeetingManager().getMeetingByInstance(p.getInstance())
                .ifPresentOrElse(old -> {
                    p.setInstance(meeting.instance(), server.getMapConfig().spawnPos()); // must go to another instance before destroy
                    server.getMeetingManager().exitMeeting(p, old);
                },()->{
                    p.setInstance(meeting.instance(), server.getMapConfig().spawnPos());
                });
    }
}
