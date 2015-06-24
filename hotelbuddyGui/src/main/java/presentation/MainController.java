package presentation;

import application.card.JavaCard;
import application.hotelbuddy.CryptographyApplet;
import application.hotelbuddy.IdentificationApplet;
import application.log.LogHelper;
import application.log.LogLevel;
import common.AlertHelper;
import common.Result;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import presentation.models.ConfigurationModel;
import presentation.models.ConnectionModel;
import presentation.models.IdentificationModel;
import presentation.models.LogModel;

/**
 * Created by Patrick on 19.06.2015.
 */
public class MainController
{
    public Button con_connectButton;
    public Label con_statusLabel;

    public Button conf_setIdentificationButton, conf_setupCardKeysButton, conf_exchangePublicKeys;
    public DatePicker conf_birthDateDatePicker;
    public TextField conf_carIdTextField, conf_safePinTextField, conf_nameTextField;

    public Label id_nameLabel, id_birthDateLabel;
    public Button id_getButton;

    public TextArea log_logTextArea;

    private ConnectionModel connectionModel;
    private ConfigurationModel configurationModel;
    private IdentificationModel identificationModel;
    private LogModel logModel;

    public MainController()
    {
        this.configurationModel = new ConfigurationModel();
        this.connectionModel = new ConnectionModel();
        this.identificationModel = new IdentificationModel();
        this.logModel = new LogModel();
    }

    @FXML
    public void initialize()
    {
        conf_setIdentificationButton.addEventHandler(ActionEvent.ACTION, e -> setIdentificationData());
        conf_setupCardKeysButton.addEventHandler(ActionEvent.ACTION, e -> setupCardKeys());
        conf_exchangePublicKeys.addEventHandler(ActionEvent.ACTION, e -> exportTerminalKey());
        con_connectButton.addEventHandler(ActionEvent.ACTION, e -> connectToSmartCardAsync(true));
        id_getButton.addEventHandler(ActionEvent.ACTION, e -> getIdentificationData());

        conf_nameTextField.textProperty().bindBidirectional(this.configurationModel.nameProperty());
        conf_carIdTextField.textProperty().bindBidirectional(this.configurationModel.carIdProperty());
        conf_safePinTextField.textProperty().bindBidirectional(this.configurationModel.safePinProperty());
        conf_birthDateDatePicker.valueProperty().bindBidirectional(this.configurationModel.birthDateProperty());

        id_nameLabel.textProperty().bind(this.identificationModel.nameProperty());
        id_birthDateLabel.textProperty().bind(this.identificationModel.birthDateProperty());

        log_logTextArea.textProperty().bind(this.logModel.logMessageProperty());

        con_statusLabel.textProperty().bind(this.connectionModel.connectionStatusProperty());
        con_statusLabel.textFillProperty().bind(this.connectionModel.connectionStatusColorProperty());

        LogHelper.setOnNewLogEntry(this::onNewLog);

        initializeTerminalCryptography();
        connectToSmartCardAsync(false);
    }

    private void exportTerminalKey()
    {
        CryptographyApplet.exportTerminalPublicKeyToCard();
        CryptographyApplet.importCardPublicKey();
    }

    private void setupCardKeys()
    {
        CryptographyApplet.setupCardKey();
    }

    private void getIdentificationData()
    {
        Result<String> nameResult = IdentificationApplet.getName();

        if (!nameResult.isSuccess())
        {
            this.identificationModel.setName("");
            AlertHelper.showErrorAlert(nameResult.getErrorMessage());
            return;
        }

        this.identificationModel.setName(nameResult.getData());
    }

    private void connectToSmartCardAsync(boolean showMessage)
    {
        new Thread(() -> initCard(showMessage)).start();
    }

    private void onNewLog(String s)
    {
        String m = this.logModel.getLogMessage() + s + "\n";
        this.logModel.setLogMessage(m);
    }

    private void initCard(boolean showMessage)
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

        Result<Boolean> importCardPublicKeyResult = CryptographyApplet.importCardPublicKey();
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

    private void initializeTerminalCryptography()
    {
        Result<Boolean> setupTerminalKey = CryptographyApplet.loadTerminalKeys();
        if (!setupTerminalKey.isSuccess())
        {
            AlertHelper.showErrorAlert(setupTerminalKey.getErrorMessage());
        }
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
        IdentificationApplet.setName(this.configurationModel.getName());
    }
}
