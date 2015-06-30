package hotelbuddy;

/**
 * Created by Georg on 24.06.2015.
 */
public class DateHelper
{
    /**
     * Length of th memory of a date.
     */
    public static final byte DATE_LENGTH = 0x04;

    /**
     * Calculates the year difference from the date inside the buffer at offset to the given birthday.
     *
     * @param startDateBuffer Memory containing the start date
     * @param offset Offset of the start date in the startDateBuffer
     * @param endDate End date for the calculation
     *
     * @return the year difference from start to end date.
     */
    public static byte yearDifference(byte[] startDateBuffer, int offset, byte[] endDate)
    {
        // Age by year difference
        byte age = (byte) (startDateBuffer[offset + 3] - endDate[3]);

        // Century difference?
        if (startDateBuffer[offset + 2] > endDate[2])
        {
            // Add century if necessary
            age = (byte) (100 + age);
        }

        if (startDateBuffer[offset + 1] < endDate[1])
        {
            // There was no birthday this year by month.
            return (byte) (age - 1);
        }

        if (startDateBuffer[offset + 1] > endDate[1])
        {
            // There was a birthday this year by month.
            return age;
        }

        if (startDateBuffer[offset] < endDate[0])
        {
            // There was no birthday this year by day.
            return (byte) (age - 1);
        }

        // There was a birthday this year by day.
        return age;
    }

    /**
     * Checks whether the given date in the buffer at offset is valid.
     *
     * @param buffer buffer containing the date to check
     * @param offset offset of the date to check
     *
     * @return true, if the date is valid. false otherwise.
     */
    public static boolean checkDate(byte[] buffer, int offset)
    {
        // Check Day Range
        if (buffer[offset] < 1 || buffer[offset] > 31)
        {
            return false;
        }

        // Check Month Range
        if (buffer[offset + 1] < 1 || buffer[offset + 1] > 12)
        {
            return false;
        }

        // Check Century Range
        if (buffer[offset + 2] < 19 || buffer[offset + 2] > 20)
        {
            return false;
        }

        // Check Year Range
        if (buffer[offset + 3] < 0 || buffer[offset + 3] > 99)
        {
            return false;
        }
        return true;
    }
}
