package hotelbuddy;

import javacard.framework.*;
import javacard.security.KeyBuilder;
import javacard.security.RSAPrivateKey;
import javacard.security.RSAPublicKey;
import javacardx.crypto.Cipher;

public class Cryptography extends Applet implements ICryptography
{
    // Applet
    private static final short CRYPTOGRAPHY_CLA = (byte) 0x43;
    private static final byte CRYPTOGRAPHY_SECRET = 0x2A;

    // Clients
    private static final byte[] IDENTIFICATION_AID = {0x49, 0x64, 0x65, 0x6e, 0x74, 0x69, 0x66, 0x69, 0x63, 0x61, 0x74, 0x69, 0x6f, 0x6e};
    private static final byte[] CRYPTOGRAPHY_AID = {0x43, 0x72, 0x79, 0x70, 0x74, 0x6f, 0x67, 0x72, 0x61, 0x70, 0x68, 0x79};

    // Instructions
    private static final byte INS_ImportCardPrivateMod = (byte) 0xF0;
    private static final byte INS_ImportCardPrivateExp = (byte) 0xF1;
    private static final byte INS_ImportCardPublicMod = (byte) 0xF2;
    private static final byte INS_ImportCardPublicExp = (byte) 0xF3;
    private static final byte INS_ExportCardPublicMod = (byte) 0xF4;
    private static final byte INS_ExportCardPublicExp = (byte) 0xF5;

    private static final byte INS_ImportTerminalPublicMod = (byte) 0xE0;
    private static final byte INS_ImportTerminalPublicExp = (byte) 0xE1;

    private static final byte INS_RESET = (byte) 0xFF;

    // Crypto
    private RSAPrivateKey cardPrivateKey;
    private RSAPublicKey cardPublicKey;
    private RSAPublicKey terminalPublicKey;

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
        cardPrivateKey = (RSAPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, KeyBuilder.LENGTH_RSA_1024, false);

        cardPublicKeyFlag = 0x00;
        cardPublicKey = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, KeyBuilder.LENGTH_RSA_1024, false);

        terminalPublicKeyFlag = 0x00;
        terminalPublicKey = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, KeyBuilder.LENGTH_RSA_1024, false);

        rsaCipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);
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
            case INS_ExportCardPublicMod:
                exportPublicModulus(apdu);
                break;
            case INS_ExportCardPublicExp:
                exportPublicExponent(apdu);
                break;
            case INS_ImportTerminalPublicMod:
                messageLength = (short) (buf[ISO7816.OFFSET_LC] & 0xFF);
                importTerminalPublicModulus(apdu, messageLength);
                break;
            case INS_ImportTerminalPublicExp:
                messageLength = (short) (buf[ISO7816.OFFSET_LC] & 0xFF);
                importTerminalPublicExponent(apdu, messageLength);
                break;
            case INS_ImportCardPrivateMod:
                messageLength = (short) (buf[ISO7816.OFFSET_LC] & 0xFF);
                importCardPrivateModulus(apdu, messageLength);
                break;
            case INS_ImportCardPrivateExp:
                messageLength = (short) (buf[ISO7816.OFFSET_LC] & 0xFF);
                importCardPrivateExponent(apdu, messageLength);
                break;
            case INS_ImportCardPublicMod:
                messageLength = (short) (buf[ISO7816.OFFSET_LC] & 0xFF);
                importCardPublicModulus(apdu, messageLength);
                break;
            case INS_ImportCardPublicExp:
                messageLength = (short) (buf[ISO7816.OFFSET_LC] & 0xFF);
                importCardPublicExponent(apdu, messageLength);
                break;
            case INS_RESET:
                reset();
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

    private void reset()
    {
        terminalPublicKeyFlag = 0x00;
        terminalPublicKey = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, KeyBuilder.LENGTH_RSA_1024, false);
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

        byte[] buffer = apdu.getBuffer();
        terminalPublicKey.setExponent(buffer, ISO7816.OFFSET_CDATA, lc);

        terminalPublicKeyFlag = (byte) (terminalPublicKeyFlag | 0xF0);
    }

    /**
     * Encrypts the passed message with terminalPublicKey and writes it into the buffer
     *
     * @param buffer  apdu buffer
     * @param message message to encrypt
     * @return length of encrypted message (usually 128 Byte)
     */
    public short encrypt(byte[] buffer, byte[] message)
    {
        rsaCipher.init(terminalPublicKey, Cipher.MODE_ENCRYPT);
        return rsaCipher.doFinal(message, (short) 0, (short) message.length, buffer, (short) 0);
    }

    /**
     * Decrypts the passed message with cardPrivateKey and writes it into the buffer
     *
     * @param buffer  apdu buffer
     * @param offset message to decrypt (128 Byte)
     * @return trimmed decrypted message
     */
    public short decrypt(byte[] buffer, byte offset)
    {
        rsaCipher.init(cardPrivateKey, Cipher.MODE_DECRYPT);
        return rsaCipher.doFinal(buffer, (short) offset, (short) 128, buffer, (short) 0);
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
        if (!client_aid.equals(IDENTIFICATION_AID, (short) 0, (byte) IDENTIFICATION_AID.length))
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
