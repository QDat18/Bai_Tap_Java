package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.NhanVien;

public class NhanVienDAO {

    // --- Phương thức trợ giúp để lấy mã nhân viên lớn nhất hiện có ---
    public synchronized String generateNextMaNV() {
        String latestMaNV = null;
        String sql = "SELECT TOP 1 MaNV FROM NhanVien WHERE MaNV LIKE 'NV%' ORDER BY MaNV DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                latestMaNV = rs.getString("MaNV");
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy mã NV cuối cùng:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return "NV01";
        }

        if (latestMaNV != null && latestMaNV.startsWith("NV")) {
            try {
                String numberPart = latestMaNV.substring(2);
                while (numberPart.startsWith("0")) {
                    numberPart = numberPart.substring(1);
                }
                int number = numberPart.isEmpty() ? 0 : Integer.parseInt(numberPart);
                int nextNumber = number + 1;
                return String.format("NV%02d", nextNumber);

            } catch (NumberFormatException e) {
                System.err.println("Lỗi định dạng phần số của mã NV cuối cùng: " + latestMaNV);
                e.printStackTrace();
                return "NV01";
            }
        }

        return "NV01";
    }

    // Phương thức thêm nhân viên (bao gồm cả thông tin tài khoản)
    public boolean addNhanVien(NhanVien nhanVien) throws SQLException {
        String newMaNV = generateNextMaNV();
        nhanVien.setMaNV(newMaNV);

        String sql = "INSERT INTO NhanVien (MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, Role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nhanVien.getMaNV());
            pstmt.setString(2, nhanVien.getTenNV());
            pstmt.setString(3, nhanVien.getDiachi());
            pstmt.setString(4, nhanVien.getGioitinh());
            pstmt.setString(5, nhanVien.getSDT());
            pstmt.setString(6, nhanVien.getTendangnhap());
            pstmt.setString(7, nhanVien.getMatkhau());
            pstmt.setString(8, nhanVien.getEmail());
            pstmt.setString(9, nhanVien.getRole());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Phương thức cập nhật thông tin nhân viên
    public boolean updateNhanVien(NhanVien nhanVien) {
        String sql = "UPDATE NhanVien SET TenNV = ?, Diachi = ?, Gioitinh = ?, SDT = ?, " +
                     "Tendangnhap = ?, Matkhau = ?, Email = ?, Role = ? " +
                     "WHERE MaNV = ?"; 

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int index = 1;
            pstmt.setString(index++, nhanVien.getTenNV());
            pstmt.setString(index++, nhanVien.getDiachi());
            pstmt.setString(index++, nhanVien.getGioitinh());
            pstmt.setString(index++, nhanVien.getSDT());
            pstmt.setString(index++, nhanVien.getTendangnhap());
            pstmt.setString(index++, nhanVien.getMatkhau());
            pstmt.setString(index++, nhanVien.getEmail());
            pstmt.setString(index++, nhanVien.getRole());
            pstmt.setString(index++, nhanVien.getMaNV());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Cập nhật nhân viên thành công: " + nhanVien.getMaNV());
                return true;
            } else {
                System.out.println("Không tìm thấy nhân viên để cập nhật: " + nhanVien.getMaNV());
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Lỗi CSDL khi cập nhật nhân viên:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            if (e.getErrorCode() == 2627) { 
                System.err.println("Lỗi: Tên đăng nhập hoặc Email đã tồn tại.");
            }
            return false;
        }
    }

    public boolean deleteNhanVien(String maNV) {
        String sql = "DELETE FROM NhanVien WHERE MaNV = ?";
        boolean success = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maNV);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Xóa nhân viên thành công: " + maNV);
                success = true;
            } else {
                System.out.println("Không tìm thấy nhân viên để xóa: " + maNV);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi CSDL khi xóa nhân viên:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            if (e.getErrorCode() == 547) {
                System.err.println("Không thể xóa nhân viên này vì có ràng buộc khóa ngoại với các bảng khác.");
            }
        }
        return success;
    }

    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> danhSachNhanVien = new ArrayList<>();
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, Role FROM NhanVien";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("MaNV"));
                nv.setTenNV(rs.getString("TenNV"));
                nv.setDiachi(rs.getString("Diachi"));
                nv.setGioitinh(rs.getString("Gioitinh"));
                nv.setSDT(rs.getString("SDT"));
                nv.setTendangnhap(rs.getString("Tendangnhap"));
                nv.setMatkhau(rs.getString("Matkhau"));
                nv.setEmail(rs.getString("Email"));
                nv.setRole(rs.getString("Role"));
                danhSachNhanVien.add(nv);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách nhân viên:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return null;
        }
        return danhSachNhanVien;
    }

    public NhanVien getNhanVienById(String maNV) {
        NhanVien nv = null;
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, Role FROM NhanVien WHERE MaNV = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maNV);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nv = new NhanVien();
                    nv.setMaNV(rs.getString("MaNV"));
                    nv.setTenNV(rs.getString("TenNV"));
                    nv.setDiachi(rs.getString("Diachi"));
                    nv.setGioitinh(rs.getString("Gioitinh"));
                    nv.setSDT(rs.getString("SDT"));
                    nv.setTendangnhap(rs.getString("Tendangnhap"));
                    nv.setMatkhau(rs.getString("Matkhau"));
                    nv.setEmail(rs.getString("Email"));
                    nv.setRole(rs.getString("Role"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy nhân viên theo mã:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
        return nv;
    }

    public NhanVien getNhanVienBySDT(String sdt) {
        NhanVien nv = null;
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Role FROM NhanVien WHERE SDT = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sdt);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nv = new NhanVien();
                    nv.setMaNV(rs.getString("MaNV"));
                    nv.setTenNV(rs.getString("TenNV"));
                    nv.setDiachi(rs.getString("Diachi"));
                    nv.setGioitinh(rs.getString("Gioitinh"));
                    nv.setSDT(rs.getString("SDT"));
                    nv.setRole(rs.getString("Role"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy nhân viên theo SĐT:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
        return nv;
    }

    public List<NhanVien> searchNhanVienByMaNV(String searchTerm) {
        List<NhanVien> danhSachNhanVien = new ArrayList<>();
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, Role FROM NhanVien WHERE MaNV = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchTerm);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(rs.getString("MaNV"));
                    nv.setTenNV(rs.getString("TenNV"));
                    nv.setDiachi(rs.getString("Diachi"));
                    nv.setGioitinh(rs.getString("Gioitinh"));
                    nv.setSDT(rs.getString("SDT"));
                    nv.setTendangnhap(rs.getString("Tendangnhap"));
                    nv.setMatkhau(rs.getString("Matkhau"));
                    nv.setEmail(rs.getString("Email"));
                    nv.setRole(rs.getString("Role"));
                    danhSachNhanVien.add(nv);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm nhân viên theo Mã NV:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return null;
        }
        return danhSachNhanVien;
    }

    public List<NhanVien> searchNhanVienByTenNV(String searchTerm) {
        List<NhanVien> danhSachNhanVien = new ArrayList<>();
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, Role FROM NhanVien WHERE TenNV LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(rs.getString("MaNV"));
                    nv.setTenNV(rs.getString("TenNV"));
                    nv.setDiachi(rs.getString("Diachi"));
                    nv.setGioitinh(rs.getString("Gioitinh"));
                    nv.setSDT(rs.getString("SDT"));
                    nv.setTendangnhap(rs.getString("Tendangnhap"));
                    nv.setMatkhau(rs.getString("Matkhau"));
                    nv.setEmail(rs.getString("Email"));
                    nv.setRole(rs.getString("Role"));
                    danhSachNhanVien.add(nv);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm nhân viên theo Tên NV:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return null;
        }
        return danhSachNhanVien;
    }

    public List<NhanVien> searchNhanVienByDiaChi(String searchTerm) {
        List<NhanVien> danhSachNhanVien = new ArrayList<>();
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, Role FROM NhanVien WHERE Diachi LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + searchTerm + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(rs.getString("MaNV"));
                    nv.setTenNV(rs.getString("TenNV"));
                    nv.setDiachi(rs.getString("Diachi"));
                    nv.setGioitinh(rs.getString("Gioitinh"));
                    nv.setSDT(rs.getString("SDT"));
                    nv.setTendangnhap(rs.getString("Tendangnhap"));
                    nv.setMatkhau(rs.getString("Matkhau"));
                    nv.setEmail(rs.getString("Email"));
                    nv.setRole(rs.getString("Role"));
                    danhSachNhanVien.add(nv);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm nhân viên theo Địa chỉ:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return null;
        }
        return danhSachNhanVien;
    }

    public NhanVien authenticate(String tendangnhap, String matkhau) {
        NhanVien authenticatedNhanVien = null;
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, Role FROM NhanVien WHERE Tendangnhap = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tendangnhap);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHashedPassword = rs.getString("Matkhau");
                    if (matkhau.equals(storedHashedPassword)) {
                        authenticatedNhanVien = new NhanVien();
                        authenticatedNhanVien.setMaNV(rs.getString("MaNV"));
                        authenticatedNhanVien.setTenNV(rs.getString("TenNV"));
                        authenticatedNhanVien.setDiachi(rs.getString("Diachi"));
                        authenticatedNhanVien.setGioitinh(rs.getString("Gioitinh"));
                        authenticatedNhanVien.setSDT(rs.getString("SDT"));
                        authenticatedNhanVien.setTendangnhap(rs.getString("Tendangnhap"));
                        authenticatedNhanVien.setMatkhau(storedHashedPassword);
                        authenticatedNhanVien.setEmail(rs.getString("Email"));
                        authenticatedNhanVien.setRole(rs.getString("Role"));
                    } else {
                        System.out.println("Xác thực thất bại: Sai mật khẩu cho tên đăng nhập " + tendangnhap);
                        authenticatedNhanVien = null;
                    }
                } else {
                    System.out.println("Xác thực thất bại: Không tìm thấy tên đăng nhập " + tendangnhap);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi CSDL khi xác thực đăng nhập:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
        return authenticatedNhanVien;
    }

    public NhanVien getNhanVienByTendangnhap(String tendangnhap) {
        NhanVien nv = null;
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, Role FROM NhanVien WHERE Tendangnhap = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tendangnhap);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nv = new NhanVien();
                    nv.setMaNV(rs.getString("MaNV"));
                    nv.setTenNV(rs.getString("TenNV"));
                    nv.setDiachi(rs.getString("Diachi"));
                    nv.setGioitinh(rs.getString("Gioitinh"));
                    nv.setSDT(rs.getString("SDT"));
                    nv.setTendangnhap(rs.getString("Tendangnhap"));
                    nv.setMatkhau(rs.getString("Matkhau"));
                    nv.setEmail(rs.getString("Email"));
                    nv.setRole(rs.getString("Role"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy nhân viên theo tên đăng nhập:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
        return nv;
    }

    public NhanVien getNhanVienByEmail(String email) {
        NhanVien nv = null;
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, Role FROM NhanVien WHERE Email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nv = new NhanVien();
                    nv.setMaNV(rs.getString("MaNV"));
                    nv.setTenNV(rs.getString("TenNV"));
                    nv.setDiachi(rs.getString("Diachi"));
                    nv.setGioitinh(rs.getString("Gioitinh"));
                    nv.setSDT(rs.getString("SDT"));
                    nv.setTendangnhap(rs.getString("Tendangnhap"));
                    nv.setMatkhau(rs.getString("Matkhau"));
                    nv.setEmail(rs.getString("Email"));
                    nv.setRole(rs.getString("Role"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy nhân viên theo email:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
        return nv;
    }

    public boolean updatePassword(String tendangnhap, String newMatkhau) {
        String sql = "UPDATE NhanVien SET Matkhau = ? WHERE Tendangnhap = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newMatkhau);
            pstmt.setString(2, tendangnhap);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi CSDL khi cập nhật mật khẩu:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUsernameExists(String tendangnhap) {
        String sql = "SELECT 1 FROM NhanVien WHERE Tendangnhap = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tendangnhap);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi CSDL khi kiểm tra tên đăng nhập tồn tại:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isEmailExists(String email) {
        String sql = "SELECT 1 FROM NhanVien WHERE Email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi CSDL khi kiểm tra email tồn tại:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    public List<NhanVien> searchNhanVienByRole(String searchTerm) {
        List<NhanVien> danhSachNhanVien = new ArrayList<>();
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, Role FROM NhanVien WHERE Role = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchTerm);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(rs.getString("MaNV"));
                    nv.setTenNV(rs.getString("TenNV"));
                    nv.setDiachi(rs.getString("Diachi"));
                    nv.setGioitinh(rs.getString("Gioitinh"));
                    nv.setSDT(rs.getString("SDT"));
                    nv.setTendangnhap(rs.getString("Tendangnhap"));
                    nv.setMatkhau(rs.getString("Matkhau"));
                    nv.setEmail(rs.getString("Email"));
                    nv.setRole(rs.getString("Role"));
                    danhSachNhanVien.add(nv);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm nhân viên theo Role:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return null;
        }
        return danhSachNhanVien;
    }
}