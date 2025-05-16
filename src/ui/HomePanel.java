package ui;

// Import các DAO cần thiết
import dao.HoaDonBanDAO;
import dao.HoaDonNhapDAO;
import dao.KhachHangDAO;
import dao.LoaiDAO;
import dao.SanPhamDAO;
import java.awt.*;
import java.awt.event.MouseAdapter; // For ProductBoxPanel mouse listener
import java.awt.event.MouseEvent; // For ProductBoxPanel mouse listener
import java.text.DecimalFormat; // For currency formatting
import java.util.List;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder; // <-- Thêm import này
import javax.swing.border.TitledBorder; // <-- Thêm import này
import model.Loai; // Import NhanVien model instead
import model.NhanVien; // Ensure this class exists with getMaloai() and toString()
import model.SanPham; // Ensure this class exists with getTenSP(), getGiaban(), getSoluong()


public class HomePanel extends JPanel {

    // Define colors (reusing the palette)
    Color coffeeBrown = new Color(102, 51, 0); // Màu nâu cà phê
    Color lightBeige = new Color(245, 245, 220); // Màu be nhạt (cho nền)
    Color darkGray = new Color(50, 50, 50); // Màu xám đậm (cho văn bản, viền)
    Color accentGreen = new Color(60, 179, 113); // Màu xanh lá (ví dụ: Doanh thu)
    Color accentOrange = new Color(255, 165, 0); // Màu cam (ví dụ: Hết hàng)
    Color accentBlue = new Color(30, 144, 255); // Màu xanh dương (ví dụ: Hóa đơn bán, nút Lọc)
    Color linkColor = new Color(0, 102, 204); // Màu xanh cho liên kết

    // UI Components (Welcome)
    private JLabel lblWelcomeMessage;

    // UI Components (Product Display and Filtering)
    private JComboBox<Loai> cbLoai; // ComboBox to select product category
    private JButton btnFilter; // Button to trigger filtering
    private JPanel productContainerPanel; // Panel to hold ProductBoxPanels
    private JScrollPane productScrollPane; // Scroll pane for the product container

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

    // Thông tin nhân viên đã đăng nhập - Change from ACC to NhanVien
    private NhanVien loggedInUser; // Changed from loggedInAccount (ACC)

