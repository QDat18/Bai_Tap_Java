package ui;

import java.awt.*; // Import model SanPham
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL; // Import MouseAdapter for click handling
import javax.imageio.ImageIO; // Import MouseEvent
import javax.swing.*; // Potentially useful for loading images from URL if needed
import javax.swing.border.EmptyBorder; // For loading images from file path
import model.SanPham; // For reading images


public class ProductBoxPanel extends JPanel {

    // Define colors (reusing the palette)
    Color coffeeBrown = new Color(102, 51, 0); // Màu nâu cà phê
    Color lightBeige = new Color(245, 245, 220); // Màu be nhạt (cho nền)
    Color darkGray = new Color(50, 50, 50); // Màu xám đậm (cho văn bản, viền)
    Color accentGreen = new Color(60, 179, 113); // Màu xanh lá (cho nút Thêm/Tạo mới)
    Color accentOrange = new Color(255, 165, 0); // Màu cam (cho nút Xóa)
    Color accentBlue = new Color(30, 144, 255); // Màu xanh dương (cho nút Chi tiết/Lọc)
    Color linkColor = new Color(0, 102, 204); // Màu xanh cho liên kết (ví dụ: ở LoginDialog)

    // UI Components
    private JLabel lblProductImage;
    private JLabel lblProductName;
    private JLabel lblProductPrice;
    // TODO: Add other labels or buttons if needed (e.g., add to cart)

    private SanPham product; // The SanPham object this box represents

