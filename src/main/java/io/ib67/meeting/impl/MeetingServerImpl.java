package io.ib67.meeting.impl;

import io.ib67.meeting.MeetingServer;
import io.ib67.meeting.command.CommandGo;
import io.ib67.meeting.command.CommandHelp;
import io.ib67.meeting.command.CommandNew;
import io.ib67.meeting.data.MapConfig;
import io.ib67.meeting.data.MeetingConfig;
import io.ib67.meeting.meeting.MeetingManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ServerListPingEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.block.Block;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.NBTReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static net.minestom.server.MinecraftServer.*;

@Getter
@Slf4j
public class MeetingServerImpl implements MeetingServer {
    private static final Path CURRENT = Path.of(".");
    private final MapConfig mapConfig = loadMap();
    private final MeetingManager meetingManager = new SimpleMeetingManager(mapConfig);
    private final MeetingConfig meetingConfig = MeetingConfig.load(CURRENT);

    private MapConfig loadMap() {
        var worldDir = CURRENT.resolve("world");
        if (Files.notExists(worldDir)) {
            log.warn("`world` is missing! We'll create a new world for you.");
            var inst = MinecraftServer.getInstanceManager().createInstanceContainer();
            inst.setGenerator(it -> {
                it.modifier().fillHeight(0, 1, Block.GRASS_BLOCK);
            });
            return new MapConfig(inst, new Pos(0, 2, 0));
        }
        // parse spawnPos
        Pos pos = null;
        try (var reader = new NBTReader(worldDir.resolve("level.dat"));) {
            var root = (NBTCompound) reader.read();
            root = root.getCompound("Data");
            var x = root.getInt("SpawnX");
            var y = root.getInt("SpawnY");
            var z = root.getInt("SpawnZ");
            pos = new Pos(x, y, z);
            log.info("Read map: "+root.getString("LevelName")+" | Spawn: "+pos);
        } catch (IOException e) {
            System.exit(-1);
            throw new RuntimeException(e);
        } catch (NBTException e) {
            throw new RuntimeException(e);
        }
        var inst = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader(worldDir));
        return new MapConfig(inst, pos);
    }

    public void run(MinecraftServer server) {
        if (getMeetingConfig().isOnlineMode()) {
            MojangAuth.init();
        }
        var listener = new EventHandlers(this);
        getGlobalEventHandler().addListener(PlayerLoginEvent.class, listener::handleLogin);
        getGlobalEventHandler().addListener(PlayerSpawnEvent.class, listener::handleSpawn);
        getGlobalEventHandler().addListener(PlayerDisconnectEvent.class, listener::handleExit);
        getGlobalEventHandler().addListener(ServerListPingEvent.class, listener::handlePing);
        getGlobalEventHandler().addListener(PlayerRespawnEvent.class, listener::handleRespawn);
        getGlobalEventHandler().addListener(PlayerBlockBreakEvent.class, listener::handleBreak);
        getGlobalEventHandler().addListener(PlayerMoveEvent.class,listener::handleMove);
        getGlobalEventHandler().addListener(PlayerChatEvent.class,listener::handleChat);
        // create kept rooms
        for (var entry : meetingConfig.getKeptMeeting().entrySet()) {
            log.info("Creating Meeting: `" + entry.getKey() + "`  " + entry.getValue());
            meetingManager.createMeeting(
                    true,
                    entry.getKey(),
                    entry.getValue().toString()
            );
        }
        // commands
        getCommandManager().register(new CommandHelp());
        getCommandManager().register(new CommandNew(this));
        getCommandManager().register(new CommandGo(this));

        log.info("Done! Listening on " + meetingConfig.getListenAddr() + ":" + meetingConfig.getListenPort());
        server.start(meetingConfig.getListenAddr(), meetingConfig.getListenPort());
    }
}
