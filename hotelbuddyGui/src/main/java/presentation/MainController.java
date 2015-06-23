package presentation;

import application.card.JavaCard;
import application.hotelbuddy.CryptographyApplet;
import application.hotelbuddy.IdentificationApplet;
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
        if (!JavaCard.current().connect().isSuccess())
        {
            // TODO show alert
            return;
        }

        CryptographyApplet.setupRSACryptographyHelper();
    }

    private void setIdentificationData()
    {
        IdentificationApplet.setName(this.model.getName());
    }
}
