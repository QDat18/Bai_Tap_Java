package ui;

import dao.ChiTietHoaDonNhapDAO;
import dao.HoaDonNhapDAO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.ChiTietHoaDonNhap;
import model.HoaDonNhap;

public class CTHoaDonNhapDetailsDialog extends JDialog {

    // Define colors (reusing the palette)
    Color coffeeBrown = new Color(102, 51, 0);
    Color darkGray = new Color(50, 50, 50);
    Color lightBeige = new Color(245, 245, 220);
    Color accentOrange = new Color(255, 165, 0); // Color for Close button
    Color accentBlue = new Color(30, 144, 255); // Color for Export button
    Color parchment = new Color(255, 245, 224); // Vintage parchment background
    Color sepiaText = new Color(112, 66, 20); // Sepia tone for text
    Color darkSepia = new Color(80, 40, 10); // Darker sepia for borders

    // UI Components for header info
    private JLabel lblMaHDNValue;
    private JLabel lblNgayNhapValue;
    private JLabel lblTenNVValue;
    private JLabel lblTenNCCValue;
    private JLabel lblTongTienValue;

    // UI Components for details table
    private JTable tblChiTietHoaDonNhap;
    private DefaultTableModel tblChiTietModel;
    private JScrollPane scrollPaneChiTiet;

    // UI Components for dialog actions
    private JButton btnDong;
    private JButton btnExportImage;

    // DAOs
    private HoaDonNhapDAO hoaDonNhapDAO;
    private ChiTietHoaDonNhapDAO chiTietHoaDonNhapDAO;

    // Formatters
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat printDateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
    private DecimalFormat currencyFormatter = new DecimalFormat("#,##0");

