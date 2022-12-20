package io.ib67.meeting;

import io.ib67.meeting.data.MapConfig;
import io.ib67.meeting.data.MeetingConfig;
import io.ib67.meeting.meeting.MeetingManager;

public interface MeetingServer {
    MeetingManager getMeetingManager();
    MeetingConfig getMeetingConfig();
    MapConfig getMapConfig();
}
