package io.ib67.meeting.data;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;

public record MapConfig(
        InstanceContainer template,
        Pos spawnPos
) {
}
