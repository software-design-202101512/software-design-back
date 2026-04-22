package com.edumanager.user;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ExcelParsingService {

    /**
     * 교사 엑셀 파싱
     * 엑셀 컬럼 순서: 이메일, 이름, 과목ID, 담임여부(TRUE/FALSE), 담임학년, 담임반
     */
    public List<TeacherRegistrationRequest> parseTeacherExcel(MultipartFile file) throws IOException {
        List<TeacherRegistrationRequest> requests = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                requests.add(TeacherRegistrationRequest.builder()
                        .email(getCellStringValue(row.getCell(0)))
                        .name(getCellStringValue(row.getCell(1)))
                        .subjectId(getCellLongValue(row.getCell(2)))
                        .isHomeroom(getCellBooleanValue(row.getCell(3)))
                        .homeroomGrade(getCellStringValue(row.getCell(4)))
                        .homeroomClassNum(getCellStringValue(row.getCell(5)))
                        .build());
            }
        }

        return requests;
    }

    /**
     * 학생 엑셀 파싱
     * 엑셀 컬럼 순서: 이메일, 이름, 학년, 반, 번호, 성별
     */
    public List<StudentRegistrationRequest> parseStudentExcel(MultipartFile file) throws IOException {
        List<StudentRegistrationRequest> requests = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                requests.add(StudentRegistrationRequest.builder()
                        .email(getCellStringValue(row.getCell(0)))
                        .name(getCellStringValue(row.getCell(1)))
                        .grade(getCellStringValue(row.getCell(2)))
                        .classNum(getCellStringValue(row.getCell(3)))
                        .studentNum(getCellStringValue(row.getCell(4)))
                        .gender(getCellStringValue(row.getCell(5)))
                        .build());
            }
        }

        return requests;
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Long getCellLongValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING -> {
                try {
                    yield Long.parseLong(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    private Boolean getCellBooleanValue(Cell cell) {
        if (cell == null) return false;
        return switch (cell.getCellType()) {
            case BOOLEAN -> cell.getBooleanCellValue();
            case STRING -> Boolean.parseBoolean(cell.getStringCellValue().trim());
            case NUMERIC -> cell.getNumericCellValue() != 0;
            default -> false;
        };
    }
}