    // Constructor nhận thông tin nhân viên đã đăng nhập - Change parameter type
    public HomePanel(NhanVien user) { // Accept NhanVien object
        this.loggedInUser = user; // Lưu thông tin nhân viên (Changed from loggedInAccount = account)

        // Initialize DAOs
        sanPhamDAO = new SanPhamDAO();
        loaiDAO = new LoaiDAO();
        hoaDonBanDAO = new HoaDonBanDAO();
        hoaDonNhapDAO = new HoaDonNhapDAO();
        khachHangDAO = new KhachHangDAO();

        // Set layout for the panel
        setLayout(new BorderLayout(10, 10)); // Use BorderLayout main
        setBackground(lightBeige);
        setBorder(new EmptyBorder(20, 20, 20, 20)); // Padding

        // --- Top Panel (Welcome and Filtering Controls) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(lightBeige);

        // Left side: Welcome message
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        welcomePanel.setBackground(lightBeige);

        // Get display name and role from the logged-in NhanVien object
        String displayName = loggedInUser.getTenNV(); // Use TenNV as display name
        if (displayName == null || displayName.isEmpty()) {
            displayName = loggedInUser.getTendangnhap(); // Fallback to username
        }
        String role = loggedInUser.getRole();

        lblWelcomeMessage = new JLabel("<html><h2>Chào mừng, " + displayName + "!</h2><p>Vai trò của bạn: <b>" + role + "</b></p></html>");
        lblWelcomeMessage.setFont(new Font("Arial", Font.PLAIN, 16));
        lblWelcomeMessage.setForeground(darkGray);
        welcomePanel.add(lblWelcomeMessage);

        topPanel.add(welcomePanel, BorderLayout.WEST);

        // Right side: Filtering controls
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(lightBeige);
        filterPanel.add(createLabel("Lọc sản phẩm theo loại:")); // Helper method

        cbLoai = new JComboBox<>();
        cbLoai.setBackground(Color.WHITE);
        cbLoai.setForeground(darkGray);
        filterPanel.add(cbLoai);

        btnFilter = new JButton("Lọc");
        styleButton(btnFilter, accentBlue, Color.WHITE); // Style the filter button
        filterPanel.add(btnFilter);

        // ************ BẮT ĐẦU THÊM ACTION LISTENER VÀO cbLoai ************
         cbLoai.addActionListener(e -> {
             // Lắng nghe sự kiện khi mục được chọn trong ComboBox thay đổi
             Loai selectedLoai = (Loai) cbLoai.getSelectedItem();
             if (selectedLoai != null) {
                 String maLoai = selectedLoai.getMaloai();

                 // Cập nhật tiêu đề của ScrollPane dựa trên lựa chọn loại
                 if (maLoai != null && !maLoai.isEmpty()) {
                     productScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1),
                             "Danh sách Sản phẩm theo loại: " + selectedLoai.getTenloai(), TitledBorder.LEADING, TitledBorder.TOP,
                             new Font("Arial", Font.BOLD, 14), coffeeBrown));
                 } else {
                     // Trường hợp "Tất cả loại" được chọn (Mã loại rỗng)
                     productScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1),
                             "Danh sách Sản phẩm", TitledBorder.LEADING, TitledBorder.TOP,
                             new Font("Arial", Font.BOLD, 14), coffeeBrown));
                 }

                 // Hiển thị danh sách sản phẩm dựa trên loại đã chọn
                 // Gọi displayProducts với null để nó tự load theo cbLoai
                 displayProducts(null);

                 // Tùy chọn: Đặt lại tiêu đề welcome message nếu bạn đã thay đổi nó
                 // trong các listener của thẻ thống kê
                 // String displayNama = loggedInUser.getTenNV(); // Corrected to loggedInUser
                 // if (displayNama == null || displayNama.isEmpty()) {
                 //     displayNama = loggedInUser.getTendangnhap(); // Corrected to loggedInUser
                 // }
                 // lblWelcomeMessage.setText("<html><h2>Chào mừng, " + displayNama + "!</h2><p>Vai trò của bạn: <b>" + loggedInUser.getRole() + "</b></p></html>"); // Corrected to loggedInUser

             }
         });

        // ************ KẾT THÚC THÊM ACTION LISTENER VÀO cbLoai ************


        // Nếu bạn giữ nút Lọc, làm cho nó gọi logic tương tự như ComboBox Listener
          btnFilter.addActionListener(e -> {
              // Lấy lựa chọn hiện tại và kích hoạt lại logic lọc
              cbLoai.setSelectedItem(cbLoai.getSelectedItem()); // Kích hoạt lại listener của ComboBox
          });


        topPanel.add(filterPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // --- Center Content Panel (Stats and Products List) ---
        JPanel centerContentPanel = new JPanel();
        centerContentPanel.setLayout(new BoxLayout(centerContentPanel, BoxLayout.Y_AXIS));
        centerContentPanel.setBackground(lightBeige);

        // --- Quick Stats Section (Mini Dashboard) ---
        JPanel statsGridPanel = new JPanel(new GridLayout(1, 4, 15, 15)); // 1 row, 4 columns, with gaps
        statsGridPanel.setBackground(lightBeige);
        // statsGridPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100)); // Consider if this constraint is needed

        // --- Create and add Stat Cards ---
        // Note: Accessing value labels via getComponent is fragile; direct references are better.
        // We keep this pattern for consistency with the original code structure.
        JPanel totalSalesCard = createStatCard("Doanh thu hôm nay", "0 VNĐ", accentGreen);
        lblTotalSalesTodayValue = (JLabel)((JPanel)totalSalesCard.getComponent(1)).getComponent(0);
        statsGridPanel.add(totalSalesCard);

        JPanel dailySalesCountCard = createStatCard("Hóa đơn bán hôm nay", "0", accentBlue);
        lblDailySalesCountValue = (JLabel)((JPanel)dailySalesCountCard.getComponent(1)).getComponent(0);
        statsGridPanel.add(dailySalesCountCard);

        JPanel lowStockCard = createStatCard("Sản phẩm tồn kho thấp", "0", accentOrange); // Ban đầu là 0
        lblLowStockValue = (JLabel)((JPanel)lowStockCard.getComponent(1)).getComponent(0); // Giả định cấu trúc
        statsGridPanel.add(lowStockCard);

        // Add MouseListener to LowStockCard
         lowStockCard.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Change cursor on hover
         lowStockCard.addMouseListener(new MouseAdapter() {
              @Override
              public void mouseClicked(MouseEvent e) {
                  System.out.println("Clicked on Low Stock Card");
                  // Get low stock products from DAO
                  List<SanPham> lowStockProducts = sanPhamDAO.getLowStockProducts(10); // Use your low stock threshold (e.g., 10)

                  // Update product list title
                  productScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1),
                          "Danh sách Sản phẩm tồn kho thấp", TitledBorder.LEADING, TitledBorder.TOP,
                          new Font("Arial", Font.BOLD, 14), coffeeBrown));

                  // Reset category combobox to "All" or default
                  cbLoai.setSelectedIndex(0); // Assumes "All Categories" is the first item

                  // Display the low stock products
                  displayProducts(lowStockProducts);

                  // Optional: Update welcome message or another label
                  // lblWelcomeMessage.setText("<html><h2>Đang xem: Sản phẩm tồn kho thấp</h2></html>");
              }

              // Optional: Add hover effect
               // private Color originalBorderColor = accentOrange; // This is captured when the card is created
               @Override
               public void mouseEntered(MouseEvent e) {
                    // originalBorderColor = ((LineBorder)((TitledBorder) lowStockCard.getBorder()).getBorder()).getLineColor(); // Re-capture if needed
                    lowStockCard.setBorder(BorderFactory.createLineBorder(accentOrange.darker(), 3)); // Darker border on hover
               }

               @Override
               public void mouseExited(MouseEvent e) {
                   // Need to restore the *original* border, not just a LineBorder
                    // This requires storing the border object itself or recreating it.
                    // Simple approach: Revert to the standard titled border with original color and thickness 2
                    lowStockCard.setBorder(BorderFactory.createLineBorder(accentOrange, 2));
               }
         });


        JPanel newCustomersCard = createStatCard("Khách hàng mới hôm nay", "0", coffeeBrown);
        lblNewCustomersTodayValue = (JLabel)((JPanel)newCustomersCard.getComponent(1)).getComponent(0);
        statsGridPanel.add(newCustomersCard);

        // Wrap the stats grid in a panel for TitledBorder and padding
        statsWrapperPanel = new JPanel(new BorderLayout()); // Instance variable
        statsWrapperPanel.setBackground(lightBeige);
        statsWrapperPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(darkGray, 1), "Thống kê nhanh", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), darkGray));
        statsWrapperPanel.add(statsGridPanel, BorderLayout.CENTER);
        statsWrapperPanel.add(Box.createRigidArea(new Dimension(0, 10)), BorderLayout.SOUTH); // Add spacing below grid

        centerContentPanel.add(statsWrapperPanel);
        centerContentPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing between stats and product list

        // --- Product List Display ---
        productContainerPanel = new JPanel();
        productContainerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15)); // FlowLayout with gaps
        productContainerPanel.setBackground(lightBeige);

        productScrollPane = new JScrollPane(productContainerPanel);
        productScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Danh sách Sản phẩm", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), coffeeBrown));
        // Corrected typo here: SCALLBAR to SCROLLBAR
        productScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        productScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        productScrollPane.getVerticalScrollBar().setUnitIncrement(16); // Faster scrolling

        centerContentPanel.add(productScrollPane);
        centerContentPanel.add(Box.createVerticalGlue()); // Push components up if using BoxLayout

        add(centerContentPanel, BorderLayout.CENTER);

        // --- Bottom Section (Charts - Placeholder) ---
        chartsPlaceholderPanel = new JPanel(new BorderLayout()); // Instance variable
        chartsPlaceholderPanel.setBackground(lightBeige);
        chartsPlaceholderPanel.setPreferredSize(new Dimension(this.getWidth(), 200));
        chartsPlaceholderPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(darkGray, 1), "Biểu đồ Doanh thu (Placeholder)", TitledBorder.LEADING, TitledBorder.TOP, new Font("Arial", Font.BOLD, 14), darkGray));

        JLabel lblChartMessage = new JLabel("<html><center>Đặt mã nguồn biểu đồ tại đây.</center><br><center>Cần sử dụng thư viện biểu đồ (ví dụ: JFreeChart).</center></html>", SwingConstants.CENTER);
        lblChartMessage.setForeground(darkGray);
        lblChartMessage.setFont(new Font("Arial", Font.PLAIN, 12));
        chartsPlaceholderPanel.add(lblChartMessage, BorderLayout.CENTER);

        add(chartsPlaceholderPanel, BorderLayout.SOUTH);

        // --- Event Listeners ---
        // Filter button listener is now handled by ComboBox listener
        // btnFilter.addActionListener(e -> filterProductsByCategory()); // Removed redundant listener

        // TODO: Add action listeners for Quick Access Buttons if applicable

        // --- Load initial data ---
        // Consider using SwingWorker for DAO calls to keep the UI responsive.
        loadCategories();
        displayProducts(null); // Load all products initially
        loadStatisticalData();

        // Apply permissions based on the logged-in user's role
        applyRolePermissions();
    }

    // Helper method to load categories into the combobox
    private void loadCategories() {
        cbLoai.removeAllItems(); // Clear existing items
        // Add "All Categories" option
        cbLoai.addItem(new Loai("", "-- Tất cả loại --"));

        // Assuming getAllLoai() exists and returns List<Loai>
        List<Loai> loaiList = loaiDAO.getAllLoai();
        if (loaiList != null) {
            for (Loai loai : loaiList) {
                cbLoai.addItem(loai); // Assumes Loai's toString() is appropriate
            }
        } else {
            System.out.println("Không lấy được danh sách loại sản phẩm từ CSDL.");
            // Optional: Add sample categories if DAO fails
        }
    }

    // Method to display a list of products in the container panel
    // Pass a List<SanPham> to display, or pass null to load from DAO based on filter
    public void displayProducts(List<SanPham> danhSach) {
        productContainerPanel.removeAll(); // Clear existing product boxes

        List<SanPham> productsToDisplay = danhSach;

        // If the list is null, fetch from DAO based on the current filter
        if (productsToDisplay == null) {
            Loai selectedLoai = (Loai) cbLoai.getSelectedItem();
            if (selectedLoai != null && selectedLoai.getMaloai() != null && !selectedLoai.getMaloai().isEmpty()) {
                // Assuming getSanPhamByLoai(String maLoai) exists
                productsToDisplay = sanPhamDAO.getSanPhamByLoai(selectedLoai.getMaloai());
            } else {
                // Assuming getAllSanPham() exists
                productsToDisplay = sanPhamDAO.getAllSanPham();
            }
        }

        // Add ProductBoxPanels for each product
        if (productsToDisplay != null && !productsToDisplay.isEmpty()) {
            for (SanPham sp : productsToDisplay) {
                // Use the nested ProductBoxPanel class
                ProductBoxPanel productBox = new ProductBoxPanel(sp);
                productContainerPanel.add(productBox);
            }
            // Optional: Add glue for FlowLayout alignment - BoxLayout handles this better if used
            // productContainerPanel.add(Box.createHorizontalGlue());
        } else {
            // Display a message if no products are found
            JLabel noProductLabel = new JLabel("Không tìm thấy sản phẩm nào.");
            noProductLabel.setForeground(darkGray);
            noProductLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            productContainerPanel.add(noProductLabel);
        }

        // Revalidate and repaint to update the display
        productContainerPanel.revalidate();
        productContainerPanel.repaint();
        productScrollPane.revalidate(); // Revalidate the scroll pane too
        productScrollPane.repaint();
    }

    // Action method for the Filter button (now primarily handled by ComboBox listener)
    private void filterProductsByCategory() {
         // This method is now redundant if ComboBox listener directly calls displayProducts(null).
         // Keeping it here for now, but the ComboBox listener is more direct.
         // If btnFilter is the *only* way to filter after selecting, then use this.
         // Since ComboBox listener is more common, we'll let it handle the logic.
         // To use btnFilter after selection, you'd remove the listener from cbLoai
         // and only call displayProducts(null) here.
         // For now, let the ComboBox listener drive the update.
         // displayProducts(null); // Redundant if cbLoai listener does this
    }

    // Helper method to create a simple stat card
    private JPanel createStatCard(String title, String value, Color headerColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(headerColor, 2));
        card.setBackground(Color.WHITE);
         // Add padding inside the card
         card.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createLineBorder(headerColor, 2),
              BorderFactory.createEmptyBorder(10, 10, 10, 10) // Padding
         ));


        JPanel cardHeader = new JPanel();
        cardHeader.setBackground(headerColor);
        // Use BoxLayout for header to center label horizontally and vertically
        cardHeader.setLayout(new BoxLayout(cardHeader, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally
        cardHeader.add(Box.createVerticalGlue()); // Push label to center vertically
        cardHeader.add(lblTitle);
        cardHeader.add(Box.createVerticalGlue()); // Push label to center vertically
        card.add(cardHeader, BorderLayout.NORTH);

        JPanel cardContent = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cardContent.setBackground(Color.WHITE);
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setForeground(darkGray);
        lblValue.setFont(new Font("Arial", Font.BOLD, 24));
        cardContent.add(lblValue);
        card.add(cardContent, BorderLayout.CENTER);

        return card;
    }

    // Method to load statistical data from DAOs and update the display
    private void loadStatisticalData() {
        // Loading statistical data should ideally use SwingWorker for responsiveness.
        System.out.println("Đang tải dữ liệu thống kê...");

        DecimalFormat currencyFormatter = new DecimalFormat("#,### VNĐ");

        // --- Load and update Total Sales ---
        try {
            // Assuming getTotalSalesAmountForToday() exists in HoaDonBanDAO
            double totalSalesToday = hoaDonBanDAO.getTotalSalesAmountForToday();
            lblTotalSalesTodayValue.setText(currencyFormatter.format(totalSalesToday));
            lblTotalSalesTodayValue.setForeground(accentGreen);
        } catch (Exception e) {
            System.err.println("Lỗi khi tải dữ liệu Doanh thu hôm nay: " + e.getMessage());
            lblTotalSalesTodayValue.setText("Lỗi");
            lblTotalSalesTodayValue.setForeground(Color.RED);
        }

        // --- Load and update Daily Sales Count ---
        try {
            // Assuming getSalesCountForToday() exists in HoaDonBanDAO
            int dailySalesCount = hoaDonBanDAO.getSalesCountForToday();
            lblDailySalesCountValue.setText(String.valueOf(dailySalesCount));
            lblDailySalesCountValue.setForeground(accentBlue);
        } catch (Exception e) {
            System.err.println("Lỗi khi tải dữ liệu Hóa đơn bán hôm nay: " + e.getMessage());
            lblDailySalesCountValue.setText("Lỗi");
            lblDailySalesCountValue.setForeground(Color.RED);
        }

        // --- Load and update Low Stock Count ---
        try {
            // Assuming getLowStockCount(int threshold) exists in SanPhamDAO
            int lowStockCount = sanPhamDAO.getLowStockCount(10); // Example threshold
            lblLowStockValue.setText(String.valueOf(lowStockCount));
            lblLowStockValue.setForeground(lowStockCount > 0 ? accentOrange : darkGray); // Highlight if low stock
        } catch (Exception e) {
            System.err.println("Lỗi khi tải dữ liệu Sản phẩm tồn kho thấp: " + e.getMessage());
            lblLowStockValue.setText("Lỗi");
            lblLowStockValue.setForeground(Color.RED);
        }

        // --- Load and update New Customers Count ---
        try {
            // Assuming getNewCustomersCountForToday() exists in KhachHangDAO (as added)
            int newCustomersToday = khachHangDAO.getNewCustomersCountForToday();
             if (newCustomersToday == -1) { // Check for error return value (or use exception handling)
                 lblNewCustomersTodayValue.setText("Lỗi");
                 lblNewCustomersTodayValue.setForeground(Color.RED);
             } else {
                lblNewCustomersTodayValue.setText(String.valueOf(newCustomersToday));
                lblNewCustomersTodayValue.setForeground(coffeeBrown);
             }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải dữ liệu Khách hàng mới hôm nay: " + e.getMessage());
            lblNewCustomersTodayValue.setText("Lỗi");
            lblNewCustomersTodayValue.setForeground(Color.RED);
        }

        // TODO: Add calls for other statistics if needed (e.g., HoaDonNhap stats)

        // No need to revalidate/repaint statsWrapperPanel here as labels updating
        // doesn't change layout size unless content changes dramatically.
        // The initial revalidate/repaint in the constructor is sufficient for layout.
    }

    // TODO: Add method to integrate actual charts
    /*
     private void loadSalesChart() {
         // Need a charting library (e.g., JFreeChart)
         // Fetch data from DAO
         // Create dataset
         // Create chart
         // Create ChartPanel and add to chartsPlaceholderPanel
     }
     */

    // Method to apply role-based permissions for content visibility
    private void applyRolePermissions() {
         // Get role from the logged-in NhanVien object
         String role = (loggedInUser != null) ? loggedInUser.getRole() : ""; // Use loggedInUser

        // Example: Only "Manager" or "Admin" can view stats and charts
        boolean canViewStatsAndCharts = "Manager".equalsIgnoreCase(role) || "Admin".equalsIgnoreCase(role);

        // Control visibility of the stats and charts panels
        statsWrapperPanel.setVisible(canViewStatsAndCharts);
        chartsPlaceholderPanel.setVisible(canViewStatsAndCharts);

        // Example: All roles can view the product list and filter
        // If you needed to restrict filtering, you would hide filterPanel:
        // filterPanel.setVisible(...);

        // Revalidate and repaint to update the layout after changing visibility
        this.revalidate();
        this.repaint();
    }

    // Helper method to create styled labels
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        return label;
    }

    // Helper method to style buttons
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false); // Remove focus border
        button.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createLineBorder(fgColor, 1), // Outer border
                 BorderFactory.createEmptyBorder(5, 15, 5, 15))); // Inner padding
        button.setOpaque(true); // Make background visible
        button.setBorderPainted(true); // Ensure border is painted
        button.setFont(new Font("Arial", Font.BOLD, 12));
    }

    // Nested class for displaying individual product boxes
    // It's recommended to move this to its own ProductBoxPanel.java file.
    class ProductBoxPanel extends JPanel {
        private SanPham sanPham;
        private JLabel lblProductImage; // Placeholder for image
        private JLabel lblProductName;
        private JLabel lblProductPrice;
        private JLabel lblProductStock;

        public ProductBoxPanel(SanPham sp) {
            this.sanPham = sp;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Vertical layout
            setBorder(BorderFactory.createLineBorder(darkGray, 1)); // Border around the box
            setBackground(Color.WHITE); // White background
            setPreferredSize(new Dimension(150, 180)); // Fixed size for consistency
            setAlignmentX(Component.CENTER_ALIGNMENT); // Center in BoxLayout


            // Load and display product image (replace with your actual image loading logic)
             // Example loading from a fixed path + product code
             String imagePath = "D:\\KyIV_HocVienNganHang\\WebDesign\\BTL_web\\assets\\banner\\icon-fs.png"; // Assuming a base path

             // Construct full path assuming image name is like 'product_MA.png'
             // Replace with your actual image file naming convention
             String fullImagePath = imagePath; // Placeholder, needs actual logic to append filename based on sp.getMaSP()

             try {
                 // Attempt to load the image from the constructed path
                 ImageIcon originalIcon = new ImageIcon(fullImagePath);
                 if (originalIcon.getImageLoadStatus() == MediaTracker.COMPLETE) { // Check if image loaded successfully
                     Image img = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                     lblProductImage = new JLabel(new ImageIcon(img));
                     lblProductImage.setAlignmentX(Component.CENTER_ALIGNMENT);
                     add(lblProductImage);
                     add(Box.createRigidArea(new Dimension(0, 5))); // Spacing
                 } else {
                     // Handle case where image file does not exist or is invalid
                     JLabel noImageLabel = new JLabel("Không có ảnh", SwingConstants.CENTER); // Changed text
                     noImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                     noImageLabel.setForeground(darkGray);
                     noImageLabel.setFont(new Font("Arial", Font.ITALIC, 10)); // Smaller font
                     add(noImageLabel);
                     add(Box.createRigidArea(new Dimension(0, 5)));
                 }
             } catch (Exception e) { // Catch any exceptions during image loading
                 System.err.println("Error loading image for product " + sp.getMaSP() + ": " + e.getMessage());
                 // Add a placeholder label or image
                 JLabel noImageLabel = new JLabel("Lỗi tải ảnh", SwingConstants.CENTER); // Changed text
                 noImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                 noImageLabel.setForeground(Color.RED); // Red for error
                 noImageLabel.setFont(new Font("Arial", Font.ITALIC, 10));
                 add(noImageLabel);
                 add(Box.createRigidArea(new Dimension(0, 5)));
             }
             // Add some vertical space at the top if no image was added yet (to keep spacing consistent)
             if (lblProductImage == null) {
                 add(Box.createRigidArea(new Dimension(0, 10))); // Add space if image loading failed
             }


            lblProductName = new JLabel(sp.getTenSP(), SwingConstants.CENTER);
            lblProductName.setFont(new Font("Arial", Font.BOLD, 14));
            lblProductName.setAlignmentX(Component.CENTER_ALIGNMENT); // Center horizontally

            DecimalFormat currencyFormatter = new DecimalFormat("#,### VNĐ");
            lblProductPrice = new JLabel(currencyFormatter.format(sp.getGiaban()), SwingConstants.CENTER);
            lblProductPrice.setFont(new Font("Arial", Font.PLAIN, 12));
            lblProductPrice.setForeground(accentGreen); // Green for price
            lblProductPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

            lblProductStock = new JLabel("Còn lại: " + sp.getSoluong(), SwingConstants.CENTER);
            lblProductStock.setFont(new Font("Arial", Font.PLAIN, 12));
            lblProductStock.setForeground(sp.getSoluong() <= 10 ? accentOrange : darkGray); // Highlight low stock in orange
            lblProductStock.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Add components to the box
            add(lblProductName);
            add(lblProductPrice);
            add(lblProductStock);

            // Optional: Add vertical glue to push content towards the center
            add(Box.createVerticalGlue());


            // Optional: Add mouse listener to open product details or add to cart
             setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hand cursor on hover
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    System.out.println("Clicked on product: " + sanPham.getTenSP() + " (ID: " + sanPham.getMaSP() + ")");
                    // TODO: Implement action on click (e.g., open detail dialog, add to cart)
                     // Example: Show product details in a dialog
                      // ProductDetailDialog detailDialog = new ProductDetailDialog(null, sanPham); // Assuming a ProductDetailDialog exists
                      // detailDialog.setVisible(true);
                }
                 // Optional: Add hover effect for the product box
                 private Color originalBorderColor; // This will store the color before hover

                 @Override
                 public void mouseEntered(MouseEvent e) {
                     // Capture the original border color and type
                     // Assumes border is a CompoundBorder with LineBorder outside
                     if (getBorder() instanceof CompoundBorder) {
                         CompoundBorder compoundBorder = (CompoundBorder) getBorder();
                         if (compoundBorder.getOutsideBorder() instanceof LineBorder) {
                             originalBorderColor = ((LineBorder) compoundBorder.getOutsideBorder()).getLineColor();
                         } else {
                             // Fallback if border structure is different
                              originalBorderColor = darkGray; // Default to darkGray
                         }
                     } else if (getBorder() instanceof LineBorder) {
                         originalBorderColor = ((LineBorder) getBorder()).getLineColor();
                     } else {
                         // Fallback for other border types or no border
                         originalBorderColor = darkGray; // Default
                     }

                      setBorder(BorderFactory.createLineBorder(accentBlue, 2)); // Thicker border on hover
                 }

                 @Override
                 public void mouseExited(MouseEvent e) {
                     // Revert to original border (assuming thickness 1 for default product box border)
                      setBorder(BorderFactory.createLineBorder(originalBorderColor, 1)); // Revert
                 }
            });
        }

        // Optional: Method to get the product associated with this box
        public SanPham getSanPham() {
            return sanPham;
        }
    }


    // --- Main method for testing (Optional - Comment out when integrated) ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create sample NhanVien objects for testing different roles
             // Ensure NhanVien has set properties like TenNV, Tendangnhap, Role, MaNV
             NhanVien sampleAdminUser = new NhanVien();
             sampleAdminUser.setMaNV("NV001");
             sampleAdminUser.setTenNV("Admin User");
             sampleAdminUser.setTendangnhap("admin");
             sampleAdminUser.setRole("Admin");

             NhanVien sampleManagerUser = new NhanVien();
             sampleManagerUser.setMaNV("NV002");
             sampleManagerUser.setTenNV("Manager User");
             sampleManagerUser.setTendangnhap("manager");
             sampleManagerUser.setRole("Manager");

             NhanVien sampleStaffUser = new NhanVien();
             sampleStaffUser.setMaNV("NV003");
             sampleStaffUser.setTenNV("Staff User");
             sampleStaffUser.setTendangnhap("staff");
             sampleStaffUser.setRole("Staff");

             NhanVien sampleGuestUser = new NhanVien();
             sampleGuestUser.setMaNV("NV004");
             sampleGuestUser.setTenNV("Guest User");
             sampleGuestUser.setTendangnhap("guest");
             sampleGuestUser.setRole("Guest");


            JFrame frame = new JFrame("Trang chủ Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700); // Larger size for more content
            frame.setLocationRelativeTo(null);

            // Add the HomePanel with a sample user (change to test roles: sampleAdminUser, sampleManagerUser, etc.)
            // Note: For this main to run without full DAO implementations,
            // the DAO methods called in loadCategories, displayProducts,
            // and loadStatisticalData need to return mock/sample data or empty lists
            // to avoid NullPointerExceptions or errors.
             // HomePanel home = new HomePanel(sampleAdminUser); // Test Admin
             // HomePanel home = new HomePanel(sampleManagerUser); // Test Manager
             HomePanel home = new HomePanel(sampleStaffUser); // Test Staff (should only see products/filter)
             // HomePanel home = new HomePanel(sampleGuestUser); // Test Guest (should only see products/filter)

            frame.add(home);

            frame.setVisible(true);
        });
    }
}
