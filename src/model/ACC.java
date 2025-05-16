package model;

public class ACC {
    private String tendangnhap;
    private String matkhau;
    private String email;
    private String role;
    private String chucvu;
    private String tenHienThi;
    private String maNV; 

    // Constructors
    public ACC() {
    }

    // Ví dụ Constructor đầy đủ:
    public ACC(String tendangnhap, String matkhau, String email, String role, String chucvu, String tenHienThi, String maNV) {
        this.tendangnhap = tendangnhap;
        this.matkhau = matkhau;
        this.email = email;
        this.role = role;
        this.chucvu = chucvu;
        this.tenHienThi = tenHienThi;
        this.maNV = maNV;
    }

     public ACC(String tendangnhap, String matkhau, String email, String role, String chucvu, String tenHienThi) {
         this.tendangnhap = tendangnhap;
         this.matkhau = matkhau;
         this.email = email;
         this.role = role;
         this.chucvu = chucvu;
         this.tenHienThi = tenHienThi;
         this.maNV = null;
     }


    // Getters and Setters
    public String getTendangnhap() {
        return tendangnhap;
    }

    public void setTendangnhap(String tendangnhap) {
        this.tendangnhap = tendangnhap;
    }

    public String getMatkhau() {
        return matkhau;
    }

    public void setMatkhau(String matkhau) {
        this.matkhau = matkhau;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getChucvu() {
        return chucvu;
    }

    public void setChucvu(String chucvu) {
        this.chucvu = chucvu;
    }

    public String getTenHienThi() {
        return tenHienThi;
    }

    public void setTenHienThi(String tenHienThi) {
        this.tenHienThi = tenHienThi;
    }

    // <-- THÊM GETTER VÀ SETTER CHO maNV
    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }


    // Optional: toString()
    @Override
    public String toString() {
        return "ACC{" +
               "tendangnhap='" + tendangnhap + '\'' +
               // ", matkhau='" + matkhau + '\'' + // Không nên in mật khẩu
               ", email='" + email + '\'' +
               ", role='" + role + '\'' +
               ", chucvu='" + chucvu + '\'' +
               ", tenHienThi='" + tenHienThi + '\'' +
               ", maNV='" + maNV + '\'' + // <-- THÊM maNV VÀO toString
               '}';
    }
}
