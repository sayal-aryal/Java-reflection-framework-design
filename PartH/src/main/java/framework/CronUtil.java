package framework;


import java.util.Calendar;
import java.util.Date;

public class CronUtil {
    public static long getNextDelay(String cronExpression) {
        String[] cronParts = cronExpression.split(" ");

        if (cronParts.length != 2) {
            throw new IllegalArgumentException("Invalid cron expression: " + cronExpression);
        }

        String secondsStr = cronParts[0];
        String minutesStr = cronParts[1];

        int seconds = parseCronValue(secondsStr, 0, 59);
        int minutes = parseCronValue(minutesStr, 0, 59);

        Calendar current = Calendar.getInstance();
        current.setTime(new Date());
        current.set(Calendar.MILLISECOND, 0);

        Calendar nextExecution = (Calendar) current.clone();
        nextExecution.set(Calendar.SECOND, seconds);
        nextExecution.set(Calendar.MINUTE, minutes);

        if (nextExecution.before(current)) {
            nextExecution.add(Calendar.MINUTE, 1);
        }

        return nextExecution.getTimeInMillis() - current.getTimeInMillis();
    }

    public static long getFixedRate(String cronExpression) {
        String[] cronParts = cronExpression.split(" ");

        if (cronParts.length != 2) {
            throw new IllegalArgumentException("Invalid cron expression: " + cronExpression);
        }

        String secondsStr = cronParts[0];
        String minutesStr = cronParts[1];

        int seconds = parseCronValue(secondsStr, 0, 59);
        int minutes = parseCronValue(minutesStr, 0, 59);

        long fixedRateInMilliSeconds = seconds * 1000 + minutes * 60 * 1000;

        return fixedRateInMilliSeconds;
    }

    private static int parseCronValue(String value, int minValue, int maxValue) {
        if (value.equals("*")) {
            return minValue;
        }

        int cronValue = Integer.parseInt(value);
        if (cronValue < minValue || cronValue > maxValue) {
            throw new IllegalArgumentException("Invalid cron expression value: " + value);
        }

        return cronValue;
    }
}

