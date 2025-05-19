package ui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import model.SanPham;

// Định nghĩa màu sắc theo giao diện gốc của bạn
// Tốt nhất nên có một class AppColors chung để quản lý màu sắc cho toàn bộ ứng dụng
class OriginalColors {
    public static final Color coffeeBrown = new Color(102, 51, 0); // Màu nâu cà phê
    public static final Color lightBeige = new Color(245, 245, 220); // Màu be nhạt (cho nền)
    public static final Color darkGray = new Color(50, 50, 50); // Màu xám đậm (cho văn bản, viền)
    public static final Color accentGreen = new Color(60, 179, 113); // Màu xanh lá (ví dụ: Doanh thu, nút Thêm/Tạo mới)
    public static final Color accentOrange = new Color(255, 165, 0); // Màu cam (ví dụ: Hết hàng, nút Xóa)
    public static final Color accentBlue = new Color(30, 144, 255); // Màu xanh dương (ví dụ: Hóa đơn bán, nút Lọc)
    public static final Color linkColor = new Color(0, 102, 204); // Màu xanh cho liên kết (ví dụ: ở LoginDialog)
}


public class ProductBoxPanel extends JPanel {

    // Sử dụng màu sắc gốc
    private final Color coffeeBrown = OriginalColors.coffeeBrown;
    private final Color lightBeige = OriginalColors.lightBeige;
    private final Color darkGray = OriginalColors.darkGray;
    private final Color accentGreen = OriginalColors.accentGreen;
    private final Color accentOrange = OriginalColors.accentOrange;
    private final Color accentBlue = OriginalColors.accentBlue;


    // UI Components
    private JLabel lblProductImage;
    private JLabel lblProductName;
    private JLabel lblProductPrice;
    private JLabel lblProductStock;

    private SanPham product; // The SanPham object this box represents

