USE QuanLyDLpro;
GO

-- 1. Fix sp_SumCN: change MaCongNo to MaDL and check 'Chưa thanh toán'
IF OBJECT_ID('dbo.sp_SumCN', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_SumCN;
GO
CREATE PROCEDURE dbo.sp_SumCN
    @madl VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @TongCongNo FLOAT;
    
    SELECT @TongCongNo = ISNULL(SUM(TienNo), 0)
    FROM dbo.PhieuCongNo
    WHERE MaDL = @madl AND TrangThai = N'Chưa thanh toán';
    
    SELECT @TongCongNo AS TongCongNo;
END;
GO

-- 2. Fix sp_TienDHNoDaiLy: handle 'All' and fix month comparison to match C# logic
IF OBJECT_ID('dbo.sp_TienDHNoDaiLy', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_TienDHNoDaiLy;
GO
CREATE PROCEDURE dbo.sp_TienDHNoDaiLy
    @madl VARCHAR(10),
    @thang VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @TongTienNo FLOAT;
    
    SELECT @TongTienNo = ISNULL(SUM(dh.TongTien), 0)
    FROM dbo.DonHang dh
    WHERE dh.MaDL = @madl 
      AND dh.TinhTrangThanhToan = N'Chưa thanh toán'
      AND dh.TrangThai = N'Đã xét duyệt'
      AND dh.TinhTrangGH = N'Đã giao'
      AND (@thang = 'All' OR MONTH(dh.NgayLap) = TRY_CAST(@thang AS INT));
    
    SELECT @TongTienNo AS TongTienDonHangNo;
END;
GO

-- 3. Fix sp_SoDHDaiLy: only count orders that are delivered, approved, and unpaid
IF OBJECT_ID('dbo.sp_SoDHDaiLy', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_SoDHDaiLy;
GO
CREATE PROCEDURE dbo.sp_SoDHDaiLy
    @madl VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @SoDH INT;
    
    SELECT @SoDH = COUNT(*)
    FROM dbo.DonHang
    WHERE MaDL = @madl
      AND TinhTrangThanhToan = N'Chưa thanh toán'
      AND TrangThai = N'Đã xét duyệt'
      AND TinhTrangGH = N'Đã giao';
    
    SELECT @SoDH AS SoDonHang;
END;
GO
