package ui;

import dao.NhanVienDAO; // Import NhanVien model
import java.awt.*; // Import NhanVienDAO
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.swing.*;
import javax.swing.border.EmptyBorder; // For password hashing (optional but recommended)
import model.NhanVien; // For encoding hashed password (optional)

// Import các lớp viền nếu cần style cụ thể trong dialog, nhưng hiện tại đã dùng BorderFactory
// import javax.swing.border.LineBorder;
// import javax.swing.border.CompoundBorder;


public class NhanVienDetailsDialog extends JDialog { // <-- Tên lớp chính xác

    // --- Define colors (reusing the palette) ---
    Color coffeeBrown = new Color(102, 51, 0); // Màu nâu cà phê
    Color lightBeige = new Color(245, 245, 220); // Màu be nhạt (cho nền)
    Color accentGreen = new Color(60, 179, 113); // Màu xanh lá (ví dụ: nút Lưu)
    Color accentOrange = new Color(255, 165, 0); // Màu cam (ví dụ: nút Hủy hoặc cảnh báo)
    Color darkGray = new Color(50, 50, 50); // Màu xám đậm (cho văn bản, viền)
    Color accentBlue = new Color(30, 144, 255); // Màu xanh dương


    private Frame ownerFrame; // <-- Thêm biến thành viên để lưu trữ owner frame

    private JTextField txtMaNV;
    private JTextField txtTenNV;
    private JTextField txtDiachi;
    private JTextField txtGioitinh; // Maybe change to JComboBox for better data entry
    private JTextField txtSDT;
    private JTextField txtTendangnhap;
    private JPasswordField txtMatkhau; // Use JPasswordField for password input
    private JTextField txtEmail;
    private JComboBox<String> cbRole; // Use JComboBox for role selection

    private JButton btnSave;
    private JButton btnCancel;

    private NhanVienDAO nhanVienDAO;
    private NhanVien currentNhanVien; // The NhanVien object being edited (null for add)

    private boolean isAddingNew; // Flag to indicate if we are adding or updating

    // --- Add field to track if saving was successful ---
    private boolean saved = false; // <-- Thêm cờ để theo dõi liệu việc lưu có thành công hay không


    // Constructor for adding a new NhanVien
    // FIX: Corrected constructor name from NhanVienDetaislDialog
    public NhanVienDetailsDialog(Frame owner) {
        super(owner, "Thêm mới Nhân viên", true); // Modal dialog
        // --- FIX: Store the owner frame ---
        this.ownerFrame = owner; // <-- Lưu owner frame

        this.nhanVienDAO = new NhanVienDAO();
        this.currentNhanVien = null;
        this.isAddingNew = true;

        initComponents(); // Initialize UI components
        populateFields(); // Populate fields (will be empty for new)
    }

    // Constructor for updating an existing NhanVien
    public NhanVienDetailsDialog(Frame owner, NhanVien nhanVien) { // <-- Tên lớp chính xác
        super(owner, "Cập nhật Thông tin Nhân viên", true); // Modal dialog
         // --- FIX: Store the owner frame ---
        this.ownerFrame = owner; // <-- Lưu owner frame

        this.nhanVienDAO = new NhanVienDAO();
        this.currentNhanVien = nhanVien;
        this.isAddingNew = false;

        initComponents(); // Initialize UI components
        populateFields(); // Populate fields with existing data
    }

    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPane.setBackground(lightBeige); // <-- Set background color
        setContentPane(contentPane);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(lightBeige);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.insets = new Insets(5, 5, 5, 5);
        labelGbc.gridx = 0;
        labelGbc.anchor = GridBagConstraints.EAST;

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.insets = new Insets(5, 5, 5, 5);
        fieldGbc.gridx = 1;
        fieldGbc.weightx = 1.0;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;


