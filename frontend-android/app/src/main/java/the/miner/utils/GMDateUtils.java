package the.miner.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Provide method for handling date
 *
 * @see https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
 */
public final class GMDateUtils {

    /**
     * Convert string to date
     *
     * @param str    date string
     * @param format date format
     * @return Date object
     */
    public static Date stringToDate(String str, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        try {
            return formatter.parse(str);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Convert date to string
     *
     * @param date date
     * @return date string
     */
    public static String dateToString(Date date, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    /**
     * Get current date time
     *
     * @return now
     */
    public static Date now() {
        return Calendar.getInstance().getTime();
    }

    /**
     * Get current date time in string format
     *
     * @param format string format
     * @return current date time string
     */
    public static String nowString(String format) {
        return dateToString(now(), format);
    }
}
