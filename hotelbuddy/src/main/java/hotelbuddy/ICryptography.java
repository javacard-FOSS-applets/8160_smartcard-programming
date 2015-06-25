package hotelbuddy;

import javacard.framework.Shareable;

/**
 * Created by Patrick on 16.06.2015.
 */
public interface ICryptography extends Shareable
{
    /**
     * Encrypts the passed message with terminalPublicKey and writes it into the buffer
     *
     * @param buffer  apdu buffer
     * @param message message to encrypt
     * @return length of encrypted message (usually 128 Byte)
     */
    short encrypt(byte[] buffer, byte[] message);

    /**
     * Decrypts the passed message with cardPrivateKey and writes it into the buffer
     *
     * @param buffer  apdu buffer
     * @param offset message to decrypt (128 Byte)
     * @return trimmed decrypted message
     */
    short decrypt(byte[] buffer, byte offset);
}