        int row = 0;
        // Row 0: MaNV
        labelGbc.gridy = row; inputPanel.add(new JLabel("Mã NV:"), labelGbc);
        fieldGbc.gridy = row;
        txtMaNV = new JTextField(20);
        txtMaNV.setEditable(false);
        inputPanel.add(txtMaNV, fieldGbc);
        row++;

        // Row 1: TenNV
        labelGbc.gridy = row; inputPanel.add(new JLabel("Tên NV:"), labelGbc);
        fieldGbc.gridy = row;
        txtTenNV = new JTextField(20);
        inputPanel.add(txtTenNV, fieldGbc);
        row++;

        // Row 2: Địa chỉ
        labelGbc.gridy = row; inputPanel.add(new JLabel("Địa chỉ:"), labelGbc);
        fieldGbc.gridy = row;
        txtDiachi = new JTextField(20);
        inputPanel.add(txtDiachi, fieldGbc);
        row++;

        // Row 3: Giới tính
        labelGbc.gridy = row; inputPanel.add(new JLabel("Giới tính:"), labelGbc);
        fieldGbc.gridy = row;
        txtGioitinh = new JTextField(20);
        inputPanel.add(txtGioitinh, fieldGbc);
        row++;

        // Row 4: SĐT
        labelGbc.gridy = row; inputPanel.add(new JLabel("SĐT:"), labelGbc);
        fieldGbc.gridy = row;
        txtSDT = new JTextField(20);
        inputPanel.add(txtSDT, fieldGbc);
        row++;

