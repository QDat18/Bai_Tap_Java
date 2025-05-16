package ui;

import java.awt.*; // Import model SanPham
import java.io.File;
import java.net.URL; // Nếu sản phẩm có thông tin liên quan đến nhân viên nhập/quản lý
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.SanPham; // For loading images from file path


public class ProductDetailsDialog extends JDialog {

    // Define colors (reusing the palette)
    Color coffeeBrown = new Color(102, 51, 0); // Màu nâu cà phê
    Color lightBeige = new Color(245, 245, 220); // Màu be nhạt (cho nền)
    Color darkGray = new Color(50, 50, 50); // Màu xám đậm (cho văn bản, viền)
    Color accentBlue = new Color(30, 144, 255); // Màu xanh dương

    // UI Components
    private JLabel lblProductImage;
    private JLabel lblMaSP;
    private JLabel lblTenSP;
    private JLabel lblMaloai; // Or lblTenloai if you fetch category name
    private JLabel lblGianhap;
    private JLabel lblGiaban;
    private JLabel lblSoluong;
    // TODO: Add other labels if needed

    // Data Access Objects (optional, if you need to fetch related info)
    // private LoaiDAO loaiDAO;
    // private NhanVienDAO nhanVienDAO;


    // Constructor nhận đối tượng SanPham để hiển thị chi tiết
    public ProductDetailsDialog(Frame owner, SanPham product) {
        super(owner, "Chi tiết Sản phẩm: " + product.getTenSP(), true); // Modal dialog with title
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 500); // Set a default size (adjust as needed)
        setLocationRelativeTo(owner); // Center relative to the owner frame


        // Initialize optional DAOs if needed
        // loaiDAO = new LoaiDAO();
        // nhanVienDAO = new NhanVienDAO();


