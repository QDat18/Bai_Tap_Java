package ui;

import dao.HoaDonBanDAO;
import dao.KhachHangDAO;
import dao.NhanVienDAO;
import dao.SanPhamDAO;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.CTHoaDonBan;
import model.HoaDonBan;
import model.KhachHang;
import model.NhanVien;
import model.SanPham;

public class HoaDonBanCreationDialog extends JDialog {

    // Define colors
    Color coffeeBrown = new Color(102, 51, 0);
    Color lightBeige = new Color(245, 245, 220);
    Color accentGreen = new Color(60, 179, 113);
    Color darkGray = new Color(50, 50, 50);

    // UI Components (Header Info)
    private JLabel lblMaHDB;
    private JLabel lblNgayBan;
    private JComboBox<NhanVien> cbNhanVien;
    private JComboBox<KhachHang> cbKhachHang;

    // UI Components (Product Selection for Details)
    private JComboBox<SanPham> cbMaSP; // Thay txtMaSP bằng JComboBox
    private JTextField txtTenSP;
    private JTextField txtDonGiaBan;
    private JTextField txtSoLuongBan;
    private JTextField txtKhuyenMai;
    private JButton btnAddProductToInvoice;

    // UI Components (Invoice Details Table)
    private JTable chiTietTable;
    private DefaultTableModel chiTietTableModel;

    // UI Components (Footer Info)
    private JLabel lblTongTien;

    // Action Buttons
    private JButton btnSaveInvoice;
    private JButton btnCancel;

    // Data Access Objects
    private HoaDonBanDAO hoaDonBanDAO;
    private NhanVienDAO nhanVienDAO;
    private KhachHangDAO khachHangDAO;
    private SanPhamDAO sanPhamDAO;

    // List to hold current invoice details
    private List<CTHoaDonBan> currentChiTietList;

    // Variable to hold the calculated total amount
    private int currentTotalAmount = 0;

    // Logged-in user information
    private NhanVien creator;

    // Formatters
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0");
    private SanPhamUI sanphamUI;
    // Flag to indicate if save was successful
    private boolean savedSuccessfully = false;

