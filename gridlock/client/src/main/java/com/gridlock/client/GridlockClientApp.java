package com.gridlock.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GridlockClientApp extends Application {

    private TextArea logArea;
    private Button connectButton;
    private Button requestDataButton;
    private Label statusLabel;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ExecutorService executorService;
    private boolean connected = false;

    @Override
    public void start(Stage primaryStage) {
        executorService = Executors.newSingleThreadExecutor();
        
        // Create UI components
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        
        connectButton = new Button("Connect to Server");
        requestDataButton = new Button("Request Simulation Data");
        requestDataButton.setDisable(true);
        
        statusLabel = new Label("Not Connected");
        
        // Set up button actions
        connectButton.setOnAction(e -> toggleConnection());
        requestDataButton.setOnAction(e -> requestData());
        
        // Layout
        HBox buttonBox = new HBox(10, connectButton, requestDataButton);
        buttonBox.setPadding(new Insets(10));
        
        BorderPane statusPane = new BorderPane();
        statusPane.setLeft(statusLabel);
        statusPane.setPadding(new Insets(5, 10, 5, 10));
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        VBox.setVgrow(logArea, Priority.ALWAYS);
        root.getChildren().addAll(buttonBox, logArea, statusPane);
        
        // Create scene
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Gridlock Client");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        log("Gridlock client initialized. Click 'Connect to Server' to begin.");
    }
    
    private void toggleConnection() {
        if (!connected) {
            connectToServer();
        } else {
            disconnectFromServer();
        }
    }
    
    private void connectToServer() {
        executorService.submit(() -> {
            try {
                // Connect to server (default: localhost:12345)
                socket = new Socket("localhost", 12345);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                Platform.runLater(() -> {
                    connected = true;
                    connectButton.setText("Disconnect");
                    requestDataButton.setDisable(false);
                    statusLabel.setText("Connected to server");
                    log("Connected to server at localhost:12345");
                });
                
                // Start listening for server messages
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String message = inputLine;
                    Platform.runLater(() -> log("Received: " + message));
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    log("Error: " + e.getMessage());
                    disconnectFromServer();
                });
            }
        });
    }
    
    private void disconnectFromServer() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (Exception e) {
            log("Error during disconnect: " + e.getMessage());
        } finally {
            Platform.runLater(() -> {
                connected = false;
                connectButton.setText("Connect to Server");
                requestDataButton.setDisable(true);
                statusLabel.setText("Not Connected");
                log("Disconnected from server");
            });
        }
    }
    
    private void requestData() {
        if (connected && out != null) {
            out.println("REQUEST_SIMULATION_DATA");
            log("Requested simulation data from server");
        } else {
            log("Not connected to server");
        }
    }
    
    private void log(String message) {
        logArea.appendText(message + "\n");
    }
    
    @Override
    public void stop() {
        disconnectFromServer();
        executorService.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}