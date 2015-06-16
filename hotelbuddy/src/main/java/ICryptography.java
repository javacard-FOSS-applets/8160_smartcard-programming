import javacard.framework.Shareable;

/**
 * Created by Patrick on 16.06.2015.
 */
public interface ICryptography extends Shareable
{
    short REASON_OK = (short) 0x00;
    short SECRETE_BAD = (short) 0x01;
    short AID_BAD = (short) 0x02;
    short CALLER_AID_NEQ_GIVEN_AID = (short) 0x03;

    /**
     * Encrypts the message with the imported key
     *
     * @param message message to encrypt
     * @return encrypted message
     */
    byte[] encrypt(byte[] message);

    /**
     * Decrypts the message with the imported key
     *
     * @param message message to decrypt
     * @return decrypted message
     */
    byte[] decrypt(byte[] message);
}
