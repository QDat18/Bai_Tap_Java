package model;

public class KhachHang {
    private String MaKH;
    private String Tenkhach;
    private String Diachi;

    public KhachHang(){

    }

    public KhachHang(String maKH, String tenkhach, String diachi) {
        MaKH = maKH;
        Tenkhach = tenkhach;
        Diachi = diachi;
    }

    public String getMaKH() {
        return MaKH;
    }
    public void setMaKH(String maKH) {
        MaKH = maKH;
    }
    public String getTenkhach() {
        return Tenkhach;
    }
    public void setTenkhach(String tenkhach) {
        Tenkhach = tenkhach;
    }
    public String getDiachi() {
        return Diachi;
    }
    public void setDiachi(String diachi) {
        Diachi = diachi;
    }

    @Override
    public String toString() {
        return Tenkhach;
    }

}
