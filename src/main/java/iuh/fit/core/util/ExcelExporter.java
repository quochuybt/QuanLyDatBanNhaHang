package iuh.fit.core.util;

import iuh.fit.core.dto.HoaDonDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExporter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public boolean exportHoaDonReport(List<HoaDonDTO> dsHoaDon, String filePath) {
        if (dsHoaDon == null || dsHoaDon.isEmpty()) {
            return false;
        }

        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }

        if (!filePath.toLowerCase().endsWith(".xlsx")) {
            filePath += ".xlsx";
        }

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(filePath)) {

            Sheet sheet = workbook.createSheet("Danh sách hóa đơn");

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle textStyle = createTextStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle dateStyle = createTextStyle(workbook);

            createTitle(sheet, titleStyle);
            createHeader(sheet, headerStyle);

            int rowIndex = 3;
            int stt = 1;

            double tongDoanhThu = 0;

            for (HoaDonDTO hd : dsHoaDon) {
                Row row = sheet.createRow(rowIndex++);

                createCell(row, 0, stt++, textStyle);
                createCell(row, 1, safeString(hd.getMaHD()), textStyle);
                createCell(row, 2, formatDateTime(hd.getNgayLap()), dateStyle);
                createCell(row, 3, safeString(hd.getMaDon()), textStyle);
                createCell(row, 4, safeString(hd.getMaNV()), textStyle);
                createCell(row, 5, safeString(hd.getHinhThucThanhToan()), textStyle);
                createCell(row, 6, hd.getGiamGia(), numberStyle);
                createCell(row, 7, hd.getTongThanhToan(), numberStyle);

                tongDoanhThu += hd.getTongThanhToan();
            }

            createSummary(sheet, rowIndex + 1, tongDoanhThu, numberStyle, headerStyle);

            autoSizeColumns(sheet, 8);

            workbook.write(fos);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void createTitle(Sheet sheet, CellStyle titleStyle) {
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(28);

        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO DANH SÁCH HÓA ĐƠN");
        titleCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        Row timeRow = sheet.createRow(1);
        Cell timeCell = timeRow.createCell(0);
        timeCell.setCellValue("Ngày xuất: " + LocalDateTime.now().format(DATE_TIME_FORMATTER));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));
    }

    private void createHeader(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(2);

        String[] headers = {
                "STT",
                "Mã hóa đơn",
                "Ngày lập",
                "Mã đơn",
                "Mã nhân viên",
                "Hình thức thanh toán",
                "Giảm giá",
                "Tổng thanh toán"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void createSummary(
            Sheet sheet,
            int rowIndex,
            double tongDoanhThu,
            CellStyle numberStyle,
            CellStyle headerStyle
    ) {
        Row summaryRow = sheet.createRow(rowIndex);

        Cell labelCell = summaryRow.createCell(6);
        labelCell.setCellValue("Tổng doanh thu:");
        labelCell.setCellStyle(headerStyle);

        Cell valueCell = summaryRow.createCell(7);
        valueCell.setCellValue(tongDoanhThu);
        valueCell.setCellStyle(numberStyle);
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        setBorder(style);
        return style;
    }

    private CellStyle createTextStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.LEFT);
        setBorder(style);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0 \"₫\""));

        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setAlignment(HorizontalAlignment.RIGHT);

        setBorder(style);
        return style;
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void createCell(Row row, int columnIndex, String value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int columnIndex, int value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int columnIndex, double value, CellStyle style) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private String safeString(String value) {
        return value == null || value.trim().isEmpty() ? "N/A" : value;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "N/A" : dateTime.format(DATE_TIME_FORMATTER);
    }

    private void autoSizeColumns(Sheet sheet, int totalColumns) {
        for (int i = 0; i < totalColumns; i++) {
            sheet.autoSizeColumn(i);

            int currentWidth = sheet.getColumnWidth(i);
            int minWidth = 3000;
            int maxWidth = 9000;

            if (currentWidth < minWidth) {
                sheet.setColumnWidth(i, minWidth);
            } else if (currentWidth > maxWidth) {
                sheet.setColumnWidth(i, maxWidth);
            }
        }
    }
}