package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.ChiTietHoaDonNhap;
import model.HoaDonNhap;


public class HoaDonNhapDAO {

    private ChiTietHoaDonNhapDAO chiTietHoaDonNhapDAO;
    private SanPhamDAO sanPhamDAO; // Cần SanPhamDAO để cập nhật tồn kho


    public HoaDonNhapDAO() {
        // Khởi tạo các DAO phụ
         chiTietHoaDonNhapDAO = new ChiTietHoaDonNhapDAO();
         sanPhamDAO = new SanPhamDAO();
         // Các DAO khác không cần khởi tạo ở đây vì join được thực hiện trong SQL của phương thức getAll/getById
    }

    // Phương thức tạo mã Hóa đơn Nhập tự động (mô phỏng generateNextHoaDonBanCode)
    public synchronized String generateNextHoaDonNhapCode() { // synchronized để đảm bảo tính duy nhất cơ bản
        String latestMaHDN = null;
        // Lấy mã HĐN lớn nhất hiện có theo format "HDN" + số
        String sql = "SELECT TOP 1 MaHDN FROM HoaDonNhap WHERE MaHDN LIKE 'HDN%' ORDER BY MaHDN DESC"; // <-- Đảm bảo tên bảng HoaDonNhap chính xác

        try (Connection conn = DatabaseConnection.getConnection(); // <-- Sử dụng DatabaseConnection
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                latestMaHDN = rs.getString("MaHDN");
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy mã HĐN cuối cùng:");
            e.printStackTrace();
             // Nếu có lỗi, trả về null, logic tạo mã ở UI có thể xử lý hoặc báo lỗi
            return null;
        }

        int nextNumber = 1; // Bắt đầu từ 1 nếu chưa có HĐN nào
        if (latestMaHDN != null && latestMaHDN.startsWith("HDN")) {
            try {
                // Lấy phần số sau "HDN"
                String numberPart = latestMaHDN.substring(3);
                nextNumber = Integer.parseInt(numberPart) + 1;
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                System.err.println("Lỗi khi phân tích mã HĐN cuối cùng (" + latestMaHDN + "), bắt đầu lại từ 1.");
                 // Nếu có lỗi phân tích, bắt đầu từ 1
                nextNumber = 1;
            }
        }

         // Định dạng mã mới (ví dụ: HDN001, HDN010)
         // Điều chỉnh số chữ số 0 tùy theo nhu cầu (ví dụ: %03d cho HDN001)
        String nextMaHDN = String.format("HDN%03d", nextNumber);

        // TODO: Có thể thêm logic kiểm tra xem mã vừa tạo đã tồn tại chưa (hiếm khi xảy ra với logic này nhưng cẩn thận)

        return nextMaHDN;
    }


    // Phương thức lấy tất cả hóa đơn nhập (mô phỏng getAllHoaDonBan)
    // Join với NhanVien và NhaCC để lấy tên hiển thị trên UI
    public List<HoaDonNhap> getAllHoaDonNhap() {
        List<HoaDonNhap> hoaDonNhaps = new ArrayList<>();
        // SQL join để lấy thêm tên NV và tên NCC
        String sql = "SELECT hdn.MaHDN, hdn.MaNV, nv.TenNV, hdn.MaNCC, ncc.TenNCC, hdn.Ngaynhap, hdn.Tongtien " + // <-- Đảm bảo tên bảng/cột chính xác
                     "FROM HoaDonNhap hdn " +
                     "JOIN NhanVien nv ON hdn.MaNV = nv.MaNV " +
                     "JOIN NhaCC ncc ON hdn.MaNCC = ncc.MaNCC " +
                     "ORDER BY hdn.Ngaynhap DESC, hdn.MaHDN DESC"; // Sắp xếp theo ngày và mã giảm dần

        try (Connection conn = DatabaseConnection.getConnection(); // <-- Sử dụng DatabaseConnection
             PreparedStatement pstmt = conn.prepareStatement(sql); // Sử dụng PreparedStatement ngay cả khi không có tham số để dễ dàng thêm sau này
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // Cần constructor trong HoaDonNhap model để nhận các trường join (TenNV, TenNCC)
                HoaDonNhap hdn = new HoaDonNhap(
                    rs.getString("MaHDN"),
                    rs.getString("MaNV"),
                    rs.getString("TenNV"), // Lấy TenNV từ join
                    rs.getString("MaNCC"),
                    rs.getString("TenNCC"), // Lấy TenNCC từ join
                    rs.getDate("Ngaynhap"),
                    rs.getInt("Tongtien")
                );
                hoaDonNhaps.add(hdn);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách hóa đơn nhập (có join):");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return null;
        }
        return hoaDonNhaps;
    }

