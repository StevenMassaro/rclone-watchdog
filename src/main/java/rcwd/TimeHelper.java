package rcwd;

import org.apache.commons.lang.mutable.MutableLong;

import java.util.concurrent.TimeUnit;

public class TimeHelper {

    public static String elapsedTimeToHumanString(long startTime){
        return elapsedTimeToHumanString(startTime, System.nanoTime());
    }

    public static String elapsedTimeToHumanString(long startTime, long endTime) {
        MutableLong elapsedTime = new MutableLong(endTime - startTime);
        String timeString = convertUnitToHumanString(TimeUnit.DAYS, elapsedTime);
        timeString += convertUnitToHumanString(TimeUnit.HOURS, elapsedTime);
        timeString += convertUnitToHumanString(TimeUnit.MINUTES, elapsedTime);
        timeString += convertUnitToHumanString(TimeUnit.SECONDS, elapsedTime);
        return timeString.replaceAll(" {2}", " ");
    }

    /**
     * @param timeUnit    destination time unit
     * @param elapsedTime elapsed time in nanoseconds
     */
    public static String convertUnitToHumanString(TimeUnit timeUnit, MutableLong elapsedTime) {
        String timeString = "";
        long convertedTime = timeUnit.convert(elapsedTime.longValue(), TimeUnit.NANOSECONDS);
        if (convertedTime > 0) {
            String timeUnitString = timeUnit.toString().toLowerCase();
            // if the value of this time unit is > 1, then the string should be plural, otherwise chop off the "s" on the end
            timeString += convertedTime
                    + " "
                    + (convertedTime > 1 ? timeUnitString : timeUnitString.substring(0, timeUnit.toString().length() - 1))
                    + " ";
            elapsedTime = new MutableLong(elapsedTime.longValue() - TimeUnit.NANOSECONDS.convert(convertedTime, timeUnit));
        }
        return timeString;
    }
}
