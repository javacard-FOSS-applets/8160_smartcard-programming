package identification;

import cryptography.ICryptography;
import javacard.framework.*;

/**
 * Created by Georg on 16.06.2015.
 */
public class Identification extends Applet
{
    // Java Card
    // Applet
    private static final byte IDENTIFICATION_CLA = 0x49;

    // Instructions
    private static final byte SET_NAME = (byte) 0xA0;
    private static final byte GET_NAME = (byte) 0xA1;

    private static final byte SET_BIRTHDAY = (byte) 0xB0;
    private static final byte CHECK_AGE = (byte) 0xB1;

    private static final byte SET_CARID = (byte) 0xC0;
    private static final byte GET_CARID = (byte) 0xC1;

    private static final byte SET_SAFEPIN = (byte) 0xD0;
    private static final byte CHECK_SAFEPIN = (byte) 0xD1;

    // Other Applets
    private static final byte[] CRYPTOGRAPHY_AID = {0x43, 0x72, 0x79, 0x70, 0x74, 0x6f, 0x67, 0x72, 0x61, 0x70, 0x68, 0x79};
    private static final byte CRYPTOGRAPHY_SECRET = 0x2A;

    // Data
    private short MAX_NAME_LENGTH = 50;
    private byte[] name;

    private short MAX_BIRTHDAY_LENGTH = 10;
    private byte[] birthDay;

    private short MAX_CARID_LENGTH = 8;
    private byte[] carId;

    private short MAX_SAFEPIN_LENGTH = 4;
    private byte[] safePin;

    protected Identification()
    {
        register();

        name = new byte[MAX_NAME_LENGTH];
        birthDay = new byte[MAX_BIRTHDAY_LENGTH];
        carId = new byte[MAX_CARID_LENGTH];
        safePin = new byte[MAX_SAFEPIN_LENGTH];
    }

    public static void install(byte[] bArray, short bOffset, byte bLength)
    {
        new Identification();
    }

    public void process(APDU apdu) throws ISOException
    {
        if (selectingApplet())
        {
            ISOException.throwIt(ISO7816.SW_NO_ERROR);
            return;
        }

        byte[] buf = apdu.getBuffer();

        if (buf[ISO7816.OFFSET_CLA] != IDENTIFICATION_CLA)
        {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
            return;
        }

        switch (buf[ISO7816.OFFSET_INS])
        {
            case SET_NAME:
                setName(apdu);
                break;
            case GET_NAME:
                getName(apdu);
                break;
            case SET_BIRTHDAY:
                setBirthday(apdu);
                break;
            case CHECK_AGE:
                checkAge(apdu);
                break;
            case SET_CARID:
                setCarId(apdu);
                break;
            case GET_CARID:
                getCarId(apdu);
                break;
            case SET_SAFEPIN:
                setSafePin(apdu);
                break;
            case CHECK_SAFEPIN:
                checkSafePin(apdu);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    public boolean select()
    {
        return true;
    }

    public void deselect()
    {
    }

    private void checkSafePin(APDU apdu)
    {

    }

    private void setSafePin(APDU apdu)
    {

    }

    private void getCarId(APDU apdu)
    {

    }

    private void setCarId(APDU apdu)
    {

    }

    private void checkAge(APDU apdu)
    {

    }

    private void setBirthday(APDU apdu)
    {

    }

    private void getName(APDU apdu)
    {
        if (Util.arrayCompare(name, (short) 0x00, new byte[name.length], (short) 0x00, (short) name.length) == 0)
        {
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
        }

        byte[] message = encryptMessage(name);

        Util.arrayCopy(message, (short) 0, apdu.getBuffer(), (short) 0, (short) message.length);
        apdu.setOutgoingAndSend((short) 0, (short) message.length);
    }

    private void setName(APDU apdu)
    {
        byte[] message = decryptMessage(apdu);

        if (message.length > MAX_NAME_LENGTH)
        {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        Util.arrayCopy(message, (short) 0, name, (short) 0, (short) message.length);
    }

    private byte[] encryptMessage(byte[] messsage)
    {
        AID cryptogrphyAid = JCSystem.lookupAID(CRYPTOGRAPHY_AID, (short) 0, (byte) CRYPTOGRAPHY_AID.length);
        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(cryptogrphyAid, CRYPTOGRAPHY_SECRET);

        return cryptoApp.encrypt(messsage);
    }

    private byte[] decryptMessage(APDU apdu)
    {
        short messageLength = 128;
        byte[] message = JCSystem.makeTransientByteArray(messageLength, JCSystem.CLEAR_ON_DESELECT);
        Util.arrayCopy(apdu.getBuffer(), ISO7816.OFFSET_CDATA, message, (short) 0, messageLength);

        AID cryptogrphyAid = JCSystem.lookupAID(CRYPTOGRAPHY_AID, (short) 0, (byte) CRYPTOGRAPHY_AID.length);
        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(cryptogrphyAid, CRYPTOGRAPHY_SECRET);

        return cryptoApp.decrypt(message);
    }
}
