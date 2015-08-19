package application.card;

/**
 * Created by Patrick on 22.06.2015.
 */
public class ApduHelper
{
    public static HotelBuddyCommand getSelectCommand(String appletId)
    {
        return new HotelBuddyCommand((byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, appletId.getBytes(), (byte) 0x00);
    }

    public static HotelBuddyCommand getCommand(byte classByte, byte instruction, byte[] content, byte answerLength)
    {
        return new HotelBuddyCommand(classByte, instruction, (byte)0x00, (byte)0x00, content, answerLength);
    }

    public static HotelBuddyCommand getCommand(byte classByte, byte instruction, byte answerLength)
    {
        return new HotelBuddyCommand(classByte, instruction, (byte)0x00, (byte)0x00, answerLength);
    }
}
