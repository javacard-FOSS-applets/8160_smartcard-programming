import com.licel.jcardsim.base.Simulator;
import javacard.framework.AID;
import org.junit.Assert;
import org.junit.Test;

public class IdentificationTest
{
    private static final byte[] IdentificationAIDBytes = "|identification".getBytes();
    private static final AID IdentificationAID = new AID(IdentificationAIDBytes, (short) 0, (byte) IdentificationAIDBytes.length);

    @Test
    public void Test_Encryption()
    {
        Simulator sim = new Simulator();

        sim.installApplet(IdentificationAID, Identification.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(TestHelper.ToHexString(atr));

        System.out.println("\nSelecting Applect...");
        boolean isAppletSelected = sim.selectApplet(IdentificationAID);
        System.out.println(isAppletSelected);

        System.out.println("\nExporting Public EXP...");
        System.out.println("Sending command...");
        byte[] command = new byte[]{(byte) 0x49, (byte) 0xF2, 0x00, 0x00, 0x04};
        System.out.println(TestHelper.ToHexString(command));

        System.out.println("\nAnswer...");
        byte[] answer = sim.transmitCommand(command);
        System.out.println(TestHelper.ToHexString(answer));
        String answerString = new String(answer, 0, answer.length - 2);
        System.out.println(answerString);

        System.out.println("\nExporting Public MOD...");
        System.out.println("Sending command...");
        command = new byte[]{(byte) 0x49, (byte) 0xF0, 0x00, 0x00, (byte) 0x80};
        System.out.println(TestHelper.ToHexString(command));

        System.out.println("\nAnswer...");
        answer = sim.transmitCommand(command);
        System.out.println(TestHelper.ToHexString(answer));
        answerString = new String(answer, 0, answer.length - 2);
        System.out.println(answerString);

        System.out.println("\nEncrypting...");
        System.out.println("Sending command...");
        command = TestHelper.ToCommand((byte) 0x49, (byte) 0xD0, "abc".getBytes(), (byte) 0x03);
        System.out.println(TestHelper.ToHexString(command));

        System.out.println("\nAnswer...");
        answer = sim.transmitCommand(command);
        System.out.println(TestHelper.ToHexString(answer));
        answerString = new String(answer, 0, answer.length - 2);
        System.out.println(answerString);

        System.out.println("\nDecrypting...");
        System.out.println("Sending command...");
        byte[] encryptedText = new byte[answer.length - 2];
        System.arraycopy(answer, 0, encryptedText, 0, encryptedText.length);
        command = TestHelper.ToCommand((byte) 0x49, (byte) 0xD2, encryptedText, (byte) 0x03);
        System.out.println(TestHelper.ToHexString(command));

        System.out.println("\nAnswer...");
        answer = sim.transmitCommand(command);
        System.out.println(TestHelper.ToHexString(answer));
        answerString = new String(answer, 0, answer.length - 2);
        System.out.println(answerString);

        Assert.assertEquals("abc", answerString.trim());    }
}