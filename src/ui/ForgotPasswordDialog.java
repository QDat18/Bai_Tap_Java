package ui;

// REMOVE THIS INCORRECT IMPORT: import com.sun.jdi.connect.Transport;
// Remove import dao.ACCDAO;
import dao.NhanVienDAO; // Import NhanVienDAO instead
import java.awt.*;
// REMOVE THIS: import java.net.PasswordAuthentication;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
// Remove import model.ACC;
import model.NhanVien; // Import model NhanVien instead
import java.util.Random; // Để tạo mã OTP
import java.util.logging.Level; // Logging
import java.util.logging.Logger; // Logging
import java.util.Properties; // Cho cấu hình email
import java.util.Arrays; // For clearing password char array

// Import các lớp từ JavaMail API
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

// CẦN THÊM THƯ VIỆN JAVA ACTIVATION FRAMEWORK (JAF) VÀ JAVA MAIL API VÀO PROJECT
// Nếu dùng Maven, thêm dependency:
// <dependency>
//     <groupId>com.sun.mail</groupId> // Hoặc jakarta.mail
//     <artifactId>jakarta.mail-api</artifactId> // Hoặc javax.mail
//     <version>1.6.7</version> // Hoặc phiên bản mới nhất
// </dependency>
// <dependency>
//     <groupId>jakarta.activation</groupId>
//     <artifactId>jakarta.activation-api</artifactId>
//     <version>1.2.2</version> // Hoặc phiên bản mới nhất
// </dependency>
// Nếu dùng Gradle, thêm dependency:
// implementation 'com.sun.mail:jakarta.mail-api:1.6.7' // Hoặc phiên bản mới nhất
// implementation 'jakarta.activation:jakarta.activation-api:1.2.2' // Hoặc phiên bản mới nhất
// Nếu không dùng, tải JAR và thêm vào Build Path.


public class ForgotPasswordDialog extends JDialog {

    // Define colors (reusing the palette)
    Color coffeeBrown = new Color(102, 51, 0);
    Color lightBeige = new Color(245, 245, 220);
    Color accentGreen = new Color(60, 179, 113);
    Color darkGray = new Color(50, 50, 50);
    Color linkColor = new Color(0, 102, 204); // Không dùng trong phiên bản này, nhưng giữ lại

    // UI Components
    // Step 1 Components
    private JTextField txtUsernameStep1;
    private JTextField txtEmailStep1;
    private JButton btnSendOtp;
    private JButton btnCancelStep1;

    // Step 2 Components
    private JTextField txtOtp; // Trường nhập mã OTP
    private JPasswordField txtNewPasswordStep2;
    private JPasswordField txtConfirmPasswordStep2;
    private JButton btnResetPasswordStep2;
    private JButton btnCancelStep2; // Nút hủy cho bước 2

    // Panel và Layout cho các bước
    private CardLayout cardLayout;
    private JPanel cardPanel; // Panel chứa các panel bước (Step 1 và Step 2)
    private JPanel step1Panel; // Panel cho bước 1 (Nhập Username/Email)
    private JPanel step2Panel; // Panel cho bước 2 (Nhập OTP/Mật khẩu mới)

    // Data Access Object - Change from ACCDAO to NhanVienDAO
    private NhanVienDAO nhanVienDAO;

    // Variables for OTP and User
    private String generatedOtp; // Lưu mã OTP đã tạo
    private NhanVien userToReset; // Lưu thông tin nhân viên cần đặt lại mật khẩu (Changed from ACC)

