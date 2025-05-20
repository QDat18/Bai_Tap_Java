package ui;

import dao.CTHoaDonBanDAO;
import dao.HoaDonBanDAO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.CTHoaDonBan;
import model.HoaDonBan;

public class CTHoaDonBanDetailsDialog extends JDialog {

    // Define colors (reusing and adding classic-style colors)
    Color coffeeBrown = new Color(102, 51, 0);
    Color darkGray = new Color(50, 50, 50);
    Color lightBeige = new Color(245, 245, 220);
    Color accentOrange = new Color(255, 165, 0);
    Color accentBlue = new Color(30, 144, 255);
    Color accentGreen = new Color(60, 179, 113);
    Color parchment = new Color(255, 245, 224); // Vintage parchment background
    Color sepiaText = new Color(112, 66, 20); // Sepia tone for text
    Color darkSepia = new Color(80, 40, 10); // Darker sepia for borders

    // UI Components for header info
    private JLabel lblMaHDB;
    private JLabel lblTenNV;
    private JLabel lblTenKH;
    private JLabel lblNgayBan;
    private JLabel lblTongTien;

    // UI Components for details table
    private JTable chiTietTable;
    private DefaultTableModel chiTietTableModel;

    // Data Access Objects
    private HoaDonBanDAO hoaDonBanDAO;
    private CTHoaDonBanDAO ctHoaDonBanDAO;

    // Date and currency formatters
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private SimpleDateFormat printDateFormat = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0");

