package hotelbuddy;

import javacard.framework.*;

/**
 * Created by Johannes on 22.06.15.
 */
public class Access extends Applet
{
    // Java Card
    // Applet
    private static final byte ACCESS_CLA = 0x42;

    // Instructions
    private static final byte INIT_ACCESS_MEMORY = (byte) 0xA0;

    private static final byte SET_ACCESS_RIGHT = (byte) 0xC1;
    private static final byte GET_ACCESS_RIGHT = (byte) 0xC2;

    private static final byte RESET = (byte) 0xF0;

    // Other Applets
    private static final byte[] CRYPTOGRAPHY_AID = {0x43, 0x72, 0x79, 0x70, 0x74, 0x6f, 0x67, 0x72, 0x61, 0x70, 0x68, 0x79};
    private static final byte CRYPTOGRAPHY_SECRET = 0x2A;

    // Access Rights
    private static final byte[] ACCESS_DENIED = {(byte) 0x00};
    private static final byte[] ACCESS_GRANTED = {(byte) 0x10};

    // Data
    private static final byte KEY_LENGTH = 2;
    private static final byte VALUE_LENGTH = 1;
    private static final byte MAX_ENTRY_COUNT = 20;
    private static final byte INIT_ACCESS_MEMORY_LENGTH = 1;
    // Key: object to access/use, value: set permission
    private static byte[] permissionDictionary;
    private static byte nextFreeIndex;

    private static boolean initExecuted;

    /**
     * Initializes the applet.
     */
    public Access()
    {
        register();

        nextFreeIndex = 0;
        initExecuted = false;
    }

    /**
     * Installs the applet on the card.
     * @param bArray
     * @param bOffset
     * @param bLength
     */
    public static void install(byte[] bArray, short bOffset, byte bLength)
    {
        new Access();
    }

