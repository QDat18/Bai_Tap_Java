// File: ui/NhaCCDetailsDialog.java
package ui;

import dao.NhaCCDAO;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import model.NhaCC;

public class NhaCCDetailsDialog extends JDialog {

    // Define colors (reusing the palette from NhaCCUI)
    Color coffeeBrown = new Color(102, 51, 0);
    Color lightBeige = new Color(245, 245, 220);
    Color darkGray = new Color(50, 50, 50);
    Color accentBlue = new Color(30, 144, 255); // Color for Close button

    // UI Components
    private JLabel lblMaNCCValue;
    private JLabel lblTenNCCValue;
    private JLabel lblDiaChiValue;
    private JLabel lblSDTValue;
    private JButton btnClose;

    private NhaCCDAO nhaCCDAO;
    private String maNCC; // To fetch details

    public NhaCCDetailsDialog(Frame owner, String maNCC) {
        super(owner, "Chi tiết Nhà Cung Cấp", true); // Modal dialog
        this.maNCC = maNCC;
        this.nhaCCDAO = new NhaCCDAO(); // Initialize DAO

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 300); // Adjusted size
        setLocationRelativeTo(owner); // Center relative to owner frame
        setResizable(false); // Not resizable

        initComponents();
        loadNhaCCDetails(); // Load data when dialog is initialized
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // BorderLayout with some spacing
        setBackground(lightBeige);
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding around the content

        // --- Details Panel ---
        JPanel detailsPanel = new JPanel(new GridBagLayout()); // Use GridBagLayout for organized labels/values
        detailsPanel.setBackground(lightBeige);
        detailsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1),
                                                            "Thông tin chi tiết", // Titled Border
                                                            TitledBorder.LEADING,
                                                            TitledBorder.TOP,
                                                            new Font("Arial", Font.BOLD, 14),
                                                            darkGray));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Padding around components
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left

        Font labelFont = new Font("Arial", Font.BOLD, 13); // Font for field names
        Font valueFont = new Font("Arial", Font.PLAIN, 13); // Font for field values


        // Row 1: Mã NCC
        gbc.gridx = 0; gbc.gridy = 0; detailsPanel.add(createStyledLabel("Mã NCC:", labelFont), gbc);
        gbc.gridx = 1; gbc.gridy = 0; lblMaNCCValue = createStyledLabel("", valueFont); detailsPanel.add(lblMaNCCValue, gbc);

        // Row 2: Tên NCC
        gbc.gridx = 0; gbc.gridy = 1; detailsPanel.add(createStyledLabel("Tên NCC:", labelFont), gbc);
        gbc.gridx = 1; gbc.gridy = 1; lblTenNCCValue = createStyledLabel("", valueFont); detailsPanel.add(lblTenNCCValue, gbc);

        // Row 3: Địa chỉ
        gbc.gridx = 0; gbc.gridy = 2; detailsPanel.add(createStyledLabel("Địa chỉ:", labelFont), gbc);
        gbc.gridx = 1; gbc.gridy = 2; lblDiaChiValue = createStyledLabel("", valueFont); detailsPanel.add(lblDiaChiValue, gbc);

        // Row 4: SĐT
        gbc.gridx = 0; gbc.gridy = 3; detailsPanel.add(createStyledLabel("SĐT:", labelFont), gbc);
        gbc.gridx = 1; gbc.gridy = 3; lblSDTValue = createStyledLabel("", valueFont); detailsPanel.add(lblSDTValue, gbc);

        add(detailsPanel, BorderLayout.CENTER); // Add details panel to the center of the dialog

        // --- Button Panel (Bottom) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5)); // Align button to the right
        buttonPanel.setBackground(lightBeige);

        btnClose = new JButton("Đóng");
        styleButton(btnClose, accentBlue, Color.WHITE);
        btnClose.addActionListener(e -> dispose()); // Close the dialog when button is clicked
        buttonPanel.add(btnClose);

        add(buttonPanel, BorderLayout.SOUTH); // Add button panel to the bottom of the dialog
    }

    // Method to load employee details from database and display them
    private void loadNhaCCDetails() {
        // Assuming NhaCCDAO has a method like getNhaCCById(String maNCC)
        NhaCC nhaCC = nhaCCDAO.getNhaCCByMaNCC(maNCC);

        if (nhaCC != null) {
            // Populate the JLabels with retrieved data (handle nulls for display)
            lblMaNCCValue.setText(nhaCC.getMaNCC());
            lblTenNCCValue.setText(nhaCC.getTenNCC() != null ? nhaCC.getTenNCC() : "N/A");
            lblDiaChiValue.setText(nhaCC.getDiachi() != null ? nhaCC.getDiachi() : "N/A");
            lblSDTValue.setText(nhaCC.getSDT() != null ? nhaCC.getSDT() : "N/A");
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin nhà cung cấp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose(); // Close dialog if no supplier found
        }
    }

    // Helper method to create styled labels for values
    private JLabel createStyledLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(darkGray);
        return label;
    }

    // Helper method to style buttons (copied from NhaCCUI for consistency)
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false); // Remove focus border
        button.setBorder(BorderFactory.createCompoundBorder(
                          BorderFactory.createLineBorder(fgColor, 1), // Thin border around button
                          BorderFactory.createEmptyBorder(5, 15, 5, 15))); // Padding inside button
        button.setOpaque(true); // Ensure background is fully painted
        button.setBorderPainted(true); // Ensure border is painted
        button.setFont(new Font("Arial", Font.BOLD, 12));
    }

     public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> {
             // This test requires a running database with NhaCC data
             // And the NhaCCDAO configured correctly.
             // Replace "NCC001" with a valid MaNCC from your database for testing.
             String testMaNCC = "NCC01"; // Replace with a valid ID from your DB

             JFrame frame = new JFrame("Owner Frame");
             frame.setSize(300, 200);
             frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             frame.setVisible(true);

             // Pass the owner frame and the test MaNCC
             NhaCCDetailsDialog dialog = new NhaCCDetailsDialog(frame, testMaNCC);
             dialog.setVisible(true);

             frame.dispose(); // Dispose owner frame after dialog is closed
         });
     }
}
