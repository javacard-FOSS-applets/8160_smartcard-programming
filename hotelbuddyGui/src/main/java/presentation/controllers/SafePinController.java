package presentation.controllers;

import application.hotelbuddy.IdentificationApplet;
import common.Result;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import presentation.controls.NumericTextField;
import presentation.models.SafePinModel;

/**
 * Created by Patrick on 08.07.2015.
 */
public class SafePinController
{
    public NumericTextField safePinTextField;
    public Label resultLabel;
    public Button checkButton;

    private SafePinModel model;

    public SafePinController()
    {
        this.model = new SafePinModel();
    }

    @FXML
    public void initialize()
    {
        initializeBindings();
    }

    /**
     * Checks the entered Safe PIN
     */
    private void checkSafePin()
    {
        Result<Boolean> nameResult = IdentificationApplet.checkSafePin(this.model.getSafePin());
        if (!nameResult.isSuccess())
        {
            this.model.setCheckStatus("Wrong Safe PIN!");
            this.model.setCheckStatusColor(Color.RED);
            return;
        }

        this.model.setCheckStatus("Correct Safe PIN");
        this.model.setCheckStatusColor(Color.GREEN);
    }

    private void initializeBindings()
    {
        checkButton.addEventHandler(ActionEvent.ACTION, e -> checkSafePin());

        safePinTextField.setMaxlength(IdentificationApplet.SAFEPIN_LENGTH);
        safePinTextField.textProperty().bindBidirectional(this.model.safePinProperty());
        resultLabel.textProperty().bind(this.model.checkStatusProperty());
        resultLabel.textFillProperty().bind(this.model.checkStatusColorProperty());
    }
}
