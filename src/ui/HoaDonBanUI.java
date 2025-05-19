package ui;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.common.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import dao.CTHoaDonBanDAO;
import dao.HoaDonBanDAO;
import dao.NhanVienDAO;
import model.CTHoaDonBan;
import model.HoaDonBan;
import model.NhanVien;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class HoaDonBanUI extends JPanel {

    // Define colors using java.awt.Color
    java.awt.Color coffeeBrown = new java.awt.Color(102, 51, 0);
    java.awt.Color lightBeige = new java.awt.Color(245, 245, 220);
    java.awt.Color accentGreen = new java.awt.Color(60, 179, 113);
    java.awt.Color accentOrange = new java.awt.Color(255, 165, 0);
    java.awt.Color accentBlue = new java.awt.Color(30, 144, 255);
    java.awt.Color darkGray = new java.awt.Color(50, 50, 50);

    // UI Components
    private JButton btnCreateNew;
    private JButton btnViewDetails;
    private JButton btnDelete;
    private JButton btnRefresh;
    private JButton btnPrintInvoice;
    private JButton btnExportInvoice;
    private JButton btnExportAllInvoice;
    // Search Components
    private JPanel searchPanel;
    private JComboBox<String> cbSearchCriteria;
    private JTextField txtSearchTerm;
    private JButton btnSearch;

    private JTable hoaDonBanTable;
    private DefaultTableModel tableModel;

    private HoaDonBanDAO hoaDonBanDAO;
    private CTHoaDonBanDAO ctHoaDonBanDAO;
    private NhanVienDAO nhanVienDAO;

    private NhanVien currentUser;

    // Date and Number formatters
    private SimpleDateFormat tableDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private SimpleDateFormat printExportDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    private SanPhamUI sanphamUI;

    public HoaDonBanUI(NhanVien currentUser, SanPhamUI sanphamUI) {
        this.currentUser = currentUser;
        this.sanphamUI = sanphamUI;
        this.hoaDonBanDAO = new HoaDonBanDAO();
        this.ctHoaDonBanDAO = new CTHoaDonBanDAO();
        this.nhanVienDAO = new NhanVienDAO();

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
        styleButton(btnCreateNew, accentGreen, java.awt.Color.WHITE);
        buttonPanel.add(btnCreateNew);

        btnViewDetails = new JButton("Xem Chi tiết");
        styleButton(btnViewDetails, accentBlue, java.awt.Color.WHITE);
        buttonPanel.add(btnViewDetails);

        btnDelete = new JButton("Xóa Hóa đơn");
        styleButton(btnDelete, accentOrange, java.awt.Color.WHITE);
        buttonPanel.add(btnDelete);

        btnRefresh = new JButton("Làm mới Bảng");
        styleButton(btnRefresh, darkGray, java.awt.Color.WHITE);
        buttonPanel.add(btnRefresh);

        btnPrintInvoice = new JButton("In Hóa đơn");
        styleButton(btnPrintInvoice, coffeeBrown, java.awt.Color.WHITE);
        buttonPanel.add(btnPrintInvoice);

        btnExportInvoice = new JButton("Xuất Hóa đơn (Excel)");
        styleButton(btnExportInvoice, coffeeBrown.darker(), java.awt.Color.WHITE);
        buttonPanel.add(btnExportInvoice);

        btnExportAllInvoice = new JButton("Xuất Tất cả Hóa đơn (Excel)");
        styleButton(btnExportAllInvoice, coffeeBrown.darker(), java.awt.Color.WHITE);
        buttonPanel.add(btnExportAllInvoice);

        topControlPanel.add(buttonPanel, BorderLayout.NORTH);

        // Search Panel
        searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(lightBeige);
        searchPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(coffeeBrown, 1), 
                "Tìm kiếm Hóa đơn", 
                0, 0, 
                new java.awt.Font("Arial", java.awt.Font.BOLD, 12), 
                coffeeBrown));

        searchPanel.add(createLabel("Tìm theo:"));
        String[] searchOptions = {"Mã HDB", "Ngày bán", "Tên NV", "Tên KH"};
        cbSearchCriteria = new JComboBox<>(searchOptions);
        cbSearchCriteria.setBackground(java.awt.Color.WHITE);
        cbSearchCriteria.setForeground(darkGray);
        searchPanel.add(cbSearchCriteria);

        searchPanel.add(createLabel("Từ khóa:"));
        txtSearchTerm = new JTextField(15);
        searchPanel.add(txtSearchTerm);

        btnSearch = new JButton("Tìm kiếm");
        styleButton(btnSearch, coffeeBrown, java.awt.Color.WHITE);
        searchPanel.add(btnSearch);

        topControlPanel.add(searchPanel, BorderLayout.CENTER);

        add(topControlPanel, BorderLayout.NORTH);

        // --- Table Panel ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(lightBeige);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(coffeeBrown, 1), 
                "Danh sách Hóa đơn Bán", 
                0, 0, 
                new java.awt.Font("Arial", java.awt.Font.BOLD, 14), 
                coffeeBrown));

        // Table Model: Columns for HoaDonBan
        tableModel = new DefaultTableModel(new Object[]{"Mã HDB", "Mã NV", "Tên NV", "Mã KH", "Tên KH", "Ngày bán", "Tổng tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Date.class;
                if (columnIndex == 6) return Integer.class;
                return super.getColumnClass(columnIndex);
            }
        };
        hoaDonBanTable = new JTable(tableModel);

        // Style table header and cells
        hoaDonBanTable.getTableHeader().setBackground(coffeeBrown);
        hoaDonBanTable.getTableHeader().setForeground(java.awt.Color.WHITE);
        hoaDonBanTable.getTableHeader().setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? java.awt.Color.WHITE : new java.awt.Color(230, 230, 230));
                if (isSelected) {
                    c.setBackground(new java.awt.Color(180, 210, 230));
                }
                if (column == 6) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                if (column == 5) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
            }

            @Override
            protected void setValue(Object value) {
                if (value instanceof Date) {
                    setText(tableDateFormat.format((Date) value));
                } else {
                    super.setValue(value);
                }
            }
        };
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            hoaDonBanTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JScrollPane scrollPane = new JScrollPane(hoaDonBanTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);

        // --- Event Listeners ---
        btnCreateNew.addActionListener(e -> openHoaDonBanCreationDialog());
        btnViewDetails.addActionListener(e -> viewHoaDonBanDetails());
        btnDelete.addActionListener(e -> deleteHoaDonBan());
        btnRefresh.addActionListener(e -> loadHoaDonBanTable());
        btnSearch.addActionListener(e -> searchHoaDonBan());
        btnPrintInvoice.addActionListener(e -> printSelectedInvoice());
        btnExportInvoice.addActionListener(e -> exportSelectedInvoiceToExcel());
        btnExportAllInvoice.addActionListener(e -> exportAllInvoicesToExcel());

        // Table row selection listener
        hoaDonBanTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                applyRolePermissions();
                if (e.getClickCount() == 2) {
                    viewHoaDonBanDetails();
                }
            }
        });

        loadHoaDonBanTable();
        applyRolePermissions();
    }

    private void loadHoaDonBanTable() {
        tableModel.setRowCount(0);
        List<HoaDonBan> danhSach = hoaDonBanDAO.getAllHoaDonBan();
        if (danhSach != null) {
            for (HoaDonBan hdb : danhSach) {
                tableModel.addRow(new Object[]{
                    hdb.getMaHDB(),
                    hdb.getMaNV(),
                    hdb.getTenNV(),
                    hdb.getMaKH(),
                    hdb.getTenKH(),
                    hdb.getNgayban(),
                    hdb.getTongtien()
                });
            }
        } else {
            System.out.println("Không lấy được dữ liệu hóa đơn bán từ CSDL.");
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu hóa đơn bán.", "Lỗi Tải dữ liệu", JOptionPane.ERROR_MESSAGE);
        }
        applyRolePermissions();
    }

    private void openHoaDonBanCreationDialog() {
        if (currentUser == null || (!"Admin".equalsIgnoreCase(currentUser.getRole()) &&
                                    !"Manager".equalsIgnoreCase(currentUser.getRole()) &&
                                    !"Staff".equalsIgnoreCase(currentUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền tạo hóa đơn bán.",
                "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Frame owner = (Frame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
        if (owner instanceof JFrame) {
            HoaDonBanCreationDialog creationDialog = new HoaDonBanCreationDialog(
                (JFrame) owner, this.currentUser, this.sanphamUI);
            creationDialog.setVisible(true);
            if (creationDialog.isSavedSuccessfully()) {
                loadHoaDonBanTable();
            }
        } else {
            System.err.println("Could not get the main JFrame for showing the creation dialog.");
            JOptionPane.showMessageDialog(this, "Không thể mở cửa sổ tạo hóa đơn.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewHoaDonBanDetails() {
        int selectedRow = hoaDonBanTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để xem chi tiết.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maHDB = tableModel.getValueAt(selectedRow, 0).toString();
        Frame owner = (Frame) SwingUtilities.getAncestorOfClass(JFrame.class, this);
        if (owner instanceof JFrame) {
            CTHoaDonBanDetailsDialog detailsDialog = new CTHoaDonBanDetailsDialog((JFrame) owner, maHDB);
            detailsDialog.setVisible(true);
        } else {
            System.err.println("Could not get the main JFrame for showing the details dialog.");
            JOptionPane.showMessageDialog(this, "Không thể mở cửa sổ chi tiết hóa đơn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteHoaDonBan() {
        if (currentUser == null || (!"Admin".equalsIgnoreCase(currentUser.getRole()) && !"Manager".equalsIgnoreCase(currentUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền xóa hóa đơn bán.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = hoaDonBanTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maHDB = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa hóa đơn " + maHDB + " này?\nViệc này sẽ cập nhật lại tồn kho sản phẩm.",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = hoaDonBanDAO.deleteHoaDonBan(maHDB);
            if (success) {
                JOptionPane.showMessageDialog(this, "Xóa hóa đơn thành công.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadHoaDonBanTable();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa hóa đơn thất bại. Vui lòng kiểm tra lại hoặc có lỗi hệ thống (console).", "Lỗi", JOptionPane.ERROR_MESSAGE);
                System.err.println("Failed to delete HoaDonBan with MaHDB: " + maHDB);
            }
        }
    }

    private void searchHoaDonBan() {
        String criteria = (String) cbSearchCriteria.getSelectedItem();
        String searchTerm = txtSearchTerm.getText().trim();

        if (searchTerm.isEmpty()) {
            loadHoaDonBanTable();
            return;
        }

        List<HoaDonBan> searchResults = new ArrayList<>();
        try {
            searchResults = hoaDonBanDAO.searchHoaDonBan(criteria, searchTerm);
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi DAO searchHoaDonBan: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Tìm kiếm thất bại. Lỗi hệ thống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);
        if (searchResults != null && !searchResults.isEmpty()) {
            for (HoaDonBan hdb : searchResults) {
                tableModel.addRow(new Object[]{
                    hdb.getMaHDB(),
                    hdb.getMaNV(),
                    hdb.getTenNV(),
                    hdb.getMaKH(),
                    hdb.getTenKH(),
                    hdb.getNgayban(),
                    hdb.getTongtien()
                });
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả nào cho '" + searchTerm + "'", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }

        applyRolePermissions();
        hoaDonBanTable.clearSelection();
    }

    private void printSelectedInvoice() {
        int selectedRow = hoaDonBanTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để in.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentUser == null || (!"Admin".equalsIgnoreCase(currentUser.getRole()) && !"Manager".equalsIgnoreCase(currentUser.getRole()) && !"Staff".equalsIgnoreCase(currentUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền in hóa đơn bán.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String maHDB = tableModel.getValueAt(selectedRow, 0).toString();
        HoaDonBan hdb = hoaDonBanDAO.getHoaDonBanById(maHDB);
        List<CTHoaDonBan> chiTietList = ctHoaDonBanDAO.getChiTietHoaDonByMaHDB(maHDB);

        if (hdb == null || chiTietList == null || chiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu chi tiết cho hóa đơn này.", "Lỗi In", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setJobName("Hóa đơn bán hàng - " + maHDB);

        printerJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));

                int y = 10;
                int lineHeight = 15;

                g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
                g2d.drawString("THE COFFEE TEAM", 100, y); y += lineHeight * 2;
                g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
                g2d.drawString("HÓA ĐƠN BÁN HÀNG", 90, y); y += lineHeight * 2;

                g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
                g2d.drawString("Mã HĐ: " + hdb.getMaHDB(), 10, y); y += lineHeight;
                g2d.drawString("Ngày: " + (hdb.getNgayban() != null ? printExportDateFormat.format(hdb.getNgayban()) : "N/A"), 10, y); y += lineHeight;
                g2d.drawString("Khách hàng: " + (hdb.getTenKH() != null ? hdb.getTenKH() : "Khách lẻ"), 10, y); y += lineHeight;
                g2d.drawString("Nhân viên: " + (hdb.getTenNV() != null ? hdb.getTenNV() : "N/A"), 10, y); y += lineHeight * 2;

                g2d.drawString("----------------------------------------------------------------------", 10, y); y += lineHeight;
                g2d.drawString(String.format("%-10s %-25s %-5s %-10s %-5s %-10s", "Mã SP", "Tên SP", "SL", "Đơn giá", "KM(%)", "Thành tiền"), 10, y); y += lineHeight;
                g2d.drawString("----------------------------------------------------------------------", 10, y); y += lineHeight;

                for (CTHoaDonBan ct : chiTietList) {
                    String tenSP = ct.getTenSP().length() > 22 ? ct.getTenSP().substring(0, 20) + "..." : ct.getTenSP();
                    g2d.drawString(String.format("%-10s %-25s %-5d %-10s %-5d %-10s",
                        ct.getMaSP(),
                        tenSP,
                        ct.getSoluong(),
                        currencyFormat.format(ct.getGiaban()),
                        ct.getKhuyenmai(),
                        currencyFormat.format(ct.getThanhtien())), 10, y);
                    y += lineHeight;
                }

                g2d.drawString("----------------------------------------------------------------------", 10, y); y += lineHeight;
                g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
                g2d.drawString(String.format("Tổng tiền: %s", currencyFormat.format(hdb.getTongtien())), 10, y); y += lineHeight * 2;

                g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 10));
                g2d.drawString("======================================================================", 10, y); y += lineHeight;
                g2d.drawString("                    Cảm ơn quý khách!", 10, y); y += lineHeight;
                g2d.drawString("======================================================================", 10, y); y += lineHeight;

                return Printable.PAGE_EXISTS;
            }
        });

        if (printerJob.printDialog()) {
            try {
                printerJob.print();
                JOptionPane.showMessageDialog(this, "Đang gửi hóa đơn đến máy in...", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi in hóa đơn: " + e.getMessage(),
                    "Lỗi In", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void exportSelectedInvoiceToExcel() {
        int selectedRow = hoaDonBanTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để xuất.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (currentUser == null || (!"Admin".equalsIgnoreCase(currentUser.getRole()) && !"Manager".equalsIgnoreCase(currentUser.getRole()) && !"Staff".equalsIgnoreCase(currentUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền xuất hóa đơn bán.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String maHDB = tableModel.getValueAt(selectedRow, 0).toString();
        HoaDonBan hdb = hoaDonBanDAO.getHoaDonBanById(maHDB);
        List<CTHoaDonBan> chiTietList = ctHoaDonBanDAO.getChiTietHoaDonByMaHDB(maHDB);

        if (hdb == null || chiTietList == null || chiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu chi tiết cho hóa đơn này.", "Lỗi Xuất Excel", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu Hóa đơn " + maHDB + " dưới dạng Excel");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Workbook (*.xlsx)", "xlsx"));
        fileChooser.setSelectedFile(new File("HoaDonBan_" + maHDB.replace(" ", "_") + ".xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".xlsx")) {
                fileToSave = new File(filePath + ".xlsx");
            }

            try (Workbook workbook = new XSSFWorkbook();
                 FileOutputStream outputStream = new FileOutputStream(fileToSave)) {

                Sheet sheet = workbook.createSheet("Hóa đơn " + maHDB);

                // Create header row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Mã HDB", "Ngày Bán", "Khách Hàng", "Nhân Viên"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    CellStyle style = workbook.createCellStyle();
                    org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                    font.setBold(true);
                    style.setFont(font);
                    cell.setCellStyle(style);
                }

                // Add invoice header details
                Row dataRow = sheet.createRow(1);
                dataRow.createCell(0).setCellValue(hdb.getMaHDB());
                dataRow.createCell(1).setCellValue(hdb.getNgayban() != null ? printExportDateFormat.format(hdb.getNgayban()) : "N/A");
                dataRow.createCell(2).setCellValue(hdb.getTenKH() != null ? hdb.getTenKH() : "Khách lẻ");
                dataRow.createCell(3).setCellValue(hdb.getTenNV() != null ? hdb.getTenNV() : "N/A");

                sheet.createRow(3);

                // Create details table header
                Row detailsHeaderRow = sheet.createRow(4);
                String[] detailsHeaders = {"Mã SP", "Tên SP", "Số lượng", "Đơn giá", "Khuyến mãi (%)", "Thành tiền"};
                for (int i = 0; i < detailsHeaders.length; i++) {
                    Cell cell = detailsHeaderRow.createCell(i);
                    cell.setCellValue(detailsHeaders[i]);
                    CellStyle style = workbook.createCellStyle();
                    org.apache.poi.ss.usermodel.Font font = workbook.createFont();
                    font.setBold(true);
                    style.setFont(font);
                    cell.setCellStyle(style);
                }

                // Add details items
                int rowNum = 5;
                for (CTHoaDonBan ct : chiTietList) {
                    Row detailRow = sheet.createRow(rowNum++);
                    detailRow.createCell(0).setCellValue(ct.getMaSP());
                    detailRow.createCell(1).setCellValue(ct.getTenSP());
                    detailRow.createCell(2).setCellValue(ct.getSoluong());
                    detailRow.createCell(3).setCellValue(ct.getGiaban());
                    detailRow.createCell(4).setCellValue(ct.getKhuyenmai());
                    detailRow.createCell(5).setCellValue(ct.getThanhtien());
                }

                // Add total amount
                sheet.createRow(rowNum++);
                Row totalRow = sheet.createRow(rowNum++);
                Cell totalLabelCell = totalRow.createCell(4);
                totalLabelCell.setCellValue("Tổng tiền:");
                CellStyle boldStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font boldFont = workbook.createFont();
                boldFont.setBold(true);
                boldStyle.setFont(boldFont);
                totalLabelCell.setCellStyle(boldStyle);

                Cell totalValueCell = totalRow.createCell(5);
                totalValueCell.setCellValue(hdb.getTongtien());
                totalValueCell.setCellStyle(boldStyle);

                // Auto-size columns
                for (int i = 0; i < detailsHeaders.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Write the workbook to the file
                workbook.write(outputStream);

                // Open the generated Excel file
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().open(fileToSave);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Đã xuất file Excel thành công nhưng không thể mở tự động.\nVui lòng mở thủ công tại: " + fileToSave.getAbsolutePath(),
                                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Đã xuất file Excel thành công.\nVui lòng mở thủ công tại: " + fileToSave.getAbsolutePath(),
                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(this, "Lỗi: Không thể ghi file. Vui lòng kiểm tra đường dẫn và quyền ghi.",
                        "Lỗi Xuất Excel", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất hóa đơn ra Excel: " + e.getMessage(),
                        "Lỗi Xuất Excel", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi không xác định khi xuất hóa đơn ra Excel: " + e.getMessage(),
                        "Lỗi Xuất Excel", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Đã hủy thao tác xuất hóa đơn.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportAllInvoicesToExcel() {
        List<HoaDonBan> danhSachHoaDonBan = hoaDonBanDAO.getAllHoaDonBan();
        if (danhSachHoaDonBan == null || danhSachHoaDonBan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có hóa đơn bán nào để xuất.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu tất cả hóa đơn bán");
        fileChooser.setSelectedFile(new File("All_HoaDonBan_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".xlsx"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("All_HoaDonBan");

                Row headerRow = sheet.createRow(0);
                String[] headers = {"Mã HĐB", "Mã NV", "Tên NV", "Mã KH", "Tên Khách hàng", "Ngày bán", "Tổng tiền"};
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.BROWN.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                int rowNum = 1;
                for (HoaDonBan hdn : danhSachHoaDonBan) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(hdn.getMaHDB());
                    row.createCell(1).setCellValue(hdn.getMaNV());
                    row.createCell(2).setCellValue(hdn.getTenNV());
                    row.createCell(3).setCellValue(hdn.getMaKH());
                    row.createCell(4).setCellValue(hdn.getTenKH());
                    row.createCell(5).setCellValue(hdn.getNgayban() != null ? tableDateFormat.format(hdn.getNgayban()) : "N/A");
                    row.createCell(6).setCellValue(hdn.getTongtien());
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                    workbook.write(fileOut);
                    JOptionPane.showMessageDialog(this, "Xuất tất cả hóa đơn thành công! File: " + filePath, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất tất cả hóa đơn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void applyRolePermissions() {
        btnCreateNew.setEnabled(false);
        btnViewDetails.setEnabled(false);
        btnDelete.setEnabled(false);
        btnPrintInvoice.setEnabled(false);
        btnExportInvoice.setEnabled(false);
        btnRefresh.setEnabled(true);
        btnSearch.setEnabled(true);
        txtSearchTerm.setEnabled(true);
        cbSearchCriteria.setEnabled(true);

        if (currentUser == null) {
            return;
        }

        String role = currentUser.getRole();
        int selectedRow = hoaDonBanTable.getSelectedRow();

        if ("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role)) {
            btnCreateNew.setEnabled(true);
        }

        if (selectedRow >= 0) {
            btnViewDetails.setEnabled(true);
            if ("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role)) {
                btnDelete.setEnabled(true);
            }
            if ("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role)) {
                btnPrintInvoice.setEnabled(true);
                btnExportInvoice.setEnabled(true);
            }

            if (!"Admin".equalsIgnoreCase(role)) {
                String creatorMaNV = (String) tableModel.getValueAt(selectedRow, 1);
                NhanVien creator = nhanVienDAO.getNhanVienById(creatorMaNV);
                if (creator != null && "Admin".equalsIgnoreCase(creator.getRole())) {
                    btnViewDetails.setEnabled(false);
                    btnDelete.setEnabled(false);
                    btnPrintInvoice.setEnabled(false);
                    btnExportInvoice.setEnabled(false);
                }
            }
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
        return label;
    }

    private void styleButton(JButton button, java.awt.Color bgColor, java.awt.Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fgColor, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NhanVien dummyUser = new NhanVien();
            dummyUser.setMaNV("NV01");
            dummyUser.setTenNV("Test User");
            dummyUser.setRole("Admin");
            SanPhamUI dummySanPhamUI = null;
            JFrame frame = new JFrame("Quản lý Hóa đơn Bán Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            frame.add(new HoaDonBanUI(dummyUser, dummySanPhamUI));
            frame.setVisible(true);
        });
    }

    public java.awt.Color getCoffeeBrown() {
        return coffeeBrown;
    }
}