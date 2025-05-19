package ui;

import dao.KhachHangDAO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import model.KhachHang;
import model.NhanVien;

public class KhachHangUI extends JPanel {

    // Adjusted color palette for better contrast
    private final Color COFFEE_BROWN = new Color(120, 60, 0); // Adjusted for better contrast on LIGHT_BEIGE
    private final Color LIGHT_BEIGE = new Color(245, 245, 220); // Background
    private final Color ACCENT_GREEN = new Color(60, 179, 113); // Add button
    private final Color ACCENT_ORANGE = new Color(255, 165, 0); // Delete button
    private final Color DARK_GRAY = new Color(30, 30, 30); // Darker for better text readability
    private final Color ACCENT_BLUE = new Color(30, 144, 255); // Update and Search buttons
    private final Color BUTTON_TEXT = new Color(255, 255, 255); // White text for buttons
    private final Color TABLE_ALTERNATE = new Color(230, 230, 200); // Slightly darker for table rows

    // Font constants
    private final Font HEADER_FONT = new Font("Roboto", Font.BOLD, 20);
    private final Font LABEL_FONT = new Font("Roboto", Font.BOLD, 14);
    private final Font BUTTON_FONT = new Font("Roboto", Font.BOLD, 14);
    private final Font TABLE_FONT = new Font("Roboto", Font.PLAIN, 14);
    private final Font INPUT_FONT = new Font("Roboto", Font.PLAIN, 14);

    // UI Components
    private JTextField txtMaKH;
    private JTextField txtTenkhach;
    private JTextField txtSDT;
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnSearch;
    private JTextField txtSearch;
    private JComboBox<String> cbSearchType;
    private JTable khachHangTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScrollPane;

    // Data Access Object
    private KhachHangDAO khachHangDAO;

    // Current selected KhachHang
    private KhachHang currentKhachHang;

    // User account for permission checking
    private NhanVien currentUser;

    // Constructor
    public KhachHangUI(NhanVien currentUser) {
        this.currentUser = currentUser;
        khachHangDAO = new KhachHangDAO();
        currentKhachHang = null;

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(LIGHT_BEIGE);

        initializeComponents();
        loadKhachHangTable();
        updateButtonState();
    }

