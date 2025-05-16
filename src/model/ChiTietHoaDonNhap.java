package model;

public class ChiTietHoaDonNhap {
    private String maHDN;
    private String maSP;
    private int soluong;
    private int dongia;
    private int khuyenmai;
    private int thanhtien;

    private String tenSP;

    // Constructor
    public ChiTietHoaDonNhap() {
    }

    // Constructor with parameters (matching DB columns)
    public ChiTietHoaDonNhap(String maHDN, String maSP, int soluong, int dongia, int khuyenmai, int thanhtien) {
        this.maHDN = maHDN;
        this.maSP = maSP;
        this.soluong = soluong;
        this.dongia = dongia;
        this.khuyenmai = khuyenmai;
        this.thanhtien = thanhtien;
    }

     // Constructor with display fields (for loading data from DAO with joins)
     public ChiTietHoaDonNhap(String maHDN, String maSP, String tenSP, int soluong, int dongia, int khuyenmai, int thanhtien) {
         this.maHDN = maHDN;
         this.maSP = maSP;
         this.tenSP = tenSP;
         this.soluong = soluong;
         this.dongia = dongia;
         this.khuyenmai = khuyenmai;
         this.thanhtien = thanhtien;
     }


    // Getters and Setters
    public String getMaHDN() {
        return maHDN;
    }

    public void setMaHDN(String maHDN) {
        this.maHDN = maHDN;
    }

    public String getMaSP() {
        return maSP;
    }

    public void setMaSP(String maSP) {
        this.maSP = maSP;
    }

    public int getSoluong() {
        return soluong;
    }

    public void setSoluong(int soluong) {
        this.soluong = soluong;
    }

    public int getDongia() {
        return dongia;
    }

    public void setDongia(int dongia) {
        this.dongia = dongia;
    }

    public int getKhuyenmai() {
        return khuyenmai;
    }

    public void setKhuyenmai(int khuyenmai) {
        this.khuyenmai = khuyenmai;
    }

    public int getThanhtien() {
        return thanhtien;
    }

    public void setThanhtien(int thanhtien) {
        this.thanhtien = thanhtien;
    }

     // Getters and Setters for display fields
     public String getTenSP() {
         return tenSP;
     }

     public void setTenSP(String tenSP) {
         this.tenSP = tenSP;
     }


     // Optional: toString() for debugging
    @Override
    public String toString() {
        return "ChiTietHoaDonNhap{" +
               "maHDN='" + maHDN + '\'' +
               ", maSP='" + maSP + '\'' +
               ", soluong=" + soluong +
               ", dongia=" + dongia +
               ", khuyenmai=" + khuyenmai +
               ", thanhtien=" + thanhtien +
               ", tenSP='" + tenSP + '\'' +
               '}';
    }
}
