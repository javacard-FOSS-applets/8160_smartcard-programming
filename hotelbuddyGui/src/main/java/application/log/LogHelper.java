package application.log;


import java.time.ZonedDateTime;

/**
 * Created by Patrick on 22.06.2015.
 */
public class LogHelper
{
    public static void log(LogLevel level, String message, Object... args)
    {
        logInternal(level, message, args);
    }

    public static void logException(Exception ex)
    {
        logInternal(LogLevel.ERROR, "%s: %s", ex.getClass().getName(), ex.getLocalizedMessage());
    }

    private static void logInternal(LogLevel level, String message, Object... args)
    {
        String str = String.format(message, args);
        System.out.println(String.format("%s [%s]: %s", ZonedDateTime.now().toLocalTime(), level.toString(), str));
    }
}
