package common;

import hotelbuddy.ICryptography;
import javacard.framework.*;

/**
 * Created by Georg on 18.06.2015.
 */
public class CryptographyMock extends Applet implements ICryptography
{
    private static final byte CRYPTOGRAPHY_SECRET = 0x2A;

    private static boolean encrypted = false;
    private static boolean decrypted = false;
    public static short DataLength = 0;

    protected CryptographyMock()
    {
        register();
    }

    public static void install(byte[] bArray, short bOffset, byte bLength)
    {
        new CryptographyMock();
    }

    @Override
    public void process(APDU apdu) throws ISOException
    {
        if (selectingApplet())
        {
            return;
        }

        ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
    }

    public boolean select()
    {
        return true;
    }

    public void deselect()
    {
    }

    public short encrypt(byte[] buffer, byte[] message)
    {
        encrypted = true;
        System.arraycopy(message, 0, buffer, 0, message.length);
        return (short) message.length;
    }

    public short decrypt(byte[] message, byte offset)
    {
        decrypted = true;
        System.arraycopy(trim(message, offset), 0, message, 0, DataLength);
        return DataLength;
    }

    public static boolean encryptWasCalled()
    {
        return encrypted;
    }

    public static boolean decryptWasCalled()
    {
        return decrypted;
    }

    public static void reset()
    {
        encrypted = false;
        decrypted = false;
        DataLength = 0;
    }

    @Override
    public Shareable getShareableInterfaceObject(AID client_aid, byte parameter)
    {
        if (parameter != CRYPTOGRAPHY_SECRET)
        {
            return null;
        }

        return this;
    }

    private byte[] trim(byte[] message, byte offset)
    {
        byte[] shortenedMessage = new byte[DataLength];
        Util.arrayCopy(message, offset, shortenedMessage, (byte) 0x00, (byte) DataLength);

        return shortenedMessage;
    }
}
