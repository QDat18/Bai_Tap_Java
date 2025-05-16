package ui;

import dao.ChiTietHoaDonNhapDAO;
import dao.HoaDonNhapDAO;
import java.awt.*; // Import NhaCCDAO
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;
import javax.swing.*; // Import NhaCC
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel; // Đảm bảo import đúng DatabaseConnection
import javax.swing.table.JTableHeader;
import model.ChiTietHoaDonNhap;
import model.HoaDonNhap;

public class CTHoaDonNhapDetailsDialog extends JDialog {

    // Define colors (reusing the palette)
     Color coffeeBrown = new Color(102, 51, 0);
     Color darkGray = new Color(50, 50, 50);
     Color lightBeige = new Color(245, 245, 220);
     Color accentOrange = new Color(255, 165, 0); // Color for Close button


     // UI Components for header info
     private JLabel lblMaHDNValue;
     private JLabel lblNgayNhapValue;
     private JLabel lblTenNVValue; // Display TenNV
     private JLabel lblTenNCCValue; // Display TenNCC (thay Khách hàng)
     private JLabel lblTongTienValue; // Display formatted total

     // UI Components for details table
     private JTable tblChiTietHoaDonNhap;
     private DefaultTableModel tblChiTietModel;
     private JScrollPane scrollPaneChiTiet;

     // UI Components for dialog actions
     private JButton btnDong;

     // DAOs
     private HoaDonNhapDAO hoaDonNhapDAO;
     private ChiTietHoaDonNhapDAO chiTietHoaDonNhapDAO;
     // Các DAO phụ không cần khởi tạo ở đây nếu thông tin tên đã được join trong DAO chính
     // private NhanVienDAO nhanVienDAO;
     // private NhaCCDAO nhaCCDAO;
     // private SanPhamDAO sanPhamDAO;


     // Formatters
     private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
     private DecimalFormat currencyFormatter = new DecimalFormat("#,##0 VNĐ"); // Format with currency symbol