    // Logger
    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordDialog.class.getName());

    // --- Cấu hình Email Gửi OTP ---
    // THAY THẾ CÁC GIÁ TRỊ NÀY BẰNG THÔNG TIN EMAIL CỦA BẠN
    // LƯU Ý: Cần bật tính năng "less secure app access" HOẶC sử dụng "App Passwords"
    // trong cài đặt bảo mật tài khoản Google/email của bạn nếu dùng Gmail.
     private static final String SENDER_EMAIL = "hoangquangdat182005@gmail.com"; // Địa chỉ email gửi
     private static final String SENDER_PASSWORD = "fhcx yrgd cqfw qecz"; // Mật khẩu email gửi (hoặc mật khẩu ứng dụng)
     private static final String SMTP_HOST = "smtp.gmail.com"; // Ví dụ: smtp.gmail.com
     private static final String SMTP_PORT = "587"; // Ví dụ: 587 (TLS) hoặc 465 (SSL)


    // Constructor: Accept Dialog owner
    public ForgotPasswordDialog(Window owner) { // Use Window owner
        super(owner, "Quên mật khẩu", Dialog.ModalityType.APPLICATION_MODAL); // Use ModalityType
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Initialize DAO - Use NhanVienDAO
        nhanVienDAO = new NhanVienDAO(); // Changed from accDAO = new ACCDAO();

        // --- Main Content Panel with CardLayout ---
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout); // Sử dụng CardLayout

        // --- Step 1 Panel (Username/Email Input) ---
        step1Panel = new JPanel(new GridBagLayout());
        step1Panel.setBackground(lightBeige);
        step1Panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.insets = new Insets(10, 10, 10, 10);
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        gbc1.weightx = 1.0;

        // Title Step 1
        gbc1.gridx = 0; gbc1.gridy = 0; gbc1.gridwidth = 2; gbc1.anchor = GridBagConstraints.CENTER;
        JLabel lblTitle1 = new JLabel("XÁC MINH TÀI KHOẢN", SwingConstants.CENTER);
        lblTitle1.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle1.setForeground(coffeeBrown);
        step1Panel.add(lblTitle1, gbc1);

        // Username Step 1
        gbc1.gridx = 0; gbc1.gridy = 1; gbc1.gridwidth = 1; gbc1.anchor = GridBagConstraints.WEST; step1Panel.add(createLabel("Tên đăng nhập:"), gbc1);
        gbc1.gridx = 1; gbc1.gridy = 1; txtUsernameStep1 = new JTextField(20); step1Panel.add(txtUsernameStep1, gbc1);

        // Email Step 1
        gbc1.gridx = 0; gbc1.gridy = 2; step1Panel.add(createLabel("Email đăng ký:"), gbc1);
        gbc1.gridx = 1; gbc1.gridy = 2; txtEmailStep1 = new JTextField(20); step1Panel.add(txtEmailStep1, gbc1);

        // Action Buttons Step 1
        gbc1.gridx = 0; gbc1.gridy = 3; gbc1.gridwidth = 2; gbc1.anchor = GridBagConstraints.CENTER;
        JPanel actionButtonPanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionButtonPanel1.setBackground(lightBeige);

        btnSendOtp = new JButton("Gửi mã OTP");
        styleButton(btnSendOtp, accentGreen, Color.WHITE);
        actionButtonPanel1.add(btnSendOtp);

        btnCancelStep1 = new JButton("Hủy");
        styleButton(btnCancelStep1, darkGray, Color.WHITE);
        btnCancelStep1.addActionListener(e -> dispose()); // Hủy ở bước 1
        actionButtonPanel1.add(btnCancelStep1);

        step1Panel.add(actionButtonPanel1, gbc1);


        // --- Step 2 Panel (OTP/New Password Input) ---
        step2Panel = new JPanel(new GridBagLayout());
        step2Panel.setBackground(lightBeige);
        step2Panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(10, 10, 10, 10);
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.weightx = 1.0;

        // Title Step 2
        gbc2.gridx = 0; gbc2.gridy = 0; gbc2.gridwidth = 2; gbc2.anchor = GridBagConstraints.CENTER;
        JLabel lblTitle2 = new JLabel("NHẬP OTP VÀ MẬT KHẨU MỚI", SwingConstants.CENTER);
        lblTitle2.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle2.setForeground(coffeeBrown);
        step2Panel.add(lblTitle2, gbc2);

        // OTP Input Step 2
        gbc2.gridx = 0; gbc2.gridy = 1; gbc2.gridwidth = 1; gbc2.anchor = GridBagConstraints.WEST; step2Panel.add(createLabel("Mã OTP:"), gbc2);
        gbc2.gridx = 1; gbc2.gridy = 1; txtOtp = new JTextField(10); step2Panel.add(txtOtp, gbc2); // OTP thường ngắn

        // New Password Step 2
        gbc2.gridx = 0; gbc2.gridy = 2; step2Panel.add(createLabel("Mật khẩu mới:"), gbc2);
        gbc2.gridx = 1; gbc2.gridy = 2; txtNewPasswordStep2 = new JPasswordField(20); step2Panel.add(txtNewPasswordStep2, gbc2);

        // Confirm New Password Step 2
        gbc2.gridx = 0; gbc2.gridy = 3; step2Panel.add(createLabel("Xác nhận mật khẩu mới:"), gbc2);
        gbc2.gridx = 1; gbc2.gridy = 3; txtConfirmPasswordStep2 = new JPasswordField(20); step2Panel.add(txtConfirmPasswordStep2, gbc2);

        // Action Buttons Step 2
        gbc2.gridx = 0; gbc2.gridy = 4; gbc2.gridwidth = 2; gbc2.anchor = GridBagConstraints.CENTER;
        JPanel actionButtonPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionButtonPanel2.setBackground(lightBeige);

        btnResetPasswordStep2 = new JButton("Đặt lại mật khẩu");
        styleButton(btnResetPasswordStep2, accentGreen, Color.WHITE);
        actionButtonPanel2.add(btnResetPasswordStep2);

        btnCancelStep2 = new JButton("Hủy");
        styleButton(btnCancelStep2, darkGray, Color.WHITE);
        btnCancelStep2.addActionListener(e -> dispose()); // Hủy ở bước 2
        actionButtonPanel2.add(btnCancelStep2);

        step2Panel.add(actionButtonPanel2, gbc2);


        // Add panels to cardPanel
        cardPanel.add(step1Panel, "Step1");
        cardPanel.add(step2Panel, "Step2");

        // Add cardPanel to the dialog's content pane
        getContentPane().add(cardPanel);


        // --- Event Listeners ---
        btnSendOtp.addActionListener(e -> processSendOtp());

        btnResetPasswordStep2.addActionListener(e -> processResetPassword());


        // Initial display
        cardLayout.show(cardPanel, "Step1"); // Bắt đầu hiển thị Step 1


        pack(); // Điều chỉnh kích thước dialog cho vừa với nội dung
        setLocationRelativeTo(owner); // Center relative to owner dialog/frame
        setResizable(false); // Không cho phép thay đổi kích thước
    }

    // Helper method to create styled labels
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    // Helper method to style buttons
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                             BorderFactory.createLineBorder(fgColor, 1), // Use fgColor for border
                             BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setFont(new Font("Arial", Font.BOLD, 12));
    }


    // --- Process Send OTP Logic (Step 1) ---
    private void processSendOtp() {
        String username = txtUsernameStep1.getText().trim();
        String email = txtEmailStep1.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ tên đăng nhập và email.",
                    "Lỗi xác minh", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1. Verify Username and Email using NhanVienDAO
        userToReset = nhanVienDAO.getNhanVienByTendangnhap(username); // Use NhanVienDAO method

        if (userToReset == null) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập không tồn tại.",
                    "Lỗi xác minh", JOptionPane.ERROR_MESSAGE);
            txtUsernameStep1.requestFocusInWindow();
            return;
        }

        // Check if the email provided matches the email in the database for that user
        if (!userToReset.getEmail().equalsIgnoreCase(email)) {
            JOptionPane.showMessageDialog(this, "Email không khớp với email đã đăng ký cho tài khoản này.",
                    "Lỗi xác minh", JOptionPane.ERROR_MESSAGE);
            txtEmailStep1.requestFocusInWindow();
            return;
        }

        // 2. Generate OTP
        generatedOtp = generateOtp(6); // Tạo mã OTP 6 chữ số

        // 3. Send OTP to Email (Actual Implementation)
        // Sử dụng SwingWorker để gửi email ở background, tránh làm đơ UI
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                // Thực hiện gửi email trong luồng nền
                return sendOtpEmail(userToReset.getEmail(), generatedOtp); // Use userToReset email
            }

            @Override
            protected void done() {
                try {
                    boolean emailSent = get(); // Lấy kết quả từ doInBackground()
                    if (emailSent) {
                        JOptionPane.showMessageDialog(ForgotPasswordDialog.this, // Sử dụng ForgotPasswordDialog.this để tham chiếu đúng dialog
                                "Mã OTP đã được gửi đến email đăng ký của bạn. Vui lòng kiểm tra hộp thư.",
                                "Gửi OTP thành công", JOptionPane.INFORMATION_MESSAGE);

                        // Switch to Step 2
                        cardLayout.show(cardPanel, "Step2");
                        // Reset fields in Step 2
                        txtOtp.setText("");
                        txtNewPasswordStep2.setText("");
                        txtConfirmPasswordStep2.setText("");
                        txtOtp.requestFocusInWindow(); // Focus vào trường nhập OTP

                        // Cập nhật lại kích thước dialog sau khi chuyển panel
                        pack();

                    } else {
                        // sendOtpEmail returned false (config error, connection failed, etc.)
                        // An error message should have been logged by sendOtpEmail
                        JOptionPane.showMessageDialog(ForgotPasswordDialog.this, // Sử dụng ForgotPasswordDialog.this
                                "Không thể gửi mã OTP đến email. Vui lòng kiểm tra lại thông tin hoặc thử lại sau.",
                                "Lỗi gửi OTP", JOptionPane.ERROR_MESSAGE);
                         // Clear sensitive data from password fields in step 2 just in case
                         Arrays.fill(txtNewPasswordStep2.getPassword(), ' ');
                         Arrays.fill(txtConfirmPasswordStep2.getPassword(), ' ');
                        // Không chuyển sang Step 2 nếu gửi email thất bại
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Lỗi trong SwingWorker khi gửi email OTP", ex);
                    JOptionPane.showMessageDialog(ForgotPasswordDialog.this, // Sử dụng ForgotPasswordDialog.this
                                "Đã xảy ra lỗi trong quá trình gửi email: " + ex.getMessage(),
                                "Lỗi gửi OTP", JOptionPane.ERROR_MESSAGE);
                     // Clear sensitive data
                     Arrays.fill(txtNewPasswordStep2.getPassword(), ' ');
                     Arrays.fill(txtConfirmPasswordStep2.getPassword(), ' ');
                }
            }
        }.execute(); // Chạy SwingWorker
    }

    // --- Process Reset Password Logic (Step 2) ---
    private void processResetPassword() {
        String enteredOtp = txtOtp.getText().trim();
        char[] newPasswordChars = txtNewPasswordStep2.getPassword();
        char[] confirmPasswordChars = txtConfirmPasswordStep2.getPassword();

        // Basic validation for Step 2
        if (enteredOtp.isEmpty() || newPasswordChars.length == 0 || confirmPasswordChars.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin.",
                    "Lỗi đặt lại mật khẩu", JOptionPane.WARNING_MESSAGE);
             // Clear sensitive data
             Arrays.fill(newPasswordChars, ' ');
             Arrays.fill(confirmPasswordChars, ' ');
            return;
        }

        if (!Arrays.equals(newPasswordChars, confirmPasswordChars)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới và xác nhận mật khẩu mới không khớp.",
                    "Lỗi đặt lại mật khẩu", JOptionPane.WARNING_MESSAGE);
            txtNewPasswordStep2.setText("");
            txtConfirmPasswordStep2.setText("");
             // Clear sensitive data
             Arrays.fill(newPasswordChars, ' ');
             Arrays.fill(confirmPasswordChars, ' ');
            txtNewPasswordStep2.requestFocusInWindow();
            return;
        }

        // 1. Verify OTP
        if (generatedOtp == null || !generatedOtp.equals(enteredOtp)) {
            JOptionPane.showMessageDialog(this, "Mã OTP không hợp lệ. Vui lòng kiểm tra lại.",
                    "Lỗi xác minh OTP", JOptionPane.ERROR_MESSAGE);
            txtOtp.requestFocusInWindow();
             // Clear sensitive data
             Arrays.fill(newPasswordChars, ' ');
             Arrays.fill(confirmPasswordChars, ' ');
            return;
        }

        // Ensure userToReset is not null (should be set in Step 1)
        if (userToReset == null) {
             JOptionPane.showMessageDialog(this, "Thông tin tài khoản không hợp lệ. Vui lòng bắt đầu lại.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
             // Clear sensitive data
             Arrays.fill(newPasswordChars, ' ');
             Arrays.fill(confirmPasswordChars, ' ');
             dispose(); // Close the dialog as something is wrong
             return;
        }


        // 2. Update Password in Database
        // Sử dụng SwingWorker để cập nhật CSDL ở background, tránh làm đơ UI
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                 // TODO: HASH PASSWORD before updating using userToReset.getTendangnhap() and the new password
                 // String hashedPassword = hashPassword(new String(newPasswordChars)); // Implement hashing
                 // Call NhanVienDAO's updatePassword method

                // Use NhanVienDAO's updatePassword method
                 return nhanVienDAO.updatePassword(userToReset.getTendangnhap(), new String(newPasswordChars)); // Use Tendangnhap and the new password
            }

            @Override
            protected void done() {
                try {
                    boolean updateSuccessful = get(); // Lấy kết quả từ doInBackground()
                    if (updateSuccessful) {
                        JOptionPane.showMessageDialog(ForgotPasswordDialog.this,
                                "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập với mật khẩu mới.",
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        dispose(); // Close dialog on success
                    } else {
                        // Xử lý trường hợp updatePassword trả về false
                         JOptionPane.showMessageDialog(ForgotPasswordDialog.this,
                                "Cập nhật mật khẩu thất bại. Vui lòng thử lại.",
                                "Lỗi cập nhật", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) { // Catch any exceptions during update
                    LOGGER.log(Level.SEVERE, "Lỗi trong SwingWorker khi cập nhật mật khẩu", ex);
                     JOptionPane.showMessageDialog(ForgotPasswordDialog.this,
                            "Có lỗi xảy ra khi đặt lại mật khẩu: " + ex.getMessage(),
                            "Lỗi cập nhật", JOptionPane.ERROR_MESSAGE);
                } finally {
                     // Always clear sensitive data from memory
                     Arrays.fill(newPasswordChars, ' ');
                     Arrays.fill(confirmPasswordChars, ' ');
                }
            }
        }.execute(); // Chạy SwingWorker
    }


    // --- Helper method to generate OTP ---
    private String generateOtp(int length) {
        String numbers = "0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(numbers.charAt(rnd.nextInt(numbers.length())));
        }
        return sb.toString();
    }

    // --- Helper method to send OTP Email (Actual Implementation) ---
    // NOTE: This implementation uses JavaMail API but requires you to fill in your details.
    // It also assumes your email account allows sending emails programmatically (less secure apps, app passwords, etc.).
    private boolean sendOtpEmail(String recipientEmail, String otp) {
        LOGGER.log(Level.INFO, "Attempting to send OTP {0} to {1}", new Object[]{otp, recipientEmail});

        // --- Debugging: Log email and masked password ---
        String maskedPassword = SENDER_PASSWORD.length() > 4 ?
                                 SENDER_PASSWORD.substring(0, 2) + "..." + SENDER_PASSWORD.substring(SENDER_PASSWORD.length() - 2) :
                                 "***";
        LOGGER.log(Level.INFO, "Using Sender Email: {0}, Password (masked): {1}", new Object[]{SENDER_EMAIL, maskedPassword});


        // Kiểm tra cấu hình email
        if (SENDER_EMAIL.equals("hoangquangdat182005@gmail.com") || SENDER_PASSWORD.equals("fhcx yrgd cqfw qecz") ||
             SMTP_HOST.equals("smtp.gmail.com") || SMTP_PORT.equals("587")) { // Check against your actual defaults
             // This check should really be against placeholder values, not the user's values.
             // Let's assume the user has filled them in correctly for now, but log if they are default placeholders.
             if (SENDER_EMAIL.equals("your_email@example.com") || SENDER_PASSWORD.equals("your_email_password")) { // Placeholder check
                 LOGGER.log(Level.SEVERE, "Cấu hình email gửi OTP chưa được thiết lập đầy đủ. Vui lòng thay thế placeholder.");
                 // Do NOT return false just because of placeholder check in the middle of a method.
                 // This check is better placed before calling sendOtpEmail or in a configuration class.
             }
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // Hoặc mail.smtp.ssl.enable nếu dùng SSL
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        // Nếu dùng Gmail, có thể cần thêm dòng này nếu gặp lỗi SSL
        // props.put("mail.smtp.ssl.trust", SMTP_HOST);


        // Tạo Session với Authenticator
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() { // Use javax.mail.PasswordAuthentication
                return new javax.mail.PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD); // Đảm bảo mật khẩu chính xác ở đây
            }
        });

        try {
            // Tạo đối tượng Message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL)); // Địa chỉ người gửi
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail)); // Địa chỉ người nhận
            message.setSubject("Mã OTP đặt lại mật khẩu của bạn"); // Tiêu đề email
            message.setText("Mã OTP của bạn là: " + otp + "\nVui lòng nhập mã này để đặt lại mật khẩu."); // Nội dung email

            // Gửi email
            javax.mail.Transport.send(message); // Use the fully qualified name to avoid conflict
            LOGGER.log(Level.INFO, "OTP email sent successfully to {0}", recipientEmail);
            return true; // Gửi thành công
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send OTP email", e); // Updated log message
             // Do not re-throw here if SwingWorker handles it. Just return false.
            return false; // Gửi thất bại
        } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Lỗi không xác định khi gửi email OTP", e);
             return false; // Gửi thất bại
        }
    }
}

//     // For testing - can delete in production
// //     public static void main(String[] args) {
// //         SwingUtilities.invokeLater(() -> {
// //             // Tạo một dummy owner frame/dialog cho testing
// //             JFrame ownerFrame = new JFrame("Forgot Password Dialog Test Owner");
// //             ownerFrame.setSize(300, 200);
// //             ownerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
// //             // ownerFrame.setVisible(true); // Keep it invisible if you only want to see the dialog
// //             ForgotPasswordDialog dialog = new ForgotPasswordDialog(ownerFrame); // Pass dummy owner
// //             dialog.setVisible(true);

// //             System.exit(0);
// //         });
// //     }
// }
