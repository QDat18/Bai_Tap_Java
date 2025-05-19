package ui;

import dao.NhanVienDAO;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.NhanVien;

public class LoginDialog extends JDialog {

    private final Color coffeeBrown = new Color(102, 51, 0);
    private final Color lightBeige = new Color(245, 245, 220);
    private final Color accentGreen = new Color(60, 179, 113);
    private final Color darkGray = new Color(50, 50, 50);
    private final Color linkColor = new Color(0, 102, 204);
    private final Color hoverGreen = new Color(72, 209, 143);
    private final Color hoverLink = new Color(30, 144, 255);

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnCancel;
    private JButton btnRegister;
    private JButton btnForgotPassword;
    private JLabel lblTitle;
    private JLabel lblLoading;
    private JLabel lblLogo;
    private JLabel lblWelcome;
    private JPanel loadingPanel;
    private JPanel mainContentPanel;
    private Timer loadingTimer;
    private JProgressBar progressBar;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private NhanVienDAO nhanVienDAO;
    private NhanVien loggedInUser;

    public LoginDialog(Frame owner) {
        super(owner, "Đăng nhập Hệ thống", true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        nhanVienDAO = new NhanVienDAO();
        
        setSize(600, 550);
        setLocationRelativeTo(owner);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 600, 550, 15, 15));
        setResizable(false);
        
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());
        contentPane.setBackground(lightBeige);
        contentPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(102, 51, 0, 100), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);
        cardPanel.setOpaque(false);
        
        createMainPanel();
        createLoadingPanel();
        
        cardPanel.add(mainContentPanel, "main");
        cardPanel.add(loadingPanel, "loading");
        contentPane.add(cardPanel, BorderLayout.CENTER);
        
        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        toolBar.setOpaque(false);
        JButton btnClose = new JButton("×");
        btnClose.setForeground(coffeeBrown);
        btnClose.setFont(new Font("Arial", Font.BOLD, 20));
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.addActionListener(e -> cancelLogin());
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnClose.setForeground(Color.RED); }
            public void mouseExited(MouseEvent e) { btnClose.setForeground(coffeeBrown); }
        });
        toolBar.add(btnClose);
        contentPane.add(toolBar, BorderLayout.NORTH);
        
        MouseAdapter dragWindowAdapter = new MouseAdapter() {
            private Point initialClick;
            @Override public void mousePressed(MouseEvent e) { initialClick = e.getPoint(); }
            @Override public void mouseDragged(MouseEvent e) {
                Point currentPoint = e.getLocationOnScreen();
                setLocation(currentPoint.x - initialClick.x, currentPoint.y - initialClick.y);
            }
        };
        addMouseListener(dragWindowAdapter);
        addMouseMotionListener(dragWindowAdapter);
        
        btnLogin.addActionListener(e -> performLogin());
        btnCancel.addActionListener(e -> cancelLogin());
        txtPassword.addActionListener(e -> performLogin());
        btnRegister.addActionListener(e -> openRegistrationDialog());
        btnForgotPassword.addActionListener(e -> openForgotPasswordDialog());
        
        getContentPane().add(contentPane);
    }
    
    private void createMainPanel() {
        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setOpaque(false);
        
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        lblLogo = new JLabel();
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/images/logo.png"));
            Image scaledImage = logoIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            lblLogo.setText("LOGO");
            lblLogo.setFont(new Font("Arial", Font.BOLD, 24));
            lblLogo.setForeground(coffeeBrown);
        }
        logoPanel.add(lblLogo);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContentPanel.add(logoPanel);
        mainContentPanel.add(Box.createVerticalStrut(10));
        
        lblTitle = new JLabel("ĐĂNG NHẬP HỆ THỐNG");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitle.setForeground(coffeeBrown);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContentPanel.add(lblTitle);
        mainContentPanel.add(Box.createVerticalStrut(30));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.EAST;
        JLabel lblUsername = new JLabel("Tên đăng nhập:");
        lblUsername.setFont(new Font("Arial", Font.BOLD, 16));
        lblUsername.setForeground(darkGray);
        formPanel.add(lblUsername, gbc);
        
        gbc.gridx = 1; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        txtUsername = new JTextField(20);
        txtUsername.setFont(new Font("Arial", Font.PLAIN, 16));
        txtUsername.setPreferredSize(new Dimension(250, 35));
        formPanel.add(txtUsername, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.EAST;
        JLabel lblPassword = new JLabel("Mật khẩu:");
        lblPassword.setFont(new Font("Arial", Font.BOLD, 16));
        lblPassword.setForeground(darkGray);
        formPanel.add(lblPassword, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 16));
        txtPassword.setPreferredSize(new Dimension(250, 35));
        formPanel.add(txtPassword, gbc);
        
        mainContentPanel.add(formPanel);
        mainContentPanel.add(Box.createVerticalStrut(30));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));
        btnLogin.setBackground(accentGreen);
        btnLogin.setForeground(coffeeBrown);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnLogin.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnLogin.setBackground(hoverGreen); }
            public void mouseExited(MouseEvent e) { btnLogin.setBackground(accentGreen); }
        });
        
        btnCancel = new JButton("Hủy");
        btnCancel.setFont(new Font("Arial", Font.BOLD, 16));
        btnCancel.setBackground(darkGray);
        btnCancel.setForeground(coffeeBrown);
        btnCancel.setFocusPainted(false);
        btnCancel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnCancel.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnCancel.setBackground(new Color(70, 70, 70)); }
            public void mouseExited(MouseEvent e) { btnCancel.setBackground(accentGreen); }
        });
        
        buttonPanel.add(btnLogin);
        buttonPanel.add(Box.createHorizontalStrut(20));
        buttonPanel.add(btnCancel);
        
        mainContentPanel.add(buttonPanel);
        mainContentPanel.add(Box.createVerticalStrut(20));
        
        JPanel linksPanel = new JPanel();
        linksPanel.setOpaque(false);
        linksPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        btnRegister = new JButton("Đăng ký tài khoản");
        btnRegister.setFont(new Font("Arial", Font.PLAIN, 14));
        btnRegister.setForeground(linkColor);
        btnRegister.setBorderPainted(false);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnRegister.setForeground(hoverLink); }
            public void mouseExited(MouseEvent e) { btnRegister.setForeground(linkColor); }
        });
        
        btnForgotPassword = new JButton("Quên mật khẩu?");
        btnForgotPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        btnForgotPassword.setForeground(linkColor);
        btnForgotPassword.setBorderPainted(false);
        btnForgotPassword.setContentAreaFilled(false);
        btnForgotPassword.setFocusPainted(false);
        btnForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgotPassword.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnForgotPassword.setForeground(hoverLink); }
            public void mouseExited(MouseEvent e) { btnForgotPassword.setForeground(linkColor); }
        });
        
        linksPanel.add(btnRegister);
        linksPanel.add(Box.createHorizontalStrut(30));
        linksPanel.add(btnForgotPassword);
        
        mainContentPanel.add(linksPanel);
    }
    
    private void createLoadingPanel() {
        loadingPanel = new JPanel();
        loadingPanel.setLayout(new BoxLayout(loadingPanel, BoxLayout.Y_AXIS));
        loadingPanel.setOpaque(false);
        
        lblLogo = new JLabel();
        try {
            ImageIcon logoIcon = new ImageIcon(getClass().getResource("/images/logo.png"));
            Image scaledImage = logoIcon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            lblLogo.setText("LOGO");
            lblLogo.setFont(new Font("Arial", Font.BOLD, 28));
            lblLogo.setForeground(coffeeBrown);
        }
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingPanel.add(Box.createVerticalStrut(30));
        loadingPanel.add(lblLogo);
        loadingPanel.add(Box.createVerticalStrut(30));
        
        lblLoading = new JLabel("Đang đăng nhập...");
        lblLoading.setFont(new Font("Arial", Font.BOLD, 20));
        lblLoading.setForeground(coffeeBrown);
        lblLoading.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingPanel.add(lblLoading);
        loadingPanel.add(Box.createVerticalStrut(20));
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setForeground(accentGreen);
        progressBar.setPreferredSize(new Dimension(250, 8));
        progressBar.setMaximumSize(new Dimension(250, 8));
        progressBar.setBackground(new Color(235, 235, 210));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingPanel.add(progressBar);
        loadingPanel.add(Box.createVerticalStrut(40));
        
        lblWelcome = new JLabel("");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 20));
        lblWelcome.setForeground(coffeeBrown);
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadingPanel.add(lblWelcome);
    }
    
    private void performLogin() {
        String username = txtUsername.getText().trim();
        char[] passwordChars = txtPassword.getPassword();
        String password = new String(passwordChars);

        if (username.isEmpty() || passwordChars.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ Tên đăng nhập và Mật khẩu.",
                    "Lỗi đăng nhập", JOptionPane.WARNING_MESSAGE);
            clearPasswordField(passwordChars);
            return;
        }

        if (!isValidUsername(username)) {
            JOptionPane.showMessageDialog(this,
                    "Tên đăng nhập phải có độ dài 4-20 ký tự và chỉ chứa chữ cái, số, _ hoặc .",
                    "Lỗi đăng nhập", JOptionPane.WARNING_MESSAGE);
            txtUsername.setText("");
            clearPasswordField(passwordChars);
            txtUsername.requestFocusInWindow();
            return;
        }

        cardLayout.show(cardPanel, "loading");

        loggedInUser = nhanVienDAO.authenticate(username, password);

        if (loggedInUser != null) {
            String displayName = loggedInUser.getTenNV();
            if (displayName == null || displayName.isEmpty()) {
                displayName = loggedInUser.getTendangnhap();
            }
            lblWelcome.setText("The Coffee Team chào " + displayName);
        } else {
            lblWelcome.setText("The Coffee Team chào bạn");
        }

        String[] loadingStates = {"Đang đăng nhập.", "Đang đăng nhập..", "Đang đăng nhập..."};
        final int[] currentState = {0};
        Timer animationTimer = new Timer(90, e -> {
            lblLoading.setText(loadingStates[currentState[0] % loadingStates.length]);
            currentState[0]++;
        });
        animationTimer.start();

        loadingTimer = new Timer(1000, e -> {
            animationTimer.stop();

            if (loggedInUser != null) {
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Tên đăng nhập hoặc Mật khẩu không chính xác.",
                        "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
                cardLayout.show(cardPanel, "main");
                txtPassword.setText("");
                txtUsername.requestFocusInWindow();
            }
            clearPasswordField(passwordChars);
        });
        loadingTimer.setRepeats(false);
        loadingTimer.start();
    }
    
    private void cancelLogin() {
        loggedInUser = null;
        dispose();
    }
    
    public NhanVien getLoggedInUser() {
        return loggedInUser;
    }
    
    private void openRegistrationDialog() {
        RegistrationDialog registrationDialog = new RegistrationDialog(this);
        registrationDialog.setVisible(true);
        if (registrationDialog.isRegistrationSuccessful()) {
            JOptionPane.showMessageDialog(this,
                    "Tài khoản đã được tạo thành công. Bạn có thể đăng nhập ngay bây giờ.",
                    "Đăng ký thành công", JOptionPane.INFORMATION_MESSAGE);
            txtUsername.setText(registrationDialog.getRegisteredUsername());
            txtPassword.setText("");
        }
    }
    
    private void openForgotPasswordDialog() {
        ForgotPasswordDialog forgotPasswordDialog = new ForgotPasswordDialog(this);
        forgotPasswordDialog.setVisible(true);
    }
    
    private boolean isValidUsername(String username) {
        String usernameRegex = "^[a-zA-Z0-9_.]{4,20}$";
        return username.matches(usernameRegex);
    }

    private void clearPasswordField(char[] field) {
        Arrays.fill(field, ' ');
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            JFrame ownerFrame = new JFrame("Login Dialog Test Owner");
            ownerFrame.setSize(300, 200);
            ownerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            LoginDialog loginDialog = new LoginDialog(ownerFrame);
            loginDialog.setVisible(true);
            
            NhanVien user = loginDialog.getLoggedInUser();
            if (user != null) {
                System.out.println("Test login successful: " + user.getTendangnhap());
            } else {
                System.out.println("Test login canceled or failed.");
            }
            
            System.exit(0);
        });
    }
}