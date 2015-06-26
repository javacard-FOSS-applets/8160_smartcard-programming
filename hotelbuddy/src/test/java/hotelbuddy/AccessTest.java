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
    public void test_setAccessRight_valid()
    {
        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, CryptographyMock.class);
        sim.installApplet(AccessAID, Identification.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        boolean isAppletSelected = sim.selectApplet(AccessAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nInitializing memory");
        byte[] initMessage = {(byte) 0x01};
        CryptographyMock.DataLength = 1;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x42, (byte) 0xA0, initMessage, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());
        CryptographyMock.reset();

        System.out.println("\nSetting Access Right");
        byte[] accessPair = {(byte) 0xA0, (byte) 0x01, (byte) 0x10};
        CryptographyMock.DataLength = (short) accessPair.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x42, (byte) 0xA0, accessPair, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        System.out.println("\nGetting Access Right");
        byte[] key = {(byte) 0xA0, (byte) 0x01};
        CryptographyMock.DataLength = (short) accessPair.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x42, (byte) 0xA0, key, (byte) 0x01);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.encryptWasCalled());

        byte[] expectedAnswer = { (byte) 0x10 };
        TestHelper.compareWithoutStatusBytes(expectedAnswer, answer, expectedAnswer.length);

        CryptographyMock.reset();
    }
}