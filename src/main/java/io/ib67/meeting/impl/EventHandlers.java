package io.ib67.meeting.impl;

import io.ib67.meeting.MeetingServer;
import io.ib67.meeting.data.MeetingSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.ping.ResponseData;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;

@Slf4j
@RequiredArgsConstructor
public class EventHandlers {
    private final MeetingServer server;

    public void handleLogin(@NotNull PlayerLoginEvent e) {
        log.info(e.getPlayer().getUsername() + " is connecting...");
        // check player nums
        if (isServerFull()) {
            e.getPlayer().kick("Server is full.");
            return;
        }
        // select instances.
        var ip = e.getPlayer().getPlayerConnection().getServerAddress();
        var arr = ip.split("\\.");
        if (arr.length > 0) {
            var code = arr[0];
            server.getMeetingManager().getMeetingByCode(code)
                    .ifPresentOrElse(session -> {
                        e.setSpawningInstance(session.instance());
                    }, () -> {
                        e.setSpawningInstance(server.getMapConfig().template()); // go lobby
                    });
        } else {
            // localhost or something... fallback
            e.setSpawningInstance(server.getMapConfig().template());
        }
    }

    private boolean isServerFull() {
        return MinecraftServer.getConnectionManager().getOnlinePlayers().size() >= server.getMeetingConfig().getMaxPlayers();
    }

    public void handleSpawn(@NotNull PlayerSpawnEvent e) {
        var inst = e.getSpawnInstance();
        if (server.getMeetingConfig().isNightVision()) {
            e.getPlayer().addEffect(new Potion(PotionEffect.NIGHT_VISION, (byte) 255, Integer.MAX_VALUE));
        }
        e.getPlayer().setAllowFlying(server.getMeetingConfig().isAllowFlight());
        server.getMeetingManager().getMeetingByInstance(inst)
                .ifPresentOrElse(meeting -> {
                    meeting.greet(e.getPlayer());
                    e.getPlayer().teleport(server.getMapConfig().spawnPos());
                }, () -> {
                    e.getPlayer().sendMessage("It seems that you are not in any meeting. Type /help for help");
                    e.getPlayer().teleport(server.getMapConfig().spawnPos());
                });
        // check
        var pos = server.getMapConfig().spawnPos();
        try {
            inst.loadChunk(pos).get();
            var highestY = server.getMapConfig().spawnPos().y();
            for (int i = -64; i <= highestY; i++) {
                if (!inst.getBlock(pos.blockX(), i, pos.blockZ()).isAir()) {
                    return;
                }
            }
            e.getPlayer().setGameMode(GameMode.SPECTATOR);
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException("Can't load chunk",ex);
        }
    }

    public void handleExit(@NotNull PlayerDisconnectEvent e) {
        var inst = e.getInstance();
        server.getMeetingManager().getMeetingByInstance(inst)
                .ifPresentOrElse(meeting -> {
                    // move to another instance
                    e.getPlayer().setInstance(server.getMapConfig().template());
                    server.getMeetingManager().exitMeeting(e.getPlayer(), meeting);
                }, () -> {
                    log.info(e.getPlayer().getUsername() + " left the server.");
                });
    }

    public void handlePing(@NotNull ServerListPingEvent e) {
        if (!server.getMeetingConfig().isShowMotd()) {
            return;
        }
        var a = e.getConnection().getServerAddress().split("\\.");
        var data = e.getResponseData();
        data.clearEntries();
        data.setPlayersHidden(true);
        if (a.length == 0) {
            putDefault(false, data, null);
        } else {
            var identifier = a[0];
            server.getMeetingManager().getMeetingByCode(identifier)
                    .ifPresentOrElse(meeting -> {
                        data.setOnline(meeting.instance().getPlayers().size());
                        putMotd(meeting, data);
                    }, () -> {
                        putDefault(true, data, identifier);
                    });
        }
    }

    public void handleRespawn(PlayerRespawnEvent event) {
        event.setRespawnPosition(server.getMapConfig().spawnPos());
    }

    private void putMotd(MeetingSession meeting, ResponseData data) {
        data.setDescription(
                Component.text(meeting.title()).color(NamedTextColor.GOLD)
                        .appendSpace()
                        .append(
                                Component.text("is open.").color(NamedTextColor.WHITE)
                        ).appendNewline()
                        .append(
                                Component.text("Powered by MineMeeting | " + meeting.inviteCode())
                                        .color(NamedTextColor.DARK_GRAY)
                        )
        );
    }

    private void putDefault(boolean notExist, ResponseData motdData, String code) {
        motdData.setOnline(MinecraftServer.getConnectionManager().getOnlinePlayers().size());
        motdData.setDescription(
                Component.text("Mine Meeting").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD)
                        .append(
                                Component.text(" | ").decoration(TextDecoration.BOLD, false).color(NamedTextColor.GRAY)
                        ).decoration(TextDecoration.BOLD, false).append(
                                notExist ? Component.text(code + " is ended or not exists")
                                        .color(NamedTextColor.RED)
                                        : isServerFull()
                                        ? Component.text("Server is full.").color(NamedTextColor.RED).decorate(TextDecoration.BOLD)
                                        : Component.text("Join us to get started!").decoration(TextDecoration.BOLD, false).color(NamedTextColor.GREEN)
                        ).appendNewline()
                        // statistics
                        .append(
                                Component.text("Holding ").color(NamedTextColor.WHITE)
                                        .append(
                                                Component.text(server.getMeetingManager().getMeetings().size())
                                                        .color(NamedTextColor.AQUA)
                                        ).appendSpace()
                                        .append(
                                                Component.text("meeting rooms, online mode is ")
                                                        .color(NamedTextColor.WHITE)
                                                        .append(
                                                                Component.text(server.getMeetingConfig().isOnlineMode()
                                                                        ? "on"
                                                                        : "off"
                                                                ).color(NamedTextColor.RED)
                                                        )
                                        )
                        )
        );
    }

    public void handleBreak(@NotNull PlayerBlockBreakEvent e) {
        if (!server.getMeetingConfig().isCanBreakBlocks()) {
            e.setCancelled(true);
            return;
        }
        if (server.getMeetingConfig().isAutoRestoreBlocks()) {
            e.getInstance().scheduler().scheduleTask(() -> {
                var pos = e.getBlockPosition();
                e.getInstance().setBlock(pos, e.getBlock());
            }, TaskSchedule.seconds(3), TaskSchedule.stop());
        }
    }

    public void handleMove(@NotNull PlayerMoveEvent event) {
        var distance = server.getMeetingConfig().getMaxDistanceFromSpawn();
        if(distance != -1){
            if(event.getNewPosition().distance(server.getMapConfig().spawnPos()) > distance){
                event.setNewPosition(event.getPlayer().getPosition());
            }
        }
    }
}
