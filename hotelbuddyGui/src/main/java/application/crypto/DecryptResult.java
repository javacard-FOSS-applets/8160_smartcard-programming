package application.crypto;

/**
 * Created by Patrick on 22.06.2015.
 */
public class DecryptResult
{
    private String message;
    private boolean success;

    public DecryptResult(boolean success, String message)
    {
        this.message = message;
        this.success = success;
    }

    public String getData()
    {
        return message;
    }

    public boolean isSuccess()
    {
        return success;
    }
}
