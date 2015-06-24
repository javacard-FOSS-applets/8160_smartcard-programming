package application.hotelbuddy;

import application.card.JavaCardHelper;
import application.crypto.RSACryptographyHelper;
import application.log.LogHelper;
import application.log.LogLevel;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;
import sun.security.rsa.RSAPrivateKeyImpl;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * Created by Patrick on 23.06.2015.
 */
public class CryptographyApplet
{
    private static byte CLA = (byte) 0x43;
    private static byte INS_ImportCardPrivateMod = (byte) 0xF0;
    private static byte INS_ImportCardPrivateExp = (byte) 0xF1;
    private static byte INS_ImportCardPublicMod = (byte) 0xF2;
    private static byte INS_ImportCardPublicExp = (byte) 0xF3;
    private static byte INS_ExportCardPublicMod = (byte) 0xF4;
    private static byte INS_ExportCardPublicExp = (byte) 0xF5;

    private static byte INS_ImportTerminalPublicMod = (byte) 0xE0;
    private static byte INS_ImportTerminalPublicExp = (byte) 0xE1;

    public static Result<Boolean> importCardPublicKey()
    {
        Result<byte[]> exportModResult = JavaCardHelper.sendCommand(CLA, INS_ExportCardPublicMod);
        if (!exportModResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Export of modulus failed.");
            return new ErrorResult<>(exportModResult.getErrorMessage());
        }

        Result<byte[]> expResult = JavaCardHelper.sendCommand(CLA, INS_ExportCardPublicExp);
        if (!expResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Export of exponent failed.");
            return new ErrorResult<>(expResult.getErrorMessage());
        }

        RSACryptographyHelper.current().initialize();
        RSACryptographyHelper.current().setCardPublicKey(new BigInteger(exportModResult.getData()), new BigInteger(expResult.getData()));

        return new SuccessResult<>(true);
    }

    public static Result<Boolean> setupCardKey()
    {
        Result<Boolean> loadCardKeyFromFileResult = loadCardKeyFromFile();
        if (!loadCardKeyFromFileResult.isSuccess())
        {
            return new ErrorResult<>(loadCardKeyFromFileResult.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "Setup card keys done");
        return loadCardKeyFromFileResult;
    }

    private static Result<Boolean> loadCardKeyFromFile()
    {
        BigInteger privateMod, privateExp, publicMod, publicExp;

        if (!Files.exists(Paths.get("cardKey.txt")))
        {
            LogHelper.log(LogLevel.FAILURE, "Reading cardKey failed. File doesn't exists.");
            return new ErrorResult<>("Reading cardKey failed. File cardKey.txt doesn't exists.");
        }

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(new File("cardKey.txt")));
            privateMod = readLine(br);
            privateExp = readLine(br);
            publicMod = readLine(br);
            publicExp = readLine(br);
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

        RSAPrivateKey privateKey = null;
        RSAPublicKey publicKey = null;
        try
        {
            KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");

            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(privateMod, privateExp);
            privateKey = (RSAPrivateKey) rsaKeyFactory.generatePrivate(keySpec);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(publicMod, publicExp);
            publicKey = (RSAPublicKey) rsaKeyFactory.generatePublic(spec);
        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
        }

        return exportCardKeyToCard(privateKey, publicKey);
    }

    private static Result<Boolean> exportCardKeyToCard(RSAPrivateKey privateKey, RSAPublicKey publicKey)
    {
        JavaCardHelper.selectApplet("Cryptography");

        Result<Boolean> importPrivateKeyToCardResult = importKeyToCard(
                privateKey.getModulus().toByteArray(),
                INS_ImportCardPrivateMod,
                privateKey.getPrivateExponent().toByteArray(),
                INS_ImportCardPrivateExp);
        if (!importPrivateKeyToCardResult.isSuccess())
        {
            return new ErrorResult<>(importPrivateKeyToCardResult.getErrorMessage());
        }

        Result<Boolean> importPublicKeyToCardResult = importKeyToCard(
                publicKey.getModulus().toByteArray(),
                INS_ImportCardPublicMod,
                publicKey.getPublicExponent().toByteArray(),
                INS_ImportCardPublicExp);
        if (!importPublicKeyToCardResult.isSuccess())
        {
            return new ErrorResult<>(importPublicKeyToCardResult.getErrorMessage());
        }

        return new SuccessResult<>(true);
    }

