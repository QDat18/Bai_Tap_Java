package ui;

import dao.HoaDonNhapDAO;
import dao.NhaCCDAO; // Cần cho tìm kiếm/hiển thị tên NCC
import dao.NhanVienDAO; // Cần cho tìm kiếm/hiển thị tên NV
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.HoaDonNhap;
import model.NhanVien; // Sử dụng model NhanVien mới

public class HoaDonNhapUI extends JPanel {

    // Define colors (reusing the palette)
    Color coffeeBrown = new Color(102, 51, 0);
    Color lightBeige = new Color(245, 245, 220);
    Color accentGreen = new Color(60, 179, 113); // Color for Create button
    Color accentOrange = new Color(255, 165, 0); // Color for Delete button (if applicable)
    Color accentBlue = new Color(30, 144, 255); // Color for View Details (if applicable)
    Color darkGray = new Color(50, 50, 50);
    Color tableRowEven = Color.WHITE;
    Color tableRowOdd = new Color(230, 230, 230);

    // UI Components
    private JTextField txtTimKiem;
    private JButton btnThem, btnXemChiTiet, btnXoa, btnLamMoi, btnTimKiem;
    private JTable tblHoaDonNhap;
    private DefaultTableModel tblModel;
    private JScrollPane scrollPane;
    private JComboBox<String> cbTimTheo; // Combobox for search criteria

    private HoaDonNhapDAO hoaDonNhapDAO;
    // Cần các DAO phụ để lấy tên hiển thị hoặc hỗ trợ tìm kiếm nếu logic tìm kiếm ở UI
    private NhaCCDAO nhaCCDAO;
    private NhanVienDAO nhanVienDAO;


    // Date format for displaying dates
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    // Variable to store the logged-in user's account - Change to NhanVien
    private NhanVien currentUser; // Changed from ACC currentUserAccount

    // Constructor
    // Nhận đối tượng NhanVien của người dùng đang đăng nhập từ MainApplicationFrame
    public HoaDonNhapUI(NhanVien currentUser) { // Accept NhanVien instead of ACC
        this.currentUser = currentUser; // Gán đối tượng NhanVien
        // Loại bỏ dòng: this.currentUserAccount = currentUserAccount;

        // Initialize DAOs
        hoaDonNhapDAO = new HoaDonNhapDAO();
        nhanVienDAO = new NhanVienDAO(); // Khởi tạo NhanVienDAO để lấy tên nhân viên hiển thị hoặc hỗ trợ tìm kiếm
        nhaCCDAO = new NhaCCDAO(); // Khởi tạo NhaCCDAO để lấy tên nhà cung cấp hiển thị hoặc hỗ trợ tìm kiếm


        setLayout(new BorderLayout(10, 10));
        setBackground(lightBeige);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Top Panel (Actions and Search) ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setBackground(lightBeige);

        // Action Buttons
        btnThem = createButton("Thêm Hóa đơn nhập");
        styleButton(btnThem, accentGreen, Color.WHITE);
        btnThem.addActionListener(e -> themHoaDonNhap());

        btnXemChiTiet = createButton("Xem Chi tiết");
        styleButton(btnXemChiTiet, accentBlue, Color.WHITE);
        btnXemChiTiet.addActionListener(e -> xemChiTietHoaDonNhap());

        btnXoa = createButton("Xóa Hóa đơn nhập"); // Optional: Implement transactional delete
        styleButton(btnXoa, accentOrange, Color.WHITE);
         // Thêm listener cho nút Xóa nếu bạn muốn kích hoạt chức năng này
         // btnXoa.addActionListener(e -> xoaHoaDonNhap());


        btnLamMoi = createButton("Làm mới");
        styleButton(btnLamMoi, darkGray, Color.WHITE);
        btnLamMoi.addActionListener(e -> lamMoi());


        topPanel.add(btnThem);
        topPanel.add(btnXemChiTiet);
        // topPanel.add(btnXoa); // Add if implemented
        topPanel.add(btnLamMoi);


        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        searchPanel.setBackground(lightBeige);

        searchPanel.add(createLabel("Tìm theo:"));
         // Search criteria unchanged, assumes corresponding search methods in DAO or UI filtering exist
        cbTimTheo = new JComboBox<>(new String[]{
            "Mã HĐN", "Tên NV", "Tên NCC", "Ngày nhập" // Criteria matching UI and potential search method in DAO
        });
        cbTimTheo.setBackground(Color.WHITE);
        cbTimTheo.setForeground(darkGray);
        searchPanel.add(cbTimTheo);

        searchPanel.add(createLabel("Từ khóa:"));
        txtTimKiem = createTextField(); // Use default size
        txtTimKiem.setPreferredSize(new Dimension(200, 25));
        searchPanel.add(txtTimKiem);

        btnTimKiem = createButton("Tìm");
        styleButton(btnTimKiem, coffeeBrown, Color.WHITE);
        btnTimKiem.addActionListener(e -> timKiemHoaDonNhap());

        searchPanel.add(btnTimKiem);


        // Combine top panels
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(lightBeige);
        northPanel.add(topPanel, BorderLayout.WEST); // Actions on the left
        northPanel.add(searchPanel, BorderLayout.EAST); // Search on the right

        add(northPanel, BorderLayout.NORTH);


        // --- Table Panel ---
        JPanel panelTable = new JPanel(new BorderLayout());
        panelTable.setBackground(lightBeige);
        panelTable.setBorder(new TitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Danh sách hóa đơn nhập", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), coffeeBrown));


        // Column names matching the data we will load (including joined names)
        tblModel = new DefaultTableModel(
                 new Object[]{"Mã HĐN", "Mã NV", "Tên NV", "Mã NCC", "Tên NCC", "Ngày nhập", "Tổng tiền"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        tblHoaDonNhap = new JTable(tblModel);
        setupTableStyle(tblHoaDonNhap); // Apply styling

        // Double-click to view details (similar to HoaDonBanUI)
        tblHoaDonNhap.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 if (e.getClickCount() == 2) { // Double-click
                     xemChiTietHoaDonNhap(); // Call view details method
                 }
             }
        });


        scrollPane = new JScrollPane(tblHoaDonNhap);
        scrollPane.setBackground(lightBeige); // Match background


        panelTable.add(scrollPane, BorderLayout.CENTER);

        add(panelTable, BorderLayout.CENTER);


        // Load initial data
        loadHoaDonNhapTable();

        // Check permissions based on the logged-in NhanVien role
        // Check permissions for buttons
        checkPermissions(); // Assuming this method exists or needed
    }

