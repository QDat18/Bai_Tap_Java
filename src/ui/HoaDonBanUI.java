package ui;

import dao.HoaDonBanDAO;
import dao.NhanVienDAO; // Import NhanVienDAO for search criteria combo box (if needed)
import java.awt.*; // Import KhachHangDAO for search criteria combo box (if needed)
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent; // Needed for row selection and double-click
import java.text.SimpleDateFormat; // Needed for row selection and double-click
import java.util.ArrayList; // For date formatting
import java.util.Date;
import java.util.List; // For date handling
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.HoaDonBan;
import model.NhanVien; // Import NhanVien model


public class HoaDonBanUI extends JPanel {

    // Define colors (reusing the palette)
    Color coffeeBrown = new Color(102, 51, 0);
    Color lightBeige = new Color(245, 245, 220);
    Color accentGreen = new Color(60, 179, 113); // Color for Create button
    Color accentOrange = new Color(255, 165, 0); // Color for Delete button
    Color accentBlue = new Color(30, 144, 255); // Color for View Details button
    Color darkGray = new Color(50, 50, 50);

    // UI Components
    private JButton btnCreateNew;
    private JButton btnViewDetails;
    private JButton btnDelete;
    private JButton btnRefresh; // Added Refresh button

    // Search Components
    private JPanel searchPanel;
    private JComboBox<String> cbSearchCriteria;
    private JTextField txtSearchTerm; // For text-based search
    private JButton btnSearch;

    // For Date search criteria (optional, can use JTextField and specific format)
    // private JFormattedTextField txtSearchDate;
    // private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); // Match your date format


    private JTable hoaDonBanTable;
    private DefaultTableModel tableModel;

    private HoaDonBanDAO hoaDonBanDAO;
     // DAOs for populating search comboboxes (optional, could search by text name directly)
     // Removed these as they were not used in the search logic provided,
     // but keep if you plan to populate search comboboxes dynamically or use them otherwise.
     // private NhanVienDAO nhanVienDAO;
     // private KhachHangDAO khachHangDAO;


    // FIX: Change type from ACC to NhanVien
    private NhanVien currentUser; // Thông tin nhân viên đã đăng nhập (thay vì ACC)

