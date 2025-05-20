package ui;

import dao.*;
import model.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.*;
import java.util.List;

public class HomePanel extends JPanel {
    // Color constants
    private final Color PRIMARY_COLOR = new Color(78, 52, 46);
    private final Color SECONDARY_COLOR = new Color(250, 250, 235);
    private final Color ACCENT_COLOR = new Color(102, 187, 106);
    private final Color WARNING_COLOR = new Color(255, 152, 0);
    private final Color INFO_COLOR = new Color(41, 182, 246);
    private final Color TEXT_COLOR = new Color(33, 33, 33);
    private final Color MUTED_TEXT_COLOR = new Color(80, 80, 80);
    private final Color BORDER_COLOR = new Color(189, 189, 189);
    private final Color ACCENT_BLUE = new Color(30, 144, 255);

    // UI Components
    private JLabel lblWelcomeMessage;
    private JComboBox<Loai> cbLoai;
    private JButton btnFilter, btnAddInvoice, btnViewAll, btnViewInWindow;
    private JPanel productContainerPanel, productTitleAndButtonsPanel;
    private JScrollPane productScrollPane;
    private JLabel lblTotalSalesTodayValue, lblLowStockValue, lblDailySalesCountValue, lblNewCustomersTodayValue;
    private JPanel chartsPlaceholderPanel, statsWrapperPanel;

    // DAOs
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();
    private final LoaiDAO loaiDAO = new LoaiDAO();
    private final HoaDonBanDAO hoaDonBanDAO = new HoaDonBanDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    // User info
    private final NhanVien loggedInUser;
    private final SanPhamUI sanphamUI;

    public HomePanel(NhanVien user, SanPhamUI sanphamUI) {
        this.loggedInUser = user;
        this.sanphamUI = sanphamUI;

        initUI();
        loadInitialData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(SECONDARY_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Top Panel
        add(createTopPanel(), BorderLayout.NORTH);
        
        // Center Panel
        add(createCenterPanel(), BorderLayout.CENTER);
        
        // Bottom Panel
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Welcome Panel
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setOpaque(false);
        
        String displayName = loggedInUser.getTenNV() != null && !loggedInUser.getTenNV().isEmpty() 
            ? loggedInUser.getTenNV() : loggedInUser.getTendangnhap();
        
        lblWelcomeMessage = new JLabel("<html><h2><span style='color:" + toHex(PRIMARY_COLOR) + ";'>Chào mừng, " + 
            displayName + "!</span></h2><p><span style='color:" + toHex(TEXT_COLOR) + ";'>Vai trò của bạn: <b>" + 
            loggedInUser.getRole() + "</b></span></p></html>");
        welcomePanel.add(lblWelcomeMessage);
        topPanel.add(welcomePanel, BorderLayout.WEST);

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setOpaque(false);
        
        filterPanel.add(new JLabel("Lọc sản phẩm theo loại:"));
        
        cbLoai = new JComboBox<>();
        cbLoai.setBackground(Color.WHITE);
        cbLoai.setForeground(TEXT_COLOR);
        filterPanel.add(cbLoai);
        
        btnFilter = createButton("Lọc", ACCENT_COLOR);
        filterPanel.add(btnFilter);
        
        btnAddInvoice = createButton("Thêm Hóa Đơn", INFO_COLOR);
        filterPanel.add(btnAddInvoice);
        
        topPanel.add(filterPanel, BorderLayout.EAST);
        
        // Event listeners
        cbLoai.addActionListener(e -> handleCategoryFilter());
        btnFilter.addActionListener(e -> cbLoai.setSelectedItem(cbLoai.getSelectedItem()));
        btnAddInvoice.addActionListener(e -> openHoaDonBanCreationDialog());
        
        return topPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Stats Panel
        statsWrapperPanel = new JPanel(new BorderLayout());
        statsWrapperPanel.setOpaque(false);
        statsWrapperPanel.setBorder(createTitledBorder("Thống kê nhanh"));
        
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 15, 15));
        statsGrid.setOpaque(false);
        
        statsGrid.add(createStatCard("Doanh thu hôm nay", "0 VNĐ", ACCENT_COLOR, lblTotalSalesTodayValue = new JLabel()));
        statsGrid.add(createStatCard("Hóa đơn bán hôm nay", "0", INFO_COLOR, lblDailySalesCountValue = new JLabel()));
        
        JPanel lowStockCard = createStatCard("Sản phẩm tồn kho thấp", "0", WARNING_COLOR, lblLowStockValue = new JLabel());
        setupLowStockCard(lowStockCard);
        statsGrid.add(lowStockCard);
        
