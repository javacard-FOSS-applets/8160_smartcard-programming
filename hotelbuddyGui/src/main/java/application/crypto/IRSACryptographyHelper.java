package application.crypto;

import common.Result;

/**
 * Created by Patrick on 23.06.2015.
 */
public interface IRSACryptographyHelper
{
    void initialize();

    void importPublicKey(byte[] otherMod, byte[] otherExp);

    Result<byte[]> encrypt(String message);

    Result<String> decrypt(byte[] message);

    byte[] getPublicMod();

    byte[] getPublicExp();
}