    // Constructor nhận đối tượng SanPham để hiển thị
    public ProductBoxPanel(SanPham product) {
        this.product = product;

        // Set layout for the product box
        // Use BoxLayout for vertical arrangement of components within the box
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Arrange components vertically
        setBackground(Color.WHITE); // White background for the box
        // Add border and padding
        setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(darkGray, 1), // Outer border
                    new EmptyBorder(10, 10, 10, 10))); // Inner padding
        setAlignmentX(Component.LEFT_ALIGNMENT); // Align boxes to the left in the container (used by parent layout like FlowLayout/BoxLayout)
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Change cursor on hover


        // --- Product Image ---
        lblProductImage = new JLabel();
        lblProductImage.setAlignmentX(Component.CENTER_ALIGNMENT); // Center align the image horizontally
        lblProductImage.setPreferredSize(new Dimension(100, 100)); // Set preferred size for image area (fixed size)
        lblProductImage.setMinimumSize(new Dimension(100, 100));
        lblProductImage.setMaximumSize(new Dimension(100, 100));
        lblProductImage.setBorder(BorderFactory.createLineBorder(lightBeige)); // Optional: border for image area
        loadProductImage(product.getAnh()); // Load and set the image based on path
        add(lblProductImage);

        // Add some vertical space
        add(Box.createRigidArea(new Dimension(0, 5)));


        // --- Product Name ---
        lblProductName = new JLabel("<html><b>" + product.getTenSP() + "</b></html>"); // Bold name
        lblProductName.setFont(new Font("Arial", Font.PLAIN, 14));
        lblProductName.setForeground(coffeeBrown); // Coffee color for name
        lblProductName.setAlignmentX(Component.CENTER_ALIGNMENT); // Center align text
        // Optional: Limit size to prevent stretching horizontally too much
        lblProductName.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblProductName.getPreferredSize().height));
        add(lblProductName);

         // Add some vertical space
        add(Box.createRigidArea(new Dimension(0, 3)));


        // --- Product Price ---
        lblProductPrice = new JLabel(String.format("%,d VNĐ", product.getGiaban())); // Format price
        lblProductPrice.setFont(new Font("Arial", Font.BOLD, 12));
        lblProductPrice.setForeground(accentGreen); // Green color for price
        lblProductPrice.setAlignmentX(Component.CENTER_ALIGNMENT); // Center align text
         lblProductPrice.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblProductPrice.getPreferredSize().height));
        add(lblProductPrice);


         // TODO: Add more details or buttons here if needed
         // Example: Add "Xem chi tiết" button
         /*
         JButton btnViewDetails = new JButton("Xem chi tiết");
         btnViewDetails.setAlignmentX(Component.CENTER_ALIGNMENT);
         // Add action listener to open a details dialog
         // btnViewDetails.addActionListener(e -> { ... open details dialog ... });
         add(Box.createRigidArea(new Dimension(0, 5)));
         add(btnViewDetails);
         */

        // Add a component that takes up remaining vertical space, pushing other components to the top
        add(Box.createVerticalGlue());


        // Set the size of the box (optional, BoxLayout handles sizing but this can set a minimum)
        // setPreferredSize(new Dimension(150, 200)); // Example initial size
        // setMaximumSize(new Dimension(150, 250)); // Prevent stretching too much


        // Add a mouse listener for interaction (clicking opens details)
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Clicked on product: " + product.getTenSP());
                // Get the parent Frame to make the dialog modal to it
                Frame owner = JOptionPane.getFrameForComponent(ProductBoxPanel.this); // Use ProductBoxPanel.this to get the panel's context

                if (owner instanceof JFrame) {
                     // Create and show the product details dialog
                     ProductDetailsDialog detailsDialog = new ProductDetailsDialog((JFrame) owner, product); // Pass the product object
                     detailsDialog.setVisible(true);
                } else {
                     System.err.println("Could not get the main JFrame for showing the details dialog from ProductBoxPanel.");
                     // Optional: Show an error message
                     // JOptionPane.showMessageDialog(ProductBoxPanel.this, "Không thể mở chi tiết sản phẩm.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
             // Optional: Add mouseEntered/mouseExited for hover effects (cursor change is already done in constructor)
             // @Override
             // public void mouseEntered(MouseEvent e) { ... }
             // @Override
             // public void mouseExited(MouseEvent e) { ... }
        });
    }

    // Helper method to load and scale the product image
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
            // 1. Try loading from a file path first
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                System.out.println("Loading image from file: " + imagePath);
                image = ImageIO.read(imageFile); // Use ImageIO to read the image file
            }

            // 2. If file not found or failed to read, try loading from resources
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
                 Image scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Scale image
                 lblProductImage.setIcon(new ImageIcon(scaledImage)); // Set the scaled icon
                 // Size of JLabel is now controlled by preferredSize and layout
                 // lblProductImage.setPreferredSize(new Dimension(100, 100)); // Keep the fixed size
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

    // Helper methods for displaying different states
    private void displayNoImage() {
        // Load a default "no image" icon from resources
         URL noImageUrl = getClass().getResource("/images/no_image.png"); // Assuming /images/no_image.png exists
         if (noImageUrl != null) {
             try {
                 Image noImage = ImageIO.read(noImageUrl);
                 Image scaledNoImage = noImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                 lblProductImage.setIcon(new ImageIcon(scaledNoImage));
                 lblProductImage.setText("");
             } catch (Exception e) {
                 System.err.println("Error loading default no_image.png: " + e.getMessage());
                  fallbackToText("Không có ảnh (Lỗi tải default)");
             }
         } else {
             // Fallback to text if default image is also missing
             System.err.println("Default no_image.png resource not found.");
              fallbackToText("Không có ảnh");
         }
        lblProductImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblProductImage.setVerticalAlignment(SwingConstants.CENTER);
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

    private void fallbackToText(String message) {
        lblProductImage.setIcon(null);
        lblProductImage.setText(message);
        lblProductImage.setHorizontalTextPosition(SwingConstants.CENTER);
        lblProductImage.setVerticalTextPosition(SwingConstants.CENTER);
        lblProductImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblProductImage.setVerticalAlignment(SwingConstants.CENTER);
        lblProductImage.setForeground(darkGray);
        lblProductImage.setFont(new Font("Arial", Font.PLAIN, 10));
    }


    // Helper method to create styled labels (defined earlier in other UI classes)
    // private JLabel createLabel(String text) { ... }

     // Helper method to create value labels (defined earlier in other UI classes)
     // private JLabel createValueLabel(String text) { ... }

     // Helper method to style buttons (defined earlier in other UI classes)
    // private void styleButton(JButton button, Color bgColor, Color fgColor) { ... }


    // --- Main method for testing (Optional) ---
    /*
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SanPham sampleProduct1 = new SanPham("SP001", "Cà phê Đen", "CF", 10000, 15000, 50, "C:/path/to/your/real/image.jpg"); // Example file path
            SanPham sampleProduct2 = new SanPham("SP002", "Trà Sữa Matcha", "TS", 12000, 20000, 30, "/images/matcha.png"); // Example resource path
            SanPham sampleProduct3 = new SanPham("SP003", "Sản phẩm không ảnh", "Other", 8000, 12000, 100, null);
             SanPham sampleProduct4 = new SanPham("SP004", "Ảnh lỗi resource", "Error", 5000, 10000, 10, "/images/non_existent.png"); // Non-existent resource
             SanPham sampleProduct5 = new SanPham("SP005", "Ảnh lỗi file", "Error", 6000, 11000, 20, "C:/non/existent/file.jpg"); // Non-existent file


            JFrame frame = new JFrame("Product Box Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600); // Increased size for multiple boxes
            frame.setLocationRelativeTo(null);

            // Use a JPanel with a layout manager to hold multiple ProductBoxPanels
            JPanel containerPanel = new JPanel(); // Use default FlowLayout or set a grid/flow layout
             containerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15)); // FlowLayout with spacing
            containerPanel.setBackground(new Color(245, 245, 220)); // Match lightBeige

            containerPanel.add(new ProductBoxPanel(sampleProduct1));
            containerPanel.add(new ProductBoxPanel(sampleProduct2));
            containerPanel.add(new ProductBoxPanel(sampleProduct3));
            containerPanel.add(new ProductBoxPanel(sampleProduct4)); // Test non-existent resource
            containerPanel.add(new ProductBoxPanel(sampleProduct5)); // Test non-existent file


             // Add the container panel to a scroll pane if you have many boxes
             JScrollPane scrollPane = new JScrollPane(containerPanel);
             scrollPane.setBorder(BorderFactory.createEmptyBorder()); // No border for the scroll pane
             scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // Show vertical scrollbar
             scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Hide horizontal scrollbar


            frame.add(scrollPane); // Add the scroll pane to the frame

            frame.setVisible(true);
        });
    }
    */
}
