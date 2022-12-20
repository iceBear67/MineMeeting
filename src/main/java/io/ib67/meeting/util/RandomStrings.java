package io.ib67.meeting.util;

import java.util.concurrent.ThreadLocalRandom;

public class RandomStrings {
    public static String randomString(int len) {
        var sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append((char) ThreadLocalRandom.current().nextInt((int) 'a', (int) 'z'));
        }
        return sb.toString();
    }
}
