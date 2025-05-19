package ui;

import dao.HoaDonNhapDAO;
import dao.NhaCCDAO; // Có thể không cần khởi tạo ở đây nếu chỉ gọi từ HoaDonNhapDAO transaction
import dao.NhanVienDAO; // Import NhaCCDAO thay vì KhachHangDAO
import dao.SanPhamDAO;
import java.awt.*;
import java.text.DecimalFormat; // Cần cho Model trong ComboBox (nếu dùng) hoặc hiển thị
import java.text.SimpleDateFormat; // Cần cho Model trong ComboBox (nếu dùng) hoặc hiển thị
import java.util.ArrayList;
import java.util.Date;
import java.util.List; // Import NhaCC thay vì KhachHang
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.ChiTietHoaDonNhap;
import model.HoaDonNhap;
import model.NhaCC;
import model.NhanVien;
import model.SanPham;

public class HoaDonNhapCreationDialog extends JDialog {

    // Define colors (reusing the palette)
    Color coffeeBrown = new Color(102, 51, 0);
    Color lightBeige = new Color(245, 245, 220);
    Color accentGreen = new Color(60, 179, 113); // Color for Add Product / Save
    Color accentOrange = new Color(255, 165, 0); // Color for Remove Product / Cancel
    Color accentBlue = new Color(30, 144, 255); // Optional accent
    Color darkGray = new Color(50, 50, 50);

    // UI Components - Header
    private JTextField txtMaHDN, txtNgayNhap, txtTongTien;
    private JComboBox<NhanVien> cbNhanVien; // Sử dụng JComboBox<NhanVien>
    private JComboBox<NhaCC> cbNhaCC; // Sử dụng JComboBox cho Nhà cung cấp

    // UI Components - Detail Line
    private JComboBox<SanPham> cbSanPham;
    private JTextField txtSoLuong, txtDonGia, txtKhuyenMai, txtThanhTienChiTiet;
    private JButton btnThemSP, btnXoaSP;

    // UI Components - Details Table
    private JTable tblChiTietHoaDonNhap;
    private DefaultTableModel tblChiTietModel;
    private JScrollPane scrollPaneChiTiet;

    // UI Components - Dialog Actions
    private JButton btnLuu, btnHuy;

    // DAOs
    private HoaDonNhapDAO hoaDonNhapDAO;
    // private ChiTietHoaDonNhapDAO chiTietHoaDonNhapDAO; // Khởi tạo ở HoaDonNhapDAO transaction
    private SanPhamDAO sanPhamDAO; // Cần để lấy danh sách SP và giá nhập
    private NhanVienDAO nhanVienDAO; // Cần để populate combobox NV
    private NhaCCDAO nhaCCDAO; // Cần để populate combobox NCC

    // Data structure for details
    private List<ChiTietHoaDonNhap> currentChiTietList;

    // Current logged-in employee info (passed from parent UI) - Logic is correct here
    private String employeeMaNV;
    private String employeeTenNV;

    private int currentTotalAmount = 0; // Biến lưu tổng tiền hiện tại

    // Currency format
    private DecimalFormat currencyFormatter = new DecimalFormat("#,##0"); // Format without VNĐ initially for parsing

    private boolean savedSuccessfully = false; // Flag to indicate successful save
    private SanPhamUI sanPhamUI;