    // Phương thức lấy hóa đơn nhập theo Mã HDN (mô phỏng getHoaDonBanByMaHDB)
     // Join với NhanVien và NhaCC để lấy tên hiển thị trong dialog chi tiết
     public HoaDonNhap getHoaDonNhapByMaHDN(String maHDN) {
         HoaDonNhap hdn = null;
         // SQL join để lấy thêm tên NV và tên NCC
         String sql = "SELECT hdn.MaHDN, hdn.MaNV, nv.TenNV, hdn.MaNCC, ncc.TenNCC, hdn.Ngaynhap, hdn.Tongtien " + // <-- Đảm bảo tên bảng/cột chính xác
                      "FROM HoaDonNhap hdn " +
                      "JOIN NhanVien nv ON hdn.MaNV = nv.MaNV " +
                      "JOIN NhaCC ncc ON hdn.MaNCC = ncc.MaNCC " +
                      "WHERE hdn.MaHDN = ?";

         try (Connection conn = DatabaseConnection.getConnection(); // <-- Sử dụng DatabaseConnection
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setString(1, maHDN);
             try (ResultSet rs = pstmt.executeQuery()) {
                  if (rs.next()) {
                      // Cần constructor trong HoaDonNhap model để nhận các trường join (TenNV, TenNCC)
                      hdn = new HoaDonNhap(
                          rs.getString("MaHDN"),
                          rs.getString("MaNV"),
                          rs.getString("TenNV"), // Lấy TenNV từ join
                          rs.getString("MaNCC"),
                          rs.getString("TenNCC"), // Lấy TenNCC từ join
                          rs.getDate("Ngaynhap"),
                          rs.getInt("Tongtien")
                      );
                  }
             }

         } catch (SQLException e) {
             System.err.println("Lỗi khi lấy hóa đơn nhập theo mã (có join):");
             System.err.println("SQL State: " + e.getSQLState());
             System.err.println("Error Code: " + e.getErrorCode());
             e.printStackTrace();
         }
         return hdn;
     }


