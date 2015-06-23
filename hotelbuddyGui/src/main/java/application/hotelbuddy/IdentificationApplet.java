package application.hotelbuddy;

import application.card.JavaCardHelper;
import application.crypto.RSACryptographyHelper;
import application.log.LogHelper;
import application.log.LogLevel;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;

/**
 * Created by Patrick on 23.06.2015.
 */
public class IdentificationApplet
{
    private static byte CLA = (byte) 0x49;
    private static byte INS_SetName = (byte) 0xA0;

    public static Result<Boolean> setName(String name)
    {
        JavaCardHelper.selectApplet("Identification");

        Result<byte[]> encryptedName = RSACryptographyHelper.current().encrypt(name);
        if (!encryptedName.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Encryption of name failed");
            return new ErrorResult<>(encryptedName.getErrorMessage());
        }

        Result<byte[]> expResult = JavaCardHelper.sendCommand(CLA, INS_SetName, encryptedName.getData(), (byte) 0x04);

        if (!expResult.isSuccess())
        {
            LogHelper.log(LogLevel.INFO, "Name couldn't be set.");
            return new ErrorResult<>(expResult.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "Name successfull set");
        return new SuccessResult<>(true);
    }
}
