package br.techservice.audiooggtranscriberapp.util;

import java.time.Duration;

public class DurationUtils {

    private DurationUtils() {
    }

    public static String format(Duration duration) {
        long seconds = duration.toSeconds();
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        long millis = duration.toMillisPart();

        if (minutes > 0) {
            return String.format("%d min %d s", minutes, remainingSeconds);
        }
        return String.format("%d.%03d s", remainingSeconds, millis);
    }
}