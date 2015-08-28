package hotelbuddy;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.Cipher;

public class Cryptography extends Applet implements ICryptography
{
    // Applet
    private static final short CRYPTOGRAPHY_CLA = (byte) 0x43;
    private static final byte CRYPTOGRAPHY_SECRET = 0x2A;
    private static final short KEY_SIZE = (byte) 0x40;

    // Clients
    private static final byte[] IDENTIFICATION_AID = {0x49, 0x64, 0x65, 0x6e, 0x74, 0x69, 0x66, 0x69, 0x63, 0x61, 0x74, 0x69, 0x6f, 0x6e};
    private static final byte[] ACCESS_AID = {0x41, 0x63, 0x63, 0x65, 0x73, 0x73};
    private static final byte[] BONUS_AID = {0x42, 0x6F, 0x6E, 0x75, 0x73};
    private static final byte[] CRYPTOGRAPHY_AID = {0x43, 0x72, 0x79, 0x70, 0x74, 0x6f, 0x67, 0x72, 0x61, 0x70, 0x68, 0x79};

    // Instructions
    private static final byte INS_IMPORT_CARD_PRIVATE_MOD = (byte) 0xF0;
    private static final byte INS_IMPORT_CARD_PRIVATE_EXP = (byte) 0xF1;
    private static final byte INS_IMPORT_CARD_PUBLIC_MOD = (byte) 0xF2;
    private static final byte INS_IMPORT_CARD_PUBLIC_EXP = (byte) 0xF3;
    private static final byte INS_EXPORT_CARD_PUBLIC_MOD = (byte) 0xF4;
    private static final byte INS_EXPORT_CARD_PUBLIC_EXP = (byte) 0xF5;

    private static final byte INS_IMPORT_TERMINAL_PUBLIC_MOD = (byte) 0xE0;
    private static final byte INS_IMPORT_TERMINAL_PUBLIC_EXP = (byte) 0xE1;

    // Crypto
    private RSAPrivateKey cardPrivateKey;
    private RSAPublicKey cardPublicKey;
    private RSAPublicKey terminalPublicKey;
    private MessageDigest messageDigest;
    private Signature signature;

    private Cipher rsaCipher = null;

    /**
     * Key Flags
     * 0x00 means nothing is set
     * 0x0F means modulus is set
     * 0xF0 means exponent is set
     * 0xFF means modulus and exponent is set
     */
    private byte cardPrivateKeyFlag;
    private byte cardPublicKeyFlag;
    private byte terminalPublicKeyFlag;

    protected Cryptography()
    {
        register();

        cardPrivateKeyFlag = 0x00;
        cardPrivateKey = (RSAPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, KeyBuilder.LENGTH_RSA_512, false);

        cardPublicKeyFlag = 0x00;
        cardPublicKey = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, KeyBuilder.LENGTH_RSA_512, false);

