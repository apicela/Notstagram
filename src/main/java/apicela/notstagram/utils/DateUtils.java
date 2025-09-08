package apicela.notstagram.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateUtils {

    public static Date minutesFromNow(int minutes) {
        return Date.from(Instant.now().plus(minutes, ChronoUnit.MINUTES));
    }

    public static Date secondsFromNow(int seconds) {
        return Date.from(Instant.now().plus(seconds, ChronoUnit.SECONDS));
    }

    public static LocalDateTime minutesFromNowLocal(int minutes) {
        return LocalDateTime.now().plusMinutes(minutes);
    }

    public static LocalDateTime daysFromNowLocal(int days) {
        return LocalDateTime.now().plusDays(days);
    }

    public static LocalDateTime secondsFromNowLocal(int seconds) {
        return LocalDateTime.now().plusSeconds(seconds);
    }

    public static boolean isExpired(Date expiration) {
        return expiration.before(new Date());
    }
}
