package hotelbuddy;

import javacard.framework.*;
import javacard.security.*;
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

    // Crypto
    private RSAPrivateKey cardPrivateKey;
    private RSAPublicKey cardPublicKey;
    private RSAPublicKey terminalPublicKey;

    private Cipher rsaCipher = null;

    private final static short BYTE_SIZE = 8;
    private final static short ARRAY_SIZE = 128;
    private final static short ARRAY_SIZE_BITS = BYTE_SIZE * ARRAY_SIZE;

    protected Cryptography()
    {
        register();

        KeyPair keyPair = new KeyPair(KeyPair.ALG_RSA_CRT, ARRAY_SIZE_BITS);
        keyPair.genKeyPair();

        cardPrivateKey = (RSAPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, KeyBuilder.LENGTH_RSA_1024, false);
        cardPublicKey = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, KeyBuilder.LENGTH_RSA_1024, false);
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

    private void exportPublicModulus(APDU apdu)
    {
        byte buffer[] = apdu.getBuffer();
        short modLen = cardPublicKey.getModulus(buffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, modLen);
    }

    private void exportPublicExponent(APDU apdu)
    {
        byte buffer[] = apdu.getBuffer();
        short expLen = cardPublicKey.getExponent(buffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, expLen);
    }

    private void importTerminalPublicModulus(APDU apdu, short lc)
    {
        byte[] buffer = apdu.getBuffer();
        terminalPublicKey.setModulus(buffer, ISO7816.OFFSET_CDATA, lc);
    }

    private void importTerminalPublicExponent(APDU apdu, short lc)
    {
        byte[] buffer = apdu.getBuffer();
        terminalPublicKey.setExponent(buffer, ISO7816.OFFSET_CDATA, lc);
    }

    private void importCardPrivateModulus(APDU apdu, short lc)
    {
        byte[] buffer = apdu.getBuffer();
        cardPrivateKey.setModulus(buffer, ISO7816.OFFSET_CDATA, lc);
    }

    private void importCardPrivateExponent(APDU apdu, short lc)
    {
        byte[] buffer = apdu.getBuffer();
        cardPrivateKey.setExponent(buffer, ISO7816.OFFSET_CDATA, lc);
    }

    private void importCardPublicModulus(APDU apdu, short lc)
    {
        byte[] buffer = apdu.getBuffer();
        cardPublicKey.setModulus(buffer, ISO7816.OFFSET_CDATA, lc);
    }

    private void importCardPublicExponent(APDU apdu, short lc)
    {
        byte[] buffer = apdu.getBuffer();
        cardPublicKey.setExponent(buffer, ISO7816.OFFSET_CDATA, lc);
    }

    public byte[] encrypt(byte[] message)
    {
        byte[] encryptedMessage = JCSystem.makeTransientByteArray((short) 128, JCSystem.CLEAR_ON_DESELECT);

        rsaCipher.init(terminalPublicKey, Cipher.MODE_ENCRYPT);
        rsaCipher.doFinal(message, (short) 0, (short) message.length, encryptedMessage, (short) 0);

        return encryptedMessage;
    }

    public byte[] decrypt(byte[] message)
    {
        byte[] decryptedMessage = JCSystem.makeTransientByteArray((short) 128, JCSystem.CLEAR_ON_DESELECT);

        rsaCipher.init(cardPrivateKey, Cipher.MODE_DECRYPT);
        short decryptedMessageLength = rsaCipher.doFinal(message, (short) 0, (short) message.length, decryptedMessage, (short) 0);

        byte[] response = JCSystem.makeTransientByteArray(decryptedMessageLength, JCSystem.CLEAR_ON_DESELECT);
        Util.arrayCopy(decryptedMessage, (short) 0, response, (short) 0, decryptedMessageLength);

        return response;
    }

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
