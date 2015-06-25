package presentation;

import application.card.JavaCard;
import application.crypto.KeyFileGenerator;
import application.crypto.TerminalCryptographyHelper;
import application.hotelbuddy.CryptographyApplet;
import application.hotelbuddy.IdentificationApplet;
import application.log.LogHelper;
import application.log.LogLevel;
import common.AlertHelper;
import common.Result;
import common.SuccessResult;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import presentation.models.ConfigurationModel;
import presentation.models.ConnectionModel;
import presentation.models.IdentificationModel;
import presentation.models.LogModel;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Patrick on 19.06.2015.
 */
public class MainController
{
    public Button con_connectButton, con_generateKeyButton;
    public Label con_statusLabel, con_terminalKeyStatus, con_cardKeyStatus;

    public Button conf_setIdentificationButton, conf_setupCardKeysButton;
    public DatePicker conf_birthDateDatePicker;
    public TextField conf_carIdTextField, conf_safePinTextField, conf_nameTextField;

    public Label id_nameLabel, id_birthDateLabel;
    public Button id_getButton;

    public TextArea log_logTextArea;

    private ConnectionModel connectionModel;
    private ConfigurationModel configurationModel;
    private IdentificationModel identificationModel;
    private LogModel logModel;

    /**
     * Initializes the models used by the main view
     */
    public MainController()
    {
        this.configurationModel = new ConfigurationModel();
        this.connectionModel = new ConnectionModel();
        this.identificationModel = new IdentificationModel();
        this.logModel = new LogModel();
    }

    /**
     * Initializes the Bindings.
     * Adds a callback to the LogHelper to display messages in the Log-Tab.
     * Initializes the Cryptography for the local terminal.
     * First attempt to connect to the smartcard (asynchronous). This attempt does not display any alerts.
     */
    @FXML
    public void initialize()
    {
        initializeBindings();

        LogHelper.setOnNewLogEntry(this::onNewLog);

        Result<Boolean> checkRsaKeyFilesResult = checkRsaKeyFiles();
        if (!checkRsaKeyFilesResult.isSuccess() || !checkRsaKeyFilesResult.getData())
        {
            LogHelper.log(LogLevel.INFO, "Key files missing");
            return;
        }

        Result<Boolean> initializeTerminalCryptography = initializeTerminalCryptography();
        if (!initializeTerminalCryptography.isSuccess())
        {
            return;
        }

        connectToSmartCardAsync(false);
    }

    /**
     * Checks existence of key files and writes it to the model
     *
     * @return true if all key files exist
     */
    private Result<Boolean> checkRsaKeyFiles()
    {
        this.connectionModel.setIsTerminalKeyFileAvailable(Files.exists(Paths.get("terminalKey.txt")));
        this.connectionModel.setIsCardKeyFileAvailable(Files.exists(Paths.get("cardKey.txt")));

        return new SuccessResult<>(this.connectionModel.getIsCardKeyFileAvailable() && this.connectionModel.getIsTerminalKeyFileAvailable());
    }

