package application.crypto;

import common.Result;

import java.math.BigInteger;
import java.security.PublicKey;

/**
 * Created by Patrick on 23.06.2015.
 */
public interface IRSACryptographyHelper
{
    Result<Boolean> importTerminalKeyFromFile();

    void setCardPublicKey(BigInteger modulus, BigInteger exponent);

    Result<byte[]> encrypt(byte[] message);

    Result<byte[]> decrypt(byte[] message);

    byte[] getPublicMod();

    byte[] getPublicExp();
}
