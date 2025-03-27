import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

public class RegistrationForm {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/registration_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    private JFrame mainFrame;
    private JTextField nameField, mobileField, dobField;
    private JTextArea addressArea;
    private JRadioButton maleRadio, femaleRadio;
    private JCheckBox termsCheck;
    private JTable recordsTable;
    private DefaultTableModel tableModel;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                new RegistrationForm().initialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void initialize() {
        // Initialize database driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create main application window
        mainFrame = new JFrame("Swing Registration Form");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setLocationRelativeTo(null);

        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // Create registration form panel
        JPanel registrationPanel = createRegistrationPanel();
        
        // Create records view panel
        JPanel recordsPanel = createRecordsPanel();

        // Add tabs to the application
        tabbedPane.addTab("Registration", registrationPanel);
        tabbedPane.addTab("View Records", recordsPanel);

        mainFrame.add(tabbedPane, BorderLayout.CENTER);
        mainFrame.setVisible(true);
    }

    private JPanel createRegistrationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title label
        JLabel titleLabel = new JLabel("Registration Form", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        // Name field
        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        // Mobile field
        formPanel.add(new JLabel("Mobile:"));
        mobileField = new JTextField();
        formPanel.add(mobileField);

        // Gender field
        formPanel.add(new JLabel("Gender:"));
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup genderGroup = new ButtonGroup();
        maleRadio = new JRadioButton("Male");
        femaleRadio = new JRadioButton("Female");
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        formPanel.add(genderPanel);

        // Date of Birth field
        formPanel.add(new JLabel("Date of Birth:"));
        dobField = new JTextField();
        dobField.setToolTipText("YYYY-MM-DD");
        formPanel.add(dobField);

        // Address field
        formPanel.add(new JLabel("Address:"));
        addressArea = new JTextArea(3, 20);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        formPanel.add(addressScroll);

        // Terms checkbox
        formPanel.add(new JLabel()); // Empty cell for alignment
        termsCheck = new JCheckBox("Accept Terms And Conditions.");
        formPanel.add(termsCheck);

        panel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton submitButton = new JButton("Submit");
        submitButton.addActionListener(e -> submitRegistration());
        buttonPanel.add(submitButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetForm());
        buttonPanel.add(resetButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRecordsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Title label
        JLabel titleLabel = new JLabel("Registered Users", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Table setup
        String[] columnNames = {"ID", "Name", "Mobile", "Gender", "Date of Birth", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0);
        recordsTable = new JTable(tableModel);
        recordsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane scrollPane = new JScrollPane(recordsTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton loadButton = new JButton("Load Records");
        loadButton.addActionListener(e -> loadRecords());
        buttonPanel.add(loadButton);

        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadRecords());
        buttonPanel.add(refreshButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void submitRegistration() {
        if (!termsCheck.isSelected()) {
            JOptionPane.showMessageDialog(mainFrame, "Please accept the terms and conditions.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = nameField.getText().trim();
        String mobile = mobileField.getText().trim();
        String gender = maleRadio.isSelected() ? "Male" : femaleRadio.isSelected() ? "Female" : "";
        String dob = dobField.getText().trim();
        String address = addressArea.getText().trim();

        if (name.isEmpty() || mobile.isEmpty() || gender.isEmpty() || dob.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "Please fill all fields.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO users (name, mobile, gender, dob, address) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setString(1, name);
            pstmt.setString(2, mobile);
            pstmt.setString(3, gender);
            pstmt.setDate(4, Date.valueOf(dob));
            pstmt.setString(5, address);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    JOptionPane.showMessageDialog(mainFrame, 
                        "Registration successful! Your ID: " + id, 
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    resetForm();
                    loadRecords(); // Refresh the records view
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(mainFrame, "Database error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(mainFrame, "Invalid date format. Please use YYYY-MM-DD", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        nameField.setText("");
        mobileField.setText("");
        maleRadio.setSelected(false);
        femaleRadio.setSelected(false);
        dobField.setText("");
        addressArea.setText("");
        termsCheck.setSelected(false);
    }

    private void loadRecords() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            tableModel.setRowCount(0); // Clear existing data
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, name, mobile, gender, dob, address FROM users ORDER BY id");
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("mobile"),
                    rs.getString("gender"),
                    rs.getDate("dob").toString(),
                    rs.getString("address")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(mainFrame, "Database error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}