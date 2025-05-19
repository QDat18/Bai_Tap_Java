package ui;

import dao.DatabaseConnection;
// Import các DAO cần thiết
import dao.HoaDonBanDAO;
import dao.HoaDonNhapDAO;
import dao.KhachHangDAO;
import dao.LoaiDAO;
import dao.SanPhamDAO;
import java.awt.*;
import java.awt.event.MouseAdapter; // For ProductBoxPanel mouse listener
import java.awt.event.MouseEvent; // For ProductBoxPanel mouse listener
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat; // For currency formatting
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import model.CTHoaDonBan;
import model.HoaDonBan;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import model.Loai;
import model.NhanVien;
import model.SanPham;
import ui.SanPhamAll;

public class HomePanel extends JPanel {

    // Define colors (New refined palette for HomePanel content)
    private final Color primaryColor = new Color(78, 52, 46); // #4E342E - Dark Brown (for titles, borders)
    private final Color secondaryColor = new Color(250, 250, 235); // #FAFAEB - Very light warm beige (background)
    private final Color accentColor = new Color(102, 187, 106); // #66BB6A - Medium Green (Positive stats like Sales, maybe Filter button)
    private final Color warningColor = new Color(255, 152, 0); // #FF9800 - Orange (Low Stock)
    private final Color infoColor = new Color(41, 182, 246); // #29B6F6 - Light Blue (Info stats like Sales Count, New Customers)
    private final Color textColor = new Color(33, 33, 33); // #212121 - Very Dark Gray (General text)
    private final Color mutedTextColor = new Color(80, 80, 80); // #505050 - Muted text color (for less important info)
    private final Color borderColor = new Color(189, 189, 189); // #BDBDBD - Light Gray (Component borders)
    private final Color accentBlue = new Color(30, 144, 255);
    private final Color lightBeige = new Color(245, 245, 220); // Ví dụ màu nền
    private final Color darkGray = new Color(50, 50, 50); // Ví dụ màu chữ
    // UI Components (Welcome)
    private JLabel lblWelcomeMessage;

    // UI Components (Product Display and Filtering)
    private JComboBox<Loai> cbLoai; // ComboBox to select product category
    private JButton btnFilter; // Button to trigger filtering
    private JPanel productContainerPanel; // Panel to hold ProductBoxPanels
    private JScrollPane productScrollPane; // Scroll pane for the product container
    private JButton btnViewAllProductsProducts;

    // UI Components for Stats (Mini Dashboard) - Made accessible for updates
    private JLabel lblTotalSalesTodayValue;
    private JLabel lblLowStockValue;
    private JLabel lblDailySalesCountValue;
    private JLabel lblNewCustomersTodayValue;

    // UI Components for Charts Placeholder - Made accessible for role permissions
    private JPanel chartsPlaceholderPanel;
    private JPanel statsWrapperPanel; // Made instance variable to control visibility

    // Data Access Objects
    private SanPhamDAO sanPhamDAO;
    private LoaiDAO loaiDAO;
    private HoaDonBanDAO hoaDonBanDAO;
    private HoaDonNhapDAO hoaDonNhapDAO;
    private KhachHangDAO khachHangDAO;

    // Thông tin nhân viên đã đăng nhập
    private NhanVien loggedInUser;
    private SanPhamUI sanphamUI;

