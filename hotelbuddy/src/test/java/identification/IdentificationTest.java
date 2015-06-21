package identification;

import com.licel.jcardsim.base.Simulator;
import common.CryptographyMock;
import common.TestHelper;
import javacard.framework.AID;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        CryptographyMock.DataLength = (short) name.length();
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

    @Test
    public void Test_SetName_Too_Long_Throws_WRONG_LENGTH()
    {
        String name = Stream.generate(() -> String.valueOf('a')).limit(51).collect(Collectors.joining());

        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, CryptographyMock.class);
        sim.installApplet(IdentificationAID, Identification.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        CryptographyMock.DataLength = (short) name.length();
        boolean isAppletSelected = sim.selectApplet(IdentificationAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting too long Name");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xA0, name.getBytes(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x67, (byte) 0x00});

        CryptographyMock.reset();
    }

    @Test
    public void Test_SetName_Too_Short_Throws_WRONG_LENGTH()
    {
        String name = "";

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
        System.out.println("\nSetting too short Name");
        CryptographyMock.DataLength = (short) name.length();
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xA0, name.getBytes(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x67, (byte) 0x00});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }

    @Test
    public void Test_SetBirthday()
    {
        byte[] birthday = {(byte) 12, (byte) 10, (byte) 19, (byte) 92};

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
        System.out.println("\nSetting Birthday");
        CryptographyMock.DataLength = (short) birthday.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xB0, birthday, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }

    @Test
    public void Test_SetBirthday_Throws_WRONG_LENGTH()
    {
        byte[] tooShortBirthday = {(byte) 12, (byte) 10, (byte) 19};
        byte[] tooLongBirthday = {(byte) 12, (byte) 10, (byte) 19, (byte) 19, (byte) 19};

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
        System.out.println("\nSetting Too Short Birthday");
        CryptographyMock.DataLength = (short) tooShortBirthday.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xB0, tooShortBirthday, (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x67, (byte) 0x00});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());
        CryptographyMock.reset();

        System.out.println("\nSetting Too Long Birthday");
        CryptographyMock.DataLength = (short) tooLongBirthday.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xB0, tooLongBirthday, (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x67, (byte) 0x00});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());
        CryptographyMock.reset();
    }

    @Test
    public void Test_SetBirthday_Throws_DATA_INVALID()
    {
        ArrayList<byte[]> wrongBirthdays = new ArrayList<>();
        wrongBirthdays.add(new byte[]{(byte) 0, (byte) 10, (byte) 19, (byte) 92});
        wrongBirthdays.add(new byte[]{(byte) 32, (byte) 10, (byte) 19, (byte) 92});
        wrongBirthdays.add(new byte[]{(byte) 12, (byte) 0, (byte) 19, (byte) 92});
        wrongBirthdays.add(new byte[]{(byte) 32, (byte) 13, (byte) 19, (byte) 92});
        wrongBirthdays.add(new byte[]{(byte) 32, (byte) 10, (byte) 18, (byte) 92});
        wrongBirthdays.add(new byte[]{(byte) 32, (byte) 10, (byte) 21, (byte) 92});
        wrongBirthdays.add(new byte[]{(byte) 32, (byte) 10, (byte) 19, (byte) -1});
        wrongBirthdays.add(new byte[]{(byte) 32, (byte) 10, (byte) 19, (byte) 100});

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

        for (byte[] message : wrongBirthdays)
        {
            byte[] answer;
            System.out.println("\nSetting Too Wrong Birthday");
            CryptographyMock.DataLength = (short) message.length;
            answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xB0, message, (byte) 0x00);
            TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x84});
            Assert.assertTrue(CryptographyMock.decryptWasCalled());
            CryptographyMock.reset();
        }
    }
}