    // Phương thức Lưu hóa đơn nhập đầy đủ với Transaction (mô phỏng saveHoaDonBan)
    // Lưu Header, Details và cập nhật TỒN KHO (TĂNG)
    // Trả về true nếu thành công, false nếu thất bại
    public boolean saveHoaDonNhapTransaction(HoaDonNhap hoaDonNhap, List<ChiTietHoaDonNhap> chiTietHoaDon) {
        Connection conn = null;
        boolean success = false;

        try {
            conn = DatabaseConnection.getConnection(); // <-- Lấy kết nối từ DatabaseConnection
            conn.setAutoCommit(false); // <-- Bắt đầu giao dịch

            // 1. Lưu thông tin HoaDonNhap (header)
             String insertHoaDonSql = "INSERT INTO HoaDonNhap (MaHDN, MaNV, MaNCC, Ngaynhap, Tongtien) VALUES (?, ?, ?, ?, ?)"; // <-- Đảm bảo tên bảng/cột chính xác
             try (PreparedStatement pstmt = conn.prepareStatement(insertHoaDonSql)) {
                 pstmt.setString(1, hoaDonNhap.getMaHDN());
                 pstmt.setString(2, hoaDonNhap.getMaNV());
                 pstmt.setString(3, hoaDonNhap.getMaNCC());
                 pstmt.setDate(4, new java.sql.Date(hoaDonNhap.getNgayNhap().getTime())); // Chuyển util.Date sang sql.Date
                 pstmt.setInt(5, hoaDonNhap.getTongTien());

                 int headerAffected = pstmt.executeUpdate();
                  if (headerAffected == 0) {
                       System.err.println("Lưu header hóa đơn nhập thất bại trong giao dịch.");
                       conn.rollback(); // Rollback nếu lưu header thất bại
                       return false;
                  }
             }


            // 2. Lưu danh sách CTHoaDonNhap (details)
            if (chiTietHoaDon != null && !chiTietHoaDon.isEmpty()) {
                for (ChiTietHoaDonNhap ct : chiTietHoaDon) {
                    ct.setMaHDN(hoaDonNhap.getMaHDN()); // Gán MaHDN vừa lưu vào chi tiết
                    // Gọi DAO chi tiết, truyền Connection để sử dụng chung transaction
                    chiTietHoaDonNhapDAO.addChiTietHoaDonNhap(conn, ct); // Phương thức này cần ném SQLException nếu có lỗi

                    // 3. Cập nhật số lượng tồn kho sản phẩm (TĂNG)
                    // Gọi DAO sản phẩm, truyền Connection. updateStockQuantity cần ném SQLException nếu có lỗi
                    // Phương thức updateStockQuantity trong SanPhamDAO cần xử lý việc TĂNG số lượng (ví dụ: quantityChange > 0)
                    sanPhamDAO.updateStockQuantity(conn, ct.getMaSP(), ct.getSoluong());
                }
            }

            conn.commit(); // <-- Commit giao dịch nếu tất cả các bước thành công
            success = true;
            System.out.println("Lưu hóa đơn nhập " + hoaDonNhap.getMaHDN() + " và chi tiết thành công.");

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // <-- Rollback giao dịch on error
                    System.err.println("Rollback giao dịch do lỗi khi lưu hóa đơn nhập.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            System.err.println("Lỗi khi lưu hóa đơn nhập (giao dịch):");
             System.err.println("SQL State: " + e.getSQLState());
             System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            success = false;
        } finally {
            // Đảm bảo kết nối được đóng và auto-commit được khôi phục
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Trả lại chế độ auto-commit mặc định
                    conn.close(); // Đóng kết nối
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return success;
    }

     // TODO: Triển khai các phương thức updateHoaDonNhapTransaction() và deleteHoaDonNhapTransaction() tương tự HoaDonBanDAO
     // Update: cần xóa chi tiết cũ, thêm chi tiết mới, điều chỉnh tồn kho (giảm số lượng cũ, tăng số lượng mới)
     // Delete: cần xóa chi tiet, xóa header, và GIẢM tồn kho sản phẩm đã nhập

     // Phương thức tìm kiếm hóa đơn nhập (mô phỏng searchHoaDonBan)
     // Có thể triển khai ở đây hoặc trong UI bằng cách lọc dữ liệu từ getAllHoaDonNhap (kém hiệu quả)
      /*
      public List<HoaDonNhap> searchHoaDonNhap(String searchTerm, String searchCriteria) {
           List<HoaDonNhap> hoaDonList = new ArrayList<>();
           String sql = ""; // Xây dựng câu SQL tùy theo searchCriteria (Mã HĐN, Tên NV, Tên NCC, Ngày nhập)

           // Cần join tương tự getAllHoaDonNhap()
           // Example based on TenNV:
           if ("Tên NV".equals(searchCriteria)) {
               sql = "SELECT hdn.MaHDN, hdn.MaNV, nv.TenNV, hdn.MaNCC, ncc.TenNCC, hdn.Ngaynhap, hdn.Tongtien " +
                     "FROM HoaDonNhap hdn JOIN NhanVien nv ON hdn.MaNV = nv.MaNV JOIN NhaCC ncc ON hdn.MaNCC = ncc.MaNCC " +
                     "WHERE nv.TenNV LIKE ?";
           }
           // Add other cases for "Mã HĐN", "Tên NCC", "Ngày nhập"

           if (sql.isEmpty()) {
               // Handle unsupported criteria or return all
               return getAllHoaDonNhap(); // Fallback to loading all
           }

           try (Connection conn = DatabaseConnection.getConnection(); // <-- Sử dụng DatabaseConnection
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // Set parameter based on searchCriteria
                // Need careful handling for Date search
                if ("Ngày nhập".equals(searchCriteria)) {
                    // Attempt to parse date - handle errors
                    // try {
                    //     java.util.Date date = new SimpleDateFormat("dd/MM/yyyy").parse(searchTerm);
                    //     pstmt.setDate(1, new java.sql.Date(date.getTime()));
                    // } catch (java.text.ParseException e) {
                    //     System.err.println("Lỗi định dạng ngày tìm kiếm.");
                    //     return hoaDonList; // Return empty list if date format is invalid
                    // }
                } else {
                     pstmt.setString(1, "%" + searchTerm + "%");
                }


               try (ResultSet rs = pstmt.executeQuery()) {
                   while (rs.next()) {
                       HoaDonNhap hdn = new HoaDonNhap(
                            rs.getString("MaHDN"),
                            rs.getString("MaNV"),
                            rs.getString("TenNV"),
                            rs.getString("MaNCC"),
                            rs.getString("TenNCC"),
                            rs.getDate("Ngaynhap"),
                            rs.getInt("Tongtien")
                       );
                       hoaDonList.add(hdn);
                   }
               }
           } catch (SQLException e) {
               System.err.println("Lỗi khi tìm kiếm hóa đơn nhập:");
               e.err.println("SQL State: " + e.getSQLState());
               System.err.println("Error Code: " + e.getErrorCode());
               e.printStackTrace();
               return null;
           }
           return hoaDonList;
       }
       */
}