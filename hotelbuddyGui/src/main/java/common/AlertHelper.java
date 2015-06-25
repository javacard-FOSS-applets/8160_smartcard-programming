package common;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Created by Patrick on 24.06.2015.
 */
public class AlertHelper
{
    public static void showErrorAlert(String content)
    {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
            a.show();
        });
    }
    public static void showSuccessAlert(String content)
    {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
            a.show();
        });
    }
}
