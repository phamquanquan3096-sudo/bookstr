package com.quanly.config;

import com.quanly.entity.*;
import com.quanly.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ChucVuRepository chucVuRepo;
    private final NhanVienRepository nhanVienRepo;
    private final LoaiDLRepository loaiDLRepo;
    private final DaiLyRepository daiLyRepo;
    private final SanPhamRepository sanPhamRepo;
    private final KhoRepository khoRepo;

    public DataInitializer(ChucVuRepository chucVuRepo,
                           NhanVienRepository nhanVienRepo,
                           LoaiDLRepository loaiDLRepo,
                           DaiLyRepository daiLyRepo,
                           SanPhamRepository sanPhamRepo,
                           KhoRepository khoRepo) {
        this.chucVuRepo = chucVuRepo;
        this.nhanVienRepo = nhanVienRepo;
        this.loaiDLRepo = loaiDLRepo;
        this.daiLyRepo = daiLyRepo;
        this.sanPhamRepo = sanPhamRepo;
        this.khoRepo = khoRepo;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed Roles (ChucVu)
        chucVuRepo.save(new ChucVu("NVKD", "Nhân viên Kinh doanh"));
        chucVuRepo.save(new ChucVu("NVKT", "Nhân viên Kế toán"));
        chucVuRepo.save(new ChucVu("NVK", "Nhân viên Kho"));
        chucVuRepo.save(new ChucVu("QTHT", "Quản trị hệ thống"));
        chucVuRepo.save(new ChucVu("NVGH", "Nhân viên Giao hàng"));

        // Seed Staff (NhanVien) from project credentials
        if (nhanVienRepo.count() == 0) {
            saveStaff("NV01", "Nguyễn Văn Kinh Doanh", "NVKD", "0901234567", "kd@gmail.com", "kinhdoanh", "123");
            saveStaff("NV02", "Trần Thị Kế Toán", "NVKT", "0901234568", "kt@gmail.com", "NVKT", "123");
            saveStaff("NV03", "Lê Văn Kho", "NVK", "0901234569", "kho@gmail.com", "kho", "123");
            saveStaff("NV04", "Quản Trị Hệ Thống", "QTHT", "0901234570", "admin@gmail.com", "QTHT", "QTHT");
            saveStaff("NV05", "Nguyễn Văn Giao Hàng", "NVGH", "0901234571", "gh@gmail.com", "NVGH", "NVGH");
        }

        // Seed Agency Types (LoaiDL)
        loaiDLRepo.save(new LoaiDL("LDL01", "Đại lý Cấp 1", 500000000.0));
        loaiDLRepo.save(new LoaiDL("LDL02", "Đại lý Cấp 2", 200000000.0));
        loaiDLRepo.save(new LoaiDL("LDL03", "Đại lý Mới", 50000000.0));

        // Seed Agency (DaiLy)
        if (daiLyRepo.count() == 0) {
            DaiLy dl = new DaiLy();
            dl.setMaDL("DL01");
            dl.setMaLoaiDL("LDL01");
            dl.setUserName("daily");
            dl.setPassword("123456");
            dl.setTenDL("Đại lý Minh Phát");
            dl.setSdt("0988776655");
            dl.setDiaChi("123 Nguyễn Văn Cừ, Q.5, TP.HCM");
            dl.setEmail("daily@gmail.com");
            dl.setNgayTao(new Date());
            dl.setTongDoanhSo(150000000.0);
            daiLyRepo.save(dl);
        }

        // Seed Products (SanPham)
        if (sanPhamRepo.count() == 0) {
            saveProduct("SP01", "Nước ngọt Coca-Cola 330ml", "Thùng", 240000, 500);
            saveProduct("SP02", "Nước khoáng Lavie 500ml", "Thùng", 95000, 1000);
            saveProduct("SP03", "Bánh Quy Bơ Danisa 454g", "Hộp", 185000, 300);
            saveProduct("SP04", "Sữa tươi Vinamilk 100%", "Thùng", 360000, 800);
            saveProduct("SP05", "Mì Hảo Hảo Tôm Chua Cay", "Thùng", 115000, 1500);
        }

        // Seed Warehouses (Kho)
        if (khoRepo.count() == 0) {
            khoRepo.save(new Kho("KHO01", "Kho Tổng Tân Bình", "Trường Chinh, Tân Bình, TP.HCM"));
            khoRepo.save(new Kho("KHO02", "Kho Miền Tây", "TP. Cần Thơ"));
        }
    }

    private void saveStaff(String maNV, String tenNV, String maChucVu, String sdt, String email, String username, String pass) {
        NhanVien nv = new NhanVien();
        nv.setMaNV(maNV);
        nv.setTenNV(tenNV);
        nv.setMaChucVu(maChucVu);
        nv.setSdt(sdt);
        nv.setEmail(email);
        nv.setUserName(username);
        nv.setPassword(pass);
        nhanVienRepo.save(nv);
    }

    private void saveProduct(String maSP, String tenSP, String donViTinh, Integer gia, Integer tongTon) {
        SanPham sp = new SanPham();
        sp.setMaSP(maSP);
        sp.setTenSP(tenSP);
        sp.setDonViTinh(donViTinh);
        sp.setGia(gia);
        sp.setTongTon(tongTon);
        sp.setNgaySX(new Date());
        sanPhamRepo.save(sp);
    }
}
