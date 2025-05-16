package ui;


import model.NhanVien;

import dao.NhanVienDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays; // For clearing password char array

// Import cho password hashing (RẤT QUAN TRỌNG cho bảo mật)
// Bạn cần thêm một thư viện hashing như BCrypt hoặc Argon2 và lớp tiện ích tương ứng
// import utils.PasswordHasher; // Ví dụ: import lớp PasswordHasher của bạn


public class RegistrationDialog extends JDialog {

     // Define colors (reusing the palette)
     Color coffeeBrown = new Color(102, 51, 0);
     Color lightBeige = new Color(245, 245, 220);
     Color accentGreen = new Color(60, 179, 113); // Color for Register button
     Color darkGray = new Color(50, 50, 50);
     Color linkColor = new Color(0, 102, 204);

    // UI Components (Input Fields)
     private JTextField txtUsername; // maps to tendangnhap
     private JPasswordField txtPassword; // maps to matkhau
     private JPasswordField txtConfirmPassword;
     private JTextField txtEmail; // maps to email
     private JTextField txtDisplayName; // maps to TenNV (assuming display name is employee name)
    // Loại bỏ JTextField txtPosition; // maps to Chucvu
     // TODO: Add fields for Diachi, Gioitinh, SDT if registration includes full employee details
     // private JTextField txtAddress;
     // private JComboBox<String> cbGender; // Use JComboBox for gender
     // private JTextField txtPhone;


     private JButton btnRegister;
     private JButton btnCancel;

    // Data Access Object - Change from ACCDAO to NhanVienDAO
    private NhanVienDAO nhanVienDAO;
    private boolean registrationSuccessful;
     private String registeredUsername; // To pass back the registered username if successful


    public RegistrationDialog(Window owner) { // Changed owner type to Window
        super(owner, "Đăng ký tài khoản", Dialog.ModalityType.APPLICATION_MODAL); // Added ModalityType
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Initialize DAO - Initialize NhanVienDAO
        nhanVienDAO = new NhanVienDAO();
        registrationSuccessful = false;

        // Set up the UI (similar to your existing code)
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); // Adjusted padding
        mainPanel.setBackground(lightBeige);

        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // 2 columns, gap
        inputPanel.setBackground(lightBeige);

        // Add input fields (ensure names match the NhanVien properties you want to set)
        inputPanel.add(new JLabel("Tên đăng nhập:", JLabel.RIGHT));
        txtUsername = new JTextField();
        inputPanel.add(txtUsername);

        inputPanel.add(new JLabel("Mật khẩu:", JLabel.RIGHT));
        txtPassword = new JPasswordField();
        inputPanel.add(txtPassword);

        inputPanel.add(new JLabel("Xác nhận mật khẩu:", JLabel.RIGHT));
        txtConfirmPassword = new JPasswordField();
        inputPanel.add(txtConfirmPassword);

        inputPanel.add(new JLabel("Email:", JLabel.RIGHT));
        txtEmail = new JTextField();
        inputPanel.add(txtEmail);

        inputPanel.add(new JLabel("Tên hiển thị:", JLabel.RIGHT)); // Mapear cho TenNV
        txtDisplayName = new JTextField();
        inputPanel.add(txtDisplayName);

        // Loại bỏ thêm txtPosition vào inputPanel
        // inputPanel.add(new JLabel("Chức vụ:", JLabel.RIGHT)); // Mapear cho Chucvu
        // txtPosition = new JTextField(); // Or JComboBox if limited positions
        // inputPanel.add(txtPosition);

         // TODO: Add input fields for Diachi, Gioitinh, SDT if they are required for registration
         // inputPanel.add(new JLabel("Địa chỉ:", JLabel.RIGHT));
         // txtAddress = new JTextField();
         // inputPanel.add(txtAddress);

         // inputPanel.add(new JLabel("Giới tính:", JLabel.RIGHT));
         // cbGender = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"}); // Example JComboBox
         // inputPanel.add(cbGender);

         // inputPanel.add(new JLabel("SĐT:", JLabel.RIGHT));
         // txtPhone = new JTextField();
         // inputPanel.add(txtPhone);