        terminalPublicKeyFlag = 0x00;
        terminalPublicKey = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, KeyBuilder.LENGTH_RSA_512, false);

        rsaCipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);

        messageDigest = MessageDigest.getInstance(MessageDigest.ALG_SHA, false);
        signature = Signature.getInstance(Signature.ALG_RSA_MD5_PKCS1, false);
    }

    public static void install(byte[] bArray, short bOffset, byte bLength)
    {
        new Cryptography();
    }

    public void process(APDU apdu)
    {
        if (selectingApplet())
        {
            ISOException.throwIt(ISO7816.SW_NO_ERROR);
            return;
        }

        byte[] buf = apdu.getBuffer();

        if (buf[ISO7816.OFFSET_CLA] != CRYPTOGRAPHY_CLA)
        {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
            return;
        }

        short messageLength;
        switch (buf[ISO7816.OFFSET_INS])
        {
            case INS_EXPORT_CARD_PUBLIC_MOD:
                exportPublicModulus(apdu);
                break;
            case INS_EXPORT_CARD_PUBLIC_EXP:
                exportPublicExponent(apdu);
                break;
            case INS_IMPORT_TERMINAL_PUBLIC_MOD:
                messageLength = (short) (buf[ISO7816.OFFSET_LC] & 0x00FF);
                importTerminalPublicModulus(apdu, messageLength);
                break;
            case INS_IMPORT_TERMINAL_PUBLIC_EXP:
                messageLength = (short) (buf[ISO7816.OFFSET_LC] & 0x00FF);
                importTerminalPublicExponent(apdu, messageLength);
                break;
            case INS_IMPORT_CARD_PRIVATE_MOD:
                messageLength = (short) (buf[ISO7816.OFFSET_LC] & 0x00FF);
                importCardPrivateModulus(apdu, messageLength);
                break;
            case INS_IMPORT_CARD_PRIVATE_EXP:
                messageLength = (short) (buf[ISO7816.OFFSET_LC] & 0x00FF);
                importCardPrivateExponent(apdu, messageLength);
                break;
            case INS_IMPORT_CARD_PUBLIC_MOD:
                messageLength = (short) (buf[ISO7816.OFFSET_LC] & 0x00FF);
                importCardPublicModulus(apdu, messageLength);
                break;
            case INS_IMPORT_CARD_PUBLIC_EXP:
                messageLength = (short) (buf[ISO7816.OFFSET_LC] & 0x00FF);
                importCardPublicExponent(apdu, messageLength);
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

    /**
     * Sets the public modulus from the card into the APDU.
     * If the public modulus is not set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     *
     * @param apdu APDU
     */
    private void exportPublicModulus(APDU apdu)
    {
        if ((cardPublicKeyFlag & 0x0F) != 0x0F)
        {
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        byte buffer[] = apdu.getBuffer();
        short modLen = cardPublicKey.getModulus(buffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, modLen);
    }

    /**
     * Sets the public exponent from the card into the APDU.
     * If the public exponent is not set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     *
     * @param apdu APDU
     */
    private void exportPublicExponent(APDU apdu)
    {
        if ((cardPublicKeyFlag & 0xF0) != 0xF0)
        {
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        byte buffer[] = apdu.getBuffer();
        short expLen = cardPublicKey.getExponent(buffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, expLen);
    }

    /**
     * Sets the passed private modulus to the private key of the card
     * If the private modulus is already set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     *
     * @param apdu APDU
     */
    private void importCardPrivateModulus(APDU apdu, short lc)
    {
        if ((cardPrivateKeyFlag & 0x0F) == 0x0F)
        {
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        apdu.setIncomingAndReceive();
        byte[] buffer = apdu.getBuffer();
        cardPrivateKey.setModulus(buffer, ISO7816.OFFSET_CDATA, lc);

        cardPrivateKeyFlag = (byte) (cardPrivateKeyFlag | 0x0F);
    }

    /**
     * Sets the passed private exponent to the private key of the card
     * If the private exponent is already set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     *
     * @param apdu APDU
     */
    private void importCardPrivateExponent(APDU apdu, short lc)
    {
        if ((cardPrivateKeyFlag & 0xF0) == 0xF0)
        {
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        apdu.setIncomingAndReceive();
        byte[] buffer = apdu.getBuffer();
        cardPrivateKey.setExponent(buffer, ISO7816.OFFSET_CDATA, lc);

        cardPrivateKeyFlag = (byte) (cardPrivateKeyFlag | 0xF0);
    }

    /**
     * Sets the passed public modulus to the public key of the card
     * If the public modulus is already set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     *
     * @param apdu APDU
     */
    private void importCardPublicModulus(APDU apdu, short lc)
    {
        if ((cardPublicKeyFlag & 0x0F) == 0x0F)
        {
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        apdu.setIncomingAndReceive();
        byte[] buffer = apdu.getBuffer();
        cardPublicKey.setModulus(buffer, ISO7816.OFFSET_CDATA, lc);

        cardPublicKeyFlag = (byte) (cardPublicKeyFlag | 0x0F);
    }

    /**
     * Sets the passed public exponent to the public key of the card
     * If the public exponent is already set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     *
     * @param apdu APDU
     */
    private void importCardPublicExponent(APDU apdu, short lc)
    {
        if ((cardPublicKeyFlag & 0xF0) == 0xF0)
        {
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        apdu.setIncomingAndReceive();
        byte[] buffer = apdu.getBuffer();
        cardPublicKey.setExponent(buffer, ISO7816.OFFSET_CDATA, lc);

        cardPublicKeyFlag = (byte) (cardPublicKeyFlag | 0xF0);
    }

    /**
     * Sets the passed public modulus to the public key of the terminal
     * If the public modulus is already set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     *
     * @param apdu APDU
     */
    private void importTerminalPublicModulus(APDU apdu, short lc)
    {
        if ((terminalPublicKeyFlag & 0x0F) == 0x0F)
        {
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        apdu.setIncomingAndReceive();
        byte[] buffer = apdu.getBuffer();
        terminalPublicKey.setModulus(buffer, ISO7816.OFFSET_CDATA, lc);

        terminalPublicKeyFlag = (byte) (terminalPublicKeyFlag | 0x0F);
    }

    /**
     * Sets the passed public exponent to the public key of the terminal
     * If the public exponent is already set, ISO7816.SW_COMMAND_NOT_ALLOWED is thrown.
     *
     * @param apdu APDU
     */
    private void importTerminalPublicExponent(APDU apdu, short lc)
    {
        if ((terminalPublicKeyFlag & 0xF0) == 0xF0)
        {
            ISOException.throwIt(ISO7816.SW_COMMAND_NOT_ALLOWED);
            return;
        }

        apdu.setIncomingAndReceive();
        byte[] buffer = apdu.getBuffer();
        terminalPublicKey.setExponent(buffer, ISO7816.OFFSET_CDATA, lc);

        terminalPublicKeyFlag = (byte) (terminalPublicKeyFlag | 0xF0);
    }

    /**
     * Encrypts the passed message with terminalPublicKey and writes it into the buffer at offset 0.
     *
     * @param buffer  apdu buffer
     * @param message message to encrypt
     * @param offset  start offset of the message
     * @param length  length of the message
     * @return length of encrypted message (usually 64 Byte)
     */
    public short encrypt(byte[] buffer, byte[] message, byte offset, byte length)
    {
        signature.init(cardPrivateKey, Signature.MODE_SIGN);
        short len = signature.sign(message, (short) offset, (short) length, buffer, KEY_SIZE);

        rsaCipher.init(terminalPublicKey, Cipher.MODE_ENCRYPT);
        short len2 = rsaCipher.doFinal(message, (short) offset, (short) length, buffer, (short) 0);

        return (short) (len + len2);
    }

    /**
     * Decrypts the passed message with cardPrivateKey and writes it into the buffer at offset 0.
     *
     * @param buffer apdu buffer
     * @param offset message to decrypt (64 Byte)
     * @return trimmed decrypted message
     */
    public short decrypt(byte[] buffer, byte offset)
    {
        rsaCipher.init(cardPrivateKey, Cipher.MODE_DECRYPT);
        short len = rsaCipher.doFinal(buffer, (short) offset, KEY_SIZE, buffer, (short) 0);

        signature.init(terminalPublicKey, Signature.MODE_VERIFY);
        if (!signature.verify(buffer, (short) 0, len, buffer, (short) (offset + KEY_SIZE), KEY_SIZE))
        {
            ISOException.throwIt(ISO7816.SW_DATA_INVALID);
        }

        return len;
    }

    /**
     * Called when other applets requesting this applet
     *
     * @param client_aid AID of the caller
     * @param parameter  secret
     * @return this applet
     */
    public Shareable getShareableInterfaceObject(AID client_aid, byte parameter)
    {
        if (!client_aid.equals(IDENTIFICATION_AID, (short) 0, (byte) IDENTIFICATION_AID.length)
                && !client_aid.equals(ACCESS_AID, (short) 0, (byte) ACCESS_AID.length)
                && !client_aid.equals(BONUS_AID, (short) 0, (byte) BONUS_AID.length))
        {
            return null;
        }

        if (parameter != CRYPTOGRAPHY_SECRET)
        {
            return null;
        }

        return this;
    }
}
