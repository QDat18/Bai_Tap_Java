package model;

import java.util.Date; // For Ngayban

public class HoaDonBan {
    private String MaHDB;
    private String MaNV;
    private String MaKH;
    private Date Ngayban;
    private int Tongtien; // Changed to int or double based on your database type

    // Added fields for displaying names in UI (populated by DAO joins)
    private String TenNV;
    private String TenKH;

    public HoaDonBan() {
    }

    // Constructor for creating new HoaDonBan
    public HoaDonBan(String maHDB, String maNV, String maKH, Date ngayban, int tongtien) {
        MaHDB = maHDB;
        MaNV = maNV;
        MaKH = maKH;
        Ngayban = ngayban;
        Tongtien = tongtien;
    }

    // --- Getters and Setters ---

    public String getMaHDB() {
        return MaHDB;
    }

    public void setMaHDB(String MaHDB) {
        this.MaHDB = MaHDB;
    }

    public String getMaNV() {
        return MaNV;
    }

    public void setMaNV(String MaNV) {
        this.MaNV = MaNV;
    }

    public String getMaKH() {
        return MaKH;
    }

    public void setMaKH(String MaKH) {
        this.MaKH = MaKH;
    }

    public Date getNgayban() {
        return Ngayban;
    }

    public void setNgayban(Date Ngayban) {
        this.Ngayban = Ngayban;
    }

    public int getTongtien() {
        return Tongtien;
    }

    public void setTongtien(int Tongtien) {
        this.Tongtien = Tongtien;
    }

    // Getters and Setters for added fields
    public String getTenNV() {
        return TenNV;
    }

    public void setTenNV(String TenNV) {
        this.TenNV = TenNV;
    }

    public String getTenKH() {
        return TenKH;
    }

    public void setTenKH(String TenKH) {
        this.TenKH = TenKH;
    }

    @Override
    public String toString() {
        return MaHDB + " - " + (Ngayban != null ? Ngayban.toString() : "") + " - " + TenKH;
    }
}