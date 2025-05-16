package ui;

import model.NhanVien;
import dao.NhanVienDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;


public class NhanVienUI extends JPanel {

    // Define colors (reusing the palette)
    Color coffeeBrown = new Color(102, 51, 0);
    Color lightBeige = new Color(245, 245, 220);
    Color accentGreen = new Color(60, 179, 113); // Color for Add button
    Color accentOrange = new Color(255, 165, 0); // Color for Delete button
    Color darkGray = new Color(50, 50, 50);
    Color accentBlue = new Color(30, 144, 255); // Color for Search button

    // UI Components (Input Fields and Buttons in detail panel)
    private JTextField txtMaNV;
    private JTextField txtTenNV;
    private JTextField txtDiachi;
    private JTextField txtGioitinh; // Could be JComboBox or JRadioButton in practice
    private JTextField txtSDT;
    // Fields for account info (Display only in main UI, editing in Dialog)
    private JTextField txtTendangnhap;
    // REMOVED: private JPasswordField txtMatkhau; // Password field removed from main UI panel detail view
    private JTextField txtEmail;
    private JTextField txtRole; // Could be JComboBox in practice

    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear; // Add a clear button
    private JButton btnSearch;
    private JTextField txtSearch;
    private JComboBox<String> cbSearchType; // For selecting search criteria

    // Table components
    private JTable nhanVienTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScrollPane;

    // Data Access Object
    private NhanVienDAO nhanVienDAO;

    // Current selected NhanVien or the one being added/edited
    private NhanVien currentNhanVien;

    // User account for permission checking
    private NhanVien currentUser;


