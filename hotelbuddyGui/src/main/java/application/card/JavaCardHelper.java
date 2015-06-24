package application.card;

import application.log.LogHelper;
import application.log.LogLevel;
import common.Result;
import opencard.opt.terminal.ISOCommandAPDU;

/**
 * Created by Patrick on 23.06.2015.
 */
public class JavaCardHelper
{
    public static Result<byte[]> selectApplet(String appletId)
    {
        ISOCommandAPDU command = ApduHelper.getSelectCommand(appletId);
        Result<byte[]> selectResult = JavaCard.current().sendCommand(command);

        if (!selectResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Could not select applet: %s", appletId);
            return selectResult;
        }

        LogHelper.log(LogLevel.INFO, "Applet %s selected", appletId);
        return selectResult;
    }

    public static Result<byte[]> sendCommand(byte cla, byte ins, byte[] content, byte answerLength)
    {
        ISOCommandAPDU command = ApduHelper.getCommand(cla, ins, content, answerLength);
        return JavaCard.current().sendCommand(command);
    }

    public static Result<byte[]> sendCommand(byte cla, byte ins)
    {
        return sendCommand(cla, ins, new byte[0], (byte) 0x00);
    }
}
