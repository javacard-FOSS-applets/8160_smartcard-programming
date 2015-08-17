package application.card;

import application.log.LogHelper;
import application.log.LogLevel;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;
import opencard.core.event.CTListener;
import opencard.core.event.CardTerminalEvent;
import opencard.core.event.EventGenerator;
import opencard.core.service.CardRequest;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.HexString;
import opencard.opt.util.PassThruCardService;

/**
 * Created by Patrick on 23.06.2015.
 */
public class JavaCard implements IJavaCard, CTListener
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
        if (card != null)
        {
            return new SuccessResult<>(true);
        }

        try
        {
            SmartCard.start();
            EventGenerator.getGenerator().addCTListener(this);
        }
        catch (ClassNotFoundException ex)
        {
            LogHelper.log(ex);
            shutdown();
            return new ErrorResult<>("Terminal not available. Please check your opencard.properties file.");
        }
        catch (Exception ex)
        {
            LogHelper.log(ex);
            shutdown();
            return new ErrorResult<>("SmartCard couldn't be started.");
        }

        LogHelper.log(LogLevel.INFO, "Setting up connection to smartcard");

        CardRequest cardRequest = new CardRequest(CardRequest.ANYCARD, null, PassThruCardService.class);
        cardRequest.setTimeout(1);

        try
        {
            card = SmartCard.waitForCard(cardRequest);
        }
        catch (Exception ex)
        {
            LogHelper.log(ex);
            shutdown();
            return new ErrorResult<>("No smartcard found.");
        }

        if (card == null)
        {
            LogHelper.log(LogLevel.WARNING, "No smartcard found");
            shutdown();
            return new ErrorResult<>("No smartcard found.");
        }

        LogHelper.log(LogLevel.INFO, "Connection to smartcard established");
        return new SuccessResult<>(true);
    }

    @Override
    public Result<byte[]> sendCommand(HotelBuddyCommand command)
    {
        if (card == null)
        {
            LogHelper.log(LogLevel.WARNING, "No card available");
            return new ErrorResult<>("No card available. Please connect your card before sending a command.");
        }

        try
        {
            PassThruCardService passThru = (PassThruCardService) card.getCardService(PassThruCardService.class, true);

            LogHelper.log(LogLevel.INFO, "Sending %s", HexString.hexifyShort(command.getCLA(), command.getINS()));
            ResponseAPDU responseApdu = passThru.sendCommandAPDU(command);

            String status = HexString.hexifyShort(responseApdu.sw1(), responseApdu.sw2());
            if (status.equals("6E00"))
            {
                LogHelper.log(LogLevel.FAILURE, "Unknown class byte %02x", command.getCLA());
                return new ErrorResult<>("Unknown class byte. Please check your command.");
            }
            else if (status.equals("6D00"))
            {
                LogHelper.log(LogLevel.FAILURE, "Unknown instruction byte %02x", command.getINS());
                return new ErrorResult<>("Incorrect instruction byte. Please check your command.");
            }
            else if (status.equals("6986"))
            {
                LogHelper.log(LogLevel.FAILURE, "Data already set or not available");
                return new ErrorResult<>("The data is already set. If you want to set new data, please reset the applet.");
            }
            else if (status.equals("6984"))
            {
                LogHelper.log(LogLevel.FAILURE, "Data invalid. Signature verification failed.");
                return new ErrorResult<>("Data invalid. Signature verification failed.");
            }
            else if (!status.equals("9000"))
            {
                LogHelper.log(LogLevel.FAILURE, "Answer incorrect: %s", status);
                return new ErrorResult<>("Incorrect answer. Please check your command.");
            }

            LogHelper.log(LogLevel.INFO, "Command successfull");
            byte[] data = responseApdu.data();
            return data == null ? new SuccessResult<>(new byte[0]) : new SuccessResult<>(data);
        }
        catch (Exception ex)
        {
            LogHelper.log(ex);
            return new ErrorResult<>(ex.getMessage());
        }
    }

    @Override
    public void shutdown()
    {
        LogHelper.log(LogLevel.INFO, "Shutdown initiated");

        try
        {
            if (card != null)
            {
                card.close();
                card = null;
            }

            SmartCard.shutdown();
        }
        catch (CardTerminalException ex)
        {
            LogHelper.log(ex);
        }
    }

    @Override
    public void cardInserted(CardTerminalEvent cardTerminalEvent) throws CardTerminalException
    {
        LogHelper.log(LogLevel.INFO, "Card inserted");
    }

    @Override
    public void cardRemoved(CardTerminalEvent cardTerminalEvent) throws CardTerminalException
    {
        LogHelper.log(LogLevel.INFO, "Card removed");

    }
}
