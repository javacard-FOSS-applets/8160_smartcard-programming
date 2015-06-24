package application;

import org.junit.Test;
import sun.security.rsa.RSAPrivateCrtKeyImpl;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.crypto.Cipher;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

/**
 * Created by Patrick on 19.06.2015.
 */
public class SmartCardConnectorTest
{
    @Test
    public void testConnect() throws Exception
    {
        Cipher rsaCipher;
        RSAPrivateCrtKeyImpl myPrivateKey;
        RSAPublicKeyImpl myPublicKey;
        PublicKey otherPublicKey;

        rsaCipher = Cipher.getInstance("RSA");

        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        final KeyPair key = keyGen.generateKeyPair();

        myPrivateKey = (RSAPrivateCrtKeyImpl) key.getPrivate();
        myPublicKey = (RSAPublicKeyImpl) key.getPublic();

        PrintWriter writer = new PrintWriter("key.txt", "UTF-8");
        writer.println(myPrivateKey.getModulus());
        writer.println(myPrivateKey.getPrivateExponent());
        writer.println(myPublicKey.getModulus());
        writer.println(myPublicKey.getPublicExponent());
        writer.close();
    }
}