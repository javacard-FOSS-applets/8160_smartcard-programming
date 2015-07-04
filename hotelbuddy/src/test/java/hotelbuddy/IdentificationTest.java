package hotelbuddy;

import com.licel.jcardsim.base.Simulator;
import common.CryptographyMock;
import common.TestHelper;
import javacard.framework.AID;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    public void Test_SetName_Second_Time_Throws_WRONG_LENGTH()
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
        CryptographyMock.DataLength = (short) name.length();
        boolean isAppletSelected = sim.selectApplet(IdentificationAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting Name");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xA0, name.getBytes(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting Name Second Time");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xA0, name.getBytes(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});

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
    public void Test_Birthday()
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

        CryptographyMock.DataLength = (short) birthday.length;

        byte[] answer;
        System.out.println("\nSetting Birthday");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xB0, birthday, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        System.out.println("\nGetting Birthday");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xB1, birthday, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.encryptWasCalled());

        TestHelper.compareWithoutStatusBytes(birthday, answer, birthday.length);

        CryptographyMock.reset();
    }

    @Test
    public void Test_SetBirthday_With_Wrong_Date_Lengths_Throws_WRONG_LENGTH()
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
    public void Test_SetBirthday_With_Invalid_Dates_Throws_DATA_INVALID()
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
            System.out.println("\nSetting Too Invalid Birthday");
            CryptographyMock.DataLength = (short) message.length;
            answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xB0, message, (byte) 0x00);
            TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x84});
            Assert.assertTrue(CryptographyMock.decryptWasCalled());
            CryptographyMock.reset();
        }
    }

    @Test
    public void Test_SetBirthday_Second_Time_Throws_COMMAND_NOT_ALLOWED()
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
        CryptographyMock.DataLength = (short) birthday.length;
        boolean isAppletSelected = sim.selectApplet(IdentificationAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting Birthday");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xA0, birthday, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting Birthday Second Time");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xA0, birthday, (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});

        CryptographyMock.reset();
    }

    @Test
    public void Test_GetBirthday_Before_SetBirthday_Throws_COMMAND_NOT_ALLOWED()
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

        byte[] answer;
        System.out.println("\nTry getting Birthday, before setting Birthday");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xB1, new byte[0], (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});
        Assert.assertFalse(CryptographyMock.encryptWasCalled());
        CryptographyMock.reset();
    }

    @Test
    public void Test_CheckAge_Allowed()
    {
        byte[] birthday = {(byte) 12, (byte) 10, (byte) 19, (byte) 92};

        ArrayList<byte[]> allowed = new ArrayList<>();

        allowed.add(new byte[]{(byte) 11, (byte) 10, (byte) 19, (byte) 99, (byte) 6});
        allowed.add(new byte[]{(byte) 12, (byte) 10, (byte) 19, (byte) 99, (byte) 7});
        allowed.add(new byte[]{(byte) 13, (byte) 10, (byte) 19, (byte) 99, (byte) 7});
        allowed.add(new byte[]{(byte) 12, (byte) 9, (byte) 19, (byte) 99, (byte) 6});
        allowed.add(new byte[]{(byte) 12, (byte) 11, (byte) 19, (byte) 99, (byte) 7});

        allowed.add(new byte[]{(byte) 11, (byte) 10, (byte) 20, (byte) 00, (byte) 7});
        allowed.add(new byte[]{(byte) 12, (byte) 10, (byte) 20, (byte) 00, (byte) 8});
        allowed.add(new byte[]{(byte) 13, (byte) 10, (byte) 20, (byte) 00, (byte) 8});
        allowed.add(new byte[]{(byte) 12, (byte) 9, (byte) 20, (byte) 00, (byte) 7});
        allowed.add(new byte[]{(byte) 12, (byte) 11, (byte) 20, (byte) 00, (byte) 8});

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
        CryptographyMock.reset();

        for (byte[] message : allowed)
        {
            System.out.println("\nChecking Age Allowed");
            CryptographyMock.DataLength = (short) message.length;
            answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xB2, message, (byte) 0x00);

            TestHelper.EnsureStatusBytesNoError(answer);
            Assert.assertTrue(CryptographyMock.decryptWasCalled());
            Assert.assertTrue(CryptographyMock.encryptWasCalled());
            TestHelper.compareWithoutStatusBytes(new byte[]{0x01}, answer, 1);

            CryptographyMock.reset();
        }
    }

    @Test
    public void Test_CheckAge_Not_Allowed()
    {
        byte[] birthday = {(byte) 12, (byte) 10, (byte) 19, (byte) 92};

        ArrayList<byte[]> notAllowed = new ArrayList<>();

        notAllowed.add(new byte[]{(byte) 11, (byte) 10, (byte) 19, (byte) 99, (byte) 7});
        notAllowed.add(new byte[]{(byte) 12, (byte) 10, (byte) 19, (byte) 99, (byte) 8});
        notAllowed.add(new byte[]{(byte) 13, (byte) 10, (byte) 19, (byte) 99, (byte) 8});
        notAllowed.add(new byte[]{(byte) 12, (byte) 9, (byte) 19, (byte) 99, (byte) 7});
        notAllowed.add(new byte[]{(byte) 12, (byte) 11, (byte) 19, (byte) 99, (byte) 8});

        notAllowed.add(new byte[]{(byte) 11, (byte) 10, (byte) 20, (byte) 00, (byte) 8});
        notAllowed.add(new byte[]{(byte) 12, (byte) 10, (byte) 20, (byte) 00, (byte) 9});
        notAllowed.add(new byte[]{(byte) 13, (byte) 10, (byte) 20, (byte) 00, (byte) 9});
        notAllowed.add(new byte[]{(byte) 12, (byte) 9, (byte) 20, (byte) 00, (byte) 8});
        notAllowed.add(new byte[]{(byte) 12, (byte) 11, (byte) 20, (byte) 00, (byte) 9});

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
        CryptographyMock.reset();

        for (byte[] message : notAllowed)
        {
            System.out.println("\nChecking Age Allowed");
            CryptographyMock.DataLength = (short) message.length;
            answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xB2, message, (byte) 0x00);

            TestHelper.EnsureStatusBytesNoError(answer);
            Assert.assertTrue(CryptographyMock.decryptWasCalled());
            Assert.assertTrue(CryptographyMock.encryptWasCalled());
            TestHelper.compareWithoutStatusBytes(new byte[]{0x00}, answer, 1);

            CryptographyMock.reset();
        }
    }

    @Test
    public void Test_CarId()
    {
        String carId = "LPP7700";

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
        System.out.println("\nSetting CarId...");
        CryptographyMock.DataLength = (short) carId.length();
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xC0, carId.getBytes(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        System.out.println("\nGetting CarId...");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xC1, new byte[0], (byte) 0xFF);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.encryptWasCalled());

        String answerString = new String(answer, 0, answer.length - 2);
        Assert.assertEquals(carId, answerString);

        CryptographyMock.reset();
    }

    @Test
    public void Test_GetCarId_Called_Before_SetCarId_Throws_COMMAND_NOT_ALLOWED()
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

        System.out.println("\nGetting CarId...");
        byte[] answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xC1, new byte[0], (byte) 0xFF);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});

        CryptographyMock.reset();
    }

    @Test
    public void Test_SetCarId_Too_Long_Throws_WRONG_LENGTH()
    {
        String name = Stream.generate(() -> String.valueOf('a')).limit(9).collect(Collectors.joining());

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
        System.out.println("\nSetting too long CarId");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xC0, name.getBytes(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x67, (byte) 0x00});

        CryptographyMock.reset();
    }

    @Test
    public void Test_SetCarId_Second_Time_Throws_WRONG_LENGTH()
    {
        String carId = "LPP7700";

        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, CryptographyMock.class);
        sim.installApplet(IdentificationAID, Identification.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        CryptographyMock.DataLength = (short) carId.length();
        boolean isAppletSelected = sim.selectApplet(IdentificationAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting CarId");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xC0, carId.getBytes(), (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting CarId Second Time");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xC0, carId.getBytes(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});

        CryptographyMock.reset();
    }

    @Test
    public void Test_SetCarId_Too_Short_Throws_WRONG_LENGTH()
    {
        String carId = "";

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
        System.out.println("\nSetting too short CarId");
        CryptographyMock.DataLength = (short) carId.length();
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xC0, carId.getBytes(), (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x67, (byte) 0x00});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }

    @Test
    public void Test_SetSafePin()
    {
        byte[] pin = {(byte) 0, (byte) 1, (byte) 8, (byte) 9};

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

        CryptographyMock.DataLength = (short) pin.length;

        byte[] answer;
        System.out.println("\nSetting SafePin");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xD0, pin, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());

        CryptographyMock.reset();
    }

    @Test
    public void Test_SetSafePin_With_Wrong_Date_Lengths_Throws_WRONG_LENGTH()
    {
        byte[] tooShortSafePin = {(byte) 0, (byte) 1, (byte) 8};
        byte[] tooLongSafePin = {(byte) 0, (byte) 1, (byte) 8, (byte) 9, (byte) 2};

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
        System.out.println("\nSetting Too Short SafePin");
        CryptographyMock.DataLength = (short) tooShortSafePin.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xD0, tooShortSafePin, (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x67, (byte) 0x00});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());
        CryptographyMock.reset();

        System.out.println("\nSetting Too Long SafePin");
        CryptographyMock.DataLength = (short) tooLongSafePin.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xD0, tooLongSafePin, (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x67, (byte) 0x00});
        Assert.assertTrue(CryptographyMock.decryptWasCalled());
        CryptographyMock.reset();
    }

    @Test
    public void Test_SetSafePin_With_Invalid_SafePins_Throws_DATA_INVALID()
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

        for (boolean[] errors : TestHelper.getBinary(4))
        {
            if (Arrays.equals(errors, new boolean[]{false, false, false, false}))
            {
                continue;
            }

            List<byte[]> messages = createErrorPins(errors, 0, 4);

            for (byte[] message : messages)
            {
                byte[] answer;
                System.out.println("\nSetting Invalid Pin...");
                CryptographyMock.DataLength = (short) message.length;
                answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xD0, message, (byte) 0x00);
                TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x84});
                Assert.assertTrue(CryptographyMock.decryptWasCalled());
                CryptographyMock.reset();
            }
        }
    }

    private ArrayList<byte[]> createErrorPins(boolean[] errors, int currentPosition, int maxPosition)
    {
        if (currentPosition == maxPosition)
        {
            ArrayList pins = new ArrayList<byte[]>();
            pins.add(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0});
            return pins;
        }

        ArrayList<byte[]> pins = createErrorPins(errors, currentPosition + 1, maxPosition);

        if (errors[currentPosition])
        {
            for (byte[] pin : pins)
            {
                pin[currentPosition] = (byte) 10;
            }

            for (byte[] pin : TestHelper.cloneList(pins))
            {
                pin[currentPosition] = (byte) -1;
                pins.add(pin);
            }
        }

        return pins;
    }

    @Test
    public void Test_SetSafePin_Second_Time_Throws_COMMAND_NOT_ALLOWED()
    {
        byte[] pin = {(byte) 0, (byte) 1, (byte) 8, (byte) 9};

        Simulator sim = new Simulator();

        sim.installApplet(CryptographyAID, CryptographyMock.class);
        sim.installApplet(IdentificationAID, Identification.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applet...");
        CryptographyMock.DataLength = (short) pin.length;
        boolean isAppletSelected = sim.selectApplet(IdentificationAID);
        System.out.println(isAppletSelected);

        byte[] answer;
        System.out.println("\nSetting SafePin");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xD0, pin, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);

        System.out.println("\nSetting SafePin Second Time");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xD0, pin, (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});

        CryptographyMock.reset();
    }

    @Test
    public void Test_CheckSafePin_Before_SetSafePin_Throws_COMMAND_NOT_ALLOWED()
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

        byte[] answer;
        System.out.println("\nTry getting SafePin, before setting SafePin");
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xD1, new byte[0], (byte) 0x00);
        TestHelper.EnsureStatusBytes(answer, new byte[]{(byte) 0x69, (byte) 0x86});
        Assert.assertFalse(CryptographyMock.encryptWasCalled());
        CryptographyMock.reset();
    }

    @Test
    public void Test_CheckPin()
    {
        byte[] pin = {(byte) 0, (byte) 0, (byte) 0, (byte) 0};

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
        CryptographyMock.DataLength = (short) pin.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xD0, pin, (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        CryptographyMock.reset();

        for (byte one = 0; one < 10; one++)
        {
            for (byte two = 0; two < 10; two++)
            {
                for (byte three = 0; three < 10; three++)
                {
                    for (byte four = 0; four < 10; four++)
                    {
                        byte[] message = new byte[]{one, two, three, four};

                        if (Arrays.equals(message, pin))
                        {
                            continue;
                        }

                        System.out.println("\nChecking Wrong Pin");
                        CryptographyMock.DataLength = (short) message.length;
                        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xD1, message, (byte) 0x00);

                        TestHelper.EnsureStatusBytesNoError(answer);
                        Assert.assertTrue(CryptographyMock.decryptWasCalled());
                        Assert.assertTrue(CryptographyMock.encryptWasCalled());
                        TestHelper.compareWithoutStatusBytes(new byte[]{0x00}, answer, 1);

                        CryptographyMock.reset();
                    }
                }
            }
        }

        System.out.println("\nChecking Right Pin");
        CryptographyMock.DataLength = (short) pin.length;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xD1, pin, (byte) 0x00);

        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertTrue(CryptographyMock.decryptWasCalled());
        Assert.assertTrue(CryptographyMock.encryptWasCalled());
        TestHelper.compareWithoutStatusBytes(new byte[]{0x01}, answer, 1);

        CryptographyMock.reset();
    }

    @Test
    public void Test_Reset()
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

        byte[] answer;
        System.out.println("\nResetting");
        CryptographyMock.DataLength = (short) 0;
        answer = TestHelper.ExecuteCommand(sim, (byte) 0x49, (byte) 0xFF, new byte[0], (byte) 0x00);
        TestHelper.EnsureStatusBytesNoError(answer);
        Assert.assertFalse(CryptographyMock.decryptWasCalled());
        Assert.assertFalse(CryptographyMock.encryptWasCalled());
        CryptographyMock.reset();
    }
}