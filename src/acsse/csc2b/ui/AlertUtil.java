package acsse.csc2b.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
/**
 * Basic alert dialog.
 */
public class AlertUtil {

    /**
     * Utility method to show an alert dialog.
     *
     * @param alertType The type of alert (ERROR, WARNING, INFORMATION, etc.)
     * @param title     The title of the alert dialog
     * @param header    The header text (can be null for no header)
     * @param content   The content of the alert message
     */
    public static void showAlert(AlertType alertType, String title, String header, String content) {
        // Create a new alert with the specified alert type
        Alert alert = new Alert(alertType);
        
        // Set the title, header, and content text
        alert.setTitle(title);
        if (header != null) {
            alert.setHeaderText(header);
        } else {
            alert.setHeaderText(null);  // No header text
        }
        alert.setContentText(content);

        // Display the alert and wait for the user to close it
        alert.showAndWait();
    }
}
