package ui;

// Import NhanVien thay vì ACC
import dao.DatabaseConnection;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter; // Cần import
import java.awt.event.WindowEvent; // Cần import
import java.awt.image.BufferedImage; // Cần import
import java.io.File; // Cần import
import java.sql.SQLException;
import java.util.HashMap; // Cần import nếu tải từ file
import java.util.Map; // Cần import cho kiểm tra kết nối CSDL
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder; // Cần import
import model.NhanVien;

/**
 * Cửa sổ chính của ứng dụng quản lý cửa hàng cà phê.
 * Chứa các panel chức năng và điều hướng giữa chúng, có phân quyền theo vai trò của NhanVien.
 */
public class MainApplicationFrame extends JFrame {

    // Định nghĩa màu sắc sử dụng trong giao diện
    Color coffeeBrown = new Color(102, 51, 0); // Màu nâu cà phê
    Color lightBeige = new Color(245, 245, 220); // Màu be nhạt (cho nền)
    Color darkGray = new Color(50, 50, 50); // Màu xám đậm (cho văn bản, viền)
    Color accentBlue = new Color(30, 144, 255); // Màu xanh dương (cho menu được chọn)
    Color accentGreen = new Color(60, 179, 113); // Màu xanh lá
    Color accentOrange = new Color(255, 165, 0); // Màu cam

    // UI Components
    private JPanel contentPanel; // Panel để chứa nội dung chức năng hiện tại (sử dụng CardLayout)
    private CardLayout cardLayout; // Layout manager cho contentPanel

    private JLabel lblAppTitle; // Label cho tiêu đề ứng dụng
    private JLabel lblLogo; // Label cho logo ứng dụng
    private JLabel lblWelcome; // Label chào mừng người dùng (ở header)
    private JButton btnLogout; // Nút đăng xuất (ở header)

    // Panel cho menu bên (điều hướng)
    private JPanel menuPanel;

    // Thông tin nhân viên đã đăng nhập
    private NhanVien loggedInUser;

    // Map để lưu các panel UI chức năng và tên tương ứng trên menu
    private Map<String, JPanel> uiPanels;

    // Map để lưu các nút menu dựa trên tên chức năng (card name)
    private Map<String, JButton> menuButtons;

    // Định nghĩa thứ tự các mục menu (quan trọng cho việc hiển thị và xác định panel mặc định)
    private static final String[] MENU_ORDER = {
         "Trang chủ",
         "Quản lý Sản phẩm",
         "Quản lý Hóa đơn Bán",
         "Quản lý Khách hàng",
         "Quản lý Nhà cung cấp",
         "Quản lý Nhân viên",
         "Quản lý Loại sản phẩm",
         "Quản lý Hóa đơn Nhập",
         "Thống kê và Báo cáo"
         // Không bao gồm "Quản lý Tài khoản" vì thông tin tài khoản được quản lý trong NhanVien
    };


    /**
     * Constructor khởi tạo cửa sổ ứng dụng chính.
     *
     * @param user Thông tin đối tượng NhanVien của người dùng đã đăng nhập.
     */
    public MainApplicationFrame(NhanVien user) {
        this.loggedInUser = user; // Lưu thông tin nhân viên đã đăng nhập

        // JFrame setup
        setTitle("Hệ thống Quản lý Cửa hàng Cà phê");
        // Xử lý việc đóng cửa sổ bằng WindowListener để hỏi người dùng xác nhận thoát
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 800); // Kích thước cửa sổ mặc định
        setLocationRelativeTo(null); // Căn giữa cửa sổ
        setMinimumSize(new Dimension(800, 600)); // Đặt kích thước tối thiểu

        // Sử dụng BorderLayout cho panel nội dung chính của Frame
        JPanel mainContentPane = new JPanel(new BorderLayout());
        setContentPane(mainContentPane);


        // --- Header Panel (Phía trên - NORTH) ---
        JPanel headerPanel = new JPanel(new BorderLayout()); // Header dùng BorderLayout
        headerPanel.setBackground(coffeeBrown); // Màu nền header
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Thêm padding


