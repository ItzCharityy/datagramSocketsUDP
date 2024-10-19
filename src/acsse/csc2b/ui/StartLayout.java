package acsse.csc2b.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * The StartLayout class represents the initial layout for the application.
 * It provides options for the user to select between different modes: Leecher or Seeder.
 * The layout consists of a heading label and two buttons to navigate to the respective modes.
 */
public class StartLayout extends StackPane {

    /**
     * Constructs a StartLayout instance and initializes the layout.
     */
    public StartLayout() {
        // Add the VBox to the StackPane and center it
        this.getChildren().add(setUpStarting());
        
        // Center the VBox within the StackPane
        this.setAlignment(Pos.CENTER);
    }

    /**
     * Sets up the starting layout with a heading and mode selection buttons.
     * 
     * @return a VBox containing the heading label and buttons for mode selection.
     */
    private VBox setUpStarting() {
        // Create a VBox with 20px spacing between elements
        VBox vbox = new VBox(20D);
        vbox.setAlignment(Pos.CENTER);  // Center the content within the VBox

        // Create and style the heading label
        Label lblHeading = new Label("SELECT MODE");
        lblHeading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");  // Set font size and weight

        // Create an HBox to hold the buttons with 10px spacing between them
        HBox btnModesHolder = new HBox(10);
        btnModesHolder.setAlignment(Pos.CENTER);  // Center the buttons within the HBox

        // Create buttons for Leecher and Seeder modes
        Button leecherButton = new Button("Leecher");
        Button seederButton = new Button("Seeder");

        // Set action for the Leecher button
        leecherButton.setOnAction((e) -> {
            // Remove the current layout and add the LeecherUI layout
            this.getChildren().remove(0);
            this.getChildren().add(new LeecherUI());
        });

        // Set action for the Seeder button
        seederButton.setOnAction((e) -> {
            // Remove the current layout and add the SeederUI layout
            this.getChildren().remove(0);
            this.getChildren().add(new SeederUI());
        });

        // Add buttons to the HBox
        btnModesHolder.getChildren().addAll(leecherButton, seederButton);

        // Add the heading label and the HBox (with buttons) to the VBox
        vbox.getChildren().addAll(lblHeading, btnModesHolder);

        // Return the fully constructed VBox
        return vbox;
    }
}