    // Constructor
    public CTHoaDonNhapDetailsDialog(Frame owner, String maHDN) {
        super(owner, "Chi tiết Hóa đơn Nhập: " + maHDN, true);

        // Initialize DAOs
        hoaDonNhapDAO = new HoaDonNhapDAO();
        chiTietHoaDonNhapDAO = new ChiTietHoaDonNhapDAO();

        setLayout(new BorderLayout(10, 10));
        setBackground(lightBeige);
        setPreferredSize(new Dimension(700, 500));
        setResizable(false);

        // --- Header Info Panel ---
        JPanel headerInfoPanel = new JPanel(new GridBagLayout());
        headerInfoPanel.setBackground(lightBeige);
        headerInfoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Thông tin chung", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), coffeeBrown));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
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
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; headerInfoPanel.add(createLabel("Nhà cung cấp:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0; lblTenNCCValue = createValueLabel(""); headerInfoPanel.add(lblTenNCCValue, gbc);

        // Row 4: Tổng tiền (span two columns)
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST; headerInfoPanel.add(createLabel("Tổng tiền:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0; lblTongTienValue = createValueLabel(""); headerInfoPanel.add(lblTongTienValue, gbc);

        add(headerInfoPanel, BorderLayout.NORTH);

        // --- Details Table Panel ---
        JPanel detailsTablePanel = new JPanel(new BorderLayout());
        detailsTablePanel.setBackground(lightBeige);
        detailsTablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Chi tiết sản phẩm", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), coffeeBrown));

        tblChiTietModel = new DefaultTableModel(
                new Object[]{"Mã SP", "Tên SP", "Số lượng", "Đơn giá nhập", "Thành tiền"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
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

        btnExportImage = createButton("Xuất Hóa đơn thành ảnh");
        styleButton(btnExportImage, accentBlue, Color.WHITE);
        btnExportImage.addActionListener(e -> exportInvoiceToImage(maHDN));
        actionPanel.add(btnExportImage);

        btnDong = createButton("Đóng");
        styleButton(btnDong, accentOrange, Color.WHITE);
        btnDong.addActionListener(e -> dispose());
        actionPanel.add(btnDong);

        add(actionPanel, BorderLayout.SOUTH);

        // Load data for the given MaHDN
        loadHoaDonNhapDetails(maHDN);

        // Pack and center the dialog
        pack();
        setLocationRelativeTo(owner);
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
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
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
                BorderFactory.createLineBorder(fgColor, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
    }

    // Helper method to style the table
    private void setupTableStyle(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(20);
        table.setFillsViewportHeight(true);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(coffeeBrown);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setReorderingAllowed(false);

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
                if (isSelected) {
                    c.setBackground(new Color(180, 210, 230));
                }

                String columnName = table.getColumnModel().getColumn(column).getHeaderValue().toString();
                if (columnName.equals("Số lượng") || columnName.equals("Đơn giá nhập") || columnName.equals("Khuyến mãi (%)") || columnName.equals("Thành tiền")) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else if (columnName.equals("Mã SP")) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                setText(value != null ? value.toString() : "");
                return c;
            }
        });
    }

    // Load header and detail data for the given MaHDN
    private void loadHoaDonNhapDetails(String maHDN) {
        HoaDonNhap hdn = hoaDonNhapDAO.getHoaDonNhapByMaHDN(maHDN);

        if (hdn != null) {
            lblMaHDNValue.setText(hdn.getMaHDN());
            lblNgayNhapValue.setText(hdn.getNgayNhap() != null ? dateFormat.format(hdn.getNgayNhap()) : "N/A");
            lblTenNVValue.setText(hdn.getTenNV() != null ? hdn.getTenNV() : "N/A");
            lblTenNCCValue.setText(hdn.getTenNCC() != null ? hdn.getTenNCC() : "N/A");
            lblTongTienValue.setText(currencyFormatter.format(hdn.getTongTien()) + " VNĐ");

            tblChiTietModel.setRowCount(0);
            List<ChiTietHoaDonNhap> chiTietList = chiTietHoaDonNhapDAO.getChiTietHoaDonNhapByMaHDN(maHDN);

            if (chiTietList != null) {
                for (ChiTietHoaDonNhap ct : chiTietList) {
                    Vector<Object> row = new Vector<>();
                    row.add(ct.getMaSP());
                    row.add(ct.getTenSP());
                    row.add(ct.getSoluong());
                    row.add(currencyFormatter.format(ct.getDongia()));
                    row.add(currencyFormatter.format(ct.getThanhtien()));
                    tblChiTietModel.addRow(row);
                }
            } else {
                System.out.println("Không tìm thấy chi tiết hóa đơn nhập cho mã: " + maHDN);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy hóa đơn nhập có mã: " + maHDN, "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    // Method to export invoice as an image in a classic style
    private void exportInvoiceToImage(String maHDN) {
        HoaDonNhap hdn = hoaDonNhapDAO.getHoaDonNhapByMaHDN(maHDN);
        List<ChiTietHoaDonNhap> chiTietList = chiTietHoaDonNhapDAO.getChiTietHoaDonNhapByMaHDN(maHDN);

        if (hdn == null || chiTietList == null || chiTietList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy dữ liệu chi tiết cho hóa đơn này.", "Lỗi Xuất", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create a BufferedImage with receipt dimensions (e.g., 350x750 pixels)
        int width = 350;
        int height = 750;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Enable anti-aliasing for smoother text and shapes
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Parchment background with subtle texture effect
        g2d.setColor(parchment);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(new Color(200, 180, 150, 50)); // Semi-transparent overlay for texture
        for (int i = 0; i < width; i += 10) {
            for (int j = 0; j < height; j += 10) {
                if (Math.random() > 0.9) {
                    g2d.fillRect(i, j, 5, 5);
                }
            }
        }

        // Draw a decorative border
        g2d.setColor(darkSepia);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(10, 10, width - 20, height - 20);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(15, 15, width - 30, height - 30);

        int y = 30;
        int lineHeight = 20;
        int pageWidth = width;

        // Header: Vietnamese Title with Ornate Font
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.setColor(sepiaText);
        g2d.drawString("THE COFFEE TEAM", autoCenterText(g2d, "THE COFFEE TEAM", pageWidth), y);
        y += lineHeight * 2;

        // Address in classic style
        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        String address = "Số 12 Phố Chùa Bộc, Q.Đống Đa, TP. Hà Nội";
        g2d.drawString(address, autoCenterText(g2d, address, pageWidth), y);
        y += lineHeight;
        g2d.drawString("HN 101", autoCenterText(g2d, "HN 101", pageWidth), y);
        y += lineHeight * 1;

        // Decorative divider
        g2d.setColor(darkSepia);
        drawDecorativeLine(g2d, 20, y, pageWidth - 20);
        y += lineHeight;

        // Items Header with Precise Alignment
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(sepiaText);
        g2d.drawString("Số HĐ: " + hdn.getMaHDN(), 20, y); y += lineHeight;
        g2d.drawString("Ngày: " + (hdn.getNgayNhap() != null ? printDateFormat.format(hdn.getNgayNhap()) : "N/A"), 20, y); y += lineHeight;
        g2d.drawString("Nhân viên: " + (hdn.getMaNV() != null ? hdn.getMaNV() : "N/A"), 20, y); y += lineHeight;
        g2d.drawString("Nhà cung cấp: " + (hdn.getTenNCC() != null ? hdn.getTenNCC() : "Nhà Cung Cấp toàn quốc"), 20, y); y += lineHeight;
        g2d.drawString("Giao hàng: Nhà phân phối Hạnh Phúc", 20, y); y += lineHeight * 2;

        // Items Header with Precise Alignment
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("STT     Tên món      Số Lượng       Đơn giá         Thành tiền", 20, y);
        drawDecorativeLine(g2d, 20, y + 5, pageWidth - 20);
        y += lineHeight * 2;

        // Items List with Fixed Column Widths
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        int itemCount = 0;
        int sttWidth = 40;
        int nameWidth = 100;
        int qtyWidth = 30;
        int priceWidth = 80;
        int totalWidth = 80;
        for (ChiTietHoaDonNhap ct : chiTietList) {
            itemCount++;
            String itemName = ct.getTenSP().length() > 10 ? ct.getTenSP().substring(0, 10) + "..." : ct.getTenSP();
            String sttStr = String.format("%-4d", itemCount);
            String nameStr = String.format("%-10s", itemName);
            String qtyStr = String.format("%-5d", ct.getSoluong());
            String priceStr = String.format("%-12s", currencyFormatter.format(ct.getDongia()));
            String totalStr = String.format("%-12s", currencyFormatter.format(ct.getThanhtien()));

            g2d.drawString(sttStr, 20, y);
            g2d.drawString(nameStr, 20 + sttWidth, y);
            g2d.drawString(qtyStr, 20 + sttWidth + nameWidth, y);
            g2d.drawString(priceStr, 20 + sttWidth + nameWidth + qtyWidth, y);
            g2d.drawString(totalStr, 20 + sttWidth + nameWidth + qtyWidth + priceWidth, y);
            y += lineHeight;
        }
        y += 5;
        drawDecorativeLine(g2d, 20, y, pageWidth - 20); y += lineHeight;
        g2d.setFont(new Font("Arial", Font.ITALIC, 10));
        String text = String.format("Đơn vị tính: VNĐ", chiTietList.size()); 
        FontMetrics fm = g2d.getFontMetrics();
        int stringWidth = fm.stringWidth(text);
        int x = width - 20 - stringWidth;
        g2d.drawString(text, x, y);
        y += lineHeight;
        // Totals
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString(String.format("Tổng số món: %-25d", chiTietList.size()), 20, y); y += lineHeight;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(String.format("Tổng cộng: %-23s VNĐ", currencyFormatter.format(hdn.getTongTien())), 20, y); y += lineHeight;
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Phương thức: Chuyển khoản", 20, y); y += lineHeight;
        g2d.drawString(String.format("Đã thanh toán: %-23s VNĐ", currencyFormatter.format(hdn.getTongTien())), 20, y); y += lineHeight * 2;

        // Footer
        g2d.setFont(new Font("Arial", Font.ITALIC, 11));
        g2d.drawString("Cảm ơn quý đối tác đã hợp tác với THE COFFEE TEAM.", autoCenterText(g2d, "Cảm ơn quý đối tác đã hợp tác với THE COFFEE TEAM.", pageWidth), y); y += lineHeight;
        g2d.drawString("Hẹn gặp lại!", autoCenterText(g2d, "Hẹn gặp lại!", pageWidth), y); y += lineHeight * 1;
        // g2d.drawString("Liên hệ: 02701 807", 20, y); y += lineHeight;
        // g2d.drawString("Website: https://web.facebook.com/duy.nguyenquang.5439", 20, y); y += lineHeight;
        try {
            // 1. Tải ảnh QR code từ file
            BufferedImage qrImage = ImageIO.read(new File("D://KyIV_HocVienNganHang//LapTrinhHuongDoiTuong//QuanLyCuaHangCaPhe//CoffeeShop//src//images//QR.jpg"));
            int qrWidth = qrImage.getWidth();
            int qrHeight = qrImage.getHeight();
            int qrX = (pageWidth - qrWidth) / 3;
            g2d.drawImage(qrImage, qrX, y, null);
            
            y += qrHeight + lineHeight;
        } catch (IOException e) {
            e.printStackTrace();
            g2d.drawString("[QR Code]", autoCenterText(g2d, "[QR Code]", pageWidth), y);
            y += lineHeight * 2;
        }

        // Wi-Fi Password
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Mật khẩu Wi-Fi: ban50kroicho", autoCenterText(g2d, "Mật khẩu Wi-Fi: ban50kroicho", pageWidth), y);

        g2d.dispose();

        // Save the image
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu Hóa đơn Nhập thành ảnh");
        fileChooser.setSelectedFile(new File("HoaDonNhap_" + maHDN + "_" + new SimpleDateFormat("HHmmss").format(new java.util.Date()) + ".png"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".png")) {
                fileToSave = new File(filePath + ".png");
            }

            try {
                ImageIO.write(image, "png", fileToSave);
                JOptionPane.showMessageDialog(this, "Xuất hóa đơn thành công! File: " + fileToSave.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(fileToSave);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất hóa đơn thành ảnh: " + e.getMessage(), "Lỗi Xuất", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    // Helper method to center text
    private int autoCenterText(Graphics2D g2d, String text, int pageWidth) {
        FontMetrics fm = g2d.getFontMetrics();
        int stringWidth = fm.stringWidth(text);
        return (pageWidth - stringWidth) / 2;
    }

    // Helper method to draw a decorative line
    private void drawDecorativeLine(Graphics2D g2d, int x1, int y, int x2) {
        g2d.setColor(darkSepia);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(x1, y, x2, y);
        g2d.setStroke(new BasicStroke(0.5f));
        g2d.drawLine(x1 + 5, y - 3, x2 - 5, y - 3);
        g2d.drawLine(x1 + 5, y + 3, x2 - 5, y + 3);
    }
}