package application.card;

import application.log.LogHelper;
import application.log.LogLevel;
import common.ByteHelper;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;
import opencard.core.service.CardRequest;
import opencard.core.service.SmartCard;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.HexString;
import opencard.opt.terminal.ISOCommandAPDU;
import opencard.opt.util.PassThruCardService;

/**
 * Created by Patrick on 23.06.2015.
 */
public class JavaCard implements IJavaCard
{
    private static IJavaCard instance = null;

    SmartCard card;

    private JavaCard()
    {
    }

    public static IJavaCard current()
    {
        return instance == null ? (instance = new JavaCard()) : instance;
    }

    @Override
    public Result<Boolean> connect()
    {
        LogHelper.log(LogLevel.INFO, "Setting up connection to smartcard");

        try
        {
            SmartCard.start();
        }
        catch (ClassNotFoundException ex)
        {
            LogHelper.logException(ex);
            return new ErrorResult<>("Terminal not available. Please check your opencard.properties file.");
        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
            return new ErrorResult<>("SmartCard couldn't be started.");
        }

        CardRequest cardRequest = new CardRequest(CardRequest.ANYCARD, null, PassThruCardService.class);
        cardRequest.setTimeout(1);

        try
        {
            card = SmartCard.waitForCard(cardRequest);
        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
            return new ErrorResult<>("No smartcard found.");
        }

        if (card == null)
        {
            LogHelper.log(LogLevel.WARNING, "No smartcard found");
            return new ErrorResult<>("No smartcard found.");
        }

        LogHelper.log(LogLevel.INFO, "Connection to smartcard established");
        return new SuccessResult<>(true);
    }

    @Override
    public Result<byte[]> sendCommand(ISOCommandAPDU command)
    {
        if (card == null)
        {
            LogHelper.log(LogLevel.WARNING, "No card available");
            return new ErrorResult<>("No card available. Please connect your card before sending a command.");
        }

        ISOCommandAPDU commandApdu;
        ResponseAPDU responseApdu;
        PassThruCardService passThru;

        try
        {
            passThru = (PassThruCardService) card.getCardService(PassThruCardService.class, true);
            commandApdu = command;

            LogHelper.log(LogLevel.INFO, "Sending %s", ByteHelper.ToHexString(commandApdu.getBuffer()));

            responseApdu = passThru.sendCommandAPDU(commandApdu);

            String status = HexString.hexifyShort(responseApdu.sw1(), responseApdu.sw2());
            if (!status.equals("9000"))
            {
                LogHelper.log(LogLevel.FAILURE, "Answer incorrect: %s", status);
                return new ErrorResult<>("Incorrect answer. Please check your command.");
            }

            return new SuccessResult<>(responseApdu.data());
        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
            return new ErrorResult<>("");
        }
    }
}
