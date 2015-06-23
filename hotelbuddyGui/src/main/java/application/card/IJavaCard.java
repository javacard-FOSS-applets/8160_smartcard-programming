package application.card;

import common.Result;
import opencard.opt.terminal.ISOCommandAPDU;

/**
 * Created by Patrick on 23.06.2015.
 */
public interface IJavaCard
{
    Result<Boolean> connect();

    Result<byte[]> sendCommand(ISOCommandAPDU command);
}