        statsGrid.add(createStatCard("Khách hàng mới hôm nay", "0", INFO_COLOR, lblNewCustomersTodayValue = new JLabel()));
        
        statsWrapperPanel.add(statsGrid, BorderLayout.CENTER);
        centerPanel.add(statsWrapperPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Products Panel
        JPanel productsPanel = new JPanel(new BorderLayout());
        productsPanel.setOpaque(false);
        
        productTitleAndButtonsPanel = new JPanel(new BorderLayout());
        productTitleAndButtonsPanel.setOpaque(false);
        productTitleAndButtonsPanel.setBorder(createTitledBorder("Danh sách Sản phẩm"));
        
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttonsPanel.setOpaque(false);
        
        btnViewAll = createButton("Xem tất cả", ACCENT_BLUE);
        btnViewAll.addActionListener(e -> handleViewAllProducts());
        buttonsPanel.add(btnViewAll);
        
        btnViewInWindow = createButton("Xem dạng danh sách", ACCENT_BLUE);
        btnViewInWindow.addActionListener(e -> openSanPhamAllWindow());
        buttonsPanel.add(btnViewInWindow);
        
        productTitleAndButtonsPanel.add(buttonsPanel, BorderLayout.EAST);
        productsPanel.add(productTitleAndButtonsPanel, BorderLayout.NORTH);
        
        productContainerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        productContainerPanel.setOpaque(false);
        
        productScrollPane = new JScrollPane(productContainerPanel);
        productScrollPane.setBorder(null);
        productsPanel.add(productScrollPane, BorderLayout.CENTER);
        
        centerPanel.add(productsPanel);
        
        return centerPanel;
    }

    private JPanel createBottomPanel() {
        chartsPlaceholderPanel = new JPanel(new BorderLayout());
        chartsPlaceholderPanel.setOpaque(false);
        chartsPlaceholderPanel.setBorder(createTitledBorder("Dự đoán ngày hôm sau"));
        
        JLabel lblChartMessage = new JLabel("<html><center>Đặt mã nguồn biểu đồ tại đây.</center><br><center>Cần sử dụng thư viện biểu đồ (ví dụ: JFreeChart).</center></html>", SwingConstants.CENTER);
        lblChartMessage.setForeground(MUTED_TEXT_COLOR);
        chartsPlaceholderPanel.add(lblChartMessage, BorderLayout.CENTER);
        
        return chartsPlaceholderPanel;
    }

    private void loadInitialData() {
        loadCategories();
        displayProducts(null);
        loadStatisticalData();
        loadSalesChart();
        applyRolePermissions();
    }

    private void loadCategories() {
        cbLoai.removeAllItems();
        cbLoai.addItem(new Loai("", "-- Tất cả loại --"));

        List<Loai> loaiList = loaiDAO.getAllLoai();
        if (loaiList != null) {
            loaiList.forEach(cbLoai::addItem);
        }
    }

    private void displayProducts(List<SanPham> products) {
        productContainerPanel.removeAll();

        List<SanPham> productsToDisplay = products != null ? products : 
            cbLoai.getSelectedIndex() == 0 ? 
                sanPhamDAO.getAllSanPham() : 
                sanPhamDAO.getSanPhamByLoai(((Loai)cbLoai.getSelectedItem()).getMaloai());

        if (productsToDisplay != null && !productsToDisplay.isEmpty()) {
            productsToDisplay.forEach(sp -> productContainerPanel.add(new ProductBoxPanel(sp)));
        } else {
            JLabel noProductLabel = new JLabel("Không tìm thấy sản phẩm nào.");
            noProductLabel.setForeground(TEXT_COLOR);
            productContainerPanel.add(noProductLabel);
        }

        productContainerPanel.revalidate();
        productContainerPanel.repaint();
    }

