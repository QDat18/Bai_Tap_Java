package dao;

import java.sql.Connection; // Assuming you have this class
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.NhanVien;

// Import cho password hashing (RẤT QUAN TRỌNG cho bảo mật)
// Bạn cần thêm một thư viện hashing như BCrypt hoặc Argon2 và lớp tiện ích tương ứng
// import utils.PasswordHasher; // Ví dụ: import lớp PasswordHasher của bạn

public class NhanVienDAO {

    // --- Phương thức trợ giúp để lấy mã nhân viên lớn nhất hiện có ---
    // Trả về mã NV lớn nhất (vd: "NV015") hoặc null nếu chưa có NV nào
    // Được sử dụng để tạo mã NV tiếp theo
    public synchronized String generateNextMaNV() { // synchronized để đảm bảo tính duy nhất cơ bản
        String latestMaNV = null;
        // Lấy mã NV lớn nhất hiện có theo format "NV" + số, sắp xếp giảm dần
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
            return null; // Trả về null nếu có lỗi CSDL nghiêm trọng
        }

        // Logic tạo mã tiếp theo
        if (latestMaNV != null && latestMaNV.startsWith("NV")) {
            try {
                // Tìm phần số sau "NV", có thể có các số 0 ở đầu
                String numberPart = latestMaNV.substring(2);
                // Loại bỏ các số 0 ở đầu trước khi parse
                while (numberPart.startsWith("0")) {
                    numberPart = numberPart.substring(1);
                }
                // Nếu sau khi loại bỏ hết số 0 mà chuỗi rỗng, nghĩa là mã chỉ có "NV", coi như số 0
                 int number = numberPart.isEmpty() ? 0 : Integer.parseInt(numberPart);

                // Tăng số và định dạng lại (ví dụ: NV001, NV010, NV100)
                // Sử dụng String.format với padding để đảm bảo số chữ số
                // Giả sử muốn có ít nhất 3 chữ số (NV001, NV010, NV100, NV999, NV1000, ...)
                 int nextNumber = number + 1;
                 // Định dạng với padding 3 chữ số nếu cần, hoặc tăng số chữ số padding nếu số lượng NV lớn
                 String nextMaNV = String.format("NV%03d", nextNumber); // Format 3 chữ số

                return nextMaNV;

            } catch (NumberFormatException e) {
                System.err.println("Lỗi định dạng phần số của mã NV cuối cùng: " + latestMaNV);
                e.printStackTrace();
                // Fallback hoặc xử lý lỗi nếu định dạng không đúng - có thể trả về mã mặc định hoặc null
                return "NV001"; // Trả về mã mặc định nếu lỗi định dạng
            }
        }

