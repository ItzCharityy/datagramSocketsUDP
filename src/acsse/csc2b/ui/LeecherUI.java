package acsse.csc2b.ui;

import java.util.StringTokenizer;

import acsse.csc2b.backend.Leecher;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * The LeecherUI class represents the user interface for the Leecher.
 * It allows users to connect to a host, list available files, and download them.
 */
public class LeecherUI extends StackPane {

    private Leecher leecher;

    /**
     * Constructor for LeecherUI.
     * Initializes the user interface by displaying the connectToHost view.
     */
    public LeecherUI() {
        this.getChildren().add(connectToHost()); // Add the connect-to-host view as the first UI component
    }

    /**
     * Creates a VBox layout for connecting to a host.
     * 
     * @return VBox layout that contains the IP address and port input fields and a connect button.
     */
    private VBox connectToHost() {
        VBox box = new VBox(20); // Create a VBox with spacing between elements
        box.setPadding(new Insets(20)); // Add padding for better spacing

        GridPane boxHolder = new GridPane(); // Create a GridPane to organize elements
        boxHolder.setHgap(10); // Horizontal gap between columns
        boxHolder.setVgap(10); // Vertical gap between rows
        boxHolder.setPadding(new Insets(10)); // Padding around GridPane

        // Labels for IP Address and Port input fields
        Label lblHostIp = new Label("IP Address:");
        Label lblHostPort = new Label("Port:");

        // Text fields for entering IP Address and Port
        TextField txtHostIp = new TextField("localhost");
        TextField txtHostPort = new TextField("9876");

        // Connect button to establish connection with the host
        Button btnConnect = new Button("Connect");
        btnConnect.setMaxWidth(Double.MAX_VALUE); // Make the button span the entire GridPane width

        // Define column constraints for the GridPane layout
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50); // 50% width for first column
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50); // 50% width for second column
        boxHolder.getColumnConstraints().addAll(col1, col2);

        // Add components to the GridPane
        boxHolder.add(lblHostIp, 0, 0);
        boxHolder.add(lblHostPort, 1, 0);
        boxHolder.add(txtHostIp, 0, 1);
        boxHolder.add(txtHostPort, 1, 1);

        // Make the connect button span both columns
        GridPane.setColumnSpan(btnConnect, 2);
        GridPane.setHalignment(btnConnect, HPos.CENTER); // Center align the button

        // Add the GridPane and connect button to the VBox
        box.getChildren().addAll(boxHolder, btnConnect);
        box.setAlignment(Pos.CENTER); // Center align the VBox content

        // Event handler for connect button
        btnConnect.setOnAction((e) -> {
            try {
                // Create a new Leecher instance and connect to the host
                leecher = new Leecher("localhost", 9876);
                this.getChildren().remove(0); // Remove the connectToHost view
                this.getChildren().add(Sharing()); // Add the Sharing view

                // Show a success alert when connected
                AlertUtil.showAlert(AlertType.INFORMATION, null, null, "You've successfully connected to host ....");

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        return box;
    }

    /**
     * Creates the sharing interface, which allows users to list and download files.
     * 
     * @return VBox layout for file sharing.
     */
    private VBox Sharing() {
        // Create a label for the file sharing section
        Label lbl = new Label("Files available for sharing");
        lbl.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;"); // Style the label

        // Create a TextArea to display available files
        TextArea txtFiles = new TextArea();
        txtFiles.setPrefSize(250, 200); // Set preferred size
        txtFiles.setEditable(false); // Make it read-only for displaying files

        // Create a button to list files available for sharing
        Button btnListFiles = new Button("List Files");
        btnListFiles.setPrefWidth(150); // Set button width

        // ChoiceBox to display the list of files available for download
        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.setPrefWidth(150); // Set width of the ChoiceBox

        // Event handler for the List Files button
        btnListFiles.setOnAction((e) -> {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    // Send request to list files from the server
                    leecher.sendRequest("LIST");
                    String res = leecher.receiveResponse();

                    // Update UI components on the JavaFX thread
                    Platform.runLater(() -> {
                        txtFiles.setText(res); // Display the response in the TextArea

                        // Tokenize the response and populate the ChoiceBox with file names
                        StringTokenizer tk = new StringTokenizer(res, "\n");
                        choiceBox.getItems().clear(); // Clear previous options

                        while (tk.hasMoreTokens()) {
                            String file = tk.nextToken();
                            choiceBox.getItems().add(file); // Add each file to the ChoiceBox
                        }

                        // Select the first item if available
                        if (!choiceBox.getItems().isEmpty()) {
                            choiceBox.getSelectionModel().select(0);
                        }
                    });

                    return null;
                }
            };

            // Run the task in a new thread
            new Thread(task).start();
        });

        // HBox to hold the ChoiceBox and download button
        HBox hbox = new HBox(10); // Set spacing between elements
        hbox.setAlignment(Pos.CENTER); // Center align HBox content

        // Create a download button
        Button btnDownload = new Button("Download");
        btnDownload.setPrefWidth(150); // Set width for the download button

        // Event handler for the Download button
        btnDownload.setOnAction((e) -> {
            String selectedItem = choiceBox.getSelectionModel().getSelectedItem(); // Get the selected file
            int selectedIndex = choiceBox.getSelectionModel().getSelectedIndex() + 1;

            // Send request to download the selected file
            leecher.sendRequest("FILE " + selectedIndex);

            // Task to handle file download in the background
            Task<Void> backgrouTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    leecher.receiveFile(selectedItem); // Receive the file
                    return null;
                }
            };

            // Start the download task in a new thread
            new Thread(backgrouTask).start();
        });

        // Add the ChoiceBox and download button to the HBox
        hbox.getChildren().addAll(choiceBox, btnDownload);

        // Create a VBox layout to organize the components
        VBox vbox = new VBox(20); // Set vertical spacing
        vbox.setAlignment(Pos.CENTER); // Center align VBox content
        vbox.setPadding(new Insets(20)); // Add padding
        vbox.setPrefSize(400, 500); // Set preferred size

        // Add the label, TextArea, List Files button, and HBox to the VBox
        vbox.getChildren().addAll(lbl, txtFiles, btnListFiles, hbox);

        // Adjust the window size when the VBox is added to the scene
        vbox.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getWindow().setHeight(500);
                newScene.getWindow().setWidth(400);
            }
        });

        return vbox;
    }
}
