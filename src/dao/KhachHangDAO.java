package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.KhachHang;

public class KhachHangDAO {

    // Phương thức thêm khách hàng
    public void addKhachHang(KhachHang khachHang) {
        String sql = "INSERT INTO KhachHang (MaKH, Tenkhach, SDT) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, khachHang.getMaKH());
            pstmt.setString(2, khachHang.getTenkhach());
            pstmt.setString(3, khachHang.getSDT());

            pstmt.executeUpdate();
            System.out.println("Thêm khách hàng thành công: " + khachHang.getTenkhach());
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm khách hàng:");
            e.printStackTrace();
        }
    }

    // Phương thức cập nhật thông tin khách hàng
    public void updateKhachHang(KhachHang khachHang) {
        String sql = "UPDATE KhachHang SET Tenkhach = ?, SDT = ? WHERE MaKH = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, khachHang.getTenkhach());
            pstmt.setString(2, khachHang.getSDT());
            pstmt.setString(3, khachHang.getMaKH());

            int affectedRows = pstmt.executeUpdate();
             if (affectedRows > 0) {
                System.out.println("Cập nhật khách hàng thành công: " + khachHang.getMaKH());
            } else {
                System.out.println("Không tìm thấy khách hàng để cập nhật: " + khachHang.getMaKH());
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật khách hàng:");
            e.printStackTrace();
        }
    }

    // Phương thức xóa khách hàng
    public void deleteKhachHang(String maKH) {
        String sql = "DELETE FROM KhachHang WHERE MaKH = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maKH);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Xóa khách hàng thành công: " + maKH);
            } else {
                System.out.println("Không tìm thấy khách hàng để xóa: " + maKH);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa khách hàng:");  
            e.printStackTrace();
        }
    }

    // Phương thức lấy tất cả khách hàng
    public List<KhachHang> getAllKhachHang() {
        List<KhachHang> danhSachKhachHang = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) { // executeQuery cho truy vấn SELECT

            while (rs.next()) {
                KhachHang khachHang = new KhachHang();
                khachHang.setMaKH(rs.getString("MaKH"));
                khachHang.setTenkhach(rs.getString("Tenkhach"));
                khachHang.setSDT(rs.getString("SDT"));
                danhSachKhachHang.add(khachHang);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách khách hàng:"); // Optional: Thêm thông báo lỗi
            e.printStackTrace();
        }
        return danhSachKhachHang;
    }

    // Phương thức lấy khách hàng theo mã
    public KhachHang getKhachHangById(String maKH) {
        KhachHang khachHang = null;
        String sql = "SELECT * FROM KhachHang WHERE MaKH = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maKH);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Chỉ cần kiểm tra dòng đầu tiên
                    khachHang = new KhachHang();
                    khachHang.setMaKH(rs.getString("MaKH"));
                    khachHang.setTenkhach(rs.getString("Tenkhach"));
                    khachHang.setSDT(rs.getString("SDT"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy khách hàng theo mã:"); // Optional: Thêm thông báo lỗi
            e.printStackTrace();
        }
        return khachHang; // Trả về đối tượng KhachHang hoặc null nếu không tìm thấy
    }

    // Phương thức tìm kiếm khách hàng theo tên
    public List<KhachHang> searchKhachHangByName(String tenKhach) {
        List<KhachHang> danhSachKhachHang = new ArrayList<>();
        String sql = "SELECT * FROM KhachHang WHERE Tenkhach LIKE ?"; // Sử dụng LIKE cho tìm kiếm gần đúng
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + tenKhach + "%"); // Thêm % để tìm kiếm chứa chuỗi

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    KhachHang kh = new KhachHang();
                    kh.setMaKH(rs.getString("MaKH"));
                    kh.setTenkhach(rs.getString("Tenkhach"));
                    kh.setSDT(rs.getString("SDT"));
                    danhSachKhachHang.add(kh);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm khách hàng theo tên:");
            e.printStackTrace();
        }
        return danhSachKhachHang;
    }

    // Phương thức đề xuất mã khách hàng tiếp theo
    public String suggestNextMaKH() {
        String nextMaKH = "KH01";
        String sql = "SELECT MAX(MaKH) AS maxMaKH FROM KhachHang";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                String maxMaKH = rs.getString("maxMaKH");
                if (maxMaKH != null) {
                    // Lấy phần số từ MaKH (loại bỏ "KH" và chuyển thành số)
                    int number = Integer.parseInt(maxMaKH.replace("KH", ""));
                    // Tăng số lên 1 và định dạng lại với 3 chữ số
                    number++;
                    nextMaKH = String.format("KH%02d", number);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi đề xuất mã khách hàng tiếp theo: " + e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Lỗi định dạng số khi đề xuất mã khách hàng: " + e.getMessage());
            e.printStackTrace();
        }
        return nextMaKH;
    }

    // Phương thức đếm số khách hàng mới hôm nay
    public int getNewCustomersCountForToday() {
        int count = 0;
        String query = "SELECT COUNT(T1.MaKH) " +
                       "FROM ( " +
                       "    SELECT MaKH, MIN(NgayBan) AS FirstPurchaseDate " +
                       "    FROM HoaDonBan " +
                       "    WHERE MaKH IS NOT NULL " +
                       "    GROUP BY MaKH " +
                       ") AS T1 " +
                       "WHERE CAST(T1.FirstPurchaseDate AS DATE) = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(query)) {

            pst.setDate(1, Date.valueOf(LocalDate.now()));

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi tải dữ liệu Khách hàng mới hôm nay (dựa trên Hóa đơn): " + e.getMessage());
            e.printStackTrace();
            return -1;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while getting new customers count (based on HoaDon): " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
        return count;
    }

    public String getLastMaKH() {
        String sql = "SELECT TOP 1 MaKH FROM KhachHang ORDER BY MaKH DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getString("MaKH");
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy mã khách hàng cuối: " + e.getMessage());
            return null;
        }
    }

    public boolean saveKhachHang(KhachHang kh) {
        String sql = "INSERT INTO KhachHang (MaKH, Tenkhach, SDT) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kh.getMaKH());
            pstmt.setString(2, kh.getTenkhach());
            pstmt.setString(3, kh.getSDT());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi lưu khách hàng: " + e.getMessage());
            return false;
        }
    }
}