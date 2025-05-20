package ui;

import dao.LoaiDAO;
import dao.SanPhamDAO;
import model.Loai;
import model.NhanVien;
import model.SanPham;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

public class EditProductDialog extends JDialog {

    // Colors
    private final Color primaryColor = new Color(101, 67, 33); // Coffee brown
    private final Color secondaryColor = new Color(245, 245, 220); // Light beige
    private final Color textColor = new Color(51, 51, 51); // Dark gray
    private final Color accentGreen = new Color(76, 175, 80); // Green for confirmation
    private final Color accentRed = new Color(244, 67, 54); // Red for cancel/errors
    private final Color accentBlue = new Color(33, 150, 243); // Blue for actions

    // Fields
    private JTextField txtMaSP;
    private JTextField txtTenSP;
    private JComboBox<Loai> cbLoai;
    private JTextField txtGiaMua;
    private JTextField txtGiaBan;
    private JTextField txtSoLuong;
    private JLabel lblImagePreview;
    private JButton btnSelectImage;
    
    private File selectedImageFile;
    private String imagePath = "";
    private boolean isImageChanged = false;
    
    // DAOs
    private SanPhamDAO sanPhamDAO;
    private LoaiDAO loaiDAO;
    
    // Logged in user
    private NhanVien loggedInUser;
    
    // Product to edit
    private SanPham productToEdit;
    
    // Result tracking
    private boolean updateSuccess = false;
    private SanPham updatedProduct = null;

    /**
     * Constructor for editing an existing product
     */
    public EditProductDialog(JFrame parent, SanPham productToEdit, NhanVien loggedInUser) {
        super(parent, "Chỉnh Sửa Sản Phẩm", true);
        this.loggedInUser = loggedInUser;
        this.productToEdit = productToEdit;
        this.imagePath = productToEdit.getAnh() != null ? productToEdit.getAnh().toString() : "";
        
        // Initialize DAOs
        sanPhamDAO = new SanPhamDAO();
        loaiDAO = new LoaiDAO();
        
        initUI();
        loadCategories();
        loadProductData();
    }
    
    private void initUI() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(getOwner());
        getContentPane().setBackground(secondaryColor);
        setLayout(new BorderLayout(10, 10));
        
        // Main panel with form fields
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(secondaryColor);
        mainPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        
        // Form panel
        JPanel formPanel = createFormPanel();
        
        // Image panel
        JPanel imagePanel = createImagePanel();
        
        // Split main panel
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(imagePanel, BorderLayout.EAST);
        
        // Buttons panel
        JPanel buttonsPanel = createButtonsPanel();
        
        // Add panels to dialog
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
        
