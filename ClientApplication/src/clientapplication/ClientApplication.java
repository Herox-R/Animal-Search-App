/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package clientapplication;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.regex.*;
/**
 *
 * @author Sylvester N
 */


public class ClientApplication extends JFrame {
    private JTextField searchField;
    private JButton searchButton;
    private JTextArea resultArea;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientApplication() {
        setTitle("Animal Search");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Search panel
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout());
        JLabel searchLabel = new JLabel("Enter animal name or species: ");
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        searchButton.addActionListener(new SearchButtonListener());
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // Result area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        add(scrollPane, BorderLayout.CENTER);

        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(new ExitMenuItemListener());
        fileMenu.add(exitItem);

        JMenu adminMenu = new JMenu("Admin");
        JMenuItem adminItem = new JMenuItem("Open Admin GUI");
        adminItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new AdminGUI();
            }
        });
        adminMenu.add(adminItem);

        menuBar.add(fileMenu);
        menuBar.add(adminMenu);
        setJMenuBar(menuBar);

        setVisible(true);

        // Connect to server
        try {
            socket = new Socket("localhost", 12345); // Change to your server's IP address and port
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class SearchButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String searchText = searchField.getText().trim();
            if (searchText.isEmpty()) {
                JOptionPane.showMessageDialog(ClientApplication.this, "Please enter a search term.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            out.println("SEARCH:" + searchText); // Send search request to server
            try {
                String response = in.readLine(); // Receive response from server
                if (response != null && !response.isEmpty()) {
                    resultArea.setText(response); // Display response in resultArea
                } else {
                    resultArea.setText("No results found.");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class ExitMenuItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int option = JOptionPane.showConfirmDialog(ClientApplication.this, "Minimize, Exit, or Cancel?", "Exit", JOptionPane.YES_NO_CANCEL_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                // Minimize
                setExtendedState(JFrame.ICONIFIED);
            } else if (option == JOptionPane.NO_OPTION) {
                // Exit
                try {
                    out.close();
                    in.close();
                    socket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
            // If option is CANCEL_OPTION, do nothing
        }
    }

    // Inner class for administrator GUI
    private class AdminGUI extends JFrame {
        private JTextField animalNameField;
        private JTextField speciesField;
        private JButton insertButton;
        private JButton deleteButton;

        public AdminGUI() {
            setTitle("Administrator Interface");
            setSize(400, 200);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLayout(new GridLayout(3, 2));

            JLabel animalNameLabel = new JLabel("Animal Name: ");
            animalNameField = new JTextField();
            add(animalNameLabel);
            add(animalNameField);

            JLabel speciesLabel = new JLabel("Species: ");
            speciesField = new JTextField();
            add(speciesLabel);
            add(speciesField);

            insertButton = new JButton("Insert");
            insertButton.addActionListener(new InsertButtonListener());
            add(insertButton);

            deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new DeleteButtonListener());
            add(deleteButton);

            setVisible(true);
        }

        // Inner class for insert button action
private class InsertButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        String animalName = animalNameField.getText().trim();
        String species = speciesField.getText().trim();

        // Validate data
        if (!isValidInput(animalName, species)) {
            JOptionPane.showMessageDialog(AdminGUI.this, "Invalid input. Please enter valid data.");
            return;
        }

        out.println("INSERT:" + animalName + ":" + species);

        try {
            String response = in.readLine();
            JOptionPane.showMessageDialog(AdminGUI.this, response);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(AdminGUI.this, "Error communicating with server.");
        }
    }
}

        // Inner class for delete button action
        private class DeleteButtonListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                String animalName = animalNameField.getText().trim();
                if (animalName.isEmpty()) {
                    JOptionPane.showMessageDialog(AdminGUI.this, "Please enter an animal name.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                out.println("DELETE:" + animalName);
                try {
                    String response = in.readLine();
                    JOptionPane.showMessageDialog(AdminGUI.this, response);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // Method to validate input data
        private boolean isValidInput(String animalName, String species) {
            // Ensure no numbers in species
            Pattern pattern = Pattern.compile("[0-9]");
            Matcher matcher = pattern.matcher(species);
            return !matcher.find() && !animalName.isEmpty() && !species.isEmpty();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientApplication());
    }
}