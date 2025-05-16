package ui;

import model.NhaCC; // Import model NhaCC
import model.NhanVien; // Import NhanVien for current user/permissions (optional for NhaCC, but consistent)
import dao.NhaCCDAO; // Import NhaCCDAO

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


public class NhaCungCapUI extends JPanel {

    // Define colors (reusing the palette)
    Color coffeeBrown = new Color(102, 51, 0);
    Color lightBeige = new Color(245, 245, 220);
    Color accentGreen = new Color(60, 179, 113); // Color for Add button
    Color accentOrange = new Color(255, 165, 0); // Color for Delete button
    Color darkGray = new Color(50, 50, 50);
    Color accentBlue = new Color(30, 144, 255); // Color for Search button

    // UI Components (Input Fields and Buttons in detail panel)
    private JTextField txtMaNCC;
    private JTextField txtTenNCC;
    private JTextField txtDiachi;
    private JTextField txtSDT;


    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear; // Add a clear button
    private JButton btnSearch;
    private JTextField txtSearch;
    private JComboBox<String> cbSearchType; // For selecting search criteria

    // Table components
    private JTable nhaCungCapTable;
    private DefaultTableModel tableModel;
    private JScrollPane tableScrollPane;

    // Data Access Object
    private NhaCCDAO nhaCCDAO;

    // Current selected NhaCC
    private NhaCC currentNhaCC;

     // User account for permission checking (optional for NhaCC, but consistent)
    private NhanVien currentUser;


    // Constructor
    public NhaCungCapUI(NhanVien currentUser) { // Accept current user for potential permissions
        this.currentUser = currentUser;
        nhaCCDAO = new NhaCCDAO();
        currentNhaCC = null; // No supplier selected initially

        initComponents(); // Setup UI components - This is where the layout is defined

        // --- Permissions Check and initial state ---
        // applyPermissions(); // Permissions based on currentUser role (Optional)
         loadNhaCCTable(); // Load data into the table on startup
         updateButtonState(); // Set initial button state


    } // End of Constructor


    // --- Initialize Components and Layout ---
    private void initComponents() {
        setLayout(new BorderLayout(10, 10)); // BorderLayout with gaps
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(lightBeige);

        // --- Top Panel (Search) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); // FlowLayout for search components
        searchPanel.setBackground(lightBeige);

        cbSearchType = new JComboBox<>(new String[]{"Mã NCC", "Tên NCC", "Địa chỉ", "SĐT"}); // Search types

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
        String[] columnNames = {"Mã NCC", "Tên NCC", "Địa chỉ", "SĐT"};

        tableModel = new DefaultTableModel(columnNames, 0) {
             // Make columns non-editable
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Users edit data via the form on the right
            }
        };
        nhaCungCapTable = new JTable(tableModel);
        nhaCungCapTable.setFillsViewportHeight(true); // Use entire viewport height
        nhaCungCapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only allow single row selection
        nhaCungCapTable.getTableHeader().setBackground(coffeeBrown);
        nhaCungCapTable.getTableHeader().setForeground(Color.WHITE);
        nhaCungCapTable.setBackground(lightBeige);
        nhaCungCapTable.setForeground(darkGray);
        nhaCungCapTable.setRowHeight(25); // Slightly taller rows

