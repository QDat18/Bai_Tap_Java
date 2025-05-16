package ui;

// Remove import dao.ACCDAO;
import dao.NhanVienDAO; // Import NhanVienDAO instead
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
// Remove import model.ACC;
import model.NhanVien; // Import model NhanVien instead

public class LoginDialog extends JDialog {

    // Define colors (reusing the palette)
     Color coffeeBrown = new Color(102, 51, 0);
     Color lightBeige = new Color(245, 245, 220);
     Color accentGreen = new Color(60, 179, 113);
     Color darkGray = new Color(50, 50, 50);
     Color linkColor = new Color(0, 102, 204); // Color for links

    // UI Components
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnCancel;
    private JButton btnRegister; // Button for Registration
    private JButton btnForgotPassword; // Button for Forgot Password (or JLabel link)

    // Data Access Object - Change from ACCDAO to NhanVienDAO
    private NhanVienDAO nhanVienDAO;

    // Logged-in user object - Change from ACC to NhanVien
    private NhanVien loggedInUser; // Changed from loggedInAccount

    public LoginDialog(Frame owner) { // Constructor vẫn có thể nhận Frame owner nếu cần khi gọi từ MainFrame
        super(owner, "Đăng nhập Hệ thống", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Initialize DAO - Use NhanVienDAO
        nhanVienDAO = new NhanVienDAO(); // Changed from accDAO = new ACCDAO();

        // --- Content Pane ---
        JPanel contentPane = new JPanel(new GridBagLayout());
        contentPane.setBackground(lightBeige);
        contentPane.setBorder(new EmptyBorder(30, 40, 30, 40)); // Tăng padding từ 20 lên 30-40

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); // Tăng khoảng cách giữa các thành phần từ 10 lên 15
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: Title Label
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24)); // Tăng font size từ 18 lên 24
        lblTitle.setForeground(coffeeBrown);
        contentPane.add(lblTitle, gbc);
        gbc.gridwidth = 1;

        // Row 1: Username
        gbc.gridx = 0; gbc.gridy = 1; contentPane.add(createLabel("Tên đăng nhập:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        txtUsername = new JTextField(20); // Tăng độ rộng từ 15 lên 20
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 14)); // Tăng font chữ input
        txtUsername.setPreferredSize(new Dimension(250, 30)); // Thiết lập kích thước cố định lớn hơn
        contentPane.add(txtUsername, gbc);

        // Row 2: Password
        gbc.gridx = 0; gbc.gridy = 2; contentPane.add(createLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        txtPassword = new JPasswordField(20); // Tăng độ rộng từ 15 lên 20
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14)); // Tăng font chữ input
        txtPassword.setPreferredSize(new Dimension(250, 30)); // Thiết lập kích thước cố định lớn hơn
        contentPane.add(txtPassword, gbc);

        // Row 3: Action Buttons Panel (Login/Cancel)
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionButtonPanel.setBackground(lightBeige);

        btnLogin = new JButton("Đăng nhập");
        styleButton(btnLogin, accentGreen, Color.WHITE);
        actionButtonPanel.add(btnLogin);

        btnCancel = new JButton("Hủy");
        styleButton(btnCancel, darkGray, Color.WHITE);
        actionButtonPanel.add(btnCancel);

        contentPane.add(actionButtonPanel, gbc);

        // Row 4: Register and Forgot Password Links/Buttons
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        JPanel linksPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0)); // Spacing between links
        linksPanel.setBackground(lightBeige);

        // Use JButtons styled like links
        btnRegister = new JButton("Đăng ký tài khoản");
        styleLinkButton(btnRegister); // Use helper for link styling
        linksPanel.add(btnRegister);

        btnForgotPassword = new JButton("Quên mật khẩu?");
        styleLinkButton(btnForgotPassword); // Use helper for link styling
        linksPanel.add(btnForgotPassword);

        contentPane.add(linksPanel, gbc);

        // --- Event Listeners ---
        btnLogin.addActionListener(e -> performLogin());
        btnCancel.addActionListener(e -> cancelLogin());
        txtPassword.addActionListener(e -> performLogin()); // Enter key on password field

        // Action listeners for new buttons
        btnRegister.addActionListener(e -> openRegistrationDialog());
        btnForgotPassword.addActionListener(e -> openForgotPasswordDialog());

        // Add the content pane to the dialog
        getContentPane().add(contentPane);

        pack();
        setMinimumSize(new Dimension(450, 350)); // Thiết lập kích thước tối thiểu
        setLocationRelativeTo(owner); // Center relative to owner (or screen if owner is null)
        setResizable(false);
    }

    // Helper method to create styled labels
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.BOLD, 16)); // Tăng font từ 12 lên 16 và làm đậm
        return label;
    }

    // Helper method to style regular buttons
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                             BorderFactory.createLineBorder(fgColor, 1),
                             BorderFactory.createEmptyBorder(8, 25, 8, 25))); // Tăng padding button
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setFont(new Font("Arial", Font.BOLD, 16)); // Tăng font từ 12 lên 16
        button.setPreferredSize(new Dimension(150, 40)); // Thiết lập kích thước cố định cho button
    }

    // Helper method to style buttons like links
    private void styleLinkButton(JButton button) {
        button.setBackground(lightBeige); // Match background
        button.setForeground(linkColor); // Use link color
        button.setBorderPainted(false); // No border
        button.setContentAreaFilled(false); // No fill color
        button.setFocusPainted(false); // No focus border
        button.setOpaque(false); // Make it transparent
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Change cursor on hover
        button.setFont(new Font("Arial", Font.PLAIN, 14)); // Tăng font cho link

        // Add underline for links
        button.setText("<html><u>" + button.getText() + "</u></html>");
    }

    // --- Login Logic ---
    private void performLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu.",
                "Lỗi đăng nhập", JOptionPane.WARNING_MESSAGE);
             // Clear sensitive data from password field memory
            java.util.Arrays.fill(txtPassword.getPassword(), ' ');
            return;
        }

        // TODO: In a real application, hash the password before sending to DAO
        // Call authenticate method from NhanVienDAO
        loggedInUser = nhanVienDAO.authenticate(username, password); // Changed DAO call and variable

        if (loggedInUser != null) { // Check if NhanVien object was returned
            // Login successful
             String displayName = loggedInUser.getTenNV(); // Use TenNV as display name
             if (displayName == null || displayName.isEmpty()) {
                 displayName = loggedInUser.getTendangnhap(); // Fallback to username
             }
            JOptionPane.showMessageDialog(this, "Đăng nhập thành công! Xin chào, " + displayName,
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
             // Clear sensitive data from password field memory
             java.util.Arrays.fill(txtPassword.getPassword(), ' ');
            dispose(); // Close the login dialog
        } else {
            // Login failed
            JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc Mật khẩu không chính xác.",
                "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            txtPassword.setText("");
             // Clear sensitive data from password field memory
             java.util.Arrays.fill(txtPassword.getPassword(), ' ');
            txtUsername.requestFocusInWindow();
        }
    }

    private void cancelLogin() {
        loggedInUser = null; // Set the user object to null
         // Clear sensitive data from password field memory
         java.util.Arrays.fill(txtPassword.getPassword(), ' ');
        dispose();
    }

    // --- Method to get the logged-in USER (NhanVien) after dialog is closed ---
    public NhanVien getLoggedInUser() { // Changed return type and method name
        return loggedInUser;
    }

    // --- Action Methods for Features ---
    private void openRegistrationDialog() {
        // Pass the LoginDialog itself (this) as the owner
        RegistrationDialog registrationDialog = new RegistrationDialog(this); // Pass 'this' (LoginDialog)
        registrationDialog.setVisible(true);

        // This check runs after RegistrationDialog is closed
        if (registrationDialog.isRegistrationSuccessful()) {
            // Optionally pre-fill username or just inform
            JOptionPane.showMessageDialog(this,
                "Tài khoản đã được tạo thành công. Bạn có thể đăng nhập ngay bây giờ.",
                "Đăng ký thành công", JOptionPane.INFORMATION_MESSAGE);
            // You might want to clear password fields here after successful registration
            txtUsername.setText(registrationDialog.getRegisteredUsername()); // Optionally pre-fill username
            txtPassword.setText("");
        }
    }

    private void openForgotPasswordDialog() {
        // Pass the LoginDialog itself (this) as the owner
        ForgotPasswordDialog forgotPasswordDialog = new ForgotPasswordDialog(this); // Pass 'this' (LoginDialog)
        forgotPasswordDialog.setVisible(true);

        // This code runs after ForgotPasswordDialog is closed
        // No action needed in LoginDialog based on ForgotPasswordDialog result here usually
    }


    // --- Main method: Application Entry Point ---
    // This is the starting point of your application
    // (Should be in MainApplicationFrame's main, but included here for standalone testing of LoginDialog)
     public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> {
             // Create a dummy owner frame for testing the dialog
             JFrame ownerFrame = new JFrame("Login Dialog Test Owner");
             ownerFrame.setSize(300, 200);
             ownerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             // ownerFrame.setVisible(true); // Keep it invisible if you only want to see the dialog

             LoginDialog loginDialog = new LoginDialog(ownerFrame); // Pass the dummy owner frame
             loginDialog.setVisible(true);

             // This code runs after the loginDialog is closed
             // Changed from ACC account to NhanVien user and getLoggedInAccount() to getLoggedInUser()
             NhanVien user = loginDialog.getLoggedInUser();
             if (user != null) {
                 System.out.println("Test login successful: " + user.getTendangnhap()); // Use Tendangnhap
                 // Normally, you would then open the MainApplicationFrame here
                 // MainApplicationFrame mainFrame = new MainApplicationFrame(user); // Pass the NhanVien user object
                 // mainFrame.setVisible(true);
             } else {
                 System.out.println("Test login canceled or failed.");
             }
              // ownerFrame.dispose(); // Dispose the dummy frame
              System.exit(0); // Exit the test application
         });
     }
}
