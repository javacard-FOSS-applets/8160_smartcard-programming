package application.hotelbuddy;

import application.card.JavaCardHelper;
import application.log.LogHelper;
import application.log.LogLevel;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;

import javax.security.auth.login.LoginException;
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
    private static final byte INS_SetCarId = (byte) 0xC0;
    private static final byte INS_GetCarId = (byte) 0xC1;
    private static final byte INS_SetSafePin = (byte) 0xD0;
    private static final byte INS_CheckSafePin = (byte) 0xD1;

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
            return new SuccessResult<>(true);
        }

        Result<byte[]> result = sendValue(name.getBytes(), INS_SetName, "Name");
        return !result.isSuccess() ? new ErrorResult<>(result.getErrorMessage()) : new SuccessResult<>(true);
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
     * Sends the given carid to the card
     *
     * @param carId carId
     * @return result of the operation
     */
    public static Result<Boolean> setCarId(String carId)
    {
        if (carId.equals(""))
        {
            return new SuccessResult<>(true);
        }

        Result<byte[]> result = sendValue(carId.getBytes(), INS_SetCarId, "Car Id");
        return !result.isSuccess() ? new ErrorResult<>(result.getErrorMessage()) : new SuccessResult<>(true);
    }

    /**
     * receives the carId from the card
     *
     * @return result of the operation
     */
    public static Result<String> getCarId()
    {
        Result<byte[]> result = getValue(INS_GetCarId, "Car Id");
        if (!result.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Invalid PIN format");
            return new ErrorResult<>(result.getErrorMessage());
        }

        return new SuccessResult<>(new String(result.get()));
    }

    /**
     * Sends the given carid to the card
     *
     * @param safePin safePin
     * @return result of the operation
     */
    public static Result<Boolean> setSafePin(String safePin)
    {
        if (safePin.length() < 4 || safePin.length() > 4)
        {
            LogHelper.log(LogLevel.FAILURE, "Invalid PIN format");
            return new ErrorResult<>("Safe PIN needs to be 4 digits long");
        }

        byte[] pin = ConvertSafePin(safePin);
        Result<byte[]> result = sendValue(pin, INS_SetSafePin, "Safe PIN");
        return !result.isSuccess() ? new ErrorResult<>(result.getErrorMessage()) : new SuccessResult<>(true);
    }

    /**
     * sends the given safe pin to the card to check it
     *
     * @return result of the operation
     */
    public static Result<Boolean> checkSafePin(String safePin)
    {
        if (safePin.length() < 4 || safePin.length() > 4)
        {
            return new ErrorResult<>("Safe PIN needs to be 4 digits long");
        }

        byte[] pin = ConvertSafePin(safePin);
        Result<byte[]> result = sendValue(pin, INS_CheckSafePin, "Safe PIN");
        if (!result.isSuccess())
        {
            return new ErrorResult<>(result.getErrorMessage());
        }

        if (result.get()[0] != 0x01)
        {
            LogHelper.log(LogLevel.FAILURE, "Wrong Safe PIN!");
            return new ErrorResult<>("Wrong Safe PIN!");
        }

        return new SuccessResult<>(true);
    }

    private static byte[] ConvertSafePin(String safePin)
    {
        byte[] pin = new byte[4];
        for (int i = 0; i < 4; i++)
        {
            pin[i] = (byte) Integer.parseInt(Character.toString(safePin.charAt(i)));
        }
        return pin;
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
            return new SuccessResult<>(true);
        }

        byte[] d = new byte[4];
        d[0] = (byte) date.getDayOfMonth();
        d[1] = (byte) date.getMonth().getValue();
        d[2] = (byte) (date.getYear() / 100);
        d[3] = (byte) (date.getYear() % 100);

        Result<byte[]> result = sendValue(d, INS_SetBirthDay, "Birthday");
        return !result.isSuccess() ? new ErrorResult<>(result.getErrorMessage()) : new SuccessResult<>(true);
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
    private static Result<byte[]> sendValue(byte[] data, byte ins, String desc)
    {
        Result<Boolean> selectResult = JavaCardHelper.selectApplet(AppletName);
        if (!selectResult.isSuccess())
        {
            return new ErrorResult<>(selectResult.getErrorMessage());
        }

        Result<byte[]> result = JavaCardHelper.sendCommand(CLA, ins, data, (byte) 0x00);

        if (!result.isSuccess())
        {
            LogHelper.log(LogLevel.INFO, "%s couldn't be send", desc);
            return new ErrorResult<>(result.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "%s successfull send", desc);
        return result;
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

        Result<byte[]> result = JavaCardHelper.sendCommand(CLA, ins);

        if (!result.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Could't receive %s", desc);
            return new ErrorResult<>(result.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "%s successfull received", desc);
        return result;
    }
}
