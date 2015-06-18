package common;

import cryptography.ICryptography;
import javacard.framework.*;

/**
 * Created by Georg on 18.06.2015.
 */
public class CryptographyMock extends Applet implements ICryptography
{
    private static final byte CRYPTOGRAPHY_SECRET = 0x2A;

    private static boolean encrypted = false;
    private static boolean decrypted = false;

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

    public byte[] encrypt(byte[] message)
    {
        encrypted = true;
        return trimZeroes(message);
    }

    public byte[] decrypt(byte[] message)
    {
        decrypted = true;
        return trimZeroes(message);
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

    private byte[] trimZeroes(byte[] message)
    {
        byte length = 0;
        while (message[length] != 0x00)
        {
            length++;
        }

        byte[] shortenedMessage = new byte[length];
        Util.arrayCopy(message, (byte) 0x00, shortenedMessage, (byte) 0x00, length);

        return shortenedMessage;
    }
}
