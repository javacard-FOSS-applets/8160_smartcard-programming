package application.hotelbuddy;

import application.card.JavaCardHelper;
import application.crypto.RSACryptographyHelper;
import application.log.LogHelper;
import application.log.LogLevel;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;

import java.time.LocalDate;

/**
 * Created by Patrick on 23.06.2015.
 */
public class IdentificationApplet
{
    private static final byte CLA = (byte) 0x49;
    private static final byte INS_SetName = (byte) 0xA0;
    private static final byte INS_GetName = (byte) 0xA1;
    private static final byte INS_SetBirthDay = (byte) 0xB0;
    private static final byte INS_GetBirthDay = (byte) 0xB1;
    private static final byte INS_Reset = (byte) 0xFF;

    public static Result<Boolean> reset()
    {
        JavaCardHelper.selectApplet("Identification");

        Result<byte[]> result = JavaCardHelper.sendCommand(CLA, INS_Reset);
        if (!result.isSuccess())
        {
            LogHelper.log(LogLevel.INFO, "Reset failed");
            return new ErrorResult<>(result.getErrorMessage());
        }

        return new SuccessResult<>(true);
    }

    public static Result<Boolean> setName(String name)
    {
        return setValue(name.getBytes(), INS_SetName, "Name");
    }

    public static Result<String> getName()
    {
        return getValue(INS_GetName, "Name");
    }

    public static Result<Boolean> setBirthDay(LocalDate date)
    {
        byte[] d = new byte[4];
        d[0] = (byte) date.getDayOfMonth();
        d[1] = (byte) date.getMonth().getValue();
        d[2] = (byte) (date.getYear() / 100);
        d[3] = (byte) (date.getYear() % 10);

        return setValue(d, INS_SetBirthDay, "Birthday");
    }

    public static Result<String> getBirthDay()
    {
        return getValue(INS_GetBirthDay, "BirthDay");
    }

    private static Result<Boolean> setValue(byte[] d, byte ins, String field)
    {
        JavaCardHelper.selectApplet("Identification");

        Result<byte[]> encryptedMessage = RSACryptographyHelper.current().encrypt(d);
        if (!encryptedMessage.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Encryption of %s failed", field);
            return new ErrorResult<>(encryptedMessage.getErrorMessage());
        }

        Result<byte[]> result = JavaCardHelper.sendCommand(CLA, ins, encryptedMessage.getData(), (byte) 0x04);

        if (!result.isSuccess())
        {
            LogHelper.log(LogLevel.INFO, "%s couldn't be set.", field);
            return new ErrorResult<>(result.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "%s successfull set", field);
        return new SuccessResult<>(true);
    }

    private static Result<String> getValue(byte ins, String field)
    {
        JavaCardHelper.selectApplet("Identification");

        Result<byte[]> encryptedMessage = JavaCardHelper.sendCommand(CLA, ins, new byte[0], (byte) 0x00);

        if (!encryptedMessage.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Could't receive %s", field);
            return new ErrorResult<>(encryptedMessage.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "%s successfull received", field);

        Result<String> decryptedMessage = RSACryptographyHelper.current().decrypt(encryptedMessage.getData());
        if (!decryptedMessage.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Decryption of %s failed", field);
            return new ErrorResult<>(decryptedMessage.getErrorMessage());
        }

        return new SuccessResult<>(decryptedMessage.getData());
    }
}
