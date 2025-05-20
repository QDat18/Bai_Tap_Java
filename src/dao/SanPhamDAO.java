package dao;

import java.sql.Connection; // Import model SanPham
import java.sql.PreparedStatement; // Import model Loai if needed for search results (e.g., setting Loai object or TenLoai)
import java.sql.ResultSet; // Assuming you have a DatabaseConnection class
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.SanPham;

public class SanPhamDAO {

    // --- CRUD Operations ---

    /**
     * Adds a new SanPham record to the database.
     *
     * @param sanPham The SanPham object to add.
     * @return true if the insertion is successful, false otherwise.
     */
    public boolean addSanPham(SanPham sanPham) {
        // Corrected SQL to match the table structure used in other methods (Soluong instead of Soluongton)
        String sql = "INSERT INTO SanPham (MaSP, TenSP, Maloai, Gianhap, Giaban, Soluong, Anh) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); // Get connection
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sanPham.getMaSP());
            pstmt.setString(2, sanPham.getTenSP());
            pstmt.setString(3, sanPham.getMaloai());
            pstmt.setInt(4, sanPham.getGianhap());
            pstmt.setInt(5, sanPham.getGiaban());
            pstmt.setInt(6, sanPham.getSoluong()); // Use Soluong
            pstmt.setString(7, sanPham.getAnh());

            int affectedRows = pstmt.executeUpdate(); // Execute and get affected rows count

