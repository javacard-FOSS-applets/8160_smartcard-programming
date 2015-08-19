package application.hotelbuddy;

import common.ErrorResult;
import common.Result;
import common.SuccessResult;

import java.nio.ByteBuffer;

/**
 * Created by Patrick on 17.08.2015.
 */
public class BonusApplet
{
    private static final String APPLET_NAME = "Bonus";

    private static final byte CLA = (byte) 0x42;

    private static final byte INS_REGISTER_BONUS = (byte) 0xB0;
    private static final byte INS_GET_ALL_BONUS = (byte) 0xB1;
    private static final byte INS_RESET = (byte) 0xF0;

    /**
     * Sends the given name to the card
     *
     * @param points points
     * @return result of the operation
     */
    public static Result<Boolean> registerBonus(Short points)
    {
        if (points < 1)
        {
            return new SuccessResult<>(true);
        }

        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(points);
        byte[] bytes = buffer.array();

        Result<byte[]> result =  CommonApplet.sendValue(APPLET_NAME, CLA, INS_REGISTER_BONUS, bytes);
        return !result.isSuccess() ? new ErrorResult<>(result.getErrorMessage()) : new SuccessResult<>(true);
    }

    /**
     * receives the name from the card
     *
     * @return result of the operation
     */
    public static Result<Short> getAllBonus()
    {
        Result<byte[]> result =  CommonApplet.sendValue(APPLET_NAME, CLA, INS_GET_ALL_BONUS);
        if (!result.isSuccess())
        {
            return new ErrorResult<>(result.getErrorMessage());
        }

        ByteBuffer wrapped = ByteBuffer.wrap(result.get());
        return new SuccessResult<>(wrapped.getShort());
    }

    public static Result<Boolean> reset()
    {
        return CommonApplet.reset(APPLET_NAME, CLA, INS_RESET);
    }
}
