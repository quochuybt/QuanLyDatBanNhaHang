package util; // Đặt lớp này trong một package tiện ích

import entity.HoaDon;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class ExcelExporter {

    private final String[] HEADERS = {"Mã HD", "Ngày Lập", "Tổng Tiền", "Trạng Thái", "Thanh Toán", "Tiền Khách Đưa", "Tiền Thối"};
    private final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /**
     * Xuất danh sách hóa đơn ra file Excel.
     * @param listHoaDon Danh sách hóa đơn cần xuất.
     * @param filePath Đường dẫn file (ví dụ: "hoa_don.xlsx").
     * @return true nếu xuất thành công, false nếu xảy ra lỗi.
     */
    public boolean exportToExcel(List<HoaDon> listHoaDon, String filePath) {
        // 1. Tạo Workbook (file Excel)
        Workbook workbook = new XSSFWorkbook();

        try {
            // 2. Tạo Sheet
            Sheet sheet = workbook.createSheet("DanhSachHoaDon");

            // Tạo Style cho Header
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 3. Tạo Hàng Header
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 4. Ghi dữ liệu từ danh sách HoaDon
            int rowNum = 1;
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            for (HoaDon hd : listHoaDon) {
                Row row = sheet.createRow(rowNum++);

                // Cột 1: Mã HD
                row.createCell(0).setCellValue(hd.getMaHD());

                // Cột 2: Ngày Lập
                row.createCell(1).setCellValue(hd.getNgayLap().format(DATE_FORMAT));

                // Cột 3: Tổng Tiền
                Cell tongTienCell = row.createCell(2);
                tongTienCell.setCellValue(hd.getTongTien());
                tongTienCell.setCellStyle(currencyStyle);

                // Cột 4: Trạng Thái
                row.createCell(3).setCellValue(hd.getTrangThai());

                // Cột 5: Hình Thức Thanh Toán
                row.createCell(4).setCellValue(hd.getHinhThucThanhToan());

                // Cột 6: Tiền Khách Đưa
                Cell tienDuaCell = row.createCell(5);
                tienDuaCell.setCellValue(hd.getTienKhachDua());
                tienDuaCell.setCellStyle(currencyStyle);

                // Cột 7: Tiền Thối
                row.createCell(6).setCellValue(hd.getTienThoi());
            }

            // Tự động điều chỉnh độ rộng cột
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 5. Ghi Workbook ra File
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Phương thức tạo Style cho Header
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    // Phương thức tạo Style cho tiền tệ (giả định định dạng số)
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        // Định dạng tiền tệ: ví dụ #,##0 ₫
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }
}