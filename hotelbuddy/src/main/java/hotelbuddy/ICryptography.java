package hotelbuddy;

import javacard.framework.Shareable;

/**
 * Created by Patrick on 16.06.2015.
 */
public interface ICryptography extends Shareable
{
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
