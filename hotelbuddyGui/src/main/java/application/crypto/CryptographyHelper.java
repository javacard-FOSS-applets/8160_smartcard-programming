package application.crypto;

import application.card.JavaCardHelper;
import application.hotelbuddy.CryptographyApplet;
import application.log.LogHelper;
import application.log.LogLevel;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Created by Patrick on 24.06.2015.
 */
public class CryptographyHelper
{
    public static BigInteger readLine(BufferedReader br) throws IOException
    {
        String str = br.readLine();
        if (str == null)
        {
            throw new IOException();
        }

        return new BigInteger(str);
    }

    public static Result<Boolean> importKeyToCard(byte cla, byte[] modulus, byte insMod, byte[] exponent, byte insExp)
    {
        byte[] mod = stripLeadingZero(modulus);

        Result<byte[]> importModResult = JavaCardHelper.sendCommand(cla, insMod, mod, (byte) 0x00);
        if (!importModResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Import of modulus failed.");
            return new ErrorResult<>(importModResult.getErrorMessage());
        }

        byte[] exp = stripLeadingZero(exponent);

        Result<byte[]> importExpResult = JavaCardHelper.sendCommand(cla, insExp, exp, (byte) 0x00);
        if (!importExpResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Import of exponent failed.");
            return new ErrorResult<>(importExpResult.getErrorMessage());
        }

        return new SuccessResult<>(true);
    }

    public static byte[] stripLeadingZero(byte[] value)
    {
        byte[] result = value;
        if (value[0] == 0)
        {
            result = new byte[value.length - 1];
            System.arraycopy(value, 1, result, 0, result.length);
        }
        return result;
    }

    public static byte[] addLeadingZero(byte[] value)
    {
        byte[] result = new byte[value.length + 1];
        result[0] = 0;
        System.arraycopy(value, 0, result, 1, value.length);

        return result;
    }
}
