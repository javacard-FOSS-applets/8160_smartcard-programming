package common;

/**
 * Created by Georg on 24.06.2015.
 */
public class Pair <T,R>
{
    private T t;

    private  R r;

    public Pair(T t, R r)
    {
        this.t = t;
        this.r = r;
    }

    public T getFirst()
    {
        return t;
    }

    public R getSecond()
    {
        return r;
    }
}
