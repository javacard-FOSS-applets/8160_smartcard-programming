package hotelbuddy;

import com.licel.jcardsim.base.Simulator;
import common.CryptographyMock;
import common.TestHelper;
import javacard.framework.AID;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Johannes on 29.07.2015.
 */
public class BonusTest
{
    private static final byte[] CryptographyAIDBytes = "Cryptography".getBytes();
    private static final byte[] BonusAIDBytes = "Bonus".getBytes();
    private static final AID CryptographyAID = new AID(CryptographyAIDBytes, (short) 0, (byte) CryptographyAIDBytes.length);
    private static final AID BonusAID = new AID(BonusAIDBytes, (short) 0, (byte) BonusAIDBytes.length);

    @Test
    public void Test_RegisterBonus_Valid()
    {
        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, CryptographyMock.class);
        sim.installApplet(BonusAID, Bonus.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        boolean isAppletSelected = sim.selectApplet(BonusAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting bonus points");
        byte[] setMessage = { (byte) 0x75, (byte) 0x30 };
        CryptographyMock.DataLength = (short) setMessage.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x42, (byte) 0xB0, setMessage, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        byte[] expectedAnswer = { (byte) 0x75, (byte) 0x30 };
        System.out.println("\nGetting bonus points from card");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x42, (byte) 0xB1, new byte[0], (byte) 0x02);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.encryptWasCalled());
        TestHelper.compareWithoutStatusBytes(expectedAnswer, answer, expectedAnswer.length);

        CryptographyMock.reset();
    }

    @Test
    public void Test_RegisterBonus_Error_WrongMessageLength()
    {
        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, CryptographyMock.class);
        sim.installApplet(BonusAID, Bonus.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        boolean isAppletSelected = sim.selectApplet(BonusAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting bonus points");
        byte[] setMessage = { (byte) 0xFF };
        CryptographyMock.DataLength = (short) setMessage.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x42, (byte) 0xB0, setMessage, (byte) 0x00);
        // Error: wrong length
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x67, (byte) 0x00});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }

    @Test
    public void Test_RegisterBonus_Error_AddingNegativePoints()
    {
        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, CryptographyMock.class);
        sim.installApplet(BonusAID, Bonus.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        boolean isAppletSelected = sim.selectApplet(BonusAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting bonus points");
        byte[] setMessage = { (byte) 0xFF, (byte) 0xFF };
        CryptographyMock.DataLength = (short) setMessage.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x42, (byte) 0xB0, setMessage, (byte) 0x00);
        // Error: data invalid
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x84});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }

    @Test
    public void Test_RegisterBonus_Error_AddingNoPoints()
    {
        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, CryptographyMock.class);
        sim.installApplet(BonusAID, Bonus.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        boolean isAppletSelected = sim.selectApplet(BonusAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting bonus points");
        byte[] setMessage = { (byte) 0x00, (byte) 0x00 };
        CryptographyMock.DataLength = (short) setMessage.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x42, (byte) 0xB0, setMessage, (byte) 0x00);
        // Error: data invalid
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x84});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }

    @Test
    public void Test_RegisterBonus_Error_BonusOverflow()
    {
        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, CryptographyMock.class);
        sim.installApplet(BonusAID, Bonus.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        boolean isAppletSelected = sim.selectApplet(BonusAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting bonus points");
        byte[] setMessage = { (byte) 0x75, (byte) 0x00 };
        CryptographyMock.DataLength = (short) setMessage.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x42, (byte) 0xB0, setMessage, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        System.out.println("\nSetting bonus points which causes overflow");
        CryptographyMock.DataLength = (short) setMessage.length;
        // sending same value again which causes overflow
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x42, (byte) 0xB0, setMessage, (byte) 0x00);
        // Error: conditions not satisfied
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x85});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }

    @Test
    public void Test_Reset()
    {
        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, CryptographyMock.class);
        sim.installApplet(BonusAID, Bonus.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        boolean isAppletSelected = sim.selectApplet(BonusAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting bonus points");
        byte[] setMessage = { (byte) 0x75, (byte) 0x30 };
        CryptographyMock.DataLength = (short) setMessage.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x42, (byte) 0xB0, setMessage, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        System.out.println("\nReset the bonus points");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x42, (byte) 0xF0, new byte[0], (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        byte[] expectedAnswer = { (byte) 0x00, (byte) 0x00 };
        System.out.println("\nGetting bonus points from card");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x42, (byte) 0xB1, new byte[0], (byte) 0x02);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.encryptWasCalled());
        TestHelper.compareWithoutStatusBytes(expectedAnswer, answer, expectedAnswer.length);

        CryptographyMock.reset();
    }
}
