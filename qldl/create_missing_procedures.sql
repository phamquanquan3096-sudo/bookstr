USE QuanLyDLpro;
GO

-- 1. Create sp_LocPhieuCongNo if it does not exist
IF OBJECT_ID('dbo.sp_LocPhieuCongNo', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_LocPhieuCongNo;
GO
CREATE PROCEDURE dbo.sp_LocPhieuCongNo
    @thang VARCHAR(50) = NULL,
    @thanhtoan NVARCHAR(50) = NULL,
    @madl VARCHAR(50) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT MaCongNo, MaDL, TienNo, HanTra, TrangThai, NgayLapCN
    FROM dbo.PhieuCongNo
    WHERE (@madl IS NULL OR @madl = 'ALL' OR MaDL = @madl)
      AND (@thang IS NULL OR @thang = 'ALL' OR MONTH(NgayLapCN) = TRY_CAST(@thang AS INT))
      AND (@thanhtoan IS NULL OR @thanhtoan = 'ALL' OR TrangThai = @thanhtoan)
    ORDER BY NgayLapCN DESC;
END;
GO

-- 2. Create sp_CongnoDLy if it does not exist
IF OBJECT_ID('dbo.sp_CongnoDLy', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_CongnoDLy;
GO
CREATE PROCEDURE dbo.sp_CongnoDLy
    @madl VARCHAR(50),
    @tienno FLOAT,
    @hantra DATETIME,
    @ngaylap DATETIME,
    @thang VARCHAR(50) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @NextId INT;
    DECLARE @NewMaCN VARCHAR(20);
    
    SELECT @NextId = ISNULL(MAX(TRY_CAST(SUBSTRING(MaCongNo, 3, 10) AS INT)), 0) + 1 FROM dbo.PhieuCongNo;
    SET @NewMaCN = 'CN' + RIGHT('0000' + CAST(@NextId AS VARCHAR(10)), 4);
    
    INSERT INTO dbo.PhieuCongNo (MaCongNo, MaDL, TienNo, HanTra, TrangThai, NgayLapCN)
    VALUES (@NewMaCN, @madl, @tienno, @hantra, N'Chưa thanh toán', @ngaylap);
END;
GO

-- 3. Create sp_TaoCongNo if it does not exist
IF OBJECT_ID('dbo.sp_TaoCongNo', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_TaoCongNo;
GO
CREATE PROCEDURE dbo.sp_TaoCongNo
    @madl VARCHAR(50),
    @tienno FLOAT,
    @hantra DATETIME,
    @ngaylap DATETIME,
    @thang VARCHAR(50) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @NextId INT;
    DECLARE @NewMaCN VARCHAR(20);
    
    SELECT @NextId = ISNULL(MAX(TRY_CAST(SUBSTRING(MaCongNo, 3, 10) AS INT)), 0) + 1 FROM dbo.PhieuCongNo;
    SET @NewMaCN = 'CN' + RIGHT('0000' + CAST(@NextId AS VARCHAR(10)), 4);
    
    INSERT INTO dbo.PhieuCongNo (MaCongNo, MaDL, TienNo, HanTra, TrangThai, NgayLapCN)
    VALUES (@NewMaCN, @madl, @tienno, @hantra, N'Chưa thanh toán', @ngaylap);
    
    SELECT MaCongNo, MaDL, TienNo, HanTra, TrangThai, NgayLapCN
    FROM dbo.PhieuCongNo
    WHERE MaCongNo = @NewMaCN;
END;
GO

-- 4. Create delete_kho if it does not exist
IF OBJECT_ID('dbo.delete_kho', 'P') IS NOT NULL
    DROP PROCEDURE dbo.delete_kho;
GO
CREATE PROCEDURE dbo.delete_kho
AS
BEGIN
    SET NOCOUNT ON;
    SELECT 1 AS Result;
END;
GO
