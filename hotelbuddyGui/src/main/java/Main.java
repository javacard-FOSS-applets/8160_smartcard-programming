import application.card.JavaCard;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Patrick on 19.06.2015.
 */
public class Main extends Application
{
    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        Parent root = FXMLLoader.load(getClass().getResource("/views/MainView.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/app.css").toString());
        Stage stage = new Stage();
        stage.setTitle("hotelbuddy");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(e -> close());
    }

    private void close()
    {
        JavaCard.current().shutdown();
    }
}
