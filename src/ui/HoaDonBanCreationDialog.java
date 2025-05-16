package ui;

import dao.HoaDonBanDAO;
import dao.KhachHangDAO;
import dao.NhanVienDAO;
import dao.SanPhamDAO;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder; // Import TitledBorder
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.CTHoaDonBan;
import model.HoaDonBan;
import model.KhachHang;
import model.NhanVien; // Import NhanVien
import model.SanPham;
// Loại bỏ import model.ACC; // Remove ACC import


// Make sure this class is in its OWN FILE: HoaDonBanCreationDialog.java
public class HoaDonBanCreationDialog extends JDialog {

    // Define colors (reusing the palette)
    Color coffeeBrown = new Color(102, 51, 0);
    Color lightBeige = new Color(245, 245, 220);
    Color accentGreen = new Color(60, 179, 113); // Example color
    Color accentOrange = new Color(255, 165, 0);
    Color darkGray = new Color(50, 50, 50);
    Color accentBlue = new Color(30, 144, 255);


    // UI Components (Header Info)
    private JLabel lblMaHDB;
    private JLabel lblNgayBan; // Display current date/time
    private JComboBox<NhanVien> cbNhanVien; // ComboBox for selecting Employee
    private JComboBox<KhachHang> cbKhachHang; // ComboBox for selecting Customer

    // UI Components (Product Selection for Details)
    private JTextField txtMaSP; // Input for product code
    private JTextField txtTenSP; // Display product name (auto-filled)
    private JTextField txtDonGiaBan; // Display selling price (auto-filled)
    private JTextField txtSoLuongBan; // Input for quantity
    private JTextField txtKhuyenMai; // Input for discount (percentage?)
    private JButton btnAddProductToInvoice; // Button to add product to detail table

    // UI Components (Invoice Details Table)
    private JTable chiTietTable;
    private DefaultTableModel chiTietTableModel;

    // UI Components (Footer Info)
    private JLabel lblTongTien; // Display total amount

    // Action Buttons
    private JButton btnSaveInvoice;
    private JButton btnCancel;

    // Data Access Objects
    private HoaDonBanDAO hoaDonBanDAO;
    private NhanVienDAO nhanVienDAO;
    private KhachHangDAO khachHangDAO;
    private SanPhamDAO sanPhamDAO;

    // List to hold current invoice details being built
    private List<CTHoaDonBan> currentChiTietList;

    // Variable to hold the calculated total amount
    private int currentTotalAmount = 0;

    // FIX: Change type from ACC to NhanVien
    // Logged-in user information (passed from parent UI)
    private NhanVien creator; // Changed from ACC loggedInAccount

    // Date formatter for displaying date/time
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0");

    // Flag to indicate if the save was successful
    private boolean savedSuccessfully = false; // Add savedSuccessfully flag


