package hotelbuddy;

import javacard.framework.*;

/**
 * Created by Johannes on 22.06.15.
 */
public class Access extends Applet
{
    // Java Card
    // Applet
    private static final byte ACCESS_CLA = 0x41;

    // Instructions
    private static final byte INS_SET_ACCESS_RIGHTS = (byte) 0xC1;
    private static final byte INS_GET_ACCESS_RIGHT = (byte) 0xC2;

    private static final byte INS_RESET = (byte) 0xF0;

    // Other Applets
    private static final byte[] CRYPTOGRAPHY_AID = {0x43, 0x72, 0x79, 0x70, 0x74, 0x6f, 0x67, 0x72, 0x61, 0x70, 0x68, 0x79};
    private static final byte CRYPTOGRAPHY_SECRET = 0x2A;

    // Access Rights
    private static final byte[] ACCESS_DENIED = {(byte) 0x00};
    private static final byte[] ACCESS_GRANTED = {(byte) 0x10};

    // Constants and Offsets
    private static final byte KEY_LENGTH = 2;
    private static final byte VALUE_LENGTH = 1;
    private static final byte ENTRY_LENGTH = 3;
    private static final byte MAX_ENTRY_COUNT = 20;

    private static final byte OFFSET_INDEX_KEY_EXISTS = MAX_ENTRY_COUNT * ENTRY_LENGTH;
    private static final byte OFFSET_INDEX_GET_VALUE = 2;
    private static final byte OFFSET_RESULT_GET_VALUE = 3;

    // Data
    // Key: object to access/use, value: set permission
    private static byte[] permissionDictionary;
    private static byte nextFreeIndex;

    private static boolean accessRightsAlreadySet;

    /**
     * Initializes the applet.
     */
    public Access()
    {
        register();

        nextFreeIndex = 0;
        accessRightsAlreadySet = false;
        permissionDictionary = new byte[MAX_ENTRY_COUNT * ENTRY_LENGTH];
    }

    /**
     * Installs the applet on the card.
     *
     * @param bArray
     * @param bOffset
     * @param bLength
     */
    public static void install(byte[] bArray, short bOffset, byte bLength)
    {
        new Access();
    }

