package ui;

import dao.CTHoaDonBanDAO; // Import CTHoaDonBanDAO
import dao.HoaDonBanDAO; // Import HoaDonBanDAO
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer; // Import TitledBorder
import javax.swing.table.DefaultTableModel;
import model.CTHoaDonBan;
import model.HoaDonBan;
// Assuming other necessary imports for your models exist
// import model.SanPham; // If needed for display details in table


public class CTHoaDonBanDetailsDialog extends JDialog {

     // Define colors (reusing the palette)
     Color coffeeBrown = new Color(102, 51, 0);
     Color darkGray = new Color(50, 50, 50);
     Color lightBeige = new Color(245, 245, 220);
     Color accentOrange = new Color(255, 165, 0);
     Color accentBlue = new Color(30, 144, 255);
     Color accentGreen = new Color(60, 179, 113);


     // UI Components for header info
     private JLabel lblMaHDB;
     private JLabel lblTenNV; // Display TenNV
     private JLabel lblTenKH; // Display TenKH
     private JLabel lblNgayBan; // Display Ngayban
     private JLabel lblTongTien; // Display Tongtien

     // UI Components for details table
     private JTable chiTietTable;
     private DefaultTableModel chiTietTableModel;

     // Data Access Objects
     private HoaDonBanDAO hoaDonBanDAO; // To get HoaDonBan header
     private CTHoaDonBanDAO ctHoaDonBanDAO; // To get CTHoaDonBan details

     // Date and currency formatters
     // Use Timestamp for date/time if your DB stores time
     private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
     private DecimalFormat currencyFormat = new DecimalFormat("#,##0"); // For currency display


