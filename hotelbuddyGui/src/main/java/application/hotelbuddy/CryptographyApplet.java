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

    public static Result<Boolean> setupRSACryptographyHelper()
    {
        JavaCardHelper.selectApplet("Cryptography");

        Result<byte[]> modResult = JavaCardHelper.sendCommand(CLA, INS_ExportMod, new byte[0], (byte) 0x04);
        if (!modResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Export of modulus failed.");
            return new ErrorResult<>(modResult.getErrorMessage());
        }

        Result<byte[]> expResult = JavaCardHelper.sendCommand(CLA, INS_ExportExp, new byte[0], (byte) 0x04);
        if (!expResult.isSuccess())
        {
            LogHelper.log(LogLevel.FAILURE, "Export of exponent failed.");
            return new ErrorResult<>(expResult.getErrorMessage());
        }

        RSACryptographyHelper.current().initialize();
        RSACryptographyHelper.current().importPublicKey(modResult.getData(), expResult.getData());

        return new SuccessResult<>(true);
    }
}
