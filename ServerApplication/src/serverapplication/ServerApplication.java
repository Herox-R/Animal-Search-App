package serverapplication;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 *
 * @author Sylvester N
 */

public class ServerApplication extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton startButton;
    private JButton shutdownButton;
    private ServerSocket serverSocket;
    private boolean isRunning;

    private Connection connection;
    private PreparedStatement insertAnimalStatement;
    private PreparedStatement deleteAnimalStatement;
    private PreparedStatement searchAnimalStatement;

    public ServerApplication() {
        setTitle("Server Application");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize database connection
        initializeDatabase();

        // Username panel
        JPanel usernamePanel = new JPanel();
        usernamePanel.setLayout(new FlowLayout());
        JLabel usernameLabel = new JLabel("Username: ");
        usernameField = new JTextField(10);
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameField);
        add(usernamePanel, BorderLayout.NORTH);

        // Password panel
        JPanel passwordPanel = new JPanel();
        passwordPanel.setLayout(new FlowLayout());
        JLabel passwordLabel = new JLabel("Password: ");
        passwordField = new JPasswordField(10);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        add(passwordPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        startButton = new JButton("Start Server");
        startButton.addActionListener(new StartButtonListener());
        buttonPanel.add(startButton);
        shutdownButton = new JButton("Shutdown Server");
        shutdownButton.addActionListener(new ShutdownButtonListener());
        shutdownButton.setEnabled(false);
        buttonPanel.add(shutdownButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void initializeDatabase() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            String connectionString = "jdbc:sqlserver://localhost:1433;databaseName=AnimalDatabase;user=sa;password=Mpotjo@123456789;encrypt=true;trustServerCertificate=true;";
            connection = DriverManager.getConnection(connectionString);

            // Prepare statements
            insertAnimalStatement = connection.prepareStatement("INSERT INTO Animal (animalName, description, speciesId) VALUES (?, ?, ?)");
            deleteAnimalStatement = connection.prepareStatement("DELETE FROM Animal WHERE animalName = ?");
            searchAnimalStatement = connection.prepareStatement(
                "SELECT a.animalName, a.description, s.speciesName FROM Animal a " +
                "JOIN Species s ON a.speciesId = s.speciesId " +
                "WHERE a.animalName LIKE ? OR s.speciesName LIKE ?");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to database", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private class StartButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (authenticate(username, password)) {
                startButton.setEnabled(false);
                shutdownButton.setEnabled(true);
                isRunning = true;
                new Thread(ServerApplication.this::startServer).start();  // Run server in a new thread
            } else {
                JOptionPane.showMessageDialog(ServerApplication.this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ShutdownButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            shutdownServer();
        }
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(12345); // Change to your desired port
            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                new ServerThread(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void shutdownServer() {
        try {
            isRunning = false;
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            SwingUtilities.invokeLater(() -> {
                startButton.setEnabled(true);
                shutdownButton.setEnabled(false);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean authenticate(String username, String password) {
        try {
            PreparedStatement authStatement = connection.prepareStatement("SELECT * FROM Users WHERE username = ? AND password = ?");
            authStatement.setString(1, username);
            authStatement.setString(2, password);
            ResultSet rs = authStatement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private class ServerThread extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ServerThread(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String request;
                while ((request = in.readLine()) != null) {
                    String[] parts = request.split(":", 3);
                    String action = parts[0];
                    String response = "";

                    switch (action) {
                        case "SEARCH":
                            response = handleSearch(parts[1]);
                            break;
                        case "INSERT":
                            response = handleInsert(parts[1], parts[2]);
                            break;
                        case "DELETE":
                            response = handleDelete(parts[1]);
                            break;
                        default:
                            response = "Unknown request";
                    }

                    out.println(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                    in.close();
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private String handleSearch(String query) {
            try {
                searchAnimalStatement.setString(1, "%" + query + "%");
                searchAnimalStatement.setString(2, "%" + query + "%");
                ResultSet rs = searchAnimalStatement.executeQuery();
                StringBuilder result = new StringBuilder();

                while (rs.next()) {
                    result.append("Animal Name: ").append(rs.getString("animalName"))
                          .append(", Description: ").append(rs.getString("description"))
                          .append(", Species: ").append(rs.getString("speciesName"))
                          .append("\n");
                }

                return result.length() == 0 ? "No results found." : result.toString();
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error occurred during search.";
            }
        }

        private String handleInsert(String animalName, String speciesName) {
            try {
                // Find speciesId for the given speciesName
                PreparedStatement findSpeciesStmt = connection.prepareStatement("SELECT speciesId FROM Species WHERE speciesName = ?");
                findSpeciesStmt.setString(1, speciesName);
                ResultSet rs = findSpeciesStmt.executeQuery();

                if (rs.next()) {
                    int speciesId = rs.getInt("speciesId");
                    insertAnimalStatement.setString(1, animalName);
                    insertAnimalStatement.setString(2, ""); // Assuming description is empty for simplicity
                    insertAnimalStatement.setInt(3, speciesId);
                    int rowsAffected = insertAnimalStatement.executeUpdate();

                    return rowsAffected > 0 ? "Animal inserted successfully." : "Failed to insert animal.";
                } else {
                    return "Species not found.";
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error occurred during insert: " + e.getMessage();
            }
        }

        private String handleDelete(String animalName) {
            try {
                deleteAnimalStatement.setString(1, animalName);
                int rowsAffected = deleteAnimalStatement.executeUpdate();
                return rowsAffected > 0 ? "Animal deleted successfully." : "Animal not found.";
            } catch (SQLException e) {
                e.printStackTrace();
                return "Error occurred during delete.";
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerApplication::new);
    }
}