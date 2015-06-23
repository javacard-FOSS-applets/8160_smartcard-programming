package application.crypto;


import application.log.LogHelper;
import application.log.LogLevel;
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
public class RSACryptographyHelper
{
    private Cipher rsaCipher;

    private RSAPrivateCrtKeyImpl myPrivateKey;
    private RSAPublicKeyImpl myPublicKey;

    private PublicKey otherPublicKey;

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

    public EncryptResult encrypt(String message)
    {
        try
        {
            rsaCipher.init(Cipher.ENCRYPT_MODE, this.otherPublicKey);
            return new EncryptResult(true, rsaCipher.doFinal(message.getBytes()));
        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
            return new EncryptResult(false, new byte[0]);
        }
    }

    public DecryptResult decrypt(byte[] message)
    {
        try
        {
            rsaCipher.init(Cipher.DECRYPT_MODE, this.myPrivateKey);
            return new DecryptResult(true, new String(rsaCipher.doFinal(message)).trim());
        }
        catch (Exception ex)
        {
            LogHelper.logException(ex);
            return new DecryptResult(false, "");
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
