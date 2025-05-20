package ui;

import dao.LoaiDAO;
import dao.SanPhamDAO;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import model.Loai;
import model.NhanVien;
import model.SanPham;

public class SanPhamAll extends JFrame {

    private SanPhamDAO sanPhamDAO;
    private LoaiDAO loaiDAO;
    private JLabel lblStatus;
    private JPanel mainPanel;
    private JComboBox<Loai> cbLoai;
    private JPanel filterPanel;
    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnReset;
    private String activeFilter = null;
    private NhanVien loggedInUser;
    private Map<String, Loai> loaiCache;
    private JProgressBar progressBar;
    private JPanel loadingPanel;
    private JButton btnSort;
    private String currentSort = "default";
    private boolean isGridView = true; // Để kiểm soát chế độ hiển thị (lưới hoặc danh sách)

    // Colors
    private final Color primaryColor = new Color(101, 67, 33); // Coffee brown
    private final Color secondaryColor = new Color(245, 245, 220); // Light beige
    private final Color textColor = new Color(51, 51, 51); // Dark gray
    private final Color accentGreen = new Color(76, 175, 80); // Green
    private final Color accentOrange = new Color(255, 152, 0); // Orange
    private final Color accentBlue = new Color(33, 150, 243); // Blue

    public SanPhamAll() {
        this(null, null);
    }

    public SanPhamAll(NhanVien loggedInUser) {
        this(loggedInUser, null);
    }

    public SanPhamAll(NhanVien loggedInUser, String initialFilterCategory) {
        super("Danh sách Sản phẩm");
        this.loggedInUser = loggedInUser;
        this.activeFilter = initialFilterCategory;
        this.loaiCache = new HashMap<>();
        
        sanPhamDAO = new SanPhamDAO();
        loaiDAO = new LoaiDAO();

        initUI();
        initLoadingPanel();

        if (activeFilter != null) {
            setTitle("Danh sách Sản phẩm - Loại: " + getLoaiNameByMaLoai(activeFilter));
            filterByCategory(activeFilter);
        } else {
            loadAndDisplayProducts();
        }

        pack();
        setLocationRelativeTo(null);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(1200, 700));
        getContentPane().setBackground(secondaryColor);

