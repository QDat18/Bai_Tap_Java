package ui;

import dao.LoaiDAO;
import dao.SanPhamDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.Loai;
import model.NhanVien;
import model.SanPham;

/**
 * Giao diện người dùng cho chức năng Quản lý Sản phẩm.
 * Panel này hiển thị thông tin sản phẩm, cho phép quản lý (thêm, sửa, xóa)
 * và tìm kiếm. Tích hợp phân quyền cơ bản dựa trên vai trò của NhanVien đăng nhập.
 */
public class SanPhamUI extends JPanel {

    // Define colors (reusing the palette)
    Color coffeeBrown = new Color(102, 51, 0);
    Color lightBeige = new Color(245, 245, 220);
    Color accentGreen = new Color(60, 179, 113);
    Color accentOrange = new Color(255, 165, 0);
    Color accentBlue = new Color(30, 144, 255);
    Color darkGray = new Color(50, 50, 50);

    // UI Components (Input Fields)
    private JTextField txtMaSP;
    private JTextField txtTenSP;
    private JComboBox<Loai> cbLoai; // JComboBox for Loai
    private JTextField txtGianhap;
    private JTextField txtGiaban;
    private JTextField txtSoluong;
    private JTextField txtAnh; // Field to display image path
    private JButton btnChooseImage; // Button to choose image file
    private JLabel lblProductImagePreview; // Label to preview image

    // UI Components (Action Buttons)
    private JButton btnAdd;
    private JButton btnEdit;
    private JButton btnDelete;
    private JButton btnClear;

    // UI Components (Search)
    private JButton btnSearch;
    private JTextField txtSearch;
    private JComboBox<String> cbSearchCriteria;

    // UI Components (Table)
    private JTable productTable;
    private DefaultTableModel tableModel;

    // Data Access Objects
    private SanPhamDAO sanPhamDAO;
    private LoaiDAO loaiDAO; // LoaiDAO

    // Thông tin nhân viên đã đăng nhập - Changed from ACC to NhanVien
    private NhanVien loggedInUser;

    // Optional: Formatter for currency display in table
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0");


