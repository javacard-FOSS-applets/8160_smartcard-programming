package application.card;

import common.Result;
import opencard.opt.terminal.ISOCommandAPDU;

/**
 * Created by Patrick on 23.06.2015.
 */
public class JavaCardHelper
{
    public static void selectApplet(String appletId)
    {
        ISOCommandAPDU command = ApduHelper.getSelectCommand(appletId);
        JavaCard.current().sendCommand(command);
    }

    public static Result<byte[]> sendCommand(byte cla, byte ins, byte[] content, byte answerLength)
    {
        ISOCommandAPDU command = ApduHelper.getCommand(cla, ins, content, answerLength);
        return JavaCard.current().sendCommand(command);
    }
}
