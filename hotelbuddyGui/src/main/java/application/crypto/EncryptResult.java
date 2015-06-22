package application.crypto;

/**
 * Created by Patrick on 22.06.2015.
 */
public class EncryptResult
{
    private byte[] message;
    private boolean success;

    public EncryptResult(boolean success, byte[] message)
    {
        this.message = message;
        this.success = success;
    }

    public byte[] getDate()
    {
        return message;
    }

    public boolean isSuccess()
    {
        return success;
    }
}
