package hotelbuddy;

import com.licel.jcardsim.base.Simulator;
import common.CryptographyMock;
import common.TestHelper;
import javacard.framework.AID;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Johannes on 22.06.15.
 */
public class AccessTest
{
    private static final byte[] CryptographyAIDBytes = "Cryptography".getBytes();
    private static final byte[] AccessAIDBytes = "Access".getBytes();
    private static final AID CryptographyAID = new AID(CryptographyAIDBytes, (short) 0, (byte) CryptographyAIDBytes.length);
    private static final AID AccessAID = new AID(AccessAIDBytes, (short) 0, (byte) AccessAIDBytes.length);

    @Test
    public void Test_SetAccessRights_Valid()
    {
        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, CryptographyMock.class);
        sim.installApplet(AccessAID, Access.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        boolean isAppletSelected = sim.selectApplet(AccessAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nInitializing memory");
        byte[] initMessage = {(byte) 0x02};
        CryptographyMock.DataLength = 1;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xA0, initMessage, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());
        CryptographyMock.reset();

        System.out.println("\nSetting Access Right");
        // first byte: 2 entries, following the key value pairs [key 0xA0 0x01] [value 0x10] [key 0xA0 0x02] [value 0x10]
        byte[] setMessage = {(byte) 0x02, (byte) 0xA0, (byte) 0x01, (byte) 0x10, (byte) 0xA0, (byte) 0x02, (byte) 0x10};
        CryptographyMock.DataLength = (short) setMessage.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xC1, setMessage, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        byte[] expectedAnswer = { (byte) 0x10 };

        System.out.println("\nGetting Access Right for first key");
        byte[] key = {(byte) 0xA0, (byte) 0x01};
        CryptographyMock.DataLength = (short) key.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xC2, key, (byte) 0x01);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.encryptWasCalled());
        TestHelper.compareWithoutStatusBytes(expectedAnswer, answer, expectedAnswer.length);

        CryptographyMock.reset();

        System.out.println("\nGetting Access Right for second key");
        key[1] = (byte) 0x02;
        CryptographyMock.DataLength = (short) key.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xC2, key, (byte) 0x01);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.encryptWasCalled());

        TestHelper.compareWithoutStatusBytes(expectedAnswer, answer, expectedAnswer.length);

        CryptographyMock.reset();
    }

    @Test
    public void Test_SetAccessRights_NotInitialized()
    {
        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, CryptographyMock.class);
        sim.installApplet(AccessAID, Access.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        boolean isAppletSelected = sim.selectApplet(AccessAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting Access Right");
        // first byte: 2 entries, following the key value pairs [key 0xA0 0x01] [value 0x10] [key 0xA0 0x02] [value 0x10]
        byte[] setMessage = {(byte) 0x02, (byte) 0xA0, (byte) 0x01, (byte) 0x10, (byte) 0xA0, (byte) 0x02, (byte) 0x10};
        CryptographyMock.DataLength = (short) setMessage.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xC1, setMessage, (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[] { (byte) 0x69, (byte) 0x86 });

        CryptographyMock.reset();
    }
}