            // executeUpdate() usually returns > 0 for successful INSERT
            if (affectedRows > 0) {
                System.out.println("Thêm sản phẩm thành công: " + sanPham.getTenSP());
                return true; // Return true on success
            } else {
                // This case is less common for simple INSERT without exceptions
                System.err.println("Thêm sản phẩm thất bại: Không có dòng nào được thêm.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Lỗi CSDL khi thêm sản phẩm:");
            e.printStackTrace();
            // Consider checking SQLState for specific errors like duplicate primary key
            return false; // Return false on error
        } catch (Exception e) {
            System.err.println("Lỗi ngoại lệ khi thêm sản phẩm:");
            e.printStackTrace();
            return false; // Catch other exceptions
        }
    }

    /**
     * Updates an existing SanPham record in the database.
     *
     * @param sanPham The SanPham object with updated information.
     * @return true if the update is successful (at least one row affected), false otherwise.
     */
    public boolean updateSanPham(SanPham sanPham) {
        // Corrected SQL to match the table structure (Soluong instead of Soluongton)
        String sql = "UPDATE SanPham SET TenSP = ?, Maloai = ?, Gianhap = ?, Giaban = ?, Soluong = ?, Anh = ? WHERE MaSP = ?";
        try (Connection conn = DatabaseConnection.getConnection(); // Get connection
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sanPham.getTenSP());
            pstmt.setString(2, sanPham.getMaloai());
            pstmt.setInt(3, sanPham.getGianhap());
            pstmt.setInt(4, sanPham.getGiaban());
            pstmt.setInt(5, sanPham.getSoluong()); // Use Soluong
            pstmt.setString(6, sanPham.getAnh());
            pstmt.setString(7, sanPham.getMaSP()); // WHERE condition

            int affectedRows = pstmt.executeUpdate(); // Execute and get affected rows count

            if (affectedRows > 0) {
                System.out.println("Cập nhật sản phẩm thành công: " + sanPham.getMaSP());
                return true; // Return true if at least 1 row was affected (update successful)
            } else {
                System.out.println("Không tìm thấy sản phẩm để cập nhật hoặc dữ liệu không thay đổi: " + sanPham.getMaSP());
                return false; // Return false if no rows were affected
            }
        } catch (SQLException e) {
            System.err.println("Lỗi CSDL khi cập nhật sản phẩm:");
            e.printStackTrace();
            return false; // Return false on error
        } catch (Exception e) {
            System.err.println("Lỗi ngoại lệ khi cập nhật sản phẩm:");
            e.printStackTrace();
            return false; // Catch other exceptions
        }
    }

    public String getLastProductId() {
        String lastId = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT TOP 1 MaSP FROM SanPham ORDER BY MaSP DESC";
            
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                lastId = rs.getString("MaSP");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy mã sản phẩm cuối: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return lastId;
    }
    /**
     * Deletes a SanPham record from the database by its ID.
     *
     * @param maSP The ID of the SanPham to delete.
     * @return true if the deletion is successful (at least one row affected), false otherwise.
     */
    public boolean deleteSanPham(String maSP) {
        String sql = "DELETE FROM SanPham WHERE MaSP = ?";
        try (Connection conn = DatabaseConnection.getConnection(); // Get connection
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maSP);

            int affectedRows = pstmt.executeUpdate(); // Execute and get affected rows count

            if (affectedRows > 0) {
                System.out.println("Xóa sản phẩm thành công: " + maSP);
                return true; // Return true if at least 1 row was affected (deletion successful)
            } else {
                System.out.println("Không tìm thấy sản phẩm để xóa: " + maSP);
                return false; // Return false if no rows were affected
            }
        } catch (SQLException e) {
            System.err.println("Lỗi CSDL khi xóa sản phẩm:");
            e.printStackTrace();
            // Consider checking SQLState for foreign key constraint violations (e.g., product is in a sales detail)
            return false; // Return false on error
        } catch (Exception e) {
            System.err.println("Lỗi ngoại lệ khi xóa sản phẩm:");
            e.printStackTrace();
            return false; // Catch other exceptions
        }
    }

    /**
     * Retrieves all SanPham records from the database.
     *
     * @return A list of all SanPham objects, or an empty list if none are found or an error occurs.
     */
    public List<SanPham> getAllSanPham() {
        List<SanPham> danhSachSanPham = new ArrayList<>();
        // Corrected SQL to select all columns needed by SanPham model
        String sql = "SELECT MaSP, TenSP, Maloai, Gianhap, Giaban, Soluong, Anh FROM SanPham"; // Use Soluong

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                SanPham sanPham = new SanPham();
                sanPham.setMaSP(rs.getString("MaSP"));
                sanPham.setTenSP(rs.getString("TenSP"));
                sanPham.setMaloai(rs.getString("Maloai"));
                sanPham.setGianhap(rs.getInt("Gianhap")); // Get int
                sanPham.setGiaban(rs.getInt("Giaban"));  // Get int
                sanPham.setSoluong(rs.getInt("Soluong")); // Get int (Soluong from DB)
                sanPham.setAnh(rs.getString("Anh"));
                danhSachSanPham.add(sanPham);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách sản phẩm:");
            e.printStackTrace();
            // Return empty list instead of null on error for better UI handling
            return new ArrayList<>();
        }
        return danhSachSanPham;
    }

    /**
     * Retrieves a SanPham record from the database by its ID.
     *
     * @param maSP The ID of the SanPham to retrieve.
     * @return The SanPham object if found, or null otherwise.
     */
    public SanPham getSanPhamById(String maSP) {
        SanPham sanPham = null;
        // Corrected SQL to select all columns needed by SanPham model
        String sql = "SELECT MaSP, TenSP, Maloai, Gianhap, Giaban, Soluong, Anh FROM SanPham WHERE MaSP = ?"; // Use Soluong

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maSP);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    sanPham = new SanPham();
                    sanPham.setMaSP(rs.getString("MaSP"));
                    sanPham.setTenSP(rs.getString("TenSP"));
                    sanPham.setMaloai(rs.getString("Maloai"));
                    sanPham.setGianhap(rs.getInt("Gianhap"));
                    sanPham.setGiaban(rs.getInt("Giaban"));
                    sanPham.setSoluong(rs.getInt("Soluong")); // Use Soluong
                    sanPham.setAnh(rs.getString("Anh"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy sản phẩm theo mã:");
            e.printStackTrace();
        }
        return sanPham; // Return null if not found or error
    }


    // --- Stock Management ---

    /**
     * Updates the stock quantity for a product within a database transaction.
     * This method is designed to be called from transactional methods (e.g., in HoaDonBanDAO).
     *
     * @param conn The database connection for the transaction.
     * @param maSP The ID of the product to update.
     * @param quantityChange The amount to change the stock by (positive for increase, negative for decrease).
     * @throws SQLException if a database error occurs.
     */
    public void updateStockQuantity(Connection conn, String maSP, int quantityChange) throws SQLException {
        // Corrected SQL to use "Soluong" column
        String sql = "UPDATE SanPham SET Soluong = Soluong + ? WHERE MaSP = ?";
        // Use the provided connection
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, quantityChange);
            pstmt.setString(2, maSP);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                // This might indicate the product wasn't found, which could be an issue
                System.err.println("Cảnh báo: Không tìm thấy sản phẩm để cập nhật tồn kho (MaSP: " + maSP + ").");
                // Depending on logic, you might throw an exception here if product must exist
                // throw new SQLException("Product not found for stock update: " + maSP);
            } else {
                System.out.println("Đã cập nhật tồn kho cho sản phẩm: " + maSP + ", thay đổi: " + quantityChange);
            }
        }
        // Do not close connection here, it's managed by the caller (e.g., HoaDonBanDAO)
    }


    /**
     * Retrieves the current stock quantity for a product.
     *
     * @param maSP The ID of the product.
     * @return The stock quantity, or 0 if the product is not found or an error occurs.
     */
    public int getStockQuantity(String maSP) {
        int stock = 0;
        // Corrected SQL to use "Soluong" column
        String sql = "SELECT Soluong FROM SanPham WHERE MaSP = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maSP);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stock = rs.getInt("Soluong"); // Use Soluong
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy số lượng tồn kho:");
            e.printStackTrace();
        }
        return stock;
    }

    /**
     * Retrieves a list of products with stock quantity less than or equal to a given threshold.
     *
     * @param threshold The maximum stock quantity for products to be included.
     * @return A list of SanPham objects (with MaSP, TenSP, Soluong) that are low in stock, or an empty list on error.
     */
    public List<SanPham> getLowStockProducts(int threshold) {
        List<SanPham> lowStockList = new ArrayList<>();
        // SQL query to get products with stock quantity <= threshold
        // Select necessary columns: MaSP, TenSP, Soluong
        // Corrected SQL to use "Soluong" column
        String sql = "SELECT MaSP, TenSP, Soluong FROM SanPham WHERE Soluong <= ?";

        try (Connection conn = DatabaseConnection.getConnection(); // Get database connection
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, threshold); // Set threshold parameter

            try (ResultSet rs = pstmt.executeQuery()) {
                // Iterate through the result set
                while (rs.next()) {
                    // Create a SanPham object for each result row
                    SanPham sp = new SanPham();
                    // Populate SanPham object with data from ResultSet
                    sp.setMaSP(rs.getString("MaSP"));
                    sp.setTenSP(rs.getString("TenSP"));
                    sp.setSoluong(rs.getInt("Soluong")); // Use Soluong

                    // Add the product to the list
                    lowStockList.add(sp);
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi tải danh sách Sản phẩm tồn kho thấp:");
            e.printStackTrace();
            // Return empty list on error
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while getting low stock products: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }

        return lowStockList; // Return the list of low stock products
    }

    /**
     * Gets the count of products with stock quantity less than or equal to a given threshold.
     *
     * @param threshold The maximum stock quantity for products to be counted.
     * @return The count of low stock products, or 0 on error.
     */
    public int getLowStockCount(int threshold) {
        // Re-using getLowStockProducts to get the count is simple but might be less efficient
        // for large datasets compared to a direct COUNT(*) query.
        // For now, this is acceptable.
        List<SanPham> lowStockProducts = getLowStockProducts(threshold);
        return lowStockProducts.size(); // Return the size of the list
    }


    // --- Search Operation (Added) ---

    /**
     * Searches for SanPham records based on the specified criteria and search term.
     * Can search by product name, product ID, or product category name.
     *
     * @param criteria   The search criteria ("Tên SP", "Mã SP", "Loại SP").
     * @param searchTerm The term to search for.
     * @return A list of matching SanPham objects, or an empty list if none are found or an error occurs.
     */
    public List<SanPham> searchSanPham(String criteria, String searchTerm) {
        List<SanPham> sanPhamList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // Base SQL query. We will add the WHERE clause based on the criteria.
        // We join with the Loai table to be able to search by category name.
        // Select necessary columns for the SanPham model.
        // Corrected SQL to use "Soluong" column
        String sql = "SELECT sp.MaSP, sp.TenSP, sp.Maloai, sp.Gianhap, sp.Giaban, sp.Soluong, sp.Anh, l.TenLoai " +
                     "FROM SanPham sp " +
                     "JOIN Loai l ON sp.Maloai = l.MaLoai " + // Join with Loai table
                     "WHERE "; // Placeholder for WHERE clause

        // Build the WHERE clause based on the criteria
        switch (criteria) {
            case "Tên SP":
                sql += "sp.TenSP LIKE ?"; // Search by product name
                searchTerm = "%" + searchTerm + "%"; // Use LIKE for partial matching
                break;
            case "Mã SP":
                sql += "sp.MaSP LIKE ?"; // Search by product ID
                searchTerm = "%" + searchTerm + "%"; // Use LIKE for partial matching
                break;
            case "Loại SP":
                sql += "l.TenLoai LIKE ?"; // Search by category name from Loai table
                searchTerm = "%" + searchTerm + "%"; // Use LIKE for partial matching
                break;
            default:
                // If an invalid criteria is provided, log an error and return an empty list
                System.err.println("Invalid search criteria: " + criteria);
                return sanPhamList; // Return empty list
        }

        try {
            conn = DatabaseConnection.getConnection();
            pstmt = conn.prepareStatement(sql);

            // Set the search term parameter
            pstmt.setString(1, searchTerm);

            rs = pstmt.executeQuery();

            // Process the results
            while (rs.next()) {
                SanPham sp = new SanPham();
                sp.setMaSP(rs.getString("MaSP"));
                sp.setTenSP(rs.getString("TenSP"));
                sp.setMaloai(rs.getString("Maloai")); // Set Maloai
                // Optional: You might want to set the full Loai object or TenLoai in the SanPham model
                // sp.setTenloai(rs.getString("TenLoai")); // If SanPham model has setTenloai

                sp.setGianhap(rs.getInt("Gianhap"));
                sp.setGiaban(rs.getInt("Giaban"));
                sp.setSoluong(rs.getInt("Soluong")); // Use Soluong from DB
                sp.setAnh(rs.getString("Anh"));

                sanPhamList.add(sp);
            }

        } catch (SQLException e) {
            System.err.println("Error searching SanPham:");
            e.printStackTrace();
            // Return empty list instead of null on error for consistency
            return new ArrayList<>();
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources after search:");
                e.printStackTrace();
            }
        }

        return sanPhamList;
    }

    // --- Specific Search Methods (Already in user's code) ---
    // These methods were already present in the user's provided code.
    // Keeping them as they might be used elsewhere, but the general searchSanPham
    // is used by SanPhamUI.

    /**
     * Searches for SanPham records by product name (partial match).
     *
     * @param tenSP The product name or part of the name to search for.
     * @return A list of matching SanPham objects, or an empty list on error.
     */
     public List<SanPham> searchSanPhamByName(String tenSP) {
         List<SanPham> danhSachSanPham = new ArrayList<>();
         String sql = "SELECT MaSP, TenSP, Maloai, Gianhap, Giaban, Soluong, Anh FROM SanPham WHERE TenSP LIKE ?";
         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setString(1, "%" + tenSP + "%");

             try (ResultSet rs = pstmt.executeQuery()) {
                 while (rs.next()) {
                     SanPham sanPham = new SanPham();
                     sanPham.setMaSP(rs.getString("MaSP"));
                     sanPham.setTenSP(rs.getString("TenSP"));
                     sanPham.setMaloai(rs.getString("Maloai"));
                     sanPham.setGianhap(rs.getInt("Gianhap"));
                     sanPham.setGiaban(rs.getInt("Giaban"));
                     sanPham.setSoluong(rs.getInt("Soluong"));
                     sanPham.setAnh(rs.getString("Anh"));
                     danhSachSanPham.add(sanPham);
                 }
             }
         } catch (SQLException e) {
              System.err.println("Lỗi khi tìm kiếm sản phẩm theo tên:");
             e.printStackTrace();
             return new ArrayList<>(); // Return empty list on error
         }
         return danhSachSanPham;
     }

          public List<SanPham> searchSanPhamByMaLoai(String Maloai) {
         List<SanPham> danhSachSanPham = new ArrayList<>();
         String sql = "SELECT MaSP, TenSP, Maloai, Gianhap, Giaban, Soluong, Anh FROM SanPham WHERE Maloai LIKE ?";
         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setString(1, "%" + Maloai + "%");

             try (ResultSet rs = pstmt.executeQuery()) {
                 while (rs.next()) {
                     SanPham sanPham = new SanPham();
                     sanPham.setMaSP(rs.getString("MaSP"));
                     sanPham.setTenSP(rs.getString("TenSP"));
                     sanPham.setMaloai(rs.getString("Maloai"));
                     sanPham.setGianhap(rs.getInt("Gianhap"));
                     sanPham.setGiaban(rs.getInt("Giaban"));
                     sanPham.setSoluong(rs.getInt("Soluong"));
                     sanPham.setAnh(rs.getString("Anh"));
                     danhSachSanPham.add(sanPham);
                 }
             }
         } catch (SQLException e) {
              System.err.println("Lỗi khi tìm kiếm sản phẩm theo tên:");
             e.printStackTrace();
             return new ArrayList<>(); // Return empty list on error
         }
         return danhSachSanPham;
     }

    /**
     * Retrieves a list of products belonging to a specific category.
     *
     * @param maLoai The ID of the category.
     * @return A list of SanPham objects in the specified category, or an empty list on error.
     */
     public List<SanPham> getSanPhamByLoai(String maLoai) {
          List<SanPham> sanPhamList = new ArrayList<>();
          // Select from SanPham where Maloai matches
          String sql = "SELECT MaSP, TenSP, Maloai, Gianhap, Giaban, Soluong, Anh FROM SanPham WHERE Maloai = ?";

          try (Connection conn = DatabaseConnection.getConnection();
               PreparedStatement pstmt = conn.prepareStatement(sql)) {

               pstmt.setString(1, maLoai);

               try (ResultSet rs = pstmt.executeQuery()) {
                   while (rs.next()) {
                       SanPham sp = new SanPham();
                       sp.setMaSP(rs.getString("MaSP"));
                       sp.setTenSP(rs.getString("TenSP"));
                       sp.setMaloai(rs.getString("Maloai"));
                       sp.setGianhap(rs.getInt("Gianhap"));
                       sp.setGiaban(rs.getInt("Giaban"));
                       sp.setSoluong(rs.getInt("Soluong"));
                       sp.setAnh(rs.getString("Anh"));
                       sanPhamList.add(sp);
                   }
               }
          } catch (SQLException e) {
               System.err.println("Lỗi khi lấy sản phẩm theo loại:");
               e.printStackTrace();
               return new ArrayList<>(); // Return empty list on error
          }
          return sanPhamList;
     }
}
