package io.ib67.meeting.meeting;

import io.ib67.meeting.data.MeetingSession;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;

import java.util.Collection;
import java.util.Optional;

public interface MeetingManager {
    Optional<MeetingSession> getMeetingByCode(String inviteCode);

    Optional<MeetingSession> getMeetingByInstance(Instance instance);

    Collection<MeetingSession> getMeetings();

    void unregisterMeeting(MeetingSession session);

    MeetingSession createMeeting(boolean persistent, String inviteCode, String title);

    void exitMeeting(Player player, MeetingSession meeting);
}
