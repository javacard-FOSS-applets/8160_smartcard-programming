package application;

import application.card.SmartCardConnector;
import org.junit.Test;

/**
 * Created by Patrick on 19.06.2015.
 */
public class SmartCardConnectorTest
{

    @Test
    public void testConnect() throws Exception
    {
        SmartCardConnector card = new SmartCardConnector();
        card.connect();
    }
}