    // Constructor nhận thông tin nhân viên đã đăng nhập
    public HomePanel(NhanVien user, SanPhamUI sanphamUI) {
        this.loggedInUser = user;
        sanphamUI = new SanPhamUI(loggedInUser);
        // Initialize DAOs
        sanPhamDAO = new SanPhamDAO();
        loaiDAO = new LoaiDAO();
        hoaDonBanDAO = new HoaDonBanDAO();
        hoaDonNhapDAO = new HoaDonNhapDAO();
        khachHangDAO = new KhachHangDAO();

        // Set layout for the panel
        setLayout(new BorderLayout(10, 10));
        setBackground(secondaryColor);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- Top Panel (Welcome and Filtering Controls) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Left side: Welcome message
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setOpaque(false);

        String displayName = loggedInUser.getTenNV();
        if (displayName == null || displayName.isEmpty()) {
            displayName = loggedInUser.getTendangnhap();
        }
        String role = loggedInUser.getRole();

        lblWelcomeMessage = new JLabel("<html><h2><span style='color:" + toHex(primaryColor) + ";'>Chào mừng, " + displayName + "!</span></h2><p><span style='color:" + toHex(textColor) + ";'>Vai trò của bạn: <b>" + role + "</b></span></p></html>");
        lblWelcomeMessage.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomePanel.add(lblWelcomeMessage);

        topPanel.add(welcomePanel, BorderLayout.WEST);

        // Right side: Filtering controls
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setOpaque(false);
        filterPanel.add(createLabel("Lọc sản phẩm theo loại:"));

        cbLoai = new JComboBox<>();
        cbLoai.setBackground(Color.WHITE);
        cbLoai.setForeground(textColor);
        cbLoai.setFont(new Font("Arial", Font.PLAIN, 12));
        filterPanel.add(cbLoai);

        btnFilter = new JButton("Lọc");
        styleButton(btnFilter, accentColor, Color.WHITE);

        filterPanel.add(btnFilter);

        JButton btnAddInvoice = new JButton("Thêm Hóa Đơn");
        styleButton(btnAddInvoice, infoColor, Color.WHITE);
        filterPanel.add(btnAddInvoice);

        topPanel.add(filterPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        cbLoai.addActionListener(e -> {
            Loai selectedLoai = (Loai) cbLoai.getSelectedItem();
            if (selectedLoai != null) {
                String maLoai = selectedLoai.getMaloai();

                String title;
                if (maLoai != null && !maLoai.isEmpty()) {
                    title = "Danh sách Sản phẩm theo loại: " + selectedLoai.getTenloai();
                } else {
                    title = "Danh sách Sản phẩm";
                }
                productScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1),
                          title, TitledBorder.LEADING, TitledBorder.TOP,
                          new Font("Arial", Font.BOLD, 14), primaryColor));

                displayProducts(null);
            }
        });

        btnFilter.addActionListener(e -> {
            cbLoai.setSelectedItem(cbLoai.getSelectedItem());
        });

        btnAddInvoice.addActionListener(e -> openHoaDonBanCreationDialog());
        topPanel.add(filterPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);



        // --- Center Content Panel (Stats and Products List) ---
        JPanel centerContentPanel = new JPanel();
        centerContentPanel.setLayout(new BoxLayout(centerContentPanel, BoxLayout.Y_AXIS));
        centerContentPanel.setOpaque(false);

        // --- Quick Stats Section (Mini Dashboard) ---
        JPanel statsGridPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        statsGridPanel.setOpaque(false);



        // --- Create and add Stat Cards ---
        JPanel totalSalesCard = createStatCard("Doanh thu hôm nay", "0 VNĐ", accentColor);
        lblTotalSalesTodayValue = (JLabel)((JPanel)totalSalesCard.getComponent(1)).getComponent(0);
        statsGridPanel.add(totalSalesCard);

        JPanel dailySalesCountCard = createStatCard("Hóa đơn bán hôm nay", "0", infoColor);
        lblDailySalesCountValue = (JLabel)((JPanel)dailySalesCountCard.getComponent(1)).getComponent(0);
        statsGridPanel.add(dailySalesCountCard);

        JPanel lowStockCard = createStatCard("Sản phẩm tồn kho thấp", "0", warningColor);
        lblLowStockValue = (JLabel)((JPanel)lowStockCard.getComponent(1)).getComponent(0);
        statsGridPanel.add(lowStockCard);

         lowStockCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
         lowStockCard.addMouseListener(new MouseAdapter() {
              @Override
              public void mouseClicked(MouseEvent e) {
                  System.out.println("Clicked on Low Stock Card");
                  List<SanPham> lowStockProducts = sanPhamDAO.getLowStockProducts(10);

                  productScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(primaryColor, 1),
                          "Danh sách Sản phẩm tồn kho thấp", TitledBorder.LEADING, TitledBorder.TOP,
                          new Font("Arial", Font.BOLD, 14), primaryColor));

                  cbLoai.setSelectedIndex(0);
                  displayProducts(lowStockProducts);
              }

              @Override
              public void mouseEntered(MouseEvent e) {
                  lowStockCard.setBorder(BorderFactory.createLineBorder(warningColor.darker(), 3));
              }

              @Override
              public void mouseExited(MouseEvent e) {
                  lowStockCard.setBorder(BorderFactory.createCompoundBorder(
                      BorderFactory.createLineBorder(warningColor, 2),
                      BorderFactory.createEmptyBorder(10, 10, 10, 10)
                  ));
              }
         });
        

        JPanel newCustomersCard = createStatCard("Khách hàng mới hôm nay", "0", infoColor);
        lblNewCustomersTodayValue = (JLabel)((JPanel)newCustomersCard.getComponent(1)).getComponent(0);
        statsGridPanel.add(newCustomersCard);

        // Wrap the stats grid in a panel for TitledBorder and padding
        statsWrapperPanel = new JPanel(new BorderLayout());
        statsWrapperPanel.setOpaque(false);
        statsWrapperPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(primaryColor, 1), "Thống kê nhanh", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), primaryColor));
        statsWrapperPanel.add(statsGridPanel, BorderLayout.CENTER);
        statsWrapperPanel.add(Box.createRigidArea(new Dimension(0, 15)), BorderLayout.SOUTH);

        centerContentPanel.add(statsWrapperPanel);
        centerContentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

