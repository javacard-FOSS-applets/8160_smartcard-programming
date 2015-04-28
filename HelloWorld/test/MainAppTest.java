import com.licel.jcardsim.base.Simulator;
import javacard.framework.AID;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Patrick on 28.04.2015.
 */
public class MainAppTest
{
    @Test
    public void TestHello()
    {
        Simulator sim = new Simulator();

        // byte[] aidBytes = "|helloApp".getBytes();
        byte[] aidBytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        AID appletAID = new AID(aidBytes, (short) 0, (byte) aidBytes.length);

        sim.installApplet(appletAID, MainApp.class);

        System.out.println("Getting ATR...");
        byte[] atr = sim.getATR();
        System.out.println(new String(atr));
        System.out.println(bytesToHex(atr));

        System.out.println("\nSelecting Applect...");
        boolean isAppletSelected = sim.selectApplet(appletAID);
        System.out.println(isAppletSelected);

        System.out.println("\nSending command...");
        byte[] command = new byte[]{0x00, 0x00, 0x00, 0x00, 0x06};
        System.out.println(bytesToHex(command));

        System.out.println("\nAnswer...");
        byte[] answer = sim.transmitCommand(command);
        System.out.println(bytesToHex(answer));
        String answerString = new String(answer, 0, answer.length - 2);
        System.out.println(answerString);

        Assert.assertEquals("Hello!", answerString);
    }

    public static String bytesToHex(byte[] in)
    {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in)
        {
            builder.append(String.format("%02x ", b));
        }
        return builder.toString();
    }
}
