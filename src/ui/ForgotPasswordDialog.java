package ui;

// Remove incorrect imports and keep necessary ones
import dao.NhanVienDAO;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.NhanVien;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;
import java.util.Arrays;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ForgotPasswordDialog extends JDialog {

    // Define colors
    Color coffeeBrown = new Color(102, 51, 0);
    Color lightBeige = new Color(245, 245, 220);
    Color accentGreen = new Color(60, 179, 113);
    Color darkGray = new Color(50, 50, 50);

    // UI Components
    private JTextField txtUsernameStep1;
    private JTextField txtEmailStep1;
    private JButton btnSendOtp;
    private JButton btnCancelStep1;

    private JTextField txtOtp;
    private JPasswordField txtNewPasswordStep2;
    private JPasswordField txtConfirmPasswordStep2;
    private JButton btnResetPasswordStep2;
    private JButton btnCancelStep2;

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JPanel step1Panel;
    private JPanel step2Panel;

    private NhanVienDAO nhanVienDAO;

    private String generatedOtp;
    private NhanVien userToReset;

    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordDialog.class.getName());

    private static final String SENDER_EMAIL = "hoangquangdat182005@gmail.com";
    private static final String SENDER_PASSWORD = "fhcx yrgd cqfw qecz";
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";

    public ForgotPasswordDialog(Window owner) {
        super(owner, "Quên mật khẩu", Dialog.ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        nhanVienDAO = new NhanVienDAO();

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        step1Panel = new JPanel(new GridBagLayout());
        step1Panel.setBackground(lightBeige);
        step1Panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.insets = new Insets(10, 10, 10, 10);
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        gbc1.weightx = 1.0;

        gbc1.gridx = 0; gbc1.gridy = 0; gbc1.gridwidth = 2; gbc1.anchor = GridBagConstraints.CENTER;
        JLabel lblTitle1 = new JLabel("XÁC MINH TÀI KHOẢN", SwingConstants.CENTER);
        lblTitle1.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle1.setForeground(coffeeBrown);
        step1Panel.add(lblTitle1, gbc1);

        gbc1.gridx = 0; gbc1.gridy = 1; gbc1.gridwidth = 1; gbc1.anchor = GridBagConstraints.WEST;
        step1Panel.add(createLabel("Tên đăng nhập:"), gbc1);
        gbc1.gridx = 1; gbc1.gridy = 1;
        txtUsernameStep1 = new JTextField(20);
        step1Panel.add(txtUsernameStep1, gbc1);

        gbc1.gridx = 0; gbc1.gridy = 2;
        step1Panel.add(createLabel("Email đăng ký:"), gbc1);
        gbc1.gridx = 1; gbc1.gridy = 2;
        txtEmailStep1 = new JTextField(20);
        step1Panel.add(txtEmailStep1, gbc1);

        gbc1.gridx = 0; gbc1.gridy = 3; gbc1.gridwidth = 2; gbc1.anchor = GridBagConstraints.CENTER;
        JPanel actionButtonPanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionButtonPanel1.setBackground(lightBeige);

        btnSendOtp = new JButton("Gửi mã OTP");
        styleButton(btnSendOtp, accentGreen, Color.WHITE);
        actionButtonPanel1.add(btnSendOtp);

        btnCancelStep1 = new JButton("Hủy");
        styleButton(btnCancelStep1, darkGray, Color.WHITE);
        btnCancelStep1.addActionListener(e -> dispose());
        actionButtonPanel1.add(btnCancelStep1);

        step1Panel.add(actionButtonPanel1, gbc1);

        step2Panel = new JPanel(new GridBagLayout());
        step2Panel.setBackground(lightBeige);
        step2Panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(10, 10, 10, 10);
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        gbc2.weightx = 1.0;

        gbc2.gridx = 0; gbc2.gridy = 0; gbc2.gridwidth = 2; gbc2.anchor = GridBagConstraints.CENTER;
        JLabel lblTitle2 = new JLabel("NHẬP OTP VÀ MẬT KHẨU MỚI", SwingConstants.CENTER);
        lblTitle2.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitle2.setForeground(coffeeBrown);
        step2Panel.add(lblTitle2, gbc2);

        gbc2.gridx = 0; gbc2.gridy = 1; gbc2.gridwidth = 1; gbc2.anchor = GridBagConstraints.WEST;
        step2Panel.add(createLabel("Mã OTP:"), gbc2);
        gbc2.gridx = 1; gbc2.gridy = 1;
        txtOtp = new JTextField(10);
        step2Panel.add(txtOtp, gbc2);

        gbc2.gridx = 0; gbc2.gridy = 2;
        step2Panel.add(createLabel("Mật khẩu mới:"), gbc2);
        gbc2.gridx = 1; gbc2.gridy = 2;
        txtNewPasswordStep2 = new JPasswordField(20);
        step2Panel.add(txtNewPasswordStep2, gbc2);

        gbc2.gridx = 0; gbc2.gridy = 3;
        step2Panel.add(createLabel("Xác nhận mật khẩu mới:"), gbc2);
        gbc2.gridx = 1; gbc2.gridy = 3;
        txtConfirmPasswordStep2 = new JPasswordField(20);
        step2Panel.add(txtConfirmPasswordStep2, gbc2);

        gbc2.gridx = 0; gbc2.gridy = 4; gbc2.gridwidth = 2; gbc2.anchor = GridBagConstraints.CENTER;
        JPanel actionButtonPanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionButtonPanel2.setBackground(lightBeige);

        btnResetPasswordStep2 = new JButton("Đặt lại mật khẩu");
        styleButton(btnResetPasswordStep2, accentGreen, Color.WHITE);
        actionButtonPanel2.add(btnResetPasswordStep2);

        btnCancelStep2 = new JButton("Hủy");
        styleButton(btnCancelStep2, darkGray, Color.WHITE);
        btnCancelStep2.addActionListener(e -> dispose());
        actionButtonPanel2.add(btnCancelStep2);

        step2Panel.add(actionButtonPanel2, gbc2);

        cardPanel.add(step1Panel, "Step1");
        cardPanel.add(step2Panel, "Step2");

        getContentPane().add(cardPanel);

        btnSendOtp.addActionListener(e -> processSendOtp());
        btnResetPasswordStep2.addActionListener(e -> processResetPassword());

        cardLayout.show(cardPanel, "Step1");

        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
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

    private void processSendOtp() {
        String username = txtUsernameStep1.getText().trim();
        String email = txtEmailStep1.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ tên đăng nhập và email.",
                    "Lỗi xác minh", JOptionPane.WARNING_MESSAGE);
            return;
        }

        userToReset = nhanVienDAO.getNhanVienByTendangnhap(username);

        if (userToReset == null) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập không tồn tại.",
                    "Lỗi xác minh", JOptionPane.ERROR_MESSAGE);
            txtUsernameStep1.requestFocusInWindow();
            return;
        }

        if (!userToReset.getEmail().equalsIgnoreCase(email)) {
            JOptionPane.showMessageDialog(this, "Email không khớp với email đã đăng ký cho tài khoản này.",
                    "Lỗi xác minh", JOptionPane.ERROR_MESSAGE);
            txtEmailStep1.requestFocusInWindow();
            return;
        }

        generatedOtp = generateOtp(6);

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return sendOtpEmail(userToReset.getEmail(), generatedOtp);
            }

            @Override
            protected void done() {
                try {
                    boolean emailSent = get();
                    if (emailSent) {
                        JOptionPane.showMessageDialog(ForgotPasswordDialog.this,
                                "Mã OTP đã được gửi đến email đăng ký của bạn. Vui lòng kiểm tra hộp thư.",
                                "Gửi OTP thành công", JOptionPane.INFORMATION_MESSAGE);

                        cardLayout.show(cardPanel, "Step2");
                        txtOtp.setText("");
                        txtNewPasswordStep2.setText("");
                        txtConfirmPasswordStep2.setText("");
                        txtOtp.requestFocusInWindow();

                        pack();
                    } else {
                        JOptionPane.showMessageDialog(ForgotPasswordDialog.this,
                                "Không thể gửi mã OTP đến email. Vui lòng kiểm tra lại thông tin hoặc thử lại sau.",
                                "Lỗi gửi OTP", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Lỗi trong SwingWorker khi gửi email OTP", ex);
                    JOptionPane.showMessageDialog(ForgotPasswordDialog.this,
                            "Đã xảy ra lỗi trong quá trình gửi email: " + ex.getMessage(),
                            "Lỗi gửi OTP", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void processResetPassword() {
        String enteredOtp = txtOtp.getText().trim();
        char[] newPasswordChars = txtNewPasswordStep2.getPassword();
        char[] confirmPasswordChars = txtConfirmPasswordStep2.getPassword();
        String newPassword = new String(newPasswordChars);
        String confirmPassword = new String(confirmPasswordChars);

        // Validation
        if (enteredOtp.isEmpty() || newPasswordChars.length == 0 || confirmPasswordChars.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin.",
                    "Lỗi đặt lại mật khẩu", JOptionPane.WARNING_MESSAGE);
            clearPasswordFields(newPasswordChars, confirmPasswordChars);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới và xác nhận mật khẩu mới không khớp.",
                    "Lỗi đặt lại mật khẩu", JOptionPane.WARNING_MESSAGE);
            txtNewPasswordStep2.setText("");
            txtConfirmPasswordStep2.setText("");
            clearPasswordFields(newPasswordChars, confirmPasswordChars);
            txtNewPasswordStep2.requestFocusInWindow();
            return;
        }

        if (!isValidPassword(newPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải có ít nhất 6 ký tự, bao gồm chữ cái in hoa, chữ cái thường và số.",
                    "Lỗi đặt lại mật khẩu", JOptionPane.WARNING_MESSAGE);
            txtNewPasswordStep2.setText("");
            txtConfirmPasswordStep2.setText("");
            clearPasswordFields(newPasswordChars, confirmPasswordChars);
            txtNewPasswordStep2.requestFocusInWindow();
            return;
        }

        if (generatedOtp == null || !generatedOtp.equals(enteredOtp)) {
            JOptionPane.showMessageDialog(this, "Mã OTP không hợp lệ. Vui lòng kiểm tra lại.",
                    "Lỗi xác minh OTP", JOptionPane.ERROR_MESSAGE);
            txtOtp.requestFocusInWindow();
            clearPasswordFields(newPasswordChars, confirmPasswordChars);
            return;
        }

        if (userToReset == null) {
            JOptionPane.showMessageDialog(this, "Thông tin tài khoản không hợp lệ. Vui lòng bắt đầu lại.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            clearPasswordFields(newPasswordChars, confirmPasswordChars);
            dispose();
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return nhanVienDAO.updatePassword(userToReset.getTendangnhap(), newPassword);
            }

            @Override
            protected void done() {
                try {
                    boolean updateSuccessful = get();
                    if (updateSuccessful) {
                        JOptionPane.showMessageDialog(ForgotPasswordDialog.this,
                                "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập với mật khẩu mới.",
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(ForgotPasswordDialog.this,
                                "Cập nhật mật khẩu thất bại. Vui lòng thử lại.",
                                "Lỗi cập nhật", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, "Lỗi trong SwingWorker khi cập nhật mật khẩu", ex);
                    JOptionPane.showMessageDialog(ForgotPasswordDialog.this,
                            "Có lỗi xảy ra khi đặt lại mật khẩu: " + ex.getMessage(),
                            "Lỗi cập nhật", JOptionPane.ERROR_MESSAGE);
                } finally {
                    clearPasswordFields(newPasswordChars, confirmPasswordChars);
                }
            }
        }.execute();
    }

    private String generateOtp(int length) {
        String numbers = "0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(numbers.charAt(rnd.nextInt(numbers.length())));
        }
        return sb.toString();
    }

    private boolean sendOtpEmail(String recipientEmail, String otp) {
        LOGGER.log(Level.INFO, "Attempting to send OTP {0} to {1}", new Object[]{otp, recipientEmail});

        String maskedPassword = SENDER_PASSWORD.length() > 4 ?
                SENDER_PASSWORD.substring(0, 2) + "..." + SENDER_PASSWORD.substring(SENDER_PASSWORD.length() - 2) :
                "***";
        LOGGER.log(Level.INFO, "Using Sender Email: {0}, Password (masked): {1}", new Object[]{SENDER_EMAIL, maskedPassword});

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Mã OTP đặt lại mật khẩu của bạn");
            message.setText("Mã OTP của bạn là: " + otp + "\nVui lòng nhập mã này để đặt lại mật khẩu.");

            javax.mail.Transport.send(message);
            LOGGER.log(Level.INFO, "OTP email sent successfully to {0}", recipientEmail);
            return true;
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send OTP email", e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi không xác định khi gửi email OTP", e);
            return false;
        }
    }

    // Validation method for password
    private boolean isValidPassword(String password) {
        if (password.length() < 6) return false;
        boolean hasUpperCase = !password.equals(password.toLowerCase());
        boolean hasLowerCase = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        return hasUpperCase && hasLowerCase && hasDigit;
    }

    // Helper method to clear password fields
    private void clearPasswordFields(char[]... fields) {
        for (char[] field : fields) {
            Arrays.fill(field, ' ');
        }
    }
}