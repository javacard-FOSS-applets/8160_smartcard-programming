package presentation.models;

import javafx.beans.property.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;


/**
 * Created by Patrick on 24.06.2015.
 */
public class ConnectionModel
{
    private BooleanProperty isConnectionEstablished = new SimpleBooleanProperty(false);
    private StringProperty connectionStatus = new SimpleStringProperty("Disconnected");
    private ObjectProperty<Paint> connectionStatusColor = new SimpleObjectProperty<>(Color.RED);

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
