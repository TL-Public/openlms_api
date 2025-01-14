package com.tl.reap_admin_api.service;


import com.tl.reap_admin_api.dao.HistoricDataMonthWiseDao;
import com.tl.reap_admin_api.dto.HistoricDataMonthWiseDto;
import com.tl.reap_admin_api.mapper.HistoricDataMonthWiseMapper;
import com.tl.reap_admin_api.model.HistoricDataMonthWise;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class HistoricMonthWiseDataService {

    @Autowired
    private HistoricDataMonthWiseDao historicDataDao;

    @Autowired
    private HistoricDataMonthWiseMapper historicDataMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getTraineeCountByCategoryAndMonth() {
        String sql = """
           SELECT
                cat.id AS category_id, 
                cat.name AS category_name, 
                hd.mnth, 
                sum(hd.trainee_cnt) AS total_trainees
            FROM 
                public.historicdata hd  
            JOIN 
                public.courses c ON hd.course_code = c.course_code
            LEFT JOIN 
                public.categories cat ON c.category_id = cat.id
            GROUP BY 
                cat.id, cat.name, hd.mnth
			ORDER BY  hd.mnth, cat.id
        """;

        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getTraineeCountByCourseAndMonth() {
        String sql = """
            SELECT 
                c.course_code,
                cat.name AS category_name,
                hd.mnth,
                SUM(hd.trainee_cnt) AS total_trainees
            FROM 
                public.historicdata hd
            JOIN 
                public.courses c ON hd.course_code = c.course_code
            LEFT JOIN 
                public.categories cat ON c.category_id = cat.id
            GROUP BY 
                c.id, c.course_code, c.duration, cat.name, hd.mnth
            ORDER BY 
                hd.mnth, c.course_code
        """;

        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getTotalTraineeCountByMonthForAllCourses() {
        String sql = """
           select mnth, SUM(hd.trainee_cnt) from historicdata hd group by mnth order by mnth
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getTotalTraineeCountByMonthForStates() {
        String sql = """
           select state_id, mnth, SUM(hd.trainee_cnt) from historicdata hd group by mnth,state_id order by mnth,state_id
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getTotalCourseCountForStates() {
        String sql = """
           select state_id, SUM(hd.crs_cnt) as cnt from historicdata hd group by state_id order by cnt DESC
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @Transactional
    public void processExcelFile(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<HistoricDataMonthWiseDto> dtoList = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getRowNum() < 3) continue; // Skip header rows

                HistoricDataMonthWiseDto dto = processRow(row);
                if (dto != null) {
                    dtoList.add(dto);
                }
            }

            List<HistoricDataMonthWise> entities = new ArrayList<>();
            for (HistoricDataMonthWiseDto dto : dtoList) {
                entities.addAll(historicDataMapper.toEntities(dto));
            }

            historicDataDao.deleteAll(); // Clear existing data
            historicDataDao.batchInsert(entities);
        }
    }

    private HistoricDataMonthWiseDto processRow(Row row) {
        HistoricDataMonthWiseDto dto = new HistoricDataMonthWiseDto();
        dto.setStateName(getCellValueAsString(row.getCell(1)));
        dto.setStateId(getCellValueAsLong(row.getCell(2)));
        dto.setEpds(getCellValueAsString(row.getCell(3)));
        dto.setCategoryId(getCellValueAsLong(row.getCell(4)));
        dto.setCourseCode(getCellValueAsString(row.getCell(5)));
        dto.setCourseName(getCellValueAsString(row.getCell(6)));

        // Process monthly data
        HistoricDataMonthWiseDto.MonthlyData[] monthlyData = new HistoricDataMonthWiseDto.MonthlyData[12];
        for (int i = 0; i < 12; i++) {
            int cellIndex = 7 + i * 2;
            monthlyData[i] = new HistoricDataMonthWiseDto.MonthlyData(
                getCellValueAsLong(row.getCell(cellIndex)),
                getCellValueAsLong(row.getCell(cellIndex + 1))
            );
        }
        dto.setMonthlyData(monthlyData);

        return dto;
    }


    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            default:
                return "";
        }
    }

    private Long getCellValueAsLong(Cell cell) {
        if (cell == null) return 0L;
        switch (cell.getCellType()) {
            case NUMERIC:
                return (long) cell.getNumericCellValue();
            case STRING:
                try {
                    return Long.parseLong(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0L;
                }
            default:
                return 0L;
        }
    }
}