JPanel productHeaderPanel = new JPanel(new BorderLayout());
productHeaderPanel.setOpaque(false);

// Nút "Xem tất cả" ở góc phải
JButton btnViewAll = new JButton("Xem tất cả");
styleButton(btnViewAll, accentBlue, Color.WHITE);
btnViewAll.setToolTipText("Hiển thị tất cả sản phẩm không phân loại");
btnViewAll.addActionListener(e -> {
    // Đặt lại bộ lọc loại về "Tất cả"
    cbLoai.setSelectedIndex(0);
    
    // Cập nhật tiêu đề của khung hiển thị
    productScrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(primaryColor, 1),
            "Danh sách Tất cả Sản phẩm", 
            TitledBorder.LEADING, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14), primaryColor));
    
    // Hiển thị tất cả sản phẩm
    displayProducts(null);
    
    // Thêm hiệu ứng highlight ngắn cho nút khi được nhấn
    btnViewAll.setBackground(accentBlue.darker().darker());
    
    // Timer để khôi phục màu sau 150ms
    Timer timer = new Timer(150, event -> {
        btnViewAll.setBackground(accentBlue);
        ((Timer)event.getSource()).stop();
    });
    timer.setRepeats(false);
    timer.start();
    
    // Log hành động (tùy chọn, cho debugging)
    System.out.println("Đã nhấn nút Xem tất cả - Hiển thị tất cả sản phẩm");
});

