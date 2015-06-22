package application;

import application.log.LogHelper;
import application.log.LogLevel;
import opencard.core.event.CTListener;
import opencard.core.event.CardTerminalEvent;
import opencard.core.event.EventGenerator;
import opencard.core.service.CardRequest;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminalException;
import opencard.core.terminal.ResponseAPDU;
import opencard.core.util.HexString;
import opencard.opt.terminal.ISOCommandAPDU;
import opencard.opt.util.PassThruCardService;

/**
 * Created by Patrick on 19.06.2015.
 */
public class SmartCardConnector implements CTListener
{
    SmartCard card;

    public boolean connect()
    {
        try
        {
            SmartCard.start();
        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
            return false;
        }

        LogHelper.log(LogLevel.INFO, "Smartcard started");

        EventGenerator.getGenerator().addCTListener(this);

        return true;
    }

    public boolean selectApplet(String appletAid)
    {
        try
        {
            CardRequest cardRequest = new CardRequest(CardRequest.ANYCARD, null, PassThruCardService.class);
            cardRequest.setTimeout(15);
            card = SmartCard.waitForCard(cardRequest);

            if (card == null)
            {
                LogHelper.log(LogLevel.WARNING, "No card available");
                return false;
            }

            if (!sendSelectCommand(appletAid))
            {
                LogHelper.log(LogLevel.WARNING, "Applet %s not selected", appletAid);
                return false;
            }

            LogHelper.log(LogLevel.INFO, "Applet %s selected", appletAid);
            return true;

        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
            return false;
        }
    }

    private boolean sendSelectCommand(String appletID)
    {
        if (card == null)
        {
            LogHelper.log(LogLevel.WARNING, "No card available");
            return false;
        }

        ISOCommandAPDU commandApdu;
        ResponseAPDU responseApdu;
        PassThruCardService cardService;

        try
        {
            cardService = (PassThruCardService) card.getCardService(PassThruCardService.class, true);

            commandApdu = ApduHelper.getSelectCommand(appletID);
            responseApdu = cardService.sendCommandAPDU(commandApdu);

            String status = HexString.hexifyShort(responseApdu.sw1(), responseApdu.sw2());
            if (!status.equals("9000"))
            {
                LogHelper.log(LogLevel.FAILURE, "Could not select applet: %s", status);
                return false;
            }

            return true;
        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
            return false;
        }
    }

    public void cardInserted(CardTerminalEvent cardTerminalEvent)
    {
        LogHelper.log(LogLevel.INFO, "Smartcard inserted");
    }

    public void cardRemoved(CardTerminalEvent cardTerminalEvent) throws CardTerminalException
    {
        LogHelper.log(LogLevel.INFO, "Smartcard removed");
    }
}