    @Override
    public void process(APDU apdu) throws ISOException
    {
        if (selectingApplet())
        {
            ISOException.throwIt(ISO7816.SW_NO_ERROR);
            return;
        }

        byte[] buf = apdu.getBuffer();

        if (buf[ISO7816.OFFSET_CLA] != ACCESS_CLA)
        {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
            return;
        }

        switch (buf[ISO7816.OFFSET_INS])
        {
            case INIT_ACCESS_MEMORY:
                initAccessMemory(apdu);
                break;
            case SET_ACCESS_RIGHT:
                setAccessRight(apdu);
                break;
            case GET_ACCESS_RIGHT:
                getAccessRight(apdu);
                break;
            case RESET:
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
        if (!initExecuted)
        {
            return;
        }

        nextFreeIndex = 0;
        initExecuted = false;

        Util.arrayFillNonAtomic(permissionDictionary, (short) 0, (short) permissionDictionary.length, (byte) 0);
    }

    /**
     * Initializes the dictionary memory with the space requested by the APDU message.
     * The message is expected to be 1 byte which represents the number of entries to be reserved.
     * @param apdu the APDU received by the card
     */
    private void initAccessMemory(APDU apdu)
    {
        if (initExecuted)
        {
            // Initialization allowed only once.
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
        }

        byte[] message = decryptMessage(apdu);

        if (message.length != INIT_ACCESS_MEMORY_LENGTH)
        {
            // Data has invalid length.
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        if (message[0] > MAX_ENTRY_COUNT)
        {
            // Data out of bounds.
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }

        // The entry count delivered via APDU multiplied with number of bytes for one entry.
        byte dictionarySize = (byte) (message[0] * (KEY_LENGTH + VALUE_LENGTH));

        permissionDictionary = new byte[dictionarySize];
        Util.arrayFillNonAtomic(permissionDictionary, (short) 0, dictionarySize, (byte) 0);

        initExecuted = true;
    }

    /**
     * Sets the access right for the key received from the APDU message.
     * The message is expected to have the following pattern:
     * [key bytes][value bytes] combined as one byte array.
     * @param apdu the APDU received by the card
     */
    private void setAccessRight(APDU apdu)
    {
        if (!initExecuted)
        {
            // Initialization must be executed before setting rights.
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
        }

        byte[] message = decryptMessage(apdu);

        if (message.length != KEY_LENGTH + VALUE_LENGTH)
        {
            // Data has invalid length.
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        byte[] key = new byte[KEY_LENGTH];
        System.arraycopy(message, 0, key, 0, key.length);

        // Check for allowed values?

        if (keyExists(key))
        {
            // Data already set, command invalid
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        if (nextFreeIndex > permissionDictionary.length - (KEY_LENGTH + VALUE_LENGTH))
        {
            // Initialized memory used up.
            // Setting new values not allowed after initial setup.
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
        }

        // At this point, setting key and value is allowed.
        Util.arrayCopy(message, (short) 0, permissionDictionary, (short) nextFreeIndex, (short) (KEY_LENGTH + VALUE_LENGTH));
    }

    /**
     * Checks if the key exists in the dictionary.
     * @param key the key to look for in the dictionary
     * @return true if key is found in dictionary, false otherwise
     */
    private boolean keyExists(byte[] key)
    {
        byte index = 0;

        while (index < permissionDictionary.length)
        {
            if (Util.arrayCompare(permissionDictionary, (short) index, key, (short) 0, KEY_LENGTH) == 0)
            {
                // Matching entry found
                return true;
            }

            index = (byte) (index + KEY_LENGTH + VALUE_LENGTH);
        }

        return false;
    }

    /**
     * Gets the access rights for the key received from the APDU message.
     * The message is expected to have the size of a dictionary key.
     * @param apdu the APDU received by the card
     */
    private void getAccessRight(APDU apdu)
    {
        if (!initExecuted)
        {
            // Initialization must be executed before getting rights.
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
        }

        byte[] message = decryptMessage(apdu);

        if (message.length != KEY_LENGTH)
        {
            // Data has invalid length.
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            return;
        }

        byte[] key = new byte[KEY_LENGTH];
        System.arraycopy(message, 0, key, 0, key.length);

        send(apdu, getValue(key));
    }

    /**
     * Gets the value from the dictionary for the given key.
     * If the key does not exist, the access will be denied.
     * @param key the key to get the value for
     * @return value from dictionary if key exists, otherwise {@code ACCESS_DENIED}
     */
    private byte[] getValue(byte[] key)
    {
        byte index = 0;
        byte[] value = new byte[VALUE_LENGTH];

        while (index < permissionDictionary.length)
        {
            if (Util.arrayCompare(permissionDictionary, (short) index, key, (short) 0, KEY_LENGTH) == 0)
            {
                // Matching entry found
                Util.arrayCopy(permissionDictionary, (short) (index + KEY_LENGTH), value, (short) 0, VALUE_LENGTH);
                return value;
            }

            index = (byte) (index + KEY_LENGTH + VALUE_LENGTH);
        }

        Util.arrayCopy(ACCESS_DENIED, (short) 0, value, (short) 0, VALUE_LENGTH);
        return value;
    }

    /**
     * Prepares response APDU and sends message to terminal.
     * @param apdu the response APDU
     * @param content the response to send
     */
    private void send(APDU apdu, byte[] content)
    {
        byte[] buffer = apdu.getBuffer();
        short len = encryptMessage(buffer, content);

        apdu.setOutgoingAndSend((short) 0, len);
    }

    /**
     * Encrypts a message with the help of the cryptography applet.
     * The encrypted message will be written in the {@code buffer} and the length of it will be returned.
     * @param buffer the buffer to write the encrypted message to
     * @param message the message to encrypt
     * @return number of bytes written in the buffer
     */
    private short encryptMessage(byte[] buffer, byte[] message)
    {
        AID cryptographyAid = JCSystem.lookupAID(CRYPTOGRAPHY_AID, (short) 0, (byte) CRYPTOGRAPHY_AID.length);
        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(cryptographyAid, CRYPTOGRAPHY_SECRET);

        return cryptoApp.encrypt(buffer, message);
    }

    /**
     * Decrypts a message with the help of the cryptography applet.
     * @param apdu command APDU that contains the message to decrypt
     * @return the decrypted message
     */
    private byte[] decryptMessage(APDU apdu)
    {
        byte[] buffer = apdu.getBuffer();

        AID cryptographyAid = JCSystem.lookupAID(CRYPTOGRAPHY_AID, (short) 0, (byte) CRYPTOGRAPHY_AID.length);
        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(cryptographyAid, CRYPTOGRAPHY_SECRET);

        short len = cryptoApp.decrypt(buffer, ISO7816.OFFSET_CDATA);
        byte[] decryptedMessage = JCSystem.makeTransientByteArray(len, JCSystem.CLEAR_ON_DESELECT);
        Util.arrayCopy(buffer, (short) 0, decryptedMessage, (short) 0, len);

        return decryptedMessage;
    }
}