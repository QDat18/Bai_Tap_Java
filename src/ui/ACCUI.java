package ui;

import dao.ACCDAO;
import dao.NhanVienDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.ACC;
import model.NhanVien; // Import SQLException if needed (e.g. for specific handling like foreign key)

public class ACCUI extends JPanel {

    // Define colors (reusing the palette)
    Color coffeeBrown = new Color(102, 51, 0);
    Color lightBeige = new Color(245, 245, 220);
    Color accentGreen = new Color(60, 179, 113); // Color for Add button
    Color accentOrange = new Color(255, 165, 0); // Color for Delete button
    Color darkGray = new Color(50, 50, 50);
    Color tableRowEven = Color.WHITE; // Define colors for table rows
    Color tableRowOdd = new Color(230, 230, 230);


    // UI Components (Input Fields)
    private JTextField txtTendangnhap;
    private JPasswordField txtMatkhau; // Use JPasswordField for password
    private JTextField txtEmail;
    private JComboBox<String> cbRole; // Use JComboBox for predefined roles
    private JTextField txtChucvu;
    private JTextField txtTenHienThi;
    private JComboBox<NhanVien> cbNhanVien; // <-- COMBOBOX CHỌN NHÂN VIÊN


    // UI Components (Action Buttons)
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnClear;

    // UI Components (Search)
    private JButton btnSearch;
    private JTextField txtSearch;
    private JComboBox<String> cbSearchCriteria;

    // UI Components (Table)
    private JTable accTable;
    private DefaultTableModel tableModel;

    // Data Access Objects
    private ACCDAO accDAO;
    private NhanVienDAO nhanVienDAO;


    // Logged-in user information
    private ACC loggedInAccount;


