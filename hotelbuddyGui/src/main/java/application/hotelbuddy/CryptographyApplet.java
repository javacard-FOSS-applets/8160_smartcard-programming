package application.hotelbuddy;

import application.card.JavaCardHelper;
import application.crypto.RSACryptographyHelper;
import application.log.LogHelper;
import application.log.LogLevel;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;

/**
 * Created by Patrick on 23.06.2015.
 */
public class CryptographyApplet
{
    private static byte CLA = (byte) 0x43;
    private static byte INS_ExportMod = (byte) 0xF0;
    private static byte INS_ExportExp = (byte) 0xF2;
    private static byte INS_ImportMod = (byte) 0xE0;
    private static byte INS_ImportExp = (byte) 0xE2;

    public static Result<Boolean> setupRSACryptographyHelper()
    {
        JavaCardHelper.selectApplet("Cryptography");

        Result<byte[]> exportModResult = JavaCardHelper.sendCommand(CLA, INS_ExportMod);
        if (!exportModResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Export of modulus failed.");
            return new ErrorResult<>(exportModResult.getErrorMessage());
        }

        Result<byte[]> expResult = JavaCardHelper.sendCommand(CLA, INS_ExportExp);
        if (!expResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Export of exponent failed.");
            return new ErrorResult<>(expResult.getErrorMessage());
        }

        RSACryptographyHelper.current().initialize();
        RSACryptographyHelper.current().importPublicKey(exportModResult.getData(), expResult.getData());

        byte[] myMod = RSACryptographyHelper.current().getPublicMod();
        byte[] myMod2 = new byte[myMod.length - 1];
        System.arraycopy(myMod, 1, myMod2, 0, myMod2.length);

        Result<byte[]> importModResult = JavaCardHelper.sendCommand(CLA, INS_ImportMod, myMod2, (byte) 0x00);
        if (!importModResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Import of modulus failed.");
            return new ErrorResult<>(importModResult.getErrorMessage());
        }

        byte[] myExp = RSACryptographyHelper.current().getPublicExp();
        Result<byte[]> importExpResult = JavaCardHelper.sendCommand(CLA, INS_ImportExp, myExp, (byte) 0x00);
        if (!importExpResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Import of exponent failed.");
            return new ErrorResult<>(importExpResult.getErrorMessage());
        }

        LogHelper.log(LogLevel.INFO, "Exported my public key");
        return new SuccessResult<>(true);
    }
}
