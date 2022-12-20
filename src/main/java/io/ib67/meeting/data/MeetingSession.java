package io.ib67.meeting.data;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ScopedComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.stream.Collectors;

public record MeetingSession(
        String title,
        String inviteCode,
        boolean persistent,
        Instance instance
) {
    public void greet(Player player) {
        player.sendMessage(
                Component.text("Welcome! ") //huh, over-engineered api, isn't it?
                        .color(NamedTextColor.GREEN)
                        .style(Style.style(TextDecoration.BOLD))
                        .append(
                                Component.text("You just joined ")
                                        .append(
                                                Component.text(title())
                                                        .style(Style.style(TextDecoration.UNDERLINED))
                                        )
                        )
        );
        instance.getPlayers().forEach(p -> {
            p.sendMessage(
                    Component.text(player.getUsername()).color(NamedTextColor.AQUA)
                            .append(Component.text(" joined the meeting!").color(NamedTextColor.WHITE))
            );
        });
        player.sendMessage(Component.text("Here are " + instance.getPlayers().size() + " players in the meeting, including ")
                .append(
                        Component.text(instance.getPlayers().stream()
                                .map(it -> it.getUsername())
                                .collect(Collectors.joining(", "))).color(NamedTextColor.AQUA)
                ));
    }
}
