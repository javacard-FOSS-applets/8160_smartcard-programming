package application;

import application.log.LogHelper;
import application.log.LogLevel;
import opencard.core.event.CTListener;
import opencard.core.event.CardTerminalEvent;
import opencard.core.event.EventGenerator;
import opencard.core.service.CardRequest;
import opencard.core.service.SmartCard;
import opencard.core.terminal.CardTerminalException;
import opencard.opt.util.PassThruCardService;

/**
 * Created by Patrick on 19.06.2015.
 */
public class SmartCardConnector implements CTListener
{
    SmartCard card;

    public void Connect()
    {
        try
        {
            SmartCard.start();
        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
            return;
        }

        LogHelper.log(LogLevel.INFO, "Smartcard started");

        EventGenerator.getGenerator().addCTListener(this);
    }

    public void cardInserted(CardTerminalEvent cardTerminalEvent)
    {
        LogHelper.log(LogLevel.INFO, "Smartcard inserted");

        try
        {
            CardRequest cardRequest = new CardRequest(CardRequest.ANYCARD, null, PassThruCardService.class);
            cardRequest.setTimeout(15);
            card = SmartCard.waitForCard(cardRequest);
            if (card != null)
            {
//                System.out.println("card present");
//                if (selectApplet(card))
//                {
//                    System.out.println("Applet selected");
//                }
//                else
//                {
//                    System.out.println("Applet NOT selected!!!");
//                }
            }
        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
        }
    }

    public void cardRemoved(CardTerminalEvent cardTerminalEvent) throws CardTerminalException
    {
        LogHelper.log(LogLevel.INFO, "Smartcard removed");
    }
}
