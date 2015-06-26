package application.hotelbuddy;

import application.card.JavaCardHelper;
import application.crypto.CryptographyHelper;
import application.crypto.ImportedKeys;
import application.crypto.RSACryptographyHelper;
import application.log.LogHelper;
import application.log.LogLevel;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;
import common.TerminalPaths;

import java.math.BigInteger;
import java.nio.file.Path;

/**
 * Created by Patrick on 23.06.2015.
 */
public class CryptographyApplet
{
    private static final String AppletName = "Cryptography";

    private static final byte CLA = (byte) 0x43;
    private static final byte INS_ImportCardPrivateMod = (byte) 0xF0;
    private static final byte INS_ImportCardPrivateExp = (byte) 0xF1;
    private static final byte INS_ImportCardPublicMod = (byte) 0xF2;
    private static final byte INS_ImportCardPublicExp = (byte) 0xF3;
    private static final byte INS_ExportCardPublicMod = (byte) 0xF4;
    private static final byte INS_ExportCardPublicExp = (byte) 0xF5;

    private static final byte INS_ImportTerminalPublicMod = (byte) 0xE0;
    private static final byte INS_ImportTerminalPublicExp = (byte) 0xE1;

    private static final Path CardKeyFilePath = TerminalPaths.CardKeyPath;

    /**
     * Exports the public key of the terminal to the card
     * The key is received from RSACryptographyHelper
     *
     * @return result of the operation
     */
    public static Result<Boolean> exportTerminalPublicKeyToCard()
    {
        Result<Boolean> selectResult = JavaCardHelper.selectApplet(AppletName);
        if (!selectResult.isSuccess())
        {
            return selectResult;
        }

        return CryptographyHelper.exportKeyToCard(
                CLA,
                RSACryptographyHelper.current().getPublicMod(),
                INS_ImportTerminalPublicMod,
                RSACryptographyHelper.current().getPublicExp(),
                INS_ImportTerminalPublicExp);
    }

    /**
     * Imports the public key from the card into the RSACryptographyHelper
     *
     * @return result of the operation
     */
    public static Result<Boolean> importPublicKeyFromCard()
    {
        Result<Boolean> selectResult = JavaCardHelper.selectApplet(AppletName);
        if (!selectResult.isSuccess())
        {
            return selectResult;
        }

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

        byte[] modulus = CryptographyHelper.addLeadingZero(exportModResult.get());
        byte[] exponent = CryptographyHelper.addLeadingZero(exportExponentResult.get());

        RSACryptographyHelper.current().setCardPublicKey(new BigInteger(modulus), new BigInteger(exponent));

        return new SuccessResult<>(true);
    }

    /**
     * loads the card keys from CardKeyFilePath
     * exports the keys to the card
     *
     * @return result of the operation
     */
    public static Result<Boolean> loadAndExportCardKeysFromFile()
    {
        Result<ImportedKeys> readResult = CryptographyHelper.readKeysFromFile(CardKeyFilePath);
        if (!readResult.isSuccess())
        {
            return new ErrorResult<>(readResult.getErrorMessage());
        }

        Result<Boolean> exportToCartResult = CryptographyHelper.exportKeyToCard(
                CLA,
                readResult.get().getPrivateMod().toByteArray(),
                INS_ImportCardPrivateMod,
                readResult.get().getPrivateExp().toByteArray(),
                INS_ImportCardPrivateExp);
        if (!exportToCartResult.isSuccess())
        {
            return exportToCartResult;
        }

        return CryptographyHelper.exportKeyToCard(
                CLA,
                readResult.get().getPublicMod().toByteArray(),
                INS_ImportCardPublicMod,
                readResult.get().getPublicExp().toByteArray(),
                INS_ImportCardPublicExp);
    }
}
