import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;

/**
 * Created by Patrick on 28.04.2015.
 */
public class MainApp extends Applet
{
    /**
     * Installs this applet.
     *
     * @param bArray  the array containing installation parameters
     * @param bOffset the starting offset in bArray
     * @param bLength the length in bytes of the parameter data in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength)
    {
        new MainApp();
    }

    /**
     * Only this class's install method should create the applet object.
     */
    protected MainApp()
    {
        register();
    }

    /**
     * Processes an incoming APDU.
     *
     * @param apdu the incoming APDU
     * @see APDU
     */
    public void process(APDU apdu)
    {
        if (selectingApplet())
        {
            return;
        }

        byte[] buffer = apdu.getBuffer();

        switch (buffer[ISO7816.OFFSET_INS])
        {
            case 0x00:
                buffer[0] = 'H';
                buffer[1] = 'e';
                buffer[2] = 'l';
                buffer[3] = 'l';
                buffer[4] = 'o';
                buffer[5] = '!';
                apdu.setOutgoingAndSend((short) 0, (short) 6);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                break;
        }
    }
}