        // Add window listener to confirm before closing if changes were made
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmClose();
            }
        });
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(primaryColor);
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Chỉnh Sửa Sản Phẩm");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel userLabel = new JLabel("Nhân viên: " + loggedInUser.getTenNV());
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);
        
        headerPanel.add(userLabel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(secondaryColor);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor),
                "Thông tin sản phẩm",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), primaryColor));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Product ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Mã sản phẩm:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.7;
        txtMaSP = new JTextField(20);
        txtMaSP.setEditable(false);
        txtMaSP.setBackground(new Color(240, 240, 240));
        formPanel.add(txtMaSP, gbc);
        
        // Product Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Tên sản phẩm:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.7;
        txtTenSP = new JTextField(20);
        formPanel.add(txtTenSP, gbc);
        
        // Category
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Loại sản phẩm:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0.7;
        cbLoai = new JComboBox<>();
        formPanel.add(cbLoai, gbc);
        
        // Price (Buy)
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Giá mua:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 0.7;
        txtGiaMua = new JTextField(20);
        formPanel.add(txtGiaMua, gbc);
        
        // Price (Sell)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Giá bán:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 0.7;
        txtGiaBan = new JTextField(20);
        formPanel.add(txtGiaBan, gbc);
        
        // Quantity
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Số lượng:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 0.7;
        txtSoLuong = new JTextField(20);
        formPanel.add(txtSoLuong, gbc);
        
        // Format input for numeric fields
        setupNumericField(txtGiaMua);
        setupNumericField(txtGiaBan);
        setupNumericField(txtSoLuong);
        
        return formPanel;
    }
    
    private JPanel createImagePanel() {
        JPanel imagePanel = new JPanel(new BorderLayout(10, 10));
        imagePanel.setBackground(secondaryColor);
        imagePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(primaryColor),
                "Hình ảnh sản phẩm",
                TitledBorder.LEADING, TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14), primaryColor));
        
        // Image preview
        lblImagePreview = new JLabel();
        lblImagePreview.setPreferredSize(new Dimension(200, 200));
        lblImagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        lblImagePreview.setText("Chưa có ảnh");
        
        // Select image button
        btnSelectImage = new JButton("Chọn ảnh");
        styleButton(btnSelectImage, accentBlue, Color.WHITE);
        btnSelectImage.addActionListener(e -> selectImage());
        
        imagePanel.add(lblImagePreview, BorderLayout.CENTER);
        imagePanel.add(btnSelectImage, BorderLayout.SOUTH);
        
        return imagePanel;
    }
    
    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonsPanel.setBackground(secondaryColor);
        
        JButton btnCancel = new JButton("Hủy");
        styleButton(btnCancel, accentRed, Color.WHITE);
        btnCancel.addActionListener(e -> confirmClose());
        
        JButton btnSave = new JButton("Lưu thay đổi");
        styleButton(btnSave, accentGreen, Color.WHITE);
        btnSave.addActionListener(e -> updateProduct());
        
        JButton btnReset = new JButton("Đặt lại");
        styleButton(btnReset, accentBlue, Color.WHITE);
        btnReset.addActionListener(e -> loadProductData());
        
        buttonsPanel.add(btnReset);
        buttonsPanel.add(btnCancel);
        buttonsPanel.add(btnSave);
        
        return buttonsPanel;
    }
    
    private void loadCategories() {
        cbLoai.removeAllItems();
        
        // Add a default "Select category" item
        cbLoai.addItem(new Loai("", "-- Chọn loại sản phẩm --"));
        
        try {
            List<Loai> loaiList = loaiDAO.getAllLoai();
            if (loaiList != null) {
                for (Loai loai : loaiList) {
                    cbLoai.addItem(loai);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải danh sách loại sản phẩm: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void loadProductData() {
        // Set existing product data
        txtMaSP.setText(productToEdit.getMaSP());
        txtTenSP.setText(productToEdit.getTenSP());
        
        // Select the category in the combobox
        String maLoai = productToEdit.getMaloai();
        for (int i = 0; i < cbLoai.getItemCount(); i++) {
            Loai loai = cbLoai.getItemAt(i);
            if (loai.getMaloai().equals(maLoai)) {
                cbLoai.setSelectedIndex(i);
                break;
            }
        }
        
        // Format and set prices
        txtGiaMua.setText(formatMoneyValue((int)productToEdit.getGianhap()));
        txtGiaBan.setText(formatMoneyValue((int)productToEdit.getGiaban()));
        txtSoLuong.setText(String.valueOf(productToEdit.getSoluong()));
        
        // Load image if available
        if (productToEdit.getAnh() != null && !productToEdit.getAnh().toString().isEmpty()) {
            imagePath = productToEdit.getAnh().toString();
            loadImagePreview(imagePath);
        }
    }
    
    private void loadImagePreview(String path) {
        if (path == null || path.isEmpty()) {
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("Chưa có ảnh");
            return;
        }
        
        try {
            File imageFile = new File(path);
            if (imageFile.exists() && imageFile.isFile()) {
                BufferedImage img = ImageIO.read(imageFile);
                if (img != null) {
                    // Calculate dimensions to keep aspect ratio
                    int previewWidth = lblImagePreview.getWidth();
                    int previewHeight = lblImagePreview.getHeight();
                    
                    if (previewWidth <= 0) previewWidth = 200;
                    if (previewHeight <= 0) previewHeight = 200;
                    
                    // Calculate scale to fit
                    double scale = Math.min(
                            (double)previewWidth / img.getWidth(),
                            (double)previewHeight / img.getHeight());
                    
                    int scaledWidth = (int)(img.getWidth() * scale);
                    int scaledHeight = (int)(img.getHeight() * scale);
                    
                    Image scaledImg = img.getScaledInstance(
                            scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                    
                    lblImagePreview.setIcon(new ImageIcon(scaledImg));
                    lblImagePreview.setText("");
                } else {
                    lblImagePreview.setIcon(null);
                    lblImagePreview.setText("Không thể đọc hình ảnh");
                }
            } else {
                lblImagePreview.setIcon(null);
                lblImagePreview.setText("Ảnh không tồn tại");
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("Lỗi đọc ảnh");
        }
    }
    
    private void selectImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn hình ảnh sản phẩm");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        
        // Set file filter to only show image files
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            
            // Kiểm tra xem file có tồn tại không
            if (!selectedImageFile.exists() || !selectedImageFile.isFile()) {
                JOptionPane.showMessageDialog(this,
                        "File không tồn tại hoặc không phải là file.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // Đọc hình ảnh
                BufferedImage img = ImageIO.read(selectedImageFile);
                
                // Kiểm tra xem file có phải là hình ảnh hợp lệ không
                if (img == null) {
                    JOptionPane.showMessageDialog(this,
                            "File không phải là hình ảnh hợp lệ.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                isImageChanged = true;
                
                // Tính toán kích thước hiển thị giữ tỷ lệ
                int originalWidth = img.getWidth();
                int originalHeight = img.getHeight();
                int previewWidth = lblImagePreview.getWidth();
                int previewHeight = lblImagePreview.getHeight();
                
                // Nếu lblImagePreview chưa được hiển thị, sử dụng kích thước mặc định
                if (previewWidth <= 0) previewWidth = 200;
                if (previewHeight <= 0) previewHeight = 200;
                
                // Tính tỷ lệ để giữ nguyên tỷ lệ khung hình
                double widthRatio = (double) previewWidth / originalWidth;
                double heightRatio = (double) previewHeight / originalHeight;
                double ratio = Math.min(widthRatio, heightRatio);
                
                int scaledWidth = (int) (originalWidth * ratio);
                int scaledHeight = (int) (originalHeight * ratio);
                
                // Tạo hình ảnh thu nhỏ
                Image scaledImg = img.getScaledInstance(
                        scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                
                // Hiển thị hình ảnh
                lblImagePreview.setIcon(new ImageIcon(scaledImg));
                lblImagePreview.setText("");
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Không thể đọc file ảnh: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                
                // Reset image selection
                selectedImageFile = null;
                isImageChanged = false;
                lblImagePreview.setIcon(null);
                lblImagePreview.setText("Chưa có ảnh");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Lỗi không xác định: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                
                // Reset image selection
                selectedImageFile = null;
                isImageChanged = false;
                lblImagePreview.setIcon(null);
                lblImagePreview.setText("Chưa có ảnh");
            }
        }
    }
    
    private void updateProduct() {
        // Validate input
        if (!validateInput()) {
            return;
        }
        
        try {
            // Prepare the image path
            if (isImageChanged && selectedImageFile != null) {
                // Save the image to a designated folder
                String imageDir = "src/images/products/";
                Path dirPath = Paths.get(imageDir);
                
                // Create directory if it doesn't exist
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                }
                
                // Generate a unique filename
                String fileName = "product_" + UUID.randomUUID().toString() + ".jpg";
                Path targetPath = Paths.get(imageDir + fileName);
                
                // Copy the file
                Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                imagePath = targetPath.toString();
            }
            
            // Update the product object
            SanPham updatedSanPham = new SanPham();
            updatedSanPham.setMaSP(txtMaSP.getText().trim());
            updatedSanPham.setTenSP(txtTenSP.getText().trim());
            
            Loai selectedLoai = (Loai) cbLoai.getSelectedItem();
            if (selectedLoai != null && !selectedLoai.getMaloai().isEmpty()) {
                updatedSanPham.setMaloai(selectedLoai.getMaloai());
            } else {
                throw new Exception("Vui lòng chọn loại sản phẩm");
            }
            
            // Parse numeric fields
            int giaNhap = parseMoneyValue(txtGiaMua.getText());
            int giaBan = parseMoneyValue(txtGiaBan.getText());
            updatedSanPham.setGianhap(giaNhap);
            updatedSanPham.setGiaban(giaBan);
            updatedSanPham.setSoluong(Integer.parseInt(txtSoLuong.getText().replaceAll("[^0-9]", "")));
            
            // Set image path if available
            if (!imagePath.isEmpty()) {
                updatedSanPham.setAnh(imagePath);
            }
            
            // Save to database
            boolean success = sanPhamDAO.updateSanPham(updatedSanPham);
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Cập nhật sản phẩm thành công!",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                
                updateSuccess = true;
                this.updatedProduct = updatedSanPham;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Không thể cập nhật sản phẩm. Vui lòng thử lại.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi cập nhật sản phẩm: " + e.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private boolean validateInput() {
        // Product name
        if (txtTenSP.getText().trim().isEmpty()) {
            showValidationError("Vui lòng nhập tên sản phẩm");
            txtTenSP.requestFocus();
            return false;
        }
        
        // Category
        Loai selectedLoai = (Loai) cbLoai.getSelectedItem();
        if (selectedLoai == null || selectedLoai.getMaloai().isEmpty()) {
            showValidationError("Vui lòng chọn loại sản phẩm");
            cbLoai.requestFocus();
            return false;
        }
        
        // Buy price
        if (txtGiaMua.getText().trim().isEmpty()) {
            showValidationError("Vui lòng nhập giá mua");
            txtGiaMua.requestFocus();
            return false;
        }
        
        try {
            int giaMua = parseMoneyValue(txtGiaMua.getText());
            if (giaMua < 0) {
                showValidationError("Giá mua không được âm");
                txtGiaMua.requestFocus();
                return false;
            }
        } catch (Exception e) {
            showValidationError("Giá mua không hợp lệ");
            txtGiaMua.requestFocus();
            return false;
        }
        
        // Sell price
        if (txtGiaBan.getText().trim().isEmpty()) {
            showValidationError("Vui lòng nhập giá bán");
            txtGiaBan.requestFocus();
            return false;
        }
        
        try {
            int giaBan = parseMoneyValue(txtGiaBan.getText());
            if (giaBan < 0) {
                showValidationError("Giá bán không được âm");
                txtGiaBan.requestFocus();
                return false;
            }
        } catch (Exception e) {
            showValidationError("Giá bán không hợp lệ");
            txtGiaBan.requestFocus();
            return false;
        }
        
        // Quantity
        if (txtSoLuong.getText().trim().isEmpty()) {
            showValidationError("Vui lòng nhập số lượng");
            txtSoLuong.requestFocus();
            return false;
        }
        
        try {
            int soLuong = Integer.parseInt(txtSoLuong.getText().replaceAll("[^0-9]", ""));
            if (soLuong < 0) {
                showValidationError("Số lượng không được âm");
                txtSoLuong.requestFocus();
                return false;
            }
        } catch (Exception e) {
            showValidationError("Số lượng không hợp lệ");
            txtSoLuong.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this,
                message,
                "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
    }
    
    private void confirmClose() {
        // Check if any fields have been modified
        boolean hasChanges = !txtTenSP.getText().equals(productToEdit.getTenSP())
                || !getSelectedLoaiMa().equals(productToEdit.getMaloai())
                || parseMoneyValue(txtGiaMua.getText()) != productToEdit.getGianhap()
                || parseMoneyValue(txtGiaBan.getText()) != productToEdit.getGiaban()
                || Integer.parseInt(txtSoLuong.getText().replaceAll("[^0-9]", "")) != productToEdit.getSoluong()
                || isImageChanged;
        
        if (hasChanges) {
            int option = JOptionPane.showConfirmDialog(this,
                    "Bạn có muốn hủy thay đổi? Dữ liệu đã sửa sẽ không được lưu.",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            
            if (option == JOptionPane.YES_OPTION) {
                dispose();
            }
        } else {
            dispose();
        }
    }
    
    private String getSelectedLoaiMa() {
        Loai selectedLoai = (Loai) cbLoai.getSelectedItem();
        return (selectedLoai != null) ? selectedLoai.getMaloai() : "";
    }
    
    private void setupNumericField(JTextField field) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }
        });
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (!field.getText().trim().isEmpty()) {
                    try {
                        // Parse to number and format
                        int value = parseMoneyValue(field.getText());
                        field.setText(formatMoneyValue(value));
                    } catch (NumberFormatException ex) {
                        // Invalid number, leave as is
                    }
                }
            }
        });
    }
    
    private String formatMoneyValue(int value) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(value);
    }
    
    private int parseMoneyValue(String text) {
        // Remove all non-numeric characters
        String numericString = text.replaceAll("[^0-9]", "");
        if (numericString.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(numericString);
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
    
    /**
     * Check if the product was successfully updated
     * @return true if the product was updated
     */
    public boolean isUpdateSuccessful() {
        return updateSuccess;
    }
    
    /**
     * Get the updated product
     * @return The updated SanPham object, or null if not updated
     */
    public SanPham getUpdatedProduct() {
        return updatedProduct;
    }
}