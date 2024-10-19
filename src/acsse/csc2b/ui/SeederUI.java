package acsse.csc2b.ui;

import acsse.csc2b.backend.Seeder;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The SeederUI class provides the user interface for the Seeder mode.
 * It allows the user to bind to a port, display files available for sharing,
 * and add new files to the list of files available for sharing.
 */
public class SeederUI extends StackPane {

    private Seeder seeder; // Instance of Seeder for managing file sharing

    /**
     * Constructs a SeederUI instance and initializes the layout.
     */
    public SeederUI() {
        // Add the VBox layout to the StackPane and center it
        this.getChildren().add(bindToPort());
        this.setAlignment(Pos.CENTER);  // Center the VBox within the StackPane
    }

    /**
     * Creates and sets up the layout for binding to a port.
     * 
     * @return a VBox containing the port input and bind button.
     */
    private VBox bindToPort() {
        // Create a label to prompt the user for the port number
        Label lbl = new Label("Enter port to bind to:");
        lbl.setStyle("-fx-font-size: 16px;");  // Optional style for font size

        // Create an HBox for the TextField and Button with 20px spacing
        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER);  // Center the HBox content (TextField and Button)

        // Create a TextField for entering the port number
        TextField portToBind = new TextField();
        portToBind.setText("9876");  // Default port value
        portToBind.setPrefWidth(100);  // Set preferred width for the TextField

        // Create a Button to bind to the specified port
        Button btnBind = new Button("Bind");
        
        btnBind.setOnAction((e) -> {
            try {
                // Parse the port number from the TextField
                int port = Integer.parseInt(portToBind.getText());
                
                // Initialize the Seeder with the specified port
                seeder = new Seeder(port);
                
                // Show success alert
                AlertUtil.showAlert(AlertType.INFORMATION, "Success", null, "Successfully bound to port: " + port);
                
                // Remove the current layout and add the file selection layout
                this.getChildren().remove(0);
                this.getChildren().add(SelectFilesToShare());
                
                // Start the Seeder thread if not already running
                if (!seeder.isRunning()) {
                    Thread th = new Thread(seeder);
                    th.setDaemon(true); // Ensure the thread terminates when the app exits
                    th.start();
                }
            } catch (NumberFormatException nfe) {
                // Show error alert for invalid port number
                AlertUtil.showAlert(AlertType.ERROR, "Error", "Invalid Port Number", "Ensure the port number entered is an integer.");
            } catch (Exception ex) {
                ex.printStackTrace();  // Print stack trace for other exceptions
            }
        });

        // Add the TextField and Button to the HBox
        box.getChildren().addAll(portToBind, btnBind);

        // Create a VBox to hold the label and HBox, with 10px spacing and center alignment
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);  // Center content within the VBox

        // Add the label and HBox to the VBox
        vbox.getChildren().addAll(lbl, box);

        return vbox;  // Return the fully constructed VBox
    }

    /**
     * Creates and sets up the layout for selecting files to share.
     * 
     * @return a VBox containing the list of files and a button to add files.
     */
    private VBox SelectFilesToShare() {
        // Create a label to indicate available files for sharing
        Label lbl = new Label("Files available for sharing");
        lbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Create a TextArea to display the list of files
        TextArea txtFiles = new TextArea();
        txtFiles.setPrefSize(200, 200);  // Set preferred size for TextArea
        txtFiles.setMaxWidth(250);
        txtFiles.setEditable(false);  // Make the TextArea read-only for displaying files

        // Create a button to allow users to add files to the sharing list
        Button btnSelectFilesToShare = new Button("Add Files");
        btnSelectFilesToShare.setPrefWidth(150);

        // Update the TextArea with initial files if available
        if (Seeder.initialFiles != null) {
            txtFiles.clear();
            Platform.runLater(() -> txtFiles.setText(Seeder.initialFiles));
        }

        btnSelectFilesToShare.setOnAction((e) -> {
            // Get the current stage from the button
            Stage currentStage = (Stage) btnSelectFilesToShare.getScene().getWindow();
            
            // Call the method to add files and get the updated list
            String res = seeder.AddFiles(currentStage);
            
            // Update the TextArea with the new list of files
            txtFiles.clear();
            txtFiles.setText(res);
        });

        // Create a VBox layout and set its properties
        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPrefSize(400, 400);  // Set preferred size for VBox

        // Add the label, TextArea, and button to the VBox
        vbox.getChildren().addAll(lbl, txtFiles, btnSelectFilesToShare);

        // Set the scene size when VBox is added to the scene
        vbox.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getWindow().setHeight(400);  // Set the window height
                newScene.getWindow().setWidth(400);   // Set the window width
            }
        });

        return vbox;  // Return the fully constructed VBox
    }
}
