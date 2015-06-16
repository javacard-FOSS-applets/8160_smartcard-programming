import javacard.framework.*;
import javacard.security.KeyBuilder;
import javacard.security.KeyPair;
import javacard.security.RSAPrivateCrtKey;
import javacard.security.RSAPublicKey;
import javacardx.crypto.Cipher;

public class Cryptography extends Applet implements ICryptography
{
    // Applet
    private static final short CRYPTOGRAPHY_CLA = (byte) 0x49;
    private static final byte CRYPTOGRAPHY_SECRET = 0x2A;

    // Clients
    private static final byte[] IDENTIFICATION_AID = {0x69, 0x64, 0x65, 0x6e, 0x74, 0x69, 0x66, 0x69, 0x63, 0x61, 0x74, 0x69, 0x6f, 0x6e};

    // Instructions
    private final static byte EXPORT_RSA_PUBLIC_MOD = (byte) 0xF0;
    private final static byte EXPORT_RSA_PUBLIC_EXP = (byte) 0xF2;

    private final static byte IMPORT_RSA_PUBLIC_MOD = (byte) 0xE0;
    private final static byte IMPORT_RSA_PUBLIC_EXP = (byte) 0xE2;

    private final static byte RSA_ENCODE = (byte) 0xD0;
    private final static byte RSA_DECODE = (byte) 0xD2;

    // Crypto
    private RSAPrivateCrtKey rsaPrivateKey;
    private RSAPublicKey rsaPublicKey;
    private RSAPublicKey otherPartyRsaPublicKey;

    private Cipher rsaCipher = null;

    private final static short BYTE_SIZE = 8;
    private final static short ARRAY_SIZE = 128;
    private final static short ARRAY_SIZE_BITS = BYTE_SIZE * ARRAY_SIZE;

    protected Cryptography()
    {
        register();

        KeyPair keyPair = new KeyPair(KeyPair.ALG_RSA_CRT, ARRAY_SIZE_BITS);
        keyPair.genKeyPair();

        rsaPrivateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
        rsaPublicKey = (RSAPublicKey) keyPair.getPublic();

        otherPartyRsaPublicKey =
                (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC,
                        KeyBuilder.LENGTH_RSA_1024,
                        false);

        this.rsaCipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);
    }

    public static void install(byte[] bArray, short bOffset, byte bLength)
    {
        new Cryptography();
    }

    public void process(APDU apdu)
    {
        byte[] buf = apdu.getBuffer();
        short lc = (short) (buf[ISO7816.OFFSET_LC] & (short) 0x00FF);
        switch (buf[ISO7816.OFFSET_CLA])
        {
            case CRYPTOGRAPHY_CLA:
                switch (buf[ISO7816.OFFSET_INS])
                {
                    case EXPORT_RSA_PUBLIC_MOD:
                        exportPublicModulus(apdu);
                        break;
                    case EXPORT_RSA_PUBLIC_EXP:
                        exportPublicExponent(apdu);
                        break;
                    case IMPORT_RSA_PUBLIC_MOD:
                        importPublicModulus(apdu, lc);
                        break;
                    case IMPORT_RSA_PUBLIC_EXP:
                        importPublicExponent(apdu, lc);
                        break;
//                    case RSA_ENCODE:
//                        rsa_encode(apdu);
//                        break;
//                    case RSA_DECODE:
//                        rsa_decode(apdu);
//                        break;
                    case ISO7816.CLA_ISO7816:
                        if (selectingApplet())
                        {
                            ISOException.throwIt(ISO7816.SW_NO_ERROR);
                        }
                        break;
                    default:
                        ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
                }
                break;
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
        short modLen = rsaPublicKey.getModulus(buffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, modLen);
    }

    private void exportPublicExponent(APDU apdu)
    {
        byte buffer[] = apdu.getBuffer();
        short expLen = rsaPublicKey.getExponent(buffer, (short) 0);
        apdu.setOutgoingAndSend((short) 0, expLen);
    }

    private void importPublicModulus(APDU apdu, short lc)
    {
        byte[] buffer = apdu.getBuffer();
        otherPartyRsaPublicKey.setModulus(buffer, ISO7816.OFFSET_CDATA, lc);
    }

    private void importPublicExponent(APDU apdu, short lc)
    {
        byte[] buffer = apdu.getBuffer();
        otherPartyRsaPublicKey.setExponent(buffer, ISO7816.OFFSET_CDATA, lc);
    }

//    private void rsa_encode(APDU apdu)
//    {
//        byte buffer[] = apdu.getBuffer();
//        short byteRead = apdu.setIncomingAndReceive();
//        rsaCipher.init(otherPartyRsaPublicKey, Cipher.MODE_ENCRYPT);
//        short ret =
//                rsaCipher.doFinal(
//                        buffer,
//                        (short) ISO7816.OFFSET_CDATA,
//                        byteRead,
//                        buffer,
//                        (short) 0);
//        apdu.setOutgoingAndSend((short) 0, ret);
//    }
//
//    private void rsa_decode(APDU apdu)
//    {
//        byte buffer[] = apdu.getBuffer();
//        short byteRead = apdu.setIncomingAndReceive();
//        rsaCipher.init(rsaPrivateKey, Cipher.MODE_DECRYPT);
//        short ret =
//                rsaCipher.doFinal(
//                        buffer,
//                        (short) ISO7816.OFFSET_CDATA,
//                        byteRead,
//                        buffer,
//                        (short) 0);
//        apdu.setOutgoingAndSend((short) 0, ret);
//    }

    public byte[] encrypt(byte[] message)
    {
        rsaCipher.init(otherPartyRsaPublicKey, Cipher.MODE_ENCRYPT);

        byte[] encryptedMessage = JCSystem.makeTransientByteArray((short) 128, JCSystem.CLEAR_ON_DESELECT);
        short ret = rsaCipher.doFinal(message, (short) 0, (short) message.length, encryptedMessage, (short) 0);

        return encryptedMessage;
    }

    public byte[] decrypt(byte[] message)
    {
        rsaCipher.init(rsaPrivateKey, Cipher.MODE_ENCRYPT);

        byte[] decryptedMessage = JCSystem.makeTransientByteArray((short) 128, JCSystem.CLEAR_ON_DESELECT);
        short ret = rsaCipher.doFinal(message, (short) 0, (short) message.length, decryptedMessage, (short) 0);

        return decryptedMessage;
    }

    @Override
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
