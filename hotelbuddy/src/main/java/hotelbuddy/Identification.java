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
        birthDay = new byte[DateHelper.DATE_LENGTH];
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

    /**
     * Resets the state of the Identification applet.
     */
    private void reset()
    {
        nameLength = 0;
        birthDay[0] = 0x00;
        carIdLength = 0;
        safePinIsSet = false;
    }

    /**
     * Send the saved birthday via given APDU.
     * If the birthday is not set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     *
     * @param apdu Received APDU, which is used to send the data.
     */
    private void getBirthday(APDU apdu)
    {
        if (birthDay[0] == 0x00)
        {
            // Birthday not set yet, because day can not be 0x00
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        send(apdu, birthDay, (byte) 0, DateHelper.DATE_LENGTH);
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

    /**
     * Checks if the given age limit is passed, using the saved birthday.
     * If the birthday is not set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     * If the message length is not 5 (birthday + age limit = 4 + 1), ISO7816.SW_WRONG_LENGTH is thrown.
     * If the date of today has an invalid format, ISO7816.SW_DATA_INVALID is thrown.
     *
     * @param apdu Received APDU, which contains the date of today and the age limit.  Is also used to send the data.
     */
    private void checkAge(APDU apdu)
    {
        byte[] buffer = apdu.getBuffer();

        if (birthDay[0] == 0x00)
        {
            // Birthday not set yet, because day can not be 0x00
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        short messageLength = decryptMessage(buffer);

        if (messageLength != (DateHelper.DATE_LENGTH + 1))
        {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        if (!DateHelper.checkDate(buffer, 0))
        {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            return;
        }

        buffer[DateHelper.DATE_LENGTH + 1] = DateHelper.yearDifference(buffer, 0, birthDay) < buffer[DateHelper.DATE_LENGTH] ? (byte) 0x00 : (byte) 0x01;

        send(apdu, buffer, (byte) (DateHelper.DATE_LENGTH + 1), (byte) 1);
    }


    /**
     * Sets the birthday given by the APDU.
     * If the birthday is already set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     * If the message length is not 4 (length of a date), ISO7816.SW_WRONG_LENGTH is thrown.
     * If the birthday has an invalid format, ISO7816.SW_DATA_INVALID is thrown.
     *
     * @param apdu Received APDU, which contains the birthday.
     */
    private void setBirthday(APDU apdu)
    {
        if (birthDay[0] != 0x00)
        {
            // Birthday already set yet
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        byte[] buffer = apdu.getBuffer();

        short messageLength = decryptMessage(buffer);

        if (messageLength != DateHelper.DATE_LENGTH)
        {
            // Data doesnt have the length of a date
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        if (!DateHelper.checkDate(buffer, 0))
        {
            // Data is not valid date
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            return;
        }

        Util.arrayCopy(buffer, (short) 0, birthDay, (short) 0, DateHelper.DATE_LENGTH);
    }

    /**
     * Send the saved name via given APDU.
     * If the name is not set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     *
     * @param apdu Received APDU, which is used to send the data.
     */
    private void getName(APDU apdu)
    {
        if (nameLength == 0)
        {
            // Name not set yet
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
        }

        send(apdu, name, (byte) 0, nameLength);
    }

    /**
     * Send the given content from offset to offset+length via given APDU.
     * Encrypts the content beforehand.
     *
     * @param apdu Received APDU, which is used to send the data.
     */
    private void send(APDU apdu, byte[] content, byte offset, byte length)
    {
        byte[] buffer = apdu.getBuffer();
        short len = encryptMessage(buffer, content, offset, length);

        apdu.setOutgoingAndSend((short) 0, len);
    }

    /**
     * Sets the name given by the APDU.
     * If the name is already set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     * If the message length is greater than MAX_NAME_LENGTH or zero, ISO7816.SW_WRONG_LENGTH is thrown.
     *
     * @param apdu Received APDU, which contains the birthday.
     */
    private void setName(APDU apdu)
    {
        if (nameLength != 0)
        {
            // Name already set yet
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        byte[] buffer = apdu.getBuffer();

        short messageLength = decryptMessage(buffer);

        if (messageLength > MAX_NAME_LENGTH || messageLength == 0)
        {
            // Data doesnt have a correct length
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        Util.arrayCopy(buffer, (short) 0, name, (short) 0, messageLength);
        nameLength = (byte) messageLength;
    }

    /**
     * Encrypts the given message at offset to offset+length into the buffer at offset 0
     * by using the Cryptography applet through the applet firewall.
     *
     * @param buffer Target memory. Result will be stored at offset 0.
     * @param message Memory containing the message to encrypt
     * @param offset Offset where the message begins.
     * @param length Length of the message.
     * @return length of the encrypted message.
     */
    private short encryptMessage(byte[] buffer, byte[] message, byte offset, byte length)
    {
        AID cryptographyAid = JCSystem.lookupAID(CRYPTOGRAPHY_AID, (short) 0, (byte) CRYPTOGRAPHY_AID.length);
        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(cryptographyAid, CRYPTOGRAPHY_SECRET);

        return cryptoApp.encrypt(buffer, message, offset, length);
    }

    /**
     * Decrypts the given message at offset 0
     * by using the Cryptography applet through the applet firewall.
     *
     * @param buffer Source and target memory.
     *               Message starts at ISO7816.OFFSET_CDATA.
     *               Result will be stored at offset 0.
     * @return length of the decrypted message.
     */
    private short decryptMessage(byte[] buffer)
    {
        AID cryptographyAid = JCSystem.lookupAID(CRYPTOGRAPHY_AID, (short) 0, (byte) CRYPTOGRAPHY_AID.length);
        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(cryptographyAid, CRYPTOGRAPHY_SECRET);

        return cryptoApp.decrypt(buffer, ISO7816.OFFSET_CDATA);
    }
}
