package presentation.controllers;

import application.hotelbuddy.AccessApplet;
import application.hotelbuddy.AccessRestrictedRoom;
import application.hotelbuddy.IdentificationApplet;
import common.AlertHelper;
import common.Result;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import presentation.controls.NumericTextField;
import presentation.models.ConfigurationModel;

import java.util.HashMap;

/**
 * Created by Patrick on 08.07.2015.
 */
public class ConfigurationController
{
    public Button setIdentificationButton, restAccessControl, resetIdentification, setAccessButton;
    public DatePicker birthDateDatePicker;
    public TextField carIdTextField, nameTextField;
    public NumericTextField safePinTextField;
    public CheckBox classicBarCheckbox, casinoCheckbox, poolCheckbox, skyBarCheckbox, wellnessCheckbox;

    private ConfigurationModel model;

    public ConfigurationController()
    {
        this.model = new ConfigurationModel();
    }

    @FXML
    public void initialize()
    {
        initializeBindings();
    }

    /**
     * Uses the IdentificationApplet to set Name, Date of Birth, CarID and SafePin
     */
    private void setIdentificationData()
    {
        Result<Boolean> result = IdentificationApplet.setName(this.model.getName());
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        result = IdentificationApplet.setBirthDay(this.model.getBirthDate());
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        result = IdentificationApplet.setCarId(this.model.getCarId());
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        result = IdentificationApplet.setSafePin(this.model.getSafePin());
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        AlertHelper.showSuccessAlert("Data successfully set.");
    }

    /**
     * Resets the IdentificationApplet
     */
    private void resetIdentification()
    {
        Result<Boolean> result = IdentificationApplet.reset();
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
        }
    }

    /**
     * Resets the AccessApplet
     */
    private void resetAccess()
    {
        Result<Boolean> result = AccessApplet.reset();
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
        }
    }

    /**
     * Sets the access restriction to the AccessApplet
     */
    private void setAccessData()
    {
        HashMap<AccessRestrictedRoom, Boolean> accessRestriction = new HashMap<>();
        accessRestriction.put(AccessRestrictedRoom.ClassicBar, this.model.getClassicBarAccess());
        accessRestriction.put(AccessRestrictedRoom.Casino, this.model.getCasinoAccess());
        accessRestriction.put(AccessRestrictedRoom.Pool, this.model.getPoolAccess());
        accessRestriction.put(AccessRestrictedRoom.SkyBar, this.model.getSkyBarAccess());
        accessRestriction.put(AccessRestrictedRoom.Wellness, this.model.getWellnessAccess());

        Result<Boolean> result = AccessApplet.setAccess(accessRestriction);
        if (!result.isSuccess())
        {
            AlertHelper.showErrorAlert(result.getErrorMessage());
            return;
        }

        AlertHelper.showSuccessAlert("Data successfully set.");
    }

    private void initializeBindings()
    {
        setIdentificationButton.addEventHandler(ActionEvent.ACTION, e -> setIdentificationData());
        setAccessButton.addEventHandler(ActionEvent.ACTION, e -> setAccessData());
        resetIdentification.addEventHandler(ActionEvent.ACTION, e -> resetIdentification());
        restAccessControl.addEventHandler(ActionEvent.ACTION, e -> resetAccess());

        nameTextField.textProperty().bindBidirectional(this.model.nameProperty());
        carIdTextField.textProperty().bindBidirectional(this.model.carIdProperty());
        safePinTextField.setMaxlength(IdentificationApplet.SafePinLength);
        safePinTextField.textProperty().bindBidirectional(this.model.safePinProperty());
        birthDateDatePicker.valueProperty().bindBidirectional(this.model.birthDateProperty());

        classicBarCheckbox.selectedProperty().bindBidirectional(this.model.classicBarAccessProperty());
        casinoCheckbox.selectedProperty().bindBidirectional(this.model.casinoAccessProperty());
        poolCheckbox.selectedProperty().bindBidirectional(this.model.poolAccessProperty());
        skyBarCheckbox.selectedProperty().bindBidirectional(this.model.skyBarAccessProperty());
        wellnessCheckbox.selectedProperty().bindBidirectional(this.model.wellnessAccessProperty());
    }
}
