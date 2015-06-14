import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.security.KeyPair;
import javacard.security.RSAPrivateCrtKey;
import javacard.security.RSAPublicKey;
import javacardx.crypto.Cipher;

public class Identification extends Applet
{
    private static final short IDENTIFICATION_CLA = (byte) 0x49;

    private final static byte EXPORT_RSA_PUBLIC_MOD = (byte) 0xF0;
    private final static byte EXPORT_RSA_PUBLIC_EXP = (byte) 0xF2;

    private final static byte RSA_ENCODE = (byte) 0xD0;
    private final static byte RSA_DECODE = (byte) 0xD2;

    private RSAPrivateCrtKey rsaPrivateKey;
    private RSAPublicKey rsaPublicKey;

    private Cipher rsaCipher = null;

    private final static short BYTE_SIZE = 8;
    private final static short ARRAY_SIZE = 128;
    private final static short ARRAY_SIZE_BITS = BYTE_SIZE * ARRAY_SIZE;

    protected Identification()
    {
        register();

        KeyPair keyPair = new KeyPair(KeyPair.ALG_RSA_CRT, ARRAY_SIZE_BITS);
        keyPair.genKeyPair();

        rsaPrivateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
        rsaPublicKey = (RSAPublicKey) keyPair.getPublic();

        // Don't forget to get an instance of the appropriate cipher class
        this.rsaCipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);
    }

    public static void install(byte[] bArray, short bOffset, byte bLength)
    {
        new Identification();
    }

    public void process(APDU apdu)
    {
        byte[] buf = apdu.getBuffer();
        short lc = (short) (buf[ISO7816.OFFSET_LC] & (short) 0x00FF);
        switch (buf[ISO7816.OFFSET_CLA])
        {
            case IDENTIFICATION_CLA:
                switch (buf[ISO7816.OFFSET_INS])
                {
                    case EXPORT_RSA_PUBLIC_MOD:
                        exportPublicModulus(apdu);
                        break;
                    case EXPORT_RSA_PUBLIC_EXP:
                        exportPublicExponent(apdu);
                        break;
                    case RSA_ENCODE:
                        rsa_encode(apdu);
                        break;
                    case RSA_DECODE:
                        rsa_decode(apdu);
                        break;
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

    private void rsa_encode(APDU apdu)
    {
        byte buffer[] = apdu.getBuffer();
        short byteRead = apdu.setIncomingAndReceive();
        rsaCipher.init(rsaPublicKey, Cipher.MODE_ENCRYPT);
        short ret =
                rsaCipher.doFinal(
                        buffer,
                        (short) ISO7816.OFFSET_CDATA,
                        byteRead,
                        buffer,
                        (short) 0);
        apdu.setOutgoingAndSend((short) 0, ret);
    }

    private void rsa_decode(APDU apdu)
    {
        byte buffer[] = apdu.getBuffer();
        short byteRead = apdu.setIncomingAndReceive();
        rsaCipher.init(rsaPrivateKey, Cipher.MODE_DECRYPT);
        short ret =
                rsaCipher.doFinal(
                        buffer,
                        (short) ISO7816.OFFSET_CDATA,
                        byteRead,
                        buffer,
                        (short) 0);
        apdu.setOutgoingAndSend((short) 0, ret);
    }
}
