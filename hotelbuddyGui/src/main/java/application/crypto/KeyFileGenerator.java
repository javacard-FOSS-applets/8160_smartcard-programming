package application.crypto;

import application.log.LogHelper;
import common.ErrorResult;
import common.Result;
import common.SuccessResult;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by Patrick on 24.06.2015.
 */
public class KeyFileGenerator
{
    public static Result<Boolean> generateKeysToFile(Path filePath)
    {
        try
        {
            RSAPrivateKey privateKey;
            RSAPublicKey publicKey;

            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            final KeyPair key = keyGen.generateKeyPair();

            privateKey = (RSAPrivateKey) key.getPrivate();
            publicKey = (RSAPublicKey) key.getPublic();

            PrintWriter writer = new PrintWriter(filePath.toString());
            writer.println(privateKey.getModulus());
            writer.println(privateKey.getPrivateExponent());
            writer.println(publicKey.getModulus());
            writer.println(publicKey.getPublicExponent());
            writer.close();
        }
        catch (Exception ex)
        {
            LogHelper.log(ex);
            return new ErrorResult<>("Key file %s not generated", filePath.toString());
        }

        return new SuccessResult<>(true);
    }
}
