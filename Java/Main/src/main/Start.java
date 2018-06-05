
package main;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/*
 * Start.java
 *
 * This component is only used to start the Main java Fx application
 * 
 * Created: 2018/02/27
 * @author Rilind Hasanaj <rilind.hasanaj0018@stud.hkr.se>
 */
public class Start extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
        Scene scene = new Scene(root);
        stage.setTitle("Modular sensor platform");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        launch(args);

    }

}
