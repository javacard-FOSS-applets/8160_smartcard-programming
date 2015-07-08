package presentation.controllers;

import application.hotelbuddy.AccessApplet;
import application.hotelbuddy.AccessRestrictedRoom;
import common.Result;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import presentation.models.AccessModel;

/**
 * Created by Patrick on 08.07.2015.
 */
public class AccessController
{
    public Button checkButton;
    public Label checkStatusLabel;
    public ComboBox<AccessRestrictedRoom> roomComboBox;

    private AccessModel model = new AccessModel();

    public AccessController()
    {
        this.model = new AccessModel();
    }

    @FXML
    public void initialize()
    {
        initializeBindings();
        this.model.setRooms(FXCollections.observableArrayList(AccessRestrictedRoom.values()));
        this.roomComboBox.getSelectionModel().selectFirst();
    }

    /**
     * Uses the SelectedRoom and AccessApplet to check the access restriction
     * Shows the result in the CheckStatus label
     */
    private void checkRoom()
    {
        Result<Boolean> result = AccessApplet.checkRoom(this.model.getSelectedRoom());
        if (!result.isSuccess())
        {
            this.model.setCheckStatus("Access denied!");
            this.model.setCheckStatusColor(Color.RED);
            return;
        }

        this.model.setCheckStatus("Access allowed!");
        this.model.setCheckStatusColor(Color.GREEN);
    }

    private void initializeBindings()
    {
        checkButton.addEventHandler(ActionEvent.ACTION, e -> checkRoom());

        checkStatusLabel.textProperty().bind(this.model.checkStatusProperty());
        checkStatusLabel.textFillProperty().bind(this.model.checkStatusColorProperty());
        roomComboBox.valueProperty().bindBidirectional(this.model.selectedRoomProperty());
        roomComboBox.itemsProperty().bindBidirectional(this.model.roomsProperty());
    }
}
