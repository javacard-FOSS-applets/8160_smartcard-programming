package hotelbuddy;

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
    private static final byte GET_BIRTHDAY = (byte) 0xB1;
    private static final byte CHECK_AGE = (byte) 0xB2;

    private static final byte SET_CARID = (byte) 0xC0;
    private static final byte GET_CARID = (byte) 0xC1;

    private static final byte SET_SAFEPIN = (byte) 0xD0;
    private static final byte CHECK_SAFEPIN = (byte) 0xD1;

    private static final byte RESET = (byte) 0xFF;

    // Other Applets
    private static final byte[] CRYPTOGRAPHY_AID = {0x43, 0x72, 0x79, 0x70, 0x74, 0x6f, 0x67, 0x72, 0x61, 0x70, 0x68, 0x79};
    private static final byte CRYPTOGRAPHY_SECRET = 0x2A;

    // Data
    private final byte MAX_NAME_LENGTH = 50;
    private byte nameLength = 0;
    private byte[] name;

    private final byte DATE_LENGTH = 4;
    private byte[] birthDay;

    private final byte MAX_CARID_LENGTH = 8;
    private short carIdLength = 0;
    private byte[] carId;

    private final byte SAFEPIN_LENGTH = 4;
    private boolean safePinIsSet = false;
    private byte[] safePin;

    protected Identification()
    {
        register();

        name = new byte[MAX_NAME_LENGTH];
        birthDay = new byte[DATE_LENGTH];
        carId = new byte[MAX_CARID_LENGTH];
        safePin = new byte[SAFEPIN_LENGTH];
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
            case GET_BIRTHDAY:
                getBirthday(apdu);
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
            case RESET:
                reset();
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void reset()
    {
        nameLength = 0;
        birthDay[0] = 0x00;
        carIdLength = 0;
        safePinIsSet = false;
    }

    private void getBirthday(APDU apdu)
    {
        if (birthDay[0] == 0x00)
        {
            // Birthday not set yet, because day can not be 0x00
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        send(apdu, birthDay);
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
        if (birthDay[0] == 0x00)
        {
            // Birthday not set yet, because day can not be 0x00
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        byte[] message = decryptMessage(apdu);

        if (message.length != (DATE_LENGTH + 1))
        {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        byte[] today = JCSystem.makeTransientByteArray(DATE_LENGTH, JCSystem.CLEAR_ON_DESELECT);
        Util.arrayCopy(message, (short) 0, today, (short) 0, DATE_LENGTH);

        byte minimumAge = message[message.length - 1];

        if (!DateHelper.checkDate(today))
        {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            return;
        }

        byte[] result = JCSystem.makeTransientByteArray((short) 1, JCSystem.CLEAR_ON_DESELECT);
        result[0] = DateHelper.yearDifference(today, birthDay) < minimumAge ? (byte) 0x00 : (byte) 0x01;

        send(apdu, result);
    }

    private void setBirthday(APDU apdu)
    {
        if (birthDay[0] != 0x00)
        {
            // Birthday already set yet
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        byte[] message = decryptMessage(apdu);

        if (message.length != DATE_LENGTH)
        {
            // Data doesnt have the length of a date
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        if (!DateHelper.checkDate(message))
        {
            // Data is not valid date
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            return;
        }

        Util.arrayCopy(message, (short) 0, birthDay, (short) 0, DATE_LENGTH);
    }

    private void getName(APDU apdu)
    {
        if (nameLength == 0)
        {
            // Name not set yet
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
        }

        byte[] trimmedName = JCSystem.makeTransientByteArray(nameLength, JCSystem.CLEAR_ON_DESELECT);
        Util.arrayCopy(name, (byte) 0x00, trimmedName, (byte) 0x00, nameLength);

        send(apdu, trimmedName);
    }

    private void send(APDU apdu, byte[] content)
    {
        byte[] buffer = apdu.getBuffer();
        short len = encryptMessage(buffer, content);

        // Util.arrayCopy(message, (short) 0, apdu.getBuffer(), (short) 0, (short) message.length);
        apdu.setOutgoingAndSend((short) 0, len);
    }

    private void setName(APDU apdu)
    {
        if (nameLength != 0)
        {
            // Name already set yet
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        byte[] message = decryptMessage(apdu);

        if (message.length > MAX_NAME_LENGTH || message.length == 0)
        {
            // Data doesnt have a correct length
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        Util.arrayCopy(message, (short) 0, name, (short) 0, (short) message.length);
        nameLength = (byte) message.length;
    }

    private short encryptMessage(byte[] buffer, byte[] messsage)
    {
        AID cryptogrphyAid = JCSystem.lookupAID(CRYPTOGRAPHY_AID, (short) 0, (byte) CRYPTOGRAPHY_AID.length);
        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(cryptogrphyAid, CRYPTOGRAPHY_SECRET);

        return cryptoApp.encrypt(buffer, messsage);
    }

    private byte[] decryptMessage(APDU apdu)
    {
        byte[] buffer = apdu.getBuffer();

        AID cryptogrphyAid = JCSystem.lookupAID(CRYPTOGRAPHY_AID, (short) 0, (byte) CRYPTOGRAPHY_AID.length);
        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(cryptogrphyAid, CRYPTOGRAPHY_SECRET);

        short len = cryptoApp.decrypt(buffer, ISO7816.OFFSET_CDATA);
        byte[] decryptedMessage = JCSystem.makeTransientByteArray(len, JCSystem.CLEAR_ON_DESELECT);
        Util.arrayCopy(buffer, (short) 0, decryptedMessage, (short) 0, len);

        return decryptedMessage;
    }
}