        // Mã mặc định cho nhân viên đầu tiên hoặc khi không tìm thấy mã nào
        return "NV001";
    }

    // Phương thức thêm nhân viên (bao gồm cả thông tin tài khoản)
    public boolean addNhanVien(NhanVien nhanVien) {
        // Nếu MaNV chưa được set (ví dụ: khi tạo nhân viên mới từ UI)
        if (nhanVien.getMaNV() == null || nhanVien.getMaNV().isEmpty()) {
             String nextMaNV = generateNextMaNV();
             if (nextMaNV == null) {
                 System.err.println("Không thể tạo mã NV mới. Thêm nhân viên thất bại.");
                 return false;
             }
             nhanVien.setMaNV(nextMaNV); // Gán mã NV mới vào đối tượng
        }

        // SQL INSERT - bao gồm tất cả các cột mới và cũ
        String sql = "INSERT INTO NhanVien (MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"; // 10 cột

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int index = 1;
            pstmt.setString(index++, nhanVien.getMaNV());
            pstmt.setString(index++, nhanVien.getTenNV());
            pstmt.setString(index++, nhanVien.getDiachi());
            pstmt.setString(index++, nhanVien.getGioitinh());
            pstmt.setString(index++, nhanVien.getSDT());

            // Set values for new account fields
            pstmt.setString(index++, nhanVien.getTendangnhap());
            // TODO: Hash the password before storing!
            // pstmt.setString(index++, PasswordHasher.hashPassword(nhanVien.getMatkhau()));
            pstmt.setString(index++, nhanVien.getMatkhau()); // TEMPORARY: Storing plain text or already hashed
            pstmt.setString(index++, nhanVien.getEmail());
            pstmt.setString(index++, nhanVien.getRole());


            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                 System.out.println("Thêm nhân viên thành công: " + nhanVien.getTenNV() + " (Mã: " + nhanVien.getMaNV() + ")");
                 return true;
            } else {
                 System.err.println("Thêm nhân viên thất bại: " + nhanVien.getTenNV());
                 return false;
            }

        } catch (SQLException e) {
            System.err.println("Lỗi CSDL khi thêm nhân viên:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            // TODO: Handle specific errors like unique constraint violation on Tendangnhap
             if (e.getErrorCode() == 2627) { // SQL Server error code for unique constraint violation
                 System.err.println("Lỗi: Tên đăng nhập hoặc Email đã tồn tại.");
                 // Bạn có thể xử lý lỗi này ở UI để thông báo cho người dùng
             }
            return false;
        }
    }

    // Phương thức cập nhật thông tin nhân viên (bao gồm cả thông tin tài khoản)
    public boolean updateNhanVien(NhanVien nhanVien) {
        // SQL UPDATE - bao gồm tất cả các cột mới và cũ
        // Cẩn thận khi update Matkhau - có thể cần logic riêng hoặc phương thức riêng
        String sql = "UPDATE NhanVien SET TenNV = ?, Diachi = ?, Gioitinh = ?, SDT = ?, " +
                     "Tendangnhap = ?, Matkhau = ?, Email = ?, role = ? " +
                     "WHERE MaNV = ?"; 

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int index = 1;
            pstmt.setString(index++, nhanVien.getTenNV());
            pstmt.setString(index++, nhanVien.getDiachi());
            pstmt.setString(index++, nhanVien.getGioitinh());
            pstmt.setString(index++, nhanVien.getSDT());

            // Set values for new account fields
            pstmt.setString(index++, nhanVien.getTendangnhap());

            // TODO: Handle password update:
            // 1. Check if the password field in the UI was actually changed.
            // 2. If changed, hash the new password before setting it here.
            // 3. If not changed, DO NOT update the password column or retrieve the current hashed password first.
            // For simplicity here, we just set whatever is in the NhanVien object, but this is NOT safe for plain text passwords.
            // pstmt.setString(index++, PasswordHasher.hashPassword(nhanVien.getMatkhau())); // Hash if updating password
             pstmt.setString(index++, nhanVien.getMatkhau()); // TEMPORARY: Storing plain text or already hashed

            pstmt.setString(index++, nhanVien.getEmail());
            pstmt.setString(index++, nhanVien.getRole());

            // Set the WHERE clause parameter
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
             // TODO: Handle specific errors like unique constraint violation on Tendangnhap
             if (e.getErrorCode() == 2627) { // SQL Server error code for unique constraint violation
                 System.err.println("Lỗi: Tên đăng nhập hoặc Email đã tồn tại.");
                 // Bạn có thể xử lý lỗi này ở UI để thông báo cho người dùng
             }
            return false;
        }
    }

    // Phương thức xóa nhân viên theo MaNV (khóa chính)
    // Đã đổi trả về boolean để UI có thể kiểm tra
    public boolean deleteNhanVien(String maNV) {
        // SQL DELETE - đảm bảo tên bảng chính xác
        String sql = "DELETE FROM NhanVien WHERE MaNV = ?";
        boolean success = false; // Mặc định là thất bại
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maNV);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Xóa nhân viên thành công: " + maNV);
                success = true; // Thành công
            } else {
                System.out.println("Không tìm thấy nhân viên để xóa: " + maNV);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi CSDL khi xóa nhân viên:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            // TODO: Xử lý lỗi ràng buộc khóa ngoại nếu có (ví dụ: nhân viên này đang làm MaNV trong HoaDonBan, HoaDonNhap)
            // Bạn có thể kiểm tra mã lỗi CSDL (e.getErrorCode()) để xác định lỗi ràng buộc và đưa ra thông báo cụ thể hơn.
            // Ví dụ: SQL Server Foreign Key violation error code is 547
             if (e.getErrorCode() == 547) {
                 System.err.println("Không thể xóa nhân viên này vì có ràng buộc khóa ngoại với các bảng khác (ví dụ: hóa đơn).");
                 // Thông báo cho người dùng biết cần xóa/cập nhật các bản ghi liên quan trước.
             }
        }
        return success; // Trả về kết quả
    }

    // Phương thức lấy tất cả nhân viên (bao gồm thông tin tài khoản)
    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> danhSachNhanVien = new ArrayList<>();
        // SQL SELECT - bao gồm tất cả các cột mới và cũ
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, role FROM NhanVien";

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

                // Set new account fields
                nv.setTendangnhap(rs.getString("Tendangnhap"));
                nv.setMatkhau(rs.getString("Matkhau")); // This is the stored hashed password
                nv.setEmail(rs.getString("Email"));
                nv.setRole(rs.getString("role"));

                danhSachNhanVien.add(nv);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách nhân viên:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return null; // Trả về null nếu có lỗi
        }
        return danhSachNhanVien;
    }

    // Phương thức lấy nhân viên theo MaNV (bao gồm thông tin tài khoản)
    public NhanVien getNhanVienById(String maNV) {
        NhanVien nv = null;
        // SQL SELECT theo MaNV - bao gồm tất cả các cột mới và cũ
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, role FROM NhanVien WHERE MaNV = ?";

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

                    // Set new account fields
                    nv.setTendangnhap(rs.getString("Tendangnhap"));
                    nv.setMatkhau(rs.getString("Matkhau")); // This is the stored hashed password
                    nv.setEmail(rs.getString("Email"));
                    nv.setRole(rs.getString("role"));

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

    // Phương thức lấy nhân viên theo SDT
    // Vẫn chỉ lấy các cột cũ như trong code bạn cung cấp ban đầu cho phương thức này
    // Nếu cần lấy cả thông tin tài khoản khi tìm theo SDT, hãy thêm các cột đó vào câu SELECT
     public NhanVien getNhanVienBySDT(String sdt) {
         NhanVien nv = null;
         String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT FROM NhanVien WHERE SDT = ?";

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
                      // If you need account info here, add them to the SELECT query and set them:
                      // nv.setTendangnhap(rs.getString("Tendangnhap"));
                      // nv.setMatkhau(rs.getString("Matkhau"));
                      // nv.setEmail(rs.getString("Email"));
                      // nv.setRole(rs.getString("role"));
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

    // ************ Triển khai các phương thức tìm kiếm khác *************
    // Cập nhật các phương thức search để lấy thêm cột mới nếu cần thiết cho kết quả tìm kiếm hiển thị

     public List<NhanVien> searchNhanVienByMaNV(String searchTerm) {
         List<NhanVien> danhSachNhanVien = new ArrayList<>();
         // SQL SELECT - bao gồm tất cả các cột mới và cũ cho kết quả đầy đủ
         String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, role FROM NhanVien WHERE MaNV = ?";

         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setString(1, searchTerm); // Tìm chính xác theo mã

             try (ResultSet rs = pstmt.executeQuery()) {
                 while (rs.next()) {
                      NhanVien nv = new NhanVien();
                      nv.setMaNV(rs.getString("MaNV"));
                      nv.setTenNV(rs.getString("TenNV"));
                      nv.setDiachi(rs.getString("Diachi"));
                      nv.setGioitinh(rs.getString("Gioitinh"));
                      nv.setSDT(rs.getString("SDT"));
                       // Set new account fields
                       nv.setTendangnhap(rs.getString("Tendangnhap"));
                       nv.setMatkhau(rs.getString("Matkhau"));
                       nv.setEmail(rs.getString("Email"));
                       nv.setRole(rs.getString("role"));


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

     // Phương thức tìm kiếm nhân viên theo Tên NV (tìm gần đúng)
      public List<NhanVien> searchNhanVienByTenNV(String searchTerm) {
         List<NhanVien> danhSachNhanVien = new ArrayList<>();
         // SQL SELECT - bao gồm tất cả các cột mới và cũ cho kết quả đầy đủ
         String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, role FROM NhanVien WHERE TenNV LIKE ?";
         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setString(1, "%" + searchTerm + "%"); // Tìm kiếm chứa chuỗi con

             try (ResultSet rs = pstmt.executeQuery()) {
                 while (rs.next()) {
                      NhanVien nv = new NhanVien();
                      nv.setMaNV(rs.getString("MaNV"));
                      nv.setTenNV(rs.getString("TenNV"));
                      nv.setDiachi(rs.getString("Diachi"));
                      nv.setGioitinh(rs.getString("Gioitinh"));
                      nv.setSDT(rs.getString("SDT"));
                       // Set new account fields
                       nv.setTendangnhap(rs.getString("Tendangnhap"));
                       nv.setMatkhau(rs.getString("Matkhau"));
                       nv.setEmail(rs.getString("Email"));
                       nv.setRole(rs.getString("role"));
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

     // Phương thức tìm kiếm nhân viên theo Địa chỉ (tìm gần đúng)
      public List<NhanVien> searchNhanVienByDiaChi(String searchTerm) {
         List<NhanVien> danhSachNhanVien = new ArrayList<>();
         // SQL SELECT - bao gồm tất cả các cột mới và cũ cho kết quả đầy đủ
         String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, role FROM NhanVien WHERE Diachi LIKE ?";
         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setString(1, "%" + searchTerm + "%"); // Tìm kiếm chứa chuỗi con

             try (ResultSet rs = pstmt.executeQuery()) {
                 while (rs.next()) {
                      NhanVien nv = new NhanVien();
                      nv.setMaNV(rs.getString("MaNV"));
                      nv.setTenNV(rs.getString("TenNV"));
                      nv.setDiachi(rs.getString("Diachi"));
                      nv.setGioitinh(rs.getString("Gioitinh"));
                      nv.setSDT(rs.getString("SDT"));
                       // Set new account fields
                       nv.setTendangnhap(rs.getString("Tendangnhap"));
                       nv.setMatkhau(rs.getString("Matkhau"));
                       nv.setEmail(rs.getString("Email"));
                       nv.setRole(rs.getString("role"));
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

    // ************ Phương thức quản lý tài khoản (Tích hợp từ ACCDAO) *************

    // Phương thức xác thực đăng nhập
    public NhanVien authenticate(String tendangnhap, String matkhau) {
        NhanVien authenticatedNhanVien = null;
        // Lấy tất cả thông tin nhân viên bao gồm cả tài khoản để kiểm tra mật khẩu và tạo đối tượng NhanVien
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, role FROM NhanVien WHERE Tendangnhap = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, tendangnhap);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Tìm thấy nhân viên với tên đăng nhập này
                    String storedHashedPassword = rs.getString("Matkhau");

                    // TODO: THAY THẾ BẰNG LOGIC XÁC MINH MẬT KHẨU ĐÃ HASH
                    // if (PasswordHasher.verifyPassword(matkhau, storedHashedPassword)) {
                    // Nếu mật khẩu nhập vào (sau khi hash) khớp với mật khẩu lưu trong DB (đã hash)
                    // Sử dụng thư viện hashing để verify!

                    // TEMPORARY: So sánh mật khẩu dạng plain text (KHÔNG AN TOÀN!)
                     if (matkhau.equals(storedHashedPassword)) {
                         // Tạo đối tượng NhanVien đầy đủ nếu xác thực thành công
                         authenticatedNhanVien = new NhanVien();
                         authenticatedNhanVien.setMaNV(rs.getString("MaNV"));
                         authenticatedNhanVien.setTenNV(rs.getString("TenNV"));
                         authenticatedNhanVien.setDiachi(rs.getString("Diachi"));
                         authenticatedNhanVien.setGioitinh(rs.getString("Gioitinh"));
                         authenticatedNhanVien.setSDT(rs.getString("SDT"));
                         authenticatedNhanVien.setTendangnhap(rs.getString("Tendangnhap"));
                         authenticatedNhanVien.setMatkhau(storedHashedPassword); // Lưu mật khẩu đã hash vào object
                         authenticatedNhanVien.setEmail(rs.getString("Email"));
                         authenticatedNhanVien.setRole(rs.getString("role"));
                     } else {
                         // Mật khẩu không khớp
                         System.out.println("Xác thực thất bại: Sai mật khẩu cho tên đăng nhập " + tendangnhap);
                         authenticatedNhanVien = null; // Trả về null nếu sai mật khẩu
                     }
                     // } else {
                     //     // Mật khẩu không khớp (sau khi verify hash)
                     //     System.out.println("Authentication failed: Incorrect password for username " + tendangnhap);
                     //     authenticatedNhanVien = null;
                     // }

                } else {
                     // Không tìm thấy nhân viên với tên đăng nhập này
                     System.out.println("Xác thực thất bại: Không tìm thấy tên đăng nhập " + tendangnhap);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi CSDL khi xác thực đăng nhập:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            // Handle exceptions
        }
        return authenticatedNhanVien; // Trả về đối tượng NhanVien nếu thành công, null nếu thất bại hoặc lỗi
    }

    // Phương thức lấy nhân viên theo tên đăng nhập (để kiểm tra tồn tại, lấy thông tin)
    public NhanVien getNhanVienByTendangnhap(String tendangnhap) {
         NhanVien nv = null;
        // Lấy tất cả thông tin nhân viên bao gồm cả tài khoản
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, role FROM NhanVien WHERE Tendangnhap = ?";
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
                    nv.setMatkhau(rs.getString("Matkhau")); // This is the stored hashed password
                    nv.setEmail(rs.getString("Email"));
                    nv.setRole(rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy nhân viên theo tên đăng nhập:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
        return nv; // Trả về đối tượng NhanVien hoặc null
    }

     // Phương thức lấy nhân viên theo email (để kiểm tra tồn tại, dùng cho quên mật khẩu)
     public NhanVien getNhanVienByEmail(String email) {
         NhanVien nv = null;
         // Lấy tất cả thông tin nhân viên bao gồm cả tài khoản
        String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, role FROM NhanVien WHERE Email = ?";
          try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

              pstmt.setString(1, email);
              try(ResultSet rs = pstmt.executeQuery()) {
                  if (rs.next()) {
                     nv = new NhanVien();
                    nv.setMaNV(rs.getString("MaNV"));
                    nv.setTenNV(rs.getString("TenNV"));
                    nv.setDiachi(rs.getString("Diachi"));
                    nv.setGioitinh(rs.getString("Gioitinh"));
                    nv.setSDT(rs.getString("SDT"));
                    nv.setTendangnhap(rs.getString("Tendangnhap"));
                    nv.setMatkhau(rs.getString("Matkhau")); // This is the stored hashed password
                    nv.setEmail(rs.getString("Email"));
                    nv.setRole(rs.getString("role"));
                  }
              }
          } catch (SQLException e) {
               System.err.println("Lỗi khi lấy nhân viên theo email:");
              System.err.println("SQL State: " + e.getSQLState());
              System.err.println("Error Code: " + e.getErrorCode());
              e.printStackTrace();
          }
         return nv; // Trả về đối tượng NhanVien hoặc null
     }

    // Phương thức cập nhật mật khẩu cho nhân viên
    public boolean updatePassword(String tendangnhap, String newMatkhau) {
        String sql = "UPDATE NhanVien SET Matkhau = ? WHERE Tendangnhap = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // TODO: HASH MẬT KHẨU MỚI TRƯỚC KHI CẬP NHẬT!
            // pstmt.setString(1, PasswordHasher.hashPassword(newMatkhau));
            pstmt.setString(1, newMatkhau); // TEMPORARY: Storing plain text or pre-hashed
            pstmt.setString(2, tendangnhap);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi CSDL khi cập nhật mật khẩu:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            // Handle exceptions
            return false;
        }
    }

     // Phương thức kiểm tra tên đăng nhập đã tồn tại chưa (dùng khi đăng ký/thêm mới)
     public boolean isUsernameExists(String tendangnhap) {
         String sql = "SELECT 1 FROM NhanVien WHERE Tendangnhap = ?";
          try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
              pstmt.setString(1, tendangnhap);
              try(ResultSet rs = pstmt.executeQuery()) {
                  return rs.next(); // Nếu rs.next() là true, tên đăng nhập đã tồn tại
              }
          } catch (SQLException e) {
               System.err.println("Lỗi CSDL khi kiểm tra tên đăng nhập tồn tại:");
              System.err.println("SQL State: " + e.getSQLState());
              System.err.println("Error Code: " + e.getErrorCode());
              e.printStackTrace();
              return false; // Giả định không tồn tại khi có lỗi
          }
     }

      // Phương thức kiểm tra email đã tồn tại chưa (dùng khi đăng ký/thêm mới)
      public boolean isEmailExists(String email) {
         String sql = "SELECT 1 FROM NhanVien WHERE Email = ?";
          try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
              pstmt.setString(1, email);
              try(ResultSet rs = pstmt.executeQuery()) {
                  return rs.next(); // Nếu rs.next() là true, email đã tồn tại
              }
          } catch (SQLException e) {
               System.err.println("Lỗi CSDL khi kiểm tra email tồn tại:");
              System.err.println("SQL State: " + e.getSQLState());
              System.err.println("Error Code: " + e.getErrorCode());
              e.printStackTrace();
              return false; // Giả định không tồn tại khi có lỗi
          }
     }


    // TODO: Thêm các phương thức tìm kiếm khác nếu cần thiết (ví dụ: theo role, theo chucvu)
          public List<NhanVien> searchNhanVienByRole(String searchTerm) {
         List<NhanVien> danhSachNhanVien = new ArrayList<>();
         // SQL SELECT - bao gồm tất cả các cột mới và cũ cho kết quả đầy đủ
         String sql = "SELECT MaNV, TenNV, Diachi, Gioitinh, SDT, Tendangnhap, Matkhau, Email, role FROM NhanVien WHERE role = ?";

         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setString(1, searchTerm); // Tìm chính xác theo mã

             try (ResultSet rs = pstmt.executeQuery()) {
                 while (rs.next()) {
                      NhanVien nv = new NhanVien();
                      nv.setMaNV(rs.getString("MaNV"));
                      nv.setTenNV(rs.getString("TenNV"));
                      nv.setDiachi(rs.getString("Diachi"));
                      nv.setGioitinh(rs.getString("Gioitinh"));
                      nv.setSDT(rs.getString("SDT"));
                       // Set new account fields
                       nv.setTendangnhap(rs.getString("Tendangnhap"));
                       nv.setMatkhau(rs.getString("Matkhau"));
                       nv.setEmail(rs.getString("Email"));
                       nv.setRole(rs.getString("role"));
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