    // Constructor for creating a new HoaDonBan
    // FIX: Change parameter type from ACC to NhanVien
    public HoaDonBanCreationDialog(JFrame owner, NhanVien creator) { // Accept NhanVien object
        super(owner, "Tạo Hóa đơn Bán Mới", true);
        this.creator = creator; // Store the logged-in NhanVien object
        // Loại bỏ dòng: this.loggedInAccount = account;

        // Initialize DAOs
        hoaDonBanDAO = new HoaDonBanDAO();
        nhanVienDAO = new NhanVienDAO();
        khachHangDAO = new KhachHangDAO();
        sanPhamDAO = new SanPhamDAO();

        currentChiTietList = new ArrayList<>();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(owner);

        // --- Content Pane ---
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBackground(lightBeige);
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));


        // --- Header Panel (Invoice Info) ---
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(lightBeige);
        headerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Thông tin Hóa đơn", 0, 0, new Font("Arial", Font.BOLD, 14), coffeeBrown));


        GridBagConstraints gbcHeader = new GridBagConstraints();
        gbcHeader.insets = new Insets(5, 5, 5, 5);
        gbcHeader.fill = GridBagConstraints.HORIZONTAL;


        // Row 0: MaHDB, NgayBan
        gbcHeader.gridx = 0; gbcHeader.gridy = 0; gbcHeader.weightx = 0; headerPanel.add(createLabel("Mã HĐB:"), gbcHeader);
        gbcHeader.gridx = 1; gbcHeader.gridy = 0; gbcHeader.weightx = 1.0;
        // Generate MaHDB using the new sequential method from DAO
        String newMaHDB = hoaDonBanDAO.generateNextHoaDonBanCode(); // Call DAO method
         if ("ERROR_CODE".equals(newMaHDB)) { // Check for error code
              lblMaHDB = createValueLabel("Lỗi tạo mã!"); // Display error message
              // Initialize buttons to avoid NPE later if code generation fails
              btnAddProductToInvoice = new JButton("Thêm vào HĐ");
              btnSaveInvoice = new JButton("Lưu Hóa đơn");
              btnSaveInvoice.setEnabled(false); // Disable save button if code generation fails
              btnAddProductToInvoice.setEnabled(false); // Disable add product button
              JOptionPane.showMessageDialog(this, "Không thể tạo mã hóa đơn mới. Vui lòng kiểm tra kết nối CSDL và log lỗi.", "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
         } else {
              lblMaHDB = createValueLabel(newMaHDB);
              // Initialize buttons if code generation was successful
              btnAddProductToInvoice = new JButton("Thêm vào HĐ");
              btnSaveInvoice = new JButton("Lưu Hóa đơn");
         }
        headerPanel.add(lblMaHDB, gbcHeader);

        gbcHeader.gridx = 2; gbcHeader.gridy = 0; gbcHeader.weightx = 0; headerPanel.add(createLabel("Ngày bán:"), gbcHeader);
        gbcHeader.gridx = 3; gbcHeader.gridy = 0; gbcHeader.weightx = 1.0;
        lblNgayBan = createValueLabel(dateFormat.format(new Date()));
        headerPanel.add(lblNgayBan, gbcHeader);

        // Row 1: NhanVien, KhachHang
        gbcHeader.gridx = 0; gbcHeader.gridy = 1; gbcHeader.weightx = 0; headerPanel.add(createLabel("Nhân viên:"), gbcHeader);
        gbcHeader.gridx = 1; gbcHeader.gridy = 1; gbcHeader.weightx = 1.0;
        cbNhanVien = new JComboBox<>(); // Using JComboBox<NhanVien>
        cbNhanVien.setBackground(Color.WHITE);
        cbNhanVien.setForeground(darkGray);
        cbNhanVien.setEnabled(false); // Assuming employee is the logged-in user and cannot be changed
        headerPanel.add(cbNhanVien, gbcHeader);

        gbcHeader.gridx = 2; gbcHeader.gridy = 1; gbcHeader.weightx = 0; headerPanel.add(createLabel("Khách hàng:"), gbcHeader);
        gbcHeader.gridx = 3; gbcHeader.gridy = 1; gbcHeader.weightx = 1.0;
        cbKhachHang = new JComboBox<>(); // Using JComboBox<KhachHang>
        cbKhachHang.setBackground(Color.WHITE);
        cbKhachHang.setForeground(darkGray);
        headerPanel.add(cbKhachHang, gbcHeader);


        contentPane.add(headerPanel, BorderLayout.NORTH);


        // --- Center Panel (Product Selection and Details Table) ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(lightBeige);

        // Product Selection Panel
        JPanel productSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        productSelectionPanel.setBackground(lightBeige);
        productSelectionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Thêm sản phẩm vào hóa đơn", 0, 0, new Font("Arial", Font.BOLD, 14), coffeeBrown));


        productSelectionPanel.add(createLabel("Mã SP:"));
        txtMaSP = new JTextField(10);
        productSelectionPanel.add(txtMaSP);

        productSelectionPanel.add(createLabel("Tên SP:"));
        txtTenSP = new JTextField(15);
        txtTenSP.setEditable(false);
        productSelectionPanel.add(txtTenSP);

        productSelectionPanel.add(createLabel("Đơn giá:"));
        txtDonGiaBan = new JTextField(8);
        txtDonGiaBan.setEditable(false);
        productSelectionPanel.add(txtDonGiaBan);

        productSelectionPanel.add(createLabel("Số lượng:"));
        txtSoLuongBan = new JTextField(5);
        productSelectionPanel.add(txtSoLuongBan);

        productSelectionPanel.add(createLabel("Khuyến mãi (%):"));
        txtKhuyenMai = new JTextField(5);
        txtKhuyenMai.setText("0");
        productSelectionPanel.add(txtKhuyenMai);

        // btnAddProductToInvoice was initialized above
        styleButton(btnAddProductToInvoice, accentGreen, Color.WHITE);
        productSelectionPanel.add(btnAddProductToInvoice);


        centerPanel.add(productSelectionPanel, BorderLayout.NORTH);


        // Invoice Details Table
        JPanel detailsTablePanel = new JPanel(new BorderLayout());
        detailsTablePanel.setBackground(lightBeige);
        detailsTablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Chi tiết Hóa đơn", 0, 0, new Font("Arial", Font.BOLD, 14), coffeeBrown));


        // Table Model for details: MaSP, TenSP, Soluong, DonGiaBan, KhuyenMai, ThanhTien
        chiTietTableModel = new DefaultTableModel(new Object[]{"Mã SP", "Tên SP", "Số lượng", "Đơn giá", "Khuyến mãi (%)", "Thành tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
             @Override
             public Class<?> getColumnClass(int columnIndex) {
                  // Assuming numeric columns are Soluong (2), DonGia (3), KhuyenMai (4), ThanhTien (5)
                  if (columnIndex == 2 || columnIndex == 3 || columnIndex == 4 || columnIndex == 5) return Integer.class;
                  return super.getColumnClass(columnIndex);
             }
        };
        chiTietTable = new JTable(chiTietTableModel);

        // Apply renderers for alignment
        DefaultTableCellRenderer detailsRendererRight = new DefaultTableCellRenderer();
        detailsRendererRight.setHorizontalAlignment(SwingConstants.RIGHT);
        chiTietTable.getColumnModel().getColumn(2).setCellRenderer(detailsRendererRight); // SoLuong
        chiTietTable.getColumnModel().getColumn(3).setCellRenderer(detailsRendererRight); // DonGia
        chiTietTable.getColumnModel().getColumn(5).setCellRenderer(detailsRendererRight); // ThanhTien

        DefaultTableCellRenderer detailsRendererCenter = new DefaultTableCellRenderer();
        detailsRendererCenter.setHorizontalAlignment(SwingConstants.CENTER);
        chiTietTable.getColumnModel().getColumn(4).setCellRenderer(detailsRendererCenter); // KhuyenMai (%)

         // Style table header and cells (add basic styling)
         chiTietTable.getTableHeader().setBackground(coffeeBrown);
         chiTietTable.getTableHeader().setForeground(Color.WHITE);
         chiTietTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
         chiTietTable.setRowHeight(20); // Slightly larger row height
         chiTietTable.setFillsViewportHeight(true); // Show background color in empty area


        JScrollPane detailsScrollPane = new JScrollPane(chiTietTable);
        detailsTablePanel.add(detailsScrollPane, BorderLayout.CENTER);

        centerPanel.add(detailsTablePanel, BorderLayout.CENTER);

        contentPane.add(centerPanel, BorderLayout.CENTER);


        // --- Footer Panel (Total and Action Buttons) ---
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(lightBeige);


        // Total Panel
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        totalPanel.setBackground(lightBeige);
        totalPanel.add(createLabel("Tổng tiền:"));
        lblTongTien = createValueLabel(currencyFormat.format(currentTotalAmount));
        lblTongTien.setFont(new Font("Arial", Font.BOLD, 16));
        lblTongTien.setForeground(accentGreen);
        totalPanel.add(lblTongTien);

        footerPanel.add(totalPanel, BorderLayout.NORTH);


        // Action Buttons Panel (Save/Cancel)
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionButtonPanel.setBackground(lightBeige);

        // btnSaveInvoice was initialized above
        styleButton(btnSaveInvoice, accentGreen, Color.WHITE);
        actionButtonPanel.add(btnSaveInvoice);

        btnCancel = new JButton("Hủy");
        styleButton(btnCancel, darkGray, Color.WHITE);
        actionButtonPanel.add(btnCancel);

        footerPanel.add(actionButtonPanel, BorderLayout.SOUTH);

        contentPane.add(footerPanel, BorderLayout.SOUTH);


        setContentPane(contentPane);

        // --- Event Listeners ---
        btnCancel.addActionListener(e -> dispose());

        // Add listener to save button ONLY if code generation was successful
         if (!"Lỗi tạo mã!".equals(lblMaHDB.getText())) { // Check the displayed text
             btnSaveInvoice.addActionListener(e -> saveHoaDonBan());
         }


        txtMaSP.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String maSP = txtMaSP.getText().trim();
                    if (!maSP.isEmpty()) {
                        loadProductInfo(maSP);
                    } else {
                        txtTenSP.setText("");
                        txtDonGiaBan.setText("");
                    }
                }
            }
        });

        txtMaSP.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String maSP = txtMaSP.getText().trim();
                if (!maSP.isEmpty() && txtTenSP.getText().trim().isEmpty()) {
                    loadProductInfo(maSP);
                } else if (maSP.isEmpty()) {
                    txtTenSP.setText("");
                    txtDonGiaBan.setText("");
                }
            }
        });

         // Enable add product button only if code generation was successful
         if (!"Lỗi tạo mã!".equals(lblMaHDB.getText())) { // Check the displayed text
             btnAddProductToInvoice.addActionListener(e -> addProductToInvoice());
         }


        chiTietTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = chiTietTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        removeProductFromInvoice(selectedRow);
                    }
                }
            }
        });


        // --- Initial Data Loading ---
        loadNhanVien();
        loadKhachHang();

        // FIX: Select the logged-in employee in the combo box
        if (this.creator != null && this.creator.getMaNV() != null) {
             selectNhanVienInComboBox(this.creator.getMaNV());
        } else {
             System.err.println("Thông tin nhân viên tạo hóa đơn (creator) không hợp lệ.");
             // Optionally disable save/add buttons if creator info is missing
             btnSaveInvoice.setEnabled(false);
             btnAddProductToInvoice.setEnabled(false);
             JOptionPane.showMessageDialog(this, "Không thể xác định thông tin nhân viên tạo hóa đơn.", "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
        }


        calculateTotal();

        setModal(true);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    private JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
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


    private void loadNhanVien() {
        cbNhanVien.removeAllItems();
        List<NhanVien> nhanVienList = nhanVienDAO.getAllNhanVien();
        if (nhanVienList != null) {
            for (NhanVien nv : nhanVienList) {
                cbNhanVien.addItem(nv);
            }
        } else {
            System.err.println("Không lấy được danh sách nhân viên từ CSDL.");
        }
         // Set renderer to display TenNV
         cbNhanVien.setRenderer(new DefaultListCellRenderer() {
             @Override
             public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                 super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                 if (value instanceof NhanVien) {
                     setText(((NhanVien) value).getTenNV());
                 } else {
                     setText(""); // Handle null or other types
                 }
                 return this;
             }
         });
    }

    private void selectNhanVienInComboBox(String maNV) {
        if (maNV == null || maNV.isEmpty()) return;

        for (int i = 0; i < cbNhanVien.getItemCount(); i++) {
            NhanVien nv = cbNhanVien.getItemAt(i);
            if (nv != null && nv.getMaNV() != null && nv.getMaNV().equals(maNV)) {
                cbNhanVien.setSelectedItem(nv);
                return;
            }
        }
        System.err.println("Mã nhân viên '" + maNV + "' không tìm thấy trong danh sách ComboBox.");
         // Optionally add the employee if not found (e.g., if DB has an employee not in the standard list)
         // NhanVien employeeFromDb = nhanVienDAO.getNhanVienById(maNV);
         // if (employeeFromDb != null) {
         //     cbNhanVien.addItem(employeeFromDb);
         //     cbNhanVien.setSelectedItem(employeeFromDb);
         // }
    }


    private void loadKhachHang() {
        cbKhachHang.removeAllItems();
        List<KhachHang> khachHangList = khachHangDAO.getAllKhachHang();
        if (khachHangList != null) {
             // Add a default "Guest" or "Select Customer" option
             cbKhachHang.addItem(null); // Add null item for placeholder
            for (KhachHang kh : khachHangList) {
                cbKhachHang.addItem(kh);
            }
        } else {
             // Add only the default "Guest" or "Select Customer" option if loading fails
             cbKhachHang.addItem(null);
            System.err.println("Không lấy được danh sách khách hàng từ CSDL.");
        }
         // Set renderer to display TenKH
         cbKhachHang.setRenderer(new DefaultListCellRenderer() {
             @Override
             public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                 super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                 if (value instanceof KhachHang) {
                     setText(((KhachHang) value).getTenkhach());
                 } else if (value == null) {
                     setText("-- Chọn Khách hàng --"); // Placeholder text
                 } else {
                     setText(""); // Handle other cases
                 }
                 return this;
             }
         });
         cbKhachHang.setSelectedItem(null); // Select the placeholder initially
    }


    private void loadProductInfo(String maSP) {
        SanPham product = sanPhamDAO.getSanPhamById(maSP);

        if (product != null) {
            txtTenSP.setText(product.getTenSP());
            txtDonGiaBan.setText(String.valueOf(product.getGiaban())); // Assuming getGiaban() returns int or double
            System.out.println("Đã tải thông tin sản phẩm: " + product.getTenSP());
        } else {
            txtTenSP.setText("");
            txtDonGiaBan.setText("");
            JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm với mã: " + maSP, "Thông báo", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addProductToInvoice() {
        String maSP = txtMaSP.getText().trim();
        String tenSP = txtTenSP.getText().trim();
        String donGiaStr = txtDonGiaBan.getText().trim();
        String soLuongStr = txtSoLuongBan.getText().trim();
        String khuyenMaiStr = txtKhuyenMai.getText().trim();

        if (maSP.isEmpty() || tenSP.isEmpty() || donGiaStr.isEmpty() || soLuongStr.isEmpty() || khuyenMaiStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin sản phẩm và số lượng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int soLuong, donGia, khuyenMaiPercent;
        try {
            soLuong = Integer.parseInt(soLuongStr);
            if (soLuong <= 0) throw new NumberFormatException("Số lượng phải lớn hơn 0.");

            donGia = Integer.parseInt(donGiaStr); // Assuming DonGiaBan is int

            khuyenMaiPercent = Integer.parseInt(khuyenMaiStr);
            if (khuyenMaiPercent < 0 || khuyenMaiPercent > 100) throw new NumberFormatException("Khuyến mãi phải từ 0 đến 100%.");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng, Đơn giá, Khuyến mãi phải là số nguyên hợp lệ. " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check stock *before* adding/updating the list
        int currentStock = sanPhamDAO.getStockQuantity(maSP);
        // Calculate total quantity of this product already in the currentChiTietList
        int quantityInList = 0;
        for(CTHoaDonBan ct : currentChiTietList) {
            if (ct.getMaSP().equals(maSP)) {
                quantityInList = ct.getSoluong();
                break;
            }
        }

        if (soLuong > currentStock - quantityInList) { // Check if adding `soLuong` exceeds available stock
             JOptionPane.showMessageDialog(this, "Số lượng tồn kho không đủ. Chỉ còn " + (currentStock - quantityInList) + " sản phẩm có thể thêm.", "Lỗi tồn kho", JOptionPane.WARNING_MESSAGE);
             return;
        }


        boolean found = false;
        for (CTHoaDonBan existingCt : currentChiTietList) {
            if (existingCt.getMaSP().equals(maSP)) {
                 // Product already exists, update quantity
                 // No confirmation dialog needed if we already checked stock
                 existingCt.setSoluong(existingCt.getSoluong() + soLuong);
                 // Recalculate ThanhTien for the updated item based on its original price/discount
                 // If you wanted to update price/discount, you'd set them here:
                 // existingCt.setGiaban(donGia); existingCt.setKhuyenmai(khuyenMaiPercent);
                 double updatedThanhTienDouble = (double) existingCt.getGiaban() * existingCt.getSoluong() * (100 - existingCt.getKhuyenmai()) / 100.0;
                 existingCt.setThanhtien((int) Math.round(updatedThanhTienDouble));

                 // Update the table row
                 for (int i = 0; i < chiTietTableModel.getRowCount(); i++) {
                     if (chiTietTableModel.getValueAt(i, 0).equals(maSP)) {
                         chiTietTableModel.setValueAt(existingCt.getSoluong(), i, 2); // Update quantity column
                         chiTietTableModel.setValueAt(existingCt.getThanhtien(), i, 5); // Update ThanhTien column
                         break;
                     }
                 }

                 found = true;
                 break;
            }
        }


        if (!found) {
            // Add as a new item
            double thanhTienDouble = (double) donGia * soLuong * (100 - khuyenMaiPercent) / 100.0;
            int thanhTien = (int) Math.round(thanhTienDouble);

            CTHoaDonBan newCt = new CTHoaDonBan(null, maSP, soLuong, thanhTien, khuyenMaiPercent);
            newCt.setTenSP(tenSP); // Store TenSP for UI display
            newCt.setGiaban(donGia); // Store DonGiaBan in the detail object

            currentChiTietList.add(newCt);
            chiTietTableModel.addRow(new Object[]{
                newCt.getMaSP(),
                newCt.getTenSP(),
                newCt.getSoluong(),
                newCt.getGiaban(), // Display Giaban
                newCt.getKhuyenmai(),
                newCt.getThanhtien()
            });
        }

        calculateTotal();

        // Clear product input fields
        txtMaSP.setText("");
        txtTenSP.setText("");
        txtDonGiaBan.setText("");
        txtSoLuongBan.setText("");
        txtKhuyenMai.setText("0");
        txtMaSP.requestFocusInWindow(); // Set focus back to MaSP field
    }

    private void removeProductFromInvoice(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < currentChiTietList.size()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                     "Bạn có chắc chắn muốn xóa dòng sản phẩm này khỏi hóa đơn?",
                     "Xác nhận xóa",
                     JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                currentChiTietList.remove(rowIndex);
                chiTietTableModel.removeRow(rowIndex);
                calculateTotal();
                System.out.println("Đã xóa dòng sản phẩm khỏi hóa đơn.");
            }
        }
    }


    private void calculateTotal() {
        currentTotalAmount = 0;
        for (CTHoaDonBan ct : currentChiTietList) {
            currentTotalAmount += ct.getThanhtien();
        }
        lblTongTien.setText(currencyFormat.format(currentTotalAmount) + " VNĐ"); // Add currency symbol
    }


    private void saveHoaDonBan() {
        if (currentChiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hóa đơn không có chi tiết sản phẩm. Vui lòng thêm sản phẩm.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maHDB = lblMaHDB.getText().trim();
        // FIX: Get MaNV from the creator (NhanVien object) passed to the dialog
        String maNV = (this.creator != null) ? this.creator.getMaNV() : null;

        // Check if creator's MaNV is valid
        if (maNV == null || maNV.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Lỗi hệ thống: Không xác định được Mã Nhân viên tạo hóa đơn.\nVui lòng đăng nhập lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
             savedSuccessfully = false;
             return;
        }


        KhachHang selectedKH = (KhachHang) cbKhachHang.getSelectedItem();
        String maKH = (selectedKH != null) ? selectedKH.getMaKH() : null;
        // No need to show message if MaKH is null, it's allowed for walk-in customers

        Date ngayBan = new Date();
        int tongTien = currentTotalAmount;

        HoaDonBan newHoaDon = new HoaDonBan(maHDB, maNV, maKH, ngayBan, tongTien);

        // Call the transactional save method in HoaDonBanDAO
        // This method should handle:
        // 1. Saving HoaDonBan header
        // 2. Iterating through currentChiTietList
        // 3. Saving each CTHoaDonBan
        // 4. UPDATING SanPham stock (decrease stock for sales)
        // ALL WITHIN A DATABASE TRANSACTION
        boolean success = hoaDonBanDAO.saveHoaDonBanTransaction(newHoaDon, currentChiTietList); // Assuming this method exists and works transactionally

        if (success) {
            JOptionPane.showMessageDialog(this, "Lưu hóa đơn thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            savedSuccessfully = true; // Set flag to true
            dispose(); // Close the dialog after successful save
        } else {
             // Error message from DAO should ideally be more specific if saveTransaction fails
             // JOptionPane.showMessageDialog(this, "Lưu hóa đơn thất bại. Vui lòng kiểm tra lại hoặc có lỗi hệ thống (console).", "Lỗi", JOptionPane.ERROR_MESSAGE);
             System.err.println("Lưu hóa đơn thất bại."); // Generic failure message
             savedSuccessfully = false; // Ensure flag is false
        }
    }

    // Add getter for savedSuccessfully flag
    public boolean isSavedSuccessfully() {
        return savedSuccessfully;
    }


    // Main method for testing (Optional)
     public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> {
              // This test requires a running database with necessary tables (NhanVien, KhachHang, SanPham, HoaDonBan, CTHoaDonBan)
              // and implemented DAO methods (getAllNhanVien, getAllKhachHang, getSanPhamById, getStockQuantity, generateNextHoaDonBanCode, saveHoaDonBanTransaction).

              // FIX: Pass a sample NhanVien object for testing
              // Create a dummy NhanVien object (replace with real data if possible)
              // Ensure this NhanVien exists in your DB and has a valid MaNV and Role
              NhanVien dummyCreator = new NhanVien();
              dummyCreator.setMaNV("NV001"); // <-- Replace with an actual existing MaNV from your NhanVien table
              dummyCreator.setTenNV("Test Creator"); // Optional: Set name
              dummyCreator.setRole("Staff"); // Set a role if needed for dialog logic (though this dialog doesn't use role)

              // Optional: Check if dummy creator exists in DB before opening dialog
              NhanVienDAO testNVDao = new NhanVienDAO();
              if (testNVDao.getNhanVienById(dummyCreator.getMaNV()) == null) {
                   System.err.println("Lỗi: Không tìm thấy nhân viên với Mã NV '" + dummyCreator.getMaNV() + "'. Vui lòng cập nhật MaNV trong main method để test.");
                   return; // Stop if dummy employee not found
              }


              JFrame frame = new JFrame("Owner Frame");
              frame.setSize(300, 200);
              frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
              frame.setVisible(true);

              // FIX: Pass the dummy NhanVien object
              HoaDonBanCreationDialog dialog = new HoaDonBanCreationDialog(frame, dummyCreator);
              dialog.setVisible(true);

              // After dialog is closed, check if it was saved (optional in test main)
              if (dialog.isSavedSuccessfully()) {
                  System.out.println("Dialog closed successfully after saving.");
              } else {
                   System.out.println("Dialog closed, save was not successful or cancelled.");
              }

              // frame.dispose(); // Dispose owner frame if needed
         });
     }

}
