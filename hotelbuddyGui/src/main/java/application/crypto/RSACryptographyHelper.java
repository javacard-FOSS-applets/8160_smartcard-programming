package application.crypto;


import application.log.LogHelper;
import application.log.LogLevel;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;
import common.TerminalPaths;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * Created by Patrick on 22.06.2015.
 */
public class RSACryptographyHelper implements IRSACryptographyHelper
{
    private static final Path TerminalKeyPath = TerminalPaths.TerminalKeyPath;

    private static IRSACryptographyHelper instance;
    private Cipher rsaCipher;

    private RSAPrivateKey terminalPrivateKey;
    private RSAPublicKey terminalPublicKey;

    private PublicKey cardPublicKey;

    private RSACryptographyHelper()
    {
        try
        {
            rsaCipher = Cipher.getInstance("RSA");
        }
        catch (Exception ex)
        {
            LogHelper.log(ex);
            return;
        }

        LogHelper.log(LogLevel.INFO, "RSACryptographyHelper initialized");
    }

    public static IRSACryptographyHelper current()
    {
        return instance == null ? (instance = new RSACryptographyHelper()) : instance;
    }

    public void setCardPublicKey(BigInteger modulus, BigInteger exponent)
    {
        try
        {
            KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            cardPublicKey = rsaKeyFactory.generatePublic(spec);
        }
        catch (Exception ex)
        {
            LogHelper.log(ex);
        }
    }

    /**
     * loads the card keys from TerminalKeyPath
     * and sets them into the RSACryptographyHelper
     *
     * @return result of the operation
     */
    @Override
    public Result<Boolean> importTerminalKeyFromFile()
    {
        Result<ImportedKeys> readResult = CryptographyHelper.readKeysFromFile(TerminalKeyPath);
        if (!readResult.isSuccess())
        {
            return new ErrorResult<>(readResult.getErrorMessage());
        }

        try
        {
            KeyFactory rsaKeyFactory = KeyFactory.getInstance("RSA");

            RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(
                    readResult.get().getPrivateMod(),
                    readResult.get().getPrivateExp());
            terminalPrivateKey = (RSAPrivateKey) rsaKeyFactory.generatePrivate(keySpec);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(
                    readResult.get().getPublicMod(),
                    readResult.get().getPublicExp());
            terminalPublicKey = (RSAPublicKey) rsaKeyFactory.generatePublic(spec);
        }
        catch (Exception ex)
        {
            LogHelper.log(ex);
            return new ErrorResult<>("Couldn't set terminal keys.");
        }

        return new SuccessResult<>(true);
    }

    @Override
    public Result<byte[]> encrypt(byte[] message)
    {
        try
        {
            rsaCipher.init(Cipher.ENCRYPT_MODE, this.cardPublicKey);
            return new SuccessResult<>(rsaCipher.doFinal(message));
        }
        catch (Exception ex)
        {
            LogHelper.log(ex);
            return new ErrorResult<>("Encryption failed");
        }
    }

    @Override
    public Result<byte[]> decrypt(byte[] message)
    {
        try
        {
            rsaCipher.init(Cipher.DECRYPT_MODE, this.terminalPrivateKey);
            return new SuccessResult<>(rsaCipher.doFinal(message));
        }
        catch (Exception ex)
        {
            LogHelper.log(ex);
            return new ErrorResult<>("Decryption failed");
        }
    }

    @Override
    public byte[] getPublicMod()
    {
        return terminalPublicKey.getModulus().toByteArray();
    }

    @Override
    public byte[] getPublicExp()
    {
        return terminalPublicKey.getPublicExponent().toByteArray();
    }
}