        add(createHeaderPanel(), BorderLayout.NORTH);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(secondaryColor);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Cho phép cuộn ngang nếu cần
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16); // Tăng tốc độ cuộn ngang
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        add(createStatusPanel(), BorderLayout.SOUTH);
    }

    private void initLoadingPanel() {
        loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(secondaryColor);
        
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setString("Đang tải dữ liệu...");
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(secondaryColor);
        centerPanel.add(progressBar);
        
        loadingPanel.add(centerPanel, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setBackground(secondaryColor);
        headerPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        JLabel titleLabel = new JLabel("Danh sách Sản phẩm");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(primaryColor);
        titleLabel.setBorder(new EmptyBorder(0, 10, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(secondaryColor);

        // Search components
        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Arial", Font.BOLD, 12));
        lblSearch.setForeground(textColor);
        filterPanel.add(lblSearch);

        txtSearch = new JTextField(20);
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        filterPanel.add(txtSearch);

        btnSearch = new JButton("Tìm");
        styleButton(btnSearch, accentBlue, Color.WHITE);
        btnSearch.addActionListener(e -> searchProducts(txtSearch.getText().trim()));
        filterPanel.add(btnSearch);

        txtSearch.addActionListener(e -> searchProducts(txtSearch.getText().trim()));

        filterPanel.add(new JSeparator(JSeparator.VERTICAL));

        // Category filter
        JLabel lblFilter = new JLabel("Lọc theo loại:");
        lblFilter.setFont(new Font("Arial", Font.BOLD, 12));
        lblFilter.setForeground(textColor);
        filterPanel.add(lblFilter);

        cbLoai = new JComboBox<>();
        cbLoai.setFont(new Font("Arial", Font.PLAIN, 12));
        cbLoai.setBackground(Color.WHITE);
        cbLoai.setPreferredSize(new Dimension(200, 25));
        loadCategories();
        filterPanel.add(cbLoai);

        JButton btnFilter = new JButton("Lọc");
        styleButton(btnFilter, accentBlue, Color.WHITE);
        btnFilter.addActionListener(e -> {
            Loai selectedLoai = (Loai) cbLoai.getSelectedItem();
            if (selectedLoai != null && selectedLoai.getMaloai() != null && !selectedLoai.getMaloai().isEmpty()) {
                filterByCategory(selectedLoai.getMaloai());
                setTitle("Danh sách Sản phẩm - Loại: " + selectedLoai.getTenloai());
            } else {
                activeFilter = null;
                setTitle("Danh sách Sản phẩm");
                loadAndDisplayProducts();
            }
        });
        filterPanel.add(btnFilter);

        // Sort button
        btnSort = new JButton("Sắp xếp");
        styleButton(btnSort, accentBlue, Color.WHITE);
        btnSort.addActionListener(e -> showSortMenu());
        filterPanel.add(btnSort);

        // View toggle button
        JButton btnToggleView = new JButton(isGridView ? "Chế độ danh sách" : "Chế độ lưới");
        styleButton(btnToggleView, accentBlue, Color.WHITE);
        btnToggleView.addActionListener(e -> {
            isGridView = !isGridView;
            btnToggleView.setText(isGridView ? "Chế độ danh sách" : "Chế độ lưới");
            applyCurrentFilter(); // Refresh với chế độ xem mới
        });
        filterPanel.add(btnToggleView);

        // Reset button
        btnReset = new JButton("Tất cả sản phẩm");
        styleButton(btnReset, accentGreen, Color.WHITE);
        btnReset.addActionListener(e -> resetFilters());
        filterPanel.add(btnReset);

        headerPanel.add(filterPanel, BorderLayout.CENTER);
        
        if (loggedInUser != null) {
            headerPanel.add(createActionPanel(), BorderLayout.SOUTH);
        }

        return headerPanel;
    }

    private void showSortMenu() {
        JPopupMenu sortMenu = new JPopupMenu();
        
        JMenuItem defaultSort = new JMenuItem("Mặc định");
        defaultSort.addActionListener(e -> {
            currentSort = "default";
            applyCurrentFilter();
        });
        sortMenu.add(defaultSort);
        
        JMenuItem sortByName = new JMenuItem("Theo tên (A-Z)");
        sortByName.addActionListener(e -> {
            currentSort = "name_asc";
            applyCurrentFilter();
        });
        sortMenu.add(sortByName);
        
        JMenuItem sortByNameDesc = new JMenuItem("Theo tên (Z-A)");
        sortByNameDesc.addActionListener(e -> {
            currentSort = "name_desc";
            applyCurrentFilter();
        });
        sortMenu.add(sortByNameDesc);
        
        JMenuItem sortByPrice = new JMenuItem("Giá thấp đến cao");
        sortByPrice.addActionListener(e -> {
            currentSort = "price_asc";
            applyCurrentFilter();
        });
        sortMenu.add(sortByPrice);
        
        JMenuItem sortByPriceDesc = new JMenuItem("Giá cao đến thấp");
        sortByPriceDesc.addActionListener(e -> {
            currentSort = "price_desc";
            applyCurrentFilter();
        });
        sortMenu.add(sortByPriceDesc);
        
        sortMenu.show(btnSort, 0, btnSort.getHeight());
    }

    private void applyCurrentFilter() {
        if (activeFilter != null) {
            filterByCategory(activeFilter);
        } else if (!txtSearch.getText().trim().isEmpty()) {
            searchProducts(txtSearch.getText().trim());
        } else {
            loadAndDisplayProducts();
        }
    }

    private void resetFilters() {
        txtSearch.setText("");
        cbLoai.setSelectedIndex(0);
        activeFilter = null;
        currentSort = "default";
        setTitle("Danh sách Sản phẩm");
        loadAndDisplayProducts();
    }

    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setBackground(secondaryColor);
        
        String role = loggedInUser.getRole();
        
        if ("Admin".equalsIgnoreCase(role)) {
            JButton btnImport = new JButton("Nhập từ Excel");
            styleButton(btnImport, accentOrange, Color.WHITE);
            btnImport.addActionListener(e -> importFromExcel());
            actionPanel.add(btnImport);
        }

        if ("Admin".equalsIgnoreCase(role)){
            JButton btnAddProduct = new JButton("Thêm sản phẩm mới");
            styleButton(btnAddProduct, accentGreen, Color.WHITE);
            btnAddProduct.addActionListener(e -> openAddProductDialog());
            actionPanel.add(btnAddProduct);
        }
        
        if ("Admin".equalsIgnoreCase(role)) {
            JButton btnExport = new JButton("Xuất báo cáo");
            styleButton(btnExport, accentBlue, Color.WHITE);
            btnExport.addActionListener(e -> exportReport());
            actionPanel.add(btnExport);
        }
        
        return actionPanel;
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(secondaryColor);
        statusPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        lblStatus = new JLabel("Đang tải dữ liệu...", SwingConstants.LEFT);
        lblStatus.setFont(new Font("Arial", Font.ITALIC, 12));
        lblStatus.setForeground(textColor);
        statusPanel.add(lblStatus, BorderLayout.WEST);

        if (loggedInUser != null) {
            JLabel lblLoggedIn = new JLabel("Đăng nhập với: " + loggedInUser.getTenNV(), SwingConstants.RIGHT);
            lblLoggedIn.setFont(new Font("Arial", Font.BOLD, 12));
            lblLoggedIn.setForeground(primaryColor);
            statusPanel.add(lblLoggedIn, BorderLayout.EAST);
        }

        return statusPanel;
    }

    private void loadCategories() {
        cbLoai.removeAllItems();
        cbLoai.addItem(new Loai("", "-- Tất cả loại --"));

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<Loai> loaiList = loaiDAO.getAllLoai();
                if (loaiList != null) {
                    for (Loai loai : loaiList) {
                        loaiCache.put(loai.getMaloai(), loai);
                        publish();
                    }
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Void> chunks) {
                for (Loai loai : loaiCache.values()) {
                    cbLoai.addItem(loai);
                    if (activeFilter != null && activeFilter.equals(loai.getMaloai())) {
                        cbLoai.setSelectedItem(loai);
                    }
                }
            }
        };
        worker.execute();
    }

    private void loadAndDisplayProducts() {
        showLoading(true);
        lblStatus.setText("Đang tải dữ liệu...");

        SwingWorker<List<SanPham>, Void> worker = new SwingWorker<List<SanPham>, Void>() {
            @Override
            protected List<SanPham> doInBackground() throws Exception {
                return sanPhamDAO.getAllSanPham();
            }

            @Override
            protected void done() {
                try {
                    List<SanPham> danhSachSanPham = get();
                    showLoading(false);
                    displayProducts(danhSachSanPham);
                    lblStatus.setText("Tổng cộng: " + danhSachSanPham.size() + " sản phẩm");
                } catch (Exception e) {
                    showLoading(false);
                    lblStatus.setText("Lỗi tải dữ liệu: " + e.getMessage());
                    JOptionPane.showMessageDialog(SanPhamAll.this, 
                        "Đã xảy ra lỗi không xác định: " + e.getMessage(), 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void displayProducts(List<SanPham> products) {
        mainPanel.removeAll();

        if (products == null || products.isEmpty()) {
            lblStatus.setText("Không có dữ liệu sản phẩm.");
            return;
        }

        // Apply sorting
        products = sortProducts(products);

        // Group products by category
        Map<String, List<SanPham>> productsByCategory = new HashMap<>();
        Map<String, String> loaiNameMap = new HashMap<>();

        // Initialize with all categories
        for (Loai loai : loaiCache.values()) {
            productsByCategory.put(loai.getMaloai(), new ArrayList<>());
            loaiNameMap.put(loai.getMaloai(), loai.getTenloai());
        }

        // Categorize products
        for (SanPham sp : products) {
            String maLoai = sp.getMaloai();
            if (productsByCategory.containsKey(maLoai)) {
                productsByCategory.get(maLoai).add(sp);
            } else {
                productsByCategory.computeIfAbsent("Uncategorized", k -> new ArrayList<>()).add(sp);
                loaiNameMap.putIfAbsent("Uncategorized", "Không phân loại");
            }
        }

        // Create panels for each category
        for (Map.Entry<String, List<SanPham>> entry : productsByCategory.entrySet()) {
            String maLoai = entry.getKey();
            List<SanPham> productsInCategory = entry.getValue();

            if (productsInCategory.isEmpty()) {
                continue;
            }

            JPanel categoryPanel = createCategoryPanel(maLoai, loaiNameMap.get(maLoai), productsInCategory);
            mainPanel.add(categoryPanel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private List<SanPham> sortProducts(List<SanPham> products) {
        switch (currentSort) {
            case "name_asc":
                products.sort(Comparator.comparing(SanPham::getTenSP, String.CASE_INSENSITIVE_ORDER));
                break;
            case "name_desc":
                products.sort(Comparator.comparing(SanPham::getTenSP, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            case "price_asc":
                products.sort(Comparator.comparingDouble(SanPham::getGiaban));
                break;
            case "price_desc":
                products.sort(Comparator.comparingDouble(SanPham::getGiaban).reversed());
                break;
            default:
                // Default sorting (maybe by ID or as from DB)
                break;
        }
        return products;
    }

    private JPanel createCategoryPanel(String maLoai, String categoryName, List<SanPham> products) {
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBackground(secondaryColor);
        
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(secondaryColor);
        
        JLabel lblCategoryName = new JLabel(categoryName);
        lblCategoryName.setFont(new Font("Arial", Font.BOLD, 16));
        lblCategoryName.setForeground(primaryColor);
        lblCategoryName.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JLabel lblProductCount = new JLabel(products.size() + " sản phẩm");
        lblProductCount.setFont(new Font("Arial", Font.PLAIN, 12));
        lblProductCount.setForeground(textColor);
        lblProductCount.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        headerPanel.add(lblCategoryName, BorderLayout.WEST);
        headerPanel.add(lblProductCount, BorderLayout.EAST);
        
        // Products panel with horizontal scrolling
        JPanel productsPanel;
        JScrollPane productsScrollPane;
        
        if (isGridView) {
            // Grid view with horizontal scrolling
            productsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
            productsPanel.setBackground(secondaryColor);
            
            for (SanPham sp : products) {
                ProductBoxPanel productCard = new ProductBoxPanel(sp);
                setupProductCard(productCard, sp);
                productsPanel.add(productCard);
            }
            
            // Ensure the panel doesn't wrap to a new line
            productsPanel.setPreferredSize(new Dimension(
                (150 + 15) * products.size(), // width = (card width + spacing) * number of products
                180)); // fixed height that fits the cards
            
            // Create scroll pane for horizontal scrolling
            productsScrollPane = new JScrollPane(productsPanel);
            productsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            productsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
            productsScrollPane.getHorizontalScrollBar().setUnitIncrement(20);
            productsScrollPane.setBorder(null);
            productsScrollPane.setPreferredSize(new Dimension(1150, 200)); // Set preferred size for scroll pane
            productsScrollPane.setBackground(secondaryColor);
            productsScrollPane.getViewport().setBackground(secondaryColor);
            
            // Add navigation buttons
            JButton btnScrollLeft = new JButton("◀");
            btnScrollLeft.setFont(new Font("Arial", Font.BOLD, 16));
            styleButton(btnScrollLeft, accentBlue, Color.WHITE);
            btnScrollLeft.addActionListener(e -> {
                JScrollBar scrollBar = productsScrollPane.getHorizontalScrollBar();
                int currentValue = scrollBar.getValue();
                int newValue = Math.max(0, currentValue - 300);
                scrollBar.setValue(newValue);
            });
            
            JButton btnScrollRight = new JButton("▶");
            btnScrollRight.setFont(new Font("Arial", Font.BOLD, 16));
            styleButton(btnScrollRight, accentBlue, Color.WHITE);
            btnScrollRight.addActionListener(e -> {
                JScrollBar scrollBar = productsScrollPane.getHorizontalScrollBar();
                int currentValue = scrollBar.getValue();
                int max = scrollBar.getMaximum() - scrollBar.getVisibleAmount();
                int newValue = Math.min(max, currentValue + 300);
                scrollBar.setValue(newValue);
            });
            
            // Create panel for navigation buttons
            JPanel navPanel = new JPanel(new BorderLayout());
            navPanel.setOpaque(false);
            navPanel.add(btnScrollLeft, BorderLayout.WEST);
            navPanel.add(productsScrollPane, BorderLayout.CENTER);
            navPanel.add(btnScrollRight, BorderLayout.EAST);
            
            // Add scroll pane with navigation to the category panel
            categoryPanel.add(headerPanel);
            categoryPanel.add(navPanel);
        } else {
            // List view (vertical layout without horizontal scrolling)
            productsPanel = new JPanel();
            productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
            productsPanel.setBackground(secondaryColor);
            
            for (SanPham sp : products) {
                JPanel productRow = createProductRow(sp);
                productsPanel.add(productRow);
                productsPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Space between rows
            }
            
            // Create scroll pane for vertical list view
            productsScrollPane = new JScrollPane(productsPanel);
            productsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            productsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            productsScrollPane.setBorder(null);
            productsScrollPane.setPreferredSize(new Dimension(1150, Math.min(400, products.size() * 65))); // Limit height
            productsScrollPane.setBackground(secondaryColor);
            productsScrollPane.getViewport().setBackground(secondaryColor);
            
            // Add directly to the category panel
            categoryPanel.add(headerPanel);
            categoryPanel.add(productsScrollPane);
        }
        
        // Border
        categoryPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1),
                "",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), primaryColor));
        
        return categoryPanel;
    }

    private JPanel createProductRow(SanPham product) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        
        // Product image (small)
        JLabel imgLabel = new JLabel();
        imgLabel.setPreferredSize(new Dimension(50, 50));
        
        // Load image logic (simplified)
        if (product.getAnh() != null && !product.getAnh().toString().isEmpty()) {
            try {
                ImageIcon icon = new ImageIcon(product.getAnh().toString());
                Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                imgLabel.setIcon(new ImageIcon(img));
            } catch (Exception e) {
                imgLabel.setText("Ảnh");
            }
        } else {
            imgLabel.setText("Ảnh");
        }
        
        rowPanel.add(imgLabel, BorderLayout.WEST);
        
        // Product details
        JPanel detailsPanel = new JPanel(new GridLayout(2, 1));
        detailsPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(product.getTenSP());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setForeground(primaryColor);
        
        JLabel infoLabel = new JLabel(String.format("Mã: %s | Giá: %,d VNĐ | SL: %d", 
                product.getMaSP(), (int)product.getGiaban(), product.getSoluong()));
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        detailsPanel.add(nameLabel);
        detailsPanel.add(infoLabel);
        
        rowPanel.add(detailsPanel, BorderLayout.CENTER);
        
        // Actions
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionsPanel.setOpaque(false);
        
        JButton btnView = new JButton("Xem");
        styleButton(btnView, accentBlue, Color.WHITE);
        btnView.addActionListener(e -> new ProductDetailsDialog(SanPhamAll.this, product).setVisible(true));
        actionsPanel.add(btnView);
        
        rowPanel.add(actionsPanel, BorderLayout.EAST);
        
        // Add context menu
        if (loggedInUser != null) {
            ProductContextMenu contextMenu = new ProductContextMenu(this, product, loggedInUser);
            rowPanel.setComponentPopupMenu(contextMenu);
            
            rowPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    rowPanel.setBackground(new Color(240, 240, 240));
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    rowPanel.setBackground(Color.WHITE);
                }
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        new ProductDetailsDialog(SanPhamAll.this, product).setVisible(true);
                    }
                }
            });
        }
        
        return rowPanel;
    }

    private void setupProductCard(ProductBoxPanel productCard, SanPham product) {
        productCard.setToolTipText(createTooltipText(product));
        
        if (loggedInUser != null) {
            ProductContextMenu contextMenu = new ProductContextMenu(this, product, loggedInUser);
            productCard.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        contextMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
                
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        contextMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
                
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        new ProductDetailsDialog(SanPhamAll.this, product).setVisible(true);
                    }
                }
            });
        }
    }

    private String createTooltipText(SanPham product) {
        return "<html><b>" + product.getTenSP() + "</b><br>" +
               "Mã SP: " + product.getMaSP() + "<br>" +
               "Giá: " + String.format("%,d", (int)product.getGiaban()) + " VND<br>" +
               "Số lượng: " + product.getSoluong() + "<br>" +
               "Loại: " + getLoaiNameByMaLoai(product.getMaloai()) + "</html>";
    }

    public void filterByCategory(String maLoai) {
        if (maLoai == null || maLoai.isEmpty()) {
            loadAndDisplayProducts();
            return;
        }

        activeFilter = maLoai;
        showLoading(true);
        lblStatus.setText("Đang tải dữ liệu...");

        SwingWorker<List<SanPham>, Void> worker = new SwingWorker<List<SanPham>, Void>() {
            @Override
            protected List<SanPham> doInBackground() throws Exception {
                return sanPhamDAO.getSanPhamByLoai(maLoai);
            }

            @Override
            protected void done() {
                try {
                    List<SanPham> filteredList = get();
                    showLoading(false);
                    
                    if (filteredList == null || filteredList.isEmpty()) {
                        lblStatus.setText("Không tìm thấy sản phẩm nào thuộc loại " + getLoaiNameByMaLoai(maLoai));
                        mainPanel.removeAll();
                        mainPanel.revalidate();
                        mainPanel.repaint();
                        return;
                    }

                    displayFilteredProducts(filteredList, maLoai);
                    lblStatus.setText("Tìm thấy: " + filteredList.size() + " sản phẩm thuộc loại " + getLoaiNameByMaLoai(maLoai));                  } catch (Exception e) {
                    showLoading(false);
                    lblStatus.setText("Lỗi tải dữ liệu: " + e.getMessage());
                    JOptionPane.showMessageDialog(SanPhamAll.this, 
                        "Lỗi khi tải dữ liệu sản phẩm theo loại: " + e.getMessage(), 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void displayFilteredProducts(List<SanPham> products, String maLoai) {
        mainPanel.removeAll();
        
        // Apply sorting
        products = sortProducts(products);
        
        JPanel categoryPanel = createCategoryPanel(maLoai, getLoaiNameByMaLoai(maLoai), products);
        mainPanel.add(categoryPanel);
        mainPanel.add(Box.createVerticalGlue());

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            if (activeFilter != null) {
                filterByCategory(activeFilter);
            } else {
                loadAndDisplayProducts();
            }
            return;
        }

        showLoading(true);
        lblStatus.setText("Đang tìm kiếm...");

        SwingWorker<List<SanPham>, Void> worker = new SwingWorker<List<SanPham>, Void>() {
            @Override
            protected List<SanPham> doInBackground() throws Exception {
                return sanPhamDAO.searchSanPhamByName(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<SanPham> searchResults = get();
                    showLoading(false);
                    
                    if (searchResults == null || searchResults.isEmpty()) {
                        lblStatus.setText("Không tìm thấy sản phẩm nào khớp với từ khóa: \"" + keyword + "\"");
                        mainPanel.removeAll();
                        mainPanel.revalidate();
                        mainPanel.repaint();
                        return;
                    }

                    // Filter by category if active
                    if (activeFilter != null) {
                        List<SanPham> filteredResults = new ArrayList<>();
                        for (SanPham sp : searchResults) {
                            if (activeFilter.equals(sp.getMaloai())) {
                                filteredResults.add(sp);
                            }
                        }
                        searchResults = filteredResults;
                    }

                    displaySearchResults(searchResults, keyword);
                    
                    String statusText = "Tìm thấy: " + searchResults.size() + " sản phẩm khớp với từ khóa: \"" + keyword + "\"";
                    if (activeFilter != null) {
                        statusText += " (Loại: " + getLoaiNameByMaLoai(activeFilter) + ")";
                    }
                    lblStatus.setText(statusText);
                } catch (Exception e) {
                    showLoading(false);
                    lblStatus.setText("Lỗi tìm kiếm: " + e.getMessage());
                    JOptionPane.showMessageDialog(SanPhamAll.this, 
                        "Lỗi khi tìm kiếm sản phẩm: " + e.getMessage(), 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void displaySearchResults(List<SanPham> products, String keyword) {
        mainPanel.removeAll();
        
        // Apply sorting
        products = sortProducts(products);
        
        // Tạo panel cho kết quả tìm kiếm
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBackground(secondaryColor);
        
        String title = "Kết quả tìm kiếm: \"" + keyword + "\"";
        if (activeFilter != null) {
            title += " (Loại: " + getLoaiNameByMaLoai(activeFilter) + ")";
        }
        
        // Tạo header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(secondaryColor);
        
        JLabel lblSearchTitle = new JLabel(title);
        lblSearchTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblSearchTitle.setForeground(primaryColor);
        lblSearchTitle.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JLabel lblResultCount = new JLabel(products.size() + " kết quả");
        lblResultCount.setFont(new Font("Arial", Font.PLAIN, 12));
        lblResultCount.setForeground(textColor);
        lblResultCount.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        headerPanel.add(lblSearchTitle, BorderLayout.WEST);
        headerPanel.add(lblResultCount, BorderLayout.EAST);
        
        // Tạo panel sản phẩm dựa trên chế độ xem
        JComponent productsComponent;
        
        if (isGridView) {
            // Grid view với cuộn ngang
            JPanel productsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
            productsPanel.setBackground(secondaryColor);
            
            for (SanPham sp : products) {
                ProductBoxPanel productCard = new ProductBoxPanel(sp);
                setupProductCard(productCard, sp);
                productsPanel.add(productCard);
            }
            
            productsComponent = productsPanel;
        } else {
            // List view
            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setBackground(secondaryColor);
            
            for (SanPham sp : products) {
                JPanel productRow = createProductRow(sp);
                listPanel.add(productRow);
                listPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Space between rows
            }
            
            JScrollPane scrollPane = new JScrollPane(listPanel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setBorder(null);
            scrollPane.setPreferredSize(new Dimension(1150, Math.min(400, products.size() * 65))); // Limit height
            scrollPane.setBackground(secondaryColor);
            scrollPane.getViewport().setBackground(secondaryColor);
            
            productsComponent = scrollPane;
        }
        
        searchPanel.add(headerPanel);
        searchPanel.add(productsComponent);
        
        // Border
        searchPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1),
                "",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), primaryColor));
        
        mainPanel.add(searchPanel);
        mainPanel.add(Box.createVerticalGlue());

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void showLoading(boolean show) {
        if (show) {
            mainPanel.removeAll();
            mainPanel.add(loadingPanel);
        } else {
            mainPanel.remove(loadingPanel);
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private String getLoaiNameByMaLoai(String maLoai) {
        if (maLoai == null || maLoai.isEmpty()) {
            return "Không xác định";
        }
        
        if (maLoai.equals("Uncategorized")) {
            return "Không phân loại";
        }
        
        Loai loai = loaiCache.get(maLoai);
        if (loai != null) {
            return loai.getTenloai();
        }
        
        try {
            loai = loaiDAO.getLoaiById(maLoai);
            if (loai != null) {
                loaiCache.put(maLoai, loai);
                return loai.getTenloai();
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tên loại cho mã: " + maLoai + ": " + e.getMessage());
        }
        
        return maLoai;
    }

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

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }

    // Inner class for product context menu
    class ProductContextMenu extends JPopupMenu {
        public ProductContextMenu(JFrame parent, SanPham product, NhanVien user) {
            JMenuItem menuItemViewDetails = new JMenuItem("Xem chi tiết");
            menuItemViewDetails.addActionListener(e -> {
                ProductDetailsDialog detailsDialog = new ProductDetailsDialog(parent, product);
                detailsDialog.setVisible(true);
            });
            add(menuItemViewDetails);
            
            String role = user.getRole();
            
            if ("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role)) {
                addSeparator();
                
                JMenuItem menuItemEdit = new JMenuItem("Chỉnh sửa sản phẩm");
                menuItemEdit.addActionListener(e -> {
                    EditProductDialog dialog = new EditProductDialog(parent, product, user);
                    dialog.setVisible(true);
                    if(dialog.isUpdateSuccessful()){
                        JOptionPane.showMessageDialog(this,
                            "Đã cập nhật sản phẩm: " + dialog.getUpdatedProduct().getTenSP(),
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        
                        applyCurrentFilter();
                    }
                });
                add(menuItemEdit);
                
                JMenuItem menuItemUpdateStock = new JMenuItem("Cập nhật số lượng");
                menuItemUpdateStock.addActionListener(e -> {
                    String input = JOptionPane.showInputDialog(parent, 
                        "Nhập số lượng mới cho sản phẩm " + product.getTenSP() + ":", 
                        "Cập nhật số lượng", JOptionPane.QUESTION_MESSAGE);
                    
                    if (input != null && !input.trim().isEmpty()) {
                        try {
                            int newQuantity = Integer.parseInt(input.trim());
                            if (newQuantity < 0) {
                                JOptionPane.showMessageDialog(parent, 
                                    "Số lượng không được âm", 
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            
                            // TODO: Cập nhật số lượng trong CSDL
                            JOptionPane.showMessageDialog(parent, 
                                "Đã cập nhật số lượng cho " + product.getTenSP() + " thành " + newQuantity, 
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                                
                            // Reload data after update
                            applyCurrentFilter();
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(parent, 
                                "Vui lòng nhập một số nguyên hợp lệ", 
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                add(menuItemUpdateStock);
            }
            
            if ("Admin".equalsIgnoreCase(role)) {
                addSeparator();
                JMenuItem menuItemDelete = new JMenuItem("Xóa sản phẩm");
                menuItemDelete.setForeground(Color.RED);
                menuItemDelete.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(parent,
                        "Bạn có chắc chắn muốn xóa sản phẩm " + product.getTenSP() + "?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        // TODO: Xóa sản phẩm trong CSDL
                        JOptionPane.showMessageDialog(parent, 
                            "Đã xóa sản phẩm " + product.getTenSP(), 
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Reload data after delete
                        applyCurrentFilter();
                    }
                });
                add(menuItemDelete);
            }
        }
    }

    // Placeholder methods for actions
    private void openAddProductDialog() {
        AddProductDialog dialog = new AddProductDialog(this, loggedInUser);
        dialog.setVisible(true);
        
        if (dialog.isAddSuccessful()) {
            JOptionPane.showMessageDialog(this,
                "Đã thêm sản phẩm: " + dialog.getAddedProduct().getTenSP(),
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
            
            applyCurrentFilter();
        }
    }

    private void exportReport() {
        JOptionPane.showMessageDialog(this, 
            "Chức năng đang phát triển: Xuất báo cáo danh sách sản phẩm", 
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void importFromExcel() {
        JOptionPane.showMessageDialog(this, 
            "Chức năng đang phát triển: Nhập sản phẩm từ Excel", 
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            SanPhamAll frame = new SanPhamAll();
            frame.setVisible(true);
        });
    }
}