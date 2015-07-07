package presentation;

import application.card.JavaCard;
import application.crypto.KeyFileGenerator;
import application.crypto.RSACryptographyHelper;
import application.hotelbuddy.AccessApplet;
import application.hotelbuddy.AccessRestrictedRoom;
import application.hotelbuddy.CryptographyApplet;
import application.hotelbuddy.IdentificationApplet;
import application.log.LogHelper;
import application.log.LogLevel;
import common.AlertHelper;
import common.Result;
import common.SuccessResult;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import presentation.controls.NumericTextField;
import presentation.models.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by Patrick on 19.06.2015.
 */
public class MainController
{
    public Button con_connectButton, con_generateKeyButton;
    public Label con_statusLabel, con_terminalKeyStatus, con_cardKeyStatus;

    public Button conf_setIdentificationButton, con_initializeCardButton, conf_restAccessControl, conf_resetIdentification, conf_setAccessButton;
    public DatePicker conf_birthDateDatePicker;
    public TextField conf_carIdTextField, conf_nameTextField;
    public NumericTextField conf_safePinTextField;
    public CheckBox conf_classicBarCheckbox, conf_casinoCheckbox, conf_poolCheckbox, conf_skyBarCheckbox, conf_wellnessCheckbox;

    public Label id_nameLabel, id_birthDateLabel, id_carIdLabel;
    public Button id_getButton;

    public Button ac_checkButton;
    public Label ac_statusLabel;
    public ComboBox<AccessRestrictedRoom> ac_roomComboBox;

    public NumericTextField sa_safePinTextField;
    public Label sa_resultLabel;
    public Button sa_checkButton;

    public TextArea log_logTextArea;

    private ConnectionModel connectionModel;
    private ConfigurationModel configurationModel;
    private IdentificationModel identificationModel;
    private AccessModel accessModel;
    private SafePinModel safePinModel;
    private LogModel logModel;

    /**
     * Initializes the models used by the main view
     */
    public MainController()
    {
        this.configurationModel = new ConfigurationModel();
        this.connectionModel = new ConnectionModel();
        this.identificationModel = new IdentificationModel();
        this.accessModel = new AccessModel();
        this.safePinModel = new SafePinModel();
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
        this.accessModel.setRooms(FXCollections.observableArrayList(AccessRestrictedRoom.values()));
        this.ac_roomComboBox.getSelectionModel().selectFirst();

        LogHelper.setOnNewLogEntry(this::onNewLog);

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
        Result<Boolean> setupTerminalKey = RSACryptographyHelper.current().importTerminalKeyFromFile();
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

        Result<Boolean> importCardPublicKeyResult = CryptographyApplet.importPublicKeyFromCard();
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

        return new SuccessResult<>(checkRsaKeyFiles().get());
    }

    /**
     * Setups the private and public key for the card
     * Exchanges the public key with the card
     */
    private void setupCardKeys()
    {
        Result<Boolean> result = CryptographyApplet.loadAndExportCardKeysFromFile();
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        result = CryptographyApplet.exportTerminalPublicKeyToCard();
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        result = CryptographyApplet.importPublicKeyFromCard();
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        setConnectionStatus(true, "Connected", Color.GREEN);
    }

    /**
     * Gets the Identification data from the card
     */
    private void getIdentificationData()
    {
        Result<String> nameResult = IdentificationApplet.getName();
        if (nameResult.isSuccess())
        {
            this.identificationModel.setName(nameResult.get());
        }

        Result<String> birthDateResult = IdentificationApplet.getBirthDay();
        if (birthDateResult.isSuccess())
        {
            this.identificationModel.setBirthDate(birthDateResult.get());
        }

        Result<String> carIdResult = IdentificationApplet.getCarId();
        if (carIdResult.isSuccess())
        {
            this.identificationModel.setCarId(carIdResult.get());
        }
    }

    /**
     * Checks the entered Safe PIN
     */
    private void checkSafePin()
    {
        Result<Boolean> nameResult = IdentificationApplet.checkSafePin(this.safePinModel.getSafePin());
        if (!nameResult.isSuccess())
        {
            this.safePinModel.setCheckStatus("Wrong Safe PIN!");
            this.safePinModel.setCheckStatusColor(Color.RED);
            return;
        }

        this.safePinModel.setCheckStatus("Correct Safe PIN");
        this.safePinModel.setCheckStatusColor(Color.GREEN);
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
        Result<Boolean> result = IdentificationApplet.setName(this.configurationModel.getName());
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

        result = IdentificationApplet.setCarId(this.configurationModel.getCarId());
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        result = IdentificationApplet.setSafePin(this.configurationModel.getSafePin());
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        AlertHelper.showSuccessAlert("Data successfully set.");
    }