        // --- Content Pane ---
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBackground(lightBeige);
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));


        // --- Top Panel (Image) ---
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imagePanel.setBackground(lightBeige);
        lblProductImage = new JLabel();
        lblProductImage.setPreferredSize(new Dimension(150, 150)); // Size for image in dialog
        lblProductImage.setBorder(BorderFactory.createLineBorder(darkGray));
        loadProductImage(product.getAnh()); // Load and set image
        imagePanel.add(lblProductImage);
        contentPane.add(imagePanel, BorderLayout.NORTH);


        // --- Center Panel (Details) ---
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(lightBeige);
        detailsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(coffeeBrown, 1), "Thông tin chi tiết", 0, 0, new Font("Arial", Font.BOLD, 14), coffeeBrown));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST; // Align labels to the left


        // Row 0: Mã SP
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0; detailsPanel.add(createLabel("Mã SP:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; lblMaSP = createValueLabel(product.getMaSP()); detailsPanel.add(lblMaSP, gbc);

        // Row 1: Tên SP
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; detailsPanel.add(createLabel("Tên SP:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; lblTenSP = createValueLabel(product.getTenSP()); detailsPanel.add(lblTenSP, gbc);

        // Row 2: Loại (Mã hoặc Tên loại)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; detailsPanel.add(createLabel("Loại:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
         // Fetch LoaiSP name if needed and possible, otherwise use Maloai
         // String tenLoai = product.getMaloai(); // Default to Maloai
         // if (loaiDAO != null && product.getMaloai() != null) {
         //     LoaiSP loai = loaiDAO.getLoaiById(product.getMaloai()); // Assuming getLoaiById exists
         //     if (loai != null) tenLoai = loai.getTenloai();
         // }
        lblMaloai = createValueLabel(product.getMaloai()); // Using Maloai for now
        detailsPanel.add(lblMaloai, gbc);

        // Row 3: Giá nhập
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0; detailsPanel.add(createLabel("Giá nhập:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0; lblGianhap = createValueLabel(String.format("%,d VNĐ", product.getGianhap())); detailsPanel.add(lblGianhap, gbc);

        // Row 4: Giá bán
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0; detailsPanel.add(createLabel("Giá bán:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0; lblGiaban = createValueLabel(String.format("%,d VNĐ", product.getGiaban())); detailsPanel.add(lblGiaban, gbc);

        // Row 5: Số lượng tồn
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0; detailsPanel.add(createLabel("Số lượng tồn:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0; lblSoluong = createValueLabel(String.valueOf(product.getSoluong())); detailsPanel.add(lblSoluong, gbc);


        // Add details panel to content pane
        contentPane.add(detailsPanel, BorderLayout.CENTER);


        // --- Button Panel (SOUTH) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(lightBeige);
        JButton btnClose = new JButton("Đóng");
        styleButton(btnClose, darkGray, Color.WHITE); // Style the button
        btnClose.addActionListener(e -> dispose()); // Close the dialog
        buttonPanel.add(btnClose);

        contentPane.add(buttonPanel, BorderLayout.SOUTH);


        // Set the content pane of the dialog
        setContentPane(contentPane);
        setModal(true); // Make it a modal dialog (blocks input to owner frame)
        setResizable(false); // Prevent resizing

        // Pack the dialog to its preferred size (optional, can override setSize)
        // pack();
    }

    // Helper method to create styled labels
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(darkGray);
        label.setFont(new Font("Arial", Font.BOLD, 12)); // Labels in bold
        return label;
    }

     // Helper method to create value labels (for displaying data)
     private JLabel createValueLabel(String text) {
         JLabel label = new JLabel(text);
         label.setForeground(darkGray);
         label.setFont(new Font("Arial", Font.PLAIN, 12));
         return label;
     }

     // Helper method to style buttons
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

     // Helper method to load and scale the product image (copied from ProductBoxPanel)
    private void loadProductImage(String imagePath) {
        // Clear previous content
        lblProductImage.setIcon(null);
        lblProductImage.setText("");

        // Handle null or empty image path explicitly
        if (imagePath == null || imagePath.trim().isEmpty()) {
            displayNoImage(); // Show the "no image" state
            return; // Exit the method
        }

        Image image = null;

        try {
            // 1. Try loading from a file path
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                System.out.println("Loading image from file: " + imagePath);
                image = ImageIO.read(imageFile); // Use ImageIO to read the image file
            }

            // 2. If file not found, try loading from resources
            if (image == null) {
                System.out.println("File not found or failed to read. Trying resource: " + imagePath);
                 URL imageUrl = getClass().getResource(imagePath); // Get URL from resource
                 if (imageUrl != null) {
                     System.out.println("Resource URL found: " + imageUrl);
                      image = ImageIO.read(imageUrl); // Use ImageIO to read from URL
                 } else {
                     System.err.println("Resource image not found: " + imagePath);
                     displayImageNotFound(); // Show "not found" state
                     return; // Exit if resource not found
                 }
            }

            // 3. If an image was successfully loaded (either from file or resource)
            if (image != null) {
                 // Scale the image (using the size set in the constructor)
                 Image scaledImage = image.getScaledInstance(150, 150, Image.SCALE_SMOOTH); // Scale to preferred size
                 lblProductImage.setIcon(new ImageIcon(scaledImage)); // Set the scaled icon
                 lblProductImage.setText(""); // Clear any text
                 System.out.println("Image loaded and scaled successfully.");
            } else {
                 // If ImageIO.read returned null (invalid format, etc.)
                 System.err.println("ImageIO.read returned null for path: " + imagePath);
                 displayErrorLoadingImage("Định dạng ảnh lỗi"); // Show error state
            }

        } catch (Exception e) {
            // Catch any other exceptions during loading/scaling (IOExceptions, etc.)
            System.err.println("Exception loading or scaling image from path: " + imagePath + " - " + e.getMessage());
            e.printStackTrace();
            displayErrorLoadingImage("Lỗi tải ảnh"); // Show generic error state
        }
    }

    // Helper methods for displaying different states (copied from ProductBoxPanel)
    private void displayNoImage() {
        // Load a default "no image" icon from resources
         URL noImageUrl = getClass().getResource("/images/no_image.png"); // Assuming /images/no_image.png exists
         if (noImageUrl != null) {
             ImageIcon noImageIcon = new ImageIcon(noImageUrl);
             Image scaledNoImage = noImageIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH); // Scale to preferred size
             lblProductImage.setIcon(new ImageIcon(scaledNoImage));
             lblProductImage.setText("");
         } else {
             // Fallback to text if default image is also missing
              lblProductImage.setIcon(null);
              lblProductImage.setText("Không có ảnh");
              lblProductImage.setHorizontalTextPosition(SwingConstants.CENTER);
              lblProductImage.setVerticalTextPosition(SwingConstants.CENTER);
              lblProductImage.setHorizontalAlignment(SwingConstants.CENTER);
              lblProductImage.setVerticalAlignment(SwingConstants.CENTER);
              lblProductImage.setForeground(darkGray);
              lblProductImage.setFont(new Font("Arial", Font.PLAIN, 10));
         }
        // Size already set in constructor via preferredSize
    }

    private void displayImageNotFound() {
        lblProductImage.setIcon(null); // Clear icon
        lblProductImage.setText("Không tìm thấy ảnh"); // Set text message
        // Center the text
        lblProductImage.setHorizontalTextPosition(SwingConstants.CENTER);
        lblProductImage.setVerticalTextPosition(SwingConstants.CENTER);
        lblProductImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblProductImage.setVerticalAlignment(SwingConstants.CENTER);
        lblProductImage.setForeground(darkGray); // Text color
        lblProductImage.setFont(new Font("Arial", Font.PLAIN, 10)); // Font size
         // Size already set

    }

    private void displayErrorLoadingImage(String message) {
        lblProductImage.setIcon(null); // Clear icon
        lblProductImage.setText(message); // Set text message (e.g., "Lỗi tải ảnh")
         // Center the text
        lblProductImage.setHorizontalTextPosition(SwingConstants.CENTER);
        lblProductImage.setVerticalTextPosition(SwingConstants.CENTER);
        lblProductImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblProductImage.setVerticalAlignment(SwingConstants.CENTER);
        lblProductImage.setForeground(Color.RED); // Text color
        lblProductImage.setFont(new Font("Arial", Font.PLAIN, 10)); // Font size
        // Size already set
    }


    // --- Main method for testing (Optional) ---

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create a sample SanPham object for testing
             SanPham sampleProduct = new SanPham("SP001", "Cà phê Đen", "CF", 10000, 15000, 50, "D://KyIV_HocVienNganHang//WebDesign//BTL_web//assets//logo//logo.png"); // Replace with a valid path or null

            JFrame ownerFrame = new JFrame(); // Create a dummy owner frame
            ownerFrame.setSize(100, 100);
            ownerFrame.setLocationRelativeTo(null);
            ownerFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // ownerFrame.setVisible(true); // Make owner visible if needed

            ProductDetailsDialog dialog = new ProductDetailsDialog(ownerFrame, sampleProduct);
            dialog.setVisible(true);

            // ownerFrame.dispose(); // Dispose owner frame after dialog closes if it was just for testing
        });
    }
}