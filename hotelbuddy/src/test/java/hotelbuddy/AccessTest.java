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
    public void Test_SetAccessRights_Error_NotInitialized()
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
        // Error: command not allowed
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});

        CryptographyMock.reset();
    }

    @Test
    public void Test_SetAccessRights_Error_InvalidMessage()
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
        // minimal message (one pair) with missing value
        byte[] setMessage = {(byte) 0x02, (byte) 0xA0, (byte) 0x01};
        CryptographyMock.DataLength = (short) setMessage.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xC1, setMessage, (byte) 0x00);
        // Error: wrong length
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x67, (byte) 0x00});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }

    @Test
    public void Test_SetAccessRights_Error_ExceedingInitializedMemory()
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
        byte[] initMessage = {(byte) 0x01};
        CryptographyMock.DataLength = 1;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xA0, initMessage, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());
        CryptographyMock.reset();

        System.out.println("\nSetting Access Right");
        // two entries
        byte[] setMessage = {(byte) 0x02, (byte) 0xA0, (byte) 0x01, (byte) 0x10, (byte) 0xA0, (byte) 0x02, (byte) 0x10};
        CryptographyMock.DataLength = (short) setMessage.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xC1, setMessage, (byte) 0x00);
        // Error: data invalid
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x84});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }

    @Test
    public void Test_SetAccessRights_Error_UnknownValue()
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
        byte[] initMessage = {(byte) 0x01};
        CryptographyMock.DataLength = 1;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xA0, initMessage, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());
        CryptographyMock.reset();

        System.out.println("\nSetting Access Right");
        // unknown value 0x22
        byte[] setMessage = {(byte) 0x01, (byte) 0xA0, (byte) 0x01, (byte) 0x22};
        CryptographyMock.DataLength = (short) setMessage.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xC1, setMessage, (byte) 0x00);
        // Error: data invalid
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x84});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }

    @Test
    public void Test_SetAccessRights_Error_KeyDuplicate()
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
        byte[] initMessage = {(byte) 0x01};
        CryptographyMock.DataLength = 1;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xA0, initMessage, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());
        CryptographyMock.reset();

        System.out.println("\nSetting Access Right");
        // same keys for both entries (0xA0, 0x01)
        byte[] setMessage = {(byte) 0x02, (byte) 0xA0, (byte) 0x01, (byte) 0x10, (byte) 0xA0, (byte) 0x01, (byte) 0x10};
        CryptographyMock.DataLength = (short) setMessage.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xC1, setMessage, (byte) 0x00);
        // Error: data invalid
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x84});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }

    @Test
    public void Test_GetAccessRight_Error_NotInitialized()
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
        System.out.println("\nGetting Access Right");
        byte[] key = {(byte) 0xA0, (byte) 0x01};
        CryptographyMock.DataLength = (short) key.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xC2, key, (byte) 0x01);
        // Error: command not allowed
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});

        CryptographyMock.reset();
    }

    @Test
    public void Test_GetAccessRight_Error_RightsNotSet()
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

        System.out.println("\nGetting Access Right for first key");
        byte[] key = {(byte) 0xA0, (byte) 0x01};
        CryptographyMock.DataLength = (short) key.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xC2, key, (byte) 0x01);
        // Error: command not allowed
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});

        CryptographyMock.reset();
    }

    @Test
    public void Test_GetAccessRights_Error_InvalidMessageLength()
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

        System.out.println("\nGetting Access Right");
        byte[] key = {(byte) 0xA0};
        CryptographyMock.DataLength = (short) key.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xC2, key, (byte) 0x01);
        // Error: wrong length
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x67, (byte) 0x00});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }

    @Test
    public void Test_InitAccessMemory_Error_AlreadyInitialized()
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
        byte[] initMessage = {(byte) 0x01};
        CryptographyMock.DataLength = 1;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xA0, initMessage, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());
        CryptographyMock.reset();

        System.out.println("\nInitializing memory second time");
        CryptographyMock.DataLength = 1;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xA0, initMessage, (byte) 0x00);
        // Error: command not allowed
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});

        CryptographyMock.reset();
    }

    @Test
    public void Test_InitAccessMemory_Error_InvalidMessageLength()
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
        byte[] initMessage = {(byte) 0x01, (byte) 0x01};
        CryptographyMock.DataLength = 2;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xA0, initMessage, (byte) 0x00);
        // Error: wrong length
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x67, (byte) 0x00});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }

    @Test
    public void Test_InitAccessMemory_Error_DataOutOfBounds()
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
        byte[] initMessage = {(byte) 0xA0};
        CryptographyMock.DataLength = 1;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x41, (byte) 0xA0, initMessage, (byte) 0x00);
        // Error: data invalid
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x84});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }
}