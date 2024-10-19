
import acsse.csc2b.ui.StartLayout;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    public static void main(String[] args) {
        launch(args);  // Launches the JavaFX application
    }


    
    @Override
    public void start(Stage stage) {
        
        // Create a Scene with the VBox as its root node
        Scene scene = new Scene(new StartLayout(), 400, 200);  // Width = 400, Height = 200

        // Set the scene on the stage
        stage.setTitle("JavaFX Basic Application");
        stage.setScene(scene);
        stage.show();  // Display the stage
    }

    
}
