package identification;

import com.licel.jcardsim.base.Simulator;
import common.CryptographyMock;
import common.TestHelper;
import javacard.framework.AID;
import org.junit.Assert;
import org.junit.Test;

public class IdentificationTest
{
    private static final byte[] CryptographyAIDBytes = "Cryptography".getBytes();
    private static final byte[] IdentificationAIDBytes = "Identification".getBytes();
    private static final AID CryptographyAID = new AID(CryptographyAIDBytes, (short) 0, (byte) CryptographyAIDBytes.length);
    private static final AID IdentificationAID = new AID(IdentificationAIDBytes, (short) 0, (byte) IdentificationAIDBytes.length);

    @Test
    public void Test_Name()
    {
        String name = "asd";

        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, CryptographyMock.class);
        sim.installApplet(IdentificationAID, Identification.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        boolean isAppletSelected = sim.selectApplet(IdentificationAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting Name");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xA0, name.getBytes(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        System.out.println("\nGetting Name...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xA1, new byte[0], (byte) 0xFF);
        TestHelper.EnsureStatusBytes(answer);
        Assert.assertTrue(CryptographyMock.encryptWasCalled());

        String answerString = new String(answer, 0, answer.length - 2);
        Assert.assertEquals(name, answerString);

        CryptographyMock.reset();
    }

    @Test
    public void Test_GetName_Called_Before_SetName_Throws_()
    {
        String name = "asd";

        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, CryptographyMock.class);
        sim.installApplet(IdentificationAID, Identification.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        boolean isAppletSelected = sim.selectApplet(IdentificationAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting Name");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xA0, name.getBytes(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        System.out.println("\nGetting Name...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xA1, new byte[0], (byte) 0xFF);
        TestHelper.EnsureStatusBytes(answer);
        Assert.assertTrue(CryptographyMock.encryptWasCalled());

        String answerString = new String(answer, 0, answer.length - 2);
        Assert.assertEquals(name, answerString);

        CryptographyMock.reset();
    }

//    @Test
//    public void Test_Decryption() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException
//    {
//        String message = "abc";
//
//        Simulator sim = new Simulator();
//
//        sim.installApplet(CryptographyAID, Cryptography.class);
//        sim.installApplet(IdentificationAID, Identification.class);
//
//        System.out.println("Getting ATR...");
//        byte[] atr = sim.getATR();
//        System.out.println(new String(atr));
//        System.out.println(TestHelper.ToHexString(atr));
//
//        System.out.println("\nSelecting Applet...");
//        boolean isAppletSelected = sim.selectApplet(CryptographyAID);
//        System.out.println(isAppletSelected);
//
//        byte[] answer;
//        System.out.println("\nExporting Public EXP...");
//        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xF2, new byte[0], (byte) 0x04);
//        byte[] otherExp = TestHelper.GetAnswerWithoutStatus(answer);
//        TestHelper.EnsureStatusBytes(answer);
//
//        System.out.println("\nExporting Public MOD...");
//        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xF0, new byte[0], (byte) 0x04);
//        byte[] otherMod = TestHelper.GetAnswerWithoutStatus(answer);
//        TestHelper.EnsureStatusBytes(answer);
//
//        // Creating PublicKey for other party
//        RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(otherMod), new BigInteger(otherExp));
//        KeyFactory factory = KeyFactory.getInstance("RSA");
//        PublicKey otherPublicKey = factory.generatePublic(spec);
//
//        Cipher cipher = Cipher.getInstance("RSA");
//        cipher.init(Cipher.ENCRYPT_MODE, otherPublicKey);
//        byte[] encryptMessage = cipher.doFinal(message.getBytes());
//
//        // Selecting other applet
//        // getShareableInterfaceObject() checks the caller id
//        sim.selectApplet(IdentificationAID);
//
//        ICryptography cryptoApp = (ICryptography) JCSystem.getAppletShareableInterfaceObject(CryptographyAID, CryptographySecret);
//        System.out.println("Decrypting...");
//        byte[] decryptedMessage = cryptoApp.decrypt(encryptMessage);
//
//        Assert.assertEquals(message, new String(decryptedMessage).trim());
//    }
}