        // Set renderer for center alignment if needed (optional)
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < nhaCungCapTable.getColumnCount(); i++) {
            nhaCungCapTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }


        tableScrollPane = new JScrollPane(nhaCungCapTable);
        tableScrollPane.getViewport().setBackground(lightBeige); // Match background color


        // Add row selection listener to display selected supplier details
        nhaCungCapTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = nhaCungCapTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // Load data from the selected row into the detail panel
                    displayNhaCCDetails(selectedRow);
                    // Enable/disable buttons based on selection and permissions
                     updateButtonState();
                }
            }
        });

        centerPanel.add(tableScrollPane);


        // --- Right Side: Detail Panel ---
        JPanel detailPanel = new JPanel(new BorderLayout(10, 10)); // Detail panel layout
        detailPanel.setBackground(lightBeige);
        detailPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(darkGray), "Thông tin chi tiết Nhà cung cấp", 0, 0, new Font("Arial", Font.BOLD, 14), darkGray));


        // Use GridBagLayout for better form alignment
        JPanel detailInputPanel = new JPanel(new GridBagLayout());
        detailInputPanel.setBackground(lightBeige);
        // Add padding and a simple border to the input panel
        detailInputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(darkGray, 1), // Add a line border
                BorderFactory.createEmptyBorder(15, 15, 15, 15))); // Increased padding inside the border

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5); // Increased vertical padding between rows

        // Labels (right-aligned, slightly less horizontal weight)
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.insets = new Insets(7, 5, 7, 5);
        labelGbc.gridx = 0;
        labelGbc.anchor = GridBagConstraints.EAST; // Align labels to the right
        labelGbc.weightx = 0.1; // Give label column a small weight (optional, can be 0)
        labelGbc.fill = GridBagConstraints.NONE; // Labels don't need to fill


        // Text Fields (fill horizontally, take most horizontal weight)
        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.insets = new Insets(7, 5, 7, 5);
        fieldGbc.gridx = 1;
        fieldGbc.weightx = 1.0; // Allow fields to take most horizontal space
        fieldGbc.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally


        // Row 0: MaNCC
        labelGbc.gridy = 0; detailInputPanel.add(new JLabel("Mã NCC:"), labelGbc);
        fieldGbc.gridy = 0;
        txtMaNCC = new JTextField(20); // Initial columns hint
        txtMaNCC.setEditable(true); // MaNCC might be generated but user might edit
        detailInputPanel.add(txtMaNCC, fieldGbc);

        // Row 1: TenNCC
        labelGbc.gridy = 1; detailInputPanel.add(new JLabel("Tên NCC:"), labelGbc);
        fieldGbc.gridy = 1;
        txtTenNCC = new JTextField(20);
        detailInputPanel.add(txtTenNCC, fieldGbc);

        // Row 2: Diachi
        labelGbc.gridy = 2; detailInputPanel.add(new JLabel("Địa chỉ:"), labelGbc);
        fieldGbc.gridy = 2;
        txtDiachi = new JTextField(20);
        detailInputPanel.add(txtDiachi, fieldGbc);

        // Row 3: SDT
        labelGbc.gridy = 3; detailInputPanel.add(new JLabel("SĐT:"), labelGbc);
        fieldGbc.gridy = 3;
        txtSDT = new JTextField(20);
        detailInputPanel.add(txtSDT, fieldGbc);

        // Add some vertical space at the bottom to push components up (optional)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2; gbc.weighty = 1.0; // Give this row vertical weight
        gbc.fill = GridBagConstraints.VERTICAL; // Allow vertical filling for the glue
        detailInputPanel.add(Box.createVerticalGlue(), gbc); // Add a vertical glue


        detailPanel.add(detailInputPanel, BorderLayout.CENTER);

        // Detail button panel
        JPanel detailButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        detailButtonPanel.setBackground(lightBeige);

        btnAdd = new JButton("Thêm mới");
        styleButton(btnAdd, accentGreen, Color.WHITE);
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNhaCC();
            }
        });
        detailButtonPanel.add(btnAdd);

        btnUpdate = new JButton("Cập nhật");
        styleButton(btnUpdate, accentBlue, Color.WHITE);
         btnUpdate.setEnabled(false); // Disable initially
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateNhaCC();
            }
        });
        detailButtonPanel.add(btnUpdate);

        btnDelete = new JButton("Xóa");
        styleButton(btnDelete, accentOrange, Color.WHITE);
        btnDelete.setEnabled(false); // Disable initially
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteNhaCC();
            }
        });
        detailButtonPanel.add(btnDelete);

        btnClear = new JButton("Làm mới"); // Button to clear the form
        styleButton(btnClear, darkGray, Color.WHITE);
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });
        detailButtonPanel.add(btnClear);


        detailPanel.add(detailButtonPanel, BorderLayout.SOUTH);


        centerPanel.add(detailPanel); // Add detail panel to the right side of center panel


        add(centerPanel, BorderLayout.CENTER);

    } // End of initComponents


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
    private void loadNhaCCTable() {
        // Clear existing data
        tableModel.setRowCount(0);

        List<NhaCC> danhSachNhaCC = nhaCCDAO.getAllNhaCC();

        if (danhSachNhaCC != null) {
            for (NhaCC ncc : danhSachNhaCC) {
                // Add row to the table
                tableModel.addRow(new Object[]{
                        ncc.getMaNCC(),
                        ncc.getTenNCC(),
                        ncc.getDiachi(),
                        ncc.getSDT()
                });
            }
        }
         // Clear the form after loading table
         clearForm();
    }

    // Display details of the selected supplier in the form
    private void displayNhaCCDetails(int rowIndex) {
        // Get MaNCC from the table row
        String maNCC = (String) tableModel.getValueAt(rowIndex, 0); // Assuming MaNCC is the first column

        // Retrieve the full NhaCC object from the DAO
        currentNhaCC = nhaCCDAO.getNhaCCByMaNCC(maNCC);

        if (currentNhaCC != null) {
            txtMaNCC.setText(currentNhaCC.getMaNCC());
            txtTenNCC.setText(currentNhaCC.getTenNCC());
            txtDiachi.setText(currentNhaCC.getDiachi());
            txtSDT.setText(currentNhaCC.getSDT());


             // Update button state after displaying details
             updateButtonState();

        } else {
            // Handle case where NhaCC is not found (shouldn't happen if loaded from table)
             clearForm();
             JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin chi tiết nhà cung cấp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

     // Clear all input fields and suggest next MaNCC
     private void clearForm() {
         txtMaNCC.setText(nhaCCDAO.suggestNextMaNCC()); // Suggest next code
         txtTenNCC.setText("");
         txtDiachi.setText("");
         txtSDT.setText("");

         currentNhaCC = null; // No supplier selected
         nhaCungCapTable.clearSelection(); // Clear table selection
         updateButtonState(); // Update button state
         txtMaNCC.setEditable(false); // Make MaNCC non-editable when adding (generated)
     }

     // Handle add action
     private void addNhaCC() {
         String maNCC = txtMaNCC.getText().trim(); // Get suggested code
         String tenNCC = txtTenNCC.getText().trim();
         String diachi = txtDiachi.getText().trim();
         String sdt = txtSDT.getText().trim();

         // Basic validation
         if (maNCC.isEmpty() || tenNCC.isEmpty() || diachi.isEmpty() || sdt.isEmpty()) {
              JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin nhà cung cấp.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
              return;
         }

         // Create new NhaCC object
         NhaCC newNhaCC = new NhaCC(maNCC, tenNCC, diachi, sdt);

         // Call DAO method to add
         try {
             boolean success = nhaCCDAO.addNhaCC(newNhaCC);
             if (success) {
                 JOptionPane.showMessageDialog(this, "Thêm nhà cung cấp thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                 loadNhaCCTable(); // Reload table
                 clearForm(); // Clear form and suggest next code
             } else {
                 // DAO's add method prints detailed error (like duplicate MaNCC)
                  JOptionPane.showMessageDialog(this,
                       "Thêm nhà cung cấp thất bại. Có thể Mã NCC đã tồn tại hoặc lỗi khác.",
                       "Lỗi", JOptionPane.ERROR_MESSAGE);
             }
         } catch (Exception e) { // Catch potential exceptions from DAO (less likely with boolean return but good practice)
              JOptionPane.showMessageDialog(this, "Lỗi khi thêm nhà cung cấp: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
              e.printStackTrace();
         }
     }

    // Handle update action
    private void updateNhaCC() {
        if (currentNhaCC == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần cập nhật.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

         String maNCC = txtMaNCC.getText().trim(); // Should match currentNhaCC.getMaNCC()
         String tenNCC = txtTenNCC.getText().trim();
         String diachi = txtDiachi.getText().trim();
         String sdt = txtSDT.getText().trim();


         // Basic validation
         if (maNCC.isEmpty() || tenNCC.isEmpty() || diachi.isEmpty() || sdt.isEmpty()) {
              JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin nhà cung cấp.", "Lỗi nhập liệu", JOptionPane.WARNING_MESSAGE);
              return;
         }
          // Ensure the MaNCC in the field matches the selected item's MaNCC (should be the case if MaNCC is not editable when selected)
         if (!maNCC.equals(currentNhaCC.getMaNCC())) {
             JOptionPane.showMessageDialog(this, "Mã NCC đã chọn không khớp với dữ liệu trên form.", "Lỗi", JOptionPane.ERROR_MESSAGE);
             // You might want to reload details here
             return;
         }


         // Update the currentNhaCC object with new data from fields
         currentNhaCC.setTenNCC(tenNCC);
         currentNhaCC.setDiachi(diachi);
         currentNhaCC.setSDT(sdt);
         // MaNCC is the key, should not be changed via update form


         // Call DAO method to update
         try {
              boolean success = nhaCCDAO.updateNhaCC(currentNhaCC);
              if (success) {
                 JOptionPane.showMessageDialog(this, "Cập nhật nhà cung cấp thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                 loadNhaCCTable(); // Reload table
                 clearForm(); // Clear form
             } else {
                  JOptionPane.showMessageDialog(this, "Cập nhật nhà cung cấp thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
             }
         } catch (Exception e) {
              JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật nhà cung cấp: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
              e.printStackTrace();
         }
    }


    // Handle delete action
    private void deleteNhaCC() {
         if (currentNhaCC == null) {
             JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
             return;
         }

         int confirm = JOptionPane.showConfirmDialog(this,
             "Bạn có chắc chắn muốn xóa nhà cung cấp " + currentNhaCC.getTenNCC() + " (Mã: " + currentNhaCC.getMaNCC() + ") không?",
             "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

         if (confirm == JOptionPane.YES_OPTION) {
             try {
                 boolean success = nhaCCDAO.deleteNhaCC(currentNhaCC.getMaNCC());
                 if (success) {
                     JOptionPane.showMessageDialog(this, "Xóa nhà cung cấp thành công.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                     loadNhaCCTable(); // Reload table after deletion
                     clearForm(); // Clear form
                 } else {
                      // The DAO's delete method already prints detailed error (like foreign key)
                      JOptionPane.showMessageDialog(this,
                          "Không thể xóa nhà cung cấp. Có lỗi xảy ra (kiểm tra log hoặc ràng buộc CSDL).",
                          "Lỗi xóa", JOptionPane.ERROR_MESSAGE);
                 }
             } catch (Exception e) { // Catch potential exceptions
                   JOptionPane.showMessageDialog(this,
                      "Lỗi khi xóa nhà cung cấp: " + e.getMessage(),
                      "Lỗi xóa", JOptionPane.ERROR_MESSAGE);
                   e.printStackTrace();
             }
         }
    }


    // Handle search action
     private void performSearch() {
         String searchTerm = txtSearch.getText().trim();
         String searchType = (String) cbSearchType.getSelectedItem();
         List<NhaCC> searchResult = new ArrayList<>();

         if (searchTerm.isEmpty()) {
             loadNhaCCTable(); // Load all if search term is empty
             return;
         }

         switch (searchType) {
             case "Mã NCC":
                 // Get single supplier by ID and add to list if found
                 NhaCC foundById = nhaCCDAO.getNhaCCByMaNCC(searchTerm);
                 if (foundById != null) {
                     searchResult.add(foundById);
                 }
                 break;
             case "Tên NCC":
                 // Search by name (returns list)
                 searchResult = nhaCCDAO.searchNhaCCByName(searchTerm);
                 break;
             case "Địa chỉ":
                  // Need to implement search by DiaChi in NhaCCDAO
                  // searchResult = nhaCCDAO.searchNhaCCByDiaChi(searchTerm); // Example if method exists
                  JOptionPane.showMessageDialog(this, "Tìm kiếm theo Địa chỉ chưa được triển khai trong DAO.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                  return; // Exit switch if not implemented
             case "SĐT":
                 // Need to implement search by SDT in NhaCCDAO
                 // searchResult = nhaCCDAO.searchNhaCCBySDT(searchTerm); // Example if method exists
                  JOptionPane.showMessageDialog(this, "Tìm kiếm theo SĐT chưa được triển khai trong DAO.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                  return; // Exit switch if not implemented
             default:
                 // Should not happen
                 JOptionPane.showMessageDialog(this, "Loại tìm kiếm không hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                 return;
         }

         // Update table with search results (only proceed if a search method was actually called and returned results)
          // Check if searchResult was populated by a handled case (Mã NCC or Tên NCC)
          boolean handledSearch = "Mã NCC".equals(searchType) || "Tên NCC".equals(searchType);

          if (handledSearch) {
               tableModel.setRowCount(0); // Clear current table data
               if (searchResult != null) { // searchResult can be empty but not null if no results found
                   for (NhaCC ncc : searchResult) {
                       // Add row to the table
                       tableModel.addRow(new Object[]{
                               ncc.getMaNCC(),
                               ncc.getTenNCC(),
                               ncc.getDiachi(),
                               ncc.getSDT()
                       });
                   }
               }

               if (searchResult == null || searchResult.isEmpty()) { // More accurate check for no results
                    JOptionPane.showMessageDialog(this, "Không tìm thấy kết quả nào.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
               }
          } // else: If search type was not handled (Dia chi, SDT), message was already shown, do nothing with table.


          clearForm(); // Clear form after search
     }


     // Update button state based on row selection and permissions
     // Simple state: Add enabled if no row selected, Update/Delete enabled if row selected
     // Permissions based on NhanVien role can be added here if needed
      private void updateButtonState() {
          boolean rowSelected = nhaCungCapTable.getSelectedRow() >= 0;

          // Basic state:
          // Add enabled if no row selected (to enter new data)
          // Update/Delete enabled if row selected (to modify/remove selected data)
          btnAdd.setEnabled(!rowSelected);
          btnUpdate.setEnabled(rowSelected);
          btnDelete.setEnabled(rowSelected);

          // When a row is selected (editing), MaNCC should not be edited
          txtMaNCC.setEditable(!rowSelected); // Allow editing MaNCC only when form is cleared (adding new)


          // Add permission logic here using currentUser.getRole() if needed
          // Example: Only Admin/Manager can add/update/delete
          // String role = currentUser.getRole();
          // if (!"Admin".equalsIgnoreCase(role) && !"Manager".equalsIgnoreCase(role)) {
          //    btnAdd.setEnabled(false);
          //    btnUpdate.setEnabled(false);
          //    btnDelete.setEnabled(false);
          //    setFieldsEditable(false); // Disable editing input fields
          // } else {
               // Manager/Admin specific permissions can go here
               // setFieldsEditable(true); // Enable fields by default for allowed roles
               // txtMaNCC.setEditable(!rowSelected); // MaNCC remains uneditable when editing
          // }


           // Clear button is always enabled
           btnClear.setEnabled(true);
      }

    // Helper to set editability of input fields (used if adding role-based field editability)
    /*
     private void setFieldsEditable(boolean editable) {
         // Be careful with making MaNCC editable! It's the primary key.
         // txtMaNCC.setEditable(editable); // Use updateButtonState to control this instead
         txtTenNCC.setEditable(editable);
         txtDiachi.setEditable(editable);
         txtSDT.setEditable(editable);
     }
     */


    // --- Main method for testing (Optional - Should be in MainApplicationFrame) ---

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
             // This test requires a running database with NhaCC table
             // and the DatabaseConnection configured correctly.
             // Create a dummy NhanVien for permissions if needed (can be null if no permission logic)
             NhanVien sampleAdminUser = new NhanVien("NV001", "Admin User", "Admin Address", "Nam", "0123456789", "admin", "hashed_admin_password", "admin@example.com");


            JFrame frame = new JFrame("Quản lý Nhà cung cấp Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600); // Adjusted size
            frame.setLocationRelativeTo(null);

            // Pass a sample NhanVien object (can be null if no permission logic)
            frame.add(new NhaCungCapUI(sampleAdminUser)); // Pass a user or null

            frame.setVisible(true);
        });
    }

}
