package cryptography;

import common.TestHelper;
import identification.Identification;
import com.licel.jcardsim.base.Simulator;
import javacard.framework.AID;
import javacard.framework.JCSystem;
import org.junit.Assert;
import org.junit.Test;
import sun.security.rsa.RSAPrivateCrtKeyImpl;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class CryptographyTest
{
    private static final byte[] CryptographyAIDBytes = "cryptography".getBytes();
    private static final byte[] IdentificationAIDBytes = "identification".getBytes();
    private static final AID CryptographyAID = new AID(CryptographyAIDBytes, (short) 0, (byte) CryptographyAIDBytes.length);
    private static final AID IdentificationAID = new AID(IdentificationAIDBytes, (short) 0, (byte) IdentificationAIDBytes.length);
    private static final byte CryptographySecret = 42;

    @Test
    public void Test_Encryption() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException
    {
        String message = "abc";

        final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        final KeyPair key = keyGen.generateKeyPair();

        RSAPrivateCrtKeyImpl myPrivateKey = (RSAPrivateCrtKeyImpl) key.getPrivate();
        RSAPublicKeyImpl myPublicKey = (RSAPublicKeyImpl) key.getPublic();

        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, Cryptography.class);
        sim.installApplet(IdentificationAID, Identification.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        boolean isAppletSelected = sim.selectApplet(CryptographyAID);
        System.out.println(isAppletSelected);
        Assert.assertTrue(isAppletSelected);

        byte[] answer;
        System.out.println("\nImporting Public EXP...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xE2, myPublicKey.getPublicExponent().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer);

        System.out.println("\nImporting Public MOD...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xE0, myPublicKey.getModulus().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer);

        // Selecting other applet
        // getShareableInterfaceObject() checks the caller id
        sim.selectApplet(IdentificationAID);

        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(CryptographyAID, CryptographySecret);
        System.out.println("Encrypting...");
        byte[] encryptedMessage = cryptoApp.encrypt(message.getBytes());

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, myPrivateKey);
        String decryptMessage = new String(cipher.doFinal(encryptedMessage)).trim();

        Assert.assertEquals(message, decryptMessage);
    }

    @Test
    public void Test_Decryption() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException
    {
        String message = "abc";

        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, Cryptography.class);
        sim.installApplet(IdentificationAID, Identification.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        boolean isAppletSelected = sim.selectApplet(CryptographyAID);
        System.out.println(isAppletSelected);
        Assert.assertTrue(isAppletSelected);

        byte[] answer;
        System.out.println("\nExporting Public EXP...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xF2, new byte[0], (byte) 0x04);
        byte[] otherExp = TestHelper.GetAnswerWithoutStatus(answer);
        TestHelper.EnsureStatusBytes(answer);

        System.out.println("\nExporting Public MOD...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xF0, new byte[0], (byte) 0x04);
        byte[] otherMod = TestHelper.GetAnswerWithoutStatus(answer);
        TestHelper.EnsureStatusBytes(answer);

        // Creating PublicKey for other party
        RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(otherMod), new BigInteger(otherExp));
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey otherPublicKey = factory.generatePublic(spec);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, otherPublicKey);
        byte[] encryptMessage = cipher.doFinal(message.getBytes());

        // Selecting other applet
        // getShareableInterfaceObject() checks the caller id
        sim.selectApplet(IdentificationAID);

        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(CryptographyAID, CryptographySecret);
        System.out.println("Decrypting...");
        byte[] decryptedMessage = cryptoApp.decrypt(encryptMessage);

        Assert.assertEquals(message, new String(decryptedMessage));
    }
}