     // Constructor receiving MaHDB to display details for
     public CTHoaDonBanDetailsDialog(JFrame owner, String maHDB) {
         super(owner, "Chi tiết Hóa đơn Bán: " + maHDB, true); // Modal dialog with title
         setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
         setSize(600, 500); // Set a default size (adjust as needed)
         setMinimumSize(new Dimension(600, 500)); // Prevent shrinking too much
         setLocationRelativeTo(owner); // Center relative to the owner frame

         // Initialize DAOs
         hoaDonBanDAO = new HoaDonBanDAO();
         ctHoaDonBanDAO = new CTHoaDonBanDAO();


         // --- Content Pane ---
         JPanel contentPane = new JPanel(new BorderLayout(10, 10));
         contentPane.setBackground(lightBeige);
         contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));


         // --- Header Info Panel ---
         JPanel headerInfoPanel = new JPanel(new GridBagLayout());
         headerInfoPanel.setBackground(lightBeige);
          headerInfoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Thông tin chung", 0, 0, new Font("Arial", Font.BOLD, 14), coffeeBrown));


         GridBagConstraints gbc = new GridBagConstraints();
         gbc.insets = new Insets(5, 5, 5, 5);
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.weightx = 1.0;
         gbc.anchor = GridBagConstraints.WEST; // Align labels to the left


         // Row 0: MaHDB, NgayBan
         gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; headerInfoPanel.add(createLabel("Mã HĐB:"), gbc);
         gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; lblMaHDB = createValueLabel(maHDB); headerInfoPanel.add(lblMaHDB, gbc);

         gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0; headerInfoPanel.add(createLabel("Ngày bán:"), gbc);
         gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 1.0; lblNgayBan = createValueLabel(""); headerInfoPanel.add(lblNgayBan, gbc); // Will set text later


         // Row 1: TenNV, TenKH
         gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; headerInfoPanel.add(createLabel("Nhân viên:"), gbc);
         gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; lblTenNV = createValueLabel(""); headerInfoPanel.add(lblTenNV, gbc); // Will set text later

         gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0; headerInfoPanel.add(createLabel("Khách hàng:"), gbc);
         gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 1.0; lblTenKH = createValueLabel(""); headerInfoPanel.add(lblTenKH, gbc); // Will set text later


         // Row 2: TongTien
         gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; headerInfoPanel.add(createLabel("Tổng tiền:"), gbc);
         gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; gbc.gridwidth = 3; // Span across remaining columns
         lblTongTien = createValueLabel("");
         lblTongTien.setFont(new Font("Arial", Font.BOLD, 16)); // Larger font
         lblTongTien.setForeground(accentGreen); // Highlight total color
         headerInfoPanel.add(lblTongTien, gbc);
          gbc.gridwidth = 1; // Reset gridwidth


         contentPane.add(headerInfoPanel, BorderLayout.NORTH);


         // --- Details Table Panel ---
         JPanel detailsTablePanel = new JPanel(new BorderLayout());
         detailsTablePanel.setBackground(lightBeige);
          detailsTablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Chi tiết sản phẩm", 0, 0, new Font("Arial", Font.BOLD, 14), coffeeBrown));


         // Table Model for details: MaSP, TenSP, Soluong, DonGiaBan, KhuyenMai, ThanhTien
         // Note: These columns match the data fetched by CTHoaDonBanDAO.getChiTietHoaDonByMaHDB
         chiTietTableModel = new DefaultTableModel(new Object[]{"Mã SP", "Tên SP", "Số lượng", "Đơn giá", "Khuyến mãi (%)", "Thành tiền"}, 0) {
             @Override
             public boolean isCellEditable(int row, int column) {
                 return false; // Details table is not editable in a details dialog
             }
              @Override
              public Class<?> getColumnClass(int columnIndex) {
                   // Assuming numeric columns are Soluong (2), DonGia (3), KhuyenMai (4), ThanhTien (5)
                   if (columnIndex == 2 || columnIndex == 3 || columnIndex == 4 || columnIndex == 5) return Integer.class; // Numeric columns
                   return super.getColumnClass(columnIndex);
              }
         };
         chiTietTable = new JTable(chiTietTableModel);

          // Style table cells (e.g., right align numeric columns)
          DefaultTableCellRenderer detailsRendererRight = new DefaultTableCellRenderer();
          detailsRendererRight.setHorizontalAlignment(SwingConstants.RIGHT);
          chiTietTable.getColumnModel().getColumn(2).setCellRenderer(detailsRendererRight); // Soluong
          chiTietTable.getColumnModel().getColumn(3).setCellRenderer(detailsRendererRight); // DonGia
          chiTietTable.getColumnModel().getColumn(5).setCellRenderer(detailsRendererRight); // ThanhTien

          // Style KhuyenMai column to be centered (optional)
           DefaultTableCellRenderer detailsRendererCenter = new DefaultTableCellRenderer();
           detailsRendererCenter.setHorizontalAlignment(SwingConstants.CENTER);
           chiTietTable.getColumnModel().getColumn(4).setCellRenderer(detailsRendererCenter); // KhuyenMai (%)

          // Style table header (basic styling)
          chiTietTable.getTableHeader().setBackground(coffeeBrown);
          chiTietTable.getTableHeader().setForeground(Color.WHITE);
          chiTietTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
          chiTietTable.setRowHeight(20); // Slightly larger row height
          chiTietTable.setFillsViewportHeight(true); // Show background color in empty area


         // Add table to scroll pane
         JScrollPane detailsScrollPane = new JScrollPane(chiTietTable);
         detailsTablePanel.add(detailsScrollPane, BorderLayout.CENTER);

         contentPane.add(detailsTablePanel, BorderLayout.CENTER);


         // --- Footer Button Panel ---
         JPanel footerButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
         footerButtonPanel.setBackground(lightBeige);
         JButton btnClose = new JButton("Đóng");
         styleButton(btnClose, darkGray, Color.WHITE); // Style the button
         btnClose.addActionListener(e -> dispose()); // Close the dialog
         footerButtonPanel.add(btnClose);

         contentPane.add(footerButtonPanel, BorderLayout.SOUTH);


         // Set the content pane of the dialog
         setContentPane(contentPane);

         // --- Load Data ---
         loadHoaDonDetails(maHDB); // Call method to load and display data

         setModal(true); // Make it a modal dialog
         // setResizable(false); // Prevent resizing (optional)
     }

     // Method to load and display invoice header and detail data
      private void loadHoaDonDetails(String maHDB) {
          // Load header info first
          // Ensure hoaDonBanDAO.getHoaDonBanById fetches TenNV, TenKH, Ngayban (as Timestamp), Tongtien
          HoaDonBan hoaDonBan = hoaDonBanDAO.getHoaDonBanById(maHDB);

          if (hoaDonBan != null) {
              // Set header labels using data from the loaded HoaDonBan object
              lblMaHDB.setText(hoaDonBan.getMaHDB()); // Ensure MaHDB is set in constructor or here
              lblTenNV.setText(hoaDonBan.getTenNV() != null ? hoaDonBan.getTenNV() : "N/A"); // Use TenNV from model
              lblTenKH.setText(hoaDonBan.getTenKH() != null ? hoaDonBan.getTenKH() : "Khách lẻ"); // Use TenKH from model (handle NULL)
              lblNgayBan.setText(hoaDonBan.getNgayban() != null ? dateFormat.format(hoaDonBan.getNgayban()) : "N/A"); // Format date (Ngayban is java.util.Date/Timestamp)
              lblTongTien.setText(currencyFormat.format(hoaDonBan.getTongtien()) + " VNĐ"); // Format total amount and add currency symbol

              // Load details
              chiTietTableModel.setRowCount(0); // Clear existing details
              // Ensure ctHoaDonBanDAO.getChiTietHoaDonByMaHDB fetches details including TenSP and Giaban
              List<CTHoaDonBan> chiTietList = ctHoaDonBanDAO.getChiTietHoaDonByMaHDB(maHDB);

              if (chiTietList != null && !chiTietList.isEmpty()) {
                  for (CTHoaDonBan ct : chiTietList) {
                       // Add data to the details table model using data from CTHoaDonBan objects
                       chiTietTableModel.addRow(new Object[]{
                           ct.getMaSP(),
                           ct.getTenSP(), // Display TenSP from detail model (Requires JOIN in DAO)
                           ct.getSoluong(),
                           ct.getGiaban(), // Display Giaban from detail model (Requires JOIN in DAO)
                           ct.getKhuyenmai(),
                           ct.getThanhtien()
                       });
                  }
              } else {
                   System.out.println("Không tìm thấy chi tiết hóa đơn cho HDB: " + maHDB + " hoặc có lỗi khi tải chi tiết.");
                   // Optional: Add a row indicating no details found
                   // chiTietTableModel.addRow(new Object[]{"", "Không có chi tiết", "", "", "", ""});
              }

          } else {
              // Handle case where invoice header is not found
              System.err.println("Không tìm thấy hóa đơn bán với mã: " + maHDB);
              JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin hóa đơn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
              dispose(); // Close the dialog if invoice not found
          }
      }


     // Helper method to create styled labels (for field names)
      private JLabel createLabel(String text) {
          JLabel label = new JLabel(text);
          label.setForeground(darkGray);
          label.setFont(new Font("Arial", Font.BOLD, 12)); // Labels in bold
          return label;
      }

      // Helper method to create value labels (for displaying data)
      private JLabel createValueLabel(String text) {
          JLabel label = new JLabel(text);
          label.setForeground(darkGray);
          label.setFont(new Font("Arial", Font.PLAIN, 12)); // Values in plain font
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


     // Main method for testing (Optional)
     /*
     public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> {
              // This test requires a running database with HoaDonBan, CTHoaDonBan, NhanVien, KhachHang, SanPham data
              // And the DAOs/DatabaseConnection configured correctly.
              // Replace "HDB001" with a valid MaHDB from your database for testing.
              String testMaHDB = "HDB001"; // Replace with a valid ID from your DB

              JFrame frame = new JFrame("Owner Frame");
              frame.setSize(300, 200);
              frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
              frame.setVisible(true);

              // Pass the owner frame and the test MaHDB
              CTHoaDonBanDetailsDialog dialog = new CTHoaDonBanDetailsDialog(frame, testMaHDB);
              dialog.setVisible(true);

              // frame.dispose(); // Dispose owner frame after dialog closes if it was just for testing
         });
     }
     */
}
