package ui;

import dao.LoaiDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Loai;
import model.NhanVien;
// Map is not directly used in this UI logic, can remove if not needed
// import java.util.Map;


/**
 * Giao diện người dùng cho chức năng Quản lý Loại sản phẩm.
 * Panel này hiển thị thông tin loại sản phẩm, cho phép quản lý (thêm, sửa, xóa).
 * Tích hợp phân quyền cơ bản dựa trên vai trò của NhanVien đăng nhập.
 */
public class LoaiUI extends JPanel {

    // Define colors (reusing the palette)
    Color coffeeBrown = new Color(102, 51, 0);
    Color lightBeige = new Color(245, 245, 220);
    Color accentGreen = new Color(60, 179, 113);
    Color accentOrange = new Color(255, 165, 0);
    Color darkGray = new Color(50, 50, 50);
    // accentBlue is not used in this UI, but can keep for consistency if needed
    // Color accentBlue = new Color(30, 144, 255);


    // UI Components
    private JTextField txtMaloai;
    private JTextField txtTenloai;
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnClear;
    // Optional: Add search components if needed
    // private JButton btnSearch;
    // private JTextField txtSearch;

    private JTable loaiTable;
    private DefaultTableModel tableModel;

    // Data Access Object
    private LoaiDAO loaiDAO;

    // Thông tin nhân viên đã đăng nhập - Changed from ACC to NhanVien
    private NhanVien loggedInUser;


    /**
     * Constructor nhận đối tượng NhanVien của người dùng đã đăng nhập.
     *
     * @param user Đối tượng NhanVien của người dùng đã đăng nhập.
     */
    public LoaiUI(NhanVien user) { // Constructor accepts NhanVien
        this.loggedInUser = user; // Store the NhanVien object

        // Initialize DAO
        loaiDAO = new LoaiDAO();

        // Set layout for the main panel
        setLayout(new BorderLayout(15, 15));
        setBackground(lightBeige);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Top Panel (Input and Buttons) ---
        JPanel topPanel = new JPanel(new BorderLayout(15, 15)); // Use BorderLayout
        topPanel.setBackground(lightBeige);

        // --- Input Fields Panel ---
        JPanel inputFieldsPanel = new JPanel(new GridBagLayout()); // Using GridBag for alignment
        inputFieldsPanel.setBackground(lightBeige);
        inputFieldsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Thông tin Loại sản phẩm", 0, 0, new Font("Arial", Font.BOLD, 14), coffeeBrown));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Row 0: Mã Loại
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0; // Label doesn't grow horizontally
        inputFieldsPanel.add(createLabel("Mã Loại:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 1.0; // Text field grows horizontally
        txtMaloai = new JTextField(20);
        inputFieldsPanel.add(txtMaloai, gbc);

        // Row 1: Tên Loại
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0; // Label doesn't grow horizontally
        inputFieldsPanel.add(createLabel("Tên Loại:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.weightx = 1.0; // Text field grows horizontally
        txtTenloai = new JTextField(20);
        inputFieldsPanel.add(txtTenloai, gbc);


        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); // Centered FlowLayout
        buttonPanel.setBackground(lightBeige);

        btnAdd = new JButton("Thêm");
        styleButton(btnAdd, accentGreen, Color.WHITE);
        buttonPanel.add(btnAdd);

        btnEdit = new JButton("Sửa");
        styleButton(btnEdit, coffeeBrown, Color.WHITE);
        buttonPanel.add(btnEdit);

        btnDelete = new JButton("Xóa");
        styleButton(btnDelete, accentOrange, Color.WHITE);
        buttonPanel.add(btnDelete);

        btnClear = new JButton("Làm mới");
        styleButton(btnClear, darkGray, Color.WHITE);
        buttonPanel.add(btnClear);

        // Optional: Add Search components if using search
        // JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        // searchPanel.setBackground(lightBeige);
        // searchPanel.add(createLabel("Tìm theo tên:"));
        // txtSearch = new JTextField(15);
        // searchPanel.add(txtSearch);
        // btnSearch = new JButton("Tìm kiếm");
        // styleButton(btnSearch, coffeeBrown, Color.WHITE);
        // searchPanel.add(btnSearch);


        // Add input and button panels to the top panel
        topPanel.add(inputFieldsPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH); // Buttons below input fields
        // Optional: topPanel.add(searchPanel, BorderLayout.NORTH); // Search above input fields


        add(topPanel, BorderLayout.NORTH);


        // --- Table Panel ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(lightBeige);
        tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Danh sách Loại sản phẩm", 0, 0, new Font("Arial", Font.BOLD, 14), coffeeBrown));

        // Table Model
        tableModel = new DefaultTableModel(new Object[]{"Mã Loại", "Tên Loại"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };
        loaiTable = new JTable(tableModel);

        // Style table header and cells
        loaiTable.getTableHeader().setBackground(coffeeBrown);
        loaiTable.getTableHeader().setForeground(Color.WHITE);
        loaiTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(230, 230, 230));
                if (isSelected) {
                     c.setBackground(new Color(180, 210, 230));
                }
                // Default alignment is left, which is fine for these columns
                // setHorizontalAlignment(SwingConstants.LEFT);
                return c;
            }
        };
        for (int i = 0; i < loaiTable.getColumnCount(); i++) {
            loaiTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(loaiTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);


        // --- Event Listeners ---

        btnAdd.addActionListener(e -> addLoai()); // Using lambda expressions
        btnEdit.addActionListener(e -> updateLoai());
        btnDelete.addActionListener(e -> deleteLoai());
        btnClear.addActionListener(e -> clearInputFields());
        // Optional: btnSearch.addActionListener(e -> searchLoai());

        loaiTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fillInputFieldsFromTable();
                // Re-apply permissions after selecting a row to enable/disable Edit/Delete
                applyRolePermissions();
            }
        });

