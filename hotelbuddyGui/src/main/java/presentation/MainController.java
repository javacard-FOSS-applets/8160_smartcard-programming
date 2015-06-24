package presentation;

import application.card.JavaCard;
import application.hotelbuddy.CryptographyApplet;
import application.hotelbuddy.IdentificationApplet;
import application.log.LogHelper;
import common.AlertHelper;
import common.Result;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

/**
 * Created by Patrick on 19.06.2015.
 */
public class MainController
{
    @FXML
    public Button setIdentificationButton, connectButton;
    @FXML
    public DatePicker birthDateDatePicker;
    @FXML
    public TextField carIdTextField, safePinTextField, nameTextField;
    @FXML
    public TextArea logTextArea;
    @FXML
    public Label statusLabel;

    private MainModel model;

    public MainController()
    {
        this.model = new MainModel();
    }

    @FXML
    public void initialize()
    {
        setIdentificationButton.addEventHandler(ActionEvent.ACTION, e -> setIdentificationData());
        connectButton.addEventHandler(ActionEvent.ACTION, e -> connectToSmartCardAsync(true));

        nameTextField.textProperty().bindBidirectional(this.model.nameProperty());
        carIdTextField.textProperty().bindBidirectional(this.model.carIdProperty());
        safePinTextField.textProperty().bindBidirectional(this.model.safePinProperty());
        birthDateDatePicker.valueProperty().bindBidirectional(this.model.birthDateProperty());
        logTextArea.textProperty().bind(this.model.logMessageProperty());
        statusLabel.textProperty().bind(this.model.connectionStatusProperty());
        statusLabel.textFillProperty().bind(this.model.connectionStatusColorProperty());

        LogHelper.setOnNewLogEntry(this::onNewLog);

        connectToSmartCardAsync(false);
    }

    private void connectToSmartCardAsync(boolean showMessage)
    {
        new Thread(() -> initCard(showMessage)).start();
    }

    private void onNewLog(String s)
    {
        String m = this.model.getLogMessage() + s + "\n";
        this.model.setLogMessage(m);
    }

    private void initCard(boolean showMessage)
    {
        setConnectionStatus(false, "Connecting...", Color.GREEN);

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

        Result<Boolean> setupCryptoResult = CryptographyApplet.setupRSACryptographyHelper();
        if (!setupCryptoResult.isSuccess())
        {
            setConnectionStatus(false, "Disconnected", Color.ORANGERED);

            if (showMessage)
            {
                AlertHelper.showErrorAlert(setupCryptoResult.getErrorMessage());
            }
            return;
        }

        setConnectionStatus(true, "Connected", Color.ORANGE);
    }

    private void setConnectionStatus(boolean isConnectionEstablished, String statusText, Color color)
    {
        Platform.runLater(() -> {
            this.model.setIsConnectionEstablished(isConnectionEstablished);
            this.model.setConnectionStatus(statusText);
            this.model.setConnectionStatusColor(color);
        });
    }

    private void setIdentificationData()
    {
        IdentificationApplet.setName(this.model.getName());
    }
}