// Nút "Xem dạng danh sách" để mở cửa sổ SanPhamAll mới
JButton btnViewInWindow = new JButton("Xem dạng danh sách");
styleButton(btnViewInWindow, accentBlue, Color.WHITE);
btnViewInWindow.setToolTipText("Mở cửa sổ xem danh sách sản phẩm theo dạng phân loại");
btnViewInWindow.addActionListener(e -> {
    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Đổi cursor thành waiting
    
    SwingUtilities.invokeLater(() -> {
        try {
            Loai selectedLoai = (Loai) cbLoai.getSelectedItem();
            SanPhamAll sanPhamAllFrame = null;
            
            if (selectedLoai != null && selectedLoai.getMaloai() != null && !selectedLoai.getMaloai().isEmpty()) {
                // Nếu đã chọn một loại cụ thể, truyền mã loại đó để lọc
                sanPhamAllFrame = new SanPhamAll(loggedInUser, selectedLoai.getMaloai());
                System.out.println("Mở cửa sổ SanPhamAll với bộ lọc loại: " + selectedLoai.getTenloai());
            } else {
                // Nếu không, chỉ hiển thị tất cả sản phẩm
                sanPhamAllFrame = new SanPhamAll(loggedInUser);
                System.out.println("Mở cửa sổ SanPhamAll hiển thị tất cả sản phẩm");
            }
            
            // Đặt vị trí cửa sổ mới ở giữa màn hình
            sanPhamAllFrame.setLocationRelativeTo(HomePanel.this);
            
            // Hiển thị cửa sổ
            sanPhamAllFrame.setVisible(true);
            
            // Đặt lại cursor
            setCursor(Cursor.getDefaultCursor());
        } catch (Exception ex) {
            setCursor(Cursor.getDefaultCursor()); // Đảm bảo cursor trở lại bình thường ngay cả khi có lỗi
            ex.printStackTrace();
            JOptionPane.showMessageDialog(HomePanel.this, 
                "Không thể mở cửa sổ danh sách sản phẩm: " + ex.getMessage(), 
                "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    });
});

// Tạo panel chứa các nút với khoảng cách hợp lý
JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
buttonsPanel.setOpaque(false);
buttonsPanel.add(btnViewAll);
buttonsPanel.add(btnViewInWindow);

// Thêm các nút vào header
productHeaderPanel.add(buttonsPanel, BorderLayout.EAST);

// Panel chứa sản phẩm
productContainerPanel = new JPanel();
productContainerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
productContainerPanel.setOpaque(false);

// Tạo panel chính chứa header và container sản phẩm
JPanel productMainPanel = new JPanel(new BorderLayout(0, 10)); // Thêm khoảng cách giữa header và nội dung
productMainPanel.setOpaque(false);
productMainPanel.add(productHeaderPanel, BorderLayout.NORTH);
productMainPanel.add(productContainerPanel, BorderLayout.CENTER);

// Thêm vào scrollPane
productScrollPane = new JScrollPane(productMainPanel);
productScrollPane.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(primaryColor, 1), 
        "Danh sách Sản phẩm", 
        TitledBorder.LEADING, TitledBorder.TOP, 
        new Font("Arial", Font.BOLD, 14), primaryColor));
productScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
productScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
productScrollPane.getVerticalScrollBar().setUnitIncrement(16);
productScrollPane.setOpaque(false);
productScrollPane.getViewport().setOpaque(false);

centerContentPanel.add(productScrollPane);
centerContentPanel.add(Box.createVerticalGlue());

add(centerContentPanel, BorderLayout.CENTER);
        // --- Bottom Section (Dự đoán ngày hôm sau) ---
            chartsPlaceholderPanel = new JPanel(new BorderLayout());
            chartsPlaceholderPanel.setOpaque(false);
            chartsPlaceholderPanel.setPreferredSize(new Dimension(this.getWidth(), 200));
            chartsPlaceholderPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(primaryColor, 1), 
                "Dự đoán ngày hôm sau", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), primaryColor));

            JLabel lblChartMessage = new JLabel("<html><center>Đặt mã nguồn biểu đồ tại đây.</center><br><center>Cần sử dụng thư viện biểu đồ (ví dụ: JFreeChart).</center></html>", SwingConstants.CENTER);
            lblChartMessage.setForeground(mutedTextColor);
            lblChartMessage.setFont(new Font("Arial", Font.PLAIN, 12));
            chartsPlaceholderPanel.add(lblChartMessage, BorderLayout.CENTER);

            add(chartsPlaceholderPanel, BorderLayout.SOUTH);

        // --- Event Listeners ---
        // TODO: Add action listeners for Quick Access Buttons if applicable

        // --- Load initial data ---
        loadCategories();
        displayProducts(null); 
        loadStatisticalData();
        loadSalesChart();
        applyRolePermissions();
    }

    // Helper method to load categories into the combobox
    private void loadCategories() {
        cbLoai.removeAllItems();
        cbLoai.addItem(new Loai("", "-- Tất cả loại --"));

        List<Loai> loaiList = loaiDAO.getAllLoai();
        if (loaiList != null) {
            for (Loai loai : loaiList) {
                cbLoai.addItem(loai);
            }
        } else {
            System.out.println("Không lấy được danh sách loại sản phẩm từ CSDL.");
        }
    }

    public void displayProducts(List<SanPham> danhSach) {
        productContainerPanel.removeAll();

        List<SanPham> productsToDisplay = danhSach;

        if (productsToDisplay == null) {
            Loai selectedLoai = (Loai) cbLoai.getSelectedItem();
            if (selectedLoai != null && selectedLoai.getMaloai() != null && !selectedLoai.getMaloai().isEmpty()) {
                productsToDisplay = sanPhamDAO.getSanPhamByLoai(selectedLoai.getMaloai());
            } else {
                productsToDisplay = sanPhamDAO.getAllSanPham();
            }
        }

        if (productsToDisplay != null && !productsToDisplay.isEmpty()) {
            for (SanPham sp : productsToDisplay) {
                // Use the external ProductBoxPanel class
                ProductBoxPanel productBox = new ProductBoxPanel(sp);
                productContainerPanel.add(productBox);
            }
        } else {
            JLabel noProductLabel = new JLabel("Không tìm thấy sản phẩm nào.");
            noProductLabel.setForeground(textColor);
            noProductLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            productContainerPanel.add(noProductLabel);
        }

        productContainerPanel.revalidate();
        productContainerPanel.repaint();
        productScrollPane.revalidate();
        productScrollPane.repaint();
    }

    // Helper method to create a simple stat card
    private JPanel createStatCard(String title, String value, Color headerColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(new CompoundBorder(
                new LineBorder(headerColor, 2),
                new EmptyBorder(10, 10, 10, 10)
        ));
        card.setBackground(Color.WHITE);

        JPanel cardHeader = new JPanel();
        cardHeader.setBackground(headerColor);
        cardHeader.setLayout(new BoxLayout(cardHeader, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 13));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardHeader.add(Box.createVerticalGlue());
        cardHeader.add(lblTitle);
        cardHeader.add(Box.createVerticalGlue());
        card.add(cardHeader, BorderLayout.NORTH);

        JPanel cardContent = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cardContent.setBackground(Color.WHITE);
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setForeground(textColor);
        lblValue.setFont(new Font("Arial", Font.BOLD, 20));
        cardContent.add(lblValue);
        card.add(cardContent, BorderLayout.CENTER);

        return card;
    }

    private void openSanPhamAllForm() {
        SanPhamAll sanPhamAllFrame = new SanPhamAll(); // Cần đảm bảo SanPhamAll có constructor mặc định hoặc phù hợp
        sanPhamAllFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Đóng cửa sổ này mà không thoát ứng dụng chính
        sanPhamAllFrame.setSize(800, 600);
        sanPhamAllFrame.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this)); // Hiển thị ở giữa cửa sổ cha
        sanPhamAllFrame.setVisible(true);
    }


    private void openHoaDonBanCreationDialog(){
        HoaDonBanCreationDialog dialog = new HoaDonBanCreationDialog((JFrame) SwingUtilities.getWindowAncestor(this), loggedInUser, sanphamUI);
        dialog.setVisible(true);

        if(dialog.isSavedSuccessfully()){
            loadStatisticalData();
            loadSalesChart();
        }
    }

    // Method to load statistical data from DAOs and update the display
    private void loadStatisticalData() {
        System.out.println("Đang tải dữ liệu thống kê...");

        DecimalFormat currencyFormatter = new DecimalFormat("#,### VNĐ");

        try {
            double totalSalesToday = hoaDonBanDAO.getTotalSalesAmountForToday();
            lblTotalSalesTodayValue.setText(currencyFormatter.format(totalSalesToday));
            lblTotalSalesTodayValue.setForeground(accentColor.darker());
        } catch (Exception e) {
            System.err.println("Lỗi khi tải dữ liệu Doanh thu hôm nay: " + e.getMessage());
            lblTotalSalesTodayValue.setText("Lỗi");
            lblTotalSalesTodayValue.setForeground(Color.RED);
        }

        try {
            int dailySalesCount = hoaDonBanDAO.getSalesCountForToday();
            lblDailySalesCountValue.setText(String.valueOf(dailySalesCount));
            lblDailySalesCountValue.setForeground(infoColor.darker());
        } catch (Exception e) {
            System.err.println("Lỗi khi tải dữ liệu Hóa đơn bán hôm nay: " + e.getMessage());
            lblDailySalesCountValue.setText("Lỗi");
            lblDailySalesCountValue.setForeground(Color.RED);
        }

        try {
            int lowStockCount = sanPhamDAO.getLowStockCount(10);
            lblLowStockValue.setText(String.valueOf(lowStockCount));
            lblLowStockValue.setForeground(lowStockCount > 0 ? warningColor.darker() : textColor);
        } catch (Exception e) {
            System.err.println("Lỗi khi tải dữ liệu Sản phẩm tồn kho thấp: " + e.getMessage());
            lblLowStockValue.setText("Lỗi");
            lblLowStockValue.setForeground(Color.RED);
        }

        try {
            int newCustomersToday = khachHangDAO.getNewCustomersCountForToday();
             if (newCustomersToday == -1) {
                 lblNewCustomersTodayValue.setText("Lỗi");
                 lblNewCustomersTodayValue.setForeground(Color.RED);
             } else {
                lblNewCustomersTodayValue.setText(String.valueOf(newCustomersToday));
                lblNewCustomersTodayValue.setForeground(infoColor.darker());
             }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải dữ liệu Khách hàng mới hôm nay: " + e.getMessage());
            lblNewCustomersTodayValue.setText("Lỗi");
            lblNewCustomersTodayValue.setForeground(Color.RED);
        }
    }

    // TODO: Add method to integrate actual charts

    private void loadSalesChart() {
        try {
            // Clear existing content in the placeholder panel
            chartsPlaceholderPanel.removeAll();

            // Create dataset for sales data
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();

            // Connect to SQL Server and query total sales per month
            try {
                // Load SQL Server JDBC driver
                // Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                
                // // Connection string for SQL Server (replace with your details)
                // String connectionUrl = "jdbc:sqlserver://localhost:1433;databaseName=your_db;user=your_user;password=your_password;encrypt=true;trustServerCertificate=true;";
                
                try (Connection conn = DatabaseConnection.getConnection();
                    Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(
                        "SELECT FORMAT(Ngayban, 'yyyy-MM-dd') AS Date, SUM(Tongtien) AS TotalSales " +
                        "FROM HoaDonBan " +
                        "GROUP BY FORMAT(Ngayban, 'yyyy-MM-dd') " +
                        "ORDER BY Date"
                    )) {
                    while (rs.next()) {
                        String Date = rs.getString("Date"); // Format: YYYY-MM
                        double totalSales = rs.getDouble("TotalSales");
                        dataset.addValue(totalSales, "Doanh thu", Date);
                    }
                }
            } catch (SQLException e) {
                // Display error message if query fails
                JLabel errorLabel = new JLabel("Lỗi tải dữ liệu: " + e.getMessage(), SwingConstants.CENTER);
                errorLabel.setForeground(mutedTextColor);
                errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                chartsPlaceholderPanel.add(errorLabel, BorderLayout.CENTER);
                chartsPlaceholderPanel.revalidate();
                chartsPlaceholderPanel.repaint();
                e.printStackTrace();
                return;
            }

            // Create bar chart
            JFreeChart chart = ChartFactory.createBarChart(
                "Báo cáo Doanh thu Theo Ngày", // Chart title
                "Ngày",                       // X-axis label
                "Doanh thu (VND)",             // Y-axis label
                dataset,                       // Data
                PlotOrientation.VERTICAL,
                true,                          // Include legend
                true,                          // Tooltips
                false                          // URLs
            );

            // Customize chart to match UI theme
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            chart.getTitle().setFont(new Font("Arial", Font.BOLD, 14));
            chart.getTitle().setPaint(primaryColor);
            chart.getCategoryPlot().getDomainAxis().setLabelFont(new Font("Arial", Font.PLAIN, 12));
            chart.getCategoryPlot().getRangeAxis().setLabelFont(new Font("Arial", Font.PLAIN, 12));

            // Create ChartPanel and set size
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new Dimension(chartsPlaceholderPanel.getWidth(), 200));
            chartPanel.setOpaque(false);

            // Add chart to the placeholder panel
            chartsPlaceholderPanel.add(chartPanel, BorderLayout.CENTER);

            // Refresh the panel
            chartsPlaceholderPanel.revalidate();
            chartsPlaceholderPanel.repaint();
        } catch (Exception e) {
            // Display error message if chart fails to load
            JLabel errorLabel = new JLabel("Lỗi hiển thị biểu đồ: " + e.getMessage(), SwingConstants.CENTER);
            errorLabel.setForeground(mutedTextColor);
            errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            chartsPlaceholderPanel.add(errorLabel, BorderLayout.CENTER);
            chartsPlaceholderPanel.revalidate();
            chartsPlaceholderPanel.repaint();
            e.printStackTrace();
        }
    }

    private void applyRolePermissions() {
         String role = (loggedInUser != null) ? loggedInUser.getRole() : "";

        boolean canViewStatsAndCharts = "Manager".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role);

        statsWrapperPanel.setVisible(canViewStatsAndCharts);
        chartsPlaceholderPanel.setVisible(canViewStatsAndCharts);
        // btnAddInvoice.setVisible(canViewStatsAndCharts);
        revalidate();
        repaint();
        this.revalidate();
        this.repaint();
    }

    // Helper method to create styled labels
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(textColor);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        return label;
    }

    // Helper method to style buttons
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        button.setOpaque(true);
        button.setBorderPainted(true);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Thêm hiệu ứng hover
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
    }

    // Helper method to convert Color to Hex string for HTML
    private String toHex(Color color) {
        return String.format("#%06x", color.getRGB() & 0xFFFFFF);
    }

    // REMOVED: Nested ProductBoxPanel class definition moved to its own file
}