        // Load initial data
        loadLoaiTable();

        // Apply permissions based on logged-in user role
        applyRolePermissions(); // Call the implemented method
    }

    /**
     * Applies role-based permissions to UI components (buttons, input fields)
     * based on the logged-in NhanVien's role.
     */
    private void applyRolePermissions() {
        // Mặc định vô hiệu hóa các nút Thêm, Sửa, Xóa và các trường nhập liệu
        btnAdd.setEnabled(false);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
        txtMaloai.setEnabled(false); // Mã loại chỉ cho phép chỉnh sửa khi thêm mới
        txtTenloai.setEnabled(false); // Tên loại chỉ cho phép chỉnh sửa khi thêm mới

        // Nút Làm mới (Clear) và bảng thường luôn được phép
        btnClear.setEnabled(true);
        loaiTable.setEnabled(true); // Table should always be viewable


        // Optional: If search is implemented, enable search components here
        // btnSearch.setEnabled(true);
        // txtSearch.setEnabled(true);


        // Use loggedInUser instead of loggedInAccount
        if (loggedInUser == null) {
            // Không có nhân viên đăng nhập, chỉ cho phép xem (nếu có module xem riêng)
            // Các nút và trường đã disabled mặc định
            return;
        }

        String role = loggedInUser.getRole(); // Lấy vai trò từ NhanVien

        // Logic phân quyền cho module Quản lý Loại sản phẩm
        // Ví dụ: Chỉ Admin và Manager có quyền thêm, sửa, xóa loại sản phẩm. Staff chỉ được xem.
        if ("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role)) {
            // Admin và Manager có toàn quyền Thêm, Sửa, Xóa
            btnAdd.setEnabled(true);
            // Nút Sửa/Xóa chỉ được bật khi có dòng trên bảng được chọn
            btnEdit.setEnabled(loaiTable.getSelectedRow() >= 0);
            btnDelete.setEnabled(loaiTable.getSelectedRow() >= 0);

            // Cho phép chỉnh sửa các trường nhập liệu
            txtMaloai.setEnabled(true); // Enable Maloai for adding new
            txtTenloai.setEnabled(true);

            // Mã loại chỉ cho phép chỉnh sửa khi thêm mới (không có dòng nào được chọn)
            txtMaloai.setEnabled(loaiTable.getSelectedRow() < 0);

        } else {
            // Các vai trò khác (Staff, Guest, ...) chỉ được xem
            // Các nút và trường sẽ giữ trạng thái disabled ban đầu
        }
    }


    // Helper method to create styled labels
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.BOLD, 12)); // Changed to Bold for labels
        return label;
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

    // --- CRUD Operations ---

    /**
     * Loads all Loai data from the DAO and populates the loai table.
     */
    private void loadLoaiTable() {
        tableModel.setRowCount(0); // Clear existing data
        List<Loai> danhSach = loaiDAO.getAllLoai(); // Assuming getAllLoai exists and returns List<Loai>
        if (danhSach != null) {
             for (Loai loai : danhSach) {
                 tableModel.addRow(new Object[]{loai.getMaloai(), loai.getTenloai()});
             }
        } else {
             System.err.println("Không lấy được danh sách loại sản phẩm từ CSDL.");
             JOptionPane.showMessageDialog(this, "Không thể tải danh sách loại sản phẩm từ CSDL.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
             // Optional: Show an empty row or a message in the table/UI
             // tableModel.addRow(new Object[]{"", "Lỗi tải dữ liệu"});
        }
        // After loading table, re-apply permissions to ensure edit/delete buttons are correctly enabled/disabled
        applyRolePermissions(); // Re-apply permissions
        clearInputFields(); // Clear input fields after loading
        loaiTable.clearSelection(); // Clear table selection
    }

    /**
     * Handles the Add Loai action.
     * Validates input, creates a Loai object, and calls the DAO to add it.
     */
    private void addLoai() {
        // Check permissions before performing action
        if (loggedInUser == null || (!"Admin".equalsIgnoreCase(loggedInUser.getRole()) && !"Manager".equalsIgnoreCase(loggedInUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền thêm loại sản phẩm.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String maloai = txtMaloai.getText().trim();
        String tenloai = txtTenloai.getText().trim();

        // Basic validation
        if (maloai.isEmpty() || tenloai.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Mã loại và Tên loại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if Maloai already exists (Recommended)
        if (loaiDAO.getLoaiById(maloai) != null) { // Assuming getLoaiById exists in LoaiDAO
             JOptionPane.showMessageDialog(this, "Mã loại đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
             return;
        }

        Loai newLoai = new Loai(maloai, tenloai);
        boolean success = loaiDAO.addLoai(newLoai); // Assuming addLoai returns boolean

        if (success) {
            JOptionPane.showMessageDialog(this, "Thêm loại sản phẩm thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadLoaiTable(); // Refresh table
            clearInputFields(); // Clear input fields
            applyRolePermissions(); // Re-apply permissions (esp. for Maloai field)
        } else {
             JOptionPane.showMessageDialog(this, "Thêm loại sản phẩm thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
             // Log error in DAO should provide details
        }
    }

    /**
     * Handles the Update Loai action.
     * Validates input, creates a Loai object, and calls the DAO to update it.
     */
    private void updateLoai() {
        // Check permissions before performing action
        if (loggedInUser == null || (!"Admin".equalsIgnoreCase(loggedInUser.getRole()) && !"Manager".equalsIgnoreCase(loggedInUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền sửa loại sản phẩm.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String maloai = txtMaloai.getText().trim(); // Maloai is typically the key, should not be edited after selection
        String tenloai = txtTenloai.getText().trim();

        // Basic validation
        if (maloai.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Vui lòng chọn loại sản phẩm để sửa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
             return;
        }
         if (tenloai.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Vui lòng nhập Tên loại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
             return;
         }


        Loai updatedLoai = new Loai(maloai, tenloai);
        boolean success = loaiDAO.updateLoai(updatedLoai); // Assuming updateLoai returns boolean

        if (success) {
            JOptionPane.showMessageDialog(this, "Cập nhật loại sản phẩm thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadLoaiTable(); // Refresh table
            clearInputFields(); // Clear input fields
            applyRolePermissions(); // Re-apply permissions
        } else {
             JOptionPane.showMessageDialog(this, "Cập nhật loại sản phẩm thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
             // Log error in DAO should provide details
        }
    }

    /**
     * Handles the Delete Loai action.
     * Gets the selected Loai and calls the DAO to delete it.
     */
    private void deleteLoai() {
        // Check permissions before performing action
        if (loggedInUser == null || (!"Admin".equalsIgnoreCase(loggedInUser.getRole()) && !"Manager".equalsIgnoreCase(loggedInUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền xóa loại sản phẩm.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String maloai = txtMaloai.getText().trim();

        if (maloai.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn loại sản phẩm để xóa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // TODO: Add check if any SanPham is using this Loai before deleting (important for data integrity)
        // You would need a method in SanPhamDAO like countProductsByLoai(String maloai)
        // Make sure you have a SanPhamDAO instance available in this class if you add this check.
        // private SanPhamDAO sanPhamDAOForCheck = new SanPhamDAO(); // Initialize if needed
        boolean canDelete = true; // Assume true initially
        /*
         try {
             // Assuming sanPhamDAO is available and has countProductsByLoai method
             int productCount = sanPhamDAO.countProductsByLoai(maloai);
             if (productCount > 0) {
                  JOptionPane.showMessageDialog(this, "Không thể xóa loại sản phẩm này vì có " + productCount + " sản phẩm đang sử dụng.", "Lỗi ràng buộc", JOptionPane.ERROR_MESSAGE);
                  canDelete = false;
             }
         } catch (Exception e) {
             System.err.println("Error checking product count for Loai '" + maloai + "': " + e.getMessage());
             e.printStackTrace();
             JOptionPane.showMessageDialog(this, "Lỗi khi kiểm tra ràng buộc dữ liệu.", "Lỗi", JOptionPane.ERROR_MESSAGE);
             canDelete = false; // Assume cannot delete if check fails
         }
        */


        if (canDelete) {
             int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa loại sản phẩm '" + txtTenloai.getText().trim() + "' (Mã: " + maloai + ")?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

             if (confirm == JOptionPane.YES_OPTION) {
                 boolean success = loaiDAO.deleteLoai(maloai); // Assuming deleteLoai returns boolean
                 if (success) {
                     JOptionPane.showMessageDialog(this, "Xóa loại sản phẩm thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                     loadLoaiTable(); // Refresh table
                     clearInputFields(); // Clear input fields
                     applyRolePermissions(); // Re-apply permissions
                 } else {
                     JOptionPane.showMessageDialog(this, "Xóa loại sản phẩm thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                     // Log error in DAO should provide details
                 }
             }
        }
    }

    // Optional: search method if using search components
    // private void searchLoai() {
    //      // Check permissions if search needs restriction (usually not needed for viewing)
    //      String tenLoai = txtSearch.getText().trim();
    //      tableModel.setRowCount(0);
    //      // Assuming searchLoaiByName exists in LoaiDAO and returns List<Loai>
    //      List<Loai> searchResults = loaiDAO.searchLoaiByName(tenLoai);
    //      if (searchResults != null) {
    //           for (Loai loai : searchResults) {
    //                tableModel.addRow(new Object[]{loai.getMaloai(), loai.getTenloai()});
    //           }
    //      } else {
    //           System.err.println("Lỗi khi tìm kiếm loại sản phẩm.");
    //           JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm loại sản phẩm.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
    //      }
    // }

    // --- Helper UI Methods ---

    /**
     * Clears all input fields and resets button/table selection states.
     */
    private void clearInputFields() {
        txtMaloai.setText("");
        txtTenloai.setText("");
        // Re-enable Maloai field for adding new entries
        txtMaloai.setEnabled(true);
        loaiTable.clearSelection(); // Clear table selection
        // Optional: txtSearch.setText("");

        // Re-apply permissions to reset button states based on no row selection
        applyRolePermissions();
    }

    /**
     * Fills the input fields with data from the selected row in the loai table.
     */
    private void fillInputFieldsFromTable() {
        int selectedRow = loaiTable.getSelectedRow();
        if (selectedRow >= 0) {
            String maloai = tableModel.getValueAt(selectedRow, 0).toString();
            String tenloai = tableModel.getValueAt(selectedRow, 1).toString();

            txtMaloai.setText(maloai);
            txtTenloai.setText(tenloai);

            // Disable Maloai field when editing (it's the primary key)
            txtMaloai.setEnabled(false);
        } else {
             // If selection is cleared (e.g., by clicking outside rows), clear fields and enable Maloai
             clearInputFields();
        }
    }

    // --- Main method for testing (Optional) ---

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
             // This test requires a running database with Loai table
             // and the DatabaseConnection configured correctly.
             // Pass a sample NhanVien object for testing permissions

             // Create dummy NhanVien users for testing different roles
             NhanVien dummyAdmin = new NhanVien("NV001", "admin_user", "pass123", "Nguyễn Văn A", "Admin", "0123456789", "admin@example.com", "Địa chỉ 1");
            //  NhanVien dummyManager = new NhanVien("NV002", "manager_user", "pass456", "Trần Thị B", "Manager", "0987654321", "manager@example.com", "Địa chỉ 2", true);
            //  NhanVien dummyStaff = new NhanVien("NV003", "staff_user", "pass789", "Lê Văn C", "Staff", "0111222333", "staff@example.com", "Địa chỉ 3", true);
            //  NhanVien dummyGuest = new NhanVien(null, null, null, "Khách", "Guest", null, null, null, false); // Guest example


             JFrame frame = new JFrame("Quản lý Loại sản phẩm Demo");
             frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             frame.setSize(600, 400); // Smaller size for fewer fields
             frame.setLocationRelativeTo(null);

             // Choose which dummy user to test with
             LoaiUI loaiPanel = new LoaiUI(dummyAdmin); // Test with Admin account
             // LoaiUI loaiPanel = new LoaiUI(dummyManager); // Test with Manager account
             // LoaiUI loaiPanel = new LoaiUI(dummyStaff); // Test with Staff account (should have limited access)
             // LoaiUI loaiPanel = new LoaiUI(dummyGuest); // Test with Guest account (should have no access to CRUD)


             frame.add(loaiPanel);
             frame.setVisible(true);
        });
    }

}
