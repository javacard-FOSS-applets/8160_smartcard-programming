package presentation.controllers;

import application.card.JavaCard;
import application.crypto.KeyFileGenerator;
import application.crypto.RSACryptographyHelper;
import application.hotelbuddy.CryptographyApplet;
import application.log.LogHelper;
import application.log.LogLevel;
import common.AlertHelper;
import common.Result;
import common.SuccessResult;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import presentation.models.ConnectionModel;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Patrick on 08.07.2015.
 */
public class ConnectionController
{
    public Button connectButton, generateKeyButton, initializeCardButton;
    public Label statusLabel, terminalKeyStatus, cardKeyStatus;

    public ConnectionModel model;

    public ConnectionController()
    {
        this.model = new ConnectionModel();
    }

    @FXML
    public void initialize()
    {
        initializeBindings();

        Result<Boolean> checkRsaKeyFilesResult = checkRsaKeyFiles();
        if (!checkRsaKeyFilesResult.isSuccess() || !checkRsaKeyFilesResult.get())
        {
            LogHelper.log(LogLevel.INFO, "Key files missing");
            return;
        }

        Result<Boolean> initializeTerminalCryptography = initializeTerminalCryptography();
        if (!initializeTerminalCryptography.isSuccess())
        {
            return;
        }

        connectToSmarCard(false);

        JavaCard.current().setOnCardInserted(() -> onCardInserted());
        JavaCard.current().setOnCardRemoved(() -> onCardRemoved());
    }

    /**
     * Connects asynchronously to the smartcard.
     *
     * @param showMessage determines if error messages are shown in an alert window
     */
    private void connectToSmartCardAsync(boolean showMessage)
    {
        new Thread(() -> connectToSmarCard(showMessage)).start();
    }

    /**
     * Initializes the connection to the card
     * Exchanges public keys with the card
     *
     * @param showMessage determines if error messages are shown in an alert window
     */
    private void connectToSmarCard(boolean showMessage)
    {
        setConnectionStatus(false, "Connecting...", Color.ORANGE);

        Result<Boolean> connectResult = JavaCard.current().connect();
        if (!connectResult.isSuccess())
        {
            setConnectionStatus(false, "Disconnected", Color.ORANGERED);

            if (showMessage)
            {
                AlertHelper.showErrorAlert(connectResult.getErrorMessage());
            }
            return;
        }

        Result<Boolean> importCardPublicKeyResult = CryptographyApplet.getPublicKeyFromCard();
        if (!importCardPublicKeyResult.isSuccess())
        {
            setConnectionStatus(false, "Cryptography failure, please initalize your card", Color.ORANGE);

            if (showMessage)
            {
                AlertHelper.showErrorAlert(importCardPublicKeyResult.getErrorMessage());
            }
            return;
        }

        setConnectionStatus(true, "Connected", Color.GREEN);
    }

    /**
     * Generates the rsa key files
     * if the terminal key file is generated, it get's loaded
     *
     * @return result of the operation
     */
    private Result<Boolean> generateRsaKeys()
    {
        if (!this.model.getIsTerminalKeyFileAvailable())
        {
            Result<Boolean> generateResult = KeyFileGenerator.generateKeysToFile(Paths.get("terminalKey.txt"));
            if (!generateResult.isSuccess())
            {
                return generateResult;
            }

            initializeTerminalCryptography();
        }

        if (!this.model.getIsCardKeyFileAvailable())
        {
            Result<Boolean> generateResult = KeyFileGenerator.generateKeysToFile(Paths.get("cardKey.txt"));
            if (!generateResult.isSuccess())
            {
                return generateResult;

            }
        }

        return new SuccessResult<>(checkRsaKeyFiles().get());
    }

    /**
     * Initializes the Cryptography class for the terminal
     *
     * @return result
     */
    private Result<Boolean> initializeTerminalCryptography()
    {
        Result<Boolean> setupTerminalKey = RSACryptographyHelper.current().importTerminalKeyFromFile();
        if (!setupTerminalKey.isSuccess())
        {
            AlertHelper.showErrorAlert(setupTerminalKey.getErrorMessage());
        }

        return setupTerminalKey;
    }

    /**
     * Checks existence of key files and writes it to the model
     *
     * @return true if all key files exist
     */
    private Result<Boolean> checkRsaKeyFiles()
    {
        this.model.setIsTerminalKeyFileAvailable(Files.exists(Paths.get("terminalKey.txt")));
        this.model.setIsCardKeyFileAvailable(Files.exists(Paths.get("cardKey.txt")));

        return new SuccessResult<>(this.model.getIsCardKeyFileAvailable() && this.model.getIsTerminalKeyFileAvailable());
    }

    /**
     * Setups the private and public key for the card
     * Exchanges the public key with the card
     */
    private void setupCardKeys()
    {
        Result<Boolean> result = CryptographyApplet.loadAndSetCardKeys();
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        result = CryptographyApplet.setTerminalPublicKeyToCard();
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        result = CryptographyApplet.getPublicKeyFromCard();
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        setConnectionStatus(true, "Connected", Color.GREEN);
    }

    private void setConnectionStatus(boolean isConnectionEstablished, String statusText, Color color)
    {
        Platform.runLater(() -> {
            this.model.setIsConnectionEstablished(isConnectionEstablished);
            this.model.setConnectionStatus(statusText);
            this.model.setConnectionStatusColor(color);
        });
    }

    private void initializeBindings()
    {
        connectButton.addEventHandler(ActionEvent.ACTION, e -> connectToSmartCardAsync(true));
        connectButton.disableProperty().bind(this.model.isConnectionEstablishedProperty());

        generateKeyButton.addEventHandler(ActionEvent.ACTION, e -> generateRsaKeys());
        generateKeyButton.disableProperty().bind(this.model.isTerminalKeyFileAvailableProperty().and(this.model.isTerminalKeyFileAvailableProperty()));

        initializeCardButton.addEventHandler(ActionEvent.ACTION, e -> setupCardKeys());
        initializeCardButton.disableProperty().bind(this.model.isConnectionEstablishedProperty());

        statusLabel.textProperty().bind(this.model.connectionStatusProperty());
        statusLabel.textFillProperty().bind(this.model.connectionStatusColorProperty());
        terminalKeyStatus.textProperty().bind(this.model.terminalKeyStatusProperty());
        terminalKeyStatus.textFillProperty().bind(this.model.terminalKeyStatusColorProperty());
        cardKeyStatus.textProperty().bind(this.model.cardKeyStatusProperty());
        cardKeyStatus.textFillProperty().bind(this.model.cardKeyStatusColorProperty());
    }

    private void onCardInserted()
    {

        Result<Boolean> result = CryptographyApplet.getPublicKeyFromCard();
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }
        setConnectionStatus(true, "Connected", Color.GREEN);
    }

    private void onCardRemoved()
    {
        setConnectionStatus(false, "Disconnected", Color.ORANGERED);
    }
}
