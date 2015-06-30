package hotelbuddy;

import com.licel.jcardsim.base.Simulator;
import common.TestHelper;
import javacard.framework.AID;
import javacard.framework.JCSystem;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class CryptographyTest
{
    private static final byte[] CryptographyAIDBytes = "Cryptography".getBytes();
    private static final byte[] IdentificationAIDBytes = "Identification".getBytes();
    private static final AID CryptographyAID = new AID(CryptographyAIDBytes, (short) 0, (byte) CryptographyAIDBytes.length);
    private static final AID IdentificationAID = new AID(IdentificationAIDBytes, (short) 0, (byte) IdentificationAIDBytes.length);
    private static final byte CryptographySecret = 42;

    @Test
    public void Test_Encryption() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, SignatureException
    {
        // Arrange
        String message = "This is my test-message!";

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);

        KeyPair key = keyGen.generateKeyPair();
        RSAPrivateKey terminalPrivateKey = (RSAPrivateKey) key.getPrivate();
        RSAPublicKey terminalPublicKey = (RSAPublicKey) key.getPublic();

        key = keyGen.generateKeyPair();
        RSAPrivateKey cardPrivateKey = (RSAPrivateKey) key.getPrivate();
        RSAPublicKey cardPublicKey = (RSAPublicKey) key.getPublic();

        Simulator sim = new Simulator();

        // Act
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

        System.out.println("\nSetting private modulus of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF0, cardPrivateKey.getModulus().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting private exponent of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF1, cardPrivateKey.getPrivateExponent().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting public modulus of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF2, cardPublicKey.getModulus().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting public exponent of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF3, cardPublicKey.getPublicExponent().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting public modulus of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xE0, terminalPublicKey.getModulus().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting public exponent of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xE1, terminalPublicKey.getPublicExponent().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        // Selecting other applet
        // getShareableInterfaceObject() checks the caller id
        sim.selectApplet(IdentificationAID);

        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(CryptographyAID, CryptographySecret);
        System.out.println("Encrypting...");
        byte[] buffer = new byte[128];
        short len = cryptoApp.encrypt(buffer, message.getBytes(), (byte) 0, (byte) message.length());

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, terminalPrivateKey);
        String decryptMessage = new String(cipher.doFinal(buffer)).trim();

        Assert.assertEquals(message, decryptMessage);
        Assert.assertEquals(len, 128);
    }

    @Test
    public void Test_Decryption() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException
    {
        // Arrange
        String message = "This is my test-message!";

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);

        KeyPair key = keyGen.generateKeyPair();
        RSAPublicKey terminalPublicKey = (RSAPublicKey) key.getPublic();

        key = keyGen.generateKeyPair();
        RSAPrivateKey cardPrivateKey = (RSAPrivateKey) key.getPrivate();
        RSAPublicKey cardPublicKey = (RSAPublicKey) key.getPublic();

        Simulator sim = new Simulator();

        // Act
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

        System.out.println("\nSetting private modulus of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF0, cardPrivateKey.getModulus().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting private exponent of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF1, cardPrivateKey.getPrivateExponent().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting public modulus of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF2, cardPublicKey.getModulus().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting public exponent of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF3, cardPublicKey.getPublicExponent().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting public modulus of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xE0, terminalPublicKey.getModulus().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting public exponent of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xE1, terminalPublicKey.getPublicExponent().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nExporting public modulus of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF4, new byte[0], (byte) 0x04);
        byte[] otherMod = TestHelper.GetAnswerWithoutStatus(answer);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nExporting public exponent of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF5, new byte[0], (byte) 0x04);
        byte[] otherExp = TestHelper.GetAnswerWithoutStatus(answer);
        TestHelper.EnsureStatusBytesNoError(answer);

        // Creating PublicKey for other party
        RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(otherMod), new BigInteger(otherExp));
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey otherPublicKey = factory.generatePublic(spec);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, otherPublicKey);
        byte[] buffer = cipher.doFinal(message.getBytes());

        // Selecting other applet
        // getShareableInterfaceObject() checks the caller id
        sim.selectApplet(IdentificationAID);

        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(CryptographyAID, CryptographySecret);
        System.out.println("Decrypting...");
        short len = cryptoApp.decrypt(buffer, (byte) 0x00);
        byte[] decryptedMessage = new byte[len];
        System.arraycopy(buffer, 0, decryptedMessage, 0, len);

        Assert.assertEquals(message, new String(decryptedMessage));
        Assert.assertEquals(len, 24);
    }

    @Test
    public void Test_Set_Private_Mod_Of_Card_Twice_Throws_Error() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);

        KeyPair key = keyGen.generateKeyPair();
        RSAPrivateKey cardPrivateKey = (RSAPrivateKey) key.getPrivate();

        Simulator sim = new Simulator();

        // Act
        sim.installApplet(CryptographyAID, Cryptography.class);

        boolean isAppletSelected = sim.selectApplet(CryptographyAID);
        Assert.assertTrue(isAppletSelected);

        byte[] answer;

        System.out.println("\nSetting private modulus of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF0, cardPrivateKey.getModulus().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting private modulus of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF0, cardPrivateKey.getModulus().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});
    }

    @Test
    public void Test_Set_Private_Exp_Of_Card_Twice_Throws_Error() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);

        KeyPair key = keyGen.generateKeyPair();
        RSAPrivateKey cardPrivateKey = (RSAPrivateKey) key.getPrivate();

        Simulator sim = new Simulator();

        // Act
        sim.installApplet(CryptographyAID, Cryptography.class);

        boolean isAppletSelected = sim.selectApplet(CryptographyAID);
        Assert.assertTrue(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting private exponent of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF1, cardPrivateKey.getPrivateExponent().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting private exponent of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF1, cardPrivateKey.getPrivateExponent().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});
    }

    @Test
    public void Test_Set_Public_Mod_Of_Card_Twice_Throws_Error() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);

        KeyPair key = keyGen.generateKeyPair();
        RSAPublicKey cardPublicKey = (RSAPublicKey) key.getPublic();

        Simulator sim = new Simulator();

        // Act
        sim.installApplet(CryptographyAID, Cryptography.class);

        boolean isAppletSelected = sim.selectApplet(CryptographyAID);
        Assert.assertTrue(isAppletSelected);

        byte[] answer;

        System.out.println("\nSetting public modulus of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF2, cardPublicKey.getModulus().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting public modulus of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF2, cardPublicKey.getModulus().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});
    }

    @Test
    public void Test_Set_Public_Exp_Of_Card_Twice_Throws_Error() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);

        KeyPair key = keyGen.generateKeyPair();
        RSAPublicKey cardPublicKey = (RSAPublicKey) key.getPublic();

        Simulator sim = new Simulator();

        // Act
        sim.installApplet(CryptographyAID, Cryptography.class);

        boolean isAppletSelected = sim.selectApplet(CryptographyAID);
        Assert.assertTrue(isAppletSelected);

        byte[] answer;

        System.out.println("\nSetting public exponent of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF3, cardPublicKey.getPublicExponent().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting public exponent of the card...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xF3, cardPublicKey.getPublicExponent().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});
    }

    @Test
    public void Test_Set_Public_Mod_Of_Terminal_Twice_Throws_Error() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);

        KeyPair key = keyGen.generateKeyPair();
        RSAPublicKey terminalPublicKey = (RSAPublicKey) key.getPublic();

        Simulator sim = new Simulator();

        // Act
        sim.installApplet(CryptographyAID, Cryptography.class);

        boolean isAppletSelected = sim.selectApplet(CryptographyAID);
        Assert.assertTrue(isAppletSelected);

        byte[] answer;

        System.out.println("\nSetting public modulus of the terminal...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xE0, terminalPublicKey.getModulus().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting public modulus of the terminal...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xE0, terminalPublicKey.getModulus().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});
    }

    @Test
    public void Test_Set_Public_Exp_Of_Terminal_Twice_Throws_Error() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);

        KeyPair key = keyGen.generateKeyPair();
        RSAPublicKey terminalPublicKey = (RSAPublicKey) key.getPublic();

        Simulator sim = new Simulator();

        // Act
        sim.installApplet(CryptographyAID, Cryptography.class);

        boolean isAppletSelected = sim.selectApplet(CryptographyAID);
        Assert.assertTrue(isAppletSelected);

        byte[] answer;

        System.out.println("\nSetting public exponent of the terminal...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xE1, terminalPublicKey.getPublicExponent().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting public exponent of the terminal...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x43, (byte) 0xE1, terminalPublicKey.getPublicExponent().toByteArray(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});
    }
}