    private static Result<Boolean> importKeyToCard(byte[] modulus, byte insMod, byte[] exponent, byte insExp)
    {
        byte[] mod = modulus;
        if (modulus[0] == 0)
        {
            mod = new byte[modulus.length - 1];
            System.arraycopy(modulus, 1, mod, 0, mod.length);
        }

        Result<byte[]> importModResult = JavaCardHelper.sendCommand(CLA, insMod, mod, (byte) 0x00);
        if (!importModResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Import of modulus failed.");
            return new ErrorResult<>(importModResult.getErrorMessage());
        }

        byte[] exp = exponent;
        if (exponent[0] == 0)
        {
            exp = new byte[exponent.length - 1];
            System.arraycopy(exponent, 1, exp, 0, exp.length);
        }

        Result<byte[]> importExpResult = JavaCardHelper.sendCommand(CLA, insExp, exp, (byte) 0x00);
        if (!importExpResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Import of exponent failed.");
            return new ErrorResult<>(importExpResult.getErrorMessage());
        }

        return new SuccessResult<>(true);
    }

    public static Result<Boolean> loadTerminalKeys()
    {
        RSACryptographyHelper.current().initialize();

        Result<Boolean> importTerminalKeyFromFileResult = importTerminalKeyFromFile();
        if (!importTerminalKeyFromFileResult.isSuccess())
        {
            return new ErrorResult<>(importTerminalKeyFromFileResult.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "Loaded terminal key");

        return new SuccessResult<>(true);
    }

    private static Result<Boolean> importTerminalKeyFromFile()
    {
        BigInteger privateMod, privateExp, publicMod, publicExp;

        if (!Files.exists(Paths.get("guiKey.txt")))
        {
            LogHelper.log(LogLevel.FAILURE, "Reading GuiKey failed. File doesn't exists.");
            return new ErrorResult<>("Reading GuiKey failed. File guiKey.txt doesn't exists.");
        }

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(new File("guiKey.txt")));
            privateMod = readLine(br);
            privateExp = readLine(br);
            publicMod = readLine(br);
            publicExp = readLine(br);
        }
        catch (FileNotFoundException e)
        {
            LogHelper.log(LogLevel.FAILURE, "Reading GuiKey failed. File doesn't exists.");
            return new ErrorResult<>("Reading GuiKey failed. File guiKey.txt doesn't exists.");
        }
        catch (IOException e)
        {
            LogHelper.log(LogLevel.FAILURE, "Reading line from GuiKey.txt failed");
            return new ErrorResult<>("Reading GuiKey failed. Please check that guiKey.txt matches the requirements.");
        }

        RSACryptographyHelper.current().setTerminalKeys(privateMod, privateExp, publicMod, publicExp);
        return new SuccessResult<>(true);
    }

    public static Result<Boolean> exportTerminalPublicKeyToCard()
    {
        JavaCardHelper.selectApplet("Cryptography");

        Result<Boolean> importPrivateKeyToCardResult = importKeyToCard(
                RSACryptographyHelper.current().getPublicMod(),
                INS_ImportTerminalPublicMod,
                RSACryptographyHelper.current().getPublicExp(),
                INS_ImportTerminalPublicExp);
        if (!importPrivateKeyToCardResult.isSuccess())
        {
            return new ErrorResult<>(importPrivateKeyToCardResult.getErrorMessage());
        }

        return new SuccessResult<>(true);
    }

    private static BigInteger readLine(BufferedReader br) throws IOException
    {
        String str = br.readLine();
        if (str == null)
        {
            throw new IOException();
        }

        return new BigInteger(str);
    }
}
