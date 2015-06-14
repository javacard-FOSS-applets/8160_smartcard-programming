import com.licel.jcardsim.base.Simulator;

import java.nio.ByteBuffer;

public class TestHelper
{
    public static byte[] ExecuteCommand(Simulator sim, byte classByte, byte instruction, byte[] content, byte answerLength)
    {
        String answerString;
        System.out.println("Sending command...");
        byte[] command = ToCommand(classByte, instruction, content, answerLength);
        System.out.println(ToHexString(command));

        System.out.println("\nAnswer...");
        byte[] answer = sim.transmitCommand(command);
        System.out.println(ToHexString(answer));
        answerString = new String(answer, 0, answer.length - 2);
        System.out.println(answerString);

        byte[] strippedAnswer = new byte[answer.length - 2];
        System.arraycopy(answer, 0, strippedAnswer, 0, strippedAnswer.length);
        return strippedAnswer;
    }

    public static byte[] ToByteArray(String hexString)
    {
        final String[] hexChunks = hexString.replace(" ", "").split("(?<=\\G..)");

        byte[] result = new byte[hexChunks.length];
        for (int i = 0; i < hexChunks.length; i++)
        {
            result[i] = (byte) Integer.parseInt(hexChunks[i], 16);
        }

        return result;
    }

    public static byte[] ToByteArray(int i)
    {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    public static String ToHexString(byte[] b)
    {
        final StringBuilder builder = new StringBuilder();
        for (byte by : b)
        {
            builder.append(String.format("%02x ", by));
        }
        return builder.toString();
    }

    public static byte[] ToCommand(byte classByte, byte instruction, byte[] content, byte answerLength)
    {
        final int contentLength = content.length;
        final byte[] command = new byte[6 + contentLength];

        command[0] = classByte;
        command[1] = instruction;
        command[2] = 0x00;
        command[3] = 0x00;

        if (contentLength > 0)
        {
            command[4] = (byte) contentLength;
            System.arraycopy(content, 0, command, 5, contentLength);
            command[5 + contentLength] = answerLength;
        }

        return command;
    }
}