    // Date formatter for table display
    private SimpleDateFormat tableDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); // Adjust format as needed


    // FIX: Change parameter type from ACC to NhanVien
    public HoaDonBanUI(NhanVien currentUser) { // Accept NhanVien object
        this.currentUser = currentUser; // Store the logged-in NhanVien object
        // Loại bỏ dòng: this.loggedInAccount = account;

        hoaDonBanDAO = new HoaDonBanDAO();
        // Removed initialization as they were not used in the provided search logic
        // nhanVienDAO = new NhanVienDAO();
        // khachHangDAO = new KhachHangDAO();


        setLayout(new BorderLayout(15, 15));
        setBackground(lightBeige);
        setBorder(new EmptyBorder(15, 15, 15, 15));


        // --- Top Control Panel (Buttons and Search) ---
        JPanel topControlPanel = new JPanel(new BorderLayout(15, 0));
        topControlPanel.setBackground(lightBeige);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setBackground(lightBeige);

        btnCreateNew = new JButton("Tạo Hóa đơn Mới");
        styleButton(btnCreateNew, accentGreen, Color.WHITE);
        buttonPanel.add(btnCreateNew);

        btnViewDetails = new JButton("Xem Chi tiết");
        styleButton(btnViewDetails, accentBlue, Color.WHITE);
        buttonPanel.add(btnViewDetails);

        btnDelete = new JButton("Xóa Hóa đơn");
        styleButton(btnDelete, accentOrange, Color.WHITE);
        buttonPanel.add(btnDelete);

        btnRefresh = new JButton("Làm mới Bảng"); // Added Refresh button
        styleButton(btnRefresh, darkGray, Color.WHITE);
        buttonPanel.add(btnRefresh);


        topControlPanel.add(buttonPanel, BorderLayout.NORTH); // Buttons on top of the control panel


        // Search Panel
        searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(lightBeige);
        searchPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Tìm kiếm Hóa đơn", 0, 0, new Font("Arial", Font.BOLD, 12), coffeeBrown));


        searchPanel.add(createLabel("Tìm theo:"));
        String[] searchOptions = {"Mã HDB", "Ngày bán", "Tên NV", "Tên KH"}; // Search criteria
        cbSearchCriteria = new JComboBox<>(searchOptions);
        cbSearchCriteria.setBackground(Color.WHITE);
        cbSearchCriteria.setForeground(darkGray);
        searchPanel.add(cbSearchCriteria);

         searchPanel.add(createLabel("Từ khóa:"));
        txtSearchTerm = new JTextField(15);
        searchPanel.add(txtSearchTerm);


        btnSearch = new JButton("Tìm kiếm");
        styleButton(btnSearch, coffeeBrown, Color.WHITE);
        searchPanel.add(btnSearch);

        // Add search panel below buttons in the top control panel
        topControlPanel.add(searchPanel, BorderLayout.CENTER);


        add(topControlPanel, BorderLayout.NORTH); // Add the whole control panel to the main UI


        // --- Table Panel ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(lightBeige);
        tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Danh sách Hóa đơn Bán", 0, 0, new Font("Arial", Font.BOLD, 14), coffeeBrown));

        // Table Model: Columns for HoaDonBan (with names)
        tableModel = new DefaultTableModel(new Object[]{"Mã HDB", "Mã NV", "Tên NV", "Mã KH", "Tên KH", "Ngày bán", "Tổng tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
             @Override
             public Class<?> getColumnClass(int columnIndex) {
                  if (columnIndex == 5) return Date.class; // Ngày bán column
                  if (columnIndex == 6) return Integer.class; // Tổng tiền column
                  return super.getColumnClass(columnIndex);
             }
        };
        hoaDonBanTable = new JTable(tableModel);

        // Style table header and cells
        hoaDonBanTable.getTableHeader().setBackground(coffeeBrown);
        hoaDonBanTable.getTableHeader().setForeground(Color.WHITE);
        hoaDonBanTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
             @Override
             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                 final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(230, 230, 230));
                 if (isSelected) {
                      c.setBackground(new Color(180, 210, 230));
                 }
                 // Right align numeric columns (Tongtien)
                 if (column == 6) { // Assuming Tongtien is the 7th column (index 6)
                      setHorizontalAlignment(SwingConstants.RIGHT);
                 } else {
                      setHorizontalAlignment(SwingConstants.LEFT);
                 }
                  // Center align date column (Ngayban)
                  if (column == 5) { // Assuming Ngayban is the 6th column (index 5)
                       setHorizontalAlignment(SwingConstants.CENTER);
                  }
                 return c;
             }

             // Custom rendering for Date objects
             @Override
             protected void setValue(Object value) {
                  if (value instanceof Date) {
                       setText(tableDateFormat.format((Date) value));
                  } else {
                       super.setValue(value);
                  }
             }
        };
         // Apply the renderer to all columns
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
             hoaDonBanTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(hoaDonBanTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);


        // --- Event Listeners ---

        btnCreateNew.addActionListener(e -> openHoaDonBanCreationDialog());
        btnViewDetails.addActionListener(e -> viewHoaDonBanDetails());
        btnDelete.addActionListener(e -> deleteHoaDonBan());
        btnRefresh.addActionListener(e -> loadHoaDonBanTable()); // Listener for Refresh
        btnSearch.addActionListener(e -> searchHoaDonBan()); // Listener for Search

        // Table row selection listener (to enable/disable View Details/Delete buttons)
        hoaDonBanTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                 // Re-apply permissions when a row is selected
                 applyRolePermissions();

                 // Handle double-click for viewing details
                 if (e.getClickCount() == 2) {
                      viewHoaDonBanDetails();
                 }
            }
        });


        // Load initial data
        loadHoaDonBanTable();

        // Apply initial permissions
        applyRolePermissions();
    }

    // Method to load data into the table
    private void loadHoaDonBanTable() {
        tableModel.setRowCount(0); // Clear existing data
        List<HoaDonBan> danhSach = hoaDonBanDAO.getAllHoaDonBan(); // Use the updated DAO method
        if (danhSach != null) {
            for (HoaDonBan hdb : danhSach) {
                 tableModel.addRow(new Object[]{
                     hdb.getMaHDB(),
                     hdb.getMaNV(),
                     hdb.getTenNV(), // Display TenNV (assuming DAO joins)
                     hdb.getMaKH(),
                     hdb.getTenKH(), // Display TenKH (assuming DAO joins)
                     hdb.getNgayban(), // Date object
                     hdb.getTongtien() // Total amount
                 });
            }
        } else {
             System.out.println("Không lấy được dữ liệu hóa đơn bán từ CSDL.");
        }

        // After loading table, re-apply permissions to ensure buttons are correctly enabled/disabled
        applyRolePermissions();
        hoaDonBanTable.clearSelection(); // Clear selection after refresh
    }

    // Method to open the HoaDonBan creation/edit dialog
    private void openHoaDonBanCreationDialog() {
        // Check permissions before opening
        // FIX: Check role from currentUser (NhanVien)
        if (currentUser == null || (!"Admin".equalsIgnoreCase(currentUser.getRole()) && !"Manager".equalsIgnoreCase(currentUser.getRole()) && !"Staff".equalsIgnoreCase(currentUser.getRole()))) {
             JOptionPane.showMessageDialog(this, "Bạn không có quyền tạo hóa đơn bán.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
             return;
        }

        // Get the parent frame to make the dialog modal
        Frame owner = JOptionPane.getFrameForComponent(this);
        if (owner instanceof JFrame) {
             // Create and show the creation dialog
             // FIX: Pass the currentUser (NhanVien object) to the dialog
             // Assuming HoaDonBanCreationDialog has a constructor like HoaDonBanCreationDialog(JFrame owner, NhanVien creator)
             HoaDonBanCreationDialog creationDialog = new HoaDonBanCreationDialog((JFrame) owner, this.currentUser); // Pass NhanVien object
             creationDialog.setVisible(true);

             // After the dialog is closed, refresh the table to show the new invoice
             // Assuming HoaDonBanCreationDialog has an isSavedSuccessfully() method
             if (creationDialog.isSavedSuccessfully()) {
                 loadHoaDonBanTable(); // Refresh the table
             }

        } else {
             System.err.println("Could not get the main JFrame for showing the creation dialog.");
             JOptionPane.showMessageDialog(this, "Không thể mở cửa sổ tạo hóa đơn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to view details of a selected invoice
    private void viewHoaDonBanDetails() {
        int selectedRow = hoaDonBanTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để xem chi tiết.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the MaHDB from the selected row (assuming MaHDB is in the first column, index 0)
        String maHDB = tableModel.getValueAt(selectedRow, 0).toString();

        // Check if the details dialog class exists and open it
        // Get the parent frame to make the dialog modal
        Frame owner = JOptionPane.getFrameForComponent(this);
         if (owner instanceof JFrame) {
              // Open the details dialog, passing the parent frame and MaHDB
              // Assuming CTHoaDonBanDetailsDialog has a constructor like CTHoaDonBanDetailsDialog(JFrame owner, String maHDB)
              CTHoaDonBanDetailsDialog detailsDialog = new CTHoaDonBanDetailsDialog((JFrame) owner, maHDB);
              detailsDialog.setVisible(true);
         } else {
              System.err.println("Could not get the main JFrame for showing the details dialog.");
              JOptionPane.showMessageDialog(this, "Không thể mở cửa sổ chi tiết hóa đơn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
         }
    }

    // Method to delete a selected invoice
    private void deleteHoaDonBan() {
        // Check permissions before deleting
        // FIX: Check role from currentUser (NhanVien)
        if (currentUser == null || (!"Admin".equalsIgnoreCase(currentUser.getRole()) && !"Manager".equalsIgnoreCase(currentUser.getRole()))) {
             JOptionPane.showMessageDialog(this, "Bạn không có quyền xóa hóa đơn bán.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
             return;
        }

        int selectedRow = hoaDonBanTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the MaHDB from the selected row
        String maHDB = tableModel.getValueAt(selectedRow, 0).toString();

        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this,
                 "Bạn có chắc chắn muốn xóa hóa đơn " + maHDB + " này?\nViệc này sẽ cập nhật lại tồn kho sản phẩm.",
                 "Xác nhận xóa",
                 JOptionPane.YES_NO_OPTION,
                 JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Call the DAO method to delete the invoice (header and details) and restore stock
            // Assuming hoaDonBanDAO.deleteHoaDonBan(String maHDB) handles the transaction and stock update
            boolean success = hoaDonBanDAO.deleteHoaDonBan(maHDB); // Use the updated DAO method

            if (success) {
                JOptionPane.showMessageDialog(this, "Xóa hóa đơn thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadHoaDonBanTable(); // Refresh table after deletion
            } else {
                 // Provide a more specific error message if possible from the DAO
                 JOptionPane.showMessageDialog(this, "Xóa hóa đơn thất bại. Vui lòng kiểm tra lại hoặc có lỗi hệ thống (console).", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Method to search for invoices
     private void searchHoaDonBan() {
         // Check permissions (search is usually allowed for roles that can view)
         // FIX: Check role from currentUser (NhanVien) if needed for search permissions
         // if (currentUser == null || (!"Admin".equalsIgnoreCase(currentUser.getRole()) && !"Manager".equalsIgnoreCase(currentUser.getRole()) && !"Staff".equalsIgnoreCase(currentUser.getRole()))) {
         //      JOptionPane.showMessageDialog(this, "Bạn không có quyền tìm kiếm hóa đơn bán.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
         //      return;
         // }

         String criteria = (String) cbSearchCriteria.getSelectedItem();
         String searchTerm = txtSearchTerm.getText().trim();

         // If search term is empty, load all and clear search fields
         if (searchTerm.isEmpty()) {
             loadHoaDonBanTable(); // Load all if search term is empty
             txtSearchTerm.setText("");
             cbSearchCriteria.setSelectedIndex(0);
             return;
         }

         List<HoaDonBan> searchResults = new ArrayList<>();
          try {
              // Call the DAO method to perform the search
              // Assuming hoaDonBanDAO.searchHoaDonBan(String criteria, String searchTerm) exists and works
              searchResults = hoaDonBanDAO.searchHoaDonBan(criteria, searchTerm);
          } catch (Exception e) {
               System.err.println("Lỗi khi gọi DAO searchHoaDonBan: " + e.getMessage());
               e.printStackTrace();
               JOptionPane.showMessageDialog(this, "Tìm kiếm thất bại. Lỗi hệ thống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
               return; // Stop if search fails
          }


         tableModel.setRowCount(0); // Clear existing data before showing search results

         if (searchResults != null && !searchResults.isEmpty()) {
             for (HoaDonBan hdb : searchResults) {
                  tableModel.addRow(new Object[]{
                      hdb.getMaHDB(),
                      hdb.getMaNV(),
                      hdb.getTenNV(), // Display TenNV from search results (assuming DAO joins)
                      hdb.getMaKH(),
                      hdb.getTenKH(), // Display TenKH from search results (assuming DAO joins)
                      hdb.getNgayban(),
                      hdb.getTongtien()
                  });
             }
              JOptionPane.showMessageDialog(this, "Tìm thấy " + searchResults.size() + " kết quả.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
         } else {
              JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả nào cho '" + searchTerm + "'", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
         }

         // After search, re-apply permissions (buttons like View Details/Delete depend on selection)
         applyRolePermissions();
         hoaDonBanTable.clearSelection(); // Clear selection after search results are loaded
     }


    // Method to apply permissions based on the logged-in account role
     private void applyRolePermissions() {
          // Mặc định vô hiệu hóa các nút Create, View, Delete
          btnCreateNew.setEnabled(false); // Disable by default
          btnViewDetails.setEnabled(false); // Disable by default
          btnDelete.setEnabled(false); // Disable by default
          btnRefresh.setEnabled(true); // Refresh is usually allowed
          btnSearch.setEnabled(true); // Search is usually allowed
          txtSearchTerm.setEnabled(true);
          cbSearchCriteria.setEnabled(true);


          // FIX: Check currentUser (NhanVien) instead of loggedInAccount (ACC)
          if (currentUser == null) {
               // Không có thông tin nhân viên đăng nhập
               return;
          }

          String role = currentUser.getRole(); // Lấy vai trò từ đối tượng NhanVien

          // Logic phân quyền cho module Quản lý Hóa đơn Bán
          // Ví dụ: Admin và Manager có toàn quyền, Staff có thể tạo và xem, không xóa
          if ("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role)) {
               // Admin và Manager có toàn quyền
               btnCreateNew.setEnabled(true); // Admin and Manager can create
               // View Details and Delete enabled based on row selection
               btnViewDetails.setEnabled(hoaDonBanTable.getSelectedRow() >= 0);
               btnDelete.setEnabled(hoaDonBanTable.getSelectedRow() >= 0);

          } else if ("Staff".equalsIgnoreCase(role)) {
               // Staff có thể tạo và xem, không xóa
               btnCreateNew.setEnabled(true); // Staff can create
               // View Details enabled based on row selection
               btnViewDetails.setEnabled(hoaDonBanTable.getSelectedRow() >= 0);
               btnDelete.setEnabled(false); // Staff cannot delete

          } else {
               // Các vai trò khác (Guest, etc.) chỉ được xem (nếu có module xem riêng) hoặc không có quyền thao tác
               // Các nút Create, View Details, Delete sẽ giữ trạng thái disabled ban đầu
               // Search and Refresh are already enabled by default
          }

           // Specific override: Prevent deleting/viewing details of invoices created by Admin if current user is not Admin
           // This logic might be needed depending on your business rules
           int selectedRow = hoaDonBanTable.getSelectedRow();
           if (selectedRow >= 0 && !("Admin".equalsIgnoreCase(role))) { // If a row is selected and the current user is not Admin
                // Assuming TenNV column index is 2
                String creatorTenNV = (String) tableModel.getValueAt(selectedRow, 2);
                // You would need a way to know the *role* of the creator, not just their name.
                // This requires storing creator's role in HoaDonBan, or looking up their NhanVien object by MaNV (column index 1).
                String creatorMaNV = (String) tableModel.getValueAt(selectedRow, 1);
                NhanVien creator = null;
                // Create a temporary DAO instance or reuse the existing one (if not removed)
                NhanVienDAO tempNhanVienDAO = new NhanVienDAO(); // Temporary DAO
                creator = tempNhanVienDAO.getNhanVienById(creatorMaNV); // Assuming this method exists

                if (creator != null && "Admin".equalsIgnoreCase(creator.getRole())) {
                     // If the creator is Admin, and the current user is not Admin, disable Update/Delete for this row
                     btnViewDetails.setEnabled(false); // Maybe prevent viewing details too? Depends on rules.
                     btnDelete.setEnabled(false);
                }
           }
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
                           BorderFactory.createLineBorder(fgColor, 1),
                           BorderFactory.createEmptyBorder(5, 15, 5, 15)));
         button.setOpaque(true);
         button.setBorderPainted(true);
         button.setFont(new Font("Arial", Font.BOLD, 12));
     }


    // --- Main method for testing (Optional - Comment out when integrated into MainApplicationFrame) ---
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
             // This test requires a running database with HoaDonBan, CTHoaDonBan, SanPham, NhanVien, KhachHang tables
             // and the DAOs/DatabaseConnection configured correctly.

             // FIX: Pass a sample NhanVien object for testing permissions
             // Create a dummy NhanVien object (replace with real data if possible)
             // Ensure this NhanVien exists in your DB and has a valid MaNV and Role
             NhanVien dummyUser = new NhanVien();
             dummyUser.setMaNV("NV01"); // <-- Replace with an actual existing MaNV from your NhanVien table
             dummyUser.setTenNV("Test User"); // Optional: Set name
             dummyUser.setRole("Admin"); // Set a role for testing permissions ("Admin", "Manager", "Staff", "Guest")

             // Example: Create dummy NhanVien objects for different roles if needed for testing
             // NhanVien dummyAdmin = new NhanVien("NV001", "Admin User", "...", "...", "...", "admin_user", "...", "...", "Admin");
             // NhanVien dummyManager = new NhanVien("NV002", "Manager User", "...", "...", "...", "manager_user", "...", "...", "Manager");
             // NhanVien dummyStaff = new NhanVien("NV003", "Staff User", "...", "...", "...", "staff_user", "...", "...", "Staff");


             JFrame frame = new JFrame("Quản lý Hóa đơn Bán Demo");
             frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             frame.setSize(1000, 700); // Adjusted size
             frame.setLocationRelativeTo(null);
             // Pass the dummy NhanVien object representing the logged-in user
             frame.add(new HoaDonBanUI(dummyUser)); // Test with dummyUser (Admin role)
             // frame.add(new HoaDonBanUI(dummyManager)); // Test with Manager role
             // frame.add(new HoaDonBanUI(dummyStaff)); // Test with Staff role


             frame.setVisible(true);
        });
    }

}
