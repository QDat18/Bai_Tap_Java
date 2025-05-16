package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException; // Needed for Statement.RETURN_GENERATED_KEYS if auto-generating IDs
import java.text.SimpleDateFormat; // Use Timestamp for date/time
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List; // Assuming HoaDonBan uses java.util.Date
import model.CTHoaDonBan;
import model.HoaDonBan; // For date parsing in search (optional)

public class HoaDonBanDAO {

    // Initialize DAOs used in transactional methods
    private CTHoaDonBanDAO ctHoaDonBanDAO;
    private SanPhamDAO sanPhamDAO; // Assuming SanPhamDAO is available for stock updates

    public HoaDonBanDAO() {
        ctHoaDonBanDAO = new CTHoaDonBanDAO();
        sanPhamDAO = new SanPhamDAO(); // Initialize SanPhamDAO
    }

    /**
     * Generates the next sequential code for HoaDonBan (e.g., HDB001, HDB002).
     * Uses synchronization for basic thread safety.
     *
     * @return The next unique HoaDonBan code, or "ERROR_CODE" if generation fails.
     */
    public synchronized String generateNextHoaDonBanCode() { // synchronized for basic safety
        String latestMaHDB = null;

        // SQL to get the latest MaHDB following the pattern 'HDB%'
        // Adjust the query based on your specific database system (e.g., LIMIT 1 instead of TOP 1)
        String sql = "SELECT TOP 1 MaHDB FROM HoaDonBan WHERE MaHDB LIKE 'HDB%' ORDER BY MaHDB DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                latestMaHDB = rs.getString("MaHDB");
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy mã HDB cuối cùng:");
            e.printStackTrace();
            return "ERROR_CODE"; // Indicate error
        }

        String prefix = "HDB";
        int lastNumber = 0;

        if (latestMaHDB != null && latestMaHDB.startsWith(prefix)) {
            try {
                // Extract the numeric part after the prefix
                String numberPart = latestMaHDB.substring(prefix.length());
                lastNumber = Integer.parseInt(numberPart);
            } catch (NumberFormatException e) {
                System.err.println("Lỗi khi phân tích số thứ tự từ mã HDB: " + latestMaHDB);
                // If parsing fails, start numbering from 0 or 1
                lastNumber = 0;
            }
        }

        // Increment the number and format the new code
        int nextNumber = lastNumber + 1;
        String newMaHDB = String.format("%s%03d", prefix, nextNumber); // Format with leading zeros (e.g., HDB001)

