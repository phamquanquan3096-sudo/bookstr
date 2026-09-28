import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import parse_xml, OxmlElement
from docx.oxml.ns import nsdecls, qn

def build_docx():
    doc = docx.Document()
    
    # Page setup - Margins
    for section in doc.sections:
        section.top_margin = Inches(1.0)
        section.bottom_margin = Inches(1.0)
        section.left_margin = Inches(1.25)
        section.right_margin = Inches(1.0)
        
    def set_cell_background(cell, fill_hex):
        tcPr = cell._element.get_or_add_tcPr()
        shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
        tcPr.append(shd)

    def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
        tcPr = cell._element.get_or_add_tcPr()
        tcMar = OxmlElement('w:tcMar')
        for m, val in [('top', top), ('bottom', bottom), ('left', left), ('right', right)]:
            node = OxmlElement(f'w:{m}')
            node.set(qn('w:w'), str(val))
            node.set(qn('w:type'), 'dxa')
            tcMar.append(node)
        tcPr.append(tcMar)

    def add_h1(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(6)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Times New Roman'
        run.font.size = Pt(16)
        run.bold = True
        run.font.color.rgb = RGBColor(0, 51, 102)
        return p

    def add_h2(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Times New Roman'
        run.font.size = Pt(13.5)
        run.bold = True
        run.font.color.rgb = RGBColor(0, 70, 130)
        return p

    def add_h3(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(8)
        p.paragraph_format.space_after = Pt(3)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.font.name = 'Times New Roman'
        run.font.size = Pt(12)
        run.bold = True
        run.font.color.rgb = RGBColor(51, 51, 51)
        return p

    def add_p(text, bold_prefix="", italic=False):
        p = doc.add_paragraph()
        p.paragraph_format.space_after = Pt(5)
        p.paragraph_format.line_spacing = 1.15
        if bold_prefix:
            r_pre = p.add_run(bold_prefix)
            r_pre.font.name = 'Times New Roman'
            r_pre.font.size = Pt(12)
            r_pre.bold = True
        run = p.add_run(text)
        run.font.name = 'Times New Roman'
        run.font.size = Pt(12)
        run.italic = italic
        return p

    def add_bullet(text, bold_prefix=""):
        p = doc.add_paragraph(style='List Bullet')
        p.paragraph_format.space_after = Pt(3)
        p.paragraph_format.line_spacing = 1.15
        if bold_prefix:
            r_pre = p.add_run(bold_prefix)
            r_pre.font.name = 'Times New Roman'
            r_pre.font.size = Pt(12)
            r_pre.bold = True
        run = p.add_run(text)
        run.font.name = 'Times New Roman'
        run.font.size = Pt(12)
        return p

    def add_tbl(headers, rows_data, col_widths=None):
        table = doc.add_table(rows=len(rows_data) + 1, cols=len(headers))
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        table.autofit = False

        hdr_cells = table.rows[0].cells
        for i, header_text in enumerate(headers):
            hdr_cells[i].text = header_text
            set_cell_background(hdr_cells[i], "003366")
            set_cell_margins(hdr_cells[i], top=120, bottom=120, left=150, right=150)
            p = hdr_cells[i].paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            for run in p.runs:
                run.font.name = 'Times New Roman'
                run.font.size = Pt(11)
                run.font.bold = True
                run.font.color.rgb = RGBColor(255, 255, 255)

        for r_idx, row_values in enumerate(rows_data):
            row_cells = table.rows[r_idx + 1].cells
            bg_color = "F2F5F9" if r_idx % 2 == 1 else "FFFFFF"
            for c_idx, val in enumerate(row_values):
                row_cells[c_idx].text = str(val)
                set_cell_background(row_cells[c_idx], bg_color)
                set_cell_margins(row_cells[c_idx], top=100, bottom=100, left=120, right=120)
                p = row_cells[c_idx].paragraphs[0]
                p.alignment = WD_ALIGN_PARAGRAPH.LEFT
                for run in p.runs:
                    run.font.name = 'Times New Roman'
                    run.font.size = Pt(10.5)

        if col_widths:
            for row in table.rows:
                for i, w in enumerate(col_widths):
                    row.cells[i].width = Inches(w)

        doc.add_paragraph().paragraph_format.space_after = Pt(4)
        return table

    # --- COVER PAGE ---
    p_header = doc.add_paragraph()
    p_header.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p_header.add_run("BỘ GIÁO DỤC VÀ ĐÀO TẠO\nHỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG\n-----------------------------------")
    r.font.name = 'Times New Roman'
    r.font.size = Pt(13)
    r.bold = True
    
    doc.add_paragraph().paragraph_format.space_after = Pt(24)
    
    p_title = doc.add_paragraph()
    p_title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_t1 = p_title.add_run("BÁO CÁO MÔN HỌC\nĐẢM BẢO CHẤT LƯỢNG PHẦN MỀM\n\n")
    r_t1.font.name = 'Times New Roman'
    r_t1.font.size = Pt(16)
    r_t1.bold = True
    
    r_t2 = p_title.add_run("ĐỀ TÀI: XÂY DỰNG VÀ ĐẢM BẢO CHẤT LƯỢNG HỆ THỐNG QUẢN LÝ ĐẠI LÝ (QUANLYDAILY)\n(SPRING BOOT RESTFUL BACKEND & ANDROID NATIVE CLIENT)")
    r_t2.font.name = 'Times New Roman'
    r_t2.font.size = Pt(18)
    r_t2.bold = True
    r_t2.font.color.rgb = RGBColor(0, 51, 102)

    doc.add_paragraph().paragraph_format.space_after = Pt(36)

    p_info = doc.add_paragraph()
    p_info.paragraph_format.left_indent = Inches(1.5)
    r_i = p_info.add_run(
        "Ngành: Công Nghệ Thông Tin\n"
        "Giảng viên hướng dẫn: ThS. Nguyễn Anh Hào\n"
        "Sinh viên thực hiện:\n"
        "   1. K23DTCN187 - Phạm Minh Quân (Leader, Backend Developer)\n"
        "   2. K23DTCN186 - Trần Khánh Quân (Android Client Developer)\n"
        "   3. K23DTCN106 - Ngô Trùng Dương (Tester & Quality Assurance)"
    )
    r_i.font.name = 'Times New Roman'
    r_i.font.size = Pt(12)

    doc.add_paragraph().paragraph_format.space_after = Pt(48)

    p_ft = doc.add_paragraph()
    p_ft.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r_f = p_ft.add_run("TP. Hồ Chí Minh, Năm 2026")
    r_f.font.name = 'Times New Roman'
    r_f.font.size = Pt(12)
    r_f.italic = True

    doc.add_page_break()

    # --- TABLE OF CONTENTS & WORK SPLIT ---
    add_h1("BẢNG PHÂN CHIA CÔNG VIỆC THÀNH VIÊN")
    add_tbl(
        ["Thành viên", "Mã sinh viên", "Vai trò", "Công việc thực hiện"],
        [
            ["Phạm Minh Quân", "K23DTCN187", "Trưởng nhóm / Backend Dev", "Xây dựng Backend Java Spring Boot, RESTful APIs, JPA H2 Database, DataInitializer seed data."],
            ["Trần Khánh Quân", "K23DTCN186", "Android App Dev", "Thiết kế UI Android (XML), Retrofit API Client, ViewBinding, Activity & Fragment logic."],
            ["Ngô Trùng Dương", "K23DTCN106", "Tester / QA", "Xây dựng kịch bản kiểm thử (Test Case), kiểm thử tích hợp API, kiểm thử UI trên Android Emulator."]
        ],
        [1.5, 1.2, 1.5, 2.5]
    )

    add_h1("CHƯƠNG 1: TỔNG QUAN ĐỀ TÀI & CÔNG NGHỆ SỬ DỤNG")
    add_h2("1.1. Giới thiệu đề tài")
    add_p("Hệ thống Quản lý Đại Lý (QuanLyDaiLy) được xây dựng nhằm hỗ trợ các doanh nghiệp cung cấp và phân phối hàng hóa (nước giải khát, thực phẩm, sữa...) quản lý mạng lưới đại lý phân phối, đơn đặt hàng, công nợ và giao hàng một cách tự động và tối ưu. Phần mềm giải quyết triệt để các hạn chế của phương pháp quản lý thủ công bằng sổ sách, nâng cao độ chính xác trong kiểm soát tồn kho và theo dõi doanh thu.")

    add_h2("1.2. Khảo sát thực tế & Yêu cầu đổi mới")
    add_bullet("Hạn chế phần mềm cũ/sổ sách: Lưu trữ cồng kềnh, dễ thất lạc, mất nhiều thời gian tra cứu công nợ đại lý, dễ tính toán sai lệch tổng tiền đơn hàng.")
    add_bullet("Yêu cầu tin học hóa: Tự động hóa tạo đơn hàng, cập nhật tồn kho tức thì, quản lý phân loại Đại lý (Cấp 1, Cấp 2, Đại lý mới), phân công giao hàng và lập phiếu công nợ chính xác.")

    add_h2("1.3. Kiến trúc Công nghệ của Phần mềm")
    add_bullet("Backend API Server: Java 17/21, Spring Boot 3.2.3, Spring Data JPA, Hibernate ORM, H2 Embedded Database / MS SQL Server, Maven/Gradle.")
    add_bullet("Mobile Client App: Android Native Java (SDK 34), Retrofit 2.9, Gson Converter, Material Design UI, ViewBinding, Glide image loader.")

    add_h1("CHƯƠNG 2: MỤC TIÊU, PHẠM VI VÀ QUẢN LÝ DỰ ÁN")
    add_h2("2.1 Mục tiêu dự án")
    add_bullet("Tin học hóa quy trình quản lý đại lý phân phối hàng hóa.")
    add_bullet("Lưu trữ dữ liệu tập trung, đảm bảo tính toàn vẹn và bảo mật.")
    add_bullet("Phân quyền rõ ràng cho các vai trò: NVKD (Kinh doanh), NVKT (Kế toán), NVK (Kho), NVGH (Giao hàng), Đại lý.")

    add_h2("2.2 Phạm vi dự án (WBS - Work Breakdown Structure)")
    add_tbl(
        ["Mã WBS", "Giai đoạn công việc", "Sản phẩm đầu ra", "Người phụ trách"],
        [
            ["W1", "Khảo sát & Phân tích yêu cầu", "Tài liệu SRS, Use-case Diagram", "Cả nhóm"],
            ["W2", "Thiết kế CSDL & API Contract", "Entity Models, Database Diagram", "Minh Quân"],
            ["W3", "Lập trình Backend Spring Boot", "RESTful API Services (Port 8080)", "Minh Quân"],
            ["W4", "Lập trình Client Android Native", "Màn hình Login, Main, Order, Stock", "Khánh Quân"],
            ["W5", "Kiểm thử & Đảm bảo chất lượng", "Bảng Test Case & Log lỗi", "Trùng Dương"],
            ["W6", "Nghiệm thu & Đóng dự án", "Báo cáo Word & File APK Debug", "Cả nhóm"]
        ],
        [1.0, 2.0, 2.2, 1.3]
    )

    add_h1("CHƯƠNG 3: PHÂN TÍCH VÀ THIẾT KẾ HỆ THỐNG")
    add_h2("3.1 Mô hình Cơ sở dữ liệu Chuẩn (Database Schema)")
    add_p("Cơ sở dữ liệu được chuẩn hóa bao gồm các thực thể cốt lõi:")
    add_bullet("nhan_vien (manv, tennv, ma_chuc_vu, sdt, email, user_name, password)")
    add_bullet("dai_ly (madl, tendl, ma_loaidl, sdt, dia_chi, email, user_name, password, tong_doanh_so, ngay_tao)")
    add_bullet("loaidl (ma_loaidl, ten_loaidl, so_no_toi_da)")
    add_bullet("san_pham (masp, tensp, don_vi_tinh, gia, tong_ton, ngaysx, hansd)")
    add_bullet("don_hang (madh, madl, magh, ngay_lap, trang_thai, tinh_trang_thanh_toan, diem_giao, tinh_tranggh, tong_tien)")
    add_bullet("chi_tiet_don_hang (mactdh, madh, masp, so_luong, don_gia, thanh_tien)")
    add_bullet("phieu_cong_no (maphieucn, madl, ngay_lap, so_tien_no, so_tien_thu, con_no, ghi_chu)")
    add_bullet("kho & chi_tiet_kho (ma_kho, ten_kho, dia_chi, so_luong_ton)")

    add_h1("CHƯƠNG 4: KIỂM THỬ PHẦN MỀM & ĐẢM BẢO CHẤT LƯỢNG (QA)")
    add_h2("4.1 Danh sách Kịch bản Kiểm thử (Test Cases)")
    add_tbl(
        ["Mã TC", "Tên ca kiểm thử", "Dữ liệu đầu vào", "Kết quả mong đợi", "Trạng thái"],
        [
            ["TC01", "Đăng nhập NVKD hợp lệ", "User: kinhdoanh, Pass: 123", "Trả về TOKEN_STAFF_NV01, vào MainActivity", "PASS"],
            ["TC02", "Đăng nhập sai Mật khẩu", "User: daily, Pass: sai_pass", "Báo lỗi 'Mật khẩu không chính xác'", "PASS"],
            ["TC03", "Tạo đơn hàng & Trừ tồn kho", "Đại lý DL01 chọn SP01 (số lượng 5)", "Tạo DH mới, kho SP01 giảm đúng 5 đơn vị", "PASS"],
            ["TC04", "Lập phiếu công nợ kế toán", "Đại lý DL01, Nợ: 10Tr, Thu: 4Tr", "Phát hành PCN, Còn nợ = 6Tr", "PASS"],
            ["TC05", "Phân công giao hàng", "Đơn DH01, Phân công NVGH NV05", "Đơn chuyển trạng thái 'Đang giao'", "PASS"]
        ],
        [0.8, 1.8, 1.8, 1.6, 0.7]
    )

    add_h1("CHƯƠNG 5: KẾT LUẬN VÀ ĐÁNH GIÁ DỰ ÁN")
    add_p("Hệ thống Quản lý Đại Lý (QuanLyDaiLy) đã được xây dựng và kiểm thử hoàn chỉnh. Cả Backend Spring Boot RESTful API và Android Native Mobile Client đều biên dịch và hoạt động mượt mà 100%, đáp ứng đầy đủ tiêu chí môn học Đảm bảo chất lượng phần mềm.")

    # Save to file
    out_path = "d:/didong/Bao_Cao_Quan_Ly_Dai_Ly_Moi.docx"
    doc.save(out_path)
    print(f"Report saved successfully to {out_path}")

if __name__ == "__main__":
    build_docx()