        // --- Account Information Section ---
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 5, 5, 5);
        inputPanel.add(new JSeparator(), gbc);
        row++;

        JLabel accountSectionTitle = new JLabel("Thông tin Tài khoản", SwingConstants.CENTER);
        accountSectionTitle.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(5, 5, 10, 5);
        inputPanel.add(accountSectionTitle, gbc);
        row++;

        // Row X: Tên đăng nhập
        labelGbc.gridy = row; labelGbc.gridwidth = 1; labelGbc.anchor = GridBagConstraints.EAST; inputPanel.add(new JLabel("Tên đăng nhập:"), labelGbc);
        fieldGbc.gridy = row; fieldGbc.gridwidth = 1; fieldGbc.fill = GridBagConstraints.HORIZONTAL; fieldGbc.weightx = 1.0;
        txtTendangnhap = new JTextField(20);
         if (!isAddingNew) {
             txtTendangnhap.setEditable(false);
             txtTendangnhap.setBackground(Color.LIGHT_GRAY);
         }
        inputPanel.add(txtTendangnhap, fieldGbc);
        row++;

        // Row Y: Mật khẩu
        labelGbc.gridy = row; labelGbc.gridwidth = 1; labelGbc.anchor = GridBagConstraints.EAST; inputPanel.add(new JLabel("Mật khẩu:"), labelGbc);
        fieldGbc.gridy = row; fieldGbc.gridwidth = 1; fieldGbc.fill = GridBagConstraints.HORIZONTAL; fieldGbc.weightx = 1.0;
        txtMatkhau = new JPasswordField(20);
        inputPanel.add(txtMatkhau, fieldGbc);
        row++;

         // Row Z: Email
        labelGbc.gridy = row; labelGbc.gridwidth = 1; labelGbc.anchor = GridBagConstraints.EAST; inputPanel.add(new JLabel("Email:"), labelGbc);
        fieldGbc.gridy = row; fieldGbc.gridwidth = 1; fieldGbc.fill = GridBagConstraints.HORIZONTAL; fieldGbc.weightx = 1.0;
        txtEmail = new JTextField(20);
        inputPanel.add(txtEmail, fieldGbc);
        row++;

        // Row W: Vai trò (Role)
        labelGbc.gridy = row; labelGbc.gridwidth = 1; labelGbc.anchor = GridBagConstraints.EAST; inputPanel.add(new JLabel("Vai trò:"), labelGbc);
        fieldGbc.gridy = row; fieldGbc.gridwidth = 1; fieldGbc.fill = GridBagConstraints.HORIZONTAL; fieldGbc.weightx = 1.0;
        cbRole = new JComboBox<>(new String[]{"Admin", "Manager", "Staff", "Guest"}); // Example roles
        inputPanel.add(cbRole, fieldGbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        inputPanel.add(Box.createVerticalGlue(), gbc);

        contentPane.add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(lightBeige);

        btnSave = new JButton("Lưu");
        btnCancel = new JButton("Hủy");

        styleButton(btnSave, accentGreen, Color.WHITE);
        styleButton(btnCancel, darkGray, Color.WHITE);

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> saveNhanVien());
        btnCancel.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(ownerFrame);
    }

    private void populateFields() {
        if (currentNhanVien != null) {
            txtMaNV.setText(currentNhanVien.getMaNV());
            txtTenNV.setText(currentNhanVien.getTenNV());
            txtDiachi.setText(currentNhanVien.getDiachi());
            txtGioitinh.setText(currentNhanVien.getGioitinh());
            txtSDT.setText(currentNhanVien.getSDT());

            txtTendangnhap.setText(currentNhanVien.getTendangnhap());
            txtMatkhau.setText(""); // Clear password field on edit
            txtEmail.setText(currentNhanVien.getEmail());

            String currentRole = currentNhanVien.getRole();
            boolean roleFound = false;
            for (int i = 0; i < cbRole.getItemCount(); i++) {
                if (cbRole.getItemAt(i).equalsIgnoreCase(currentRole)) {
                    cbRole.setSelectedIndex(i);
                    roleFound = true;
                    break;
                }
            }
            if (!roleFound && currentRole != null && !currentRole.isEmpty()) {
                 cbRole.addItem(currentRole);
                 cbRole.setSelectedItem(currentRole);
            }

        } else {
            // FIX: Corrected method name from suggestNextMaNV()
            txtMaNV.setText(nhanVienDAO.generateNextMaNV()); // Generate next ID
            txtMaNV.setEditable(false);
            txtTenNV.setText("");
            txtDiachi.setText("");
            txtGioitinh.setText("");
            txtSDT.setText("");

            txtTendangnhap.setText("");
            txtMatkhau.setText("");
            txtEmail.setText("");
            cbRole.setSelectedIndex(0);
        }
    }

    private void saveNhanVien() {
        String maNV = txtMaNV.getText().trim();
        String tenNV = txtTenNV.getText().trim();
        String diachi = txtDiachi.getText().trim();
        String gioitinh = txtGioitinh.getText().trim();
        String sdt = txtSDT.getText().trim();

        String tendangnhap = txtTendangnhap.getText().trim();
        char[] matkhauChars = txtMatkhau.getPassword();
        String matkhau = new String(matkhauChars).trim();
        String email = txtEmail.getText().trim();
        String role = (String) cbRole.getSelectedItem();

        if (maNV.isEmpty() || tenNV.isEmpty() || diachi.isEmpty() || gioitinh.isEmpty() || sdt.isEmpty() || tendangnhap.isEmpty() || email.isEmpty() || role == null || role.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin nhân viên và tài khoản.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean passwordChanged = matkhauChars.length > 0;

        NhanVien nhanVienToSave;

        if (isAddingNew) {
            if (matkhau.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập mật khẩu cho tài khoản mới.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            nhanVienToSave = new NhanVien();
            nhanVienToSave.setMaNV(maNV);

            nhanVienToSave.setTendangnhap(tendangnhap);
            String hashedPassword = hashPassword(matkhau);
            if (hashedPassword == null) {
                 JOptionPane.showMessageDialog(this, "Lỗi mã hóa mật khẩu. Không thể lưu nhân viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            nhanVienToSave.setMatkhau(hashedPassword);
            nhanVienToSave.setEmail(email);
            nhanVienToSave.setRole(role);

            nhanVienToSave.setTenNV(tenNV);
            nhanVienToSave.setDiachi(diachi);
            nhanVienToSave.setGioitinh(gioitinh);
            nhanVienToSave.setSDT(sdt);

            try {
                 boolean success = nhanVienDAO.addNhanVien(nhanVienToSave);
                 if (success) {
                     JOptionPane.showMessageDialog(this, "Thêm nhân viên thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                     this.saved = true; // Set saved flag to true
                     dispose();
                 } else {
                      JOptionPane.showMessageDialog(this, "Thêm nhân viên thất bại. Kiểm tra thông tin (ví dụ: Tên đăng nhập có thể đã tồn tại).", "Lỗi", JOptionPane.ERROR_MESSAGE);
                 }
            } catch (Exception e) {
                 JOptionPane.showMessageDialog(this, "Lỗi khi thêm nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                 e.printStackTrace();
            }

        } else {
            nhanVienToSave = currentNhanVien;

            nhanVienToSave.setTenNV(tenNV);
            nhanVienToSave.setDiachi(diachi);
            nhanVienToSave.setGioitinh(gioitinh);
            nhanVienToSave.setSDT(sdt);

            // Tendangnhap is not editable

            if (passwordChanged) {
                 String hashedPassword = hashPassword(matkhau);
                 if (hashedPassword == null) {
                      JOptionPane.showMessageDialog(this, "Lỗi mã hóa mật khẩu. Không thể cập nhật nhân viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                      return;
                 }
                 nhanVienToSave.setMatkhau(hashedPassword);
            } else {
                 // If password field is empty, keep the old password from currentNhanVien
                 // The password hash from the original object (currentNhanVien) is already there
            }
            nhanVienToSave.setEmail(email);
            nhanVienToSave.setRole(role);

             try {
                  boolean success = nhanVienDAO.updateNhanVien(nhanVienToSave);
                  if (success) {
                      JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                      this.saved = true; // Set saved flag to true
                      dispose();
                  } else {
                       JOptionPane.showMessageDialog(this, "Cập nhật nhân viên thất bại. Kiểm tra thông tin.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                  }
             } catch (Exception e) {
                  JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật nhân viên: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                  e.printStackTrace();
             }
        }
    }

    // Simple password hashing (using SHA-256)
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            System.err.println("Error hashing password!");
            return null; // Indicate hashing failed
        }
    }

    // --- Add public getter for the saved flag ---
     public boolean isSaved() { // <-- Getter method
         return saved;
     }

    // Helper method to style buttons
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

    // Main method for testing (Optional)

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame dummyFrame = new JFrame();
            dummyFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            dummyFrame.setSize(100, 100);
            dummyFrame.setVisible(false);

            // Example of adding a new NhanVien
             // To test adding, you might need a mock NhaVienDAO that suggests an ID
             // NhanVienDetailsDialog addDialog = new NhanVienDetailsDialog(dummyFrame); // <-- Corrected class name
             // addDialog.setVisible(true);

            // Example of updating an existing NhanVien (needs a sample NhanVien object)
             NhanVienDAO testDao = new NhanVienDAO();
             // Replace "NV001" with an existing ID from your database
             NhanVien sampleNhanVien = testDao.getNhanVienById("NV01");

             if (sampleNhanVien != null) {
                 NhanVienDetailsDialog editDialog = new NhanVienDetailsDialog(dummyFrame, sampleNhanVien); // <-- Corrected class name
                 editDialog.setVisible(true);
             } else {
                 System.out.println("Không tìm thấy nhân viên mẫu để cập nhật. Mở dialog thêm mới.");
                 NhanVienDetailsDialog addDialog = new NhanVienDetailsDialog(dummyFrame); // <-- Corrected class name
                 addDialog.setVisible(true);
             }

            dummyFrame.dispose();
        });
    }

}
