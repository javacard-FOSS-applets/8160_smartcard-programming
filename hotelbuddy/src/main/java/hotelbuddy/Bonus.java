package hotelbuddy;

import javacard.framework.*;

/**
 * Created by Johannes on 29.07.2015.
 */
public class Bonus extends Applet
{
    // Java Card
    // Applet
    private static final byte BONUS_CLA = 0x42;

    // Instructions
    private static final byte INS_REGISTER_BONUS = (byte) 0xB0;
    private static final byte INS_GET_ALL_BONUS = (byte) 0xB1;
    private static final byte INS_RESET = (byte) 0xF0;

    // Other Applets
    private static final byte[] CRYPTOGRAPHY_AID = {0x43, 0x72, 0x79, 0x70, 0x74, 0x6f, 0x67, 0x72, 0x61, 0x70, 0x68, 0x79};
    private static final byte CRYPTOGRAPHY_SECRET = 0x2A;

    // Constants and Offsets
    private static final byte BONUS_LENGTH = 2;
    private static final byte GET_BONUS_OFFSET = 0;

    // Data
    private short bonusPoints;

    /**
     * Initializes the applet.
     */
    public Bonus()
    {
        register();

        // initialize internal data
        this.bonusPoints = 0;
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
        new Bonus();
    }

    public void process(APDU apdu) throws ISOException
    {
        if (selectingApplet())
        {
            ISOException.throwIt(ISO7816.SW_NO_ERROR);
            return;
        }

        byte[] buffer = apdu.getBuffer();

        if (buffer[ISO7816.OFFSET_CLA] != BONUS_CLA)
        {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
            return;
        }

        switch (buffer[ISO7816.OFFSET_INS])
        {
            case INS_REGISTER_BONUS:
                registerBonus(apdu);
                break;
            case INS_GET_ALL_BONUS:
                getAllBonus(apdu);
                break;
            case INS_RESET:
                reset(apdu);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    /**
     * Resets the bonus points to zero.
     * @param apdu the APDU received by the card.
     */
    private void reset(APDU apdu)
    {
        this.bonusPoints = 0;
    }

    /**
     * Adds the received value from APDU buffer to the bonus point pool on the card.
     * Checks and prevents overflow from happening.
     * Max bonus points are limited to max value of data type short.
     * @param apdu the APDU received by the card
     */
    private void registerBonus(APDU apdu)
    {
        byte[] buffer = apdu.getBuffer();

        short messageLength = decryptMessage(buffer);

        if (messageLength != BONUS_LENGTH)
        {
            // Data has wrong length.
            // Two bytes are expected for short value.
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        if (Util.getShort(buffer, (short) 0) <= 0)
        {
            // Invalid data, received bonus points must be positive and at least the amount of one point.
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }

        if (Short.MAX_VALUE - this.bonusPoints < Util.getShort(buffer, (short) 0))
        {
            // The addition of received bonus points would cause an overflow.
            // Maybe not the proper exception for this purpose?
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }

        this.bonusPoints += Util.getShort(buffer, (short) 0);
    }

    /**
     * Transforms the stored bonus points into a byte array and sends it.
     * @param apdu the APDU received by the card
     */
    private void getAllBonus(APDU apdu)
    {
        byte[] buffer = apdu.getBuffer();
        buffer[GET_BONUS_OFFSET] = (byte) ((this.bonusPoints & 0xFF00) >> 8);
        buffer[GET_BONUS_OFFSET + 1] = (byte) (this.bonusPoints & 0x00FF);

        send(apdu, buffer , GET_BONUS_OFFSET, BONUS_LENGTH);
    }

    /**
     * Sends the given content from offset to offset+length via given APDU.
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
