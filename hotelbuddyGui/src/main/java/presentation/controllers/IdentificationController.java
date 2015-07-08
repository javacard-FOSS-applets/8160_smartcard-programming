package presentation.controllers;

import application.hotelbuddy.IdentificationApplet;
import common.Result;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import presentation.models.IdentificationModel;

/**
 * Created by Patrick on 08.07.2015.
 */
public class IdentificationController
{
    public Label nameLabel, birthDateLabel, carIdLabel;
    public Button getButton;

    private IdentificationModel model = new IdentificationModel();

    public IdentificationController()
    {
        this.model = new IdentificationModel();
    }

    @FXML
    public void initialize()
    {
        initializeBindings();
    }

    /**
     * Uses the IdentificationApplet to get Name, Date of Birth and CarID
     */
    private void getIdentificationData()
    {
        Result<String> nameResult = IdentificationApplet.getName();
        if (nameResult.isSuccess())
        {
            this.model.setName(nameResult.get());
        }

        Result<String> birthDateResult = IdentificationApplet.getBirthDay();
        if (birthDateResult.isSuccess())
        {
            this.model.setBirthDate(birthDateResult.get());
        }

        Result<String> carIdResult = IdentificationApplet.getCarId();
        if (carIdResult.isSuccess())
        {
            this.model.setCarId(carIdResult.get());
        }
    }

    private void initializeBindings()
    {
        getButton.addEventHandler(ActionEvent.ACTION, e -> getIdentificationData());

        nameLabel.textProperty().bind(this.model.nameProperty());
        birthDateLabel.textProperty().bind(this.model.birthDateProperty());
        carIdLabel.textProperty().bind(this.model.carIdProperty());
    }
}
