package model;

import java.util.Date;

public class HoaDonNhap {
    private String maHDN;
    private String maNV;
    private String maNCC; // Correct field name based on DB schema
    private Date ngayNhap;
    private int tongTien;

    // Optional: Add display fields for UI based on joins (similar to HoaDonBan model if it has them)
    private String tenNV;
    private String tenNCC;

    // Constructor
    public HoaDonNhap() {
    }

    // Constructor with parameters (matching DB columns)
    public HoaDonNhap(String maHDN, String maNV, String maNCC, Date ngayNhap, int tongTien) {
        this.maHDN = maHDN;
        this.maNV = maNV;
        this.maNCC = maNCC;
        this.ngayNhap = ngayNhap;
        this.tongTien = tongTien;
    }

     // Constructor with display fields (for loading data from DAO with joins)
     public HoaDonNhap(String maHDN, String maNV, String tenNV, String maNCC, String tenNCC, Date ngayNhap, int tongTien) {
        this.maHDN = maHDN;
        this.maNV = maNV;
        this.tenNV = tenNV;
        this.maNCC = maNCC;
        this.tenNCC = tenNCC;
        this.ngayNhap = ngayNhap;
        this.tongTien = tongTien;
     }


    // Getters and Setters
    public String getMaHDN() {
        return maHDN;
    }

    public void setMaHDN(String maHDN) {
        this.maHDN = maHDN;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(String maNCC) {
        this.maNCC = maNCC;
    }

    public Date getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(Date ngayNhap) {
        this.ngayNhap = ngayNhap;
    }

    public int getTongTien() {
        return tongTien;
    }

    public void setTongTien(int tongTien) {
        this.tongTien = tongTien;
    }

     // Getters and Setters for display fields
     public String getTenNV() {
         return tenNV;
     }

     public void setTenNV(String tenNV) {
         this.tenNV = tenNV;
     }

     public String getTenNCC() {
         return tenNCC;
     }

     public void setTenNCC(String tenNCC) {
         this.tenNCC = tenNCC;
     }


    // Optional: toString() for debugging
    @Override
    public String toString() {
        return "HoaDonNhap{" +
               "maHDN='" + maHDN + '\'' +
               ", maNV='" + maNV + '\'' +
               ", maNCC='" + maNCC + '\'' +
               ", ngayNhap=" + ngayNhap +
               ", tongTien=" + tongTien +
               ", tenNV='" + tenNV + '\'' +
               ", tenNCC='" + tenNCC + '\'' +
               '}';
    }
}