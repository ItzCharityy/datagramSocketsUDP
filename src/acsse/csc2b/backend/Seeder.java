package acsse.csc2b.backend;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

/**
 * Seeder class that listens for requests and sends files to peers.
 */
public class Seeder extends Task<Void> {

    // Socket for sending and receiving UDP packets
    private DatagramSocket socket;
    
    // Packet for receiving data
    private DatagramPacket receivePacket;

    // Port to listen on
    private int port;

    // Commands for listing files and requesting a file
    private static final String CLIST = "LIST";
    private static final String CFILE = "FILE";
    
    // Number of files available for sharing
    private static int numFiles = 0;

    // Initially available files
    public static String initialFiles = null;

    // Buffer size for receiving data (4 KB)
    private static final int BUFFER_SIZE = 1024 * 4;

    /**
     * Constructor initializes the Seeder with a specified port.
     * @param port The port on which the Seeder will listen for requests.
     */
    public Seeder(int port) {
        this.port = port;
        try {
            // Bind the DatagramSocket to the specified port
            socket = new DatagramSocket(this.port);

            // Count files and initialize the file list for GUI
            Platform.runLater(() -> {
                countFiles();
                
                // If files are available, load them into initialFiles for GUI display
                if (numFiles >= 1) {
                    initialFiles = new String(ListFiles(), StandardCharsets.UTF_8);
                }
            });
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to continuously listen for requests and respond accordingly.
     */
    public void listenAndRespond() {
        byte[] receiveBuffer = new byte[BUFFER_SIZE];
        System.out.println("Listening on port: " + port);
        
        while (true) {
            try {
                // Receive incoming packet
                receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                socket.receive(receivePacket);

                // Extract data from the received packet
                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // If the command is "LIST", send the list of files
                if (receivedMessage.contentEquals(CLIST)) {
                    byte[] responseMessage = ListFiles();
                    response(responseMessage, receivePacket.getAddress(), receivePacket.getPort());
                } 
                // If the command is "FILE", send the requested file by ID
                else if (receivedMessage.startsWith(CFILE)) {
                    StringTokenizer tk = new StringTokenizer(receivedMessage, " ");
                    tk.nextToken(); // Skip the command
                    sendFile(tk.nextToken(), receivePacket.getAddress(), receivePacket.getPort());
                } 
                // Handle other messages
                else {
                    byte[] responseMessage = ("Seeder Received: " + receivedMessage).getBytes();
                    response(responseMessage, receivePacket.getAddress(), receivePacket.getPort());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method to send a response to the client.
     * @param toSend The data to be sent.
     * @param address The destination address.
     * @param port The destination port.
     */
    public void response(byte[] toSend, InetAddress address, int port) {
        try {
            DatagramPacket sendPacket = new DatagramPacket(toSend, toSend.length, address, port);
            socket.send(sendPacket);
            System.out.println("Response sent.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to list all available files.
     * @return A byte array representing the list of files.
     */
    private byte[] ListFiles() {
        StringBuilder sb = new StringBuilder();
        try (Scanner scan = new Scanner(new File("data/seeder/fileData.txt"))) {
            while (scan.hasNextLine()) {
                sb.append(scan.nextLine()).append("\n");
            }
        } catch (FileNotFoundException fnf) {
            fnf.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Method to add new files to the seeder's directory.
     * @param stage The stage for showing the file chooser dialog.
     * @return A string representing the list of newly added files.
     */
    public String AddFiles(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files to Add");

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(stage);

        if (selectedFiles == null || selectedFiles.isEmpty()) {
            System.out.println("No files selected.");
            return "";
        }

        File destFolder = new File("data/seeder");
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        File fileData = new File("data/seeder/fileData.txt");

        try (FileWriter writer = new FileWriter(fileData, true)) {
            StringBuilder fileList = new StringBuilder(new String(ListFiles(), StandardCharsets.UTF_8));
            for (File file : selectedFiles) {
                File destFile = new File(destFolder, file.getName());
                if (file.renameTo(destFile)) {
                    numFiles++;
                    String currentFile = numFiles + " " + file.getName();
                    fileList.append(currentFile).append("\n");
                    writer.write(currentFile + "\n");
                    System.out.println("File added: " + file.getName());
                } else {
                    System.out.println("Failed to add file: " + file.getName());
                }
            }
            return fileList.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Method to get the filename corresponding to a given file ID.
     * @param fileId The ID of the file.
     * @return The filename or null if not found.
     */
    private String getFilebyID(String fileId) throws FileNotFoundException {
        File file = new File("data/seeder/fileData.txt");
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }

        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                String line = scan.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(" ", 2);
                    if (parts.length >= 2 && Integer.parseInt(parts[0]) == Integer.parseInt(fileId)) {
                        return parts[1];  // Return the filename
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid file ID: " + fileId);
        }
        return null;  // Return null if no match is found
    }

    /**
     * Method to send a file to the client.
     * @param fileId The ID of the file.
     * @param receiverAddress The receiver's IP address.
     * @param receiverPort The receiver's port.
     */
    private void sendFile(String fileId, InetAddress receiverAddress, int receiverPort) {
        try {
            // Retrieve the filename by file ID
            String fileName = getFilebyID(fileId);
            if (fileName == null) {
                System.out.println("File ID not found.");
                return;
            }

            File file = new File("data/seeder/" + fileName);
            long fileSize = file.length();
            byte[] fileBytes = new byte[1024]; // 1 KB buffer for streaming

            // Send file metadata: size and filename
            String metadata = fileSize + " " + fileName;
            response(metadata.getBytes(), receiverAddress, receiverPort);

            // Send the file data in chunks
            try (FileInputStream fis = new FileInputStream(file)) {
                int bytesRead;
                while ((bytesRead = fis.read(fileBytes)) != -1) {
                    DatagramPacket filePacket = new DatagramPacket(fileBytes, bytesRead, receiverAddress, receiverPort);
                    socket.send(filePacket);
                }
            }
            System.out.println("File sent successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Count the number of files available for seeding.
     */
    private void countFiles() {
        try (Scanner scan = new Scanner(new File("data/seeder/fileData.txt"))) {
            while (scan.hasNextLine()) {
                scan.nextLine();
                numFiles++;
            }
        } catch (FileNotFoundException fnf) {
            fnf.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("Number of Files: " + numFiles);
    }

    @Override
    protected Void call() throws Exception {
        listenAndRespond();
        return null;
    }

    public static void main(String[] args) {
        Seeder seeder = new Seeder(8080); // Listening on port 8080
        Thread seederThread = new Thread(seeder);
        seederThread.start();
    }
}
