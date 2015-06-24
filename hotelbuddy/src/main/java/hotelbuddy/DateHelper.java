package hotelbuddy;

/**
 * Created by Georg on 24.06.2015.
 */
public class DateHelper
{
    public static byte yearDifference(byte[] today, byte[] birthDay)
    {
        byte age = (byte) (today[3] - birthDay[3]);

        if (today[2] > birthDay[2])
        {
            age = (byte) (100 + age);
        }

        if (today[1] < birthDay[1])
        {
            return (byte) (age - 1);
        }

        if (today[1] > birthDay[1])
        {
            return age;
        }

        if (today[0] < birthDay[0])
        {
            return (byte) (age - 1);
        }

        return age;
    }

    public static boolean checkDate(byte[] message)
    {
        // Check Day Range
        if (message[0] < 1 || message[0] > 31)
        {
            return false;
        }

        // Check Month Range
        if (message[1] < 1 || message[1] > 12)
        {
            return false;
        }

        // Check Century Range
        if (message[2] < 19 || message[2] > 20)
        {
            return false;
        }

        // Check Year Range
        if (message[3] < 0 || message[3] > 99)
        {
            return false;
        }
        return true;
    }
}
