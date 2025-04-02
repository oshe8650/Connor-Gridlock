package com.gridlock.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GridlockServer {
    private static final int PORT = 12345;
    private static final Random random = new Random();
    private boolean running = true;
    private ExecutorService executor;
    
    public void start() {
        executor = Executors.newCachedThreadPool();
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Gridlock Server started on port " + PORT);
            
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    
                    // Handle client connection in a separate thread
                    executor.submit(() -> handleClient(clientSocket));
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }
    
    private void handleClient(Socket clientSocket) {
        try (
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);
                
                // Process client request
                if (inputLine.equals("REQUEST_SIMULATION_DATA")) {
                    // Generate some random simulation data
                    String simulationData = generateSimulationData();
                    out.println(simulationData);
                    System.out.println("Sent simulation data to client");
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("Client disconnected");
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
    
    private String generateSimulationData() {
        // Generate a simple JSON with random data
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"timestamp\":").append(System.currentTimeMillis()).append(",");
        sb.append("\"vehicleCount\":").append(random.nextInt(50) + 10).append(",");
        sb.append("\"avgSpeed\":").append(random.nextInt(60) + 10).append(",");
        sb.append("\"congestionLevel\":").append(random.nextDouble() * 10).append(",");
        sb.append("\"trafficLights\":[");
        
        int numLights = random.nextInt(5) + 3;
        for (int i = 0; i < numLights; i++) {
            sb.append("{");
            sb.append("\"id\":").append(i).append(",");
            sb.append("\"state\":\"").append(randomLightState()).append("\"");
            sb.append("}");
            if (i < numLights - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        sb.append("}");
        
        return sb.toString();
    }
    
    private String randomLightState() {
        String[] states = {"RED", "GREEN", "YELLOW"};
        return states[random.nextInt(states.length)];
    }
    
    public void stop() {
        running = false;
        if (executor != null) {
            executor.shutdownNow();
        }
        System.out.println("Server stopped");
    }
    
    public static void main(String[] args) {
        GridlockServer server = new GridlockServer();
        server.start();
    }
}