package application.hotelbuddy;

import application.card.JavaCardHelper;
import application.crypto.CryptographyHelper;
import application.crypto.RSACryptographyHelper;
import application.log.LogHelper;
import application.log.LogLevel;
import com.sun.org.apache.xpath.internal.jaxp.JAXPVariableStack;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Patrick on 23.06.2015.
 */
public class CryptographyApplet
{
    private static final byte CLA = (byte) 0x43;
    private static final byte INS_ImportCardPrivateMod = (byte) 0xF0;
    private static final byte INS_ImportCardPrivateExp = (byte) 0xF1;
    private static final byte INS_ImportCardPublicMod = (byte) 0xF2;
    private static final byte INS_ImportCardPublicExp = (byte) 0xF3;
    private static final byte INS_ExportCardPublicMod = (byte) 0xF4;
    private static final byte INS_ExportCardPublicExp = (byte) 0xF5;

    private static final byte INS_ImportTerminalPublicMod = (byte) 0xE0;
    private static final byte INS_ImportTerminalPublicExp = (byte) 0xE1;

    private static final byte INS_Reset = (byte) 0xFF;

    private static final Path CardKeyFilePath = Paths.get("cardKey.txt");

    /**
     * Exports the public key of the terminal to the card
     * The key is received from RSACryptographyHelper
     * @return result of the operation
     */
    public static Result<Boolean> exportTerminalPublicKeyToCard()
    {
        JavaCardHelper.selectApplet("Cryptography");

        JavaCardHelper.sendCommand(CLA, INS_Reset);

        Result<Boolean> exportKeyToCardResult = exportKeyToCard(
                RSACryptographyHelper.current().getPublicMod(),
                INS_ImportTerminalPublicMod,
                RSACryptographyHelper.current().getPublicExp(),
                INS_ImportTerminalPublicExp);
        if (!exportKeyToCardResult.isSuccess())
        {
            return new ErrorResult<>(exportKeyToCardResult.getErrorMessage());
        }

        return new SuccessResult<>(true);
    }

    /**
     * Imports the public key from the card into the RSACryptographyHelper
     * @return result of the operation
     */
    public static Result<Boolean> importPublicKeyFromCard()
    {
        Result<byte[]> exportModResult = JavaCardHelper.sendCommand(CLA, INS_ExportCardPublicMod);
        if (!exportModResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Import of modulus from card failed.");
            return new ErrorResult<>(exportModResult.getErrorMessage());
        }

        Result<byte[]> exportExponentResult = JavaCardHelper.sendCommand(CLA, INS_ExportCardPublicExp);
        if (!exportExponentResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Import of exponent from failed.");
            return new ErrorResult<>(exportExponentResult.getErrorMessage());
        }

        byte[] modulus = CryptographyHelper.addLeadingZero(exportModResult.getData());
        byte[] exponent = CryptographyHelper.addLeadingZero(exportExponentResult.getData());

        RSACryptographyHelper.current().setCardPublicKey(new BigInteger(modulus), new BigInteger(exponent));

        return new SuccessResult<>(true);
    }

    /**
     * loads the card keys from CardKeyFilePath
     * exports the keys to the card
     * @return result of the operation
     */
    public static Result<Boolean> initializeCardKeys()
    {
        Result<Boolean> loadCardKeyFromFileResult = loadAndExportCardKeysFromFile();
        if (!loadCardKeyFromFileResult.isSuccess())
        {
            return new ErrorResult<>(loadCardKeyFromFileResult.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "Setup card keys done");
        return loadCardKeyFromFileResult;
    }

    /**
     * loads the card keys from CardKeyFilePath
     * exports the keys to the card
     * @return result of the operation
     */
    private static Result<Boolean> loadAndExportCardKeysFromFile()
    {
        BigInteger privateMod, privateExp, publicMod, publicExp;

        if (!Files.exists(CardKeyFilePath))
        {
            LogHelper.log(LogLevel.FAILURE, "Reading cardKey failed. File doesn't exists.");
            return new ErrorResult<>("Reading cardKey failed. File cardKey.txt doesn't exists.");
        }

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(CardKeyFilePath.toString()));
            privateMod = CryptographyHelper.readLine(br);
            privateExp = CryptographyHelper.readLine(br);
            publicMod = CryptographyHelper.readLine(br);
            publicExp = CryptographyHelper.readLine(br);
        }
        catch (FileNotFoundException e)
        {
            LogHelper.log(LogLevel.FAILURE, "Reading cardKey failed. File doesn't exists.");
            return new ErrorResult<>("Reading cardKey failed. File cardKey.txt doesn't exists.");
        }
        catch (IOException e)
        {
            LogHelper.log(LogLevel.FAILURE, "Reading line from cardKey.txt failed");
            return new ErrorResult<>("Reading cardKey failed. Please check that cardKey.txt matches the requirements.");
        }

        Result<Boolean> exportToCartResult = exportKeyToCard(privateMod.toByteArray(), INS_ImportCardPrivateMod, privateExp.toByteArray(), INS_ImportCardPrivateExp);
        if (!exportToCartResult.isSuccess())
        {
            return new ErrorResult<>(exportToCartResult.getErrorMessage());
        }

        exportToCartResult = exportKeyToCard(publicMod.toByteArray(), INS_ImportCardPublicMod, publicExp.toByteArray(), INS_ImportCardPublicExp);
        if (!exportToCartResult.isSuccess())
        {
            return new ErrorResult<>(exportToCartResult.getErrorMessage());
        }

        return new SuccessResult<>(true);
    }

    /**
     * Exports the given modulus and exponent to the card
     * The given instructions are used for the export
     * @param modulus modulus of the key
     * @param insMod instruction for the modulus
     * @param exponent exponent of the key
     * @param insExp instruction for the modulus
     * @return result of the operation
     */
    private static Result<Boolean> exportKeyToCard(byte[] modulus, byte insMod, byte[] exponent, byte insExp)
    {
        JavaCardHelper.selectApplet("Cryptography");

        Result<Boolean> importPrivateKeyToCardResult = CryptographyHelper.importKeyToCard(
                CLA,
                modulus,
                insMod,
                exponent,
                insExp);
        if (!importPrivateKeyToCardResult.isSuccess())
        {
            return new ErrorResult<>(importPrivateKeyToCardResult.getErrorMessage());
        }

        return new SuccessResult<>(true);
    }
}