    // Constructor
    public CTHoaDonBanDetailsDialog(JFrame owner, String maHDB) {
        super(owner, "Chi tiết Hóa đơn Bán: " + maHDB, true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setMinimumSize(new Dimension(600, 500));
        setLocationRelativeTo(owner);

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
        gbc.anchor = GridBagConstraints.WEST;

        // Row 0: MaHDB, NgayBan
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; headerInfoPanel.add(createLabel("Mã HĐB:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; lblMaHDB = createValueLabel(maHDB); headerInfoPanel.add(lblMaHDB, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0; headerInfoPanel.add(createLabel("Ngày bán:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 1.0; lblNgayBan = createValueLabel(""); headerInfoPanel.add(lblNgayBan, gbc);

        // Row 1: TenNV, TenKH
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; headerInfoPanel.add(createLabel("Nhân viên:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; lblTenNV = createValueLabel(""); headerInfoPanel.add(lblTenNV, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0; headerInfoPanel.add(createLabel("Khách hàng:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 1.0; lblTenKH = createValueLabel(""); headerInfoPanel.add(lblTenKH, gbc);

        // Row 2: TongTien
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; headerInfoPanel.add(createLabel("Tổng tiền:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; gbc.gridwidth = 3;
        lblTongTien = createValueLabel("");
        lblTongTien.setFont(new Font("Arial", Font.BOLD, 16));
        lblTongTien.setForeground(accentGreen);
        headerInfoPanel.add(lblTongTien, gbc);
        gbc.gridwidth = 1;

        contentPane.add(headerInfoPanel, BorderLayout.NORTH);

        // --- Details Table Panel ---
        JPanel detailsTablePanel = new JPanel(new BorderLayout());
        detailsTablePanel.setBackground(lightBeige);
        detailsTablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Chi tiết sản phẩm", 0, 0, new Font("Arial", Font.BOLD, 14), coffeeBrown));

        chiTietTableModel = new DefaultTableModel(new Object[]{"Mã SP", "Tên SP", "Số lượng", "Đơn giá", "Khuyến mãi (%)", "Thành tiền"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2 || columnIndex == 3 || columnIndex == 4 || columnIndex == 5) return Integer.class;
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

        contentPane.add(detailsTablePanel, BorderLayout.CENTER);

        // --- Footer Button Panel ---
        JPanel footerButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerButtonPanel.setBackground(lightBeige);
        JButton btnExportImage = new JButton("Xuất Hóa đơn thành ảnh");
        styleButton(btnExportImage, accentBlue, Color.WHITE);
        btnExportImage.addActionListener(e -> exportInvoiceToImage(maHDB));
        footerButtonPanel.add(btnExportImage);

        JButton btnClose = new JButton("Đóng");
        styleButton(btnClose, darkGray, Color.WHITE);
        btnClose.addActionListener(e -> dispose());
        footerButtonPanel.add(btnClose);

        contentPane.add(footerButtonPanel, BorderLayout.SOUTH);

        setContentPane(contentPane);

        loadHoaDonDetails(maHDB);
        setModal(true);
    }

    // Method to load and display invoice header and detail data
    private void loadHoaDonDetails(String maHDB) {
        HoaDonBan hoaDonBan = hoaDonBanDAO.getHoaDonBanById(maHDB);

        if (hoaDonBan != null) {
            lblMaHDB.setText(hoaDonBan.getMaHDB());
            lblTenNV.setText(hoaDonBan.getTenNV() != null ? hoaDonBan.getTenNV() : "N/A");
            lblTenKH.setText(hoaDonBan.getTenKH() != null ? hoaDonBan.getTenKH() : "Khách lẻ");
            lblNgayBan.setText(hoaDonBan.getNgayban() != null ? dateFormat.format(hoaDonBan.getNgayban()) : "N/A");
            lblTongTien.setText(currencyFormat.format(hoaDonBan.getTongtien()) + " VNĐ");

            chiTietTableModel.setRowCount(0);
            List<CTHoaDonBan> chiTietList = ctHoaDonBanDAO.getChiTietHoaDonByMaHDB(maHDB);

            if (chiTietList != null && !chiTietList.isEmpty()) {
                for (CTHoaDonBan ct : chiTietList) {
                    chiTietTableModel.addRow(new Object[]{
                        ct.getMaSP(),
                        ct.getTenSP(),
                        ct.getSoluong(),
                        ct.getGiaban(),
                        ct.getKhuyenmai(),
                        ct.getThanhtien()
                    });
                }
            }
        } else {
            System.err.println("Không tìm thấy hóa đơn bán với mã: " + maHDB);
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin hóa đơn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    // Method to export invoice as an image in a classic style with adjusted alignment
    private void exportInvoiceToImage(String maHDB) {
        HoaDonBan hdb = hoaDonBanDAO.getHoaDonBanById(maHDB);
        List<CTHoaDonBan> chiTietList = ctHoaDonBanDAO.getChiTietHoaDonByMaHDB(maHDB);

        if (hdb == null || chiTietList == null || chiTietList.isEmpty()) {
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
        y += lineHeight * 3;

        // Address in classic style
        g2d.setFont(new Font("Arial", Font.ITALIC, 12));
        String address = "ĐC: Số 12 Phố Chùa Bộc, Q. Đống Đa, TP. Hà Nội";
        g2d.drawString(address, autoCenterText(g2d, address, pageWidth), y);
        y += lineHeight;
        g2d.drawString("HN 101", autoCenterText(g2d, "HN 101", pageWidth), y);
        y += lineHeight * 2;

        // Decorative divider
        g2d.setColor(darkSepia);
        drawDecorativeLine(g2d, 20, y, pageWidth - 20);
        y += lineHeight;

        // Invoice Info
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.setColor(sepiaText);
        g2d.drawString("Số HĐ: " + hdb.getMaHDB(), 20, y); y += lineHeight;
        g2d.drawString("Ngày: " + (hdb.getNgayban() != null ? printDateFormat.format(hdb.getNgayban()) : "N/A"), 20, y); y += lineHeight;
        g2d.drawString("Nhân viên: " + (hdb.getMaNV() != null ? hdb.getMaNV() : "N/A"), 20, y); y += lineHeight;
        g2d.drawString("Khách hàng: " + (hdb.getTenKH() != null ? hdb.getTenKH() : "Khách lẻ"), 20, y); y += lineHeight;
        g2d.drawString("Giao hàng: Xanh SM", 20, y); y += lineHeight * 2;

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
        for (CTHoaDonBan ct : chiTietList) {
            itemCount++;
            String itemName = ct.getTenSP().length() > 10 ? ct.getTenSP().substring(0, 10) + "..." : ct.getTenSP();
            String sttStr = String.format("%-4d", itemCount);
            String nameStr = String.format("%-10s", itemName);
            String qtyStr = String.format("%-5d", ct.getSoluong());
            String priceStr = String.format("%-12s", currencyFormat.format(ct.getGiaban()) + " VNĐ");
            String totalStr = String.format("%-12s", currencyFormat.format(ct.getThanhtien()) + " VNĐ");

            g2d.drawString(sttStr, 20, y);
            g2d.drawString(nameStr, 20 + sttWidth, y);
            g2d.drawString(qtyStr, 20 + sttWidth + nameWidth, y);
            g2d.drawString(priceStr, 20 + sttWidth + nameWidth + qtyWidth, y);
            g2d.drawString(totalStr, 20 + sttWidth + nameWidth + qtyWidth + priceWidth, y);
            y += lineHeight;
        }
        // Decorative divider
        y += 5;
        drawDecorativeLine(g2d, 20, y, pageWidth - 20); y += lineHeight;

        // Totals
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString(String.format("Tổng số món: %-25d", chiTietList.size()), 20, y); y += lineHeight;
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(String.format("Tổng cộng: %-23s VNĐ", currencyFormat.format(hdb.getTongtien())), 20, y); y += lineHeight;
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("Phương thức: Online", 20, y); y += lineHeight;
        g2d.drawString(String.format("Đã thanh toán: %-23s VNĐ", currencyFormat.format(hdb.getTongtien())), 20, y); y += lineHeight * 2;

        // Footer
        g2d.setFont(new Font("Arial", Font.ITALIC, 11));
        g2d.drawString("Cảm ơn quý khách đã ghé thăm THE COFFEE TEAM.", autoCenterText(g2d, "Cảm ơn quý khách đã ghé thăm THE COFFEE TEAM.", pageWidth), y); y += lineHeight;
        g2d.drawString("Hẹn gặp lại!", autoCenterText(g2d, "Hẹn gặp lại!", pageWidth), y); y += lineHeight * 1;
        g2d.drawString("Liên hệ: 02701 807", 20, y); y += lineHeight;
        g2d.drawString("Website: https://web.facebook.com/duy.nguyenquang.5439", 20, y); y += lineHeight;
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
        fileChooser.setDialogTitle("Lưu Hóa đơn thành ảnh");
        fileChooser.setSelectedFile(new File("HoaDon_" + maHDB + "_" + new SimpleDateFormat("HHmmss").format(new java.util.Date()) + ".png"));
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
}