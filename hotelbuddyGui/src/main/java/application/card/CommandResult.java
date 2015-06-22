package application.card;

/**
 * Created by Patrick on 22.06.2015.
 */
public class CommandResult
{
    private boolean success;
    private byte[] response;

    public CommandResult(boolean success, byte[] response)
    {
        this.success = success;
        this.response = response;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public byte[] getData()
    {
        return response;
    }
}