    // Constructor
    public NhanVienUI(NhanVien currentUser) { // Accept current user as NhanVien
        this.currentUser = currentUser; // Store current user
        nhanVienDAO = new NhanVienDAO();
        currentNhanVien = null; // No employee selected initially

        setLayout(new BorderLayout(10, 10)); // BorderLayout with gaps
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(lightBeige);

        // --- Top Panel (Search) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); // FlowLayout for search components
        searchPanel.setBackground(lightBeige);

        // Update search types
        cbSearchType = new JComboBox<>(new String[]{"Mã NV", "Tên NV", "Địa chỉ", "Tên đăng nhập", "Email", "Role"});

        txtSearch = new JTextField(20); // Adjust size as needed
        btnSearch = new JButton("Tìm kiếm");
        styleButton(btnSearch, accentBlue, Color.WHITE);

        searchPanel.add(new JLabel("Tìm kiếm theo:"));
        searchPanel.add(cbSearchType);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        // Add action listener for search button
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });

        add(searchPanel, BorderLayout.NORTH);


        // --- Center Panel (Table and Detail) ---
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 0)); // Split into two columns
        centerPanel.setBackground(lightBeige);


        // --- Left Side: Table ---
        // Define table columns
        String[] columnNames = {"Mã NV", "Tên NV", "Địa chỉ", "Giới tính", "SĐT", "Tên đăng nhập", "Email", "Role"}; // Removed Chucvu

        tableModel = new DefaultTableModel(columnNames, 0) {
             // Make columns non-editable
             @Override
             public boolean isCellEditable(int row, int column) {
                 return false; // Users edit data via the dialog
             }
        };
        nhanVienTable = new JTable(tableModel);
        nhanVienTable.setFillsViewportHeight(true);
        nhanVienTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nhanVienTable.getTableHeader().setBackground(coffeeBrown);
        nhanVienTable.getTableHeader().setForeground(Color.WHITE);
        nhanVienTable.setBackground(lightBeige);
        nhanVienTable.setForeground(darkGray);
        nhanVienTable.setRowHeight(25);

        // Set renderer for center alignment
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < nhanVienTable.getColumnCount(); i++) {
             nhanVienTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }


        tableScrollPane = new JScrollPane(nhanVienTable);
        tableScrollPane.getViewport().setBackground(lightBeige);


        // Add row selection listener to display selected employee details
        nhanVienTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = nhanVienTable.getSelectedRow();
                if (selectedRow >= 0) {
                    displayNhanVienDetails(selectedRow);
                    // Button state updated within displayNhanVienDetails and checkPermissions
                }
            }
        });

        centerPanel.add(tableScrollPane);


        // --- Right Side: Detail Panel (Display Only) ---
        JPanel detailPanel = new JPanel(new BorderLayout(10, 10)); // Detail panel layout
        detailPanel.setBackground(lightBeige);
        detailPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(darkGray), "Thông tin chi tiết Nhân viên", 0, 0, new Font("Arial", Font.BOLD, 14), darkGray));


        JPanel detailInputPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // Grid for input fields
        detailInputPanel.setBackground(lightBeige);

        // Add input fields to detailInputPanel (DISPLAY ONLY)
        detailInputPanel.add(new JLabel("Mã NV:"));
        txtMaNV = new JTextField();
        txtMaNV.setEditable(false); // Not editable in this view
        detailInputPanel.add(txtMaNV);

        detailInputPanel.add(new JLabel("Tên NV:"));
        txtTenNV = new JTextField();
        txtTenNV.setEditable(false); // Not editable in this view
        detailInputPanel.add(txtTenNV);

        detailInputPanel.add(new JLabel("Địa chỉ:"));
        txtDiachi = new JTextField();
        txtDiachi.setEditable(false); // Not editable in this view
        detailInputPanel.add(txtDiachi);

        detailInputPanel.add(new JLabel("Giới tính:"));
        txtGioitinh = new JTextField();
        txtGioitinh.setEditable(false); // Not editable in this view
        detailInputPanel.add(txtGioitinh);

        detailInputPanel.add(new JLabel("SĐT:"));
        txtSDT = new JTextField();
        txtSDT.setEditable(false); // Not editable in this view
        detailInputPanel.add(txtSDT);

        // New fields for account info (DISPLAY ONLY)
        detailInputPanel.add(new JLabel("Tên đăng nhập:"));
        txtTendangnhap = new JTextField();
        txtTendangnhap.setEditable(false); // Not editable in this view
        detailInputPanel.add(txtTendangnhap);

        // REMOVED: Password field from display panel for better security
        // detailInputPanel.add(new JLabel("Mật khẩu:"));
        // txtMatkhau = new JPasswordField();
        // txtMatkhau.setEditable(false); // Not editable in this view
        // detailInputPanel.add(txtMatkhau);

        detailInputPanel.add(new JLabel("Email:"));
        txtEmail = new JTextField();
        txtEmail.setEditable(false); // Not editable in this view
        detailInputPanel.add(txtEmail);

        detailInputPanel.add(new JLabel("Role:"));
        txtRole = new JTextField();
        txtRole.setEditable(false); // Not editable in this view
        detailInputPanel.add(txtRole);


        detailPanel.add(detailInputPanel, BorderLayout.NORTH); // Use NORTH for input panel


        // Detail button panel (now in the CENTER or SOUTH of detailPanel)
        JPanel detailButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        detailButtonPanel.setBackground(lightBeige);

        btnAdd = new JButton("Thêm mới");
        styleButton(btnAdd, accentGreen, Color.WHITE);
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 // Open dialog for adding a new employee
                 openNhanVienDetailsDialog(null); // Pass null for adding
            }
        });
        detailButtonPanel.add(btnAdd);

        btnUpdate = new JButton("Cập nhật");
        styleButton(btnUpdate, accentBlue, Color.WHITE);
        btnUpdate.setEnabled(false); // Disable initially
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 // Open dialog for editing the selected employee
                 if (currentNhanVien != null) {
                     openNhanVienDetailsDialog(currentNhanVien); // Pass the selected employee
                 } else {
                     JOptionPane.showMessageDialog(NhanVienUI.this, "Vui lòng chọn nhân viên cần cập nhật.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                 }
            }
        });
        detailButtonPanel.add(btnUpdate);

        btnDelete = new JButton("Xóa");
        styleButton(btnDelete, accentOrange, Color.WHITE);
        btnDelete.setEnabled(false); // Disable initially
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteNhanVien();
            }
        });
        detailButtonPanel.add(btnDelete);

        btnClear = new JButton("Làm mới");
        styleButton(btnClear, darkGray, Color.WHITE);
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });
        detailButtonPanel.add(btnClear);

        detailPanel.add(detailButtonPanel, BorderLayout.SOUTH); // Button panel at the bottom


        centerPanel.add(detailPanel);


        add(centerPanel, BorderLayout.CENTER);


        // --- Permissions Check and initial state ---
        checkPermissions(); // Check permissions on load
        loadNhanVienTable(); // Load data into the table on startup
        // updateButtonState is called by checkPermissions and displayNhanVienDetails


    } // End of Constructor


    // --- Helper Methods ---

    // Apply styles to buttons
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


    // Load data from DAO into the table
    public void loadNhanVienTable() { // Make public so MainApplicationFrame can call it if needed
        tableModel.setRowCount(0);

        List<NhanVien> danhSachNhanVien = nhanVienDAO.getAllNhanVien();

        if (danhSachNhanVien != null) {
            for (NhanVien nv : danhSachNhanVien) {
                 // Add row to the table - MAKE SURE COLUMNS MATCH
                 tableModel.addRow(new Object[]{
                         nv.getMaNV(),
                         nv.getTenNV(),
                         nv.getDiachi(),
                         nv.getGioitinh(),
                         nv.getSDT(),
                         nv.getTendangnhap(),
                         nv.getEmail(),
                         nv.getRole()
                 });
            }
        }
        // Clear the form after loading table
        clearForm();
    }

    // Display details of the selected employee in the form
    private void displayNhanVienDetails(int rowIndex) {
        // Get data from the selected row (assuming MaNV is the first column)
        String maNV = (String) tableModel.getValueAt(rowIndex, 0);

        // Retrieve the full NhanVien object from the DAO to get all details (including account info)
        // Using getNhanVienByMaNV as we know the ID
        currentNhanVien = nhanVienDAO.getNhanVienById(maNV);


        if (currentNhanVien != null) {
            txtMaNV.setText(currentNhanVien.getMaNV());
            txtTenNV.setText(currentNhanVien.getTenNV());
            txtDiachi.setText(currentNhanVien.getDiachi());
            txtGioitinh.setText(currentNhanVien.getGioitinh());
            txtSDT.setText(currentNhanVien.getSDT());

            // Display account fields (read-only in this panel)
            txtTendangnhap.setText(currentNhanVien.getTendangnhap());
            // Password is NOT displayed here
            txtEmail.setText(currentNhanVien.getEmail());
            txtRole.setText(currentNhanVien.getRole());

            // Update button state based on selection and permissions
            updateButtonState();

        } else {
            clearForm();
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin chi tiết nhân viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

     // Clear all input fields
     private void clearForm() {
         txtMaNV.setText("");
         txtTenNV.setText("");
         txtDiachi.setText("");
         txtGioitinh.setText("");
         txtSDT.setText("");
         txtTendangnhap.setText("");
         // txtMatkhau.setText(""); // Password field removed
         txtEmail.setText("");
         txtRole.setText("");

         currentNhanVien = null; // No employee selected
         nhanVienTable.clearSelection(); // Clear table selection
         updateButtonState(); // Update button state
     }

    // Handle delete action
    private void deleteNhanVien() {
         if (currentNhanVien == null) {
             JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
             return;
         }

          // Prevent deleting oneself
          if (currentUser != null && currentNhanVien.getMaNV().equals(currentUser.getMaNV())) {
               JOptionPane.showMessageDialog(this, "Bạn không thể xóa tài khoản của chính mình.", "Lỗi", JOptionPane.ERROR_MESSAGE);
               return;
          }

         int confirm = JOptionPane.showConfirmDialog(this,
              "Bạn có chắc chắn muốn xóa nhân viên " + currentNhanVien.getTenNV() + " (Mã: " + currentNhanVien.getMaNV() + ") không?",
              "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

         if (confirm == JOptionPane.YES_OPTION) {
             // IMPORTANT: Consider cascading deletes or preventing deletion if employee is linked to other data (e.g., HoaDon)
             boolean success = nhanVienDAO.deleteNhanVien(currentNhanVien.getMaNV());
             if (success) {
                 JOptionPane.showMessageDialog(this, "Xóa nhân viên thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                 loadNhanVienTable(); // Reload table after deletion
                 clearForm(); // Clear form
             } else {
                  // Show a user-friendly message and potentially log the detailed error from DAO
                  JOptionPane.showMessageDialog(this,
                       "Không thể xóa nhân viên. Có lỗi xảy ra (kiểm tra log hoặc ràng buộc CSDL).",
                       "Lỗi xóa", JOptionPane.ERROR_MESSAGE);
             }
         }
    }


    // Handle search action
     private void performSearch() {
         String searchTerm = txtSearch.getText().trim();
         String searchType = (String) cbSearchType.getSelectedItem();
         List<NhanVien> searchResult = new ArrayList<>();

         if (searchTerm.isEmpty()) {
             loadNhanVienTable(); // Load all if search term is empty
             return;
         }

         switch (searchType) {
             case "Mã NV":
                 // getNhanVienById returns single, wrap in list for consistent processing
                 NhanVien foundById = nhanVienDAO.getNhanVienById(searchType);
                 if (foundById != null) searchResult.add(foundById);
                 break;
             case "Tên NV":
                 searchResult = nhanVienDAO.searchNhanVienByTenNV(searchTerm);
                 break;
             case "Địa chỉ":
                 searchResult = nhanVienDAO.searchNhanVienByDiaChi(searchTerm);
                 break;
              case "Tên đăng nhập":
                  // getNhanVienByTendangnhap returns single, wrap in list
                  NhanVien foundByUsername = nhanVienDAO.getNhanVienByTendangnhap(searchTerm);
                  if (foundByUsername != null) searchResult.add(foundByUsername);
                  break;
              case "Email":
                   // getNhanVienByEmail returns single, wrap in list
                   NhanVien foundByEmail = nhanVienDAO.getNhanVienByEmail(searchTerm);
                   if (foundByEmail != null) searchResult.add(foundByEmail);
                   break;
              case "Role":
                  // searchNhanVienByRole should return List
                  searchResult = nhanVienDAO.searchNhanVienByRole(searchTerm); // Assuming this method exists in DAO
                  break;
             default:
                 JOptionPane.showMessageDialog(this, "Loại tìm kiếm không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                 return;
         }

         // Update table with search results
         tableModel.setRowCount(0); // Clear current table data
         if (searchResult != null && !searchResult.isEmpty()) {
             for (NhanVien nv : searchResult) {
                 // Add row to the table - MAKE SURE COLUMNS MATCH
                 tableModel.addRow(new Object[]{
                          nv.getMaNV(),
                          nv.getTenNV(),
                          nv.getDiachi(),
                          nv.getGioitinh(),
                          nv.getSDT(),
                          nv.getTendangnhap(),
                          nv.getEmail(),
                          nv.getRole()
                 });
             }
         } else {
              JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả nào.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
         }
         clearForm(); // Clear form after search
     }


     // Open the detail dialog for adding or editing
     private void openNhanVienDetailsDialog(NhanVien nhanVienToEdit) {
         // Create and show the detail dialog
         // Pass the current user for permission checks within the dialog if needed (Optional, can be handled in UI)

         // Get the owner frame (usually the main application frame)
         Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);

         NhanVienDetailsDialog detailDialog;
         if (nhanVienToEdit == null) {
             // Adding new
             detailDialog = new NhanVienDetailsDialog(owner); // Use constructor for adding
         } else {
             // Editing existing
             detailDialog = new NhanVienDetailsDialog(owner, nhanVienToEdit); // Use constructor for updating
         }

         // Show the dialog (this call is blocking)
         detailDialog.setVisible(true);

         // After dialog is closed, check if changes were saved and refresh table
         if (detailDialog.isSaved()) { // Check the isSaved() flag
             loadNhanVienTable(); // Reload table to show updated data
             // Optionally select the row of the saved/updated employee
             // This requires getting the updated NhanVien object back from the dialog
             // or finding it in the table after reload (more complex).
         }
         // Clear the form regardless of save outcome, as dialog was closed
         clearForm();
     }


     // Check user permissions and enable/disable UI elements
     private void checkPermissions() {
          String role = currentUser.getRole();

          // Permissions for buttons are set by updateButtonState() based on row selection and role
          // Set initial state for buttons based on role when NO row is selected
          // Add button is enabled only for Admin
          btnAdd.setEnabled("Admin".equalsIgnoreCase(role));

          // Detail fields are read-only in this panel
          txtMaNV.setEditable(false);
          txtTenNV.setEditable(false);
          txtDiachi.setEditable(false);
          txtGioitinh.setEditable(false);
          txtSDT.setEditable(false);
          txtTendangnhap.setEditable(false);
          // txtMatkhau.setEditable(false); // Password field removed
          txtEmail.setEditable(false);
          txtRole.setEditable(false);


          updateButtonState(); // Set initial button state based on selection (none selected initially)
     }

      // Update button state based on row selection and permissions
      private void updateButtonState() {
          boolean rowSelected = nhanVienTable.getSelectedRow() >= 0;
          String role = currentUser.getRole();

          // Update button is enabled if a row is selected AND the user is Admin or Manager
          boolean canUpdate = rowSelected && ("Admin".equalsIgnoreCase(role) || "Manager".equalsIgnoreCase(role));

          // Delete button is only enabled if a row is selected AND the user is Admin
          boolean canDelete = rowSelected && "Admin".equalsIgnoreCase(role);

          // Specific check: Manager cannot update or delete Admin accounts
          if (rowSelected && !("Admin".equalsIgnoreCase(role))) { // If not Admin
               // Get the role of the selected employee (Role is column 7)
               String selectedEmployeeRole = (String) tableModel.getValueAt(nhanVienTable.getSelectedRow(), 7);
               if ("Admin".equalsIgnoreCase(selectedEmployeeRole)) {
                    // If selected employee is Admin, Manager/Staff/Guest cannot update/delete them
                    canUpdate = false;
                    canDelete = false;
               }
          }


          btnUpdate.setEnabled(canUpdate);
          btnDelete.setEnabled(canDelete);

          // Add button state is set by checkPermissions() based on role when *no* row is selected.
          // When a row *is* selected, we generally still allow adding if the user has permission.
          // So, btnAdd state depends only on the user's role, not the selection state.
          // It's better to set btnAdd state only in checkPermissions().

          // Clear button is always enabled
          btnClear.setEnabled(true);
      }


    // --- Main method for testing (Optional - Should be in MainApplicationFrame) ---

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
             // This test requires a running database with NhanVien table
             // and the DatabaseConnection configured correctly.

             // Create sample NhanVien objects for testing different roles
             // Adjust constructor if needed based on your NhanVien class structure
            //  NhanVien sampleAdminUser = new NhanVien("NV001", "Admin User", "Admin Address", "Nam", "0123456789", "admin_user", "hashed_admin_password", "admin@example.com", "Admin");
            //  NhanVien sampleManagerUser = new NhanVien("NV002", "Manager User", "Manager Address", "Nu", "0987654321", "manager_user", "hashed_manager_password", "manager@example.com", "Manager");
            //  NhanVien sampleStaffUser = new NhanVien("NV003", "Staff User", "Staff Address", "Nam", "0909090909", "staff_user", "hashed_staff_password", "staff@example.com", "Staff");


            JFrame frame = new JFrame("Quản lý Nhân viên Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1000, 700);
            frame.setLocationRelativeTo(null);
             // Pass a sample NhanVien object representing the logged-in user
            //  frame.add(new NhanVienUI(sampleAdminUser)); // Test with Admin user (Change user object to test other roles)

            frame.setVisible(true);
        });
    }

}