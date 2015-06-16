import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISOException;

/**
 * Created by Patrick on 16.06.2015.
 */
public class Identification extends Applet
{
    protected Identification()
    {
        register();
    }

    public static void install(byte[] bArray, short bOffset, byte bLength)
    {
        new Cryptography();
    }

    @Override
    public void process(APDU apdu) throws ISOException
    {
    }
}
