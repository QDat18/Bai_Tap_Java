package dao;

import java.sql.Connection;
import java.sql.PreparedStatement; // Import model SanPham (needed for joining)
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.CTHoaDonBan;


public class CTHoaDonBanDAO {
     public List<CTHoaDonBan> getChiTietHoaDonByMaHDB(Connection conn, String maHDB) throws SQLException {
        List<CTHoaDonBan> chiTietList = new ArrayList<>();
        String sql = "SELECT ct.MaHDB, ct.MaSP, ct.Soluong, ct.Thanhtien, ct.Khuyenmai, sp.TenSP, sp.Giaban " +
                     "FROM CTHoaDonBan ct " +
                     "JOIN SanPham sp ON ct.MaSP = sp.MaSP " +
                     "WHERE ct.MaHDB = ?";

        // Use the provided connection
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maHDB);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CTHoaDonBan ct = new CTHoaDonBan();
                    ct.setMaHDB(rs.getString("MaHDB"));
                    ct.setMaSP(rs.getString("MaSP"));
                    ct.setSoluong(rs.getInt("Soluong"));
                    ct.setThanhtien(rs.getInt("Thanhtien")); // Assuming this is total for the line
                    ct.setKhuyenmai(rs.getInt("Khuyenmai")); // Assuming integer discount

                    // Set the joined product info (requires getters/setters in CTHoaDonBan model)
                    ct.setTenSP(rs.getString("TenSP"));
                    ct.setGiaban(rs.getInt("Giaban")); // This is Giaban from SanPham at sale time


                    chiTietList.add(ct);
                }
            }
        }
         // Không đóng Connection ở đây vì nó được quản lý bởi phương thức gọi (HoaDonBanDAO)
         return chiTietList;
     }

     // Overload method that gets a connection if not provided (for standalone calls)
     public List<CTHoaDonBan> getChiTietHoaDonByMaHDB(String maHDB) {
         Connection conn = null;
         List<CTHoaDonBan> chiTietList = new ArrayList<>();
         try {
              conn = DatabaseConnection.getConnection();
             chiTietList = getChiTietHoaDonByMaHDB(conn, maHDB); // Call the main method
         } catch (SQLException e) {
             System.err.println("Lỗi khi lấy chi tiết hóa đơn bán (standalone):");
             e.printStackTrace();
         } finally {
             if (conn != null) {
                 try {
                     conn.close();
                 } catch (SQLException e) {
                     e.printStackTrace();
                 }
             }
         }
         return chiTietList;
     }


    public void addChiTietHoaDon(Connection conn, CTHoaDonBan ct) throws SQLException {

        String sql = "INSERT INTO CTHoaDonBan (MaHDB, MaSP, Soluong, Thanhtien, Khuyenmai) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) { 
            pstmt.setString(1, ct.getMaHDB());
            pstmt.setString(2, ct.getMaSP());
            pstmt.setInt(3, ct.getSoluong());
            pstmt.setInt(4, ct.getThanhtien()); // Assuming this is the calculated total for the line
            pstmt.setInt(5, ct.getKhuyenmai());

            pstmt.executeUpdate();
             // Không in thành công ở đây, việc này nên được xử lý ở HoaDonBanDAO để giữ transaction logic sạch sẽ
            // System.out.println("Đã thêm chi tiết hóa đơn cho SP: " + ct.getMaSP());

        } // Không đóng Connection
         // Lưu ý: Nếu có lỗi, SQLException sẽ được ném ra và bắt ở HoaDonBanDAO để rollback transaction
    }


    // Phương thức xóa tất cả chi tiết hóa đơn dựa trên mã hóa đơn bán
    // Nhận Connection làm tham số để tham gia vào giao dịch
    public void deleteChiTietHoaDonByMaHDB(Connection conn, String maHDB) throws SQLException {
         String sql = "DELETE FROM CTHoaDonBan WHERE MaHDB = ?";
         try (PreparedStatement pstmt = conn.prepareStatement(sql)) { // Sử dụng Connection được truyền vào
             pstmt.setString(1, maHDB);
             pstmt.executeUpdate();
              System.out.println("Đã xóa chi tiết hóa đơn cho HDB: " + maHDB);
         } // Không đóng Connection
         // Lưu ý: Nếu có lỗi, SQLException sẽ được ném ra và bắt ở HoaDonBanDAO để rollback transaction
    }

    // TODO: Add method to get a single CTHoaDonBan by MaHDB and MaSP if needed for editing (less common)
    // public CTHoaDonBan getChiTietHoaDonById(Connection conn, String maHDB, String maSP) throws SQLException { ... }
}
