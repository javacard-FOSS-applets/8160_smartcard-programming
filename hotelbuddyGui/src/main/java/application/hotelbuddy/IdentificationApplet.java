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
    private static final String AppletName = "Identification";

    private static final byte CLA = (byte) 0x49;
    private static final byte INS_SetName = (byte) 0xA0;
    private static final byte INS_GetName = (byte) 0xA1;
    private static final byte INS_SetBirthDay = (byte) 0xB0;
    private static final byte INS_GetBirthDay = (byte) 0xB1;

    private static final byte INS_Reset = (byte) 0xFF;

    /**
     * Sends the given name to the card
     *
     * @param name name
     * @return result of the operation
     */
    public static Result<Boolean> setName(String name)
    {
        if (name.equals(""))
        {
            return new ErrorResult<>("Please enter a name.");
        }

        return setValue(name.getBytes(), INS_SetName, "Name");
    }

    /**
     * receives the name from the card
     *
     * @return result of the operation
     */
    public static Result<String> getName()
    {
        Result<byte[]> result = getValue(INS_GetName, "Name");
        if (!result.isSuccess())
        {
            return new ErrorResult<>(result.getErrorMessage());
        }

        return new SuccessResult<>(new String(result.get()));
    }

    /**
     * Sends the given date as birthday to the card
     *
     * @param date birthday
     * @return result of the operation
     */
    public static Result<Boolean> setBirthDay(LocalDate date)
    {
        if (date == null)
        {
            return new ErrorResult<>("Please enter a date of birth.");
        }

        byte[] d = new byte[4];
        d[0] = (byte) date.getDayOfMonth();
        d[1] = (byte) date.getMonth().getValue();
        d[2] = (byte) (date.getYear() / 100);
        d[3] = (byte) (date.getYear() % 100);

        return setValue(d, INS_SetBirthDay, "Birthday");
    }

    /**
     * Recevies the birthday from the card
     *
     * @return result of the operation, date-string formatted as DD.MM.YYYY
     */
    public static Result<String> getBirthDay()
    {
        Result<byte[]> result = getValue(INS_GetBirthDay, "BirthDay");
        if (!result.isSuccess())
        {
            return new ErrorResult<>(result.getErrorMessage());
        }

        int day = result.get()[0];
        int month = result.get()[1];
        int year = (result.get()[2] * 100) + result.get()[3];

        return new SuccessResult<>(String.format("%02d.%02d.%d", day, month, year));
    }

    /**
     * Resets the identification applet
     *
     * @return result of the operation
     */
    public static Result<Boolean> reset()
    {
        Result<Boolean> selectResult = JavaCardHelper.selectApplet(AppletName);
        if (!selectResult.isSuccess())
        {
            return selectResult;
        }

        Result<byte[]> result = JavaCardHelper.sendCommand(CLA, INS_Reset);
        if (!result.isSuccess())
        {
            LogHelper.log(LogLevel.INFO, "Reset failed");
            return new ErrorResult<>(result.getErrorMessage());
        }

        return new SuccessResult<>(true);
    }

    /**
     * Encrypts the given data and sends it to the card with the given instruction
     *
     * @param data data to encrypt and send
     * @param ins  instruction
     * @param desc description of the data, used for log and error-messages
     * @return result of the operation
     */
    private static Result<Boolean> setValue(byte[] data, byte ins, String desc)
    {
        Result<Boolean> selectResult = JavaCardHelper.selectApplet(AppletName);
        if (!selectResult.isSuccess())
        {
            return selectResult;
        }

        Result<byte[]> encryptedMessage = RSACryptographyHelper.current().encrypt(data);
        if (!encryptedMessage.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Encryption of %s failed", desc);
            return new ErrorResult<>(encryptedMessage.getErrorMessage());
        }

        Result<byte[]> result = JavaCardHelper.sendCommand(CLA, ins, encryptedMessage.get(), (byte) 0x04);

        if (!result.isSuccess())
        {
            LogHelper.log(LogLevel.INFO, "%s couldn't be set.", desc);
            return new ErrorResult<>(result.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "%s successfull set", desc);
        return new SuccessResult<>(true);
    }

    /**
     * Receives a value from the card with the given instruction
     *
     * @param ins  instruction
     * @param desc description of the data, used for log and error-messages
     * @return result of the operation
     */
    private static Result<byte[]> getValue(byte ins, String desc)
    {
        Result<Boolean> selectResult = JavaCardHelper.selectApplet(AppletName);
        if (!selectResult.isSuccess())
        {
            return new ErrorResult<>(selectResult.getErrorMessage());
        }

        Result<byte[]> encryptedMessage = JavaCardHelper.sendCommand(CLA, ins, new byte[0], (byte) 0x00);

        if (!encryptedMessage.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Could't receive %s", desc);
            return new ErrorResult<>(encryptedMessage.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "%s successfull received", desc);

        Result<byte[]> decryptedMessage = RSACryptographyHelper.current().decrypt(encryptedMessage.get());
        if (!decryptedMessage.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Decryption of %s failed", desc);
            return new ErrorResult<>(decryptedMessage.getErrorMessage());
        }

        return new SuccessResult<>(decryptedMessage.get());
    }
}
