package application.card;

import application.log.LogHelper;
import application.log.LogLevel;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;
import opencard.opt.terminal.ISOCommandAPDU;

/**
 * Created by Patrick on 23.06.2015.
 */
public class JavaCardHelper
{
    public static Result<Boolean> selectApplet(String appletId)
    {
        ISOCommandAPDU command = ApduHelper.getSelectCommand(appletId);
        Result<byte[]> selectResult = JavaCard.current().sendCommand(command);

        if (!selectResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Could not select applet: %s", appletId);
            return new ErrorResult<>(selectResult.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "%s Applet selected", appletId);
        return new SuccessResult<>(true);
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
