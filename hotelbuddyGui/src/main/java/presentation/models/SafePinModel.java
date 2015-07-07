package presentation.models;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Created by Patrick on 07.07.2015.
 */
public class SafePinModel
{
    private StringProperty safePin = new SimpleStringProperty("");

    private StringProperty checkStatus = new SimpleStringProperty("Unchecked");
    private ObjectProperty<Paint> checkStatusColor = new SimpleObjectProperty<>(Color.ORANGE);

    public String getSafePin()
    {
        return safePin.get();
    }

    public StringProperty safePinProperty()
    {
        return safePin;
    }

    public void setSafePin(String safePin)
    {
        this.safePin.set(safePin);
    }

    public String getCheckStatus()
    {
        return checkStatus.get();
    }

    public StringProperty checkStatusProperty()
    {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus)
    {
        this.checkStatus.set(checkStatus);
    }

    public Paint getCheckStatusColor()
    {
        return checkStatusColor.get();
    }

    public ObjectProperty<Paint> checkStatusColorProperty()
    {
        return checkStatusColor;
    }

    public void setCheckStatusColor(Paint checkStatusColor)
    {
        this.checkStatusColor.set(checkStatusColor);
    }
}
