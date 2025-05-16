package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.Loai;
import model.SanPham;

/**
 * Data Access Object (DAO) để truy vấn dữ liệu cho các báo cáo thống kê.
 * Cung cấp các phương thức để lấy báo cáo doanh thu, tồn kho, sản phẩm bán chạy, và khách hàng mua nhiều nhất.
 */
public class ReportDAO {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("yyyy-MM");
    private static final SimpleDateFormat YEAR_FORMAT = new SimpleDateFormat("yyyy");

    /**
     * Lấy báo cáo doanh thu theo kỳ (ngày, tháng, năm) trong một khoảng thời gian.
     *
     * @param startDate Ngày bắt đầu của khoảng thời gian báo cáo.
     * @param endDate Ngày kết thúc của khoảng thời gian báo cáo.
     * @param periodType Loại kỳ báo cáo ("Ngày", "Tháng", "Năm").
     * @return Danh sách các Object[] chứa dữ liệu báo cáo (ví dụ: {Kỳ báo cáo, Doanh thu}).
     * Trả về null nếu có lỗi hoặc không có dữ liệu.
     */
    public List<Object[]> getRevenueReport(Date startDate, Date endDate, String periodType) {
        // Input validation
        if (startDate == null || endDate == null) {
            System.err.println("Error: startDate or endDate is null.");
            return null;
        }
        if (periodType == null || (!periodType.equals("Ngày") && !periodType.equals("Tháng") && !periodType.equals("Năm"))) {
            System.err.println("Error: Invalid period type for revenue report: " + periodType);
            return null;
        }

        List<Object[]> revenueData = new ArrayList<>();
        String sql;
        SimpleDateFormat currentFormat;
        String periodColumn;

        // Construct SQL query based on periodType
        switch (periodType) {
            case "Ngày":
                sql = "SELECT CAST(NgayBan AS DATE) AS Period, SUM(TongTien) AS TotalRevenue " +
                      "FROM HoaDonBan " +
                      "WHERE NgayBan BETWEEN ? AND ? " +
                      "GROUP BY CAST(NgayBan AS DATE) " +
                      "ORDER BY Period";
                currentFormat = DATE_FORMAT;
                periodColumn = "CAST(NgayBan AS DATE)";
                break;
            case "Tháng":
                sql = "SELECT CAST(YEAR(NgayBan) AS VARCHAR(4)) + '-' + RIGHT('0' + CAST(MONTH(NgayBan) AS VARCHAR(2)), 2) AS Period, SUM(TongTien) AS TotalRevenue " +
                      "FROM HoaDonBan " +
                      "WHERE NgayBan BETWEEN ? AND ? " +
                      "GROUP BY YEAR(NgayBan), MONTH(NgayBan) " +
                      "ORDER BY Period";
                currentFormat = MONTH_FORMAT;
                periodColumn = "CAST(YEAR(NgayBan) AS VARCHAR(4)) + '-' + RIGHT('0' + CAST(MONTH(NgayBan) AS VARCHAR(2)), 2)";
                break;
            case "Năm":
                sql = "SELECT YEAR(NgayBan) AS Period, SUM(TongTien) AS TotalRevenue " +
                      "FROM HoaDonBan " +
                      "WHERE NgayBan BETWEEN ? AND ? " +
                      "GROUP BY YEAR(NgayBan) " +
                      "ORDER BY Period";
                currentFormat = YEAR_FORMAT;
                periodColumn = "YEAR(NgayBan)";
                break;
            default:
                System.err.println("Unexpected period type: " + periodType);
                return null;
        }

        System.out.println("Executing getRevenueReport with periodType=" + periodType + ", startDate=" + startDate + ", endDate=" + endDate);
        System.out.println("SQL Query: " + sql);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, new Timestamp(startDate.getTime()));
            pstmt.setTimestamp(2, new Timestamp(endDate.getTime()));
            System.out.println("SQL Parameters: startDate=" + pstmt.getParameterMetaData().getParameterTypeName(1) + ", endDate=" + pstmt.getParameterMetaData().getParameterTypeName(2));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String period = rs.getString("Period");
                    double totalRevenue = rs.getDouble("TotalRevenue");
                    revenueData.add(new Object[]{period, totalRevenue});
                    System.out.println("Revenue Data: Period=" + period + ", TotalRevenue=" + totalRevenue);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching revenue report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        if (revenueData.isEmpty()) {
            System.out.println("No revenue data found for the given period.");
        }
        return revenueData;
    }

    /**
     * Lấy báo cáo tồn kho của tất cả sản phẩm.
     *
     * @return Danh sách các đối tượng SanPham chứa thông tin tồn kho.
     * Trả về null nếu có lỗi.
     */
    public List<SanPham> getInventoryReport() {
        List<SanPham> inventoryList = new ArrayList<>();
        String sql = "SELECT MaSP, TenSP, Maloai, Soluong, Gianhap " +
                     "FROM SanPham " +
                     "ORDER BY TenSP";

        System.out.println("Executing getInventoryReport");
        System.out.println("SQL Query: " + sql);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                SanPham sp = new SanPham();
                sp.setMaSP(rs.getString("MaSP"));
                sp.setTenSP(rs.getString("TenSP"));
                sp.setMaloai(rs.getString("Maloai"));
                sp.setSoluong(rs.getInt("Soluong"));
                sp.setGianhap((int) rs.getDouble("Gianhap"));
                inventoryList.add(sp);
                System.out.println("Inventory Data: MaSP=" + sp.getMaSP() + ", TenSP=" + sp.getTenSP() + ", Soluong=" + sp.getSoluong());
            }

        } catch (SQLException e) {
            System.err.println("Error fetching inventory report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        if (inventoryList.isEmpty()) {
            System.out.println("No inventory data found.");
        }
        return inventoryList;
    }

    /**
     * Lấy báo cáo các sản phẩm bán chạy nhất trong một khoảng thời gian.
     *
     * @param startDate Ngày bắt đầu của khoảng thời gian báo cáo.
     * @param endDate Ngày kết thúc của khoảng thời gian báo cáo.
     * @param limit Số lượng sản phẩm bán chạy nhất muốn lấy (ví dụ: top 10).
     * @return Danh sách các Object[] chứa dữ liệu báo cáo (ví dụ: {Mã SP, Tên SP, Số lượng bán, Tổng tiền}).
     * Trả về null nếu có lỗi hoặc không có dữ liệu.
     */
    public List<Object[]> getBestSellingProductsReport(Date startDate, Date endDate, int limit) {
        // Input validation
        if (startDate == null || endDate == null) {
            System.err.println("Error: startDate or endDate is null.");
            return null;
        }
        if (limit <= 0) {
            System.err.println("Error: Limit must be positive, got: " + limit);
            return null;
        }

        List<Object[]> bestSellers = new ArrayList<>();
        String sql = "SELECT TOP (?) cthd.MaSP, sp.TenSP, SUM(cthd.SoLuong) AS TotalQuantitySold, SUM(cthd.ThanhTien) AS TotalRevenue " +
                     "FROM CTHoaDonBan cthd " +
                     "JOIN HoaDonBan hdb ON cthd.MaHDB = hdb.MaHDB " +
                     "JOIN SanPham sp ON cthd.MaSP = sp.MaSP " +
                     "WHERE hdb.NgayBan BETWEEN ? AND ? " +
                     "GROUP BY cthd.MaSP, sp.TenSP " +
                     "ORDER BY TotalQuantitySold DESC";

        System.out.println("Executing getBestSellingProductsReport with startDate=" + startDate + ", endDate=" + endDate + ", limit=" + limit);
        System.out.println("SQL Query: " + sql);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            pstmt.setTimestamp(2, new Timestamp(startDate.getTime()));
            pstmt.setTimestamp(3, new Timestamp(endDate.getTime()));
            System.out.println("SQL Parameters: limit=" + limit + ", startDate=" + pstmt.getParameterMetaData().getParameterTypeName(2) + ", endDate=" + pstmt.getParameterMetaData().getParameterTypeName(3));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String maSP = rs.getString("MaSP");
                    String tenSP = rs.getString("TenSP");
                    int totalQuantitySold = rs.getInt("TotalQuantitySold");
                    double totalRevenue = rs.getDouble("TotalRevenue");
                    bestSellers.add(new Object[]{maSP, tenSP, totalQuantitySold, totalRevenue});
                    System.out.println("Best Seller Data: MaSP=" + maSP + ", TenSP=" + tenSP + ", TotalQuantitySold=" + totalQuantitySold);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching best selling products report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        if (bestSellers.isEmpty()) {
            System.out.println("No best-selling products data found for the given period.");
        }
        return bestSellers;
    }

    /**
     * Lấy báo cáo các khách hàng mua nhiều nhất trong một khoảng thời gian.
     *
     * @param startDate Ngày bắt đầu của khoảng thời gian báo cáo.
     * @param endDate Ngày kết thúc của khoảng thời gian báo cáo.
     * @param limit Số lượng khách hàng muốn lấy (ví dụ: top 10).
     * @return Danh sách các Object[] chứa dữ liệu báo cáo (ví dụ: {Mã KH, Tên KH, Tổng số lượng mua}).
     * Trả về null nếu có lỗi hoặc không có dữ liệu.
     */
    public List<Object[]> getTopSpendingCustomersReport(Date startDate, Date endDate, int limit) {
        // Input validation
        if (startDate == null || endDate == null) {
            System.err.println("Error: startDate or endDate is null.");
            return null;
        }
        if (limit <= 0) {
            System.err.println("Error: Limit must be positive, got: " + limit);
            return null;
        }

        List<Object[]> topCustomers = new ArrayList<>();
        String sql = "SELECT TOP (?) kh.MaKH, kh.Tenkhach AS Tenkhach, SUM(cthd.SoLuong) AS TotalQuantityPurchased " + // Changed TenKH to HoTen
                     "FROM KhachHang kh " +
                     "JOIN HoaDonBan hdb ON kh.MaKH = hdb.MaKH " +
                     "JOIN CTHoaDonBan cthd ON hdb.MaHDB = cthd.MaHDB " +
                     "WHERE hdb.NgayBan BETWEEN ? AND ? " +
                     "GROUP BY kh.MaKH, kh.Tenkhach " + // Changed TenKH to HoTen
                     "ORDER BY TotalQuantityPurchased DESC";

        System.out.println("Executing getTopSpendingCustomersReport with startDate=" + startDate + ", endDate=" + endDate + ", limit=" + limit);
        System.out.println("SQL Query: " + sql);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            pstmt.setTimestamp(2, new Timestamp(startDate.getTime()));
            pstmt.setTimestamp(3, new Timestamp(endDate.getTime()));
            System.out.println("SQL Parameters: limit=" + limit + ", startDate=" + pstmt.getParameterMetaData().getParameterTypeName(2) + ", endDate=" + pstmt.getParameterMetaData().getParameterTypeName(3));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String maKH = rs.getString("MaKH");
                    String tenKH = rs.getString("Tenkhach");
                    int totalQuantityPurchased = rs.getInt("TotalQuantityPurchased");
                    topCustomers.add(new Object[]{maKH, tenKH, totalQuantityPurchased});
                    System.out.println("Top Customer Data: MaKH=" + maKH + ", Tenkhach=" + tenKH + ", TotalQuantityPurchased=" + totalQuantityPurchased);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching top spending customers report: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        if (topCustomers.isEmpty()) {
            System.out.println("No top spending customers data found for the given period.");
        }
        return topCustomers;
    }

    /**
     * Lấy thông tin loại sản phẩm theo mã loại.
     *
     * @param maloai Mã loại cần lấy thông tin.
     * @return Đối tượng Loai hoặc null nếu không tìm thấy.
     */
    public Loai getLoaiById(String maloai) {
        if (maloai == null || maloai.trim().isEmpty()) {
            System.err.println("Error: Maloai is null or empty.");
            return null;
        }

        String sql = "SELECT Maloai, Tenloai FROM Loai WHERE Maloai = ?";
        System.out.println("Executing getLoaiById with maloai=" + maloai);
        System.out.println("SQL Query: " + sql);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maloai);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Loai loai = new Loai();
                    loai.setMaloai(rs.getString("Maloai"));
                    loai.setTenloai(rs.getString("Tenloai"));
                    System.out.println("Loai Data: Maloai=" + loai.getMaloai() + ", Tenloai=" + loai.getTenloai());
                    return loai;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting Loai by id: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        System.out.println("No Loai found for Maloai=" + maloai);
        return null;
    }
}