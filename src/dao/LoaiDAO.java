package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Loai;

public class LoaiDAO {

    public LoaiDAO() {
        // Constructor (if needed)
    }

    // Phương thức thêm loại (Category) - Trả về boolean
    public boolean addLoai(Loai loai) {
        String sql = "INSERT INTO Loai (MaLoai, TenLoai) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, loai.getMaloai());
            pstmt.setString(2, loai.getTenloai());

            int affectedRows = pstmt.executeUpdate();
            System.out.println("Thêm loại thành công: " + loai.getTenloai());
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm loại:");
            e.printStackTrace();
            return false;
        }
    }

    // Phương thức cập nhật thông tin loại - Trả về boolean
    public boolean updateLoai(Loai loai) {
        String sql = "UPDATE Loai SET TenLoai = ? WHERE MaLoai = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, loai.getTenloai());
            pstmt.setString(2, loai.getMaloai());

            int affectedRows = pstmt.executeUpdate();
            System.out.println("Cập nhật loại thành công: " + loai.getMaloai());
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật loại:");
            e.printStackTrace();
            return false;
        }
    }

    // Phương thức xóa loại - Trả về boolean
    public boolean deleteLoai(String maloai) {
        String sql = "DELETE FROM Loai WHERE MaLoai = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maloai);

            int affectedRows = pstmt.executeUpdate();
            System.out.println("Xóa loại thành công: " + maloai);
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa loại:");
            e.printStackTrace();
             // TODO: Xử lý trường hợp lỗi do ràng buộc khóa ngoại nếu cần
            return false;
        }
    }

    // Phương thức lấy tất cả loại
    public List<Loai> getAllLoai() {
        List<Loai> danhSachLoai = new ArrayList<>();
        String sql = "SELECT MaLoai, TenLoai FROM Loai";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Loai loai = new Loai();
                loai.setMaloai(rs.getString("MaLoai"));
                loai.setTenloai(rs.getString("TenLoai"));
                danhSachLoai.add(loai);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách loại:");
            e.printStackTrace();
            return null;
        }
        return danhSachLoai;
    }

    // Phương thức lấy loại theo Mã Loại
    public Loai getLoaiById(String maloai) {
        Loai loai = null;
        String sql = "SELECT MaLoai, TenLoai FROM Loai WHERE MaLoai = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maloai);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    loai = new Loai();
                    loai.setMaloai(rs.getString("MaLoai"));
                    loai.setTenloai(rs.getString("TenLoai"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy loại sản phẩm theo mã:");
            e.printStackTrace();
        }
        return loai;
    }

    // Optional: Phương thức tìm kiếm theo tên loại (nếu bạn muốn chức năng tìm kiếm)
     public List<Loai> searchLoaiByName(String tenLoai) {
        List<Loai> danhSachLoai = new ArrayList<>();
        String sql = "SELECT * FROM Loai WHERE Tenloai LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + tenLoai + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Loai loai = new Loai();
                    loai.setMaloai(rs.getString("Maloai"));
                    loai.setTenloai(rs.getString("Tenloai"));
                    danhSachLoai.add(loai);
                }
            }
        } catch (SQLException e) {
             System.err.println("Lỗi khi tìm kiếm loại sản phẩm:");
             e.printStackTrace();
             return null;
        }
         return danhSachLoai;
     }
}
