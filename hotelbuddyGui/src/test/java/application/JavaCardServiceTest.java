package application;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Patrick on 19.06.2015.
 */
public class JavaCardServiceTest
{

    @Test
    public void testConnect() throws Exception
    {
        JavaCardService card = new JavaCardService();
        card.Connect();
    }
}