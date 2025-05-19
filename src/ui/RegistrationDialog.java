package ui;

import model.NhanVien;
import dao.NhanVienDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Arrays;

public class RegistrationDialog extends JDialog {

    Color coffeeBrown = new Color(102, 51, 0);
    Color lightBeige = new Color(245, 245, 220);
    Color accentGreen = new Color(60, 179, 113);
    Color darkGray = new Color(50, 50, 50);
    Color linkColor = new Color(0, 102, 204);

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JTextField txtEmail;
    private JTextField txtDisplayName;
    private JTextField txtAddress;
    private JComboBox<String> cbGender;
    private JTextField txtPhone;
    private JButton btnRegister;
    private JButton btnCancel;

    private NhanVienDAO nhanVienDAO;
    private boolean registrationSuccessful;
    private String registeredUsername;

    public RegistrationDialog(Window owner) {
        super(owner, "Đăng ký tài khoản", Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        nhanVienDAO = new NhanVienDAO();
        registrationSuccessful = false;

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(lightBeige);

        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        inputPanel.setBackground(lightBeige);

        // Tên đăng nhập
        inputPanel.add(new JLabel("Tên đăng nhập:", JLabel.RIGHT));
        txtUsername = new JTextField();
        inputPanel.add(txtUsername);

        // Mật khẩu
        inputPanel.add(new JLabel("Mật khẩu:", JLabel.RIGHT));
        txtPassword = new JPasswordField();
        inputPanel.add(txtPassword);

        // Xác nhận mật khẩu
        inputPanel.add(new JLabel("Xác nhận mật khẩu:", JLabel.RIGHT));
        txtConfirmPassword = new JPasswordField();
        inputPanel.add(txtConfirmPassword);

        // Email
        inputPanel.add(new JLabel("Email:", JLabel.RIGHT));
        txtEmail = new JTextField();
        inputPanel.add(txtEmail);

        // Tên hiển thị
        inputPanel.add(new JLabel("Tên hiển thị:", JLabel.RIGHT));
        txtDisplayName = new JTextField();
        inputPanel.add(txtDisplayName);

        // Địa chỉ
        inputPanel.add(new JLabel("Địa chỉ:", JLabel.RIGHT));
        txtAddress = new JTextField();
        inputPanel.add(txtAddress);

        // Giới tính
        inputPanel.add(new JLabel("Giới tính:", JLabel.RIGHT));
        cbGender = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        inputPanel.add(cbGender);

        // Số điện thoại
        inputPanel.add(new JLabel("Số điện thoại:", JLabel.RIGHT));
        txtPhone = new JTextField();
        inputPanel.add(txtPhone);

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(lightBeige);

        btnRegister = new JButton("Đăng ký");
        styleButton(btnRegister, accentGreen, Color.WHITE);
        btnRegister.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                registerUser();
            }
        });
        buttonPanel.add(btnRegister);

        btnCancel = new JButton("Hủy");
        styleButton(btnCancel, darkGray, Color.WHITE);
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelRegistration();
            }
        });
        buttonPanel.add(btnCancel);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

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
        String password = new String(passwordChars);
        String confirmPassword = new String(confirmPasswordChars);
        String email = txtEmail.getText().trim();
        String displayName = txtDisplayName.getText().trim();
        String address = txtAddress.getText().trim();
        String gender = (String) cbGender.getSelectedItem();
        String phone = txtPhone.getText().trim();

        // Validation
        if (username.isEmpty() || passwordChars.length == 0 || confirmPasswordChars.length == 0 || 
            email.isEmpty() || displayName.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ tất cả thông tin.",
                    "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            clearPasswordFields(passwordChars, confirmPasswordChars);
            return;
        }

        if (!isValidUsername(username)) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập phải có độ dài 4-20 ký tự và chỉ chứa chữ cái, số, _ hoặc .",
                    "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtUsername.setText("");
            clearPasswordFields(passwordChars, confirmPasswordChars);
            txtUsername.requestFocusInWindow();
            return;
        }

        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Email không hợp lệ. Vui lòng nhập đúng định dạng (ví dụ: example@domain.com).",
                    "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtEmail.setText("");
            clearPasswordFields(passwordChars, confirmPasswordChars);
            txtEmail.requestFocusInWindow();
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu và xác nhận mật khẩu không khớp.",
                    "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtPassword.setText("");
            txtConfirmPassword.setText("");
            clearPasswordFields(passwordChars, confirmPasswordChars);
            txtPassword.requestFocusInWindow();
            return;
        }

        if (!isValidPassword(password)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ cái in hoa, chữ cái thường và số.",
                    "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtPassword.setText("");
            txtConfirmPassword.setText("");
            clearPasswordFields(passwordChars, confirmPasswordChars);
            txtPassword.requestFocusInWindow();
            return;
        }

        if (!isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "Số điện thoại phải là dãy 10-11 số (chỉ chứa số).",
                    "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            txtPhone.setText("");
            clearPasswordFields(passwordChars, confirmPasswordChars);
            txtPhone.requestFocusInWindow();
            return;
        }

        if (nhanVienDAO.isUsernameExists(username)) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập '" + username + "' đã tồn tại.",
                    "Lỗi đăng ký", JOptionPane.WARNING_MESSAGE);
            txtUsername.setText("");
            clearPasswordFields(passwordChars, confirmPasswordChars);
            return;
        }

        if (nhanVienDAO.isEmailExists(email)) {
            JOptionPane.showMessageDialog(this, "Email '" + email + "' đã được sử dụng.",
                    "Lỗi đăng ký", JOptionPane.WARNING_MESSAGE);
            txtEmail.setText("");
            clearPasswordFields(passwordChars, confirmPasswordChars);
            return;
        }

        NhanVien newNhanVien = new NhanVien();
        newNhanVien.setTenNV(displayName);
        newNhanVien.setDiachi(address);
        newNhanVien.setGioitinh(gender);
        newNhanVien.setSDT(phone);
        newNhanVien.setTendangnhap(username);
        newNhanVien.setMatkhau(password);
        newNhanVien.setEmail(email);
        newNhanVien.setRole("Staff");

        try {
            boolean success = nhanVienDAO.addNhanVien(newNhanVien);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Đăng ký nhân viên và tài khoản thành công! Bạn có thể đăng nhập.",
                        "Đăng ký thành công", JOptionPane.INFORMATION_MESSAGE);
                registrationSuccessful = true;
                registeredUsername = username;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Có lỗi xảy ra khi đăng ký. Vui lòng thử lại hoặc kiểm tra log.",
                        "Lỗi đăng ký", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 515) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi: Không thể thêm nhân viên do thiếu mã nhân viên (MaNV). Vui lòng kiểm tra cấu hình cơ sở dữ liệu.",
                        "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Lỗi CSDL: " + ex.getMessage(),
                        "Lỗi đăng ký", JOptionPane.ERROR_MESSAGE);
            }
            ex.printStackTrace();
        } finally {
            clearPasswordFields(passwordChars, confirmPasswordChars);
        }
    }

    private void cancelRegistration() {
        registrationSuccessful = false;
        registeredUsername = null;
        clearPasswordFields(txtPassword.getPassword(), txtConfirmPassword.getPassword());
        dispose();
    }

    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }

    public String getRegisteredUsername() {
        return registeredUsername;
    }

    private boolean isValidUsername(String username) {
        String usernameRegex = "^[a-zA-Z0-9_.]{4,20}$";
        return username.matches(usernameRegex);
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private boolean isValidPassword(String password) {
        if (password.length() < 6) return false;
        boolean hasUpperCase = !password.equals(password.toLowerCase());
        boolean hasLowerCase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        return hasUpperCase && hasLowerCase && hasDigit;
    }

    private boolean isValidPhone(String phone) {
        String phoneRegex = "^[0-9]{10,11}$";
        return phone.matches(phoneRegex);
    }

    private void clearPasswordFields(char[]... fields) {
        for (char[] field : fields) {
            Arrays.fill(field, ' ');
        }
    }

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

            System.exit(0);
        });
    }
}