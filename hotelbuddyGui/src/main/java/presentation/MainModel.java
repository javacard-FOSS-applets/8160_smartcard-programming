package presentation;

import javafx.beans.property.*;
import javafx.scene.paint.Paint;

import java.time.LocalDate;

/**
 * Created by Patrick on 22.06.2015.
 */
public class MainModel
{
    private StringProperty name = new SimpleStringProperty("");
    private StringProperty carId = new SimpleStringProperty("");
    private ObjectProperty<LocalDate> birthDate = new SimpleObjectProperty<>();
    private StringProperty safePin = new SimpleStringProperty("");
    private StringProperty logMessage = new SimpleStringProperty("");
    private BooleanProperty isConnectionEstablished = new SimpleBooleanProperty(false);
    private StringProperty connectionStatus = new SimpleStringProperty("Disconnected");
    private ObjectProperty<Paint> connectionStatusColor = new SimpleObjectProperty<>();

    public String getName()
    {
        return name.get();
    }

    public void setName(String name)
    {
        this.name.set(name);
    }

    public StringProperty nameProperty()
    {
        return name;
    }

    public String getCarId()
    {
        return carId.get();
    }

    public void setCarId(String carId)
    {
        this.carId.set(carId);
    }

    public StringProperty carIdProperty()
    {
        return carId;
    }

    public LocalDate getBirthDate()
    {
        return birthDate.get();
    }

    public void setBirthDate(LocalDate birthDate)
    {
        this.birthDate.set(birthDate);
    }

    public ObjectProperty<LocalDate> birthDateProperty()
    {
        return birthDate;
    }

    public String getSafePin()
    {
        return safePin.get();
    }

    public void setSafePin(String safePin)
    {
        this.safePin.set(safePin);
    }

    public StringProperty safePinProperty()
    {
        return safePin;
    }

    public String getLogMessage()
    {
        return logMessage.get();
    }

    public StringProperty logMessageProperty()
    {
        return logMessage;
    }

    public void setLogMessage(String logMessage)
    {
        this.logMessage.set(logMessage);
    }

    public boolean getIsConnectionEstablished()
    {
        return isConnectionEstablished.get();
    }

    public BooleanProperty isConnectionEstablishedProperty()
    {
        return isConnectionEstablished;
    }

    public void setIsConnectionEstablished(boolean isConnectionEstablished)
    {
        this.isConnectionEstablished.set(isConnectionEstablished);
    }

    public String getConnectionStatus()
    {
        return connectionStatus.get();
    }

    public StringProperty connectionStatusProperty()
    {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus)
    {
        this.connectionStatus.set(connectionStatus);
    }

    public Paint getConnectionStatusColor()
    {
        return connectionStatusColor.get();
    }

    public ObjectProperty<Paint> connectionStatusColorProperty()
    {
        return connectionStatusColor;
    }

    public void setConnectionStatusColor(Paint connectionStatusColor)
    {
        this.connectionStatusColor.set(connectionStatusColor);
    }
}
