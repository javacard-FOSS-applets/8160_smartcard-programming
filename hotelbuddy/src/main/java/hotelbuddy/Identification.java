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
    private static final byte INS_SET_NAME = (byte) 0xA0;
    private static final byte INS_GET_NAME = (byte) 0xA1;

    private static final byte INS_SET_BIRTHDAY = (byte) 0xB0;
    private static final byte INS_GET_BIRTHDAY = (byte) 0xB1;
    private static final byte INS_CHECK_AGE = (byte) 0xB2;

    private static final byte INS_SET_CARID = (byte) 0xC0;
    private static final byte INS_GET_CARID = (byte) 0xC1;

    private static final byte INS_SET_SAFEPIN = (byte) 0xD0;
    private static final byte INS_CHECK_SAFEPIN = (byte) 0xD1;

    private static final byte INS_RESET = (byte) 0xFF;

    // Other Applets
    private static final byte[] CRYPTOGRAPHY_AID = {0x43, 0x72, 0x79, 0x70, 0x74, 0x6f, 0x67, 0x72, 0x61, 0x70, 0x68, 0x79};
    private static final byte CRYPTOGRAPHY_SECRET = 0x2A;

    // Data
    private final byte MAX_NAME_LENGTH = 50;
    private byte currentNameLength = 0;
    private byte[] name;

    private byte[] birthDay;

    private final byte MAX_CARID_LENGTH = 8;
    private byte currentCarIdLength = 0;
    private byte[] carId;

    private final byte SAFEPIN_LENGTH = 4;
    private byte[] safePin;

    protected Identification()
    {
        register();

        name = new byte[MAX_NAME_LENGTH];
        birthDay = new byte[DateHelper.DATE_LENGTH];
        carId = new byte[MAX_CARID_LENGTH];
        safePin = new byte[SAFEPIN_LENGTH];

        reset();
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
            case INS_SET_NAME:
                setName(apdu);
                break;
            case INS_GET_NAME:
                getName(apdu);
                break;
            case INS_SET_BIRTHDAY:
                setBirthday(apdu);
                break;
            case INS_GET_BIRTHDAY:
                getBirthday(apdu);
                break;
            case INS_CHECK_AGE:
                checkAge(apdu);
                break;
            case INS_SET_CARID:
                setCarId(apdu);
                break;
            case INS_GET_CARID:
                getCarId(apdu);
                break;
            case INS_SET_SAFEPIN:
                setSafePin(apdu);
                break;
            case INS_CHECK_SAFEPIN:
                checkSafePin(apdu);
                break;
            case INS_RESET:
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
        // Length = 0 -> Not set yet
        currentNameLength = 0;
        currentCarIdLength = 0;

        // Day can not be 0 -> Not set yet
        birthDay[0] = 0x00;

        // Safe pin number one can not be greater than 9 -> Not set yet
        safePin[0] = 0x10;
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

        apdu.setIncomingAndReceive();
        send(apdu, birthDay, (byte) 0, DateHelper.DATE_LENGTH);
    }

    public boolean select()
    {
        return true;
    }

    public void deselect()
    {
    }

    /**
     * Checks if the given safe pin is equal to the saved safe pin.
     * If the safe pin is not set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     * If the message length is not SAFEPIN_LENGTH, ISO7816.SW_WRONG_LENGTH is thrown.
     * If the pin to check has an invalid format, ISO7816.SW_DATA_INVALID is thrown.
     *
     * @param apdu Received APDU, which contains pin to check. Is also used to send the data.
     */
    private void checkSafePin(APDU apdu)
    {
        if (safePin[0] == 0x10)
        {
            // Safe pin already set yet
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        apdu.setIncomingAndReceive();
        byte[] buffer = apdu.getBuffer();

        short messageLength = decryptMessage(buffer);

        if (messageLength != SAFEPIN_LENGTH)
        {
            // Data doesnt have a correct length
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        if (!checkSafePinFormat(buffer, (byte) 0x00))
        {
            // Wrong Safe Pin Format
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            return;
        }

        buffer[0] = Util.arrayCompare(safePin, (byte) 0x00, buffer, (byte) 0x00, SAFEPIN_LENGTH) == 0 ? (byte) 0x01 : (byte) 0x00;

        send(apdu, buffer, (byte) 0x00, (byte) 0x01);
    }

    /**
     * Sets the safe pin given by the APDU.
     * If the safe pin is already set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     * If the message length is not equal to SAFEPIN_LENGTH, ISO7816.SW_WRONG_LENGTH is thrown.
     * If the safe pin has an invalid format, ISO7816.SW_DATA_INVALID is thrown.
     *
     * @param apdu Received APDU, which contains the safe pin.
     */
    private void setSafePin(APDU apdu)
    {
        if (safePin[0] != 0x10)
        {
            // Safe pin already set yet
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        apdu.setIncomingAndReceive();
        byte[] buffer = apdu.getBuffer();

        short messageLength = decryptMessage(buffer);

        if (messageLength != SAFEPIN_LENGTH)
        {
            // Data doesnt have a correct length
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        if (!checkSafePinFormat(buffer, (byte) 0x00))
        {
            // Wrong Safe Pin Format
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            return;
        }

        Util.arrayCopy(buffer, (short) (0x00 & 0x00FF), safePin, (short) (0x00 & 0x00FF), SAFEPIN_LENGTH);
    }

    /**
     * Checks, whether the given safe pin inside the buffer at offset is in valid format.
     *
     * @param buffer buffer containing the safe pin
     * @param offset Offset of the safe pin in the buffer
     * @return true, if the safe pin has a valid format, false otherwise.
     */
    private boolean checkSafePinFormat(byte[] buffer, byte offset)
    {
        byte end = (byte) (offset + SAFEPIN_LENGTH);

        while (offset < end)
        {
            if (buffer[offset] < 0x00 || buffer[offset] > 0x09)
            {
                return false;
            }

            offset++;
        }

        return true;
    }

    /**
     * Send the saved car id via given APDU.
     * If the car id is not set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     *
     * @param apdu Received APDU, which is used to send the data.
     */
    private void getCarId(APDU apdu)
    {
        if (currentCarIdLength == 0)
        {
            // Name not set yet
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
        }

        apdu.setIncomingAndReceive();
        send(apdu, carId, (byte) 0, currentCarIdLength);
    }

    /**
     * Sets the car id given by the APDU.
     * If the car id is already set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     * If the message length is greater than MAX_CARID_LENGTH or zero, ISO7816.SW_WRONG_LENGTH is thrown.
     *
     * @param apdu Received APDU, which contains the car id.
     */
    private void setCarId(APDU apdu)
    {
        if (currentCarIdLength != 0)
        {
            // Name already set yet
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        apdu.setIncomingAndReceive();
        byte[] buffer = apdu.getBuffer();

        short messageLength = decryptMessage(buffer);

        if (messageLength > MAX_CARID_LENGTH || messageLength == 0)
        {
            // Data doesnt have a correct length
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        Util.arrayCopy(buffer, (short) (0x00 & 0x00FF), carId, (short) (0x00 & 0x00FF), messageLength);
        currentCarIdLength = (byte) messageLength;
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
        apdu.setIncomingAndReceive();
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
            // Wrong Data Length
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        if (!DateHelper.checkDate(buffer, (short) (0x00 & 0x00FF)))
        {
            // Wrong Date Format
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            return;
        }

        buffer[0] = DateHelper.yearDifference(buffer, (short) (0x00 & 0x00FF), birthDay) < buffer[DateHelper.DATE_LENGTH] ? (byte) 0x00 : (byte) 0x01;

        send(apdu, buffer, (byte) 0x00, (byte) 0x01);
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

        apdu.setIncomingAndReceive();
        byte[] buffer = apdu.getBuffer();

        short messageLength = decryptMessage(buffer);

        if (messageLength != DateHelper.DATE_LENGTH)
        {
            // Data doesnt have the length of a date
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        if (!DateHelper.checkDate(buffer, (short) (0x00 & 0x00FF)))
        {
            // Data is not valid date
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            return;
        }

        Util.arrayCopy(buffer, (short) (0x00 & 0x00FF), birthDay, (short) (0x00 & 0x00FF), DateHelper.DATE_LENGTH);
    }

    /**
     * Send the saved name via given APDU.
     * If the name is not set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     *
     * @param apdu Received APDU, which is used to send the data.
     */
    private void getName(APDU apdu)
    {
        if (currentNameLength == 0)
        {
            // Name not set yet
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
        }

        apdu.setIncomingAndReceive();
        send(apdu, name, (byte) 0, currentNameLength);
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

        apdu.setOutgoingAndSend((short) (0x00 & 0x00FF), len);
    }

    /**
     * Sets the name given by the APDU.
     * If the name is already set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     * If the message length is greater than MAX_NAME_LENGTH or zero, ISO7816.SW_WRONG_LENGTH is thrown.
     *
     * @param apdu Received APDU, which contains the name.
     */
    private void setName(APDU apdu)
    {
        if (currentNameLength != 0)
        {
            // Name already set yet
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        apdu.setIncomingAndReceive();
        byte[] buffer = apdu.getBuffer();

        short messageLength = decryptMessage(buffer);

        if (messageLength > MAX_NAME_LENGTH || messageLength == 0)
        {
            // Data doesnt have a correct length
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        Util.arrayCopy(buffer, (short) (0x00 & 0x00FF), name, (short) (0x00 & 0x00FF), messageLength);
        currentNameLength = (byte) messageLength;
    }

    /**
     * Encrypts the given message at offset to offset+length into the buffer at offset 0
     * by using the Cryptography applet through the applet firewall.
     *
     * @param buffer  Target memory. Result will be stored at offset 0.
     * @param message Memory containing the message to encrypt
     * @param offset  Offset where the message begins.
     * @param length  Length of the message.
     * @return length of the encrypted message.
     */
    private short encryptMessage(byte[] buffer, byte[] message, byte offset, byte length)
    {
        AID cryptographyAid = JCSystem.lookupAID(CRYPTOGRAPHY_AID, (short) (0x00 & 0x00FF), (byte) CRYPTOGRAPHY_AID.length);
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
        AID cryptographyAid = JCSystem.lookupAID(CRYPTOGRAPHY_AID, (short) (0x00 & 0x00FF), (byte) CRYPTOGRAPHY_AID.length);
        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(cryptographyAid, CRYPTOGRAPHY_SECRET);

        return cryptoApp.decrypt(buffer, ISO7816.OFFSET_CDATA);
    }
}