    /**
     * Constructor nhận thông tin nhân viên đã đăng nhập.
     *
     * @param user Đối tượng NhanVien của người dùng đã đăng nhập.
     */
    public SanPhamUI(NhanVien user) { // Constructor accepts NhanVien
        this.loggedInUser = user; // Store the NhanVien object

        // Initialize DAOs
        sanPhamDAO = new SanPhamDAO();
        loaiDAO = new LoaiDAO(); // Initialize LoaiDAO


        // Set layout for the main panel
        setLayout(new BorderLayout(15, 15));
        setBackground(lightBeige);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- Top Panel (Input, Search, and Buttons) ---
        JPanel topPanel = new JPanel(new BorderLayout(15, 0));
        topPanel.setBackground(lightBeige);

        // Panel to hold Input Fields and Image/Choose Button
        JPanel inputAndImagePanel = new JPanel(new GridBagLayout());
        inputAndImagePanel.setBackground(lightBeige);
        inputAndImagePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Thông tin Sản phẩm", 0, 0, new Font("Arial", Font.BOLD, 14), coffeeBrown));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // gbc.weightx = 1.0; // Set weightx individually below


        // Row 0: MaSP, TenSP
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; inputAndImagePanel.add(createLabel("Mã SP:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; txtMaSP = new JTextField(15); inputAndImagePanel.add(txtMaSP, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0; inputAndImagePanel.add(createLabel("Tên SP:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 1.0; txtTenSP = new JTextField(20); inputAndImagePanel.add(txtTenSP, gbc);

        // Row 1: Loai, Gianhap
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; inputAndImagePanel.add(createLabel("Loại SP:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        cbLoai = new JComboBox<>(); // Initialize JComboBox
        cbLoai.setBackground(Color.WHITE);
        cbLoai.setForeground(darkGray);
        inputAndImagePanel.add(cbLoai, gbc); // Add JComboBox

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0; inputAndImagePanel.add(createLabel("Giá nhập:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 1.0; txtGianhap = new JTextField(15); inputAndImagePanel.add(txtGianhap, gbc);


        // Row 2: Giaban, Soluong
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; inputAndImagePanel.add(createLabel("Giá bán:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0; txtGiaban = new JTextField(15); inputAndImagePanel.add(txtGiaban, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0; inputAndImagePanel.add(createLabel("Số lượng:"), gbc);
        gbc.gridx = 3; gbc.gridy = 2; gbc.weightx = 1.0; txtSoluong = new JTextField(15); inputAndImagePanel.add(txtSoluong, gbc);


        // Row 3: Image Path and Choose Button
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; inputAndImagePanel.add(createLabel("Ảnh (Đường dẫn):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0; txtAnh = new JTextField(25); inputAndImagePanel.add(txtAnh, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        gbc.gridx = 3; gbc.gridy = 3; gbc.weightx = 0;
        btnChooseImage = new JButton("Chọn ảnh");
        styleButton(btnChooseImage, darkGray, Color.WHITE);
        inputAndImagePanel.add(btnChooseImage, gbc);


        // Image Preview Label (optional, adjust position)
        lblProductImagePreview = new JLabel();
        lblProductImagePreview.setPreferredSize(new Dimension(100, 100)); // Set size for preview
        lblProductImagePreview.setBorder(BorderFactory.createLineBorder(darkGray));
        gbc.gridx = 4; // Place it in a new column
        gbc.gridy = 0;
        gbc.gridheight = 4; // Span multiple rows
        gbc.weightx = 0; // Don't let it grow horizontally
        gbc.fill = GridBagConstraints.NONE; // Don't fill cell
        gbc.anchor = GridBagConstraints.CENTER; // Center alignment
        inputAndImagePanel.add(lblProductImagePreview, gbc);
        gbc.gridheight = 1; // Reset gridheight
        gbc.fill = GridBagConstraints.HORIZONTAL; // Reset fill


        // --- Action Button Panel (Below Input Fields) ---
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        actionButtonPanel.setBackground(lightBeige);

        btnAdd = new JButton("Thêm");
        styleButton(btnAdd, accentGreen, Color.WHITE);
        actionButtonPanel.add(btnAdd);

        btnEdit = new JButton("Sửa");
        styleButton(btnEdit, coffeeBrown, Color.WHITE);
        actionButtonPanel.add(btnEdit);

        btnDelete = new JButton("Xóa");
        styleButton(btnDelete, accentOrange, Color.WHITE);
        actionButtonPanel.add(btnDelete);

        btnClear = new JButton("Làm mới");
        styleButton(btnClear, darkGray, Color.WHITE);
        actionButtonPanel.add(btnClear);


        // Combine Input Fields/Image and Action Buttons vertically
        JPanel inputImageAndButtonPanel = new JPanel(new BorderLayout());
        inputImageAndButtonPanel.setBackground(lightBeige);
        inputImageAndButtonPanel.add(inputAndImagePanel, BorderLayout.CENTER);
        inputImageAndButtonPanel.add(actionButtonPanel, BorderLayout.SOUTH);


        // --- Search Panel ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(lightBeige);
        searchPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Tìm kiếm Sản phẩm", 0, 0, new Font("Arial", Font.BOLD, 14), coffeeBrown));

        searchPanel.add(createLabel("Tìm theo:"));
        String[] searchOptions = {"Tên SP", "Mã SP", "Loại SP"}; // Search criteria
        cbSearchCriteria = new JComboBox<>(searchOptions);
        cbSearchCriteria.setBackground(Color.WHITE);
        cbSearchCriteria.setForeground(darkGray);
        searchPanel.add(cbSearchCriteria);

        searchPanel.add(createLabel("Từ khóa:"));
        txtSearch = new JTextField(15);
        searchPanel.add(txtSearch);

        btnSearch = new JButton("Tìm kiếm");
        styleButton(btnSearch, coffeeBrown, Color.WHITE);
        searchPanel.add(btnSearch);

        // Add combined input/button panel and search panel to the top panel
        topPanel.add(inputImageAndButtonPanel, BorderLayout.CENTER);
        topPanel.add(searchPanel, BorderLayout.SOUTH);


        add(topPanel, BorderLayout.NORTH);


        // --- Table Panel ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(lightBeige);
        tablePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Danh sách Sản phẩm", 0, 0, new Font("Arial", Font.BOLD, 14), coffeeBrown));

        // Table Model: Columns for SanPham
        tableModel = new DefaultTableModel(new Object[]{"Mã SP", "Tên SP", "Mã loại", "Giá nhập", "Giá bán", "Số lượng", "Ảnh"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
            // Optional: Specify column types if needed for rendering (e.g., for Integer)
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3 || columnIndex == 4 || columnIndex == 5) return Integer.class; // Assuming prices and quantity are Integer columns
                // if (columnIndex == 6) return ImageIcon.class; // If you want to display images directly in the table
                return super.getColumnClass(columnIndex);
            }
        };
        productTable = new JTable(tableModel);

        // Style table header and cells
        productTable.getTableHeader().setBackground(coffeeBrown);
        productTable.getTableHeader().setForeground(Color.WHITE);
        productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(230, 230, 230));
                if (isSelected) {
                    c.setBackground(new Color(180, 210, 230));
                }
                // Right align numeric columns (prices, quantity)
                if (column == 3 || column == 4 || column == 5) { // Assuming price/quantity are columns 3, 4, 5
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    // Optional: Format numeric values with DecimalFormat
                     if (value instanceof Number) {
                         setText(currencyFormat.format(value)); // Format number
                     }
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        };
        // Apply the renderer to all columns (or specific columns)
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
             // Apply custom renderer only to numeric columns, default to left for others
             if (i == 3 || i == 4 || i == 5) {
                 productTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
             } else {
                 DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
                 leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
                 productTable.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
             }
        }


        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(productTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(tablePanel, BorderLayout.CENTER);


        // --- Event Listeners ---

        btnAdd.addActionListener(e -> addSanPham());
        btnEdit.addActionListener(e -> updateSanPham());
        btnDelete.addActionListener(e -> deleteSanPham());
        btnClear.addActionListener(e -> clearInputFields());
        btnSearch.addActionListener(e -> searchSanPham());
        btnChooseImage.addActionListener(e -> chooseImageFile()); // Listener for choose image button

        // Table row selection to fill fields and enable/disable buttons
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Only fill fields and apply permissions if a valid row is selected
                if (productTable.getSelectedRow() >= 0) {
                    fillInputFieldsFromTable();
                    // Enable/disable edit/delete buttons based on selection and permissions
                    applyRolePermissions(); // Re-apply permissions after selection
                } else {
                    // If selection is cleared, re-apply permissions to disable Edit/Delete
                    applyRolePermissions();
                    clearInputFields(); // Clear fields if no row is selected
                }
            }
            // Handle double click to show details dialog
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getClickCount() == 2) { // Check for double click
                    int row = productTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        // Get MaSP from the selected row
                        String maSP = tableModel.getValueAt(row, 0).toString();
                        // Need a method getSanPhamById in your SanPhamDAO
                        SanPham sp = sanPhamDAO.getSanPhamById(maSP); // Fetch full SanPham object
                        if (sp != null) {
                            // Call a helper method to show dialog
                            showProductDetailsDialog(sp);
                            System.out.println("Double clicked on " + sp.getTenSP());
                        } else {
                            JOptionPane.showMessageDialog(SanPhamUI.this, "Không tìm thấy thông tin chi tiết sản phẩm.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });


        // Load initial data
        loadLoai(); // Load categories into the JComboBox
        loadProductTable(); // Load product data into the table

        // Apply permissions on initial load
        applyRolePermissions(); // Apply permissions when UI is created
    }

    /**
     * Helper method to create a styled JLabel.
     * @param text The text for the label.
     * @return The styled JLabel.
     */
     private JLabel createLabel(String text) {
         JLabel label = new JLabel(text);
         label.setForeground(darkGray);
         label.setFont(new Font("Arial", Font.BOLD, 12));
         return label;
     }

    /**
     * Helper method to style generic buttons.
     * @param button The button to style.
     * @param bgColor The background color.
     * @param fgColor The foreground color.
     */
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


    /**
     * Helper method to load categories into the JComboBox.
     * Assumes LoaiDAO.getAllLoai() returns List<Loai> and Loai has getMaloai() and getTenloai().
     */
    private void loadLoai() {
        cbLoai.removeAllItems(); // Clear existing items
        // You might want to add a default "Select Category" item here if applicable
        // cbLoai.addItem(new Loai("", "-- Chọn loại --")); // Example placeholder item

        List<Loai> loaiList = loaiDAO.getAllLoai(); // Assuming getAllLoai exists and returns List<Loai>
        if (loaiList != null) {
            for (Loai loai : loaiList) {
                cbLoai.addItem(loai); // Add each category to the combobox (requires Loai's toString() or custom renderer)
            }
        } else {
            System.out.println("Không lấy được danh sách loại sản phẩm từ CSDL.");
            JOptionPane.showMessageDialog(this, "Không thể tải danh sách loại sản phẩm từ CSDL.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            // Optional: Handle error loading categories (e.g., disable Add/Edit if categories are required)
             txtTenSP.setEnabled(false);
             cbLoai.setEnabled(false);
             txtGianhap.setEnabled(false);
             txtGiaban.setEnabled(false);
             txtSoluong.setEnabled(false);
             txtAnh.setEnabled(false);
             btnChooseImage.setEnabled(false);
             btnAdd.setEnabled(false);
             btnEdit.setEnabled(false);
        }
        // Set renderer to display TenLoai
        cbLoai.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Loai) {
                    setText(((Loai) value).getTenloai()); // Display TenLoai
                } else if (value == null) {
                    setText("-- Chọn Loại SP --"); // Placeholder text for null item if added
                } else {
                    setText(""); // Handle other cases
                }
                return this;
            }
        });
        // Optional: Select the placeholder item initially if added
        // cbLoai.setSelectedItem(null);
    }

    /**
     * Helper method to choose an image file using JFileChooser.
     */
    private void chooseImageFile() {
        System.out.println("Button 'Chọn ảnh' clicked."); // Debug print

        JFileChooser fileChooser = new JFileChooser();
        // Set the current directory (optional, helps user navigate)
        // fileChooser.setCurrentDirectory(new File(".")); // Set to current project directory
        // Or set to a specific known directory:
        // fileChooser.setCurrentDirectory(new File("D://KyIV_HocVienNganHang//WebDesign//BTL_web//assets//")); // Example path

        // Set a filter to only show image files
        // Note: ImageIO.getReaderFileSuffixes() gets supported formats like jpg, png, gif, bmp
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
        fileChooser.setFileFilter(filter);

        fileChooser.setDialogTitle("Chọn ảnh sản phẩm");

        try {
            System.out.println("Showing JFileChooser dialog..."); // Debug print
            int result = fileChooser.showOpenDialog(this); // Show open dialog relative to this panel
            System.out.println("JFileChooser dialog closed with result: " + result); // Debug print

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    String imagePath = selectedFile.getAbsolutePath();
                    System.out.println("Selected file: " + imagePath);
                    txtAnh.setText(imagePath);
                    loadAndDisplayImagePreview(imagePath);
                } else {
                    System.out.println("Selected file is null."); // Debug print
                }
            } else if (result == JFileChooser.CANCEL_OPTION) {
                System.out.println("File selection cancelled by user."); // Debug print
            } else {
                System.out.println("File chooser returned an unexpected result: " + result); // Debug print
            }
        } catch (Exception e) {
            System.err.println("Exception occurred while trying to show or process JFileChooser:");
            e.printStackTrace(); // Print the full stack trace to the console
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi mở hộp thoại chọn ảnh.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    /**
     * Helper method to load and display image preview from a file path.
     * @param imagePath The file path to the image.
     */
    private void loadAndDisplayImagePreview(String imagePath) {
        lblProductImagePreview.setIcon(null); // Clear previous icon
        lblProductImagePreview.setText(""); // Clear previous text
        lblProductImagePreview.setHorizontalAlignment(SwingConstants.CENTER); // Center text/image
        lblProductImagePreview.setVerticalAlignment(SwingConstants.CENTER); // Center text/image


        if (imagePath == null || imagePath.trim().isEmpty()) {
            // Display a default "no image" state if path is empty
            lblProductImagePreview.setText("Không có ảnh");
            lblProductImagePreview.setForeground(darkGray);
            lblProductImagePreview.setFont(new Font("Arial", Font.PLAIN, 10));
            return;
        }

        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                BufferedImage originalImage = ImageIO.read(imageFile);
                if (originalImage != null) {
                    // Scale the image to fit the preview label size while maintaining aspect ratio
                    int labelWidth = lblProductImagePreview.getPreferredSize().width;
                    int labelHeight = lblProductImagePreview.getPreferredSize().height;
                    // Calculate scale factor
                    double scaleX = (double) labelWidth / originalImage.getWidth();
                    double scaleY = (double) labelHeight / originalImage.getHeight();
                    double scale = Math.min(scaleX, scaleY);

                    int scaledWidth = (int) (originalImage.getWidth() * scale);
                    int scaledHeight = (int) (originalImage.getHeight() * scale);

                    Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                    lblProductImagePreview.setIcon(new ImageIcon(scaledImage));
                    lblProductImagePreview.setText(""); // Clear text if image loaded
                } else {
                    // Handle case where file exists but is not a valid image
                    lblProductImagePreview.setText("Định dạng ảnh lỗi");
                    lblProductImagePreview.setForeground(Color.RED);
                    lblProductImagePreview.setFont(new Font("Arial", Font.PLAIN, 10));
                }
            } else {
                // Handle case where the file path does not exist
                lblProductImagePreview.setText("Không tìm thấy ảnh");
                lblProductImagePreview.setForeground(darkGray);
                lblProductImagePreview.setFont(new Font("Arial", Font.PLAIN, 10));
            }
        } catch (Exception e) {
            System.err.println("Error loading image preview from path: " + imagePath + " - " + e.getMessage());
            e.printStackTrace();
            // Handle generic errors during loading
            lblProductImagePreview.setText("Lỗi tải ảnh");
            lblProductImagePreview.setForeground(Color.RED);
            lblProductImagePreview.setFont(new Font("Arial", Font.PLAIN, 10));
        }
    }


    /**
     * Applies role-based permissions to UI components (buttons, input fields).
     * Assumes loggedInUser is set.
     */
    private void applyRolePermissions() {
        // Mặc định vô hiệu hóa các nút Thêm, Sửa, Xóa và các trường nhập liệu
        btnAdd.setEnabled(false);
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);
        setAllInputFieldsEnabled(false);
        txtMaSP.setEnabled(false); // Mã SP chỉ cho phép chỉnh sửa khi thêm mới
        btnChooseImage.setEnabled(false); // Disable choose image button by default

        // Nút Làm mới (Clear) và Tìm kiếm (Search) thường luôn được phép
        btnClear.setEnabled(true);
        btnSearch.setEnabled(true);
        txtSearch.setEnabled(true);
        cbSearchCriteria.setEnabled(true);
        productTable.setEnabled(true); // Table should always be viewable


        // Use loggedInUser instead of loggedInAccount
        if (loggedInUser == null) {
            // Không có tài khoản đăng nhập
            return;
        }

        String role = loggedInUser.getRole(); // Lấy vai trò từ NhanVien

        // Logic phân quyền cho module Quản lý Sản phẩm
        // Ví dụ: Admin và Manager có toàn quyền, Staff chỉ được xem và thêm (hoặc chỉ xem)
        if ("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role)) {
            // Admin và Manager có toàn quyền Thêm, Sửa, Xóa
            btnAdd.setEnabled(true);
            // Nút Sửa/Xóa chỉ được bật khi có dòng trên bảng được chọn
            btnEdit.setEnabled(productTable.getSelectedRow() >= 0);
            btnDelete.setEnabled(productTable.getSelectedRow() >= 0);

            setAllInputFieldsEnabled(true); // Có thể chỉnh sửa tất cả các trường
            btnChooseImage.setEnabled(true); // Có quyền chọn ảnh

            // Mã SP chỉ cho phép chỉnh sửa khi thêm mới (không có dòng nào được chọn)
            txtMaSP.setEnabled(productTable.getSelectedRow() < 0);

        } else if ("Staff".equalsIgnoreCase(role)) {
            // Staff có thể xem và có thể có quyền Thêm
            btnAdd.setEnabled(true); // Staff có thể thêm SP mới? (Tùy quy định)
            btnEdit.setEnabled(false); // Staff không sửa/xóa
            btnDelete.setEnabled(false);

            setAllInputFieldsEnabled(true); // Có thể nhập thông tin khi thêm mới
            txtMaSP.setEnabled(true); // Có thể nhập mã SP khi thêm mới
            btnChooseImage.setEnabled(true); // Có thể chọn ảnh khi thêm mới? (Tùy quy định)

            // Nếu có dòng được chọn (chỉ để xem), vô hiệu hóa các trường nhập liệu
            if (productTable.getSelectedRow() >= 0) {
                 setAllInputFieldsEnabled(false);
                 txtMaSP.setEnabled(false); // Mã SP luôn disabled khi xem
                 btnChooseImage.setEnabled(false); // Không chọn ảnh khi xem
            }

        } else {
            // Các vai trò khác (Guest, ...) chỉ được xem (nếu có module xem riêng)
            // Các nút và trường sẽ giữ trạng thái disabled ban đầu
            // Search and Clear are already enabled by default
        }
    }

    /**
     * Helper method to enable/disable all input fields.
     * @param enabled true to enable, false to disable.
     */
    private void setAllInputFieldsEnabled(boolean enabled) {
        // Exclude txtMaSP as its enabled state depends on row selection in applyRolePermissions
        txtTenSP.setEnabled(enabled);
        cbLoai.setEnabled(enabled); // Enable/disable JComboBox
        txtGianhap.setEnabled(enabled);
        txtGiaban.setEnabled(enabled);
        txtSoluong.setEnabled(enabled);
        txtAnh.setEnabled(enabled); // Enable/disable image path field
        // Note: btnChooseImage and txtMaSP are handled separately in applyRolePermissions
    }


    // --- CRUD Operations ---

    /**
     * Loads all SanPham data from the DAO and populates the product table.
     */
    private void loadProductTable() {
        tableModel.setRowCount(0); // Clear existing data
        // Assuming getAllSanPham exists and returns List<SanPham> with Maloai and Anh
        List<SanPham> danhSach = sanPhamDAO.getAllSanPham();
        if (danhSach != null) {
            for (SanPham sp : danhSach) {
                tableModel.addRow(new Object[]{
                    sp.getMaSP(),
                    sp.getTenSP(),
                    sp.getMaloai(), // Display Maloai in table (or Tenloai if you join with Loai table in DAO)
                    sp.getGianhap(),
                    sp.getGiaban(),
                    sp.getSoluong(),
                    sp.getAnh() // Display image path
                });
            }
        } else {
            System.out.println("Không lấy được dữ liệu sản phẩm từ CSDL.");
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu sản phẩm từ CSDL.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            // Optional: Add some placeholder data if DAO returns null or empty list
            // tableModel.addRow(new Object[]{"SP001", "Sample Product", "CF", 10000, 15000, 50, "path/to/image.jpg"});
        }

        // After loading table, re-apply permissions to ensure edit/delete buttons are correctly enabled/disabled
        applyRolePermissions();
        clearInputFields(); // Clear input fields after loading
        productTable.clearSelection(); // Clear table selection
    }

    /**
     * Fills the input fields with data from the selected row in the product table.
     */
    private void fillInputFieldsFromTable() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0) {
            // Get data from the selected row
            String maSP = tableModel.getValueAt(selectedRow, 0).toString();
            String tenSP = tableModel.getValueAt(selectedRow, 1).toString();
            String maloai = tableModel.getValueAt(selectedRow, 2).toString(); // Get Maloai from table
            String gianhap = tableModel.getValueAt(selectedRow, 3).toString();
            String giaban = tableModel.getValueAt(selectedRow, 4).toString();
            String soluong = tableModel.getValueAt(selectedRow, 5).toString();
            String anh = tableModel.getValueAt(selectedRow, 6) != null ? tableModel.getValueAt(selectedRow, 6).toString() : ""; // Handle null image path


            // Set data to input fields
            txtMaSP.setText(maSP);
            txtTenSP.setText(tenSP);
            // Select the corresponding Loai in the JComboBox based on Maloai
            selectLoaiInComboBox(maloai); // <-- Call helper method to select Loai
            txtGianhap.setText(gianhap);
            txtGiaban.setText(giaban);
            txtSoluong.setText(soluong);
            txtAnh.setText(anh);

            // Load and display image preview
            loadAndDisplayImagePreview(anh);

            // MaSP should not be editable when a row is selected for editing/deleting
            txtMaSP.setEnabled(false);
        } else {
            // If no row is selected, clear fields and enable MaSP for new entry
            clearInputFields();
            txtMaSP.setEnabled(true);
        }
    }

    /**
     * Helper method to select a Loai in the JComboBox based on Maloai.
     * @param maloai The Maloai to select.
     */
    private void selectLoaiInComboBox(String maloai) {
        if (maloai == null || maloai.isEmpty()) {
            cbLoai.setSelectedItem(null); // Select no item or placeholder
            return;
        }
        for (int i = 0; i < cbLoai.getItemCount(); i++) {
            Loai loai = cbLoai.getItemAt(i);
            if (loai != null && loai.getMaloai() != null && loai.getMaloai().equals(maloai)) {
                cbLoai.setSelectedItem(loai);
                return;
            }
        }
        // If Maloai from table is not found in the combobox list
        System.err.println("Mã loại '" + maloai + "' từ bảng không tìm thấy trong danh sách combobox.");
        cbLoai.setSelectedItem(null); // Select no item or placeholder
    }


    /**
     * Handles the Add SanPham action.
     * Validates input, creates a SanPham object, and calls the DAO to add it.
     */
    private void addSanPham() {
        // Check permissions before performing action - Use loggedInUser
        if (loggedInUser == null || (!"Admin".equalsIgnoreCase(loggedInUser.getRole()) && !"Manager".equalsIgnoreCase(loggedInUser.getRole()) && !"Staff".equalsIgnoreCase(loggedInUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền thêm sản phẩm.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String maSP = txtMaSP.getText().trim();
        String tenSP = txtTenSP.getText().trim();
        Loai selectedLoai = (Loai) cbLoai.getSelectedItem();
        String maloai = (selectedLoai != null) ? selectedLoai.getMaloai() : null;

        String gianhapStr = txtGianhap.getText().trim();
        String giabanStr = txtGiaban.getText().trim();
        String soluongStr = txtSoluong.getText().trim();
        String anh = txtAnh.getText().trim();


        // Basic validation
        if (maSP.isEmpty() || tenSP.isEmpty() || maloai == null || maloai.isEmpty() || gianhapStr.isEmpty() || giabanStr.isEmpty() || soluongStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin bắt buộc (Mã SP, Tên SP, Loại SP, Giá nhập, Giá bán, Số lượng).", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Validate numeric fields
        int gianhap, giaban, soluong;
        try {
            gianhap = Integer.parseInt(gianhapStr);
            if (gianhap < 0) throw new NumberFormatException("Giá nhập phải là số nguyên không âm.");
            giaban = Integer.parseInt(giabanStr);
            if (giaban < 0) throw new NumberFormatException("Giá bán phải là số nguyên không âm.");
            soluong = Integer.parseInt(soluongStr);
            if (soluong < 0) throw new NumberFormatException("Số lượng phải là số nguyên không âm.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Định dạng số không hợp lệ: " + e.getMessage(), "Lỗi Nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create SanPham object
        SanPham newSanPham = new SanPham(maSP, tenSP, maloai, gianhap, giaban, soluong, anh);

        // Call DAO to add SanPham
        boolean success = sanPhamDAO.addSanPham(newSanPham); // Assuming addSanPham returns boolean

        if (success) {
            JOptionPane.showMessageDialog(this, "Thêm sản phẩm thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadProductTable(); // Refresh table
            clearInputFields(); // Clear input fields
            applyRolePermissions(); // Re-apply permissions (esp. for MaSP field)
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi thêm sản phẩm. Có thể Mã SP đã tồn tại hoặc lỗi CSDL.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles the Update SanPham action.
     * Validates input, creates a SanPham object, and calls the DAO to update it.
     */
    private void updateSanPham() {
        // Check permissions before performing action - Use loggedInUser
        if (loggedInUser == null || (!"Admin".equalsIgnoreCase(loggedInUser.getRole()) && !"Manager".equalsIgnoreCase(loggedInUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền sửa sản phẩm.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần sửa từ bảng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maSP = txtMaSP.getText().trim(); // Mã SP from field (should be same as selected row)
        String tenSP = txtTenSP.getText().trim();
        Loai selectedLoai = (Loai) cbLoai.getSelectedItem();
        String maloai = (selectedLoai != null) ? selectedLoai.getMaloai() : null;

        String gianhapStr = txtGianhap.getText().trim();
        String giabanStr = txtGiaban.getText().trim();
        String soluongStr = txtSoluong.getText().trim();
        String anh = txtAnh.getText().trim();

        // Basic validation (similar to add, but MaSP is not checked for existence)
        if (maSP.isEmpty() || tenSP.isEmpty() || maloai == null || maloai.isEmpty() || gianhapStr.isEmpty() || giabanStr.isEmpty() || soluongStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin bắt buộc (Tên SP, Loại SP, Giá nhập, Giá bán, Số lượng).", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        // Validate numeric fields
        int gianhap, giaban, soluong;
        try {
            gianhap = Integer.parseInt(gianhapStr);
            if (gianhap < 0) throw new NumberFormatException("Giá nhập phải là số nguyên không âm.");
            giaban = Integer.parseInt(giabanStr);
            if (giaban < 0) throw new NumberFormatException("Giá bán phải là số nguyên không âm.");
            soluong = Integer.parseInt(soluongStr);
            if (soluong < 0) throw new NumberFormatException("Số lượng phải là số nguyên không âm.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Định dạng số không hợp lệ: " + e.getMessage(), "Lỗi Nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create SanPham object with updated information
        SanPham updatedSanPham = new SanPham(maSP, tenSP, maloai, gianhap, giaban, soluong, anh);

        // Call DAO to update SanPham
        boolean success = sanPhamDAO.updateSanPham(updatedSanPham); // Assuming updateSanPham returns boolean

        if (success) {
            JOptionPane.showMessageDialog(this, "Cập nhật sản phẩm thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadProductTable(); // Refresh table
            clearInputFields(); // Clear input fields
            applyRolePermissions(); // Re-apply permissions
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật sản phẩm.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles the Delete SanPham action.
     * Gets the selected SanPham and calls the DAO to delete it.
     */
    private void deleteSanPham() {
        // Check permissions before performing action - Use loggedInUser
        if (loggedInUser == null || (!"Admin".equalsIgnoreCase(loggedInUser.getRole()) && !"Manager".equalsIgnoreCase(loggedInUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền xóa sản phẩm.", "Lỗi Phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sản phẩm cần xóa từ bảng.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maSP = tableModel.getValueAt(selectedRow, 0).toString(); // Get MaSP from selected row
        String tenSP = tableModel.getValueAt(selectedRow, 1).toString(); // Get TenSP for confirmation message

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa sản phẩm '" + tenSP + "' (Mã: " + maSP + ")?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            // Call DAO to delete SanPham
            boolean success = sanPhamDAO.deleteSanPham(maSP); // Assuming deleteSanPham returns boolean

            if (success) {
                JOptionPane.showMessageDialog(this, "Xóa sản phẩm thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadProductTable(); // Refresh table
                clearInputFields(); // Clear input fields
                applyRolePermissions(); // Re-apply permissions
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xóa sản phẩm. Sản phẩm có thể đang được sử dụng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Handles the Search SanPham action.
     * Gets search criteria and keyword, and calls the DAO to search.
     */
    private void searchSanPham() {
         // Search is generally allowed for all roles who can view the panel
         // No explicit role check needed here if the button is enabled by applyRolePermissions

        String criteria = (String) cbSearchCriteria.getSelectedItem();
        String keyword = txtSearch.getText().trim();

        if (keyword.isEmpty()) {
            // If search keyword is empty, load all products
            loadProductTable();
            return;
        }

        List<SanPham> searchResults = new ArrayList<>();
        // Call appropriate search method in DAO based on criteria
        switch (criteria) {
            case "Tên SP":
                // Assuming searchSanPhamByName exists in SanPhamDAO
                searchResults = sanPhamDAO.searchSanPhamByName(keyword);
                break;
            case "Mã SP":
                // Assuming searchSanPhamById exists in SanPhamDAO
                // Note: Searching by ID might return at most one result
                 SanPham spById = sanPhamDAO.getSanPhamById(keyword); // Assuming getSanPhamById returns SanPham
                 if (spById != null) {
                     searchResults.add(spById);
                 }
                break;
            case "Loai SP":
                // Assuming searchSanPhamByLoaiName or searchSanPhamByMaLoai exists in SanPhamDAO
                // You might need to get Maloai from LoaiDAO based on keyword (TenLoai)
                 String maLoaiToSearch = null;
                 List<Loai> loaiList = loaiDAO.getAllLoai(); // Get all categories to find matching Maloai
                 if (loaiList != null) {
                     for (Loai loai : loaiList) {
                         if (loai.getTenloai() != null && loai.getTenloai().equalsIgnoreCase(keyword)) {
                             maLoaiToSearch = loai.getMaloai();
                             break;
                         }
                     }
                 }
                 if (maLoaiToSearch != null) {
                     // Assuming searchSanPhamByMaLoai exists in SanPhamDAO
                     searchResults = sanPhamDAO.searchSanPhamByMaLoai(maLoaiToSearch);
                 } else {
                     // If no matching category found
                     JOptionPane.showMessageDialog(this, "Không tìm thấy loại sản phẩm với tên '" + keyword + "'.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                     tableModel.setRowCount(0); // Clear table if no category found
                     return;
                 }
                break;
            default:
                System.err.println("Unknown search criteria: " + criteria);
                JOptionPane.showMessageDialog(this, "Tiêu chí tìm kiếm không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
        }

        // Display search results in the table
        tableModel.setRowCount(0); // Clear existing data
        if (searchResults != null && !searchResults.isEmpty()) {
            for (SanPham sp : searchResults) {
                 tableModel.addRow(new Object[]{
                    sp.getMaSP(),
                    sp.getTenSP(),
                    sp.getMaloai(),
                    sp.getGianhap(),
                    sp.getGiaban(),
                    sp.getSoluong(),
                    sp.getAnh()
                 });
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm phù hợp với từ khóa '" + keyword + "'.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }

        // After search, clear input fields and selection, re-apply permissions
        clearInputFields();
        productTable.clearSelection();
        applyRolePermissions(); // Ensure buttons are disabled after search
    }


    /**
     * Clears all input fields and resets buttons/table selection.
     */
    private void clearInputFields() {
        txtMaSP.setText("");
        txtTenSP.setText("");
        cbLoai.setSelectedItem(null); // Clear combobox selection
        txtGianhap.setText("");
        txtGiaban.setText("");
        txtSoluong.setText("");
        txtAnh.setText("");
        lblProductImagePreview.setIcon(null); // Clear image preview
        lblProductImagePreview.setText("Không có ảnh"); // Reset preview text
        lblProductImagePreview.setForeground(darkGray);
        lblProductImagePreview.setFont(new Font("Arial", Font.PLAIN, 10));

        productTable.clearSelection(); // Clear table selection
        applyRolePermissions(); // Re-apply permissions (MaSP should be enabled for new entry)
    }

    /**
     * Shows a dialog with detailed information about a specific product.
     * @param sp The SanPham object to display details for.
     */
    private void showProductDetailsDialog(SanPham sp) {
        if (sp == null) return;

        JDialog detailsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết Sản phẩm: " + sp.getTenSP(), true); // Modal dialog
        detailsDialog.setLayout(new BorderLayout(10, 10));
        detailsDialog.setSize(400, 400);
        detailsDialog.setLocationRelativeTo(this); // Center relative to SanPhamUI panel

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; infoPanel.add(createLabel("Mã SP:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; infoPanel.add(new JLabel(sp.getMaSP()), gbc);

        gbc.gridx = 0; gbc.gridy = row; infoPanel.add(createLabel("Tên SP:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; infoPanel.add(new JLabel(sp.getTenSP()), gbc);

        // Display TenLoai instead of Maloai in details
        String tenLoai = "N/A";
        Loai loai = loaiDAO.getLoaiById(sp.getMaloai()); // Assuming getLoaiById exists in LoaiDAO
        if (loai != null) {
            tenLoai = loai.getTenloai();
        }
        gbc.gridx = 0; gbc.gridy = row; infoPanel.add(createLabel("Loại SP:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; infoPanel.add(new JLabel(tenLoai), gbc);


        gbc.gridx = 0; gbc.gridy = row; infoPanel.add(createLabel("Giá nhập:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; infoPanel.add(new JLabel(currencyFormat.format(sp.getGianhap())), gbc); // Format currency

        gbc.gridx = 0; gbc.gridy = row; infoPanel.add(createLabel("Giá bán:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; infoPanel.add(new JLabel(currencyFormat.format(sp.getGiaban())), gbc); // Format currency

        gbc.gridx = 0; gbc.gridy = row; infoPanel.add(createLabel("Số lượng:"), gbc);
        gbc.gridx = 1; gbc.gridy = row++; infoPanel.add(new JLabel(String.valueOf(sp.getSoluong())), gbc);

        // Image preview in dialog
        JLabel dialogImagePreview = new JLabel();
        dialogImagePreview.setPreferredSize(new Dimension(150, 150)); // Larger preview in dialog
        dialogImagePreview.setBorder(BorderFactory.createLineBorder(darkGray));
        loadAndDisplayImagePreviewInLabel(sp.getAnh(), dialogImagePreview, 150, 150); // Load image into this label

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; infoPanel.add(createLabel("Ảnh:"), gbc);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER; infoPanel.add(dialogImagePreview, gbc);


        detailsDialog.add(infoPanel, BorderLayout.CENTER);

        // Close button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Đóng");
        styleButton(closeButton, darkGray, Color.WHITE);
        closeButton.addActionListener(e -> detailsDialog.dispose());
        buttonPanel.add(closeButton);
        detailsDialog.add(buttonPanel, BorderLayout.SOUTH);


        detailsDialog.setVisible(true); // Show the dialog
    }

    /**
     * Helper method to load and display image preview in a specific JLabel with specified size.
     * Useful for dialogs or other custom previews.
     * @param imagePath The file path to the image.
     * @param targetLabel The JLabel to display the image in.
     * @param width The target width for the scaled image.
     * @param height The target height for the scaled image.
     */
     private void loadAndDisplayImagePreviewInLabel(String imagePath, JLabel targetLabel, int width, int height) {
         targetLabel.setIcon(null); // Clear previous icon
         targetLabel.setText(""); // Clear previous text
         targetLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center text/image
         targetLabel.setVerticalAlignment(SwingConstants.CENTER); // Center text/image


         if (imagePath == null || imagePath.trim().isEmpty()) {
             targetLabel.setText("Không có ảnh");
             targetLabel.setForeground(darkGray);
             targetLabel.setFont(new Font("Arial", Font.PLAIN, 10));
             return;
         }

         try {
             File imageFile = new File(imagePath);
             if (imageFile.exists()) {
                 BufferedImage originalImage = ImageIO.read(imageFile);
                 if (originalImage != null) {
                     // Scale the image to fit the target size while maintaining aspect ratio
                     double scaleX = (double) width / originalImage.getWidth();
                     double scaleY = (double) height / originalImage.getHeight();
                     double scale = Math.min(scaleX, scaleY);

                     int scaledWidth = (int) (originalImage.getWidth() * scale);
                     int scaledHeight = (int) (originalImage.getHeight() * scale);

                     Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                     targetLabel.setIcon(new ImageIcon(scaledImage));
                     targetLabel.setText(""); // Clear text if image loaded
                 } else {
                     targetLabel.setText("Định dạng ảnh lỗi");
                     targetLabel.setForeground(Color.RED);
                     targetLabel.setFont(new Font("Arial", Font.PLAIN, 10));
                 }
             } else {
                 targetLabel.setText("Không tìm thấy ảnh");
                 targetLabel.setForeground(darkGray);
                 targetLabel.setFont(new Font("Arial", Font.PLAIN, 10));
             }
         } catch (Exception e) {
             System.err.println("Error loading image preview from path: " + imagePath + " - " + e.getMessage());
             e.printStackTrace();
             targetLabel.setText("Lỗi tải ảnh");
             targetLabel.setForeground(Color.RED);
             targetLabel.setFont(new Font("Arial", Font.PLAIN, 10));
         }
     }


    // Main method for testing (Optional - typically handled by MainApplicationFrame)

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Quản lý Sản phẩm Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            
            // Create a dummy NhanVien user for testing permissions
            NhanVien dummyAdmin = new NhanVien("NV01", "admin", "pass", "Nam", "adminn","123", "admin@example.com", "Admin");
            // NhanVien dummyManager = new NhanVien("NV002", "manager", "pass", "Manager User", "Manager", "0987654321", "manager@example.com", "Address", true);
            // // NhanVien dummyStaff = new NhanVien("NV003", "staff", "pass", "Staff User", "Staff", "0111222333", "staff@example.com", "Address", true);
            // NhanVien dummyGuest = new NhanVien(null, null, null, "Guest User", "Guest", null, null, null, false); // Example Guest NhanVien

            // Pass the dummy user to the SanPhamUI constructor
            SanPhamUI sanPhamPanel = new SanPhamUI(dummyAdmin); // Change dummy user here

            frame.add(sanPhamPanel);
            frame.setVisible(true);
        });
    }

}
