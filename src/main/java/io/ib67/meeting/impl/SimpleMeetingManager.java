package io.ib67.meeting.impl;

import io.ib67.meeting.data.MapConfig;
import io.ib67.meeting.data.MeetingSession;
import io.ib67.meeting.meeting.MeetingManager;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.tag.Tag;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SimpleMeetingManager implements MeetingManager {
    private static final Tag<String> MEETING_ID = Tag.String("meeting_id");
    private final MapConfig mapConfig;
    private final Map<String, MeetingSession> meetings = new ConcurrentHashMap<>();

    public SimpleMeetingManager(MapConfig roomTemplate) {
        this.mapConfig = roomTemplate;
    }

    @Override
    public Optional<MeetingSession> getMeetingByCode(String inviteCode) {
        if (inviteCode == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(meetings.get(inviteCode));
    }

    @Override
    public Optional<MeetingSession> getMeetingByInstance(Instance instance) {
        return getMeetingByCode(instance.getTag(MEETING_ID));
    }

    @Override
    public Collection<MeetingSession> getMeetings() {
        return meetings.values();
    }

    @Override
    public void unregisterMeeting(MeetingSession session) {
        log.info("Destroying a meeting: " + session.title() +" ("+session.inviteCode()+")");
        session.instance().getPlayers().forEach(p -> p.kick("Meeting is ended"));
        MinecraftServer.getInstanceManager().unregisterInstance(session.instance());
        meetings.remove(session.inviteCode());
        if (session.persistent()) {
            log.warn("Unregistering a persistent meeting: " + session.inviteCode());
        }
    }

    @Override
    public MeetingSession createMeeting(boolean persistent, String inviteCode, String title) {
        var session = new MeetingSession(
                title,
                inviteCode,
                persistent,
                createInstance(inviteCode)
        );
        meetings.put(inviteCode, session);
        return session;
    }

    @Override
    public void exitMeeting(Player player, MeetingSession meeting) {
        var inst = meeting.instance();
        if (inst.getPlayers().size() - 1 <=0 && !meeting.persistent()){
            log.info(meeting.title() + " is ended.");
            unregisterMeeting(meeting);
        } else{
            inst.getPlayers().forEach(p -> {
                p.sendMessage(Component.text(player.getUsername()).color(NamedTextColor.AQUA).append(Component.text(" left the meeting.")));
            });
        }
    }

    private Instance createInstance(String inviteCode) {
        var inst = MinecraftServer.getInstanceManager().createSharedInstance(mapConfig.template());
        inst.setTag(MEETING_ID, inviteCode);
        return inst;
    }
}
