package model;

public class NhaCC {
    private String MaNCC;
    private String TenNCC;
    private String Diachi;
    private String SDT;

    public NhaCC(){

    }

    public NhaCC(String maNCC, String tenNCC, String diachi, String sdt){
        this.MaNCC = maNCC;
        this.TenNCC = tenNCC;
        this.Diachi = diachi;
        this.SDT = sdt;
    }

    public String getMaNCC(){
        return MaNCC;
    }

    public void setMaNCC(String maNCC){
        this.MaNCC = maNCC;
    }

    public String getTenNCC() {
        return TenNCC;
    }
    public void setTenNCC(String tenNCC) {
        this.TenNCC = tenNCC;
    }
    public String getDiachi() {
        return Diachi;
    }
    public void setDiachi(String diachi) {
        this.Diachi = diachi;
    }

    public String getSDT(){
        return SDT;
    }

    public void setSDT(String sdt)
    {
        this.SDT = sdt;
    }
    
    @Override
    public String toString() {
        return TenNCC;
    }

}
