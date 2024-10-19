package acsse.csc2b.backend;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Leecher class represents a client that requests files or data from a Seeder
 * over a Datagram (UDP) connection.
 * 
 * This class is responsible for sending requests to the Seeder and receiving
 * responses and files.
 * 
 * It extends JavaFX's Task<Void> to allow background tasks in a JavaFX
 * application.
 */
public class Leecher {

    private DatagramSocket socket; // Socket for sending/receiving UDP packets
    private InetAddress seederAddress; // Address of the Seeder (server)
    private int seederPort; // Port of the Seeder

    // Buffer size for receiving data chunks
    private static final int BUFFER_SIZE = 1024 * 4;

    /**
     * Constructor to initialize the Leecher with the Seeder's address and port.
     * 
     * @param address IP address of the Seeder.
     * @param port    Port number of the Seeder.
     */
    public Leecher(String address, int port) {
        try {
            // Initialize the DatagramSocket for UDP communication
            socket = new DatagramSocket();

            // Get the Seeder's IP address
            seederAddress = InetAddress.getByName(address);
            seederPort = port;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a request message to the Seeder.
     * 
     * @param message The message to be sent to the Seeder.
     */
    public void sendRequest(String message) {
        try {
            // Convert the message to bytes
            byte[] sendData = message.getBytes();

            // Create a DatagramPacket to send the message to the Seeder
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, seederAddress, seederPort);

            // Send the packet
            socket.send(sendPacket);
            System.out.println("Request sent: " + message);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Checks if the socket is closed. If it is, reinitializes it.
     * @throws SocketException 
     */
    private void checkAndReinitializeSocket() throws SocketException {
        if (socket == null || socket.isClosed()) {
            System.out.println("Socket is closed or null. Reinitializing...");
            socket = new DatagramSocket();
           
        } else {
            System.out.println("Socket is open and operational.");
        }
    }


    /**
     * Receives a response from the Seeder after sending a request.
     * 
     * @return The response message from the Seeder.
     */
    public String receiveResponse() {
        try {

            checkAndReinitializeSocket();

            // Create a buffer to store the response
            byte[] receiveBuffer = new byte[BUFFER_SIZE];

            // Create a DatagramPacket to receive the response
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            // Receive the response from the Seeder
            socket.receive(receivePacket);

            // Extract the message from the packet and return it
            return new String(receivePacket.getData(), 0, receivePacket.getLength());

        } catch (IOException e) {
            e.printStackTrace();
            return "Error receiving response.";
        }
    }

    /**
     * Receives a file from the Seeder in chunks and saves it to the local file
     * system.
     * 
     * @param fileId The ID of the file to be downloaded.
     */
    public void receiveFile(String fileId) {
        try {
            // Step 1: Receive metadata (file size and file name)
            byte[] metadataBuffer = new byte[1024]; // Buffer for metadata
            DatagramPacket metadataPacket = new DatagramPacket(metadataBuffer, metadataBuffer.length);

            // Receive metadata packet from the Seeder
            socket.receive(metadataPacket);
            String metadata = new String(metadataPacket.getData(), 0, metadataPacket.getLength());

            // Parse the metadata (fileSize and fileName)
            String[] metadataParts = metadata.split(" ");
            long fileSize = Long.parseLong(metadataParts[0]);
            String fileName = metadataParts[1];

            // Create the destination file path in the Leecher's directory
            File destFile = new File("data/leecher/" + fileName);
            destFile.getParentFile().mkdirs(); // Ensure the directory exists

            // Step 2: Receive the file data in chunks and save it
            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                byte[] fileBuffer = new byte[1024]; // Buffer for file chunks
                long totalBytesReceived = 0;

                // Receive the file data in chunks until the total file size is received
                while (totalBytesReceived < fileSize) {
                    DatagramPacket filePacket = new DatagramPacket(fileBuffer, fileBuffer.length);
                    socket.receive(filePacket); // Receive each chunk

                    // Write the received chunk to the file
                    fos.write(filePacket.getData(), 0, filePacket.getLength());
                    totalBytesReceived += filePacket.getLength();

                    // Print the progress of the file reception
                    System.out.println("Received: " + totalBytesReceived + "/" + fileSize + " bytes.");
                }

                System.out.println("File received successfully and saved to: " + destFile.getAbsolutePath());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the socket when file reception is completed
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    

    /**
     * Main method to test the Leecher functionality.
     * 
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        // Create a Leecher instance with the Seeder's IP address and port
        Leecher leecher = new Leecher("localhost", 9876);

        // Send a request to the Seeder to list available files
        leecher.sendRequest("LIST");

        // Optionally, receive a response from the Seeder
        String response = leecher.receiveResponse();
        System.out.println("Response from Seeder: " + response);

        // For receiving a file, you can uncomment this to receive a file with the specified fileId
        // leecher.receiveFile("fileId");
    }

    
}