    public HoaDonBanCreationDialog(JFrame owner, NhanVien creator, SanPhamUI sanphamUI) {
        super(owner, "Tạo Hóa đơn Bán Mới", true);
        this.creator = creator;
        this.sanphamUI = sanphamUI;

        // Initialize DAOs
        hoaDonBanDAO = new HoaDonBanDAO();
        nhanVienDAO = new NhanVienDAO();
        khachHangDAO = new KhachHangDAO();
        sanPhamDAO = new SanPhamDAO();

        currentChiTietList = new ArrayList<>();

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(owner);

        // Content Pane
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBackground(lightBeige);
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Header Panel (Invoice Info)
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBackground(lightBeige);
        headerPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(coffeeBrown, 1), 
            "Thông tin Hóa đơn", 
            TitledBorder.LEADING, TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 14), coffeeBrown));

        GridBagConstraints gbcHeader = new GridBagConstraints();
        gbcHeader.insets = new Insets(5, 5, 5, 5);
        gbcHeader.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: MaHDB, NgayBan
        gbcHeader.gridx = 0; gbcHeader.gridy = 0; gbcHeader.weightx = 0;
        headerPanel.add(createLabel("Mã HĐB:"), gbcHeader);
        gbcHeader.gridx = 1; gbcHeader.gridy = 0; gbcHeader.weightx = 1.0;
        String newMaHDB = hoaDonBanDAO.generateNextHoaDonBanCode();
        if ("ERROR_CODE".equals(newMaHDB)) {
            lblMaHDB = createValueLabel("Lỗi tạo mã!");
            btnAddProductToInvoice = new JButton("Thêm vào HĐ");
            btnSaveInvoice = new JButton("Lưu Hóa đơn");
            btnSaveInvoice.setEnabled(false);
            btnAddProductToInvoice.setEnabled(false);
            JOptionPane.showMessageDialog(this, 
                "Không thể tạo mã hóa đơn mới. Vui lòng kiểm tra kết nối CSDL.", 
                "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
        } else {
            lblMaHDB = createValueLabel(newMaHDB);
            btnAddProductToInvoice = new JButton("Thêm vào HĐ");
            btnSaveInvoice = new JButton("Lưu Hóa đơn");
        }
        headerPanel.add(lblMaHDB, gbcHeader);

        gbcHeader.gridx = 2; gbcHeader.gridy = 0; gbcHeader.weightx = 0;
        headerPanel.add(createLabel("Ngày bán:"), gbcHeader);
        gbcHeader.gridx = 3; gbcHeader.gridy = 0; gbcHeader.weightx = 1.0;
        lblNgayBan = createValueLabel(dateFormat.format(new Date()));
        headerPanel.add(lblNgayBan, gbcHeader);

        // Row 1: NhanVien, KhachHang
        gbcHeader.gridx = 0; gbcHeader.gridy = 1; gbcHeader.weightx = 0;
        headerPanel.add(createLabel("Nhân viên:"), gbcHeader);
        gbcHeader.gridx = 1; gbcHeader.gridy = 1; gbcHeader.weightx = 1.0;
        cbNhanVien = new JComboBox<>();
        cbNhanVien.setBackground(Color.WHITE);
        cbNhanVien.setForeground(darkGray);
        cbNhanVien.setEnabled(false);
        headerPanel.add(cbNhanVien, gbcHeader);

        gbcHeader.gridx = 2; gbcHeader.gridy = 1; gbcHeader.weightx = 0;
        headerPanel.add(createLabel("Khách hàng:"), gbcHeader);
        gbcHeader.gridx = 3; gbcHeader.gridy = 1; gbcHeader.weightx = 1.0;
        cbKhachHang = new JComboBox<>();
        cbKhachHang.setBackground(Color.WHITE);
        cbKhachHang.setForeground(darkGray);
        headerPanel.add(cbKhachHang, gbcHeader);

        gbcHeader.gridx = 4; gbcHeader.gridy = 1; gbcHeader.weightx = 0;
        gbcHeader.fill = GridBagConstraints.NONE;
        JButton btnAddKhachHang = new JButton("Thêm KH");
        styleButton(btnAddKhachHang, accentGreen, Color.WHITE);
        headerPanel.add(btnAddKhachHang, gbcHeader); 

        contentPane.add(headerPanel, BorderLayout.NORTH);

        // Center Panel (Product Selection and Details Table)
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(lightBeige);

        // Product Selection Panel
        JPanel productSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        productSelectionPanel.setBackground(lightBeige);
        productSelectionPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(coffeeBrown, 1), 
            "Thêm sản phẩm vào hóa đơn", 
            TitledBorder.LEADING, TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 14), coffeeBrown));

        productSelectionPanel.add(createLabel("Sản phẩm:"));
        cbMaSP = new JComboBox<>();
        cbMaSP.setPreferredSize(new Dimension(200, 25));
        cbMaSP.setBackground(Color.WHITE);
        cbMaSP.setForeground(darkGray);
        productSelectionPanel.add(cbMaSP);

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

        styleButton(btnAddProductToInvoice, accentGreen, Color.WHITE);
        productSelectionPanel.add(btnAddProductToInvoice);

        centerPanel.add(productSelectionPanel, BorderLayout.NORTH);

        // Invoice Details Table
        JPanel detailsTablePanel = new JPanel(new BorderLayout());
        detailsTablePanel.setBackground(lightBeige);
        detailsTablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(coffeeBrown, 1), 
            "Chi tiết Hóa đơn", 
            TitledBorder.LEADING, TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 14), coffeeBrown));

        chiTietTableModel = new DefaultTableModel(
            new Object[]{"Mã SP", "Tên SP", "Số lượng", "Đơn giá", "Khuyến mãi (%)", "Thành tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2 || columnIndex == 3 || columnIndex == 4 || columnIndex == 5) 
                    return Integer.class;
                return super.getColumnClass(columnIndex);
            }
        };
        chiTietTable = new JTable(chiTietTableModel);

        DefaultTableCellRenderer detailsRendererRight = new DefaultTableCellRenderer();
        detailsRendererRight.setHorizontalAlignment(SwingConstants.RIGHT);
        chiTietTable.getColumnModel().getColumn(2).setCellRenderer(detailsRendererRight);
        chiTietTable.getColumnModel().getColumn(3).setCellRenderer(detailsRendererRight);
        chiTietTable.getColumnModel().getColumn(5).setCellRenderer(detailsRendererRight);

        DefaultTableCellRenderer detailsRendererCenter = new DefaultTableCellRenderer();
        detailsRendererCenter.setHorizontalAlignment(SwingConstants.CENTER);
        chiTietTable.getColumnModel().getColumn(4).setCellRenderer(detailsRendererCenter);

        chiTietTable.getTableHeader().setBackground(coffeeBrown);
        chiTietTable.getTableHeader().setForeground(Color.WHITE);
        chiTietTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        chiTietTable.setRowHeight(20);
        chiTietTable.setFillsViewportHeight(true);

        JScrollPane detailsScrollPane = new JScrollPane(chiTietTable);
        detailsTablePanel.add(detailsScrollPane, BorderLayout.CENTER);

        centerPanel.add(detailsTablePanel, BorderLayout.CENTER);

        contentPane.add(centerPanel, BorderLayout.CENTER);

        // Footer Panel (Total and Action Buttons)
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(lightBeige);

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        totalPanel.setBackground(lightBeige);
        totalPanel.add(createLabel("Tổng tiền:"));
        lblTongTien = createValueLabel(currencyFormat.format(currentTotalAmount));
        lblTongTien.setFont(new Font("Arial", Font.BOLD, 16));
        lblTongTien.setForeground(accentGreen);
        totalPanel.add(lblTongTien);

        footerPanel.add(totalPanel, BorderLayout.NORTH);

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionButtonPanel.setBackground(lightBeige);

        styleButton(btnSaveInvoice, accentGreen, Color.WHITE);
        actionButtonPanel.add(btnSaveInvoice);

        btnCancel = new JButton("Hủy");
        styleButton(btnCancel, darkGray, Color.WHITE);
        actionButtonPanel.add(btnCancel);

        footerPanel.add(actionButtonPanel, BorderLayout.SOUTH);

        contentPane.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);

        // Event Listeners
        btnCancel.addActionListener(e -> dispose());

        if (!"Lỗi tạo mã!".equals(lblMaHDB.getText())) {
            btnSaveInvoice.addActionListener(e -> saveHoaDonBan());
            btnAddProductToInvoice.addActionListener(e -> addProductToInvoice());
        }

        btnAddKhachHang.addActionListener(e-> {
            KhachHangCreationDialog khdialog = new KhachHangCreationDialog((JFrame) SwingUtilities.getWindowAncestor((HoaDonBanCreationDialog.this)));
            khdialog.setVisible(true);
            if(khdialog.isSavedSuccessfully()){
                loadKhachHang();
                String newMaKH = khdialog.getNewMaKH();
                if(newMaKH != null){
                    selectKhachHangInComboBox(newMaKH);
                }
            }
        });

        // Thêm ActionListener cho JComboBox cbMaSP
        cbMaSP.addActionListener(e -> {
            SanPham selectedProduct = (SanPham) cbMaSP.getSelectedItem();
            if (selectedProduct != null) {
                txtTenSP.setText(selectedProduct.getTenSP());
                txtDonGiaBan.setText(String.valueOf(selectedProduct.getGiaban()));
                txtSoLuongBan.requestFocusInWindow();
            } else {
                txtTenSP.setText("");
                txtDonGiaBan.setText("");
            }
        });

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

        // Initial Data Loading
        loadNhanVien();
        loadKhachHang();
        loadSanPham();

        if (this.creator != null && this.creator.getMaNV() != null) {
            selectNhanVienInComboBox(this.creator.getMaNV());
        } else {
            System.err.println("Thông tin nhân viên tạo hóa đơn không hợp lệ.");
            btnSaveInvoice.setEnabled(false);
            btnAddProductToInvoice.setEnabled(false);
            JOptionPane.showMessageDialog(this, 
                "Không thể xác định thông tin nhân viên.", 
                "Lỗi Hệ Thống", JOptionPane.ERROR_MESSAGE);
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
            System.err.println("Không lấy được danh sách nhân viên.");
        }
        cbNhanVien.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof NhanVien) {
                    setText(((NhanVien) value).getTenNV());
                } else {
                    setText("");
                }
                return this;
            }
        });
    }

    private void selectKhachHangInComboBox(String maKH) {
        if (maKH == null || maKH.isEmpty()) return;
        for (int i = 0; i < cbKhachHang.getItemCount(); i++) {
            KhachHang kh = cbKhachHang.getItemAt(i);
            if (kh != null && kh.getMaKH() != null && kh.getMaKH().equals(maKH)) {
                cbKhachHang.setSelectedItem(kh);
                return;
            }
        }
        System.err.println("Mã khách hàng '" + maKH + "' không tìm thấy trong danh sách ComboBox.");
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
        System.err.println("Mã nhân viên '" + maNV + "' không tìm thấy.");
    }

    private void loadKhachHang() {
        cbKhachHang.removeAllItems();
        List<KhachHang> khachHangList = khachHangDAO.getAllKhachHang();
        if (khachHangList != null && !khachHangList.isEmpty()) {
            for (KhachHang kh : khachHangList) {
                cbKhachHang.addItem(kh);
            }
            cbKhachHang.setSelectedIndex(0); // Chọn khách hàng đầu tiên
            // Kích hoạt lại các nút nếu danh sách không rỗng
            if (!"Lỗi tạo mã!".equals(lblMaHDB.getText())) {
                btnSaveInvoice.setEnabled(true);
                btnAddProductToInvoice.setEnabled(true);
            }
        } else {
            System.err.println("Không lấy được danh sách khách hàng từ CSDL.");
            JOptionPane.showMessageDialog(this, 
                "Không có khách hàng nào trong hệ thống. Vui lòng thêm khách hàng trước.", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            btnSaveInvoice.setEnabled(false);
            btnAddProductToInvoice.setEnabled(false);
        }
        cbKhachHang.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof KhachHang) {
                    setText(((KhachHang) value).getTenkhach());
                } else {
                    setText("");
                }
                return this;
            }
        });
    }
    private void loadSanPham() {
        cbMaSP.removeAllItems();
        List<SanPham> sanPhamList = sanPhamDAO.getAllSanPham();
        if (sanPhamList != null) {
            cbMaSP.addItem(null); // Thêm tùy chọn rỗng
            for (SanPham sp : sanPhamList) {
                cbMaSP.addItem(sp);
            }
        } else {
            cbMaSP.addItem(null);
            System.err.println("Không lấy được danh sách sản phẩm.");
        }
        cbMaSP.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof SanPham) {
                    SanPham sp = (SanPham) value;
                    setText(sp.getMaSP() + " - " + sp.getTenSP());
                } else if (value == null) {
                    setText("-- Chọn Sản phẩm --");
                } else {
                    setText("");
                }
                return this;
            }
        });
        cbMaSP.setSelectedItem(null);
    }

    private void addProductToInvoice() {
        SanPham selectedProduct = (SanPham) cbMaSP.getSelectedItem();
        String maSP = (selectedProduct != null) ? selectedProduct.getMaSP() : "";
        String tenSP = txtTenSP.getText().trim();
        String donGiaStr = txtDonGiaBan.getText().trim();
        String soLuongStr = txtSoLuongBan.getText().trim();
        String khuyenMaiStr = txtKhuyenMai.getText().trim();

        if (selectedProduct == null || maSP.isEmpty() || tenSP.isEmpty() || 
            donGiaStr.isEmpty() || soLuongStr.isEmpty() || khuyenMaiStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn sản phẩm và nhập đầy đủ thông tin.", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int soLuong, donGia, khuyenMaiPercent;
        try {
            soLuong = Integer.parseInt(soLuongStr);
            if (soLuong <= 0) throw new NumberFormatException("Số lượng phải lớn hơn 0.");

            donGia = Integer.parseInt(donGiaStr);

            khuyenMaiPercent = Integer.parseInt(khuyenMaiStr);
            if (khuyenMaiPercent < 0 || khuyenMaiPercent > 100) 
                throw new NumberFormatException("Khuyến mãi phải từ 0 đến 100%.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Số lượng và Khuyến mãi phải là số nguyên hợp lệ. " + e.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int currentStock = sanPhamDAO.getStockQuantity(maSP);
        int quantityInList = 0;
        for (CTHoaDonBan ct : currentChiTietList) {
            if (ct.getMaSP().equals(maSP)) {
                quantityInList = ct.getSoluong();
                break;
            }
        }

        if (soLuong > currentStock - quantityInList) {
            JOptionPane.showMessageDialog(this, 
                "Số lượng tồn kho không đủ. Chỉ còn " + (currentStock - quantityInList) + " sản phẩm.", 
                "Lỗi tồn kho", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean found = false;
        for (CTHoaDonBan existingCt : currentChiTietList) {
            if (existingCt.getMaSP().equals(maSP)) {
                existingCt.setSoluong(existingCt.getSoluong() + soLuong);
                double updatedThanhTienDouble = (double) existingCt.getGiaban() * 
                    existingCt.getSoluong() * (100 - existingCt.getKhuyenmai()) / 100.0;
                existingCt.setThanhtien((int) Math.round(updatedThanhTienDouble));

                for (int i = 0; i < chiTietTableModel.getRowCount(); i++) {
                    if (chiTietTableModel.getValueAt(i, 0).equals(maSP)) {
                        chiTietTableModel.setValueAt(existingCt.getSoluong(), i, 2);
                        chiTietTableModel.setValueAt(existingCt.getThanhtien(), i, 5);
                        break;
                    }
                }
                found = true;
                break;
            }
        }

        if (!found) {
            double thanhTienDouble = (double) donGia * soLuong * (100 - khuyenMaiPercent) / 100.0;
            int thanhTien = (int) Math.round(thanhTienDouble);

            CTHoaDonBan newCt = new CTHoaDonBan(null, maSP, soLuong, thanhTien, khuyenMaiPercent);
            newCt.setTenSP(tenSP);
            newCt.setGiaban(donGia);

            currentChiTietList.add(newCt);
            chiTietTableModel.addRow(new Object[]{
                newCt.getMaSP(),
                newCt.getTenSP(),
                newCt.getSoluong(),
                newCt.getGiaban(),
                newCt.getKhuyenmai(),
                newCt.getThanhtien()
            });
        }

        calculateTotal();

        // Clear input fields
        cbMaSP.setSelectedItem(null);
        txtTenSP.setText("");
        txtDonGiaBan.setText("");
        txtSoLuongBan.setText("");
        txtKhuyenMai.setText("0");
        cbMaSP.requestFocusInWindow();
    }

    private void removeProductFromInvoice(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < currentChiTietList.size()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa dòng sản phẩm này?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                currentChiTietList.remove(rowIndex);
                chiTietTableModel.removeRow(rowIndex);
                calculateTotal();
                System.out.println("Đã xóa dòng sản phẩm.");
            }
        }
    }

    private void calculateTotal() {
        currentTotalAmount = 0;
        for (CTHoaDonBan ct : currentChiTietList) {
            currentTotalAmount += ct.getThanhtien();
        }
        lblTongTien.setText(currencyFormat.format(currentTotalAmount) + " VNĐ");
    }

    private void saveHoaDonBan() {
        if (currentChiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Hóa đơn không có chi tiết sản phẩm.", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maHDB = lblMaHDB.getText().trim();
        String maNV = (this.creator != null) ? this.creator.getMaNV() : null;

        if (maNV == null || maNV.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi hệ thống: Không xác định được Mã Nhân viên.", 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            savedSuccessfully = false;
            return;
        }

        KhachHang selectedKH = (KhachHang) cbKhachHang.getSelectedItem();
        if (selectedKH == null || selectedKH.getMaKH() == null) {
            JOptionPane.showMessageDialog(this, 
                "Vui lòng chọn một khách hàng.", 
                "Lỗi", JOptionPane.WARNING_MESSAGE);
            savedSuccessfully = false;
            return;
        }
        String maKH = selectedKH.getMaKH();

        Date ngayBan = new Date();
        int tongTien = currentTotalAmount;

        HoaDonBan newHoaDon = new HoaDonBan(maHDB, maNV, maKH, ngayBan, tongTien);

        boolean success = hoaDonBanDAO.saveHoaDonBanTransaction(newHoaDon, currentChiTietList);
        if (success) {
            JOptionPane.showMessageDialog(this, 
                "Lưu hóa đơn thành công!", 
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            savedSuccessfully = true;
            // Làm mới bảng trong SanPhamUI nếu có tham chiếu
            if (sanphamUI != null) {
                sanphamUI.loadProductTable();
            }
            dispose();
        } else {
            System.err.println("Lưu hóa đơn thất bại.");
            savedSuccessfully = false;
        }
    }

    public boolean isSavedSuccessfully() {
        return savedSuccessfully;
    }
}