package io.ib67.meeting;

import io.ib67.meeting.impl.MeetingServerImpl;
import net.minestom.server.MinecraftServer;

public class MeetingBoot {
    public static void main(String[] args) {
        var mc = MinecraftServer.init();
        new MeetingServerImpl().run(mc);
    }
}
