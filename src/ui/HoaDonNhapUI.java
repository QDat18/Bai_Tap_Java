package ui;

import dao.HoaDonNhapDAO;
import dao.NhaCCDAO;
import dao.NhanVienDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
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
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import model.HoaDonNhap;
import model.NhanVien;

public class HoaDonNhapUI extends JPanel {

    private final Color coffeeBrown = new Color(102, 51, 0);
    private final Color lightBeige = new Color(245, 245, 220);
    private final Color accentGreen = new Color(60, 179, 113);
    private final Color accentOrange = new Color(255, 165, 0);
    private final Color accentBlue = new Color(30, 144, 255);
    private final Color darkGray = new Color(50, 50, 50);
    private final Color tableRowEven = Color.WHITE;
    private final Color tableRowOdd = new Color(230, 230, 230);

    private JTextField txtTimKiem;
    private JButton btnThem, btnXemChiTiet, btnXoa, btnLamMoi, btnTimKiem, btnExportSelected, btnExportAll;
    private JTable tblHoaDonNhap;
    private DefaultTableModel tblModel;
    private JScrollPane scrollPane;
    private JComboBox<String> cbTimTheo;

    private HoaDonNhapDAO hoaDonNhapDAO;
    private NhaCCDAO nhaCCDAO;
    private NhanVienDAO nhanVienDAO;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private NhanVien currentUser;

    public HoaDonNhapUI(NhanVien currentUser) {
        this.currentUser = currentUser;

        hoaDonNhapDAO = new HoaDonNhapDAO();
        nhanVienDAO = new NhanVienDAO();
        nhaCCDAO = new NhaCCDAO();

        setLayout(new BorderLayout(10, 10));
        setBackground(lightBeige);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setBackground(lightBeige);

        btnThem = createButton("Thêm Hóa đơn nhập", accentGreen);
        btnThem.addActionListener(e -> themHoaDonNhap());

        btnXemChiTiet = createButton("Xem Chi tiết", accentBlue);
        btnXemChiTiet.addActionListener(e -> xemChiTietHoaDonNhap());

        btnXoa = createButton("Xóa Hóa đơn nhập", accentOrange);
        btnXoa.addActionListener(e -> xoaHoaDonNhap());

        btnLamMoi = createButton("Làm mới", darkGray);
        btnLamMoi.addActionListener(e -> lamMoi());

        btnExportSelected = createButton("Xuất Hóa đơn (Excel)", accentBlue);
        btnExportSelected.addActionListener(e -> exportSelectedInvoiceToExcel());

        btnExportAll = createButton("Xuất tất cả HĐN", accentGreen);
        btnExportAll.addActionListener(e -> exportAllInvoicesToExcel());

        topPanel.add(btnThem);
        topPanel.add(btnXemChiTiet);
        topPanel.add(btnXoa);
        topPanel.add(btnLamMoi);
        topPanel.add(btnExportSelected);
        topPanel.add(btnExportAll);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        searchPanel.setBackground(lightBeige);

        searchPanel.add(createLabel("Tìm theo:"));
        cbTimTheo = new JComboBox<>(new String[]{"Mã HĐN", "Tên NV", "Tên NCC", "Ngày nhập"});
        cbTimTheo.setBackground(Color.WHITE);
        cbTimTheo.setForeground(darkGray);
        searchPanel.add(cbTimTheo);

        searchPanel.add(createLabel("Từ khóa:"));
        txtTimKiem = new JTextField(20);
        txtTimKiem.setFont(new Font("Arial", Font.PLAIN, 12));
        searchPanel.add(txtTimKiem);

        btnTimKiem = createButton("Tìm", coffeeBrown);
        btnTimKiem.addActionListener(e -> timKiemHoaDonNhap());
        searchPanel.add(btnTimKiem);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setBackground(lightBeige);
        northPanel.add(topPanel, BorderLayout.WEST);
        northPanel.add(searchPanel, BorderLayout.EAST);

        add(northPanel, BorderLayout.NORTH);

        JPanel panelTable = new JPanel(new BorderLayout());
        panelTable.setBackground(lightBeige);
        panelTable.setBorder(new TitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Danh sách hóa đơn nhập", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), coffeeBrown));

        tblModel = new DefaultTableModel(new Object[]{"Mã HĐN", "Mã NV", "Tên NV", "Mã NCC", "Tên NCC", "Ngày nhập", "Tổng tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblHoaDonNhap = new JTable(tblModel);
        setupTableStyle(tblHoaDonNhap);

        tblHoaDonNhap.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    xemChiTietHoaDonNhap();
                }
            }
        });

