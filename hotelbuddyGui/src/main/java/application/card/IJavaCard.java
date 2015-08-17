package application.card;

import common.Result;

/**
 * Created by Patrick on 23.06.2015.
 */
public interface IJavaCard
{
    Result<Boolean> connect();

    Result<byte[]> sendCommand(HotelBuddyCommand command);

    void shutdown();
}
