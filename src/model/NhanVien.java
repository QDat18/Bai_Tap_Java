package model;

public class NhanVien {
    private String MaNV;
    private String TenNV;
    private String Diachi;
    private String Gioitinh;
    private String SDT;
    private String tendangnhap;
    private String matkhau; // Should store hashed password
    private String email;
    private String role;

    public NhanVien(){

    }
    public NhanVien(String maNV, String tenNV, String soDienThoai, String diaChi,
                    String tendangnhap, String matkhau, String email, String role) {
        this.MaNV = maNV;
        this.TenNV = tenNV;
        this.SDT = soDienThoai;
        this.Diachi = diaChi;
        this.tendangnhap = tendangnhap;
        this.matkhau = matkhau;
        this.email = email;
        this.role = role;
    }

    public String getMaNV() {
         return MaNV; 
    }
    
    public void setMaNV(String maNV) {
        MaNV = maNV; 
    }

    public String getTenNV() { 
        return TenNV; 
    }
    
    public void setTenNV(String tenNV) { 
        TenNV = tenNV; 
    }

    public String getDiachi() { 
        return Diachi; 
    }
    
    public void setDiachi(String diachi) { 
        Diachi = diachi; 
    }

    public String getGioitinh() { 
        return Gioitinh; 
    }
    
    public void setGioitinh(String gioitinh) { 
        Gioitinh = gioitinh; 
    } 

    public String getSDT() { 
        return SDT; 
    }
    
    public void setSDT(String sdt) {
        SDT = sdt; 
    }
    public String getTendangnhap() { return tendangnhap; }
    public void setTendangnhap(String tendangnhap) { this.tendangnhap = tendangnhap; }

    public String getMatkhau() { return matkhau; }
    public void setMatkhau(String matkhau) { this.matkhau = matkhau; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        // Adjust toString to include relevant new fields if needed for display
        return TenNV; // Or whatever is appropriate for displaying NhanVien objects
    }
}
