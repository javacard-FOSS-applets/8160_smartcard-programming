package application.crypto;

import common.Result;

import java.math.BigInteger;
import java.security.PublicKey;

/**
 * Created by Patrick on 23.06.2015.
 */
public interface IRSACryptographyHelper
{
    Result<Boolean> setTerminalKeys(BigInteger privateMod, BigInteger privateExp, BigInteger publicMod, BigInteger publicExp);

    void setCardPublicKey(BigInteger modulus, BigInteger exponent);

    Result<byte[]> encrypt(String message);

    Result<byte[]> encrypt(byte[] message);

    Result<String> decrypt(byte[] message);

    byte[] getPublicMod();

    byte[] getPublicExp();
}
