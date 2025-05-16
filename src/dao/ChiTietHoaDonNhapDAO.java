package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.ChiTietHoaDonNhap;


public class ChiTietHoaDonNhapDAO {
    private SanPhamDAO sanPhamDAO; // Khởi tạo trong constructor nếu cần

    public ChiTietHoaDonNhapDAO() {
        // sanPhamDAO = new SanPhamDAO(); // Khởi tạo nếu cần
    }

    public void addChiTietHoaDonNhap(Connection conn, ChiTietHoaDonNhap ct) throws SQLException {
         String sql = "INSERT INTO CTHoaDonNhap (MaHDN, MaSP, Soluong, Dongia, Khuyenmai, Thanhtien) VALUES (?, ?, ?, ?, ?, ?)"; // <-- Đảm bảo tên bảng/cột chính xác

         try (PreparedStatement pstmt = conn.prepareStatement(sql)) { // Sử dụng Connection được truyền vào

             pstmt.setString(1, ct.getMaHDN());
             pstmt.setString(2, ct.getMaSP());
             pstmt.setInt(3, ct.getSoluong());
             pstmt.setInt(4, ct.getDongia()); // Dongia cho hóa đơn nhập
             pstmt.setInt(5, ct.getKhuyenmai());
             pstmt.setInt(6, ct.getThanhtien());

             pstmt.executeUpdate();
             // KHÔNG in thành công ở đây. Nó được quản lý bởi HoaDonNhapDAO.
            //  System.out.println("Đã thêm chi tiết HĐN cho SP: " + ct.getMaSP());

         } // PreparedStatement tự đóng khi kết thúc try-with-resources
         // Nếu có lỗi, SQLException sẽ được ném ra và bắt ở HoaDonNhapDAO để rollback transaction
    }


    // Phương thức lấy danh sách chi tiết hóa đơn nhập dựa trên mã hóa đơn nhập (mô phỏng getChiTietHoaDonByMaHDB)
    // Join với SanPham để lấy tên sản phẩm hiển thị trên UI
    // Phương thức này tự lấy Connection vì dùng cho View Details Dialog (không tham gia transaction khác)
    public List<ChiTietHoaDonNhap> getChiTietHoaDonNhapByMaHDN(String maHDN) {
        List<ChiTietHoaDonNhap> chiTietList = new ArrayList<>();
         // SQL join để lấy tên SP (từ bảng SanPham)
         String sql = "SELECT ct.MaHDN, ct.MaSP, sp.TenSP, ct.Soluong, ct.Dongia, ct.Khuyenmai, ct.Thanhtien " + // <-- Đảm bảo tên bảng/cột chính xác
                      "FROM CTHoaDonNhap ct " +
                      "JOIN SanPham sp ON ct.MaSP = sp.MaSP " +
                      "WHERE ct.MaHDN = ?";

        // Lấy kết nối mới
        try (Connection conn = DatabaseConnection.getConnection(); // <-- Sử dụng DatabaseConnection
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHDN);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Cần cập nhật model ChiTietHoaDonNhap để có TenSP
                    ChiTietHoaDonNhap ct = new ChiTietHoaDonNhap(
                        rs.getString("MaHDN"),
                        rs.getString("MaSP"),
                        rs.getString("TenSP"), // Lấy TenSP từ join
                        rs.getInt("Soluong"),
                        rs.getInt("Dongia"),
                        rs.getInt("Khuyenmai"),
                        rs.getInt("Thanhtien")
                    );
                    chiTietList.add(ct);
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy chi tiết hóa đơn nhập (có join):");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return null;
        }
        return chiTietList;
    }


    // Phương thức xóa tất cả chi tiết hóa đơn nhập dựa trên mã hóa đơn nhập (cho transaction xóa đầy đủ)
    // Nhận Connection làm tham số để tham gia vào giao dịch xóa HoaDonNhap.
    public void deleteChiTietHoaDonNhapByMaHDN(Connection conn, String maHDN) throws SQLException {
         String sql = "DELETE FROM CTHoaDonNhap WHERE MaHDN = ?"; // <-- Đảm bảo tên bảng CTHoaDonNhap chính xác
         try (PreparedStatement pstmt = conn.prepareStatement(sql)) { // Sử dụng Connection được truyền vào
             pstmt.setString(1, maHDN);
             int affectedRows = pstmt.executeUpdate(); // Lấy số dòng bị ảnh hưởng
             System.out.println("Đã xóa " + affectedRows + " chi tiết hóa đơn nhập cho HĐN: " + maHDN);
         } // PreparedStatement sẽ tự đóng, Connection không đóng ở đây
         // Lưu ý: Nếu có lỗi, SQLException sẽ được ném ra và bắt ở HoaDonNhapDAO để rollback transaction
    }

    // TODO: Thêm các phương thức khác nếu cần (ví dụ: get chi tiết theo MaHDN và MaSP, update chi tiết...)
}