package common;

/**
 * Created by Patrick on 23.06.2015.
 */
public class ErrorResult<T> extends Result<T>
{
    public ErrorResult(String errorMessage, Object... args)
    {
        super();
        this.setSuccess(false);
        this.setErrorMessage(String.format(errorMessage, args));
    }
}