    /**
     * Initializes the Cryptography class for the terminal
     *
     * @return result
     */
    private Result<Boolean> initializeTerminalCryptography()
    {
        Result<Boolean> setupTerminalKey = TerminalCryptographyHelper.initializeTerminalKeys();
        if (!setupTerminalKey.isSuccess())
        {
            AlertHelper.showErrorAlert(setupTerminalKey.getErrorMessage());
        }

        return setupTerminalKey;
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

        Result<Boolean> exportTerminalPublicKeyResult = CryptographyApplet.exportTerminalPublicKeyToCard();
        if (!exportTerminalPublicKeyResult.isSuccess())
        {
            setConnectionStatus(false, "Cryptography failure", Color.ORANGE);

            if (showMessage)
            {
                AlertHelper.showErrorAlert(exportTerminalPublicKeyResult.getErrorMessage());
            }
            return;
        }

        Result<Boolean> importCardPublicKeyResult = CryptographyApplet.importPublicKeyFromCard();
        if (!importCardPublicKeyResult.isSuccess())
        {
            setConnectionStatus(false, "Cryptography failure", Color.ORANGE);

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
     * @return
     */
    private Result<Boolean> generateRsaKeys()
    {
        if (!this.connectionModel.getIsTerminalKeyFileAvailable())
        {
            Result<Boolean> generateResult = KeyFileGenerator.generateKeysToFile(Paths.get("terminalKey.txt"));
            if (!generateResult.isSuccess())
            {
                return generateResult;
            }

            initializeTerminalCryptography();
        }

        if (!this.connectionModel.getIsCardKeyFileAvailable())
        {
            Result<Boolean> generateResult = KeyFileGenerator.generateKeysToFile(Paths.get("cardKey.txt"));
            if (!generateResult.isSuccess())
            {
                return generateResult;

            }
        }

        return new SuccessResult<>(checkRsaKeyFiles().getData());
    }
    /**
     * Setups the private and public key for the card
     * Exchanges the public key with the card
     */
    private void setupCardKeys()
    {
        CryptographyApplet.initializeCardKeys();
        CryptographyApplet.exportTerminalPublicKeyToCard();
        CryptographyApplet.importPublicKeyFromCard();
    }

    /**
     * Gets the Identification data from the card
     */
    private void getIdentificationData()
    {
        Result<String> nameResult = IdentificationApplet.getName();
        if (nameResult.isSuccess())
        {
            this.identificationModel.setName(nameResult.getData());
        }

        Result<String> birthDateResult = IdentificationApplet.getBirthDay();
        if (birthDateResult.isSuccess())
        {
            this.identificationModel.setBirthDate(birthDateResult.getData());
        }

    }

    /**
     * Adds a new log entry to the property in the model
     *
     * @param line log entry
     */
    private void onNewLog(String line)
    {
        String m = this.logModel.getLogMessage() + line + "\n";
        this.logModel.setLogMessage(m);
    }

    private void setConnectionStatus(boolean isConnectionEstablished, String statusText, Color color)
    {
        Platform.runLater(() -> {
            this.connectionModel.setIsConnectionEstablished(isConnectionEstablished);
            this.connectionModel.setConnectionStatus(statusText);
            this.connectionModel.setConnectionStatusColor(color);
        });
    }

    private void setIdentificationData()
    {
        Result<Boolean> result = IdentificationApplet.reset();
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        result = IdentificationApplet.setName(this.configurationModel.getName());
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        result = IdentificationApplet.setBirthDay(this.configurationModel.getBirthDate());
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }
    }

    private void initializeBindings()
    {
        con_connectButton.addEventHandler(ActionEvent.ACTION, e -> connectToSmartCardAsync(true));
        con_generateKeyButton.addEventHandler(ActionEvent.ACTION, e -> generateRsaKeys());
        conf_setIdentificationButton.addEventHandler(ActionEvent.ACTION, e -> setIdentificationData());
        conf_setupCardKeysButton.addEventHandler(ActionEvent.ACTION, e -> setupCardKeys());
        id_getButton.addEventHandler(ActionEvent.ACTION, e -> getIdentificationData());

        con_statusLabel.textProperty().bind(this.connectionModel.connectionStatusProperty());
        con_statusLabel.textFillProperty().bind(this.connectionModel.connectionStatusColorProperty());
        con_terminalKeyStatus.textProperty().bindBidirectional(this.connectionModel.terminalKeyStatusProperty());
        con_terminalKeyStatus.textFillProperty().bindBidirectional(this.connectionModel.terminalKeyStatusColorProperty());
        con_cardKeyStatus.textProperty().bindBidirectional(this.connectionModel.cardKeyStatusProperty());
        con_cardKeyStatus.textFillProperty().bindBidirectional(this.connectionModel.cardKeyStatusColorProperty());

        conf_nameTextField.textProperty().bindBidirectional(this.configurationModel.nameProperty());
        conf_carIdTextField.textProperty().bindBidirectional(this.configurationModel.carIdProperty());
        conf_safePinTextField.textProperty().bindBidirectional(this.configurationModel.safePinProperty());
        conf_birthDateDatePicker.valueProperty().bindBidirectional(this.configurationModel.birthDateProperty());

        id_nameLabel.textProperty().bind(this.identificationModel.nameProperty());
        id_birthDateLabel.textProperty().bind(this.identificationModel.birthDateProperty());

        log_logTextArea.textProperty().bind(this.logModel.logMessageProperty());
    }
}
