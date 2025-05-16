package model;

public class SanPham {
    private String MaSP;
    private String TenSP;
    private String Maloai;  
    private int Gianhap;    
    private int Giaban;    
    private int Soluong;     
    private String Anh;      

    // Constructor không đối số
    public SanPham() {
    }

    // Constructor đầy đủ đối số
    public SanPham(String maSP, String tenSP, String maloai, int gianhap, int giaban, int soluong, String anh) {
        MaSP = maSP;
        TenSP = tenSP;
        Maloai = maloai;
        Gianhap = gianhap;
        Giaban = giaban;
        Soluong = soluong;
        Anh = anh;
    }

    // Getters và Setters
    public String getMaSP() {
        return MaSP;
    }

    public void setMaSP(String maSP) {
        MaSP = maSP;
    }

    public String getTenSP() {
        return TenSP;
    }

    public void setTenSP(String tenSP) {
        TenSP = tenSP;
    }

    public String getMaloai() {
        return Maloai;
    }

    public void setMaloai(String maloai) {
        Maloai = maloai;
    }

    public int getGianhap() {
        return Gianhap;
    }

    public void setGianhap(int gianhap) {
        Gianhap = gianhap;
    }

    public int getGiaban() {
        return Giaban;
    }

    public void setGiaban(int giaban) {
        Giaban = giaban;
    }

    public int getSoluong() {
        return Soluong;
    }

    public void setSoluong(int soluong) {
        Soluong = soluong;
    }

    public String getAnh() {
        return Anh;
    }

    public void setAnh(String anh) {
        Anh = anh;
    }
}