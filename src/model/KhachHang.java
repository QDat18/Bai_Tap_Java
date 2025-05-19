package model;

public class KhachHang {
    private String MaKH;
    private String Tenkhach;
    private String SDT;

    public KhachHang(){

    }

    public KhachHang(String maKH, String tenkhach, String sdt) {
        this.MaKH = maKH;
        this.Tenkhach = tenkhach;
        this.SDT = sdt;
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
    public String getSDT() {
        return SDT;
    }
    public void setSDT(String sdt) {
        SDT = sdt;
    }

    @Override
    public String toString() {
        return Tenkhach;
    }

}
