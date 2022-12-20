package io.ib67.meeting.data;

import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import io.ib67.meeting.MeetingServer;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Data
public final class MeetingConfig {
    private String listenAddr;
    private int listenPort;
    private Map<String, Object> keptMeeting;
    private int maxPlayers;
    private boolean showMotd;
    private boolean onlineMode;
    private boolean canBreakBlocks;
    private boolean autoRestoreBlocks;
    private String serverHost;
    private boolean nightVision;
    private boolean allowFlight;
    private int maxDistanceFromSpawn;

    public static MeetingConfig load(Path current) {
        var defaultCfg = ConfigFactory.load("templates/application.conf");

        var cfg = current.resolve("application.conf");
        if (Files.notExists(cfg)) {
            // extract.
            try (InputStream inputStream = MeetingServer.class.getClassLoader().getResourceAsStream("templates/application.conf")) {
                if (inputStream == null) throw new IOException("The default config template is not found");
                Files.write(cfg, inputStream.readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException("Cannot extract config from JAR", e);
            }
        }
        var config = ConfigFactory.parseFile(cfg.toFile());
        return ConfigBeanFactory.create(config.resolveWith(defaultCfg), MeetingConfig.class);
    }
}
