package application.hotelbuddy;

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
    public static final int SafePinLength = 4;

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

        Result<byte[]> result =  CommonApplet.sendValue(AppletName, CLA, name.getBytes(), INS_SetName);
        return !result.isSuccess() ? new ErrorResult<>(result.getErrorMessage()) : new SuccessResult<>(true);
    }

    /**
     * receives the name from the card
     *
     * @return result of the operation
     */
    public static Result<String> getName()
    {
        Result<byte[]> result =  CommonApplet.sendValue(AppletName, CLA, INS_GetName);
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

        Result<byte[]> result =  CommonApplet.sendValue(AppletName, CLA, carId.getBytes(), INS_SetCarId);
        return !result.isSuccess() ? new ErrorResult<>(result.getErrorMessage()) : new SuccessResult<>(true);
    }

    /**
     * receives the carId from the card
     *
     * @return result of the operation
     */
    public static Result<String> getCarId()
    {
        Result<byte[]> result =  CommonApplet.sendValue(AppletName, CLA, INS_GetCarId);
        if (!result.isSuccess())
        {
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
        if (safePin.equals(""))
        {
            return new SuccessResult<>(true);
        }

        if (safePin.length() < SafePinLength || safePin.length() > SafePinLength)
        {
            LogHelper.log(LogLevel.FAILURE, "Invalid PIN format");
            return new ErrorResult<>(String.format("Safe PIN needs to be %s digits long", SafePinLength));
        }

        byte[] pin = ConvertSafePin(safePin);
        Result<byte[]> result =  CommonApplet.sendValue(AppletName, CLA, pin, INS_SetSafePin);
        return !result.isSuccess() ? new ErrorResult<>(result.getErrorMessage()) : new SuccessResult<>(true);
    }

    /**
     * sends the given safe pin to the card to check it
     *
     * @return result of the operation
     */
    public static Result<Boolean> checkSafePin(String safePin)
    {
        if (safePin.length() < SafePinLength || safePin.length() > SafePinLength)
        {
            LogHelper.log(LogLevel.FAILURE, "Invalid PIN format");
            return new ErrorResult<>(String.format("Safe PIN needs to be %s digits long", SafePinLength));
        }

        byte[] pin = ConvertSafePin(safePin);
        Result<byte[]> result =  CommonApplet.sendValue(AppletName, CLA, pin, INS_CheckSafePin);
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

        byte[] data = new byte[4];
        data[0] = (byte) date.getDayOfMonth();
        data[1] = (byte) date.getMonth().getValue();
        data[2] = (byte) (date.getYear() / 100);
        data[3] = (byte) (date.getYear() % 100);

        Result<byte[]> result = CommonApplet.sendValue(AppletName, CLA, data, INS_SetBirthDay);
        return !result.isSuccess() ? new ErrorResult<>(result.getErrorMessage()) : new SuccessResult<>(true);
    }

    /**
     * Recevies the birthday from the card
     *
     * @return result of the operation, date-string formatted as DD.MM.YYYY
     */
    public static Result<String> getBirthDay()
    {
        Result<byte[]> result =  CommonApplet.sendValue(AppletName, CLA, INS_GetBirthDay);
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
        return CommonApplet.reset(AppletName, CLA, INS_Reset);
    }


}
