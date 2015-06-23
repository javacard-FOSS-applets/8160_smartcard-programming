package application.crypto;


import application.log.LogHelper;
import application.log.LogLevel;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;
import sun.security.rsa.RSAPrivateCrtKeyImpl;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;

/**
 * Created by Patrick on 22.06.2015.
 */
public class RSACryptographyHelper implements IRSACryptographyHelper
{
    private static IRSACryptographyHelper instance;
    private Cipher rsaCipher;
    private RSAPrivateCrtKeyImpl myPrivateKey;
    private RSAPublicKeyImpl myPublicKey;
    private PublicKey otherPublicKey;

    private RSACryptographyHelper()
    {
    }

    public static IRSACryptographyHelper current()
    {
        return instance == null ? (instance = new RSACryptographyHelper()) : instance;
    }

    public void initialize()
    {
        try
        {
            rsaCipher = Cipher.getInstance("RSA");

            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            final KeyPair key = keyGen.generateKeyPair();

            myPrivateKey = (RSAPrivateCrtKeyImpl) key.getPrivate();
            myPublicKey = (RSAPublicKeyImpl) key.getPublic();
        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
            return;
        }

        LogHelper.log(LogLevel.INFO, "RSACryptographyHelper initialized");
    }

    public void importPublicKey(byte[] otherMod, byte[] otherExp)
    {
        byte[] mod = new byte[otherMod.length + 1];
        mod[0] = 0;
        System.arraycopy(otherMod, 0, mod, 1, otherMod.length);

        try
        {
            RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(mod), new BigInteger(otherExp));
            KeyFactory factory = KeyFactory.getInstance("RSA");
            otherPublicKey = factory.generatePublic(spec);
        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
            return;
        }

        LogHelper.log(LogLevel.INFO, "RSACryptographyHelper imported other public key");
    }

    public Result<byte[]> encrypt(String message)
    {
        try
        {
            rsaCipher.init(Cipher.ENCRYPT_MODE, this.otherPublicKey);
            return new SuccessResult<>(rsaCipher.doFinal(message.getBytes()));
        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
            return new ErrorResult<>("Encryption failed");
        }
    }

    public Result<String> decrypt(byte[] message)
    {
        try
        {
            rsaCipher.init(Cipher.DECRYPT_MODE, this.myPrivateKey);
            return new SuccessResult<>(new String(rsaCipher.doFinal(message)).trim());
        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
            return new ErrorResult<>("Decryption failed");
        }
    }

    public byte[] getPublicMod()
    {
        return myPublicKey.getModulus().toByteArray();
    }

    public byte[] getPublicExp()
    {
        return myPublicKey.getPublicExponent().toByteArray();
    }
}
