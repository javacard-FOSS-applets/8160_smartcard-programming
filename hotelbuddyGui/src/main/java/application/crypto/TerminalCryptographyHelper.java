package application.crypto;

import application.log.LogHelper;
import application.log.LogLevel;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Patrick on 24.06.2015.
 */
public class TerminalCryptographyHelper
{
    private static final Path TerminalKeyPath = Paths.get("terminalKey.txt");

    /**
     * loads the card keys from CardKeyFilePath
     * and sets them into the RSACryptographyHelper
     *
     * @return result of the operation
     */
    public static Result<Boolean> initializeTerminalKeys()
    {
        Result<Boolean> importTerminalKeyFromFileResult = importTerminalKeyFromFile();
        if (!importTerminalKeyFromFileResult.isSuccess())
        {
            return new ErrorResult<>(importTerminalKeyFromFileResult.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "Loaded terminal key");

        return new SuccessResult<>(true);
    }

    /**
     * loads the card keys from CardKeyFilePath
     * and sets them into the RSACryptographyHelper
     *
     * @return result of the operation
     */
    private static Result<Boolean> importTerminalKeyFromFile()
    {
        BigInteger privateMod, privateExp, publicMod, publicExp;

        if (!Files.exists(TerminalKeyPath))
        {
            LogHelper.log(LogLevel.FAILURE, "Reading %s failed. File doesn't exists.", TerminalKeyPath.toString());
            return new ErrorResult<>("Reading GuiKey failed. File %s doesn't exists.", TerminalKeyPath.toString());
        }

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(TerminalKeyPath.toString()));
            privateMod = CryptographyHelper.readLine(br);
            privateExp = CryptographyHelper.readLine(br);
            publicMod = CryptographyHelper.readLine(br);
            publicExp = CryptographyHelper.readLine(br);
        }
        catch (FileNotFoundException e)
        {
            LogHelper.log(LogLevel.FAILURE, "Reading %s failed. File doesn't exists.", TerminalKeyPath.toString());
            return new ErrorResult<>("Reading GuiKey failed. File %s doesn't exists.", TerminalKeyPath.toString());
        }
        catch (IOException e)
        {
            LogHelper.log(LogLevel.FAILURE, "Reading line from %s failed", TerminalKeyPath.toString());
            return new ErrorResult<>("Reading GuiKey failed. Please check that %s matches the requirements.", TerminalKeyPath.toString());
        }

        RSACryptographyHelper.current().setTerminalKeys(privateMod, privateExp, publicMod, publicExp);
        return new SuccessResult<>(true);
    }
}