        mainPanel.add(inputPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(lightBeige);

        btnRegister = new JButton("Đăng ký");
        styleButton(btnRegister, accentGreen, Color.WHITE); // Use accent green for Add/Register
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser(); // Call the registration logic
            }
        });
        buttonPanel.add(btnRegister);

        btnCancel = new JButton("Hủy");
        styleButton(btnCancel, darkGray, Color.WHITE);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelRegistration(); // Close the dialog
            }
        });
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack(); // Adjusts window size to fit components
        setLocationRelativeTo(owner); // Center relative to owner frame
         setResizable(false); // Optional: Prevent resizing
    }

    // Helper method to style buttons (can be shared)
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                             BorderFactory.createLineBorder(fgColor, 1),
                             BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setFont(new Font("Arial", Font.BOLD, 12));
    }


    private void registerUser() {
        String username = txtUsername.getText().trim();
        char[] passwordChars = txtPassword.getPassword();
        char[] confirmPasswordChars = txtConfirmPassword.getPassword();
        String email = txtEmail.getText().trim();
        String displayName = txtDisplayName.getText().trim(); // Assuming this maps to TenNV
        // Loại bỏ String position = txtPosition.getText().trim(); // Assuming this maps to Chucvu

        // TODO: Get values from other fields if added (Diachi, Gioitinh, SDT)
        // String address = txtAddress.getText().trim();
        // String gender = (String) cbGender.getSelectedItem(); // Example for JComboBox
        // String phone = txtPhone.getText().trim();


        // Basic validation
        if (username.isEmpty() || passwordChars.length == 0 || confirmPasswordChars.length == 0 || email.isEmpty() || displayName.isEmpty() ) { // Removed position check
             JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin (Tên đăng nhập, Mật khẩu, Xác nhận mật khẩu, Email, Tên hiển thị).", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
             // Clear sensitive data from memory
             Arrays.fill(passwordChars, ' ');
             Arrays.fill(confirmPasswordChars, ' ');
            return;
        }
        if (!Arrays.equals(passwordChars, confirmPasswordChars)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu và xác nhận mật khẩu không khớp.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtPassword.setText("");
            txtConfirmPassword.setText("");
             // Clear sensitive data from memory
             Arrays.fill(passwordChars, ' ');
             Arrays.fill(confirmPasswordChars, ' ');
            return;
        }

        // TODO: Add more validation (email format, phone format, etc.)

        // --- Check if username or email already exists ---
         if (nhanVienDAO.isUsernameExists(username)) {
              JOptionPane.showMessageDialog(this, "Tên đăng nhập '" + username + "' đã tồn tại.", "Lỗi đăng ký", JOptionPane.WARNING_MESSAGE);
               // Clear sensitive data from memory
              Arrays.fill(passwordChars, ' ');
              Arrays.fill(confirmPasswordChars, ' ');
              return;
         }
          if (nhanVienDAO.isEmailExists(email)) {
              JOptionPane.showMessageDialog(this, "Email '" + email + "' đã được sử dụng.", "Lỗi đăng ký", JOptionPane.WARNING_MESSAGE);
               // Clear sensitive data from memory
              Arrays.fill(passwordChars, ' ');
              Arrays.fill(confirmPasswordChars, ' ');
              return;
         }


        // Create a NhanVien object with the collected data
        NhanVien newNhanVien = new NhanVien();
        // MaNV will be generated by DAO
        newNhanVien.setTenNV(displayName); // Set employee name
        // TODO: Set Diachi, Gioitinh, SDT if added to UI
        // newNhanVien.setDiachi(address);
        // newNhanVien.setGioitinh(gender);
        // newNhanVien.setSDT(phone);

        // Set account details
        newNhanVien.setTendangnhap(username);
        // TODO: Hash the password before setting it in the object!
        // String hashedPassword = PasswordHasher.hashPassword(new String(passwordChars));
        // newNhanVien.setMatkhau(hashedPassword);
        newNhanVien.setMatkhau(new String(passwordChars)); // TEMPORARY: Storing plain text or pre-hashed


        newNhanVien.setEmail(email);
        // Loại bỏ newNhanVien.setChucvu(position);
        // TODO: Set a default role or select role in UI if needed
        newNhanVien.setRole("Staff"); // Example: Default role is Staff


        // Add the new NhanVien to the database using NhanVienDAO
        try {
            boolean success = nhanVienDAO.addNhanVien(newNhanVien);

            if (success) {
                 JOptionPane.showMessageDialog(this,
                     "Đăng ký nhân viên và tài khoản thành công! Bạn có thể đăng nhập.",
                     "Đăng ký thành công", JOptionPane.INFORMATION_MESSAGE);

                registrationSuccessful = true;
                registeredUsername = username; // Store username to pass back
                dispose();
            } else {
                // addNhanVien already prints error to console, but maybe show a generic UI error
                JOptionPane.showMessageDialog(this,
                    "Có lỗi xảy ra khi đăng ký. Vui lòng kiểm tra log hoặc thử lại.",
                    "Lỗi đăng ký", JOptionPane.ERROR_MESSAGE);
                // If DAO returns false, it might be a unique constraint violation (username/email exists)
                // You might want to add more specific error handling based on DAO return/exceptions
            }


        } catch (Exception ex) { // Catching generic exception, you might catch SQLException specifically
            JOptionPane.showMessageDialog(this,
                "Có lỗi xảy ra khi đăng ký: " + ex.getMessage(),
                "Lỗi đăng ký", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
             // Always clear sensitive data from memory
             Arrays.fill(passwordChars, ' ');
             Arrays.fill(confirmPasswordChars, ' ');
        }
    }


    private void cancelRegistration() {
        registrationSuccessful = false; // Ensure this is false on cancel
         registeredUsername = null; // Clear username on cancel
         // Clear sensitive data from password field memory
         Arrays.fill(txtPassword.getPassword(), ' ');
         Arrays.fill(txtConfirmPassword.getPassword(), ' ');
        dispose();
    }

    // Method to check if registration was successful
    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }

     // Method to get the registered username (useful for pre-filling login form)
     public String getRegisteredUsername() {
         return registeredUsername;
     }


    // For testing - Should be commented out or removed in production

     public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> {
             JFrame frame = new JFrame("Test Owner Frame");
             frame.setSize(800, 600);
             frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

             RegistrationDialog dialog = new RegistrationDialog(frame);
             dialog.setVisible(true);

             if (dialog.isRegistrationSuccessful()) {
                 System.out.println("Registration successful for user: " + dialog.getRegisteredUsername());
             } else {
                 System.out.println("Registration canceled or failed");
             }

              // frame.dispose(); // Dispose the test frame if not exiting
              System.exit(0); // Exit the test application
         });
     }

}