    private void initializeComponents() {
        // Top Panel: Title and Search
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBackground(LIGHT_BEIGE);
        topPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(LIGHT_BEIGE);
        JLabel titleLabel = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 24));
        titleLabel.setForeground(COFFEE_BROWN);
        titlePanel.add(titleLabel);
        topPanel.add(titlePanel, BorderLayout.WEST);

        // Search panel
        topPanel.add(createSearchPanel(), BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Split between table and detail
        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplitPane.setDividerLocation(600);
        centerSplitPane.setResizeWeight(0.6);
        centerSplitPane.setBackground(LIGHT_BEIGE);
        centerSplitPane.setBorder(null);
        centerSplitPane.setDividerSize(8);

        centerSplitPane.setLeftComponent(createTablePanel());
        centerSplitPane.setRightComponent(createDetailPanel());

        add(centerSplitPane, BorderLayout.CENTER);
    }

    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        searchPanel.setBackground(LIGHT_BEIGE);

        cbSearchType = new JComboBox<>(new String[]{"Mã KH", "Tên Khách"});
        cbSearchType.setFont(INPUT_FONT);
        cbSearchType.setBackground(Color.WHITE);
        cbSearchType.setForeground(DARK_GRAY);
        cbSearchType.setPreferredSize(new Dimension(120, 30));

        txtSearch = new JTextField(15);
        styleInputField(txtSearch);

        btnSearch = createButton("Tìm kiếm", ACCENT_BLUE, BUTTON_TEXT);

        searchPanel.add(createLabel("Tìm kiếm theo:"));
        searchPanel.add(cbSearchType);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        btnSearch.addActionListener(e -> performSearch());

        return searchPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setBackground(LIGHT_BEIGE);
        tablePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COFFEE_BROWN, 1, true),
            "Danh sách khách hàng",
            TitledBorder.LEADING, TitledBorder.TOP, HEADER_FONT, COFFEE_BROWN
        ));

        String[] columnNames = {"Mã KH", "Tên Khách", "Địa chỉ"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        khachHangTable = new JTable(tableModel);
        khachHangTable.setFont(TABLE_FONT);
        khachHangTable.setRowHeight(32);
        khachHangTable.setShowGrid(false);
        khachHangTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        khachHangTable.setFillsViewportHeight(true);

        // Table header styling
        JTableHeader header = khachHangTable.getTableHeader();
        header.setFont(LABEL_FONT);
        header.setBackground(COFFEE_BROWN);
        header.setForeground(BUTTON_TEXT);
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        header.setOpaque(true);
        header.setBorder(BorderFactory.createEmptyBorder());

        // Cell renderer
        khachHangTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                comp.setFont(TABLE_FONT);
                if (!isSelected) {
                    comp.setBackground(row % 2 == 0 ? LIGHT_BEIGE : TABLE_ALTERNATE);
                    comp.setForeground(DARK_GRAY);
                } else {
                    comp.setBackground(ACCENT_BLUE.darker());
                    comp.setForeground(BUTTON_TEXT);
                }
                setHorizontalAlignment(JLabel.CENTER);
                return comp;
            }
        });

        tableScrollPane = new JScrollPane(khachHangTable);
        tableScrollPane.setBackground(LIGHT_BEIGE);
        tableScrollPane.getViewport().setBackground(LIGHT_BEIGE);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        khachHangTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = khachHangTable.getSelectedRow();
                if (selectedRow >= 0) {
                    displayKhachHangDetails(selectedRow);
                    updateButtonState();
                } else {
                    clearForm();
                    updateButtonState();
                }
            }
        });

        tablePanel.add(tableScrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel createDetailPanel() {
        JPanel detailPanel = new JPanel(new BorderLayout(10, 15));
        detailPanel.setBackground(LIGHT_BEIGE);
        detailPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(COFFEE_BROWN, 1, true),
            "Thông tin chi tiết",
            TitledBorder.LEADING, TitledBorder.TOP, HEADER_FONT, COFFEE_BROWN
        ));

        // Input fields panel using BoxLayout for vertical stacking
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBackground(LIGHT_BEIGE);
        fieldsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Add components using createFormField helper
        fieldsPanel.add(createFormField("Mã khách hàng:", txtMaKH = new JTextField()));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(createFormField("Tên khách hàng:", txtTenkhach = new JTextField()));
        fieldsPanel.add(Box.createVerticalStrut(15));
        fieldsPanel.add(createFormField("Địa chỉ:", txtSDT = new JTextField()));

        // Add vertical glue to push fields to the top
        fieldsPanel.add(Box.createVerticalGlue());

        detailPanel.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonsPanel.setBackground(LIGHT_BEIGE);

        btnAdd = createButton("Thêm mới", ACCENT_GREEN, BUTTON_TEXT);
        btnUpdate = createButton("Cập nhật", ACCENT_BLUE, BUTTON_TEXT);
        btnDelete = createButton("Xóa", ACCENT_ORANGE, BUTTON_TEXT);
        btnClear = createButton("Làm mới", DARK_GRAY, BUTTON_TEXT);

        btnAdd.addActionListener(e -> {
            try {
                addKhachHang();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        btnUpdate.addActionListener(e -> updateKhachHang());
        btnDelete.addActionListener(e -> deleteKhachHang());
        btnClear.addActionListener(e -> clearForm());

        buttonsPanel.add(btnAdd);
        buttonsPanel.add(btnUpdate);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnClear);

        detailPanel.add(buttonsPanel, BorderLayout.SOUTH);

        return detailPanel;
    }

    private JPanel createFormField(String labelText, JTextField textField) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(LIGHT_BEIGE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(LABEL_FONT);
        label.setForeground(DARK_GRAY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        styleInputField(textField);
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));

        panel.add(label);
        panel.add(Box.createVerticalStrut(5));
        panel.add(textField);

        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        label.setForeground(DARK_GRAY);
        return label;
    }

    private void styleInputField(JTextField textField) {
        textField.setFont(INPUT_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        textField.setBackground(Color.WHITE);
        textField.setForeground(DARK_GRAY);
    }

    private JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(darken(bgColor));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private Color darken(Color color) {
        float factor = 0.9f;
        return new Color(
            Math.max(0, (int)(color.getRed() * factor)),
            Math.max(0, (int)(color.getGreen() * factor)),
            Math.max(0, (int)(color.getBlue() * factor))
        );
    }

    // ---- Business Logic Methods ----

    private void loadKhachHangTable() {
        tableModel.setRowCount(0);
        List<KhachHang> danhSachKhachHang = khachHangDAO.getAllKhachHang();
        if (danhSachKhachHang != null) {
            for (KhachHang kh : danhSachKhachHang) {
                tableModel.addRow(new Object[]{
                    kh.getMaKH(),
                    kh.getTenkhach(),
                    kh.getSDT()
                });
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể tải dữ liệu khách hàng từ cơ sở dữ liệu.", "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
        clearForm();
    }

    private void displayKhachHangDetails(int rowIndex) {
        String maKH = (String) tableModel.getValueAt(rowIndex, 0);
        currentKhachHang = khachHangDAO.getKhachHangById(maKH);
        if (currentKhachHang != null) {
            txtMaKH.setText(currentKhachHang.getMaKH());
            txtTenkhach.setText(currentKhachHang.getTenkhach());
            txtSDT.setText(currentKhachHang.getSDT());
            updateButtonState();
        } else {
            clearForm();
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin chi tiết khách hàng.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        // Suggest the next MaKH when clearing the form for a new entry
        String suggestedMaKH = khachHangDAO.suggestNextMaKH();
        txtMaKH.setText(suggestedMaKH);
        txtTenkhach.setText("");
        txtSDT.setText("");
        currentKhachHang = null;
        khachHangTable.clearSelection();
        updateButtonState();
    }

    private void addKhachHang() throws SQLException {
        String maKH = txtMaKH.getText().trim();
        String tenKhach = txtTenkhach.getText().trim();
        String SDT = txtSDT.getText().trim();

        // Basic validation
        if (maKH.isEmpty() || tenKhach.isEmpty() || SDT.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin khách hàng.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if MaKH already exists
        if (khachHangDAO.getKhachHangById(maKH) != null) {
            JOptionPane.showMessageDialog(this, "Mã khách hàng đã tồn tại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        KhachHang newKhachHang = new KhachHang(maKH, tenKhach, SDT);
        khachHangDAO.addKhachHang(newKhachHang);
        JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        loadKhachHangTable();
        clearForm();
    }

    private void updateKhachHang() {
        if (currentKhachHang == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần cập nhật.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maKH = currentKhachHang.getMaKH();
        String tenKhach = txtTenkhach.getText().trim();
        String SDT = txtSDT.getText().trim();

        if (tenKhach.isEmpty() || SDT.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin khách hàng.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        currentKhachHang.setTenkhach(tenKhach);
        currentKhachHang.setSDT(SDT);

        khachHangDAO.updateKhachHang(currentKhachHang);
        JOptionPane.showMessageDialog(this, "Cập nhật khách hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        loadKhachHangTable();
        clearForm();
    }

    private void deleteKhachHang() {
        if (currentKhachHang == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng cần xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa khách hàng " + currentKhachHang.getTenkhach() + " (Mã: " + currentKhachHang.getMaKH() + ") không?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            khachHangDAO.deleteKhachHang(currentKhachHang.getMaKH());
            JOptionPane.showMessageDialog(this, "Xóa khách hàng thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadKhachHangTable();
            clearForm();
        }
    }

    private void performSearch() {
        String searchTerm = txtSearch.getText().trim();
        String searchType = (String) cbSearchType.getSelectedItem();
        List<KhachHang> searchResult = new ArrayList<>();

        if (searchTerm.isEmpty()) {
            loadKhachHangTable();
            return;
        }

        try {
            switch (searchType) {
                case "Mã KH":
                    KhachHang foundById = khachHangDAO.getKhachHangById(searchTerm);
                    if (foundById != null) {
                        searchResult.add(foundById);
                    }
                    break;
                case "Tên Khách":
                    searchResult = khachHangDAO.searchKhachHangByName(searchTerm);
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Loại tìm kiếm không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
            }

            tableModel.setRowCount(0);
            if (searchResult != null && !searchResult.isEmpty()) {
                for (KhachHang kh : searchResult) {
                    tableModel.addRow(new Object[]{
                        kh.getMaKH(),
                        kh.getTenkhach(),
                        kh.getSDT()
                    });
                }
                if (searchResult.size() == 1 && "Mã KH".equals(searchType)) {
                    khachHangTable.setRowSelectionInterval(0, 0);
                    displayKhachHangDetails(0);
                } else {
                    clearForm();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả nào cho từ khóa '" + searchTerm + "'.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi thực hiện tìm kiếm: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            clearForm();
        }
    }

    private void updateButtonState() {
        boolean rowSelected = khachHangTable.getSelectedRow() >= 0;
        btnAdd.setEnabled(!rowSelected);
        btnUpdate.setEnabled(rowSelected);
        btnDelete.setEnabled(rowSelected);
        txtMaKH.setEditable(!rowSelected);
        btnClear.setEnabled(true);
        btnSearch.setEnabled(true);
        txtSearch.setEnabled(true);
        cbSearchType.setEnabled(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            NhanVien sampleAdminUser = new NhanVien("NV001", "admin_user", "pass123", "Nguyễn Văn A", "Admin", "0123456789", "admin@example.com", "Địa chỉ 1");
            JFrame frame = new JFrame("Quản lý Khách hàng");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 800);
            frame.setLocationRelativeTo(null);
            frame.add(new KhachHangUI(sampleAdminUser));
            frame.setVisible(true);
        });
    }
}