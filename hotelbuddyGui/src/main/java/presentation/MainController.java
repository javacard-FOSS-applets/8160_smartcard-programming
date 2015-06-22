package presentation;

import application.SmartCardConnector;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

/**
 * Created by Patrick on 19.06.2015.
 */
public class MainController
{
    @FXML
    Button connectButton;

    SmartCardConnector card;

    public MainController()
    {
    }

    @FXML
    public void initialize()
    {
        connectButton.addEventHandler(ActionEvent.ACTION, e -> connectSmartCard());
    }

    private void connectSmartCard()
    {
        this.card = new SmartCardConnector();
        if (!card.connect())
        {
            return;
        }

        card.selectApplet("Identification");
    }
}