     // Constructor
     // Nhận Frame cha và Mã HĐN cần hiển thị
    public CTHoaDonNhapDetailsDialog(Frame owner, String maHDN) {
        super(owner, "Chi tiết Hóa đơn Nhập: " + maHDN, true); // Modal dialog with title

        // Initialize DAOs
        hoaDonNhapDAO = new HoaDonNhapDAO();
        chiTietHoaDonNhapDAO = new ChiTietHoaDonNhapDAO();
        // Các DAO phụ không cần khởi tạo ở đây nếu thông tin tên đã được join trong DAO chính
        // nhanVienDAO = new NhanVienDAO();
        // nhaCCDAO = new NhaCCDAO();
        // sanPhamDAO = new SanPhamDAO();


        setLayout(new BorderLayout(10, 10));
        setBackground(lightBeige);
        setPreferredSize(new Dimension(700, 500)); // Adjust size
        setResizable(false); // Prevent resizing


        // --- Header Info Panel ---
        JPanel headerInfoPanel = new JPanel(new GridBagLayout());
        headerInfoPanel.setBackground(lightBeige);
        headerInfoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Thông tin chung", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), coffeeBrown));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: Mã HĐN
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST; headerInfoPanel.add(createLabel("Mã HĐN:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; lblMaHDNValue = createValueLabel(""); headerInfoPanel.add(lblMaHDNValue, gbc);

        // Row 1: Ngày nhập
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; headerInfoPanel.add(createLabel("Ngày nhập:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; lblNgayNhapValue = createValueLabel(""); headerInfoPanel.add(lblNgayNhapValue, gbc);

        // Row 2: Nhân viên
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; headerInfoPanel.add(createLabel("Nhân viên:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; lblTenNVValue = createValueLabel(""); headerInfoPanel.add(lblTenNVValue, gbc);

        // Row 3: Nhà cung cấp
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; headerInfoPanel.add(createLabel("Nhà cung cấp:"), gbc); // Change label
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0; lblTenNCCValue = createValueLabel(""); headerInfoPanel.add(lblTenNCCValue, gbc);


        // Row 4: Tổng tiền (span two columns)
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; headerInfoPanel.add(createLabel("Tổng tiền:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0; lblTongTienValue = createValueLabel(""); headerInfoPanel.add(lblTongTienValue, gbc);


        add(headerInfoPanel, BorderLayout.NORTH);


        // --- Details Table Panel ---
        JPanel detailsTablePanel = new JPanel(new BorderLayout());
        detailsTablePanel.setBackground(lightBeige);
        detailsTablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Chi tiết sản phẩm", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), coffeeBrown));


        // Column names matching ChiTietHoaDonNhap plus TenSP
        tblChiTietModel = new DefaultTableModel(
                new Object[]{"Mã SP", "Tên SP", "Số lượng", "Đơn giá nhập", "Khuyến mãi (%)", "Thành tiền"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        tblChiTietHoaDonNhap = new JTable(tblChiTietModel);
        setupTableStyle(tblChiTietHoaDonNhap);


        scrollPaneChiTiet = new JScrollPane(tblChiTietHoaDonNhap);
        scrollPaneChiTiet.setBackground(lightBeige);

        detailsTablePanel.add(scrollPaneChiTiet, BorderLayout.CENTER);

        add(detailsTablePanel, BorderLayout.CENTER);


        // --- Dialog Action Panel ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionPanel.setBackground(lightBeige);

        btnDong = createButton("Đóng");
        styleButton(btnDong, accentOrange, Color.WHITE);
        btnDong.addActionListener(e -> dispose()); // Close dialog
        actionPanel.add(btnDong);

        add(actionPanel, BorderLayout.SOUTH);

        // Load data for the given MaHDN
        loadHoaDonNhapDetails(maHDN);

        // Pack and center the dialog
        pack();
        setLocationRelativeTo(owner); // Center relative to the owner frame
    }

     // Helper method to create styled labels
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    // Helper method to create labels for displaying values
    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray); // Or a slightly different color
        label.setFont(new Font("Arial", Font.PLAIN, 12)); // Plain font for value
        return label;
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

     // Helper method to style the table (similar to HoaDonBan tables)
     private void setupTableStyle(JTable table) {
         table.setFont(new Font("Arial", Font.PLAIN, 12));
         table.setRowHeight(20);
         table.setFillsViewportHeight(true); // Show background color in empty area

         // Header styling
         JTableHeader header = table.getTableHeader();
         header.setBackground(coffeeBrown);
         header.setForeground(Color.WHITE);
         header.setFont(new Font("Arial", Font.BOLD, 12));
         header.setReorderingAllowed(false); // Prevent column reordering

         // Cell alignment
         DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
         centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
         DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
         rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
         DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
         leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);


         table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
             @Override
             public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                 Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                 // No alternating colors in dialog table for simplicity, or apply if desired
                 // c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                 if (isSelected) {
                     c.setBackground(new Color(180, 210, 230)); // Selection color
                 }

                 // Apply alignment based on column header
                 String columnName = table.getColumnModel().getColumn(column).getHeaderValue().toString();
                 if (columnName.equals("Số lượng") || columnName.equals("Đơn giá nhập") || columnName.equals("Khuyến mãi (%)") || columnName.equals("Thành tiền")) {
                      setHorizontalAlignment(SwingConstants.RIGHT); // Right align numeric/currency
                      // Optional: Format currency here if needed, but already formatted when adding row
                      // if (value instanceof Number) { setText(currencyFormatter.format(value)); }
                 } else if (columnName.equals("Mã SP")) {
                      setHorizontalAlignment(SwingConstants.CENTER); // Center ID
                 }
                 else {
                      setHorizontalAlignment(SwingConstants.LEFT); // Default left (Tên SP)
                 }

                 setText(value != null ? value.toString() : ""); // Set text

                 return c;
             }
         });
     }


     // Load header and detail data for the given MaHDN (mô phỏng loadHoaDonBanDetails)
     // Sử dụng các DAO Hóa đơn Nhập
     private void loadHoaDonNhapDetails(String maHDN) {
         // Load Header Info
         // Sử dụng getHoaDonNhapByMaHDN đã join để lấy cả tên NV và NCC
         HoaDonNhap hdn = hoaDonNhapDAO.getHoaDonNhapByMaHDN(maHDN);

         if (hdn != null) {
             lblMaHDNValue.setText(hdn.getMaHDN());
             lblNgayNhapValue.setText(hdn.getNgayNhap() != null ? dateFormat.format(hdn.getNgayNhap()) : "N/A");

             // Lấy tên NV và NCC từ model đã join (DAO đã join)
             lblTenNVValue.setText(hdn.getTenNV() != null ? hdn.getTenNV() : "N/A");
             lblTenNCCValue.setText(hdn.getTenNCC() != null ? hdn.getTenNCC() : "N/A"); // Set TenNCC

             // Format và hiển thị tổng tiền
             lblTongTienValue.setText(currencyFormatter.format(hdn.getTongTien()));


             // Load Details Info
             // Sử dụng getChiTietHoaDonNhapByMaHDN đã join để lấy tên SP
             List<ChiTietHoaDonNhap> chiTietList = chiTietHoaDonNhapDAO.getChiTietHoaDonNhapByMaHDN(maHDN);

             tblChiTietModel.setRowCount(0); // Clear existing data
             if (chiTietList != null) {
                 for (ChiTietHoaDonNhap ct : chiTietList) {
                      Vector<Object> row = new Vector<>();
                      row.add(ct.getMaSP());
                      row.add(ct.getTenSP()); // Sử dụng TenSP từ model đã join
                      row.add(ct.getSoluong());
                      row.add(currencyFormatter.format(ct.getDongia())); // Format currency
                      row.add(ct.getKhuyenmai());
                      row.add(currencyFormatter.format(ct.getThanhtien())); // Format currency
                      tblChiTietModel.addRow(row);
                 }
             } else {
                 System.out.println("Không tìm thấy chi tiết hóa đơn nhập cho mã: " + maHDN);
             }

         } else {
             // Handle case where HoaDonNhap header not found
             JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn nhập có mã: " + maHDN, "Lỗi", JOptionPane.ERROR_MESSAGE);
             dispose(); // Close dialog if invoice not found
         }
     }


    // Main method for testing (Optional - Comment out when integrated)
    /*
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
             // This test requires a running database with HoaDonNhap and CTHoaDonNhap data
             // and the DAOs/DatabaseConnection configured correctly.
             // Replace "HDN001" with a valid MaHDN from your database for testing.
             String testMaHDN = "HDN001"; // Replace with a valid ID from your DB

             JFrame frame = new JFrame("Owner Frame");
             frame.setSize(300, 200);
             frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             frame.setVisible(true);

             // Pass the owner frame and the test MaHDN
             CTHoaDonNhapDetailsDialog dialog = new CTHoaDonNhapDetailsDialog(frame, testMaHDN);
             dialog.setVisible(true);

             // frame.dispose(); // Dispose owner frame after dialog closes if it was just for testing
        });
    }
    */
}