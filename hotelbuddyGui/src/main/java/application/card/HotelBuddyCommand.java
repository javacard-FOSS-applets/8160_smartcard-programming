package application.card;


import opencard.core.terminal.CommandAPDU;

/**
 * Created by Patrick on 16.08.2015.
 */
public class HotelBuddyCommand extends CommandAPDU
{
    protected int lc;

    public HotelBuddyCommand(byte classByte, byte instruction, byte p1, byte p2, byte[] content)
    {
        super(ToCommandBytes(classByte, instruction, p1, p2, content));
    }

    public byte getCLA()
    {
        return this.apdu_buffer[0];
    }

    public byte getINS()
    {
        return this.apdu_buffer[1];
    }

    public byte getP1()
    {
        return this.apdu_buffer[2];
    }

    public byte getP2()
    {
        return this.apdu_buffer[3];
    }

    public int getLC()
    {
        return this.apdu_buffer[4];
    }

    public String toString()
    {
        StringBuffer ret = new StringBuffer("APDU_Buffer = ");
        ret.append(this.makeHex(this.getBytes()));
        ret.append(" (hex) | lc = ");
        ret.append(this.lc);
        return ret.toString();
    }

    private String makeHex(byte[] buffer)
    {
        int length = buffer.length;
        String blank = "";
        StringBuffer ret = new StringBuffer(2 * length);

        for (int i = 0; i < 2 * length; ++i)
        {
            byte current = i % 2 == 1 ? (byte) (buffer[i / 2] & 15) : (byte) (buffer[i / 2] >> 4 & 15);
            ret.append((char) (current < 10 ? current + 48 : current + 55) + (i % 2 == 1 ? blank : ""));
        }

        return ret.toString();
    }

    private static byte[] ToCommandBytes(byte classByte, byte instruction, byte p1, byte p2, byte[] content)
    {
        final int contentLength = content.length;
        final byte[] command = new byte[5 + contentLength];

        command[0] = classByte;
        command[1] = instruction;
        command[2] = p1;
        command[3] = p2;

        if (contentLength > 0)
        {
            command[4] = (byte) contentLength;
            System.arraycopy(content, 0, command, 5, contentLength);
        }

        return command;
    }

    private static byte[] ToCommandBytes(byte classByte, byte instruction, byte[] content)
    {
        return ToCommandBytes(classByte, instruction, (byte) 0x00, (byte) 0x00, content);
    }
}