    public void process(APDU apdu) throws ISOException
    {
        if (selectingApplet())
        {
            ISOException.throwIt(ISO7816.SW_NO_ERROR);
            return;
        }

        byte[] buffer = apdu.getBuffer();

        if (buffer[ISO7816.OFFSET_CLA] != ACCESS_CLA)
        {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
            return;
        }

        switch (buffer[ISO7816.OFFSET_INS])
        {
            case INS_SET_ACCESS_RIGHTS:
                setAccessRights(apdu);
                break;
            case INS_GET_ACCESS_RIGHT:
                getAccessRight(apdu);
                break;
            case INS_RESET:
                reset();
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    /**
     * Resets the memory for new data deployment.
     */
    private void reset()
    {
        if (!accessRightsAlreadySet)
        {
            return;
        }

        nextFreeIndex = 0;
        accessRightsAlreadySet = false;

        Util.arrayFillNonAtomic(permissionDictionary, (short) 0, (short) permissionDictionary.length, (byte) 0);
    }

    /**
     * Sets the access rights transmitted by the APDU.
     * The message is expected to have the following pattern:
     * [key][value][key][value]...
     *
     * @param apdu the APDU received by the card
     */
    private void setAccessRights(APDU apdu)
    {
        if (accessRightsAlreadySet)
        {
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
        }

        apdu.setIncomingAndReceive();
        byte[] buffer = apdu.getBuffer();

        short messageLength = decryptMessage(buffer);

        if (messageLength % ENTRY_LENGTH != 0 || messageLength == 0 || messageLength > permissionDictionary.length)
        {
            // Data has invalid length.
            // Minimum is key value pair. Must also contain complete pairs and not exceed max entry count.
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        for (short bufferIndex = 0; bufferIndex < messageLength; bufferIndex += ENTRY_LENGTH)
        {
            if (!valueAllowed(buffer, (byte) (bufferIndex + KEY_LENGTH)))
            {
                // Value does not contain known access right, invalid data
                ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            }

            if (keyExists(buffer, (byte) (bufferIndex)))
            {
                // Key already set, data contains key duplicates.
                ISOException.throwIt(ISO7816.SW_DATA_INVALID);
            }

            // At this point, setting key and value is allowed.
            Util.arrayCopy(buffer, bufferIndex, permissionDictionary, (short) (nextFreeIndex & 0x00FF), (short) (ENTRY_LENGTH & 0x00FF));
            nextFreeIndex = (byte) (nextFreeIndex + ENTRY_LENGTH);
        }

        accessRightsAlreadySet = true;
    }

    /**
     * Checks whether a value is a known access right which is allowed to be set in the dictionary.
     *
     * @param buffer the APDU buffer
     * @param offset the buffer offset for the value to check
     * @return true if the value represents a known access right
     */
    private boolean valueAllowed(byte[] buffer, byte offset)
    {
        if (Util.arrayCompare(buffer, (short) offset, ACCESS_DENIED, (short) 0, VALUE_LENGTH) == 0)
        {
            return true;
        }

        if (Util.arrayCompare(buffer, (short) offset, ACCESS_GRANTED, (short) 0, VALUE_LENGTH) == 0)
        {
            return true;
        }

        return false;
    }

    /**
     * Checks if the key exists in the dictionary.
     *
     * @param buffer the APDU buffer
     * @param offset the buffer offset for the key to check
     * @return true if key is found in dictionary, false otherwise
     */
    private boolean keyExists(byte[] buffer, byte offset)
    {
        // dictionary key index
        buffer[OFFSET_INDEX_KEY_EXISTS] = 0;

        while (buffer[OFFSET_INDEX_KEY_EXISTS] < permissionDictionary.length)
        {
            // Check the key at the current dictionary key index and compare it to the key from the APDU buffer.
            if (Util.arrayCompare(permissionDictionary, (short) (buffer[OFFSET_INDEX_KEY_EXISTS] & 0x00FF), buffer, (short) (offset & 0x00FF), KEY_LENGTH) == 0)
            {
                // Matching entry found
                return true;
            }

            buffer[OFFSET_INDEX_KEY_EXISTS] += ENTRY_LENGTH;
        }

        return false;
    }

    /**
     * Gets the access rights for the key received from the APDU message.
     * The message is expected to have the size of a dictionary key.
     *
     * @param apdu the APDU received by the card
     */
    private void getAccessRight(APDU apdu)
    {
        if (!accessRightsAlreadySet)
        {
            // Access rights must be set before get right requests.
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
        }

        apdu.setIncomingAndReceive();

        byte[] buffer = apdu.getBuffer();
        short messageLength = decryptMessage(buffer);

        if (messageLength != KEY_LENGTH)
        {
            // Data has invalid length.
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        getValue(buffer);
        send(apdu, buffer, OFFSET_RESULT_GET_VALUE, VALUE_LENGTH);
    }

    /**
     * Gets the value from the dictionary for the given key and writes it in the APDU buffer
     * at {@code OFFSET_RESULT_GET_VALUE}.
     * If the key does not exist {@code ACCESS_DENIED} will be the result.
     *
     * @param buffer the apdu buffer, containing the key to get the value for and where to store the result
     */
    private void getValue(byte[] buffer)
    {
        buffer[OFFSET_INDEX_GET_VALUE] = 0;

        while (buffer[OFFSET_INDEX_GET_VALUE] < permissionDictionary.length)
        {
            if (Util.arrayCompare(permissionDictionary, (short) (buffer[OFFSET_INDEX_GET_VALUE] & 0x00FF), buffer, (short) 0, KEY_LENGTH) == 0)
            {
                // Matching entry found
                Util.arrayCopy(permissionDictionary, (short) ((buffer[OFFSET_INDEX_GET_VALUE] + KEY_LENGTH) & 0x00FF), buffer, (short) (OFFSET_RESULT_GET_VALUE & 0x00FF), VALUE_LENGTH);
                return;
            }

            buffer[OFFSET_INDEX_GET_VALUE] += ENTRY_LENGTH;
        }

        // Key not found in the dictionary, access will be denied.
        Util.arrayCopy(ACCESS_DENIED, (short) 0, buffer, (short) (OFFSET_RESULT_GET_VALUE & 0x00FF), VALUE_LENGTH);
    }

    /**
     * Send the given content from offset to offset+length via given APDU.
     * Encrypts the content beforehand.
     *
     * @param apdu    received APDU which is used to send the data
     * @param content the source of the content to send
     * @param offset  specifies where the message in the content source starts
     * @param length  the length of the message in the content source
     */
    private void send(APDU apdu, byte[] content, byte offset, byte length)
    {
        byte[] buffer = apdu.getBuffer();
        short len = encryptMessage(buffer, content, offset, length);

        apdu.setOutgoingAndSend((short) 0, len);
    }

    /**
     * Encrypts the given message at offset to offset+length into the buffer at offset 0
     * by using the Cryptography applet through the applet firewall.
     *
     * @param buffer  Target memory. Result will be stored at offset 0.
     * @param message Memory containing the message to encrypt
     * @param offset  Offset where the message begins
     * @param length  Length of the message
     * @return length of the encrypted message
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
     * @param buffer Source and target memory
     *               Message starts at ISO7816.OFFSET_CDATA.
     *               Result will be stored at offset 0
     * @return length of the decrypted message
     */
    private short decryptMessage(byte[] buffer)
    {
        AID cryptographyAid = JCSystem.lookupAID(CRYPTOGRAPHY_AID, (short) 0, (byte) CRYPTOGRAPHY_AID.length);
        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(cryptographyAid, CRYPTOGRAPHY_SECRET);

        return cryptoApp.decrypt(buffer, ISO7816.OFFSET_CDATA);
    }
}