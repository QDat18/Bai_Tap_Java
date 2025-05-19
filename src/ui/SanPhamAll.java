package ui;

import dao.LoaiDAO;
import dao.SanPhamDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
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
    private String activeFilter = null; // Theo dõi bộ lọc hiện tại
    private NhanVien loggedInUser; // Thông tin nhân viên đăng nhập

    // Sử dụng màu sắc từ OriginalColors trong ProductBoxPanel.java
    private final Color primaryColor = OriginalColors.coffeeBrown; // Nâu đậm (cho tiêu đề, viền)
    private final Color secondaryColor = OriginalColors.lightBeige; // Màu be sáng (nền)
    private final Color textColor = OriginalColors.darkGray; // Xám đậm (văn bản)
    private final Color accentGreen = OriginalColors.accentGreen; // Xanh lá (nút tích cực)
    private final Color accentOrange = OriginalColors.accentOrange; // Cam (cảnh báo)
    private final Color accentBlue = OriginalColors.accentBlue; // Xanh dương (nút thông thường)

    /**
     * Constructor không có tham số, chỉ hiển thị tất cả sản phẩm
     */
    public SanPhamAll() {
        this(null, null);
    }

    /**
     * Constructor với tham số nhân viên đăng nhập
     * @param loggedInUser Nhân viên đang đăng nhập
     */
    public SanPhamAll(NhanVien loggedInUser) {
        this(loggedInUser, null);
    }

    /**
     * Constructor đầy đủ với nhân viên đăng nhập và bộ lọc loại
     * @param loggedInUser Nhân viên đang đăng nhập
     * @param initialFilterCategory Mã loại để lọc ban đầu (có thể null)
     */
    public SanPhamAll(NhanVien loggedInUser, String initialFilterCategory) {
        super("Danh sách Sản phẩm");
        this.loggedInUser = loggedInUser;
        this.activeFilter = initialFilterCategory;
        
        sanPhamDAO = new SanPhamDAO();
        loaiDAO = new LoaiDAO();

        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(1200, 700));
        getContentPane().setBackground(secondaryColor);

        // Tạo panel header chứa tiêu đề và bộ lọc
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main panel với nội dung có thể cuộn
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(secondaryColor);
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);

        // Panel trạng thái ở dưới cùng
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        // Tải dữ liệu và hiển thị sản phẩm
        if (activeFilter != null) {
            setTitle("Danh sách Sản phẩm - Loại: " + getLoaiNameByMaLoai(activeFilter));
            filterByCategory(activeFilter);
        } else {
            loadAndDisplayProducts();
        }

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Tạo panel chứa tiêu đề và bộ lọc
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setBackground(secondaryColor);
        headerPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        // Tiêu đề
        JLabel titleLabel = new JLabel("Danh sách Sản phẩm");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(primaryColor);
        titleLabel.setBorder(new EmptyBorder(0, 10, 10, 0));
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        // Panel bộ lọc
        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(secondaryColor);

        // Tìm kiếm
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

        // Thêm Enter key listener cho ô tìm kiếm
        txtSearch.addActionListener(e -> searchProducts(txtSearch.getText().trim()));

        // Separator
        filterPanel.add(new JSeparator(JSeparator.VERTICAL));

        // Lọc theo loại
        JLabel lblFilter = new JLabel("Lọc theo loại:");
        lblFilter.setFont(new Font("Arial", Font.BOLD, 12));
        lblFilter.setForeground(textColor);
        filterPanel.add(lblFilter);

        cbLoai = new JComboBox<>();
        cbLoai.setFont(new Font("Arial", Font.PLAIN, 12));
        cbLoai.setBackground(Color.WHITE);
        cbLoai.setPreferredSize(new Dimension(200, 25));
        loadCategories(); // Nạp danh sách loại vào ComboBox
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

        // Nút đặt lại bộ lọc
        btnReset = new JButton("Tất cả sản phẩm");
        styleButton(btnReset, accentGreen, Color.WHITE);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbLoai.setSelectedIndex(0);
            activeFilter = null;
            setTitle("Danh sách Sản phẩm");
            loadAndDisplayProducts();
        });
        filterPanel.add(btnReset);

        headerPanel.add(filterPanel, BorderLayout.CENTER);
        
        // Thêm panel nút điều hướng tùy theo vai trò người dùng
        if (loggedInUser != null) {
            JPanel actionPanel = createActionPanel();
            headerPanel.add(actionPanel, BorderLayout.SOUTH);
        }

        return headerPanel;
    }

    /**
     * Tạo panel chứa các nút hành động dựa trên vai trò người dùng
     */
    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        actionPanel.setBackground(secondaryColor);
        
        String role = loggedInUser.getRole();
        
        // Nút thêm sản phẩm mới - chỉ cho Admin và Manager
        if ("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role)) {
            JButton btnAddProduct = new JButton("Thêm sản phẩm mới");
            styleButton(btnAddProduct, accentGreen, Color.WHITE);
            btnAddProduct.addActionListener(e -> {
                // Mở dialog thêm sản phẩm mới nếu có
                JOptionPane.showMessageDialog(this, 
                    "Chức năng đang phát triển: Thêm sản phẩm mới", 
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                // TODO: Thêm code mở dialog thêm sản phẩm mới ở đây
            });
            actionPanel.add(btnAddProduct);
        }
        
        // Nút xuất báo cáo - chỉ cho Admin và Manager
        if ("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role)) {
            JButton btnExport = new JButton("Xuất báo cáo");
            styleButton(btnExport, accentBlue, Color.WHITE);
            btnExport.addActionListener(e -> {
                JOptionPane.showMessageDialog(this, 
                    "Chức năng đang phát triển: Xuất báo cáo danh sách sản phẩm", 
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                // TODO: Thêm code xuất báo cáo ở đây
            });
            actionPanel.add(btnExport);
        }
        
        return actionPanel;
    }

    /**
     * Tạo panel trạng thái ở dưới cùng
     */
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(secondaryColor);
        statusPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        lblStatus = new JLabel("Đang tải dữ liệu...", SwingConstants.LEFT);
        lblStatus.setFont(new Font("Arial", Font.ITALIC, 12));
        lblStatus.setForeground(textColor);
        statusPanel.add(lblStatus, BorderLayout.WEST);

        // Thêm thông tin trạng thái đã đăng nhập (nếu có)
        if (loggedInUser != null) {
            JLabel lblLoggedIn = new JLabel("Đăng nhập với: " + loggedInUser.getTenNV(), SwingConstants.RIGHT);
            lblLoggedIn.setFont(new Font("Arial", Font.BOLD, 12));
            lblLoggedIn.setForeground(primaryColor);
            statusPanel.add(lblLoggedIn, BorderLayout.EAST);
        }

        return statusPanel;
    }

    /**
     * Nạp danh sách loại sản phẩm vào combobox
     */
    private void loadCategories() {
        cbLoai.removeAllItems();
        cbLoai.addItem(new Loai("", "-- Tất cả loại --"));

        List<Loai> loaiList = loaiDAO.getAllLoai();
        if (loaiList != null) {
            for (Loai loai : loaiList) {
                cbLoai.addItem(loai);
                
                // Nếu đang lọc theo loại, chọn loại đó trong combobox
                if (activeFilter != null && activeFilter.equals(loai.getMaloai())) {
                    cbLoai.setSelectedItem(loai);
                }
            }
        } else {
            System.out.println("Không lấy được danh sách loại sản phẩm từ CSDL.");
        }
    }

    /**
     * Tải và hiển thị tất cả sản phẩm theo loại
     */
    private void loadAndDisplayProducts() {
        lblStatus.setText("Đang tải dữ liệu...");
        mainPanel.removeAll();

        // Lấy tất cả sản phẩm và loại
        List<SanPham> danhSachSanPham = null;
        List<Loai> danhSachLoai = null;
        try {
            danhSachSanPham = sanPhamDAO.getAllSanPham();
            danhSachLoai = loaiDAO.getAllLoai();
            lblStatus.setText("");
        } catch (Exception e) {
            lblStatus.setText("Lỗi tải dữ liệu: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi không xác định: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        if (danhSachSanPham == null || danhSachSanPham.isEmpty()) {
            lblStatus.setText("Không có dữ liệu sản phẩm.");
            return;
        }

        if (danhSachLoai == null || danhSachLoai.isEmpty()) {
            lblStatus.setText("Không có dữ liệu loại sản phẩm.");
            return;
        }

        // Nhóm sản phẩm theo loại (MaLoai)
        Map<String, List<SanPham>> productsByCategory = new HashMap<>();
        Map<String, String> loaiNameMap = new HashMap<>();

        // Khởi tạo map với tất cả loại
        for (Loai loai : danhSachLoai) {
            productsByCategory.put(loai.getMaloai(), new ArrayList<>());
            loaiNameMap.put(loai.getMaloai(), loai.getTenloai());
        }

        // Phân loại sản phẩm
        for (SanPham sp : danhSachSanPham) {
            String maLoai = sp.getMaloai();
            if (productsByCategory.containsKey(maLoai)) {
                productsByCategory.get(maLoai).add(sp);
            } else {
                // Nếu loại sản phẩm không có trong Loai, thêm vào nhóm "Không phân loại"
                productsByCategory.computeIfAbsent("Uncategorized", k -> new ArrayList<>()).add(sp);
                loaiNameMap.putIfAbsent("Uncategorized", "Không phân loại");
            }
        }

        // Tạo panel cho mỗi loại
        for (Map.Entry<String, List<SanPham>> entry : productsByCategory.entrySet()) {
            String maLoai = entry.getKey();
            List<SanPham> productsInCategory = entry.getValue();

            if (productsInCategory.isEmpty()) {
                continue; // Bỏ qua loại không có sản phẩm
            }

            // Tạo panel cho loại
            JPanel categoryPanel = new JPanel();
            categoryPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
            categoryPanel.setBackground(secondaryColor);
            
            // Tạo header panel cho loại
            JPanel categoryHeaderPanel = new JPanel(new BorderLayout());
            categoryHeaderPanel.setBackground(secondaryColor);
            
            // Tiêu đề loại
            JLabel lblCategoryName = new JLabel(loaiNameMap.get(maLoai));
            lblCategoryName.setFont(new Font("Arial", Font.BOLD, 16));
            lblCategoryName.setForeground(primaryColor);
            lblCategoryName.setBorder(new EmptyBorder(5, 10, 5, 10));
            categoryHeaderPanel.add(lblCategoryName, BorderLayout.WEST);
            
            // Hiển thị số lượng sản phẩm trong loại
            JLabel lblProductCount = new JLabel(productsInCategory.size() + " sản phẩm");
            lblProductCount.setFont(new Font("Arial", Font.PLAIN, 12));
            lblProductCount.setForeground(textColor);
            lblProductCount.setBorder(new EmptyBorder(5, 10, 5, 10));
            categoryHeaderPanel.add(lblProductCount, BorderLayout.EAST);
            
            // Border và padding cho panel loại
            categoryPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(primaryColor, 1),
                    "",
                    TitledBorder.LEADING, TitledBorder.TOP,
                    new Font("Arial", Font.BOLD, 14), primaryColor));
            
            // Panel chính cho loại
            JPanel categoryContentPanel = new JPanel(new BorderLayout());
            categoryContentPanel.setBackground(secondaryColor);
            categoryContentPanel.add(categoryHeaderPanel, BorderLayout.NORTH);
            
            // Panel chứa sản phẩm
            JPanel productsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
            productsPanel.setBackground(secondaryColor);

            // Thêm sản phẩm dưới dạng thẻ hộp sử dụng ProductBoxPanel
            for (SanPham sp : productsInCategory) {
                ProductBoxPanel productCard = new ProductBoxPanel(sp);
                
                // Thêm context menu cho product box (tùy theo vai trò người dùng)
                if (loggedInUser != null) {
                    addProductContextMenu(productCard, sp);
                }
                
                productsPanel.add(productCard);
            }
            
            categoryContentPanel.add(productsPanel, BorderLayout.CENTER);
            categoryPanel.add(categoryContentPanel);

            mainPanel.add(categoryPanel);
            mainPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Khoảng cách giữa các loại
        }

        // Cập nhật số lượng sản phẩm
        lblStatus.setText("Tổng cộng: " + danhSachSanPham.size() + " sản phẩm");

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    /**
     * Lọc sản phẩm theo mã loại
     */
    public void filterByCategory(String maLoai) {
        if (maLoai == null || maLoai.isEmpty()) {
            loadAndDisplayProducts();
            return;
        }

        activeFilter = maLoai;
        lblStatus.setText("Đang tải dữ liệu...");
        mainPanel.removeAll();

        List<SanPham> filteredList = null;
        try {
            filteredList = sanPhamDAO.getSanPhamByLoai(maLoai);
        } catch (Exception e) {
            lblStatus.setText("Lỗi tải dữ liệu: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu sản phẩm theo loại: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        if (filteredList == null || filteredList.isEmpty()) {
            lblStatus.setText("Không tìm thấy sản phẩm nào thuộc loại " + getLoaiNameByMaLoai(maLoai));
            return;
        }

        // Tạo panel cho loại đã lọc
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
        categoryPanel.setBackground(secondaryColor);
        categoryPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1),
                getLoaiNameByMaLoai(maLoai),
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), primaryColor));

        // Thêm sản phẩm vào panel
        for (SanPham sp : filteredList) {
            ProductBoxPanel productCard = new ProductBoxPanel(sp);
            
            // Thêm context menu cho product box (tùy theo vai trò người dùng)
            if (loggedInUser != null) {
                addProductContextMenu(productCard, sp);
            }
            
            categoryPanel.add(productCard);
        }

        mainPanel.add(categoryPanel);
        mainPanel.add(Box.createVerticalGlue());

        // Cập nhật số lượng sản phẩm
        lblStatus.setText("Tìm thấy: " + filteredList.size() + " sản phẩm thuộc loại " + getLoaiNameByMaLoai(maLoai));

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    /**
     * Tìm kiếm sản phẩm theo từ khóa
     */
    private void searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            if (activeFilter != null) {
                filterByCategory(activeFilter);
            } else {
                loadAndDisplayProducts();
            }
            return;
        }

        lblStatus.setText("Đang tìm kiếm...");
        mainPanel.removeAll();

        List<SanPham> searchResults = null;
        try {
            // Giả định SanPhamDAO có phương thức searchSanPham
            searchResults = sanPhamDAO.searchSanPhamByName(keyword);
        } catch (Exception e) {
            lblStatus.setText("Lỗi tìm kiếm: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Lỗi khi tìm kiếm sản phẩm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return;
        }

        if (searchResults == null || searchResults.isEmpty()) {
            lblStatus.setText("Không tìm thấy sản phẩm nào khớp với từ khóa: \"" + keyword + "\"");
            return;
        }

        // Lọc kết quả tìm kiếm theo loại nếu đang có bộ lọc loại active
        if (activeFilter != null) {
            List<SanPham> filteredResults = new ArrayList<>();
            for (SanPham sp : searchResults) {
                if (activeFilter.equals(sp.getMaloai())) {
                    filteredResults.add(sp);
                }
            }
            searchResults = filteredResults;
        }

        // Tạo panel cho kết quả tìm kiếm
        JPanel searchResultPanel = new JPanel();
        searchResultPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
        searchResultPanel.setBackground(secondaryColor);
        
        String title = "Kết quả tìm kiếm: \"" + keyword + "\"";
        if (activeFilter != null) {
            title += " (Loại: " + getLoaiNameByMaLoai(activeFilter) + ")";
        }
        
        searchResultPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor, 1),
                title,
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), primaryColor));

        // Thêm sản phẩm vào panel
        for (SanPham sp : searchResults) {
            ProductBoxPanel productCard = new ProductBoxPanel(sp);
            
            // Thêm context menu cho product box (tùy theo vai trò người dùng)
            if (loggedInUser != null) {
                addProductContextMenu(productCard, sp);
            }
            
            searchResultPanel.add(productCard);
        }

        mainPanel.add(searchResultPanel);
        mainPanel.add(Box.createVerticalGlue());

        // Cập nhật số lượng kết quả
        String statusText = "Tìm thấy: " + searchResults.size() + " sản phẩm khớp với từ khóa: \"" + keyword + "\"";
        if (activeFilter != null) {
            statusText += " (Loại: " + getLoaiNameByMaLoai(activeFilter) + ")";
        }
        lblStatus.setText(statusText);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    /**
     * Thêm context menu cho sản phẩm (tùy theo vai trò người dùng)
     */
    private void addProductContextMenu(ProductBoxPanel productCard, SanPham product) {
        JPopupMenu contextMenu = new JPopupMenu();
        
        // Xem chi tiết - tất cả vai trò
        JMenuItem menuItemViewDetails = new JMenuItem("Xem chi tiết");
        menuItemViewDetails.addActionListener(e -> {
            ProductDetailsDialog detailsDialog = new ProductDetailsDialog(this, product);
            detailsDialog.setVisible(true);
        });
        contextMenu.add(menuItemViewDetails);
        
        // Thêm các hành động chỉnh sửa/xóa dựa trên vai trò
        if (loggedInUser != null) {
            String role = loggedInUser.getRole();
            
            if ("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role)) {
                contextMenu.addSeparator();
                
                // Chỉnh sửa sản phẩm
                JMenuItem menuItemEdit = new JMenuItem("Chỉnh sửa sản phẩm");
                menuItemEdit.addActionListener(e -> {
                    // TODO: Mở dialog chỉnh sửa sản phẩm
                    JOptionPane.showMessageDialog(this, 
                        "Chức năng đang phát triển: Chỉnh sửa sản phẩm " + product.getMaSP(), 
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                });
                contextMenu.add(menuItemEdit);
                
                // Cập nhật số lượng
                JMenuItem menuItemUpdateStock = new JMenuItem("Cập nhật số lượng");
                menuItemUpdateStock.addActionListener(e -> {
                    // TODO: Mở dialog cập nhật số lượng
                    String input = JOptionPane.showInputDialog(this, 
                        "Nhập số lượng mới cho sản phẩm " + product.getTenSP() + ":", 
                        "Cập nhật số lượng", JOptionPane.QUESTION_MESSAGE);
                    
                    if (input != null && !input.trim().isEmpty()) {
                        try {
                            int newQuantity = Integer.parseInt(input.trim());
                            if (newQuantity < 0) {
                                JOptionPane.showMessageDialog(this, 
                                    "Số lượng không được âm", 
                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                            
                            // TODO: Cập nhật số lượng trong CSDL và refresh UI
                            JOptionPane.showMessageDialog(this, 
                                "Đã cập nhật số lượng cho " + product.getTenSP() + " thành " + newQuantity, 
                                "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, 
                                "Vui lòng nhập một số nguyên hợp lệ", 
                                "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                contextMenu.add(menuItemUpdateStock);
            }
            
            if ("Admin".equalsIgnoreCase(role)) {
                // Xóa sản phẩm - chỉ Admin
                contextMenu.addSeparator();
                JMenuItem menuItemDelete = new JMenuItem("Xóa sản phẩm");
                menuItemDelete.setForeground(Color.RED);
                menuItemDelete.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(this,
                        "Bạn có chắc chắn muốn xóa sản phẩm " + product.getTenSP() + "?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        // TODO: Xóa sản phẩm trong CSDL và refresh UI
                        JOptionPane.showMessageDialog(this, 
                            "Đã xóa sản phẩm " + product.getTenSP(), 
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Refresh view
                        if (activeFilter != null) {
                            filterByCategory(activeFilter);
                        } else {
                            loadAndDisplayProducts();
                        }}
                });
                contextMenu.add(menuItemDelete);
            }
        }
        
        // Thêm context menu vào productCard với chuột phải
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
        });
    }

    /**
     * Lấy tên loại từ mã loại
     */
    private String getLoaiNameByMaLoai(String maLoai) {
        if (maLoai == null || maLoai.isEmpty()) {
            return "Không xác định";
        }
        
        if (maLoai.equals("Uncategorized")) {
            return "Không phân loại";
        }
        
        try {
            Loai loai = loaiDAO.getLoaiById(maLoai);
            if (loai != null) {
                return loai.getTenloai();
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tên loại cho mã: " + maLoai + ": " + e.getMessage());
        }
        
        return maLoai;
    }

    /**
     * Phương thức định kiểu nút
     */
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

    /**
     * Main method để test (nếu cần chạy riêng form này)
     */
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