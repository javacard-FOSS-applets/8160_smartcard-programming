package application.card;

import opencard.opt.terminal.ISOCommandAPDU;

/**
 * Created by Patrick on 22.06.2015.
 */
public class ApduHelper
{
    public static ISOCommandAPDU getSelectCommand(String appletId)
    {
        return new ISOCommandAPDU((byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, appletId.getBytes(), (byte) 0x00);
    }

    public static ISOCommandAPDU getCommand(byte classByte, byte instruction, byte[] content, byte answerLength)
    {
        return new ISOCommandAPDU(classByte, instruction, (byte) 0x00, (byte) 0x00, content, answerLength);
    }
}