    // Constructor
    // Nhận Frame cha và thông tin nhân viên tạo hóa đơn (MaNV, TenNV)
    public HoaDonNhapCreationDialog(Frame owner, String employeeMaNV, String employeeTenNV, SanPhamUI sanPhamUI) {
        super(owner, "Tạo Hóa đơn Nhập mới", true); // Modal dialog

        this.employeeMaNV = employeeMaNV; // Gán MaNV nhân viên tạo hóa đơn
        this.employeeTenNV = employeeTenNV; // Gán TenNV nhân viên tạo hóa đơn
        this.sanPhamUI = sanPhamUI;
        // Initialize DAOs
        hoaDonNhapDAO = new HoaDonNhapDAO();
        // chiTietHoaDonNhapDAO = new ChiTietHoaDonNhapDAO(); // Khởi tạo ở HoaDonNhapDAO transaction
        sanPhamDAO = new SanPhamDAO();
        nhanVienDAO = new NhanVienDAO(); // Initialize NhanVienDAO
        nhaCCDAO = new NhaCCDAO(); // Initialize NhaCCDAO


        currentChiTietList = new ArrayList<>(); // Initialize list


        setLayout(new BorderLayout(10, 10));
        setBackground(lightBeige);
        setPreferredSize(new Dimension(800, 600)); // Adjust size
        setResizable(false); // Prevent resizing


        // --- Header Panel ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        headerPanel.setBackground(lightBeige);
        headerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Thông tin Hóa đơn Nhập", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), coffeeBrown));


        headerPanel.add(createLabel("Mã HĐN:"));
        txtMaHDN = createTextField(100);
        txtMaHDN.setEditable(false); // Auto-generated unique ID
        // Generate a unique ID for the new invoice using DAO method
        txtMaHDN.setText(hoaDonNhapDAO.generateNextHoaDonNhapCode()); // Sử dụng DAO để tạo code
        headerPanel.add(txtMaHDN);


        headerPanel.add(createLabel("Ngày nhập:"));
        txtNgayNhap = createTextField(100);
        txtNgayNhap.setEditable(false); // Auto-filled with current date
        txtNgayNhap.setText(new SimpleDateFormat("dd/MM/yyyy").format(new Date())); // Set current date
        headerPanel.add(txtNgayNhap);

        headerPanel.add(createLabel("Nhân viên:"));
        cbNhanVien = new JComboBox<>();
        populateNhanVienComboBox(); // Populate with NhanVien objects
        cbNhanVien.setPreferredSize(new Dimension(150, 25));
        cbNhanVien.setBackground(Color.WHITE);
        cbNhanVien.setForeground(darkGray);
        cbNhanVien.setEnabled(false); // Assuming employee is the logged-in user and cannot be changed
         // Set the current employee in the combo box using the passed employeeMaNV
         // This requires the populateNhanVienComboBox to add NhanVien objects with correct MaNV
         setComboBoxSelectedItem(cbNhanVien, this.employeeMaNV);
        headerPanel.add(cbNhanVien);


        headerPanel.add(createLabel("Nhà cung cấp:"));
        cbNhaCC = new JComboBox<>(); // Using JComboBox<NhaCC>
        populateNhaCCComboBox(); // Populate with NhaCC objects
        cbNhaCC.setPreferredSize(new Dimension(150, 25));
        cbNhaCC.setBackground(Color.WHITE);
        cbNhaCC.setForeground(darkGray);
        headerPanel.add(cbNhaCC);

        headerPanel.add(createLabel("Tổng tiền:"));
        txtTongTien = createTextField(100);
        txtTongTien.setEditable(false); // Calculated total
        txtTongTien.setText("0 VNĐ");
        txtTongTien.setHorizontalAlignment(SwingConstants.RIGHT); // Align right for currency
        headerPanel.add(txtTongTien);

        add(headerPanel, BorderLayout.NORTH);


        // --- Details Panel ---
        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBackground(lightBeige);
        detailsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Chi tiết Hóa đơn Nhập", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), coffeeBrown));


        // Detail Input Panel
        JPanel detailInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        detailInputPanel.setBackground(lightBeige);

        detailInputPanel.add(createLabel("Sản phẩm:"));
        cbSanPham = new JComboBox<>(); // Using JComboBox<SanPham>
        populateSanPhamComboBox(); // Populate with SanPham objects
        cbSanPham.setPreferredSize(new Dimension(180, 25));
        cbSanPham.setBackground(Color.WHITE);
        cbSanPham.setForeground(darkGray);
        cbSanPham.addActionListener(e -> sanPhamSelected()); // Auto-fill DonGia when product selected
        detailInputPanel.add(cbSanPham);

        detailInputPanel.add(createLabel("Số lượng:"));
        txtSoLuong = createTextField(60);
        txtSoLuong.setHorizontalAlignment(SwingConstants.RIGHT);
        txtSoLuong.setText("1"); // Default quantity
        // Add listeners to recalculate total on key press or focus change
        txtSoLuong.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                  calculateDetailLineTotal();
             }
        });
        txtSoLuong.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                  calculateDetailLineTotal();
             }
        });
        detailInputPanel.add(txtSoLuong);

        detailInputPanel.add(createLabel("Đơn giá nhập:")); // Change label from Đơn giá bán
        txtDonGia = createTextField(80);
        txtDonGia.setHorizontalAlignment(SwingConstants.RIGHT);
        txtDonGia.setText("0");
        // Add listeners to recalculate total on key press or focus change
         txtDonGia.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                  calculateDetailLineTotal();
             }
         });
         txtDonGia.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                  calculateDetailLineTotal();
             }
         });
        detailInputPanel.add(txtDonGia);

        detailInputPanel.add(createLabel("Khuyến mãi (%):"));
        txtKhuyenMai = createTextField(50);
        txtKhuyenMai.setHorizontalAlignment(SwingConstants.RIGHT);
        txtKhuyenMai.setText("0");
        // Add listeners to recalculate total on key press or focus change
         txtKhuyenMai.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                  calculateDetailLineTotal();
             }
         });
         txtKhuyenMai.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                  calculateDetailLineTotal();
             }
         });
        detailInputPanel.add(txtKhuyenMai);

        detailInputPanel.add(createLabel("Thành tiền:"));
        txtThanhTienChiTiet = createTextField(100);
        txtThanhTienChiTiet.setEditable(false); // Calculated
        txtThanhTienChiTiet.setHorizontalAlignment(SwingConstants.RIGHT);
        txtThanhTienChiTiet.setText("0");
        detailInputPanel.add(txtThanhTienChiTiet);


        btnThemSP = createButton("Thêm SP");
        styleButton(btnThemSP, accentGreen, Color.WHITE);
        btnThemSP.addActionListener(e -> themChiTietHoaDonNhap());
        detailInputPanel.add(btnThemSP);

        btnXoaSP = createButton("Xóa SP");
        styleButton(btnXoaSP, accentOrange, Color.WHITE);
        btnXoaSP.addActionListener(e -> xoaChiTietHoaDonNhap());
        detailInputPanel.add(btnXoaSP);


        detailsPanel.add(detailInputPanel, BorderLayout.NORTH);

        // Details Table
        // Column names matching ChiTietHoaDonNhap plus TenSP from join
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

        detailsPanel.add(scrollPaneChiTiet, BorderLayout.CENTER);

        add(detailsPanel, BorderLayout.CENTER);


        // --- Dialog Action Panel ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        actionPanel.setBackground(lightBeige);

        btnLuu = createButton("Lưu Hóa đơn");
        styleButton(btnLuu, accentGreen, Color.WHITE);
        btnLuu.addActionListener(e -> luuHoaDonNhap());
        actionPanel.add(btnLuu);

        btnHuy = createButton("Hủy");
        styleButton(btnHuy, accentOrange, Color.WHITE);
        btnHuy.addActionListener(e -> dispose()); // Close dialog
        actionPanel.add(btnHuy);

        add(actionPanel, BorderLayout.SOUTH);

        // Initial setup
         clearDetailInputFields(); // Clear detail inputs
         calculateGrandTotal(); // Set initial total to 0

        // Pack and center the dialog
        pack();
        setSize(1100, 750);
        setLocationRelativeTo(owner); // Center relative to the owner frame
    }

    // Helper method to create styled labels
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    // Helper method to create text fields with preferred width
    private JTextField createTextField(int preferredWidth) {
        JTextField textField = new JTextField();
        textField.setFont(new Font("Arial", Font.PLAIN, 12));
        textField.setPreferredSize(new Dimension(preferredWidth, 25));
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


     // Populate ComboBoxes from DAOs
     private void populateNhanVienComboBox() {
         List<NhanVien> nhanVienList = nhanVienDAO.getAllNhanVien(); // Cần phương thức getAllNhanVien() trong NhanVienDAO
         cbNhanVien.removeAllItems();
         if (nhanVienList != null) {
             for (NhanVien nv : nhanVienList) {
                 cbNhanVien.addItem(nv); // Add NhanVien objects. Default renderer shows toString() or use custom
             }
         }
          // Set default renderer to display TenNV
          cbNhanVien.setRenderer(new DefaultListCellRenderer() {
              @Override
              public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                  super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                  if (value instanceof NhanVien) {
                      setText(((NhanVien) value).getTenNV()); // Display TenNV
                  } else if (value == null) {
                      setText("-- Chọn Nhân viên --");
                  }
                  return this;
              }
          });
         cbNhanVien.setSelectedItem(null); // Set initial selection to null/placeholder
     }

     private void populateNhaCCComboBox() {
         List<NhaCC> nhaCCList = nhaCCDAO.getAllNhaCC(); // Cần phương thức getAllNhaCC() trong NhaCCDAO
         cbNhaCC.removeAllItems();
         if (nhaCCList != null) {
             for (NhaCC ncc : nhaCCList) {
                 cbNhaCC.addItem(ncc); // Add NhaCC objects
             }
         }
          // Set default renderer to display TenNCC
          cbNhaCC.setRenderer(new DefaultListCellRenderer() {
              @Override
              public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                  super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                  if (value instanceof NhaCC) {
                      setText(((NhaCC) value).getTenNCC()); // Display TenNCC
                  } else if (value == null) {
                      setText("-- Chọn Nhà cung cấp --");
                  }
                  return this;
              }
          });
         cbNhaCC.setSelectedItem(null); // Set initial selection to null/placeholder
     }

     private void populateSanPhamComboBox() {
         List<SanPham> sanPhamList = sanPhamDAO.getAllSanPham(); // Cần phương thức getAllSanPham() trong SanPhamDAO
         cbSanPham.removeAllItems();
         if (sanPhamList != null) {
             for (SanPham sp : sanPhamList) {
                 cbSanPham.addItem(sp); // Add SanPham objects
             }
         }
          // Set default renderer to display TenSP
          cbSanPham.setRenderer(new DefaultListCellRenderer() {
              @Override
              public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                  super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                  if (value instanceof SanPham) {
                      setText(((SanPham) value).getTenSP()); // Display TenSP
                  } else if (value == null) {
                      setText("-- Chọn Sản phẩm --");
                  }
                  return this;
              }
          });
         cbSanPham.setSelectedItem(null); // Set initial selection to null/placeholder
     }


     // Helper to set a specific item in a combo box based on ID
      private <T> void setComboBoxSelectedItem(JComboBox<T> comboBox, String id) {
          if (id == null || id.isEmpty()) return;
          for (int i = 0; i < comboBox.getItemCount(); i++) {
              T item = comboBox.getItemAt(i);
              // Kiểm tra kiểu dữ liệu của item trước khi ép kiểu và so sánh ID
              if (item instanceof NhanVien && ((NhanVien) item).getMaNV().equals(id)) {
                  comboBox.setSelectedItem(item);
                  return;
              } else if (item instanceof NhaCC && ((NhaCC) item).getMaNCC().equals(id)) {
                   comboBox.setSelectedItem(item);
                   return;
              } else if (item instanceof SanPham && ((SanPham) item).getMaSP().equals(id)) {
                   comboBox.setSelectedItem(item);
                   return;
              }
          }
          // DEBUG: Log a warning if the item with the given ID was not found
          System.err.println("DEBUG (HoaDonNhapCreationDialog): Item with ID '" + id + "' not found in ComboBox.");
      }


     // Action when a SanPham is selected in the combo box
     private void sanPhamSelected() {
          SanPham selectedSP = (SanPham) cbSanPham.getSelectedItem();
          if (selectedSP != null) {
              // Lấy Đơn giá nhập (Gianhap) từ đối tượng SanPham được chọn
              // Ensure SanPham model has getGianhap() method
              txtDonGia.setText(currencyFormatter.format(selectedSP.getGianhap()));
              txtSoLuong.setText("1"); // Default quantity
              txtKhuyenMai.setText("0"); // Default discount
              calculateDetailLineTotal(); // Calculate initial total
          } else {
              txtDonGia.setText("0");
              txtSoLuong.setText("0");
              txtKhuyenMai.setText("0");
              txtThanhTienChiTiet.setText("0");
          }
     }

     // Calculate the total for a single detail line
     private void calculateDetailLineTotal() {
          try {
              int soLuong = Integer.parseInt(txtSoLuong.getText().trim());
              // Parse DonGia from text field (already formatted)
              double donGia = currencyFormatter.parse(txtDonGia.getText().trim()).doubleValue();
              double khuyenMaiPercent = Double.parseDouble(txtKhuyenMai.getText().trim());

              if (soLuong < 0 || donGia < 0 || khuyenMaiPercent < 0 || khuyenMaiPercent > 100) {
                  txtThanhTienChiTiet.setText("0");
                  return;
              }

              double thanhTien = soLuong * donGia * (1 - khuyenMaiPercent / 100.0);
               txtThanhTienChiTiet.setText(currencyFormatter.format(thanhTien));

          } catch (NumberFormatException | java.text.ParseException e) {
              txtThanhTienChiTiet.setText("0"); // Set to 0 if input is invalid
              // e.printStackTrace(); // Optional: Print stack trace for debugging
          }
     }


     // Add a detail line to the list and table
     private void themChiTietHoaDonNhap() {
          SanPham selectedSP = (SanPham) cbSanPham.getSelectedItem();
          if (selectedSP == null) {
              JOptionPane.showMessageDialog(this, "Vui lòng chọn một Sản phẩm.", "Lỗi Nhập liệu", JOptionPane.WARNING_MESSAGE);
              return;
          }

          try {
              String maSP = selectedSP.getMaSP();
              // String tenSP = selectedSP.getTenSP(); // TenSP is for display only in table
              int soLuong = Integer.parseInt(txtSoLuong.getText().trim());
              // Parse DonGia from text field (already formatted)
              // Assuming DonGia is int in your model, cast after parsing
              int donGia = currencyFormatter.parse(txtDonGia.getText().trim()).intValue();
              int khuyenMai = Integer.parseInt(txtKhuyenMai.getText().trim()); // Assuming KhuyenMai is int percentage

              if (soLuong <= 0) {
                   JOptionPane.showMessageDialog(this, "Số lượng phải lớn hơn 0.", "Lỗi Nhập liệu", JOptionPane.WARNING_MESSAGE);
                   return;
              }
              if (donGia < 0) {
                   JOptionPane.showMessageDialog(this, "Đơn giá nhập không được âm.", "Lỗi Nhập liệu", JOptionPane.WARNING_MESSAGE);
                   return;
              }
               if (khuyenMai < 0 || khuyenMai > 100) {
                    JOptionPane.showMessageDialog(this, "Khuyến mãi phải từ 0 đến 100.", "Lỗi Nhập liệu", JOptionPane.WARNING_MESSAGE);
                    return;
               }

              // Calculate ThanhTien
              int thanhTien = (int) Math.round(soLuong * donGia * (1 - khuyenMai / 100.0)); // Use Math.round for int cast

              // Check if product is already in the list, update quantity and total if it is
              boolean found = false;
              for (ChiTietHoaDonNhap ct : currentChiTietList) {
                  if (ct.getMaSP().equals(maSP)) {
                      // Update existing item
                      ct.setSoluong(ct.getSoluong() + soLuong);
                       // Recalculate ThanhTien for the updated item based on the original DonGia/KhuyenMai stored in ct
                       // If you want to update price/discount too, you would update ct.setDongia() and ct.setKhuyenmai() here first.
                       // The current logic keeps the original price/discount for the item in the list.
                       ct.setThanhtien((int) Math.round(ct.getSoluong() * ct.getDongia() * (1 - ct.getKhuyenmai() / 100.0)));
                       found = true;
                       break;
                  }
              }

              if (!found) {
                  // Add as a new item
                  ChiTietHoaDonNhap chiTiet = new ChiTietHoaDonNhap();
                  // MaHDN will be set when saving the main invoice
                  chiTiet.setMaSP(maSP);
                  // TenSP is for UI display only, not typically stored in ChiTiet model
                  chiTiet.setSoluong(soLuong);
                  chiTiet.setDongia(donGia); // Store DonGia nhập into detail
                  chiTiet.setKhuyenmai(khuyenMai); // Store KhuyenMai into detail
                  chiTiet.setThanhtien(thanhTien); // Store calculated ThanhTien
                  currentChiTietList.add(chiTiet);
              }

              // Refresh the details table and calculate grand total
              refreshChiTietTable();
              calculateGrandTotal();
              clearDetailInputFields(); // Clear detail input area for next item

          } catch (NumberFormatException | java.text.ParseException e) {
              JOptionPane.showMessageDialog(this, "Vui lòng nhập số lượng, đơn giá, khuyến mãi hợp lệ.", "Lỗi Nhập liệu", JOptionPane.WARNING_MESSAGE);
              // e.printStackTrace(); // Optional: Print stack trace for debugging
          }
     }

     // Remove a detail line from the list and table
     private void xoaChiTietHoaDonNhap() {
          int selectedRow = tblChiTietHoaDonNhap.getSelectedRow();
          if (selectedRow != -1) {
              // Xóa khỏi danh sách dựa trên chỉ số dòng được chọn
              currentChiTietList.remove(selectedRow);
              // Refresh the details table and calculate grand total
              refreshChiTietTable();
              calculateGrandTotal();

          } else {
              JOptionPane.showMessageDialog(this, "Vui lòng chọn một dòng Chi tiết để xóa.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
          }
     }

     // Refresh the details table based on the currentChiTietList
     private void refreshChiTietTable() {
          tblChiTietModel.setRowCount(0); // Clear existing data
          if (currentChiTietList != null) {
              // Need SanPhamDAO to get product names for the table column "Tên SP"
              SanPhamDAO spDao = new SanPhamDAO(); // Initialize temporarily to get names (or pass from outside)

              for (ChiTietHoaDonNhap ct : currentChiTietList) {
                  Vector<Object> row = new Vector<>();
                  row.add(ct.getMaSP());
                  // Get product name to display
                  String tenSP = "N/A";
                  // Ensure SanPhamDAO has getSanPhamById(String maSP) method
                  SanPham sp = spDao.getSanPhamById(ct.getMaSP());
                  if(sp != null) tenSP = sp.getTenSP();
                  row.add(tenSP); // Add product name
                  row.add(ct.getSoluong());
                  row.add(currencyFormatter.format(ct.getDongia())); // Format currency
                  row.add(ct.getKhuyenmai());
                  row.add(currencyFormatter.format(ct.getThanhtien())); // Format currency
                  tblChiTietModel.addRow(row);
              }
          }
     }

     // Calculate the grand total from the detail lines
     private void calculateGrandTotal() {
          currentTotalAmount = 0;
          if (currentChiTietList != null) {
              for (ChiTietHoaDonNhap ct : currentChiTietList) {
                   currentTotalAmount += ct.getThanhtien(); // Sum up ThanhTien from details
              }
          }
          txtTongTien.setText(currencyFormatter.format(currentTotalAmount) + " VNĐ");
     }

     // Clear input fields for a single detail line
     private void clearDetailInputFields() {
          cbSanPham.setSelectedItem(null); // Clear selected product
          txtSoLuong.setText("1"); // Reset quantity to 1
          txtDonGia.setText("0");
          txtKhuyenMai.setText("0");
          txtThanhTienChiTiet.setText("0");
     }


    // Action for "Lưu Hóa đơn" button
    private void luuHoaDonNhap() {
        // Basic Validation - Header
        NhanVien selectedNV = (NhanVien) cbNhanVien.getSelectedItem(); // Although disabled, check if an item is somehow selected
        NhaCC selectedNCC = (NhaCC) cbNhaCC.getSelectedItem();

        // Check if the employee info passed to the dialog is valid
        if (this.employeeMaNV == null || this.employeeMaNV.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Lỗi hệ thống: Không xác định được Mã Nhân viên tạo hóa đơn.\nVui lòng đăng nhập lại.", "Lỗi Nhập liệu", JOptionPane.WARNING_MESSAGE);
             return;
        }

        if (selectedNCC == null) {
             JOptionPane.showMessageDialog(this, "Vui lòng chọn Nhà Cung Cấp.", "Lỗi Nhập liệu", JOptionPane.WARNING_MESSAGE);
             return;
        }


        // Basic Validation - Details
        if (currentChiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hóa đơn nhập phải có ít nhất một chi tiết sản phẩm.", "Lỗi Nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Create HoaDonNhap object (Header)
        HoaDonNhap newHoaDon = new HoaDonNhap();
        newHoaDon.setMaHDN(txtMaHDN.getText().trim()); // Using the auto-generated code
        newHoaDon.setMaNV(this.employeeMaNV); // <-- USE EMPLOYEE ID PASSED FROM PARENT UI
        newHoaDon.setMaNCC(selectedNCC.getMaNCC()); // Get MaNCC from selected NhaCC object
        newHoaDon.setNgayNhap(new Date()); // Use current date
        newHoaDon.setTongTien(currentTotalAmount); // Total calculated from details


        // Save the entire invoice (header and details) in a transaction
        // Call the transactional save method in HoaDonNhapDAO
        // This method should handle saving HoaDonNhap header and iterating through currentChiTietList
        // to save each ChiTietHoaDonNhap and UPDATE SanPham stock (increase stock for import).
        // Ensure hoaDonNhapDAO.saveHoaDonNhapTransaction(HoaDonNhap hdn, List<ChiTietHoaDonNhap> cthdns) is implemented transactionally.
        boolean success = hoaDonNhapDAO.saveHoaDonNhapTransaction(newHoaDon, currentChiTietList); // Call the transactional method

        if (success) {
            JOptionPane.showMessageDialog(this, "Lưu hóa đơn nhập thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            savedSuccessfully = true; // Set flag
            dispose(); // Close the dialog after successful save
        } else {
             // Error message from DAO should ideally be more specific if saveTransaction fails
             // JOptionPane.showMessageDialog(this, "Lưu hóa đơn nhập thất bại. Vui lòng kiểm tra lại hoặc có lỗi hệ thống (console).", "Lỗi", JOptionPane.ERROR_MESSAGE);
             // Log error message printed by DAO if any
             System.err.println("Lưu hóa đơn nhập thất bại."); // Generic failure message
             savedSuccessfully = false; // Ensure flag is false
        }
    }

    // Method to check if the dialog saved successfully (used by parent UI)
    public boolean isSavedSuccessfully() {
        return savedSuccessfully;
    }


    // Main method for testing (Optional - Comment out when integrated into MainApplicationFrame)

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
             // This test requires a running database with necessary tables (NhanVien, NhaCC, SanPham, HoaDonNhap, CTHoaDonNhap)
             // and implemented DAO methods (getAllNhanVien, getAllNhaCC, getAllSanPham, generateNextHoaDonNhapCode, saveHoaDonNhapTransaction, getSanPhamById).

             // Pass a dummy MaNV and TenNV for testing
    //          String dummyMaNV = "NV01"; // Replace with a valid MaNV from your NhanVien table for testing
    //          String dummyTenNV = "Test Employee"; // Replace with corresponding TenNV

    //          if (new NhanVienDAO().getNhanVienById(dummyMaNV) == null) {
    //               System.err.println("Lỗi: Không tìm thấy nhân viên với Mã NV '" + dummyMaNV + "'. Vui lòng cập nhật MaNV trong main method để test.");
    //               return; // Stop if dummy employee not found
    //          }


    //          JFrame dummyFrame = new JFrame(); // Dummy owner frame
    //          dummyFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    //          dummyFrame.setSize(100, 100); // Minimal size
    //          dummyFrame.setVisible(false); // Keep hidden

    //         //  HoaDonNhapCreationDialog dialog = new HoaDonNhapCreationDialog(dummyFrame, dummyMaNV, dummyTenNV);
    //          dialog.setVisible(true);

    //          // After dialog is closed, check if it was saved (optional in test main)
    //          if (dialog.isSavedSuccessfully()) {
    //              System.out.println("Dialog closed successfully after saving.");
    //          } else {
    //               System.out.println("Dialog closed, save was not successful or cancelled.");
    //          }

    //          // dummyFrame.dispose(); // Dispose dummy frame
    //     });
    // }

    });
}
}