        scrollPane = new JScrollPane(tblHoaDonNhap);
        scrollPane.setBackground(lightBeige);
        panelTable.add(scrollPane, BorderLayout.CENTER);

        add(panelTable, BorderLayout.CENTER);

        loadHoaDonNhapTable();
        checkPermissions();
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        button.setOpaque(true);
        return button;
    }

    private void setupTableStyle(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(coffeeBrown);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setReorderingAllowed(false);

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
                    c.setBackground(new Color(180, 210, 230));
                }

                String columnName = table.getColumnModel().getColumn(column).getHeaderValue().toString();
                if (columnName.equals("Tổng tiền")) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (columnName.equals("Ngày nhập") || columnName.equals("Mã HĐN") || columnName.equals("Mã NV") || columnName.equals("Mã NCC")) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                setText(value != null ? value.toString() : "");
                return c;
            }
        });
    }

    public void loadHoaDonNhapTable() {
        tblModel.setRowCount(0);
        List<HoaDonNhap> danhSachHoaDonNhap = hoaDonNhapDAO.getAllHoaDonNhap();

        if (danhSachHoaDonNhap != null) {
            for (HoaDonNhap hdn : danhSachHoaDonNhap) {
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
        if (danhSachHoaDonNhap == null || danhSachHoaDonNhap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có hóa đơn nhập nào để hiển thị.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void themHoaDonNhap() {
        String employeeMaNV = currentUser != null ? currentUser.getMaNV() : null;
        String employeeTenNV = currentUser != null ? currentUser.getTenNV() : null;

        if (employeeMaNV == null || employeeMaNV.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Thông tin Mã Nhân viên không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Chức năng thêm hóa đơn nhập chưa được triển khai.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void xemChiTietHoaDonNhap() {
        int selectedRow = tblHoaDonNhap.getSelectedRow();
        if (selectedRow != -1) {
            String maHDN = tblModel.getValueAt(selectedRow, 0).toString();
            CTHoaDonNhapDetailsDialog detailsDialog = new CTHoaDonNhapDetailsDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this), maHDN);
            detailsDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn nhập để xem chi tiết.", "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void xoaHoaDonNhap() {
        int selectedRow = tblHoaDonNhap.getSelectedRow();
        if (selectedRow != -1) {
            String maHDN = tblModel.getValueAt(selectedRow, 0).toString();
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa Hóa đơn Nhập có mã: " + maHDN + "?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "Chức năng xóa hóa đơn nhập chưa được triển khai.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn nhập để xóa.", "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void lamMoi() {
        txtTimKiem.setText("");
        cbTimTheo.setSelectedIndex(0);
        loadHoaDonNhapTable();
        tblHoaDonNhap.clearSelection();
    }

    private void timKiemHoaDonNhap() {
        String searchTerm = txtTimKiem.getText().trim();
        String searchCriteria = (String) cbTimTheo.getSelectedItem();
        tblModel.setRowCount(0);

        if (searchTerm.isEmpty()) {
            loadHoaDonNhapTable();
            return;
        }

        List<HoaDonNhap> searchResult = new ArrayList<>();
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
        tblHoaDonNhap.clearSelection();
    }

    private void checkPermissions() {
        String role = currentUser != null ? currentUser.getRole() : "";
        btnThem.setEnabled("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role));
        btnXoa.setEnabled("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role));
        btnXemChiTiet.setEnabled(true);
        btnTimKiem.setEnabled(true);
        txtTimKiem.setEnabled(true);
        cbTimTheo.setEnabled(true);
        btnLamMoi.setEnabled(true);
        btnExportSelected.setEnabled(true);
        btnExportAll.setEnabled(true);
    }

    private void exportSelectedInvoiceToExcel() {
        int selectedRow = tblHoaDonNhap.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn nhập để xuất.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maHDN = tblModel.getValueAt(selectedRow, 0).toString();
        HoaDonNhap hoaDonNhap = hoaDonNhapDAO.getHoaDonNhapByMaHDN(maHDN);
        if (hoaDonNhap == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn nhập với mã: " + maHDN, "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu hóa đơn nhập");
        fileChooser.setSelectedFile(new File("HoaDonNhap_" + maHDN + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".xlsx"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("HoaDonNhap_" + maHDN);

                Row headerRow = sheet.createRow(0);
                String[] headers = {"Mã HĐN", "Mã NV", "Tên NV", "Mã NCC", "Tên NCC", "Ngày nhập", "Tổng tiền"};
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

                Row dataRow = sheet.createRow(1);
                dataRow.createCell(0).setCellValue(hoaDonNhap.getMaHDN());
                dataRow.createCell(1).setCellValue(hoaDonNhap.getMaNV());
                dataRow.createCell(2).setCellValue(hoaDonNhap.getTenNV());
                dataRow.createCell(3).setCellValue(hoaDonNhap.getMaNCC());
                dataRow.createCell(4).setCellValue(hoaDonNhap.getTenNCC());
                dataRow.createCell(5).setCellValue(hoaDonNhap.getNgayNhap() != null ? dateFormat.format(hoaDonNhap.getNgayNhap()) : "N/A");
                dataRow.createCell(6).setCellValue(hoaDonNhap.getTongTien());

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                    workbook.write(fileOut);
                    JOptionPane.showMessageDialog(this, "Xuất hóa đơn thành công! File: " + filePath, "Thành công", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất hóa đơn: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void exportAllInvoicesToExcel() {
        List<HoaDonNhap> danhSachHoaDonNhap = hoaDonNhapDAO.getAllHoaDonNhap();
        if (danhSachHoaDonNhap == null || danhSachHoaDonNhap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có hóa đơn nhập nào để xuất.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu tất cả hóa đơn nhập");
        fileChooser.setSelectedFile(new File("All_HoaDonNhap_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".xlsx"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("All_HoaDonNhap");

                Row headerRow = sheet.createRow(0);
                String[] headers = {"Mã HĐN", "Mã NV", "Tên NV", "Mã NCC", "Tên NCC", "Ngày nhập", "Tổng tiền"};
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
                for (HoaDonNhap hdn : danhSachHoaDonNhap) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(hdn.getMaHDN());
                    row.createCell(1).setCellValue(hdn.getMaNV());
                    row.createCell(2).setCellValue(hdn.getTenNV());
                    row.createCell(3).setCellValue(hdn.getMaNCC());
                    row.createCell(4).setCellValue(hdn.getTenNCC());
                    row.createCell(5).setCellValue(hdn.getNgayNhap() != null ? dateFormat.format(hdn.getNgayNhap()) : "N/A");
                    row.createCell(6).setCellValue(hdn.getTongTien());
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NhanVien dummyUser = new NhanVien();
            dummyUser.setMaNV("NV01");
            dummyUser.setTenNV("Test User");
            dummyUser.setRole("Manager");

            JFrame frame = new JFrame("Quản lý Hóa đơn Nhập Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
            frame.add(new HoaDonNhapUI(dummyUser));
            frame.setVisible(true);
        });
    }
}