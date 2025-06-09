package model;

public class Loai {
    private String Maloai;
    private String Tenloai;

    public Loai() {
    }

    public Loai(String maloai, String tenloai) {
        Maloai = maloai;
        Tenloai = tenloai;
    }

    public String getMaloai() {
        return Maloai;
    }

    public String getTenloai() {
        return Tenloai;
    }

    public void setMaloai(String maloai) {
        Maloai = maloai;
    }

    public void setTenloai(String tenloai) {
        Tenloai = tenloai;
    }

    @Override
    public String toString() {
        return Tenloai;
    }
}