        return newMaHDB;
    }


    /**
     * Saves a HoaDonBan (header and details) and updates product stock in a database transaction.
     * This method replaces the previous `saveHoaDonBan`.
     *
     * @param hoaDonBan The HoaDonBan header object to save.
     * @param chiTietHoaDon The list of CTHoaDonBan details to save.
     * @return true if the transaction is successful, false otherwise.
     */
    public boolean saveHoaDonBanTransaction(HoaDonBan hoaDonBan, List<CTHoaDonBan> chiTietHoaDon) {
        Connection conn = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // <-- Start Transaction

            // 1. Save HoaDonBan header
            // Assuming addHoaDonBanHeader method handles inserting the header
            boolean headerSaved = addHoaDonBanHeader(conn, hoaDonBan);
            if (!headerSaved) {
                conn.rollback(); // Rollback if header save fails
                System.err.println("Transaction failed: Could not save HoaDonBan header.");
                return false;
            }

            // 2. Save CTHoaDonBan details and update SanPham stock
            if (chiTietHoaDon != null && !chiTietHoaDon.isEmpty()) {
                for (CTHoaDonBan ct : chiTietHoaDon) {
                    // Ensure MaHDB is set in the detail object from the saved header
                    ct.setMaHDB(hoaDonBan.getMaHDB());

                    // Call the DAO method to add detail line using the current transaction connection
                    // Assuming ctHoaDonBanDAO.addChiTietHoaDon accepts Connection and CTHoaDonBan
                    // This method should handle insertion and potentially throw SQLException on error
                    ctHoaDonBanDAO.addChiTietHoaDon(conn, ct); // Assuming this method exists

                    // Update SanPham stock (decrease quantity for sales)
                    // Assuming SanPhamDAO has updateStockQuantity(Connection conn, String maSP, int quantityChange)
                    // and quantityChange is negative for decreasing stock
                    sanPhamDAO.updateStockQuantity(conn, ct.getMaSP(), -ct.getSoluong()); // Decrease stock by quantity sold
                    // If updateStockQuantity throws SQLException, it will be caught below
                }
            }

            conn.commit(); // <-- Commit transaction if all steps succeeded
            success = true;
            System.out.println("Transaction successful: Saved HoaDonBan " + hoaDonBan.getMaHDB() + " with details and updated stock.");

        } catch (SQLException e) {
            // Handle any SQL errors during the transaction
            if (conn != null) {
                try {
                    conn.rollback(); // <-- Rollback transaction on error
                    System.err.println("Transaction rolled back due to SQLException during HoaDonBan save.");
                } catch (SQLException ex) {
                    System.err.println("Error during rollback: " + ex.getMessage());
                }
            }
            System.err.println("SQLException during HoaDonBan save transaction: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        } catch (Exception e) { // Catch other potential exceptions from called methods
             if (conn != null) {
                 try {
                      conn.rollback(); // Rollback on other errors too
                      System.err.println("Transaction rolled back due to unexpected error during HoaDonBan save.");
                 } catch (SQLException ex) {
                      System.err.println("Error during rollback: " + ex.getMessage());
                 }
             }
             System.err.println("Unexpected error during HoaDonBan save transaction: " + e.getMessage());
             e.printStackTrace();
        }
        finally {
            // Restore auto-commit mode and close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close(); // Close connection
                } catch (SQLException e) {
                    System.err.println("Error closing connection after HoaDonBan save transaction: " + e.getMessage());
                }
            }
        }
        return success;
    }

     /**
      * Helper method to add a HoaDonBan header to the database within a transaction.
      * This method is called by `saveHoaDonBanTransaction`.
      *
      * @param conn The database connection for the transaction.
      * @param hdb The HoaDonBan object to insert.
      * @return true if insertion is successful, false otherwise.
      * @throws SQLException if a database error occurs.
      */
     private boolean addHoaDonBanHeader(Connection conn, HoaDonBan hdb) throws SQLException {
         // Example SQL (adjust table/column names as per your schema)
         // Assuming HoaDonBan table has columns: MaHDB, MaNV, MaKH, Ngayban, Tongtien
         String sql = "INSERT INTO HoaDonBan (MaHDB, MaNV, MaKH, Ngayban, Tongtien) VALUES (?, ?, ?, ?, ?)";
         try (PreparedStatement pst = conn.prepareStatement(sql)) {
             pst.setString(1, hdb.getMaHDB());
             pst.setString(2, hdb.getMaNV());
             // Handle potential null MaKH
             if (hdb.getMaKH() != null && !hdb.getMaKH().isEmpty()) {
                  pst.setString(3, hdb.getMaKH());
             } else {
                  pst.setNull(3, java.sql.Types.VARCHAR); // Or the appropriate SQL type for MaKH
             }
             // Use Timestamp for date/time if Ngayban column stores time
             pst.setTimestamp(4, new java.sql.Timestamp(hdb.getNgayban().getTime()));
             pst.setInt(5, hdb.getTongtien());

             int rowsAffected = pst.executeUpdate();
             return rowsAffected > 0; // Return true if at least one row was inserted
         } catch (SQLException e) {
              System.err.println("Error inserting HoaDonBan header: " + e.getMessage());
              throw e; // Re-throw to trigger rollback in the main transaction method
         }
     }


    /**
     * Retrieves all HoaDonBan records from the database, including related NhanVien and KhachHang names.
     * Uses JOINs to fetch names from NhanVien and KhachHang tables.
     *
     * @return A list of HoaDonBan objects with TenNV and TenKH populated, or null if an error occurs.
     */
    public List<HoaDonBan> getAllHoaDonBan() {
        List<HoaDonBan> hoaDonList = new ArrayList<>();
        // SQL with JOINs to get TenNV and Tenkhach
        String sql = "SELECT hdb.MaHDB, hdb.MaNV, hdb.MaKH, hdb.Ngayban, hdb.Tongtien, nv.TenNV, kh.Tenkhach " +
                     "FROM HoaDonBan hdb " +
                     "JOIN NhanVien nv ON hdb.MaNV = nv.MaNV " +
                     "LEFT JOIN KhachHang kh ON hdb.MaKH = kh.MaKH"; // Use LEFT JOIN for KhachHang in case MaKH is NULL


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                HoaDonBan hdb = new HoaDonBan();
                hdb.setMaHDB(rs.getString("MaHDB"));
                hdb.setMaNV(rs.getString("MaNV"));
                hdb.setMaKH(rs.getString("MaKH"));
                hdb.setNgayban(rs.getTimestamp("Ngayban")); // Use getTimestamp if storing date and time
                hdb.setTongtien(rs.getInt("Tongtien")); // Assuming Tongtien is INT

                // Set the joined names
                hdb.setTenNV(rs.getString("TenNV"));
                hdb.setTenKH(rs.getString("Tenkhach")); // Will be null if MaKH was null

                hoaDonList.add(hdb);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách hóa đơn bán:");
            e.printStackTrace();
            return null; // Return null if an error occurs
        }
        return hoaDonList;
    }


    /**
     * Retrieves a single HoaDonBan record by its ID, including related NhanVien and KhachHang names.
     * Uses JOINs to fetch names.
     *
     * @param maHDB The ID of the HoaDonBan to retrieve.
     * @return The HoaDonBan object with TenNV and TenKH populated, or null if not found or an error occurs.
     */
    public HoaDonBan getHoaDonBanById(String maHDB) {
         // SQL with JOINs to get TenNV and Tenkhach for a single invoice
         String sql = "SELECT hdb.MaHDB, hdb.MaNV, hdb.MaKH, hdb.Ngayban, hdb.Tongtien, nv.TenNV, kh.Tenkhach " +
                      "FROM HoaDonBan hdb " +
                      "JOIN NhanVien nv ON hdb.MaNV = nv.MaNV " +
                      "LEFT JOIN KhachHang kh ON hdb.MaKH = kh.MaKH " + // Use LEFT JOIN for KhachHang
                      "WHERE hdb.MaHDB = ?";

         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setString(1, maHDB);

             try (ResultSet rs = pstmt.executeQuery()) {
                 if (rs.next()) {
                     HoaDonBan hdb = new HoaDonBan();
                     hdb.setMaHDB(rs.getString("MaHDB"));
                     hdb.setMaNV(rs.getString("MaNV"));
                     hdb.setMaKH(rs.getString("MaKH"));
                     hdb.setNgayban(rs.getTimestamp("Ngayban")); // Use getTimestamp
                     hdb.setTongtien(rs.getInt("Tongtien"));

                     // Set the joined names
                     hdb.setTenNV(rs.getString("TenNV"));
                     hdb.setTenKH(rs.getString("Tenkhach")); // Will be null if MaKH was null

                     return hdb; // Return the HoaDonBan object if found
                 }
             }
         } catch (SQLException e) {
             System.err.println("Lỗi khi lấy hóa đơn bán theo mã:");
             e.printStackTrace();
         }
         return null; // Return null if not found or error
    }

    /**
     * Deletes a HoaDonBan and its details, and restores product stock in a transaction.
     *
     * @param maHDB The ID of the HoaDonBan to delete.
     * @return true if the transaction is successful, false otherwise.
     */
    public boolean deleteHoaDonBan(String maHDB) {
         Connection conn = null;
         boolean success = false;

         try {
             conn = DatabaseConnection.getConnection();
             conn.setAutoCommit(false); // <-- Start Transaction

             // --- Restore Stock (BEFORE deleting details) ---
             // Get details using the transaction connection
             // Assuming ctHoaDonBanDAO.getChiTietHoaDonByMaHDB accepts Connection and MaHDB
             List<CTHoaDonBan> chiTietToDelete = ctHoaDonBanDAO.getChiTietHoaDonByMaHDB(conn, maHDB);
             if (chiTietToDelete != null) {
                 for (CTHoaDonBan ct : chiTietToDelete) {
                     // Restore stock: add the sold quantity back
                     // Assuming SanPhamDAO has updateStockQuantity(Connection conn, String maSP, int quantityChange)
                     // and quantityChange is positive for increasing stock
                     sanPhamDAO.updateStockQuantity(conn, ct.getMaSP(), ct.getSoluong()); // Add the quantity back
                     // If updateStockQuantity throws SQLException, it will be caught below
                 }
                 System.out.println("Đã phục hồi tồn kho cho HDB: " + maHDB);
             }


             // --- Delete CTHoaDonBan (Details) ---
             // Assuming ctHoaDonBanDAO.deleteChiTietHoaDonByMaHDB handles delete using the current connection
             // This method should delete all detail rows for the given MaHDB and potentially throw SQLException on error
             ctHoaDonBanDAO.deleteChiTietHoaDonByMaHDB(conn, maHDB);


             // --- Delete HoaDonBan (Header) ---
             String deleteHoaDonSql = "DELETE FROM HoaDonBan WHERE MaHDB = ?";
             try (PreparedStatement pstmt = conn.prepareStatement(deleteHoaDonSql)) { // Use the current connection
                 pstmt.setString(1, maHDB);

                 int headerAffected = pstmt.executeUpdate();
                  if (headerAffected > 0) {
                      System.out.println("Đã xóa header hóa đơn cho HDB: " + maHDB);
                      success = true; // Header deleted successfully
                  } else {
                      System.out.println("Không tìm thấy hóa đơn bán để xóa: " + maHDB);
                      // Depending on business logic, you might adjust 'success' here.
                      // If details were found and deleted, but header wasn't, it might still be considered a partial success.
                      // For now, we require header deletion for overall success.
                      success = false; // Ensure success is false if header not deleted
                  }
             }

             conn.commit(); // <-- Commit transaction
             System.out.println("Xóa hóa đơn bán " + maHDB + " thành công.");


         } catch (SQLException e) {
             if (conn != null) {
                 try {
                     conn.rollback(); // <-- Rollback transaction on error
                      System.err.println("Rollback giao dịch do lỗi khi xóa hóa đơn bán.");
                 } catch (SQLException ex) {
                     ex.printStackTrace();
                 }
             }
             System.err.println("Lỗi khi xóa hóa đơn bán:");
             e.printStackTrace();
             success = false; // Ensure success is false on error
         } catch (Exception e) { // Catch other potential exceptions from called methods
              if (conn != null) {
                  try {
                       conn.rollback(); // Rollback on other errors too
                       System.err.println("Transaction rolled back due to unexpected error during HoaDonBan delete.");
                  } catch (SQLException ex) {
                       System.err.println("Error during rollback: " + ex.getMessage());
                  }
              }
              System.err.println("Unexpected error during HoaDonBan delete transaction: " + e.getMessage());
              e.printStackTrace();
              success = false;
         }
         finally {
             if (conn != null) {
                 try {
                     conn.setAutoCommit(true); // Reset auto-commit
                     conn.close(); // Close connection
                 } catch (SQLException ex) {
                     ex.printStackTrace();
                 }
             }
         }
         return success;
    }

    /**
     * Searches for HoaDonBan records based on the specified criteria and search term.
     * Uses JOINs to search by NhanVien and KhachHang names.
     *
     * @param criteria The search criteria ("Mã HDB", "Ngày bán", "Tên NV", "Tên KH").
     * @param searchTerm The term to search for.
     * @return A list of matching HoaDonBan objects with TenNV and TenKH populated, or null if an error occurs.
     */
     public List<HoaDonBan> searchHoaDonBan(String criteria, String searchTerm) {
         List<HoaDonBan> hoaDonList = new ArrayList<>();
         String sql = "SELECT hdb.MaHDB, hdb.MaNV, hdb.MaKH, hdb.Ngayban, hdb.Tongtien, nv.TenNV, kh.Tenkhach " +
                      "FROM HoaDonBan hdb " +
                      "JOIN NhanVien nv ON hdb.MaNV = nv.MaNV " +
                      "LEFT JOIN KhachHang kh ON hdb.MaKH = kh.MaKH " + // Use LEFT JOIN
                      "WHERE "; // Start WHERE clause

         // Build the WHERE clause based on criteria
         switch (criteria) {
             case "Mã HDB":
                 sql += "hdb.MaHDB LIKE ?";
                 searchTerm = "%" + searchTerm + "%"; // Use LIKE for partial matching
                 break;
             case "Ngày bán":
                 // Assuming searchTerm is in "dd/MM/yyyy" format from UI
                 // We need to parse it and compare with the date part of Ngayban
                 sql += "CAST(hdb.Ngayban AS DATE) = ?";
                 // Date parsing will be done when setting the parameter
                 break;
             case "Tên NV":
                 sql += "nv.TenNV LIKE ?";
                 searchTerm = "%" + searchTerm + "%";
                 break;
             case "Tên KH":
                 sql += "kh.Tenkhach LIKE ?";
                 searchTerm = "%" + searchTerm + "%";
                 break;
             default:
                 System.err.println("Tiêu chí tìm kiếm không hợp lệ: " + criteria);
                 return hoaDonList; // Return empty list for invalid criteria
         }

         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             // Set the search term parameter based on criteria
             switch (criteria) {
                 case "Ngày bán":
                     // Parse the date string from the UI (e.g., "dd/MM/yyyy")
                     try {
                         java.util.Date date = new SimpleDateFormat("dd/MM/yyyy").parse(searchTerm);
                         pstmt.setDate(1, new java.sql.Date(date.getTime())); // Set as SQL DATE
                     } catch (ParseException e) {
                         System.err.println("Lỗi định dạng ngày tìm kiếm. Vui lòng nhập theo định dạng dd/MM/yyyy.");
                         // JOptionPane.showMessageDialog(null, "Lỗi định dạng ngày tìm kiếm. Vui lòng nhập theo định dạng dd/MM/yyyy.", "Lỗi", JOptionPane.ERROR_MESSAGE); // Avoid Swing in DAO
                         return hoaDonList; // Return empty list if date format is invalid
                     }
                     break;
                 default: // Mã HDB, Tên NV, Tên KH
                     pstmt.setString(1, searchTerm);
                     break;
             }


             try (ResultSet rs = pstmt.executeQuery()) {
                 while (rs.next()) {
                     HoaDonBan hdb = new HoaDonBan();
                     hdb.setMaHDB(rs.getString("MaHDB"));
                     hdb.setMaNV(rs.getString("MaNV"));
                     hdb.setMaKH(rs.getString("MaKH"));
                     hdb.setNgayban(rs.getTimestamp("Ngayban")); // Use getTimestamp
                     hdb.setTongtien(rs.getInt("Tongtien"));

                     // Set the joined names
                     hdb.setTenNV(rs.getString("TenNV"));
                     hdb.setTenKH(rs.getString("Tenkhach")); // Will be null if MaKH was null

                     hoaDonList.add(hdb);
                 }
             }
         } catch (SQLException e) {
             System.err.println("Lỗi khi tìm kiếm hóa đơn bán:");
             e.printStackTrace();
             return null; // Return null if an error occurs
         }
         return hoaDonList;
     }

     /**
      * Calculates the total sales amount for today.
      *
      * @return The total sales amount for today, or 0 on error or if no sales.
      */
     public double getTotalSalesAmountForToday() {
         double totalSales = 0;
         // SQL query to calculate the sum of TongTien for invoices created today
         // Use CAST(Ngayban AS DATE) to compare only the date part
         String sql = "SELECT SUM(TongTien) FROM HoaDonBan WHERE CAST(Ngayban AS DATE) = ?";

         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             // Set today's date as a parameter
             pstmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));

             try (ResultSet rs = pstmt.executeQuery()) {
                 if (rs.next()) {
                     // SUM() returns 0 if no rows match, or null if all TongTien values are null.
                     // getDouble() handles null by returning 0, so it's safe.
                     totalSales = rs.getDouble(1);
                 }
             }

         } catch (SQLException e) {
             System.err.println("Lỗi khi tính tổng doanh thu hôm nay:");
             e.printStackTrace();
             return 0; // Return 0 on error
         }
         return totalSales; // Return the calculated total
     }

     /**
      * Counts the number of sales invoices created today.
      *
      * @return The count of sales invoices for today, or 0 on error or if no sales.
      */
     public int getSalesCountForToday() {
         int count = 0;
         // SQL query to count the number of invoices created today
         String sql = "SELECT COUNT(*) FROM HoaDonBan WHERE CAST(Ngayban AS DATE) = ?";

         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             // Set today's date as a parameter
             pstmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));

             try (ResultSet rs = pstmt.executeQuery()) {
                 if (rs.next()) {
                     // COUNT(*) returns 0 if no rows match
                     count = rs.getInt(1);
                 }
             }

         } catch (SQLException e) {
             System.err.println("Lỗi khi lấy số lượng hóa đơn bán hôm nay:");
             e.printStackTrace();
             return 0; // Return 0 on error
         }
         return count; // Return the calculated count
     }


    // --- Placeholder / Required DAO methods for Transaction ---
    // These methods need to be implemented in their respective DAO classes
    // and must accept a Connection object to participate in the transaction.

    /*
    // This method should be in CTHoaDonBanDAO.java
    public void addChiTietHoaDon(Connection conn, CTHoaDonBan cthdb) throws SQLException {
        // Example SQL (adjust table/column names)
        String sql = "INSERT INTO CTHoaDonBan (MaHDB, MaSP, Soluong, Dongia, Khuyenmai, Thanhtien) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, cthdb.getMaHDB()); // MaHDB is set in the main transaction method
            pst.setString(2, cthdb.getMaSP());
            pst.setInt(3, cthdb.getSoluong());
            pst.setInt(4, cthdb.getGiaban()); // Assuming Dongia in CTHoaDonBan stores selling price (Giaban)
            pst.setInt(5, cthdb.getKhuyenmai());
            pst.setInt(6, cthdb.getThanhtien());
            pst.executeUpdate(); // Execute the insert

            // No need to check rowsAffected here, just let SQLException handle errors
        } catch (SQLException e) {
            System.err.println("Error inserting CTHoaDonBan detail for MaSP=" + cthdb.getMaSP() + ": " + e.getMessage());
            throw e; // Re-throw to trigger rollback
        }
    }
    */

    /*
    // This method should be in CTHoaDonBanDAO.java
    public List<CTHoaDonBan> getChiTietHoaDonByMaHDB(Connection conn, String maHDB) throws SQLException {
        List<CTHoaDonBan> chiTietList = new ArrayList<>();
        // Example SQL (adjust table/column names and joins if needed for product name)
        String sql = "SELECT MaHDB, MaSP, Soluong, Dongia, Khuyenmai, Thanhtien FROM CTHoaDonBan WHERE MaHDB = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maHDB);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    CTHoaDonBan ct = new CTHoaDonBan();
                    ct.setMaHDB(rs.getString("MaHDB"));
                    ct.setMaSP(rs.getString("MaSP"));
                    ct.setSoluong(rs.getInt("Soluong"));
                    ct.setGiaban(rs.getInt("Dongia")); // Assuming Dongia in DB is selling price
                    ct.setKhuyenmai(rs.getInt("Khuyenmai"));
                    ct.setThanhtien(rs.getInt("Thanhtien"));
                    // You might need to fetch product name here if needed in the list
                    chiTietList.add(ct);
                }
            }
        } catch (SQLException e) {
             System.err.println("Error getting CTHoaDonBan details for MaHDB=" + maHDB + ": " + e.getMessage());
             throw e; // Re-throw to trigger rollback
        }
        return chiTietList;
    }
    */

    /*
    // This method should be in CTHoaDonBanDAO.java
    public void deleteChiTietHoaDonByMaHDB(Connection conn, String maHDB) throws SQLException {
        // Example SQL (adjust table name)
        String sql = "DELETE FROM CTHoaDonBan WHERE MaHDB = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, maHDB);
            pst.executeUpdate(); // Execute the delete
            // No need to check rowsAffected here, just let SQLException handle errors
        } catch (SQLException e) {
            System.err.println("Error deleting CTHoaDonBan details for MaHDB=" + maHDB + ": " + e.getMessage());
            throw e; // Re-throw to trigger rollback
        }
    }
    */

    /*
    // This method should be in SanPhamDAO.java
    // It must accept a Connection object to participate in the transaction.
    // quantityChange will be negative for sales (decrease stock)
    // quantityChange will be positive for returns/deletions (increase stock)
    public void updateStockQuantity(Connection conn, String maSP, int quantityChange) throws SQLException {
        // Example SQL to update stock
        String sql = "UPDATE SanPham SET Soluongton = Soluongton + ? WHERE MaSP = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, quantityChange);
            pst.setString(2, maSP);
            int rowsAffected = pst.executeUpdate();
            // Optional: Check rowsAffected to ensure the product exists
            if (rowsAffected == 0) {
                 System.err.println("Stock update failed: Product with MaSP=" + maSP + " not found.");
                 // Depending on your rules, you might want to throw an exception here
                 // throw new SQLException("Product not found for stock update: " + maSP);
                 // For now, we just log and continue, but a real system might require stricter checks.
            }
        } catch (SQLException e) {
            System.err.println("Error updating stock for MaSP=" + maSP + ": " + e.getMessage());
            throw e; // Re-throw to trigger rollback
        }
    }
    */
}
