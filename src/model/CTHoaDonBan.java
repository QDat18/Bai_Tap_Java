package model;

public class CTHoaDonBan {
    private String MaHDB;
    private String MaSP;
    private int Soluong;
    private int Thanhtien;
    private int Khuyenmai;
    private String TenSP;
    private int Giaban;

    public CTHoaDonBan() {
    }

    // Constructor for creating new CTHoaDonBan
    public CTHoaDonBan(String maHDB, String maSP, int soluong, int thanhtien, int khuyenmai) {
        MaHDB = maHDB;
        MaSP = maSP;
        Soluong = soluong;
        Thanhtien = thanhtien;
        Khuyenmai = khuyenmai;
    }

    // Overloaded constructor including product info (useful when fetching from DAO)
     public CTHoaDonBan(String maHDB, String maSP, int soluong, int thanhtien, int khuyenmai, String tenSP, int giaban) {
         this(maHDB, maSP, soluong, thanhtien, khuyenmai);
         TenSP = tenSP;
         Giaban = giaban;
     }

    // --- Getters and Setters ---

    public String getMaHDB() {
        return MaHDB;
    }

    public void setMaHDB(String MaHDB) {
        this.MaHDB = MaHDB;
    }

    public String getMaSP() {
        return MaSP;
    }

    public void setMaSP(String MaSP) {
        this.MaSP = MaSP;
    }

    public int getSoluong() {
        return Soluong;
    }

    public void setSoluong(int Soluong) {
        this.Soluong = Soluong;
    }

    public int getThanhtien() {
        return Thanhtien;
    }

    public void setThanhtien(int Thanhtien) {
        this.Thanhtien = Thanhtien;
    }

    public int getKhuyenmai() {
        return Khuyenmai;
    }

    public void setKhuyenmai(int Khuyenmai) {
        this.Khuyenmai = Khuyenmai;
    }

    // Getters and Setters for added fields
    public String getTenSP() {
        return TenSP;
    }

    public void setTenSP(String TenSP) {
        this.TenSP = TenSP;
    }

    public int getGiaban() {
        return Giaban;
    }

    public void setGiaban(int Giaban) {
        this.Giaban = Giaban;
    }

     // Optional: Calculate total for this detail line (if needed)
     public int calculateLineTotal() {
         // Assuming Thanhtien already includes quantity and discount
         return Thanhtien;
         // Or if Thanhtien is just unit price after discount: return (Giaban - Khuyenmai) * Soluong; // Adjust calculation based on your logic
     }

    // Optional: Override toString() for debugging
    @Override
    public String toString() {
        return "MaSP: " + MaSP + ", SoLuong: " + Soluong + ", ThanhTien: " + Thanhtien;
    }
}