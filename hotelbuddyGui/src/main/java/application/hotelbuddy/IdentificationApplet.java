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
    private static byte INS_GetName = (byte) 0xA1;

    public static Result<Boolean> setName(String name)
    {
        JavaCardHelper.selectApplet("Identification");

        Result<byte[]> encryptedName = RSACryptographyHelper.current().encrypt(name);
        if (!encryptedName.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Encryption of name failed");
            return new ErrorResult<>(encryptedName.getErrorMessage());
        }

        Result<byte[]> result = JavaCardHelper.sendCommand(CLA, INS_SetName, encryptedName.getData(), (byte) 0x04);

        if (!result.isSuccess())
        {
            LogHelper.log(LogLevel.INFO, "Name couldn't be set.");
            return new ErrorResult<>(result.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "Name successfull set");
        return new SuccessResult<>(true);
    }

    public static Result<String> getName()
    {
        JavaCardHelper.selectApplet("Identification");

        Result<byte[]> encryptedNameResult = JavaCardHelper.sendCommand(CLA, INS_GetName, new byte[0], (byte) 0x00);

        if (!encryptedNameResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Could't receive name");
            return new ErrorResult<>(encryptedNameResult.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "Name successfull received");

        Result<String> decryptedName = RSACryptographyHelper.current().decrypt(encryptedNameResult.getData());
        if (!decryptedName.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Decryption of name failed");
            return new ErrorResult<>(decryptedName.getErrorMessage());
        }

        return new SuccessResult<>(decryptedName.getData());
    }
}
