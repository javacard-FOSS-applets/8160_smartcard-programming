package application.crypto;

import application.card.JavaCardHelper;
import application.log.LogHelper;
import application.log.LogLevel;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Patrick on 24.06.2015.
 */
public class CryptographyHelper
{
    public static Result<ImportedKeys> readKeysFromFile(Path filePath)
    {
        if (!Files.exists(filePath))
        {
            LogHelper.log(LogLevel.FAILURE, "Reading %s failed. File doesn't exists.", filePath);
            return new ErrorResult<>("Reading %s failed. File %s doesn't exists.", filePath);
        }

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(filePath.toString()));
            BigInteger privateMod = CryptographyHelper.readLineAsBigInteger(br);
            BigInteger privateExp = CryptographyHelper.readLineAsBigInteger(br);
            BigInteger publicMod = CryptographyHelper.readLineAsBigInteger(br);
            BigInteger publicExp = CryptographyHelper.readLineAsBigInteger(br);

            return new SuccessResult<>(new ImportedKeys(privateMod, privateExp, publicMod, publicExp));

        }
        catch (IOException e)
        {
            LogHelper.log(LogLevel.FAILURE, "Reading line from %s failed", filePath);
            return new ErrorResult<>("Reading %s failed. Please check that %s matches the requirements.", filePath);
        }
    }

    /**
     * Executes readline with the given buffered reader
     * Converts the read string to an BigInteger
     * @param br reader of the keyfile
     * @return Line as BigInteger
     * @throws IOException thrown if something went wrong with readline()
     * @throws NumberFormatException thrown if read line can't be converted to a BigInteger
     */
    public static BigInteger readLineAsBigInteger(BufferedReader br) throws IOException, NumberFormatException
    {
        String str = br.readLine();
        if (str == null)
        {
            throw new IOException();
        }

        return new BigInteger(str);
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