    // Constructor nhận thông tin tài khoản đã đăng nhập
    public ACCUI(ACC account) {
        this.loggedInAccount = account;

        // Initialize DAOs
        accDAO = new ACCDAO();
        nhanVienDAO = new NhanVienDAO(); // <-- KHỞI TẠO NHANVIENDAO

        // Set layout for the main panel
        setLayout(new BorderLayout(15, 15));
        setBackground(lightBeige);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Top Panel (Input and Search) ---
        JPanel topPanel = new JPanel(new BorderLayout(15, 0));
        topPanel.setBackground(lightBeige);

        // --- Input Fields Panel ---
        JPanel inputFieldsPanel = new JPanel(new GridBagLayout());
        inputFieldsPanel.setBackground(lightBeige);
        inputFieldsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Thông tin Tài khoản", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), coffeeBrown));


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Cần weightx cho các trường nhập liệu để chúng giãn ra

        // Row 0: Tendangnhap
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; inputFieldsPanel.add(createLabel("Tên đăng nhập:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; txtTendangnhap = new JTextField(20); inputFieldsPanel.add(txtTendangnhap, gbc);

        // Row 1: Matkhau
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; inputFieldsPanel.add(createLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; txtMatkhau = new JPasswordField(20); inputFieldsPanel.add(txtMatkhau, gbc);

        // Row 2: Email
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; inputFieldsPanel.add(createLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; txtEmail = new JTextField(20); inputFieldsPanel.add(txtEmail, gbc);

        // Row 3: Role (Sử dụng JComboBox)
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; inputFieldsPanel.add(createLabel("Vai trò (role):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        String[] roles = {"Admin", "Manager", "Staff", "Guest"}; // Define possible roles
        cbRole = new JComboBox<>(roles);
        cbRole.setBackground(Color.WHITE);
        cbRole.setForeground(darkGray);
        inputFieldsPanel.add(cbRole, gbc);

        // Row 4: Chucvu
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; inputFieldsPanel.add(createLabel("Chức vụ:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0; txtChucvu = new JTextField(20); inputFieldsPanel.add(txtChucvu, gbc);

        // Row 5: TenHienThi
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; inputFieldsPanel.add(createLabel("Tên hiển thị:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0; txtTenHienThi = new JTextField(20); inputFieldsPanel.add(txtTenHienThi, gbc);

        // Row 6: NhanVien (MaNV) - Use ComboBox to select NhanVien
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; inputFieldsPanel.add(createLabel("Nhân viên liên kết:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 1.0;
        cbNhanVien = new JComboBox<>(); // Initialize ComboBox
        populateNhanVienComboBox(); // Populate with data from NhanVienDAO
        cbNhanVien.setBackground(Color.WHITE);
        cbNhanVien.setForeground(darkGray);
        inputFieldsPanel.add(cbNhanVien, gbc);


        // --- Action Button Panel (Below Input Fields) ---
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionButtonPanel.setBackground(lightBeige);

        btnAdd = new JButton("Thêm");
        styleButton(btnAdd, accentGreen, Color.WHITE);
        btnAdd.addActionListener(e -> addAccount());
        actionButtonPanel.add(btnAdd);

        btnEdit = new JButton("Sửa");
        styleButton(btnEdit, coffeeBrown, Color.WHITE);
        btnEdit.addActionListener(e -> updateAccount());
        actionButtonPanel.add(btnEdit);

        btnDelete = new JButton("Xóa");
        styleButton(btnDelete, accentOrange, Color.WHITE);
        btnDelete.addActionListener(e -> deleteAccount());
        actionButtonPanel.add(btnDelete);

        btnClear = new JButton("Làm mới");
        styleButton(btnClear, darkGray, Color.WHITE);
        btnClear.addActionListener(e -> clearInputFields());
        actionButtonPanel.add(btnClear);


        // Combine Input Fields and Action Buttons vertically
        JPanel inputAndButtonPanel = new JPanel(new BorderLayout());
        inputAndButtonPanel.setBackground(lightBeige);
        inputAndButtonPanel.add(inputFieldsPanel, BorderLayout.CENTER);
        inputAndButtonPanel.add(actionButtonPanel, BorderLayout.SOUTH);


        // --- Search Panel ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(lightBeige);
        searchPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Tìm kiếm Tài khoản", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), coffeeBrown));


        searchPanel.add(createLabel("Tìm theo:"));
        // Thêm "Mã NV" và "Tên NV" vào tùy chọn tìm kiếm
        String[] searchOptions = {"Tên đăng nhập", "Email", "Vai trò", "Chức vụ", "Tên hiển thị", "Mã NV", "Tên NV"};
        cbSearchCriteria = new JComboBox<>(searchOptions);
        cbSearchCriteria.setBackground(Color.WHITE);
        cbSearchCriteria.setForeground(darkGray);
        searchPanel.add(cbSearchCriteria);

        searchPanel.add(createLabel("Từ khóa:"));
        txtSearch = new JTextField(15);
        searchPanel.add(txtSearch);

        btnSearch = new JButton("Tìm kiếm");
        styleButton(btnSearch, coffeeBrown, Color.WHITE);
        btnSearch.addActionListener(e -> searchAccount());
        searchPanel.add(btnSearch);

        // Add combined input/button panel and search panel to the top panel
        topPanel.add(inputAndButtonPanel, BorderLayout.CENTER);
        topPanel.add(searchPanel, BorderLayout.SOUTH);


        add(topPanel, BorderLayout.NORTH);


        // --- Table Panel ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(lightBeige);
        tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Danh sách Tài khoản", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), coffeeBrown));


        // Table Model: Display ACC attributes (EXCLUDING password for security) + MaNV + TenNV
        // Thêm cột "Mã NV" và "Tên NV" vào bảng hiển thị
        tableModel = new DefaultTableModel(new Object[]{"Tên đăng nhập", "Email", "Vai trò (role)", "Chức vụ", "Tên hiển thị", "Mã NV", "Tên NV"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };
        accTable = new JTable(tableModel);

        // Style table header and cells
        setupTableStyle(accTable); // Sử dụng helper method để style bảng


        // Table row selection to fill fields and enable/disable buttons
        accTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fillInputFieldsFromTable();
            }
        });


        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(accTable);
        scrollPane.setBackground(lightBeige); // Match background
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);


        // Load initial data
        loadAccountTable();

        // Apply permissions on initial load
        applyRolePermissions(); // <-- Apply permissions when UI is created
    }

    // Helper method to create styled labels
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
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

     // Helper method to style the table
     private void setupTableStyle(JTable table) {
         table.setFont(new Font("Arial", Font.PLAIN, 12));
         table.setRowHeight(25);
         table.setFillsViewportHeight(true);

         // Header styling
         JTableHeader header = table.getTableHeader();
         header.setBackground(coffeeBrown);
         header.setForeground(Color.WHITE);
         header.setFont(new Font("Arial", Font.BOLD, 12));
         header.setReorderingAllowed(false);

         // Cell alignment and alternating row colors
         // Create renderers once
         DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
         centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

         DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
         leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);


         table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
             @Override
             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                 Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 c.setBackground(row % 2 == 0 ? tableRowEven : tableRowOdd);
                 if (isSelected) {
                     c.setBackground(new Color(180, 210, 230)); // Selection color
                 }

                 // Apply alignment based on column index (safer if column names change slightly)
                 // Adjust indices based on your table model column order:
                 // 0: Tendangnhap (LEFT)
                 // 1: Email (LEFT)
                 // 2: Role (LEFT)
                 // 3: Chucvu (LEFT)
                 // 4: TenHienThi (LEFT)
                 // 5: MaNV (CENTER)
                 // 6: TenNV (LEFT)

                 switch (column) {
                     case 5: // MaNV column index
                         setHorizontalAlignment(SwingConstants.CENTER);
                         break;
                     default: // Other columns (Tendangnhap, Email, Role, Chucvu, TenHienThi, TenNV)
                         setHorizontalAlignment(SwingConstants.LEFT);
                         break;
                 }


                 setText(value != null ? value.toString() : ""); // Set text

                 return c;
             }
         });
     }


    // --- Role-based Permissions ---
    private void applyRolePermissions() {
        // Mặc định vô hiệu hóa các nút Thêm, Sửa, Xóa
        btnAdd.setEnabled(false);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
        // Nút Làm mới (Clear) và Tìm kiếm (Search) thường luôn được phép
        btnClear.setEnabled(true);
        btnSearch.setEnabled(true);
        txtSearch.setEnabled(true);
        cbSearchCriteria.setEnabled(true);
        // Các trường nhập liệu cũng có thể bị vô hiệu hóa nếu không có quyền
        setAllInputFieldsEnabled(false); // Vô hiệu hóa tất cả trường nhập liệu mặc định

        if (loggedInAccount == null) {
            // Không có tài khoản đăng nhập (trường hợp này không xảy ra nếu chỉ mở qua MainFrame)
            return;
        }

        String role = loggedInAccount.getRole(); // Lấy vai trò

        // Logic phân quyền cho module Quản lý Tài khoản (ACCUI)
        // Ví dụ: Chỉ Admin mới có toàn quyền CRUD và chỉnh sửa thông tin tài khoản
        if ("Admin".equalsIgnoreCase(role)) {
            // Admin có toàn quyền Thêm, Sửa, Xóa
            btnAdd.setEnabled(true);
            // Nút Sửa/Xóa chỉ được bật khi có dòng trên bảng được chọn VÀ Admin không tự xóa tài khoản của mình
            int selectedRow = accTable.getSelectedRow();
            boolean isRowSelected = selectedRow >= 0;
            boolean isCurrentUser = false;
            if(isRowSelected) {
                String selectedUsername = tableModel.getValueAt(selectedRow, 0).toString();
                if (loggedInAccount.getTendangnhap().equals(selectedUsername)) {
                    isCurrentUser = true; // Đang chọn tài khoản của chính Admin
                }
            }

            btnEdit.setEnabled(isRowSelected); // Admin có thể sửa nếu có dòng chọn
            btnDelete.setEnabled(isRowSelected && !isCurrentUser); // Admin không thể xóa chính mình

            setAllInputFieldsEnabled(true); // Admin có thể chỉnh sửa tất cả các trường
            // Trường tên đăng nhập chỉ cho phép chỉnh sửa khi thêm mới (không có dòng nào được chọn)
            txtTendangnhap.setEnabled(selectedRow < 0); // Cho phép nhập Tên đăng nhập chỉ khi thêm mới


        } else {
            // Các vai trò khác (Manager, Staff, ...) không có quyền quản lý tài khoản
            // Các nút Thêm, Sửa, Xóa sẽ giữ nguyên trạng thái false từ đầu
            // Các trường nhập liệu cũng sẽ giữ nguyên trạng thái false
            // Riêng với vai trò Manager hoặc Staff, có thể cho phép họ chỉnh sửa thông tin cá nhân của chính họ
            int selectedRow = accTable.getSelectedRow();
            if (selectedRow >= 0) {
                String selectedUsername = tableModel.getValueAt(selectedRow, 0).toString();
                // Kiểm tra xem tài khoản được chọn có phải của người dùng đang đăng nhập không
                if (loggedInAccount.getTendangnhap().equals(selectedUsername)) {
                    // Cho phép họ sửa thông tin cá nhân (ví dụ: Email, Mật khẩu, Tên hiển thị)
                    setAllInputFieldsEnabled(true);
                    txtTendangnhap.setEnabled(false); // Không cho sửa tên đăng nhập
                    cbRole.setEnabled(false); // Không cho sửa role
                    txtChucvu.setEnabled(false); // Không cho sửa chức vụ
                    cbNhanVien.setEnabled(false); // Không cho sửa liên kết nhân viên
                    // Nút Sửa chỉ bật khi chọn chính mình
                    btnEdit.setEnabled(true);
                    btnDelete.setEnabled(false); // Không cho xóa chính mình
                }
            }
        }
    }

     // Helper method để bật/tắt tất cả các trường nhập liệu
     private void setAllInputFieldsEnabled(boolean enabled) {
         txtTendangnhap.setEnabled(enabled);
         txtMatkhau.setEnabled(enabled);
         txtEmail.setEnabled(enabled);
         cbRole.setEnabled(enabled); // Bật tắt combobox Role
         txtChucvu.setEnabled(enabled);
         txtTenHienThi.setEnabled(enabled);
         cbNhanVien.setEnabled(enabled); // <-- Bật tắt combobox NhanVien
     }


    // Populate NhanVien ComboBox
     private void populateNhanVienComboBox() {
         List<NhanVien> nhanVienList = nhanVienDAO.getAllNhanVien(); // Lấy danh sách nhân viên từ DAO
         cbNhanVien.removeAllItems();
         // Thêm một item rỗng hoặc "Chọn nhân viên" cho trường hợp tài khoản không liên kết với NV cụ thể (nếu CSDL cho phép MaNV NULL)
         cbNhanVien.addItem(null); // Thêm null item
         if (nhanVienList != null) {
             for (NhanVien nv : nhanVienList) {
                 cbNhanVien.addItem(nv); // Add NhanVien objects
             }
         }
         // Set default renderer to display MaNV và TenNV
         cbNhanVien.setRenderer(new DefaultListCellRenderer() {
             @Override
             public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                 super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                 if (value instanceof NhanVien) {
                     // Hiển thị MaNV và TenNV
                     setText(((NhanVien) value).getMaNV() + " - " + ((NhanVien) value).getTenNV());
                 } else if (value == null) {
                     setText("-- Chọn Nhân viên --"); // Text cho item null
                 }
                 return this;
             }
         });
         cbNhanVien.setSelectedItem(null); // Set initial selection to null/placeholder
     }

     // Helper to set a specific NhanVien item in the combo box based on MaNV
      private void setNhanVienComboBoxSelectedItem(String maNV) {
          if (maNV == null || maNV.isEmpty()) {
              cbNhanVien.setSelectedItem(null); // Chọn item null nếu MaNV rỗng
              return;
          }
          for (int i = 0; i < cbNhanVien.getItemCount(); i++) {
              Object itemObject = cbNhanVien.getItemAt(i);
              if (itemObject instanceof NhanVien) { // Check if the item is a NhanVien object
                 NhanVien item = (NhanVien) itemObject;
                 if (item.getMaNV().equals(maNV)) {
                     cbNhanVien.setSelectedItem(item);
                     return;
                 }
              }
          }
           // Nếu không tìm thấy nhân viên với MaNV này trong combobox (ví dụ: dữ liệu CSDL không nhất quán)
           cbNhanVien.setSelectedItem(null);
           System.err.println("DEBUG (ACCUI): Không tìm thấy Nhân viên có Mã NV '" + maNV + "' trong danh sách populate ComboBox.");
      }


    // --- CRUD Operations ---

    private void loadAccountTable() {
        tableModel.setRowCount(0); // Clear existing data
        List<ACC> danhSach = accDAO.getAllAccounts(); // Sử dụng phương thức getAllAccounts() từ ACCDAO

        if (danhSach != null) {
            for (ACC acc : danhSach) {
                // Lấy tên nhân viên tương ứng với MaNV từ NhanVienDAO để hiển thị
                String tenNV = "N/A"; // Default value
                 if (acc.getMaNV() != null && !acc.getMaNV().isEmpty()) {
                     NhanVien nv = nhanVienDAO.getNhanVienById(acc.getMaNV()); // Cần phương thức getNhanVienById trong NhanVienDAO
                     if (nv != null) {
                         tenNV = nv.getTenNV();
                     }
                 }

                Vector<Object> row = new Vector<>();
                row.add(acc.getTendangnhap());
                row.add(acc.getEmail());
                row.add(acc.getRole());
                row.add(acc.getChucvu());
                row.add(acc.getTenHienThi());
                row.add(acc.getMaNV()); // <-- THÊM CỘT MaNV VÀO BẢNG
                row.add(tenNV); // <-- THÊM CỘT Tên NV VÀO BẢNG
                tableModel.addRow(row);
            }
        } else {
            // Xử lý trường hợp không lấy được dữ liệu từ DAO
            System.err.println("Không lấy được danh sách tài khoản từ ACCDAO.");
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách tài khoản.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }

        // Sau khi load bảng, áp dụng lại phân quyền để bật/tắt nút Sửa/Xóa dựa trên lựa chọn (ban đầu chưa có lựa chọn)
        applyRolePermissions();
    }

    private void addAccount() {
        // Kiểm tra quyền trước khi thực hiện
        if (loggedInAccount == null || !"Admin".equalsIgnoreCase(loggedInAccount.getRole())) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền thêm tài khoản.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String tendangnhap = txtTendangnhap.getText().trim();
        String matkhau = new String(txtMatkhau.getPassword()).trim(); // Get password
        String email = txtEmail.getText().trim();
        String role = (String) cbRole.getSelectedItem(); // Lấy giá trị từ ComboBox
        String chucvu = txtChucvu.getText().trim();
        String tenHienThi = txtTenHienThi.getText().trim();
        NhanVien selectedNV = (NhanVien) cbNhanVien.getSelectedItem(); // Lấy đối tượng NhanVien được chọn
        String maNV = (selectedNV != null) ? selectedNV.getMaNV() : null; // Lấy MaNV từ đối tượng NhanVien (có thể null)


        // Basic validation
        if (tendangnhap.isEmpty() || matkhau.isEmpty() || role == null || role.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên đăng nhập, Mật khẩu và Vai trò.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // TODO: Add more validation (e.g., valid email format)

        // Check if username already exists before attempting to add using ACCDAO
         if (accDAO.getAccountByUsername(tendangnhap) != null) {
              JOptionPane.showMessageDialog(this, "Tên đăng nhập '" + tendangnhap + "' đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
              return;
         }


        ACC newAccount = new ACC();
        newAccount.setTendangnhap(tendangnhap);
        newAccount.setMatkhau(matkhau); // Cần băm mật khẩu ở đây trong ứng dụng thực tế
        newAccount.setEmail(email);
        newAccount.setRole(role);
        newAccount.setChucvu(chucvu);
        newAccount.setTenHienThi(tenHienThi);
        newAccount.setMaNV(maNV); // <-- SET MA NV VÀO ĐỐI TƯỢNG ACC


        // Sử dụng phương thức addAccount() từ ACCDAO
        try {
            accDAO.addAccount(newAccount); // Call actual DAO method
            JOptionPane.showMessageDialog(this, "Thêm tài khoản thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadAccountTable(); // Refresh table
            clearInputFields(); // Clear input fields
        } catch (Exception e) { // <-- Catching general Exception to handle potential issues from DAO
            System.err.println("Lỗi khi gọi DAO addAccount:");
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Thêm tài khoản thất bại. Lỗi CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAccount() {
        // Kiểm tra quyền trước khi thực hiện
        if (loggedInAccount == null || !"Admin".equalsIgnoreCase(loggedInAccount.getRole())) {
            // Nếu không phải Admin, kiểm tra xem có phải đang sửa tài khoản của chính mình không
            int selectedRow = accTable.getSelectedRow();
            if (selectedRow < 0) { // Không có dòng nào được chọn
                JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản để sửa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String selectedUsername = tableModel.getValueAt(selectedRow, 0).toString();
            if (!loggedInAccount.getTendangnhap().equals(selectedUsername)) {
                // Không phải Admin và không phải tài khoản của chính mình
                JOptionPane.showMessageDialog(this, "Bạn không có quyền sửa tài khoản này.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Nếu là Manager/Staff sửa tài khoản của mình, logic tiếp tục
        }

        // Need a selected account to update
        int selectedRow = accTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản để sửa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tendangnhap = txtTendangnhap.getText().trim(); // Username is primary key (get from field which was filled from table)
        String matkhau = new String(txtMatkhau.getPassword()).trim(); // Get password (can be new or old). If left blank, keep old? Or require change?
        String email = txtEmail.getText().trim();
        String role = (String) cbRole.getSelectedItem(); // Lấy giá trị từ ComboBox
        String chucvu = txtChucvu.getText().trim();
        String tenHienThi = txtTenHienThi.getText().trim();
        NhanVien selectedNV = (NhanVien) cbNhanVien.getSelectedItem(); // Lấy đối tượng NhanVien được chọn
        String maNV = (selectedNV != null) ? selectedNV.getMaNV() : null; // Lấy MaNV từ đối tượng NhanVien (có thể null)


        // Basic validation for fields that can be updated
        if (matkhau.isEmpty() || role == null || role.isEmpty()) { // Password and role are required for update too
            JOptionPane.showMessageDialog(this, "Mật khẩu và Vai trò không được rỗng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // TODO: Add more validation

        ACC updatedAccount = new ACC();
        updatedAccount.setTendangnhap(tendangnhap); // Set Tendangnhap (primary key)
        updatedAccount.setMatkhau(matkhau); // Set password (cần băm)
        updatedAccount.setEmail(email);
        updatedAccount.setRole(role);
        updatedAccount.setChucvu(chucvu);
        updatedAccount.setTenHienThi(tenHienThi);
        updatedAccount.setMaNV(maNV); // <-- SET MA NV VÀO ĐỐI TƯỢNG ACC KHI UPDATE


        // Sử dụng phương thức updateAccount() từ ACCDAO
        try {
            accDAO.updateAccount(updatedAccount); // Call actual DAO method
            JOptionPane.showMessageDialog(this, "Sửa tài khoản thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadAccountTable(); // Refresh table
            clearInputFields(); // Clear input fields
        } catch (Exception e) { // <-- Catching general Exception
            System.err.println("Lỗi khi gọi DAO updateAccount:");
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Sửa tài khoản thất bại. Lỗi CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAccount() {
        // Kiểm tra quyền trước khi thực hiện
        if (loggedInAccount == null || !"Admin".equalsIgnoreCase(loggedInAccount.getRole())) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền xóa tài khoản.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = accTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản để xóa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String tendangnhap = tableModel.getValueAt(selectedRow, 0).toString(); // Get username from selected row

        // Ngăn Admin tự xóa tài khoản của chính mình
        if (loggedInAccount.getTendangnhap().equals(tendangnhap)) {
            JOptionPane.showMessageDialog(this, "Bạn không thể xóa tài khoản của chính mình.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }


        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa tài khoản '" + tendangnhap + "'?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Sử dụng phương thức deleteAccount() từ ACCDAO
            try {
                accDAO.deleteAccount(tendangnhap); // Call actual DAO method
                JOptionPane.showMessageDialog(this, "Xóa tài khoản thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadAccountTable(); // Refresh table
                clearInputFields(); // Clear input fields
            } catch (Exception e) { // <-- Catch any other Exception
                System.err.println("Lỗi không xác định khi gọi DAO deleteAccount:");
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Xóa tài khoản thất bại. Lỗi không xác định.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

     private void searchAccount() {
         String criteria = (String) cbSearchCriteria.getSelectedItem();
         String searchTerm = txtSearch.getText().trim();

         tableModel.setRowCount(0); // Clear existing data
         accTable.clearSelection(); // Clear table selection
         clearInputFields(); // Clear input fields

         List<ACC> searchResults = new ArrayList<>();
         // **Placeholder search logic (Simple filtering in UI for now - Implement in DAO for efficiency)**
         // Lấy tất cả dữ liệu và lọc trên UI (không hiệu quả với lượng lớn dữ liệu)
         List<ACC> allAccounts = accDAO.getAllAccounts();
         if (allAccounts == null) {
             JOptionPane.showMessageDialog(this, "Không thể thực hiện tìm kiếm do lỗi tải dữ liệu gốc.", "Lỗi", JOptionPane.ERROR_MESSAGE);
             return;
         }

         if (searchTerm.isEmpty()) {
             loadAccountTable(); // If search term is empty, load all
             return;
         }

         // Thực hiện lọc trên danh sách allAccounts
         for (ACC acc : allAccounts) {
             boolean matches = false;
             String valueToSearch = "";
             switch (criteria) {
                 case "Tên đăng nhập":
                     valueToSearch = acc.getTendangnhap();
                     break;
                 case "Email":
                     valueToSearch = acc.getEmail();
                     break;
                 case "Vai trò":
                     valueToSearch = acc.getRole();
                     break;
                 case "Chức vụ":
                     valueToSearch = acc.getChucvu();
                     break;
                 case "Tên hiển thị":
                     valueToSearch = acc.getTenHienThi();
                     break;
                 case "Mã NV":
                     valueToSearch = acc.getMaNV();
                     break;
                 case "Tên NV":
                     // Cần lấy Tên NV từ NhanVienDAO cho từng ACC
                     String tenNV = "N/A";
                     if (acc.getMaNV() != null && !acc.getMaNV().isEmpty()) {
                         NhanVien nv = nhanVienDAO.getNhanVienById(acc.getMaNV());
                         if (nv != null) {
                             tenNV = nv.getTenNV();
                         }
                     }
                     valueToSearch = tenNV; // Sử dụng tên NV đã lấy được
                     break;
             }

             if (valueToSearch != null && valueToSearch.toLowerCase().contains(searchTerm.toLowerCase())) {
                 matches = true;
             }

             if (matches) {
                 searchResults.add(acc);
             }
         }

         // Sau khi lọc, hiển thị kết quả vào bảng
         if (!searchResults.isEmpty()) {
             for (ACC acc : searchResults) {
                 // Lấy tên nhân viên tương ứng với MaNV từ NhanVienDAO để hiển thị
                 String tenNV = "N/A"; // Default value
                 if (acc.getMaNV() != null && !acc.getMaNV().isEmpty()) {
                     NhanVien nv = nhanVienDAO.getNhanVienById(acc.getMaNV());
                     if (nv != null) {
                         tenNV = nv.getTenNV();
                     }
                 }

                 Vector<Object> row = new Vector<>();
                 row.add(acc.getTendangnhap());
                 row.add(acc.getEmail());
                 row.add(acc.getRole());
                 row.add(acc.getChucvu());
                 row.add(acc.getTenHienThi());
                 row.add(acc.getMaNV());
                 row.add(tenNV);
                 tableModel.addRow(row);
             }
             JOptionPane.showMessageDialog(this, "Tìm thấy " + searchResults.size() + " kết quả.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
         } else {
             JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả nào cho '" + searchTerm + "'", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
         }


         // Disable edit/delete after search results are loaded and selection is cleared
         btnEdit.setEnabled(false);
         btnDelete.setEnabled(false);
         // Re-apply permissions based on login role
         applyRolePermissions(); // Ensures buttons stay disabled if user is not Admin or not selecting their own account
     }


    // --- Helper UI Methods ---

    private void clearInputFields() {
        txtTendangnhap.setText("");
        txtMatkhau.setText(""); // Clear password field
        txtEmail.setText("");
        cbRole.setSelectedItem(null); // Clear selected role in combobox
        txtChucvu.setText("");
        txtTenHienThi.setText("");
        cbNhanVien.setSelectedItem(null); // <-- Clear selected NhanVien in combobox
        txtSearch.setText(""); // Clear search field too
        cbSearchCriteria.setSelectedIndex(0); // Reset search criteria

        // Reset button states and selection
        accTable.clearSelection(); // Clear table selection
        // Enable/disable buttons based on permissions (no row selected means Add is possible for Admin)
        applyRolePermissions(); // Re-apply permissions
        // Ensure input fields are enabled for a new entry (Add mode)
        setAllInputFieldsEnabled(true);
        // Tendangnhap should be enabled when clearing for a new entry
        txtTendangnhap.setEnabled(true);

    }

    private void fillInputFieldsFromTable() {
        int selectedRow = accTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Get Tendangnhap from the selected row (it's the primary key in the table)
            String tendangnhap = tableModel.getValueAt(selectedRow, 0).toString();

            // Fetch the full ACC object from the database using getAccountByUsername
            // This is necessary to get the Matkhau (if needed for comparison, though usually not loaded)
            // and crucially, the MaNV, which might not be directly visible in the table model initially
            ACC selectedAccountFromDB = accDAO.getAccountByUsername(tendangnhap);

            if (selectedAccountFromDB != null) {
                txtTendangnhap.setText(selectedAccountFromDB.getTendangnhap());
                txtMatkhau.setText(""); // Do NOT load password from DB into field, leave blank or handle hashing
                txtEmail.setText(selectedAccountFromDB.getEmail());
                // Set selected item in Role ComboBox
                cbRole.setSelectedItem(selectedAccountFromDB.getRole());
                txtChucvu.setText(selectedAccountFromDB.getChucvu());
                txtTenHienThi.setText(selectedAccountFromDB.getTenHienThi());

                // Set selected item in NhanVien ComboBox using MaNV from the fetched ACC object
                setNhanVienComboBoxSelectedItem(selectedAccountFromDB.getMaNV()); // <-- SET NHAN VIEN DỰA TRÊN MA NV


                // When a row is selected for editing/deleting:
                // - Tendangnhap field should be disabled (it's the primary key)
                // - All other input fields should be enabled (permissions permitting)
                setAllInputFieldsEnabled(true); // Enable all fields for editing
                txtTendangnhap.setEnabled(false); // Disable editing primary key

                // Apply permissions to control Edit/Delete button state based on role and selected row
                applyRolePermissions(); // This will check role and enable Edit/Delete if Admin (and not self) or Manager/Staff (for self)

            } else {
                // Handle case where the account selected in the table is not found in the database
                System.err.println("DEBUG (ACCUI): Tài khoản '" + tendangnhap + "' được chọn trong bảng nhưng không tìm thấy trong DB.");
                JOptionPane.showMessageDialog(this, "Lỗi dữ liệu: Không tìm thấy thông tin tài khoản trong CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                clearInputFields(); // Clear fields if data is inconsistent
            }


        } else {
            // Clear fields and reset button states if no row is selected
            clearInputFields(); // This will also re-apply permissions
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // This test requires a running database with ACC and NhanVien data
            // and the ACCDAO/NhanVienDAO/DatabaseConnection configured correctly.
            // Pass a sample ACC object for testing permissions (e.g., an Admin account)
            // Đảm bảo MaNV trong sampleAdminAccount TỒN TẠI trong bảng NhanVien trong DB test
            ACC sampleAdminAccount = new ACC();
            sampleAdminAccount.setTendangnhap("admin");
            sampleAdminAccount.setMatkhau("password"); // Replace with hashed password if using
            sampleAdminAccount.setEmail("admin@example.com");
            sampleAdminAccount.setRole("Admin");
            sampleAdminAccount.setChucvu("Manager");
            sampleAdminAccount.setTenHienThi("Admin User");
            sampleAdminAccount.setMaNV("NV001"); // <-- THAY BẰNG MA NV THẬT CỦA ADMIN TRONG DB TEST

            // ACC sampleStaffAccount = new ACC();
            // sampleStaffAccount.setTendangnhap("staff1");
            // sampleStaffAccount.setMatkhau("staff123");
            // sampleStaffAccount.setEmail("staff1@cafe.com");
            // sampleStaffAccount.setRole("Staff");
            // sampleStaffAccount.setChucvu("Staff");
            // sampleStaffAccount.setTenHienThi("Nguyễn Văn A (NV)");
            // sampleStaffAccount.setMaNV("NV001"); // <-- THAY BẰNG MA NV THẬT CỦA STAFF TRONG DB TEST


            JFrame frame = new JFrame("Quản lý Tài khoản Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 650); // Adjusted size to fit more fields
            frame.setLocationRelativeTo(null);
            frame.add(new ACCUI(sampleAdminAccount)); // Pass the sample account (Admin)
            // frame.add(new ACCUI(sampleStaffAccount)); // Test with Staff account
            frame.setVisible(true);
        });
    }

}