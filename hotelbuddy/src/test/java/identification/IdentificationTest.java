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
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        System.out.println("\nGetting Name...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xA1, new byte[0], (byte) 0xFF);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.encryptWasCalled());

        String answerString = new String(answer, 0, answer.length - 2);
        Assert.assertEquals(name, answerString);

        CryptographyMock.reset();
    }

    @Test
    public void Test_GetName_Called_Before_SetName_Throws_COMMAND_NOT_ALLOWED()
    {
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

        System.out.println("\nGetting Name...");
        byte[] answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xA1, new byte[0], (byte) 0xFF);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});

        CryptographyMock.reset();
    }
}