    private void resetIdentification()
    {
        Result<Boolean> result = IdentificationApplet.reset();
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
        }
    }

    private void resetAccess()
    {
        Result<Boolean> result = AccessApplet.reset();
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
        }
    }

    private void checkRoom()
    {
        Result<Boolean> result = AccessApplet.checkRoom(this.accessModel.getSelectedRoom());
        if (!result.isSuccess())
        {
            this.accessModel.setCheckStatus("Access denied!");
            this.accessModel.setCheckStatusColor(Color.RED);
            return;
        }

        this.accessModel.setCheckStatus("Access allowed!");
        this.accessModel.setCheckStatusColor(Color.GREEN);
    }

    private void setAccessData()
    {
        HashMap<AccessRestrictedRoom, Boolean> accessRestriction = new HashMap<>();
        accessRestriction.put(AccessRestrictedRoom.ClassicBar, this.configurationModel.getClassicBarAccess());
        accessRestriction.put(AccessRestrictedRoom.Casino, this.configurationModel.getCasinoAccess());
        accessRestriction.put(AccessRestrictedRoom.Pool, this.configurationModel.getPoolAccess());
        accessRestriction.put(AccessRestrictedRoom.SkyBar, this.configurationModel.getSkyBarAccess());
        accessRestriction.put(AccessRestrictedRoom.Wellness, this.configurationModel.getWellnessAccess());

        Result<Boolean> result = AccessApplet.setAccess(accessRestriction);
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
        }
    }

    private void initializeBindings()
    {
        con_connectButton.addEventHandler(ActionEvent.ACTION, e -> connectToSmartCardAsync(true));
        con_connectButton.disableProperty().bind(this.connectionModel.isConnectionEstablishedProperty());

        con_generateKeyButton.addEventHandler(ActionEvent.ACTION, e -> generateRsaKeys());
        con_generateKeyButton.disableProperty().bind(this.connectionModel.isTerminalKeyFileAvailableProperty().and(this.connectionModel.isTerminalKeyFileAvailableProperty()));

        con_initializeCardButton.addEventHandler(ActionEvent.ACTION, e -> setupCardKeys());
        con_initializeCardButton.disableProperty().bind(this.connectionModel.isConnectionEstablishedProperty());

        conf_setIdentificationButton.addEventHandler(ActionEvent.ACTION, e -> setIdentificationData());
        conf_setAccessButton.addEventHandler(ActionEvent.ACTION, e -> setAccessData());
        conf_resetIdentification.addEventHandler(ActionEvent.ACTION, e -> resetIdentification());
        conf_restAccessControl.addEventHandler(ActionEvent.ACTION, e -> resetAccess());

        id_getButton.addEventHandler(ActionEvent.ACTION, e -> getIdentificationData());

        ac_checkButton.addEventHandler(ActionEvent.ACTION, e -> checkRoom());

        sa_checkButton.addEventHandler(ActionEvent.ACTION, e -> checkSafePin());

        con_statusLabel.textProperty().bind(this.connectionModel.connectionStatusProperty());
        con_statusLabel.textFillProperty().bind(this.connectionModel.connectionStatusColorProperty());
        con_terminalKeyStatus.textProperty().bind(this.connectionModel.terminalKeyStatusProperty());
        con_terminalKeyStatus.textFillProperty().bind(this.connectionModel.terminalKeyStatusColorProperty());
        con_cardKeyStatus.textProperty().bind(this.connectionModel.cardKeyStatusProperty());
        con_cardKeyStatus.textFillProperty().bind(this.connectionModel.cardKeyStatusColorProperty());

        conf_nameTextField.textProperty().bindBidirectional(this.configurationModel.nameProperty());
        conf_carIdTextField.textProperty().bindBidirectional(this.configurationModel.carIdProperty());
        conf_safePinTextField.setMaxlength(IdentificationApplet.SafePinLength);
        conf_safePinTextField.textProperty().bindBidirectional(this.configurationModel.safePinProperty());
        conf_birthDateDatePicker.valueProperty().bindBidirectional(this.configurationModel.birthDateProperty());

        conf_classicBarCheckbox.selectedProperty().bindBidirectional(this.configurationModel.classicBarAccessProperty());
        conf_casinoCheckbox.selectedProperty().bindBidirectional(this.configurationModel.casinoAccessProperty());
        conf_poolCheckbox.selectedProperty().bindBidirectional(this.configurationModel.poolAccessProperty());
        conf_skyBarCheckbox.selectedProperty().bindBidirectional(this.configurationModel.skyBarAccessProperty());
        conf_wellnessCheckbox.selectedProperty().bindBidirectional(this.configurationModel.wellnessAccessProperty());

        id_nameLabel.textProperty().bind(this.identificationModel.nameProperty());
        id_birthDateLabel.textProperty().bind(this.identificationModel.birthDateProperty());
        id_carIdLabel.textProperty().bind(this.identificationModel.carIdProperty());

        ac_statusLabel.textProperty().bind(this.accessModel.checkStatusProperty());
        ac_statusLabel.textFillProperty().bind(this.accessModel.checkStatusColorProperty());
        ac_roomComboBox.valueProperty().bindBidirectional(this.accessModel.selectedRoomProperty());
        ac_roomComboBox.itemsProperty().bindBidirectional(this.accessModel.roomsProperty());

        sa_safePinTextField.setMaxlength(IdentificationApplet.SafePinLength);
        sa_safePinTextField.textProperty().bindBidirectional(this.safePinModel.safePinProperty());
        sa_resultLabel.textProperty().bind(this.safePinModel.checkStatusProperty());
        sa_resultLabel.textFillProperty().bind(this.safePinModel.checkStatusColorProperty());

        log_logTextArea.textProperty().bind(this.logModel.logMessageProperty());
    }
}
