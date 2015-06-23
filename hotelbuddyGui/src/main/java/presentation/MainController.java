package presentation;

import application.card.CommandResult;
import application.card.SmartCardConnector;
import application.crypto.EncryptResult;
import application.crypto.RSACryptographyHelper;
import application.log.LogHelper;
import application.log.LogLevel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

/**
 * Created by Patrick on 19.06.2015.
 */
public class MainController
{
    @FXML
    public Button setIdentificationButton;
    @FXML
    public TextField nameTextField;
    @FXML
    public DatePicker birthDateDatePicker;
    @FXML
    public TextField carIdTextField;
    @FXML
    public TextField safePinTextField;

    private MainModel model;
    private SmartCardConnector card;
    private RSACryptographyHelper crypto;

    public MainController()
    {
        this.model = new MainModel();
    }

    @FXML
    public void initialize()
    {
        setIdentificationButton.addEventHandler(ActionEvent.ACTION, e -> setIdentificationData());

        nameTextField.textProperty().bindBidirectional(this.model.nameProperty());
        carIdTextField.textProperty().bindBidirectional(this.model.carIdProperty());
        safePinTextField.textProperty().bindBidirectional(this.model.safePinProperty());
        birthDateDatePicker.valueProperty().bindBidirectional(this.model.birthDateProperty());

        initCard();
    }

    private void initCard()
    {
        this.card = new SmartCardConnector();
        if (!card.connect())
        {
            return;
        }

        card.selectApplet("Cryptography");
        CommandResult modResult = card.sendCommand((byte) 0x43, (byte) 0xF0, new byte[0], (byte) 0x04);
        CommandResult expResult = card.sendCommand((byte) 0x43, (byte) 0xF2, new byte[0], (byte) 0x04);

        if (modResult.isSuccess() && expResult.isSuccess())
        {
            this.crypto = new RSACryptographyHelper();
            this.crypto.initialize();
            this.crypto.importPublicKey(modResult.getData(), expResult.getData());
        }
    }

    private void setIdentificationData()
    {
        card.selectApplet("Identification");

        EncryptResult encryptedName = crypto.encrypt(this.model.getName());

        if (encryptedName.isSuccess())
        {
            CommandResult expResult = card.sendCommand((byte) 0x49, (byte) 0xA0, encryptedName.getDate(), (byte) 0x04);

            if (expResult.isSuccess())
            {
                LogHelper.log(LogLevel.INFO, "Name success");
            }
        }
    }
}
