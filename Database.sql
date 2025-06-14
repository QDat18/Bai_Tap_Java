USE [master]
GO
/****** Object:  Database [CAPHE]    Script Date: 20/05/2025 12:12:11 SA ******/
CREATE DATABASE [CAPHE]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'CAPHE', FILENAME = N'D:\SQL\MSSQL16.MSSQLSERVER\MSSQL\DATA\CAPHE.mdf' , SIZE = 8192KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON 
( NAME = N'CAPHE_log', FILENAME = N'D:\SQL\MSSQL16.MSSQLSERVER\MSSQL\DATA\CAPHE_log.ldf' , SIZE = 8192KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
 WITH CATALOG_COLLATION = DATABASE_DEFAULT, LEDGER = OFF
GO
ALTER DATABASE [CAPHE] SET COMPATIBILITY_LEVEL = 160
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [CAPHE].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [CAPHE] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [CAPHE] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [CAPHE] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [CAPHE] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [CAPHE] SET ARITHABORT OFF 
GO
ALTER DATABASE [CAPHE] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [CAPHE] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [CAPHE] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [CAPHE] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [CAPHE] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [CAPHE] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [CAPHE] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [CAPHE] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [CAPHE] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [CAPHE] SET  ENABLE_BROKER 
GO
ALTER DATABASE [CAPHE] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [CAPHE] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [CAPHE] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [CAPHE] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [CAPHE] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [CAPHE] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [CAPHE] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [CAPHE] SET RECOVERY FULL 
GO
ALTER DATABASE [CAPHE] SET  MULTI_USER 
GO
ALTER DATABASE [CAPHE] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [CAPHE] SET DB_CHAINING OFF 
GO
ALTER DATABASE [CAPHE] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [CAPHE] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [CAPHE] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [CAPHE] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
EXEC sys.sp_db_vardecimal_storage_format N'CAPHE', N'ON'
GO
ALTER DATABASE [CAPHE] SET QUERY_STORE = ON
GO
ALTER DATABASE [CAPHE] SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO
USE [CAPHE]
GO
/****** Object:  User [user]    Script Date: 20/05/2025 12:12:11 SA ******/
CREATE USER [user] FOR LOGIN [user] WITH DEFAULT_SCHEMA=[dbo]
GO
/****** Object:  Table [dbo].[CTHoaDonBan]    Script Date: 20/05/2025 12:12:11 SA ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[CTHoaDonBan](
	[MaHDB] [varchar](20) NOT NULL,
	[MaSP] [varchar](20) NOT NULL,
	[Soluong] [int] NULL,
	[DonGia] [int] NULL,
	[Khuyenmai] [int] NULL,
	[Thanhtien] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[MaHDB] ASC,
	[MaSP] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[CTHoaDonNhap]    Script Date: 20/05/2025 12:12:12 SA ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[CTHoaDonNhap](
	[MaHDN] [varchar](20) NOT NULL,
	[MaSP] [varchar](20) NOT NULL,
	[Soluong] [int] NOT NULL,
	[Dongia] [int] NOT NULL,
	[Khuyenmai] [int] NULL,
	[Thanhtien] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[MaHDN] ASC,
	[MaSP] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[HoaDonBan]    Script Date: 20/05/2025 12:12:12 SA ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[HoaDonBan](
	[MaHDB] [varchar](20) NOT NULL,
	[MaNV] [varchar](20) NOT NULL,
	[MaKH] [varchar](20) NOT NULL,
	[Ngayban] [date] NOT NULL,
	[Tongtien] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[MaHDB] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[HoaDonNhap]    Script Date: 20/05/2025 12:12:12 SA ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[HoaDonNhap](
	[MaHDN] [varchar](20) NOT NULL,
	[MaNV] [varchar](20) NOT NULL,
	[MaNCC] [varchar](20) NOT NULL,
	[Ngaynhap] [date] NOT NULL,
	[Tongtien] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[MaHDN] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[KhachHang]    Script Date: 20/05/2025 12:12:12 SA ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[KhachHang](
	[MaKH] [varchar](20) NOT NULL,
	[Tenkhach] [nvarchar](100) NULL,
	[SDT] [varchar](15) NULL,
PRIMARY KEY CLUSTERED 
(
	[MaKH] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Loai]    Script Date: 20/05/2025 12:12:12 SA ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Loai](
	[Maloai] [varchar](20) NOT NULL,
	[Tenloai] [nvarchar](100) NULL,
PRIMARY KEY CLUSTERED 
(
	[Maloai] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[NhaCC]    Script Date: 20/05/2025 12:12:12 SA ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[NhaCC](
	[MaNCC] [varchar](20) NOT NULL,
	[TenNCC] [nvarchar](100) NULL,
	[Diachi] [nvarchar](200) NULL,
	[SDT] [varchar](15) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[MaNCC] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[NhanVien]    Script Date: 20/05/2025 12:12:12 SA ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[NhanVien](
	[MaNV] [varchar](20) NOT NULL,
	[TenNV] [nvarchar](100) NULL,
	[Diachi] [nvarchar](200) NULL,
	[Gioitinh] [nvarchar](10) NULL,
	[SDT] [varchar](15) NOT NULL,
	[Tendangnhap] [varchar](50) NULL,
	[Matkhau] [varchar](255) NULL,
	[Email] [varchar](100) NULL,
	[role] [varchar](50) NULL,
PRIMARY KEY CLUSTERED 
(
	[MaNV] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[SanPham]    Script Date: 20/05/2025 12:12:12 SA ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[SanPham](
	[MaSP] [varchar](20) NOT NULL,
	[TenSP] [nvarchar](100) NULL,
	[Maloai] [varchar](20) NULL,
	[Gianhap] [int] NULL,
	[Giaban] [int] NULL,
	[Soluong] [int] NULL,
	[Anh] [varchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[MaSP] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ_NhanVien_Tendangnhap]    Script Date: 20/05/2025 12:12:12 SA ******/
CREATE UNIQUE NONCLUSTERED INDEX [UQ_NhanVien_Tendangnhap] ON [dbo].[NhanVien]
(
	[Tendangnhap] ASC
)
WHERE ([Tendangnhap] IS NOT NULL)
WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[CTHoaDonBan]  WITH CHECK ADD FOREIGN KEY([MaHDB])
REFERENCES [dbo].[HoaDonBan] ([MaHDB])
GO
ALTER TABLE [dbo].[CTHoaDonBan]  WITH CHECK ADD FOREIGN KEY([MaSP])
REFERENCES [dbo].[SanPham] ([MaSP])
GO
ALTER TABLE [dbo].[CTHoaDonNhap]  WITH CHECK ADD FOREIGN KEY([MaHDN])
REFERENCES [dbo].[HoaDonNhap] ([MaHDN])
GO
ALTER TABLE [dbo].[CTHoaDonNhap]  WITH CHECK ADD FOREIGN KEY([MaSP])
REFERENCES [dbo].[SanPham] ([MaSP])
GO
ALTER TABLE [dbo].[HoaDonBan]  WITH CHECK ADD FOREIGN KEY([MaKH])
REFERENCES [dbo].[KhachHang] ([MaKH])
GO
ALTER TABLE [dbo].[HoaDonBan]  WITH CHECK ADD FOREIGN KEY([MaNV])
REFERENCES [dbo].[NhanVien] ([MaNV])
GO
ALTER TABLE [dbo].[HoaDonNhap]  WITH CHECK ADD FOREIGN KEY([MaNCC])
REFERENCES [dbo].[NhaCC] ([MaNCC])
GO
ALTER TABLE [dbo].[HoaDonNhap]  WITH CHECK ADD FOREIGN KEY([MaNV])
REFERENCES [dbo].[NhanVien] ([MaNV])
GO
ALTER TABLE [dbo].[SanPham]  WITH CHECK ADD FOREIGN KEY([Maloai])
REFERENCES [dbo].[Loai] ([Maloai])
GO
USE [master]
GO
ALTER DATABASE [CAPHE] SET  READ_WRITE 
GO
