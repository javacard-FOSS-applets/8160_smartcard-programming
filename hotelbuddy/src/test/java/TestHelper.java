public class TestHelper
{
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
        command[4] = (byte) contentLength;

        System.arraycopy(content, 0, command, 5, contentLength);

        command[5 + contentLength] = answerLength;

        return command;
    }
}