    // Constructor nhận đối tượng SanPham để hiển thị
    public ProductBoxPanel(SanPham product) {
        this.product = product;

        // Set layout for the product box
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE); // White background for the box
        // Add border and padding using original darkGray
        setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(darkGray, 1),
                    new EmptyBorder(10, 10, 10, 10)));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));


        // --- Product Image ---
        lblProductImage = new JLabel();
        lblProductImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblProductImage.setPreferredSize(new Dimension(100, 100));
        lblProductImage.setMinimumSize(new Dimension(100, 100));
        lblProductImage.setMaximumSize(new Dimension(100, 100));
        // Optional: border for image area - using original lightBeige
        lblProductImage.setBorder(BorderFactory.createLineBorder(lightBeige));
        loadProductImage(product.getAnh()); // Load and set the image based on path
        add(lblProductImage);

        // Add some vertical space
        add(Box.createRigidArea(new Dimension(0, 5)));


        // --- Product Name ---
        lblProductName = new JLabel("<html><b>" + product.getTenSP() + "</b></html>"); // Bold name
        lblProductName.setFont(new Font("Arial", Font.PLAIN, 14));
        lblProductName.setForeground(coffeeBrown); // Use original coffeeBrown for name
        lblProductName.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblProductName.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblProductName.getPreferredSize().height));
        add(lblProductName);

        add(Box.createRigidArea(new Dimension(0, 3)));


        // --- Product Price ---
        DecimalFormat currencyFormatter = new DecimalFormat("#,### VNĐ");
        lblProductPrice = new JLabel(currencyFormatter.format(product.getGiaban()), SwingConstants.CENTER);
        lblProductPrice.setFont(new Font("Arial", Font.BOLD, 12));
        lblProductPrice.setForeground(accentGreen); // Use original accentGreen for price
        lblProductPrice.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblProductPrice.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblProductPrice.getPreferredSize().height));
        add(lblProductPrice);

 
        // Add some vertical space
        add(Box.createRigidArea(new Dimension(0, 3)));

        // --- Product Stock (Số lượng) ---
        // Label hiển thị số lượng tồn kho - ĐÃ CÓ SẴN TRONG CODE GỐC CỦA BẠN
        lblProductStock = new JLabel("Còn lại: " + product.getSoluong(), SwingConstants.CENTER);
        lblProductStock.setFont(new Font("Arial", Font.PLAIN, 12)); // Font size from your original code
        // Highlight low stock using original accentOrange
        lblProductStock.setForeground(product.getSoluong() <= 10 ? accentOrange : darkGray); // Color from your original code
        lblProductStock.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblProductStock.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblProductStock.getPreferredSize().height));
        add(lblProductStock); // Add the stock label to the panel
        add(Box.createVerticalGlue());

        // // Optional: Add "Xem chi tiết" button below glue if desired

        // JButton btnViewDetails = new JButton("Xem chi tiết");
        // btnViewDetails.setAlignmentX(Component.CENTER_ALIGNMENT);
        // // Style the button using original colors if needed
        // styleButton(btnViewDetails, OriginalColors.accentBlue, Color.WHITE);
        // add(Box.createRigidArea(new Dimension(0, 5)));
        // add(btnViewDetails);


        // Add a mouse listener for interaction (clicking opens details)
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Clicked on product: " + product.getTenSP());
                Frame owner = JOptionPane.getFrameForComponent(ProductBoxPanel.this);
                if (owner instanceof JFrame) {

                     ProductDetailsDialog detailsDialog = new ProductDetailsDialog((JFrame) owner, product);
                     detailsDialog.setVisible(true);
                    // JOptionPane.showMessageDialog(owner, "Xem chi tiết sản phẩm: " + product.getTenSP(), "Chi tiết", JOptionPane.INFORMATION_MESSAGE);

                } else {
                     System.err.println("Could not get the main JFrame for showing the details dialog from ProductBoxPanel.");
                    JOptionPane.showMessageDialog(ProductBoxPanel.this, "Không thể mở chi tiết sản phẩm.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
            
             @Override
             public void mouseEntered(MouseEvent e) {
                  setBorder(BorderFactory.createLineBorder(coffeeBrown, 2)); // Highlight with original primary color
             }

             
             @Override
             public void mouseExited(MouseEvent e) {
                  setBorder(BorderFactory.createLineBorder(darkGray, 1)); // Revert to original border
             }
        });
    }
    
    
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        // Áp dụng màu nền cho nút
        button.setBackground(bgColor);
        // Áp dụng màu chữ cho nút
        button.setForeground(fgColor);
        // Loại bỏ viền khi nút được focus (nhấn tab)
        button.setFocusPainted(false);
        // Tạo viền kết hợp: viền đường kẻ ngoài và padding bên trong
        button.setBorder(BorderFactory.createCompoundBorder(
                // Viền đường kẻ, màu hơi đậm hơn màu nền, độ dày 1 pixel
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                // Padding (trên, trái, dưới, phải) bên trong viền
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        // Đảm bảo màu nền được hiển thị
        button.setOpaque(true);
        // Đảm bảo viền được vẽ
        button.setBorderPainted(true);
        // Đặt font chữ cho nút
        button.setFont(new Font("Arial", Font.BOLD, 12));

        // Thêm hiệu ứng hover (tùy chọn, làm nút hơi sáng hoặc tối đi khi rê chuột)
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker()); // Màu nền tối hơn khi rê chuột
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor); // Trở lại màu nền gốc khi không rê chuột
            }
        });
    }


    // Helper method to load and scale the product image
    private void loadProductImage(String imagePath) {
        // Clear previous content
        lblProductImage.setIcon(null);
        lblProductImage.setText("");

        if (imagePath == null || imagePath.trim().isEmpty()) {
            displayNoImage();
            return;
        }

        Image image = null;

        try {
            // 1. Try loading from a file path first
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                System.out.println("Loading image from file: " + imagePath);
                image = ImageIO.read(imageFile);
            }

            // 2. If file not found or failed to read, try loading from resources
            if (image == null) {
                System.out.println("File not found or failed to read. Trying resource: " + imagePath);
                 URL imageUrl = getClass().getResource(imagePath);
                 if (imageUrl != null) {
                     System.out.println("Resource URL found: " + imageUrl);
                      image = ImageIO.read(imageUrl);
                 } else {
                     System.err.println("Resource image not found: " + imagePath);
                     displayImageNotFound();
                     return;
                 }
            }

            if (image != null) {
                 Image scaledImage = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                 lblProductImage.setIcon(new ImageIcon(scaledImage));
                 System.out.println("Image loaded and scaled successfully.");
            } else {
                 System.err.println("ImageIO.read returned null for path: " + imagePath);
                 displayErrorLoadingImage("Định dạng ảnh lỗi");
            }

        } catch (Exception e) {
            System.err.println("Exception loading or scaling image from path: " + imagePath + " - " + e.getMessage());
            e.printStackTrace();
            displayErrorLoadingImage("Lỗi tải ảnh");
        }
    }

    // Helper methods for displaying different states
    private void displayNoImage() {
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
             System.err.println("Default no_image.png resource not found.");
              fallbackToText("Không có ảnh");
         }
        lblProductImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblProductImage.setVerticalAlignment(SwingConstants.CENTER);
    }

    private void displayImageNotFound() {
        lblProductImage.setIcon(null);
        lblProductImage.setText("Không tìm thấy ảnh");
        lblProductImage.setHorizontalTextPosition(SwingConstants.CENTER);
        lblProductImage.setVerticalTextPosition(SwingConstants.CENTER);
        lblProductImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblProductImage.setVerticalAlignment(SwingConstants.CENTER);
        lblProductImage.setForeground(darkGray); // Use original darkGray for text
        lblProductImage.setFont(new Font("Arial", Font.PLAIN, 10));
    }

    private void displayErrorLoadingImage(String message) {
        lblProductImage.setIcon(null);
        lblProductImage.setText(message);
        lblProductImage.setHorizontalTextPosition(SwingConstants.CENTER);
        lblProductImage.setVerticalTextPosition(SwingConstants.CENTER);
        lblProductImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblProductImage.setVerticalAlignment(SwingConstants.CENTER);
        lblProductImage.setForeground(Color.RED); // Error in red
        lblProductImage.setFont(new Font("Arial", Font.PLAIN, 10));
    }

    private void fallbackToText(String message) {
        lblProductImage.setIcon(null);
        lblProductImage.setText(message);
        lblProductImage.setHorizontalTextPosition(SwingConstants.CENTER);
        lblProductImage.setVerticalTextPosition(SwingConstants.CENTER);
        lblProductImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblProductImage.setVerticalAlignment(SwingConstants.CENTER);
        lblProductImage.setForeground(darkGray); // Use original darkGray for text
        lblProductImage.setFont(new Font("Arial", Font.PLAIN, 10));
    }


    // --- Main method for testing (Optional) ---
    /*
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Ensure you have mock SanPham objects with realistic data for testing
            // Example: Assuming a class SanPham with constructor SanPham(String maSP, String tenSP, String maLoai, double giamua, double giaban, int soluong, String anh)
            SanPham sampleProduct1 = new SanPham("SP001", "Cà phê Sữa Đá", "CF", 10000, 25000, 50, "/images/sample_coffee1.png"); // Assuming image exists in resources
            SanPham sampleProduct2 = new SanPham("SP002", "Trà Đào Cam Sả", "TS", 15000, 30000, 8, "/images/sample_tea1.png"); // Low stock, assuming image exists
            SanPham sampleProduct3 = new SanPham("SP003", "Bánh Muffin Chocolate", "BM", 20000, 35000, 0, null); // Out of stock, no image
            SanPham sampleProduct4 = new SanPham("SP004", "Nước Ép Cam", "NEP", 18000, 28000, 25, "/images/non_existent_image.png"); // Non-existent image resource


            JFrame frame = new JFrame("Product Box Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);

            JPanel containerPanel = new JPanel();
            containerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 15));
            containerPanel.setBackground(OriginalColors.lightBeige); // Use color from OriginalColors

            containerPanel.add(new ProductBoxPanel(sampleProduct1));
            containerPanel.add(new ProductBoxPanel(sampleProduct2));
            containerPanel.add(new ProductBoxPanel(sampleProduct3));
            containerPanel.add(new ProductBoxPanel(sampleProduct4));

            JScrollPane scrollPane = new JScrollPane(containerPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


            frame.add(scrollPane);

            frame.setVisible(true);
        });
    }
    */
}
