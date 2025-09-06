package apicela.notstagram.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateUtils {

    public static Date minutesFromNow(int minutes) {
        return Date.from(Instant.now().plus(minutes, ChronoUnit.MINUTES));
    }

    public static boolean isExpired(Date expiration) {
        return expiration.before(new Date());
    }
}
