import com.licel.jcardsim.base.Simulator;
import javacard.framework.AID;
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

public class IdentificationTest
{
    private static final byte[] IdentificationAIDBytes = "|identification".getBytes();
    private static final AID IdentificationAID = new AID(IdentificationAIDBytes, (short) 0, (byte) IdentificationAIDBytes.length);

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

        sim.installApplet(IdentificationAID, Identification.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        boolean isAppletSelected = sim.selectApplet(IdentificationAID);
        System.out.println(isAppletSelected);

        System.out.println("\nExporting Public EXP...");
        byte[] otherExp = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xF2, new byte[0], (byte) 0x04);

        System.out.println("\nImporting Public EXP...");
        TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xE2, myPublicKey.getPublicExponent().toByteArray(), (byte) 0x00);

        System.out.println("\nExporting Public MOD...");
        byte[] otherMod = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xF0, new byte[0], (byte) 0x04);

        // Creating PublicKey for other party
        RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(otherMod), new BigInteger(otherExp));
        KeyFactory factory = KeyFactory.getInstance("RSA");
        PublicKey otherPublicKey = factory.generatePublic(spec);

        System.out.println("\nImporting Public MOD...");
        TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xE0, myPublicKey.getModulus().toByteArray(), (byte) 0x00);

        System.out.println("\nEncrypting...");
        byte[] encryptedMessage = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xD0, message.getBytes(), (byte) 0x03);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, myPrivateKey);
        String decryptMessage = new String(cipher.doFinal(encryptedMessage));

        Assert.assertEquals(message, decryptMessage.trim());

        cipher.init(Cipher.ENCRYPT_MODE, otherPublicKey);
        byte[] encryptMessage = cipher.doFinal(message.getBytes());

        System.out.println("\nDecrypting...");
        byte[] decryptedMessage = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xD2, encryptMessage, (byte) 0x03);

        Assert.assertEquals(message, new String(decryptedMessage).trim());
    }
}