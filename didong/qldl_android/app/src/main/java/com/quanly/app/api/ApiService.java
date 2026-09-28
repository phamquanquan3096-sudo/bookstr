package com.quanly.app.api;

import com.quanly.app.model.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface ApiService {

    @POST("api/v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/v1/auth/register-daily")
    Call<ApiResponse<DaiLy>> registerDaily(@Body DaiLy daiLy);

    @GET("api/v1/sanpham")
    Call<ApiResponse<List<SanPham>>> getSanPhams(@Query("query") String query);

    @POST("api/v1/sanpham")
    Call<ApiResponse<SanPham>> saveSanPham(@Body SanPham sp);

    @DELETE("api/v1/sanpham/{id}")
    Call<ApiResponse<String>> deleteSanPham(@Path("id") String id);

    @Multipart
    @POST("api/v1/upload")
    Call<ApiResponse<String>> uploadImage(@Part okhttp3.MultipartBody.Part file);

    @GET("api/v1/donhang")
    Call<ApiResponse<List<DonHang>>> getDonHangs(
            @Query("maDL") String maDL,
            @Query("maGH") String maGH,
            @Query("trangThai") String trangThai
    );

    @POST("api/v1/donhang")
    Call<ApiResponse<DonHang>> createDonHang(@Body OrderCreateRequest request);

    @GET("api/v1/donhang/{id}")
    Call<ApiResponse<java.util.Map<String, Object>>> getOrderDetail(@Path("id") String id);

    @PUT("api/v1/donhang/{id}/trangthai")
    Call<ApiResponse<DonHang>> updateOrderStatus(@Path("id") String id, @Query("trangThai") String trangThai);

    @PUT("api/v1/donhang/{id}/phe-duyet")
    Call<ApiResponse<DonHang>> approveOrder(@Path("id") String id);

    @GET("api/v1/ketoan/congno")
    Call<ApiResponse<List<PhieuCongNo>>> getCongNo(@Query("maDL") String maDL);

    @POST("api/v1/ketoan/congno")
    Call<ApiResponse<PhieuCongNo>> taoPhieuCongNo(@Body PhieuCongNo phieu);

    @PUT("api/v1/ketoan/congno/{maPhieuCN}/thutien")
    Call<ApiResponse<PhieuCongNo>> thuTienCongNo(@Path("maPhieuCN") String maPhieuCN, @Query("soTienThu") double soTienThu);

    @DELETE("api/v1/ketoan/congno/{maPhieuCN}")
    Call<ApiResponse<String>> deleteCongNo(@Path("maPhieuCN") String maPhieuCN);

    @GET("api/v1/kho")
    Call<ApiResponse<List<Kho>>> getKhos();

    @GET("api/v1/kho/{maKho}")
    Call<ApiResponse<Kho>> getKhoByMa(@Path("maKho") String maKho);

    @POST("api/v1/kho")
    Call<ApiResponse<Kho>> createKho(@Body Kho kho);

    @PUT("api/v1/kho/{maKho}")
    Call<ApiResponse<Kho>> updateKho(@Path("maKho") String maKho, @Body Kho kho);

    @DELETE("api/v1/kho/{maKho}")
    Call<ApiResponse<String>> deleteKho(@Path("maKho") String maKho);

    // Endpoints Nhân viên (QTV)
    @GET("api/v1/nhanvien")
    Call<ApiResponse<List<NhanVien>>> getNhanViens(@Query("search") String search);

    @GET("api/v1/nhanvien/{maNV}")
    Call<ApiResponse<NhanVien>> getNhanVienByMa(@Path("maNV") String maNV);

    @GET("api/v1/nhanvien/chucvu")
    Call<ApiResponse<List<ChucVu>>> getChucVus();

    @POST("api/v1/nhanvien")
    Call<ApiResponse<NhanVien>> createNhanVien(@Body NhanVien nv);

    @PUT("api/v1/nhanvien/{maNV}")
    Call<ApiResponse<NhanVien>> updateNhanVien(@Path("maNV") String maNV, @Body NhanVien nv);

    @DELETE("api/v1/nhanvien/{maNV}")
    Call<ApiResponse<String>> deleteNhanVien(@Path("maNV") String maNV);

    // Endpoints Thống kê Doanh thu (Kế toán)
    @GET("api/v1/ketoan/doanhthu/thongke")
    Call<ApiResponse<RevenueStats>> getRevenueStats();

    @GET("api/v1/ketoan/doanhthu/thang/{thang}")
    Call<ApiResponse<List<DonHang>>> getDonHangByThang(@Path("thang") int thang);

    // Endpoints Đại lý / Khách hàng
    @GET("api/v1/daily/{id}")
    Call<ApiResponse<DaiLy>> getDaiLyDetail(@Path("id") String id);

    @POST("api/v1/daily")
    Call<ApiResponse<DaiLy>> updateDaiLy(@Body DaiLy daiLy);

    @PUT("api/v1/donhang/{id}/giao-hang")
    Call<ApiResponse<DonHang>> confirmDelivery(@Path("id") String id);

    @PUT("api/v1/donhang/{id}/thanh-toan")
    Call<ApiResponse<DonHang>> confirmPayment(@Path("id") String id);
}