    private JPanel createStatCard(String title, String value, Color color, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new CompoundBorder(
            new LineBorder(color, 2),
            new EmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);

        JPanel header = new JPanel();
        header.setBackground(color);
        header.add(new JLabel(title, SwingConstants.CENTER) {{
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 13));
        }});

        valueLabel.setText(value);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 20));

        card.add(header, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    private void setupLowStockCard(JPanel card) {
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                productTitleAndButtonsPanel.setBorder(createTitledBorder("Danh sách Sản phẩm tồn kho thấp"));
                cbLoai.setSelectedIndex(0);
                displayProducts(sanPhamDAO.getLowStockProducts(10));
            }
            @Override public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(WARNING_COLOR.darker(), 3));
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(WARNING_COLOR, 2),
                    new EmptyBorder(10, 10, 10, 10)
                ));
            }
        });
    }

    private void handleCategoryFilter() {
        Loai selected = (Loai) cbLoai.getSelectedItem();
        if (selected != null) {
            String title = selected.getMaloai().isEmpty() ? 
                "Danh sách Sản phẩm" : 
                "Danh sách Sản phẩm theo loại: " + selected.getTenloai();
            
            productTitleAndButtonsPanel.setBorder(createTitledBorder(title));
            displayProducts(null);
        }
    }

    private void handleViewAllProducts() {
        cbLoai.setSelectedIndex(0);
        productTitleAndButtonsPanel.setBorder(createTitledBorder("Danh sách Tất cả Sản phẩm"));
        displayProducts(null);
        
        btnViewAll.setBackground(ACCENT_BLUE.darker());
        new Timer(150, e -> {
            btnViewAll.setBackground(ACCENT_BLUE);
            ((Timer)e.getSource()).stop();
        }).start();
    }

    private void openSanPhamAllWindow() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingUtilities.invokeLater(() -> {
            try {
                Loai selected = (Loai) cbLoai.getSelectedItem();
                SanPhamAll frame = selected != null && !selected.getMaloai().isEmpty() ?
                    new SanPhamAll(loggedInUser, selected.getMaloai()) :
                    new SanPhamAll(loggedInUser);
                
                frame.setLocationRelativeTo(this);
                frame.setVisible(true);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Không thể mở cửa sổ: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            } finally {
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    private void openHoaDonBanCreationDialog() {
        HoaDonBanCreationDialog dialog = new HoaDonBanCreationDialog(
            (JFrame)SwingUtilities.getWindowAncestor(this), loggedInUser, sanphamUI);
        
        dialog.setVisible(true);
        
        if (dialog.isSavedSuccessfully()) {
            loadStatisticalData();
            loadSalesChart();
        }
    }

    private void loadStatisticalData() {
        DecimalFormat currency = new DecimalFormat("#,### VNĐ");
        
        try {
            lblTotalSalesTodayValue.setText(currency.format(hoaDonBanDAO.getTotalSalesAmountForToday()));
            lblDailySalesCountValue.setText(String.valueOf(hoaDonBanDAO.getSalesCountForToday()));
            
            int lowStock = sanPhamDAO.getLowStockCount(10);
            lblLowStockValue.setText(String.valueOf(lowStock));
            lblLowStockValue.setForeground(lowStock > 0 ? WARNING_COLOR.darker() : TEXT_COLOR);
            
            int newCustomers = khachHangDAO.getNewCustomersCountForToday();
            lblNewCustomersTodayValue.setText(newCustomers == -1 ? "Lỗi" : String.valueOf(newCustomers));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi tải dữ liệu thống kê: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSalesChart() {
        chartsPlaceholderPanel.removeAll();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT FORMAT(Ngayban, 'yyyy-MM-dd') AS Date, SUM(Tongtien) AS TotalSales " +
                "FROM HoaDonBan GROUP BY FORMAT(Ngayban, 'yyyy-MM-dd') ORDER BY Date")) {
            
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            while (rs.next()) {
                dataset.addValue(rs.getDouble("TotalSales"), "Doanh thu", rs.getString("Date"));
            }
            
            JFreeChart chart = ChartFactory.createBarChart(
                "Báo cáo Doanh thu Theo Ngày", "Ngày", "Doanh thu (VND)", 
                dataset, PlotOrientation.VERTICAL, true, true, false);
            
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            
            chart.getTitle().setFont(new Font("Arial", Font.BOLD, 14));
            chart.getTitle().setPaint(PRIMARY_COLOR);
            
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(chartsPlaceholderPanel.getWidth(), 200));
            chartsPlaceholderPanel.add(chartPanel);
        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Lỗi tải biểu đồ: " + e.getMessage(), SwingConstants.CENTER);
            errorLabel.setForeground(MUTED_TEXT_COLOR);
            chartsPlaceholderPanel.add(errorLabel);
        }
        
        chartsPlaceholderPanel.revalidate();
        chartsPlaceholderPanel.repaint();
    }

    private void applyRolePermissions() {
        String role = loggedInUser.getRole();
        boolean isManagerOrAdmin = "Manager".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role);
        
        statsWrapperPanel.setVisible(isManagerOrAdmin);
        chartsPlaceholderPanel.setVisible(isManagerOrAdmin);
        revalidate();
        repaint();
    }

    // Helper methods
    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(bgColor.darker(), 1),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(bgColor.darker()); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(bgColor); }
        });
        
        return button;
    }

    private TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1), 
            title, TitledBorder.LEADING, TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 14), PRIMARY_COLOR);
    }

    private String toHex(Color color) {
        return String.format("#%06x", color.getRGB() & 0xFFFFFF);
    }
}