    // Helper method to create styled labels
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    // Helper method to create text fields
    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 12));
        return textField;
    }

    // Helper method to create buttons
    private JButton createButton(String text) {
         JButton button = new JButton(text);
         button.setFont(new Font("Arial", Font.BOLD, 12));
         button.setFocusPainted(false);
         button.setBorder(BorderFactory.createCompoundBorder(
                          BorderFactory.createLineBorder(Color.WHITE, 1),
                          BorderFactory.createEmptyBorder(5, 15, 5, 15)));
         button.setOpaque(true);
         button.setBorderPainted(true);
         return button;
    }

    // Helper method to apply button styling
     private void styleButton(JButton button, Color bgColor, Color fgColor) {
         button.setBackground(bgColor);
         button.setForeground(fgColor);
         button.setBorder(BorderFactory.createCompoundBorder(
                          BorderFactory.createLineBorder(fgColor, 1), // Use fgColor for border
                          BorderFactory.createEmptyBorder(5, 15, 5, 15)));
     }


    // Helper method to style the table (mô phỏng HoaDonBanUI)
     private void setupTableStyle(JTable table) {
         table.setFont(new Font("Arial", Font.PLAIN, 12));
         table.setRowHeight(25); // Slightly larger row height for invoices
         table.setFillsViewportHeight(true); // Show background color in empty area

         // Header styling
         JTableHeader header = table.getTableHeader();
         header.setBackground(coffeeBrown);
         header.setForeground(Color.WHITE);
         header.setFont(new Font("Arial", Font.BOLD, 12));
         header.setReorderingAllowed(false); // Prevent column reordering

         // Cell alignment and alternating row colors
         DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
         centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
         DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
         leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
          DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
          rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);


         table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
              @Override
              public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                  Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                  c.setBackground(row % 2 == 0 ? tableRowEven : tableRowOdd);
                  if (isSelected) {
                      c.setBackground(new Color(180, 210, 230)); // Selection color
                  }

                  // Apply alignment based on column header
                  String columnName = table.getColumnModel().getColumn(column).getHeaderValue().toString();
                  if (columnName.equals("Tổng tiền")) {
                       setHorizontalAlignment(SwingConstants.RIGHT); // Right align currency
                       // Optional: Format currency here if needed
                       // if (value instanceof Number) { setText(new DecimalFormat("#,##0 VNĐ").format(value)); }
                  } else if (columnName.equals("Ngày nhập") || columnName.equals("Mã HĐN") || columnName.equals("Mã NV") || columnName.equals("Mã NCC")) {
                       setHorizontalAlignment(SwingConstants.CENTER); // Center IDs and Date
                  }
                  else {
                       setHorizontalAlignment(SwingConstants.LEFT); // Default left (Tên NV, Tên NCC)
                  }

                  setText(value != null ? value.toString() : ""); // Set text

                  return c;
              }
         });
     }


    // Load data from DAO into the table (mô phỏng loadHoaDonBanTable)
    // Sử dụng phương thức getAllHoaDonNhap() đã join để lấy tên
    public void loadHoaDonNhapTable() {
        tblModel.setRowCount(0); // Clear existing data
        List<HoaDonNhap> danhSachHoaDonNhap = hoaDonNhapDAO.getAllHoaDonNhap(); // Sử dụng DAO lấy danh sách đã join

        if (danhSachHoaDonNhap != null) {
            for (HoaDonNhap hdn : danhSachHoaDonNhap) {
                Vector<Object> row = new Vector<>();
                row.add(hdn.getMaHDN());
                row.add(hdn.getMaNV());
                row.add(hdn.getTenNV()); // Sử dụng TenNV từ model đã join
                row.add(hdn.getMaNCC());
                row.add(hdn.getTenNCC()); // Sử dụng TenNCC từ model đã join
                row.add(hdn.getNgayNhap() != null ? dateFormat.format(hdn.getNgayNhap()) : "N/A"); // Format date
                row.add(hdn.getTongTien()); // Display total
                tblModel.addRow(row);
            }
        }
        // Optional: Show a message if list is empty
        if (danhSachHoaDonNhap == null || danhSachHoaDonNhap.isEmpty()) {
             System.out.println("Danh sách hóa đơn nhập trống hoặc null.");
             // Consider adding a row with "Không có dữ liệu" or showing a label
        }
    }

    // Action for "Thêm Hóa đơn nhập" button - Logic updated to use NhanVien
    private void themHoaDonNhap() {
         // Lấy thông tin nhân viên từ đối tượng NhanVien đang đăng nhập
         String employeeMaNV = null;
         String employeeTenNV = null;

         if (currentUser != null) { // Check if currentUser (NhanVien) is not null
              employeeMaNV = currentUser.getMaNV();
              employeeTenNV = currentUser.getTenNV(); // Get TenNV directly from currentUser

              System.out.println("DEBUG (HoaDonNhapUI): Lay MaNV='" + employeeMaNV + "', TenNV='" + employeeTenNV + "' tu doi tuong NhanVien dang nhap.");

              // Simple check if essential info exists
              if (employeeMaNV == null || employeeMaNV.isEmpty()) {
                   JOptionPane.showMessageDialog(this, "Thông tin Mã Nhân viên không có trong tài khoản đăng nhập (lỗi dữ liệu NhanVien).", "Lỗi", JOptionPane.ERROR_MESSAGE);
                   return;
              }
               // TenNV might be null/empty but MaNV is essential

         } else {
              // Xử lý trường hợp đối tượng NhanVien đang đăng nhập là null (lỗi khi truyền NhanVien vào UI)
              System.err.println("DEBUG (HoaDonNhapUI): Doi tuong NhanVien dang nhap la NULL.");
              JOptionPane.showMessageDialog(this, "Không có thông tin tài khoản đăng nhập được truyền vào (lỗi hệ thống).", "Lỗi", JOptionPane.ERROR_MESSAGE);
              return; // Ngừng nếu không có thông tin tài khoản
         }


         // Mở dialog tạo hóa đơn nhập, truyền thông tin nhân viên
         // Sử dụng SwingUtilities.getWindowAncestor(this) để lấy JFrame cha
         // Ensure HoaDonNhapCreationDialog constructor matches this call
         HoaDonNhapCreationDialog creationDialog = new HoaDonNhapCreationDialog(
             (JFrame) SwingUtilities.getWindowAncestor(this),
             employeeMaNV, // Truyền employeeMaNV đã lấy được
             employeeTenNV // Truyền employeeTenNV đã lấy được (có thể null nếu TenNV trong NhanVien null)
         );
         creationDialog.setVisible(true); // Hiển thị dialog

         // After the dialog is closed, check if it was saved successfully and refresh the table
         if (creationDialog.isSavedSuccessfully()) { // Dialog cần có phương thức public isSavedSuccessfully()
             loadHoaDonNhapTable(); // Làm mới bảng nếu lưu thành công
         }
    }

     // Action for "Xem Chi tiết" (mô phỏng xemChiTietHoaDonBan)
     private void xemChiTietHoaDonNhap() {
         int selectedRow = tblHoaDonNhap.getSelectedRow();
         if (selectedRow != -1) {
             // Lấy Mã HĐN từ dòng được chọn trong bảng
             String maHDN = tblModel.getValueAt(selectedRow, 0).toString();
             // Mở dialog xem chi tiết, truyền Mã HĐN
              // Ensure CTHoaDonNhapDetailsDialog constructor matches this call
             CTHoaDonNhapDetailsDialog detailsDialog = new CTHoaDonNhapDetailsDialog(
                 (JFrame) SwingUtilities.getWindowAncestor(this), // Owner frame
                 maHDN // Pass the selected MaHDN
             );
             detailsDialog.setVisible(true); // Show the details dialog

         } else {
             JOptionPane.showMessageDialog(this, "Vui lòng chọn một Hóa đơn Nhập để xem chi tiết.", "Lỗi", JOptionPane.WARNING_MESSAGE);
         }
     }


    // Action for "Xóa Hóa đơn nhập" (tùy chọn, mô phỏng xoaHoaDonBan)
    // Cần cẩn thận khi xóa hóa đơn nhập, nên dùng transaction để xóa cả chi tiết và GIẢM tồn kho
    // This method is currently not added to a button listener.
    private void xoaHoaDonNhap() {
        int selectedRow = tblHoaDonNhap.getSelectedRow();
        if (selectedRow != -1) {
            String maHDN = tblModel.getValueAt(selectedRow, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa Hóa đơn Nhập có mã: " + maHDN + "?\nViệc này sẽ xóa cả chi tiết và CẬP NHẬT LẠI TỒN KHO (GIẢM).", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // TODO: Triển khai transactional delete trong HoaDonNhapDAO
                // Phương thức này cần:
                // 1. Lấy tất cả chi tiết của HĐN này
                // 2. Với mỗi chi tiết, GIẢM số lượng tồn kho sản phẩm (ngược lại lúc nhập)
                // 3. Xóa tất cả chi tiết của HĐN này
                // 4. Xóa header HĐN này
                // TẤT CẢ TRONG MỘT TRANSACTION

                boolean success = false; // Assume failure initially

                // Ví dụ gọi phương thức transactional delete (cần tự triển khai trong HoaDonNhapDAO)
                // success = hoaDonNhapDAO.deleteHoaDonNhapTransaction(maHDN);

                // Đây là placeholder chỉ báo chưa triển khai chức năng xóa đầy đủ
                 JOptionPane.showMessageDialog(this, "Chức năng xóa hóa đơn nhập chưa được triển khai đầy đủ (cần transaction).", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                 success = false; // Đảm bảo success là false nếu chưa triển khai


                if (success) {
                    JOptionPane.showMessageDialog(this, "Xóa Hóa đơn Nhập thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    loadHoaDonNhapTable(); // Refresh table
                } else {
                    // Thông báo lỗi chi tiết hơn nếu có thể từ DAO (nếu deleteTransaction trả về false)
                    // JOptionPane.showMessageDialog(this, "Xóa Hóa đơn Nhập thất bại. Vui lòng kiểm tra ràng buộc dữ liệu hoặc lỗi hệ thống (console).", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một Hóa đơn Nhập để xóa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    // Action for "Làm mới" button
    private void lamMoi() {
        txtTimKiem.setText("");
        cbTimTheo.setSelectedIndex(0); // Reset search criteria
        loadHoaDonNhapTable(); // Reload all data
        tblHoaDonNhap.clearSelection(); // Clear table selection
        System.out.println("Đã làm mới danh sách Hóa đơn Nhập.");
    }

    // Action for "Tìm kiếm" button
    private void timKiemHoaDonNhap() {
        String searchTerm = txtTimKiem.getText().trim();
        String searchCriteria = (String) cbTimTheo.getSelectedItem();
        tblModel.setRowCount(0); // Clear existing data

        if (searchTerm.isEmpty()) {
            loadHoaDonNhapTable(); // Load all if search term is empty
            return;
        }

        // TODO: Triển khai phương thức searchHoaDonNhap trong HoaDonNhapDAO để tìm kiếm hiệu quả hơn
        // Sử dụng phương thức tìm kiếm trong DAO (cần triển khai)
        // List<HoaDonNhap> searchResult = hoaDonNhapDAO.searchHoaDonNhap(searchTerm, searchCriteria);

         // **Placeholder Search Logic (Simple filtering in UI for now - Implement in DAO for efficiency)**
         // Lấy tất cả dữ liệu và lọc trên UI (không hiệu quả với lượng lớn dữ liệu)
         List<HoaDonNhap> allHoaDonNhaps = hoaDonNhapDAO.getAllHoaDonNhap();
         List<HoaDonNhap> searchResult = new ArrayList<>();
         if (allHoaDonNhaps != null) {
              for (HoaDonNhap hdn : allHoaDonNhaps) {
                  boolean matches = false;
                  switch (searchCriteria) {
                      case "Mã HĐN":
                          if (hdn.getMaHDN() != null && hdn.getMaHDN().toLowerCase().contains(searchTerm.toLowerCase())) {
                              matches = true;
                          }
                          break;
                      case "Tên NV":
                          // Sử dụng TenNV đã có trong model (nếu getAllHoaDonNhap join)
                           if (hdn.getTenNV() != null && hdn.getTenNV().toLowerCase().contains(searchTerm.toLowerCase())) {
                               matches = true;
                           }
                          break;
                      case "Tên NCC":
                          // Sử dụng TenNCC đã có trong model (nếu getAllHoaDonNhap join)
                           if (hdn.getTenNCC() != null && hdn.getTenNCC().toLowerCase().contains(searchTerm.toLowerCase())) {
                               matches = true;
                           }
                          break;
                      case "Ngày nhập":
                          // Cần định dạng ngày và so sánh chuỗi
                          if (hdn.getNgayNhap() != null) {
                              String dateString = dateFormat.format(hdn.getNgayNhap());
                              if (dateString.contains(searchTerm)) { // So sánh chuỗi đơn giản
                                  matches = true;
                              }
                          }
                          break;
                  }
                  if (matches) {
                      searchResult.add(hdn);
                  }
              }
         }


        if (searchResult != null) {
            for (HoaDonNhap hdn : searchResult) {
                Vector<Object> row = new Vector<>();
                row.add(hdn.getMaHDN());
                row.add(hdn.getMaNV());
                row.add(hdn.getTenNV());
                row.add(hdn.getMaNCC());
                row.add(hdn.getTenNCC());
                row.add(hdn.getNgayNhap() != null ? dateFormat.format(hdn.getNgayNhap()) : "N/A");
                row.add(hdn.getTongTien());
                tblModel.addRow(row);
            }
        }

         if (searchResult == null || searchResult.isEmpty()) {
              JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả nào.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
         }
         tblHoaDonNhap.clearSelection(); // Clear selection after search
    }

    // Add permission check method (if needed, based on the user's NhanVien role)
     private void checkPermissions() {
          // Example: Only Managers and Admins can add/delete HoaDonNhap
          String role = (currentUser != null) ? currentUser.getRole() : "";

          if ("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role)) {
               btnThem.setEnabled(true);
               // btnXoa.setEnabled(true); // Enable delete button if implemented and permitted
          } else {
               btnThem.setEnabled(false);
               // btnXoa.setEnabled(false);
          }

          // Viewing details and searching are likely allowed for Staff as well
          btnXemChiTiet.setEnabled(true); // Assume everyone can view details
          btnTimKiem.setEnabled(true);
          txtTimKiem.setEnabled(true);
          cbTimTheo.setEnabled(true);
          btnLamMoi.setEnabled(true); // Assume everyone can refresh

          // You might need to add logic to disable the delete button if a row is not selected
          // or if the selected invoice was created by a higher role user (e.g., Manager cannot delete Admin's invoice)
          // This would typically be handled in a separate updateButtonState() or within the xoaHoaDonNhap method
     }


    // Main method for testing (Optional - Comment out when integrated into MainApplicationFrame)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
             // This test requires a running database with HoaDonNhap, CTHoaDonNhap, NhanVien, NhaCC, SanPham tables
             // and the DAOs/DatabaseConnection configured correctly.

             // Create a dummy NhanVien object for testing (replace with real data if possible)
             // Ensure this NhanVien exists in your DB and has a valid MaNV and Role
             NhanVien dummyUser = new NhanVien();
             dummyUser.setMaNV("NV01"); // <-- Replace with an actual existing MaNV from your NhanVien table
             dummyUser.setTenNV("Test User"); // Optional: Set name for display
             dummyUser.setRole("Manager"); // Set a role for testing permissions (e.g., "Admin", "Manager", "Staff")


             JFrame frame = new JFrame("Quản lý Hóa đơn Nhập Demo");
             frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             frame.setSize(1000, 700);
             frame.setLocationRelativeTo(null);
             // Pass the dummy NhanVien object
             frame.add(new HoaDonNhapUI(dummyUser)); // Pass the dummy NhanVien object representing the logged-in user

             frame.setVisible(true);
        });
    }

}