        // Phần bên trái của header: Logo và Tiêu đề ứng dụng
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); // Dùng FlowLayout xếp ngang
        titlePanel.setBackground(coffeeBrown); // Nền trùng với header

        // Thêm logo (ưu tiên tải từ resources)
        lblLogo = new JLabel(); // Khởi tạo Label cho logo
        loadLogoFromResources("/assets/logo/logo.png"); // <-- Gọi phương thức tải logo từ resources (thay đổi đường dẫn nếu cần)
        // Nếu không tải được từ resources, có thể thử tải từ file:
        // if (lblLogo.getIcon() == null && lblLogo.getText().equals("Logo N/A")) {
        //     loadLogo("D:\\KyIV_HocVienNganHang\\WebDesign\\BTL_web\\assets\\logo\\logo.png"); // <-- Thay bằng đường dẫn file thực tế
        // }
        titlePanel.add(lblLogo); // Thêm logo vào titlePanel


        lblAppTitle = new JLabel("<html><b><font color='white' size='+1'>Hệ thống Quản lý Cửa hàng Cà phê</font></b></html>"); // Tiêu đề ứng dụng
        lblAppTitle.setForeground(Color.WHITE); // Màu chữ tiêu đề
        lblAppTitle.setFont(new Font("Arial", Font.BOLD, 18)); // Font tiêu đề
        titlePanel.add(lblAppTitle); // Thêm tiêu đề vào titlePanel


        headerPanel.add(titlePanel, BorderLayout.WEST); // TitlePanel ở bên trái header

        // Phần bên phải của header: Thông tin người dùng và nút Đăng xuất
        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Dùng FlowLayout xếp ngang và căn phải
        userInfoPanel.setBackground(coffeeBrown); // Nền trùng với header

        // Hiển thị thông tin người dùng từ đối tượng NhanVien
        String displayName = loggedInUser.getTenNV(); // Sử dụng TenNV làm tên hiển thị
        if (displayName == null || displayName.trim().isEmpty()) {
             displayName = loggedInUser.getTendangnhap(); // Fallback về tên đăng nhập nếu TenNV rỗng
        }
        String role = loggedInUser.getRole(); // Lấy vai trò từ NhanVien

        lblWelcome = new JLabel("<html><font color='white'>Chào mừng, <b>" + displayName + "</b> | Vai trò: <b>" + role + "</b></font></html>"); // Chào mừng và hiển thị vai trò
        lblWelcome.setForeground(Color.WHITE); // Màu chữ chào mừng
        userInfoPanel.add(lblWelcome); // Thêm label chào mừng

        btnLogout = new JButton("Đăng xuất"); // Nút đăng xuất
        styleButton(btnLogout, accentOrange, Color.WHITE); // Style nút đăng xuất
        userInfoPanel.add(btnLogout); // Thêm nút đăng xuất

        headerPanel.add(userInfoPanel, BorderLayout.EAST); // UserInfoPanel ở bên phải header


        mainContentPane.add(headerPanel, BorderLayout.NORTH); // Thêm header vào panel nội dung chính


        // --- Content Panel (Trung tâm - CENTER) ---
        // Khởi tạo contentPanel và CardLayout
        contentPanel = new JPanel();
        cardLayout = new CardLayout(); // Khởi tạo CardLayout
        contentPanel.setLayout(cardLayout); // Đặt CardLayout cho contentPanel
        contentPanel.setBackground(lightBeige); // Đặt màu nền cho vùng nội dung

        // Thêm contentPanel vào panel nội dung chính
        mainContentPane.add(contentPanel, BorderLayout.CENTER);


        // --- Side Menu Panel (Bên trái - WEST) ---
        menuPanel = new JPanel();
        menuPanel.setBackground(darkGray); // Màu nền tối cho menu
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS)); // Sử dụng BoxLayout xếp dọc cho các mục menu
        menuPanel.setBorder(new EmptyBorder(10, 0, 10, 0)); // Thêm padding trên dưới

        // Khởi tạo các map để lưu các panel và nút menu
        uiPanels = new HashMap<>();
        menuButtons = new HashMap<>();

        // Tạo các nút menu và thêm các panel UI vào contentPanel
        createMenuButtons(); // Gọi phương thức tạo menu

        // Thêm menuPanel vào panel nội dung chính (trong một JScrollPane để cuộn nếu nhiều mục)
        mainContentPane.add(new JScrollPane(menuPanel), BorderLayout.WEST);


        // --- Thêm Event Listeners ---
        // Lắng nghe sự kiện đóng cửa sổ để xác nhận thoát
        addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    confirmExit(); // Gọi phương thức xác nhận thoát
                }
        });

        // Lắng nghe sự kiện click nút Đăng xuất
        btnLogout.addActionListener(e -> performLogout()); // Gọi phương thức đăng xuất


        // Áp dụng phân quyền menu dựa trên vai trò của NhanVien đăng nhập sau khi các nút được tạo
        applyMenuPermissions(); // <-- Gọi phương thức applyMenuPermissions()

        // Hiển thị panel mặc định đầu tiên mà người dùng có quyền truy cập và highlight nút tương ứng
        showInitialPanel();


        // Hiển thị Frame
        setVisible(true);
    }

    /**
     * Phương thức để tải logo từ đường dẫn file trên hệ thống.
     * Ít được ưu tiên hơn tải từ resources vì phụ thuộc vào cấu trúc file.
     *
     * @param imagePath Đường dẫn tuyệt đối hoặc tương đối đến file ảnh logo.
     */
    private void loadLogo(String imagePath) {
         lblLogo.setIcon(null); // Clear previous icon/text
         lblLogo.setText("");

         if (imagePath == null || imagePath.trim().isEmpty()) {
            System.err.println("Logo image path is null or empty.");
            lblLogo.setText("Logo N/A");
            lblLogo.setForeground(Color.RED);
            return;
         }

         try {
             File logoFile = new File(imagePath);
             if (logoFile.exists()) {
                 BufferedImage originalImage = ImageIO.read(logoFile);
                 if (originalImage != null) {
                     Image scaledImage = originalImage.getScaledInstance(50, 50, Image.SCALE_SMOOTH); // Điều chỉnh kích thước logo
                     lblLogo.setIcon(new ImageIcon(scaledImage));
                     lblLogo.setText(""); // Clear any text if image loaded
                     System.out.println("Logo loaded successfully from file: " + imagePath);
                 } else {
                     System.err.println("Failed to read image from file: " + imagePath);
                     lblLogo.setText("Logo Error"); // Show text on failure
                     lblLogo.setForeground(Color.RED);
                 }
             } else {
                 System.err.println("Logo file not found: " + imagePath);
                 lblLogo.setText("Logo N/A"); // Show text on failure
                 lblLogo.setForeground(Color.RED);
             }
         } catch (Exception e) {
             System.err.println("Exception loading logo image from file:");
             e.printStackTrace();
             lblLogo.setText("Logo Error"); // Show text on error
             lblLogo.setForeground(Color.RED);
         }
    }

    /**
     * Phương thức để tải logo từ resources (tốt nhất cho ứng dụng đóng gói).
     * Đặt ảnh trong thư mục resources của project (ví dụ: src/main/resources).
     *
     * @param resourcePath Đường dẫn đến resource (ví dụ: "/assets/logo/logo.png").
     */
    private void loadLogoFromResources(String resourcePath) {
         lblLogo.setIcon(null); // Clear previous icon/text
         lblLogo.setText("");

         if (resourcePath == null || resourcePath.trim().isEmpty()) {
            System.err.println("Logo resource path is null or empty.");
            lblLogo.setText("Logo N/A");
            lblLogo.setForeground(Color.RED);
            return;
         }

         try {
             // Sử dụng getResourceAsStream để đọc resource từ classpath
             java.io.InputStream imgStream = getClass().getResourceAsStream(resourcePath);
             if (imgStream != null) {
                 BufferedImage originalImage = ImageIO.read(imgStream);
                 if (originalImage != null) {
                     Image scaledImage = originalImage.getScaledInstance(50, 50, Image.SCALE_SMOOTH); // Điều chỉnh kích thước logo
                     lblLogo.setIcon(new ImageIcon(scaledImage));
                     lblLogo.setText(""); // Clear any text
                     System.out.println("Logo loaded successfully from resources: " + resourcePath);
                 } else {
                     System.err.println("Failed to read image from resource stream: " + resourcePath);
                     lblLogo.setText("Logo Error");
                     lblLogo.setForeground(Color.RED);
                 }
             } else {
                 System.err.println("Logo resource not found: " + resourcePath);
                 lblLogo.setText("Logo N/A");
                 lblLogo.setForeground(Color.RED);
             }
         } catch (Exception e) {
             System.err.println("Exception loading logo image from resources:");
             e.printStackTrace();
             lblLogo.setText("Logo Error");
             lblLogo.setForeground(Color.RED);
         }
    }


    /**
     * Tạo các nút menu và thêm các panel UI tương ứng vào contentPanel.
     * Panel được thêm vào contentPanel với tên mục menu làm "card identifier".
     */
    private void createMenuButtons() {
        // Clear existing panels from the map and contentPanel before adding new ones
        uiPanels.clear(); // Xóa các panel cũ trong map
        contentPanel.removeAll(); // Xóa tất cả các panel khỏi contentPanel

        // Khởi tạo các panel UI chức năng và thêm vào map uiPanels
        // Truyền đối tượng NhanVien đăng nhập vào constructor của các panel nếu cần
        uiPanels.put("Trang chủ", new HomePanel(this.loggedInUser)); // Giả định HomePanel nhận NhanVien
        // Bạn cần đảm bảo các lớp UI này tồn tại và có constructor phù hợp
         try {
              uiPanels.put("Quản lý Sản phẩm", new SanPhamUI(this.loggedInUser)); // Giả định SanPhamUI nhận NhanVien
              uiPanels.put("Thống kê và Báo cáo", new ThongKeUI()); // ThongKeUI có thể không cần NhanVien, hoặc cần tùy logic
              uiPanels.put("Quản lý Khách hàng", new KhachHangUI(this.loggedInUser)); // Giả định KhachHangUI nhận NhanVien
              uiPanels.put("Quản lý Nhà cung cấp", new NhaCungCapUI(this.loggedInUser)); // Giả định NhaCungCapUI nhận NhanVien
              uiPanels.put("Quản lý Nhân viên", new NhanVienUI(this.loggedInUser)); // Giả định NhanVienUI nhận NhanVien
              uiPanels.put("Quản lý Hóa đơn Bán", new HoaDonBanUI(this.loggedInUser)); // Giả định HoaDonBanUI nhận NhanVien
              uiPanels.put("Quản lý Loại sản phẩm", new LoaiUI(this.loggedInUser)); // Giả định LoaiUI nhận NhanVien
              uiPanels.put("Quản lý Hóa đơn Nhập", new HoaDonNhapUI(this.loggedInUser)); // Giả định HoaDonNhapUI nhận NhanDonNhapUI nhận NhanVien
         } catch (Exception e) {
             System.err.println("Lỗi khi tạo panel UI chức năng:");
             e.printStackTrace();
             JOptionPane.showMessageDialog(this, "Lỗi khi tạo các giao diện chức năng.\nVui lòng kiểm tra console.", "Lỗi UI", JOptionPane.ERROR_MESSAGE);
             // Có thể thêm logic để hiển thị panel lỗi hoặc thoát nếu các UI chính không load được
         }


        // Thêm các panel từ map uiPanels vào contentPanel sử dụng tên mục menu làm card identifier
        for (Map.Entry<String, JPanel> entry : uiPanels.entrySet()) {
            contentPanel.add(entry.getValue(), entry.getKey()); // Thêm panel với tên mục menu làm định danh card
        }

        // Xóa tất cả các nút menu cũ khỏi menuPanel nếu có
        menuPanel.removeAll();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS)); // Đảm bảo lại BoxLayout
        menuPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Thêm khoảng trống ở trên cùng

        // Clear the button map before creating new buttons
        menuButtons.clear(); // Xóa các nút cũ trong map

        // Tạo các nút cho menu dựa trên thứ tự đã định nghĩa
        for (String menuName : MENU_ORDER) {
            // Chỉ tạo nút nếu có panel UI tương ứng trong map uiPanels
            if (uiPanels.containsKey(menuName)) {
                JButton menuButton = new JButton(menuName);

                // Áp dụng styling mặc định
                styleMenuButton(menuButton, false); // false = không được chọn

                // Add hover effect (sử dụng MouseAdapter)
                menuButton.addMouseListener(new MouseAdapter() {
                     @Override
                     public void mouseEntered(MouseEvent e) {
                         // Chỉ áp dụng hiệu ứng hover nếu nút không đang được chọn
                         if (menuButton.getBackground().getRGB() != accentBlue.getRGB()) {
                             menuButton.setBackground(darkGray.brighter()); // Màu hơi sáng hơn khi hover
                         }
                         menuButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Hiện con trỏ dạng tay
                     }

                     @Override
                     public void mouseExited(MouseEvent e) {
                         // Trở lại màu ban đầu nếu nút không đang được chọn
                         if (menuButton.getBackground().getRGB() != accentBlue.getRGB()) {
                            menuButton.setBackground(darkGray); // Màu nền mặc định
                         }
                         menuButton.setCursor(Cursor.getDefaultCursor()); // Trở lại con trỏ mặc định
                     }
                });


                // Add action listener để chuyển đổi panel khi nút được nhấn
                menuButton.addActionListener(e -> {
                    // Kiểm tra quyền truy cập trước khi chuyển panel
                     if (isPanelAccessible(menuName, loggedInUser.getRole())) {
                         cardLayout.show(contentPanel, menuName); // Hiển thị panel tương ứng dùng CardLayout
                         highlightMenuButton(menuName); // Highlight nút menu được chọn

                          // Tùy chọn: Nếu panel cần refresh dữ liệu khi được hiển thị, gọi phương thức loadData() của nó
                          // JPanel currentPanelInstance = uiPanels.get(menuName);
                          // if (currentPanelInstance instanceof YourPanelInterfaceWithLoadData) {
                          //      ((YourPanelInterfaceWithLoadData) currentPanelInstance).loadData();
                          // }

                     } else {
                         // Logic này chỉ phòng trường hợp nút được hiển thị do lỗi logic phân quyền,
                         // nhưng isPanelAccessible() ở showPanel() cũng đã kiểm tra.
                         JOptionPane.showMessageDialog(this, "Bạn không có quyền truy cập chức năng này.", "Lỗi Phân quyền", JOptionPane.WARNING_MESSAGE);
                     }
                });

                menuButton.setAlignmentX(Component.CENTER_ALIGNMENT); // Căn giữa nút trong BoxLayout
                menuPanel.add(menuButton); // Thêm nút vào panel menu
                menuPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Thêm khoảng cách giữa các nút

                // Thêm nút vào map menuButtons để dễ dàng truy cập sau này (ví dụ: để highlight)
                menuButtons.put(menuName, menuButton);
            }
        }

        // Thêm VerticalGlue ở cuối để đẩy các nút lên phía trên
        menuPanel.add(Box.createVerticalGlue());

        // Cập nhật lại giao diện của panel menu
        menuPanel.revalidate();
        menuPanel.repaint();
    }


    /**
     * Helper method để style nút menu (đặt màu nền, font, border, v.v.).
     *
     * @param button     Nút cần style.
     * @param isSelected Trạng thái của nút: true nếu đang được chọn, false nếu không.
     */
    private void styleMenuButton(JButton button, boolean isSelected) {
        // Đảm bảo nút có thể lấp đầy không gian ngang trong BoxLayout
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        button.setFocusPainted(false); // Loại bỏ viền focus khi được chọn
        button.setHorizontalAlignment(SwingConstants.LEFT); // Căn chữ sang trái
        button.setFont(new Font("Arial", Font.BOLD, 14)); // Đặt font chữ
        button.setBorderPainted(true); // Đảm bảo border được vẽ

        if (isSelected) {
            // Style cho nút đang được chọn
            button.setBackground(accentBlue); // Màu nền nổi bật
            button.setForeground(Color.WHITE); // Màu chữ trắng
            // Thêm border bên trái để tạo điểm nhấn cho mục được chọn
            button.setBorder(BorderFactory.createCompoundBorder(
                         BorderFactory.createMatteBorder(0, 5, 0, 0, lightBeige), // Border màu be nhạt bên trái (độ dày 5px)
                         BorderFactory.createEmptyBorder(10, 10, 10, 15) // Padding (top, left, bottom, right). Left padding giảm đi 5 để bù lại border
            ));
        } else {
            // Style cho nút không được chọn
            button.setBackground(darkGray); // Màu nền mặc định
            button.setForeground(Color.WHITE); // Màu chữ mặc định
            // Reset border và padding cho phù hợp
            button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Padding mặc định (top, left, bottom, right)
        }

        // Đảm bảo nút là opaque để màu nền hiển thị đúng
        button.setOpaque(true);
    }


    /**
     * Helper method để highlight nút menu được chọn và reset các nút khác về trạng thái mặc định.
     *
     * @param selectedMenuName Tên của mục menu (card identifier) cần highlight.
     */
    private void highlightMenuButton(String selectedMenuName) {
        // Lặp qua tất cả các nút menu trong map
        for (Map.Entry<String, JButton> entry : menuButtons.entrySet()) {
            JButton button = entry.getValue();
            String menuName = entry.getKey();

            if (menuName.equals(selectedMenuName)) {
                // Áp dụng style "đang được chọn"
                styleMenuButton(button, true);
            } else {
                // Áp dụng style "không được chọn"
                styleMenuButton(button, false);
            }
        }
    }

    /**
     * Helper method để lấy tên của panel (card) đang hiển thị trong contentPanel.
     *
     * @return Tên của card đang hiển thị, hoặc null nếu không có card nào hiển thị rõ ràng.
     */
     private String getCurrentCardName() {
         // Lặp qua tất cả các panel trong map
         for (Map.Entry<String, JPanel> entry : uiPanels.entrySet()) {
             // isVisible() của component bên trong CardLayout thường chỉ đúng cho panel đang hiển thị
              if (entry.getValue().isVisible()) {
                  return entry.getKey(); // Trả về tên (key) của panel đang hiển thị
              }
         }
         return null; // Trường hợp không có panel nào hiển thị (rất hiếm)
     }


    /**
     * Helper method để style các nút chung (không phải nút menu, ví dụ: nút Đăng xuất).
     *
     * @param button  Nút cần style.
     * @param bgColor Màu nền.
     * @param fgColor Màu chữ.
     */
    private void styleButton(JButton button, Color bgColor, Color fgColor) {
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFocusPainted(false); // Loại bỏ viền focus
        button.setBorder(BorderFactory.createCompoundBorder(
                         BorderFactory.createLineBorder(fgColor, 1), // Viền ngoài
                         BorderFactory.createEmptyBorder(5, 15, 5, 15))); // Padding bên trong
        button.setOpaque(true); // Đảm bảo màu nền hiển thị
        button.setBorderPainted(true); // Đảm bảo viền hiển thị
        button.setFont(new Font("Arial", Font.BOLD, 12)); // Đặt font
    }


    /**
     * Xử lý sự kiện đóng cửa sổ: hiển thị hộp thoại xác nhận thoát.
     */
    private void confirmExit() {
        int option = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn thoát ứng dụng?", // Nội dung hỏi
                    "Xác nhận thoát", // Tiêu đề hộp thoại
                    JOptionPane.YES_NO_OPTION, // Các lựa chọn (Yes/No)
                    JOptionPane.QUESTION_MESSAGE); // Icon hộp thoại

        if (option == JOptionPane.YES_OPTION) {
            // TODO: Thực hiện các tác vụ dọn dẹp cần thiết trước khi thoát (ví dụ: đóng kết nối CSDL)
             // DatabaseConnection.closeConnection(); // Giả định bạn có phương thức này trong utils package

            dispose(); // Đóng Frame hiện tại
            System.exit(0); // Thoát ứng dụng hoàn toàn
        }
    }

    /**
     * Xử lý sự kiện click nút Đăng xuất: xác nhận, đóng Frame hiện tại và mở lại cửa sổ Đăng nhập.
     */
    private void performLogout() {
        int option = JOptionPane.showConfirmDialog(this,
                    "Bạn có chắc chắn muốn đăng xuất?", // Nội dung hỏi
                    "Xác nhận đăng xuất", // Tiêu đề hộp thoại
                    JOptionPane.YES_NO_OPTION, // Các lựa chọn (Yes/No)
                    JOptionPane.QUESTION_MESSAGE); // Icon hộp thoại

        if (option == JOptionPane.YES_OPTION) {
            // TODO: Thực hiện các tác vụ dọn dẹp liên quan đến đăng xuất (ví dụ: xóa dữ liệu session)

            // Đóng Frame ứng dụng chính hiện tại
            dispose();

            // Chạy lại luồng UI để hiển thị lại cửa sổ đăng nhập
            SwingUtilities.invokeLater(() -> {
                // Tạo và hiển thị lại cửa sổ đăng nhập.
                // LoginDialog cần có constructor hoặc phương thức hiển thị phù hợp.
                // Giả định LoginDialog có constructor nhận Frame cha (ở đây là null vì nó là cửa sổ đầu tiên)
                LoginDialog loginDialog = new LoginDialog(null);
                loginDialog.setVisible(true); // Hiển thị Login Dialog (dialog modal sẽ chặn luồng đến đây)

                // Sau khi Login Dialog đóng (ví dụ: người dùng đăng nhập thành công hoặc hủy)
                // Kiểm tra kết quả đăng nhập từ Login Dialog
                 NhanVien user = loginDialog.getLoggedInUser(); // Giả định LoginDialog có phương thức này

                 if (user != null) {
                     // Nếu đăng nhập thành công (nhận được đối tượng NhanVien), mở lại Main Application Frame
                     System.out.println("Đăng nhập lại thành công cho user: " + user.getTendangnhap() + " (Role: " + user.getRole() + ")");
                     MainApplicationFrame mainFrame = new MainApplicationFrame(user); // Tạo Main Frame mới với user vừa đăng nhập
                     // mainFrame.setVisible(true); // Đã được gọi ở cuối constructor của MainApplicationFrame
                 } else {
                     // Nếu đăng nhập bị hủy hoặc thất bại lần nữa, thoát ứng dụng
                     System.out.println("Đăng nhập bị hủy hoặc thất bại sau khi đăng xuất. Thoát ứng dụng.");
                     System.exit(0); // Thoát ứng dụng hoàn toàn
                 }
            });
        }
    }

    /**
     * Áp dụng phân quyền menu dựa trên vai trò của NhanVien đăng nhập.
     * Phương thức này sẽ ẩn hoặc vô hiệu hóa các nút menu mà vai trò không được phép truy cập.
     */
    private void applyMenuPermissions() {
        // Lấy vai trò từ đối tượng NhanVien đã đăng nhập.
        // Nếu loggedInUser là null (không thể xảy ra trong luồng bình thường sau đăng nhập thành công), dùng vai trò rỗng.
        String role = (loggedInUser != null) ? loggedInUser.getRole() : "";

        // Lặp qua tất cả các nút menu đã được lưu trong map menuButtons
        for (Map.Entry<String, JButton> entry : menuButtons.entrySet()) {
            String cardName = entry.getKey(); // Tên của panel/chức năng tương ứng với nút
            JButton button = entry.getValue(); // Nút menu

            // Kiểm tra xem vai trò hiện tại có quyền truy cập panel này không
            boolean isPermitted = isPanelAccessible(cardName, role);

            // Áp dụng quyền: thiết lập hiển thị và trạng thái kích hoạt của nút
            button.setVisible(isPermitted); // Ẩn nút nếu không được phép
            button.setEnabled(isPermitted); // Vô hiệu hóa nút nếu không được phép (tùy chọn, nhưng tốt)

            // Tùy chọn: Nếu bạn muốn ẩn hoàn toàn các khoảng trống của nút bị ẩn,
            // bạn cần điều chỉnh BoxLayout hoặc sử dụng một LayoutManager khác cho menuPanel,
            // hoặc thêm/xóa các "rigid area" một cách động. Với BoxLayout và setVisible(false),
            // nút sẽ không hiển thị nhưng khoảng trống của nó vẫn có thể tồn tại trừ khi bạn
            // gọi revalidate() và repaint() trên menuPanel sau khi thay đổi visibility.
            // Tuy nhiên, applyMenuPermissions được gọi trước khi menuPanel được thêm vào frame,
            // nên revalidate/repaint cuối cùng của frame sẽ xử lý việc hiển thị đúng.
        }

        // Sau khi áp dụng quyền, cần đảm bảo menuPanel được cập nhật hiển thị
        menuPanel.revalidate();
        menuPanel.repaint();
    }


    /**
     * Kiểm tra xem một panel chức năng có được phép truy cập bởi vai trò của người dùng hay không.
     * Logic phân quyền ở đây phải khớp với logic khi tạo nút trong createMenuButtons().
     *
     * @param panelName Tên của panel chức năng (card identifier).
     * @param role      Vai trò của người dùng (ví dụ: "Admin", "Manager", "Staff").
     * @return true nếu được phép truy cập, false ngược lại.
     */
    private boolean isPanelAccessible(String panelName, String role) {
         if (role == null) return false; // Không có vai trò thì không có quyền

         // Định nghĩa quyền truy cập cho từng panel dựa trên vai trò
         switch (panelName) {
             case "Trang chủ":
                 return true; // Trang chủ ai cũng có thể truy cập
             case "Quản lý Sản phẩm":
                 // Admin, Manager, Staff có quyền quản lý/xem sản phẩm
                 return "Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role);
             case "Quản lý Hóa đơn Bán":
                 // Admin, Manager, Staff có quyền tạo/xem hóa đơn bán
                 return "Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role);
             case "Quản lý Khách hàng":
                 // Admin, Manager, Staff có quyền quản lý khách hàng
                 return "Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role);
             case "Quản lý Nhà cung cấp":
                 // Chỉ Admin và Manager có quyền quản lý nhà cung cấp
                 return "Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role);
             case "Quản lý Nhân viên":
                 // Chỉ Admin có quyền quản lý nhân viên
                 return "Admin".equalsIgnoreCase(role);
             case "Quản lý Loại sản phẩm":
                 // Chỉ Admin và Manager có quyền quản lý loại sản phẩm
                 return "Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role);
             case "Quản lý Hóa đơn Nhập":
                 // Chỉ Admin và Manager có quyền quản lý hóa đơn nhập
                 return "Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role);
             case "Thống kê và Báo cáo":
                 // Chỉ Admin và Manager có quyền xem thống kê/báo cáo
                 return "Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role);
             default:
                 return false; // Các panel không xác định thì không được phép truy cập
         }
    }

     /**
      * Hiển thị panel mặc định đầu tiên mà người dùng có quyền truy cập khi khởi chạy ứng dụng.
      */
     private void showInitialPanel() {
          String firstPermittedMenuName = null;

          // Tìm panel đầu tiên trong MENU_ORDER mà người dùng có quyền truy cập
          for (String menuName : MENU_ORDER) {
              // Kiểm tra xem panel đó có tồn tại trong map uiPanels VÀ người dùng có quyền truy cập không
              if (uiPanels.containsKey(menuName) && isPanelAccessible(menuName, loggedInUser.getRole())) {
                  firstPermittedMenuName = menuName;
                  break; // Tìm thấy panel đầu tiên được phép truy cập
              }
          }

          if (firstPermittedMenuName != null) {
              // Hiển thị panel đầu tiên được phép
              cardLayout.show(contentPanel, firstPermittedMenuName);
              // Highlight nút menu tương ứng
              highlightMenuButton(firstPermittedMenuName);
          } else {
              // Trường hợp không có panel nào được phép truy cập (rất hiếm nếu Trang chủ luôn được phép)
              System.err.println("Không có panel UI nào được phép truy cập cho vai trò này: " + loggedInUser.getRole());
              // Có thể thêm một panel rỗng hoặc panel thông báo ở đây
               JLabel noAccessLabel = new JLabel("Tài khoản của bạn không có quyền truy cập chức năng nào.");
               noAccessLabel.setHorizontalAlignment(SwingConstants.CENTER);
               contentPanel.removeAll(); // Clear any default content
               contentPanel.add(noAccessLabel, BorderLayout.CENTER);
               contentPanel.revalidate();
               contentPanel.repaint();
          }
     }


    /**
     * Phương thức main để chạy ứng dụng.
     * Đây là điểm bắt đầu thực thi chương trình.
     */
    public static void main(String[] args) {
        // Chạy giao diện trên Event Dispatch Thread (luồng xử lý sự kiện của Swing)
        SwingUtilities.invokeLater(() -> {
            // --- Giai đoạn 1: Kiểm tra kết nối CSDL (Nên làm trước đăng nhập) ---
            System.out.println("Testing Database Connection...");
            try (java.sql.Connection conn = DatabaseConnection.getConnection()) { // Sử dụng try-with-resources để tự động đóng kết nối
                if (conn != null) {
                    System.out.println("Database connection successful!");
                } else {
                     System.err.println("Failed to connect to database. getConnection() returned null.");
                     JOptionPane.showMessageDialog(null, "Không thể kết nối đến cơ sở dữ liệu. Vui lòng kiểm tra cấu hình.", "Lỗi Kết nối CSDL", JOptionPane.ERROR_MESSAGE);
                     return; // Dừng khởi chạy ứng dụng nếu kết nối CSDL thất bại
                }
            } catch (SQLException e) {
                System.err.println("Database connection failed due to SQLException:");
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Lỗi CSDL khi kết nối: " + e.getMessage() + "\nVui lòng kiểm tra cấu hình và trạng thái máy chủ CSDL.", "Lỗi Kết nối CSDL", JOptionPane.ERROR_MESSAGE);
                return; // Dừng khởi chạy ứng dụng nếu có lỗi SQL
            } catch (Exception e) { // Bắt các ngoại lệ khác có thể xảy ra khi gọi getConnection()
                System.err.println("An unexpected error occurred during database connection test:");
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Đã xảy ra lỗi khi kiểm tra kết nối CSDL: " + e.getMessage(), "Lỗi Kết nối CSDL", JOptionPane.ERROR_MESSAGE);
                return;
            }


            // --- Giai đoạn 2: Đăng nhập ---
            // Hiển thị cửa sổ đăng nhập và chờ kết quả
            System.out.println("\nOpening Login Dialog...");
            // Tạo LoginDialog với owner là null vì nó là cửa sổ đầu tiên
            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true); // Hiển thị Login Dialog (dialog modal sẽ chặn luồng đến đây)

            // Sau khi Login Dialog đóng, kiểm tra kết quả đăng nhập
            // Giả định LoginDialog có phương thức getLoggedInUser() trả về NhanVien hoặc null
            NhanVien loggedInUser = loginDialog.getLoggedInUser();

            if (loggedInUser != null) {
                // Nếu đăng nhập thành công
                System.out.println("Login successful for user: " + loggedInUser.getTendangnhap() + " (Role: " + loggedInUser.getRole() + ")");
                // --- Giai đoạn 3: Khởi chạy Main Application Frame ---
                System.out.println("Launching Main Application Frame...");
                // Tạo và hiển thị cửa sổ ứng dụng chính, truyền đối tượng NhanVien đã đăng nhập
                 try {
                     MainApplicationFrame mainFrame = new MainApplicationFrame(loggedInUser);
                     // mainFrame.setVisible(true); // Đã được gọi ở cuối constructor của MainApplicationFrame
                 } catch (Throwable e) { // Bắt Throwable để báo cáo lỗi nếu có vấn đề trong quá trình khởi tạo UI Frame
                      System.err.println("Error launching Main Application Frame:");
                      e.printStackTrace();
                      JOptionPane.showMessageDialog(null, "Lỗi khi khởi tạo giao diện ứng dụng: " + e.getMessage() + "\nVui lòng kiểm tra console.", "Lỗi Khởi tạo Giao diện", JOptionPane.ERROR_MESSAGE);
                 }

            } else {
                // Nếu đăng nhập bị hủy hoặc thất bại
                System.err.println("Login failed or cancelled. Exiting application.");
                JOptionPane.showMessageDialog(null, "Đăng nhập thất bại hoặc bị hủy. Thoát ứng dụng.", "Thông báo Đăng nhập", JOptionPane.WARNING_MESSAGE);
                System.exit(0); // Thoát ứng dụng hoàn toàn
            }
        });
    }
}
