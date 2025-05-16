package dao;

import model.NhaCC; // Import model NhaCC
import dao.DatabaseConnection; // Import DatabaseConnection
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NhaCCDAO {

    // Constructor (if needed)
    public NhaCCDAO() {
    }

    // --- Phương thức trợ giúp để lấy mã nhà cung cấp lớn nhất hiện có ---
    // Trả về mã NCC lớn nhất (vd: "NCC15") hoặc null nếu chưa có NCC nào
    private String getHighestMaNCC() {
        String latestMaNCC = null;
        // SQL Server query to find the highest numeric part of existing MaNCC
        // Assumes MaNCC format is 'NCC' followed by numbers
        String sql = "SELECT TOP 1 MaNCC FROM NhaCC " + // <== Đảm bảo tên bảng NhaCC chính xác
                     "WHERE MaNCC LIKE 'NCC[0-9]%' " + // Filter for format like 'NCC1', 'NCC01', 'NCC12'
                     "ORDER BY CAST(SUBSTRING(MaNCC, 4, LEN(MaNCC) - 3) AS INT) DESC"; // Order by the numeric part

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                latestMaNCC = rs.getString("MaNCC");
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy mã nhà cung cấp cuối cùng:");
            e.printStackTrace();
            // Trả về null nếu có lỗi
            return null;
        }
        return latestMaNCC;
    }

    // --- Phương thức công khai để UI lấy mã gợi ý tiếp theo ---
    // UI sẽ gọi phương thức này để lấy mã gợi ý và hiển thị
    public String suggestNextMaNCC() {
        String latestMaNCC = getHighestMaNCC(); // Lấy mã lớn nhất hiện có

        int nextNumber = 1; // Bắt đầu từ 1 nếu chưa có NCC nào
        if (latestMaNCC != null && latestMaNCC.startsWith("NCC")) {
            try {
                String numberPart = latestMaNCC.substring(3);
                nextNumber = Integer.parseInt(numberPart) + 1;
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                System.err.println("Lỗi khi phân tích mã nhà cung cấp cuối cùng (" + latestMaNCC + "), bắt đầu lại từ NCC01.");
                 // Nếu có lỗi phân tích, bắt đầu từ 1
                nextNumber = 1;
            }
        }

         // Định dạng mã mới (vd: NCC01, NCC10)
        // Sử dụng String.format để định dạng số với leading zeros
        // "%02d" cho ít nhất 2 chữ số (NCC01, NCC09, NCC10)
        // "%03d" cho ít nhất 3 chữ số (NCC001, NCC009, NCC010, NCC100)
        // Chọn định dạng phù hợp với yêu cầu của bạn. Tôi dùng %02d.
        String nextMaNCC = String.format("NCC%02d", nextNumber);


        // TODO: Kiểm tra xem mã vừa tạo đã tồn tại chưa (trường hợp race condition hiếm gặp trong ứng dụng đơn giản)
        // Để đảm bảo tuyệt đối không trùng lặp trong môi trường đa người dùng, cần cơ chế phức tạp hơn (SEQUENCE trong DB).
        // Với ứng dụng desktop đơn giản, cách này thường chấp nhận được.


        return nextMaNCC;
    }


    // --- Phương thức thêm nhà cung cấp (DAO sử dụng mã từ đối tượng NCC) ---
    // UI chịu trách nhiệm gán MaNCC (có thể là gợi ý hoặc do người dùng sửa) trước khi gọi phương thức này
    // Trả về true nếu thành công, false nếu thất bại
    public boolean addNhaCC(NhaCC ncc) {
        // Kiểm tra xem đối tượng NCC đã có MaNCC chưa và không rỗng
        if (ncc.getMaNCC() == null || ncc.getMaNCC().trim().isEmpty()) {
             System.err.println("Đối tượng NhaCC không có MaNCC được gán. Không thể thêm.");
             return false;
        }
        String maNCC = ncc.getMaNCC().trim();

        // TODO: Optional: Thêm kiểm tra xem MaNCC này đã tồn tại trong DB chưa trước khi INSERT
        // Bạn có thể viết phương thức getNhaCCByMaNCC(maNCC) và kiểm tra kết quả.
        // Nếu đã tồn tại, thông báo cho UI biết để người dùng chọn mã khác.
        // Hoặc dựa vào ràng buộc UNIQUE KEY trên cột MaNCC trong DB để bắt lỗi khi executeUpdate() trả về false hoặc ném exception.
        // Cách hiện tại dựa vào bắt SQLException khi trùng UNIQUE KEY.

        String sql = "INSERT INTO NhaCC (MaNCC, TenNCC, Diachi, SDT) VALUES (?, ?, ?, ?)"; // <== Đảm bảo tên bảng NhaCC chính xác
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maNCC); // Sử dụng MaNCC từ đối tượng NCC (đã được gợi ý/chỉnh sửa trên UI và gán vào object)
            pstmt.setString(2, ncc.getTenNCC());
            pstmt.setString(3, ncc.getDiachi());
            pstmt.setString(4, ncc.getSDT());

            int affectedRows = pstmt.executeUpdate();
             if (affectedRows > 0) {
                 System.out.println("Thêm nhà cung cấp thành công: " + maNCC);
                 return true;
             } else {
                 // affectedRows = 0 có thể xảy ra nhưng hiếm với INSERT unless something is wrong
                 System.out.println("Thêm nhà cung cấp thất bại (Không có dòng nào được thêm): " + maNCC + ".");
                 return false;
             }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm nhà cung cấp:");
            // SQLException có thể xảy ra nếu MaNCC đã tồn tại (Unique Key violation) hoặc lỗi DB khác.
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            // Thông báo lỗi cụ thể hơn nếu là lỗi trùng mã (error code 2627 cho SQL Server UNIQUE constraint)
            if (e.getErrorCode() == 2627) { // Check for SQL Server UNIQUE constraint violation error code
                 System.err.println("Mã Nhà Cung Cấp '" + maNCC + "' đã tồn tại.");
                 // Bạn có thể muốn ném một exception tùy chỉnh ở đây để UI bắt và hiển thị thông báo "Mã đã tồn tại"
            }
            return false; // Trả về false nếu có lỗi SQL
        }
    }

    // Các phương thức khác (update, delete, getAll, getById, searchByName) giữ nguyên như đã sửa trước đó

     // Phương thức cập nhật thông tin nhà cung cấp
     public boolean updateNhaCC(NhaCC ncc) {
          String sql = "UPDATE NhaCC SET TenNCC = ?, Diachi = ?, SDT = ? WHERE MaNCC = ?"; // <== Đảm bảo tên bảng NhaCC chính xác
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, ncc.getTenNCC());
            pstmt.setString(2, ncc.getDiachi());
            pstmt.setString(3, ncc.getSDT());
            pstmt.setString(4, ncc.getMaNCC());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Cập nhật nhà cung cấp thành công: " + ncc.getMaNCC());
                return true;
            } else {
                System.out.println("Không tìm thấy nhà cung cấp để cập nhật: " + ncc.getMaNCC());
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật nhà cung cấp:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
     }

    // Phương thức xóa nhà cung cấp
    public boolean deleteNhaCC(String maNCC) {
         String sql = "DELETE FROM NhaCC WHERE MaNCC = ?"; // <== Đảm bảo tên bảng NhaCC chính xác
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maNCC);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Xóa nhà cung cấp thành công: " + maNCC);
                return true;
            } else {
                System.out.println("Không tìm thấy nhà cung cấp để xóa: " + maNCC);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa nhà cung cấp:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            // TODO: Xử lý lỗi ràng buộc khóa ngoại nếu cần (Error Code 547 cho SQL Server Foreign Key violation)
            if (e.getErrorCode() == 547) {
                 System.err.println("Không thể xóa. Nhà cung cấp có mã '" + maNCC + "' đang được sử dụng.");
                  // Bạn có thể muốn ném một exception tùy chỉnh ở đây
            }
            return false;
        }
    }

    // Phương thức lấy tất cả nhà cung cấp
    public List<NhaCC> getAllNhaCC() {
        List<NhaCC> danhSachNhaCC = new ArrayList<>();
        String sql = "SELECT MaNCC, TenNCC, Diachi, SDT FROM NhaCC"; // <== Đảm bảo tên bảng NhaCC chính xác
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                NhaCC ncc = new NhaCC();
                ncc.setMaNCC(rs.getString("MaNCC"));
                ncc.setTenNCC(rs.getString("TenNCC"));
                ncc.setDiachi(rs.getString("Diachi"));
                ncc.setSDT(rs.getString("SDT"));
                danhSachNhaCC.add(ncc);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách nhà cung cấp:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
             // Trả về danh sách rỗng nếu có lỗi
        }
        return danhSachNhaCC;
    }

     // Phương thức lấy nhà cung cấp theo mã
     public NhaCC getNhaCCByMaNCC(String maNCC) {
        NhaCC ncc = null;
        String sql = "SELECT MaNCC, TenNCC, Diachi, SDT FROM NhaCC WHERE MaNCC = ?"; // <== Đảm bảo tên bảng NhaCC chính xác
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maNCC);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    ncc = new NhaCC();
                    ncc.setMaNCC(rs.getString("MaNCC"));
                    ncc.setTenNCC(rs.getString("TenNCC"));
                    ncc.setDiachi(rs.getString("Diachi"));
                    ncc.setSDT(rs.getString("SDT"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy nhà cung cấp theo mã:");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
        return ncc;
     }


    // Phương thức tìm kiếm nhà cung cấp theo tên
     public List<NhaCC> searchNhaCCByName(String tenNCC) {
        List<NhaCC> danhSachNhaCC = new ArrayList<>();
        String sql = "SELECT MaNCC, TenNCC, Diachi, SDT FROM NhaCC WHERE TenNCC LIKE ?"; // <== Đảm bảo tên bảng NhaCC chính xác
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + tenNCC + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    NhaCC ncc = new NhaCC();
                    ncc.setMaNCC(rs.getString("MaNCC"));
                    ncc.setTenNCC(rs.getString("TenNCC"));
                    ncc.setDiachi(rs.getString("Diachi"));
                    ncc.setSDT(rs.getString("SDT"));
                    danhSachNhaCC.add(ncc);
                }
            }
        } catch (SQLException e) {
             System.err.println("Lỗi khi tìm kiếm nhà cung cấp theo tên:");
             System.err.println("SQL State: " + e.getSQLState());
             System.err.println("Error Code: " + e.getErrorCode());
             e.printStackTrace();
        }
        return danhSachNhaCC;
    }

    // TODO: Có thể thêm các phương thức tìm kiếm khác (ví dụ: tìm theo địa chỉ, SĐT)
}
