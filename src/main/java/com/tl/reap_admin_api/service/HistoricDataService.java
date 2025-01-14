package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dao.HistoricDataYearWiseDao;
import com.tl.reap_admin_api.dao.RsetiDao;
import com.tl.reap_admin_api.dao.StateDao;
import com.tl.reap_admin_api.dto.HistDataYrWsExcelDataRowDto;
import com.tl.reap_admin_api.dto.YWHistTotalTraineesDto;
import com.tl.reap_admin_api.model.HistoricDataYearWise;
import com.tl.reap_admin_api.model.RSETI;
import com.tl.reap_admin_api.model.RsetiTranslation;
import com.tl.reap_admin_api.model.State;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HistoricDataService {

    @Autowired
    private HistoricDataYearWiseDao historicDataDao;

    @Autowired
    private StateDao stateDao;

    @Autowired
    private RsetiDao rsetiDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final List<String> FISCAL_YEARS = Arrays.asList(
        "2014", "2015", "2016", "2017",
        "2018", "2019", "2020", "2021","2022", "2023","2024"
    );

    private static final String DEFAULT_LANGUAGE_CODE = "en";
    private static final Long DEFAULT_LANGUAGE_ID = 1L;

    @Transactional(readOnly = true)
    public List<YWHistTotalTraineesDto> getGrandTotal() {
        List<Object[]> results = historicDataDao.findTotalsByYear();
        return results.stream()
        .map(result -> new YWHistTotalTraineesDto(
            String.valueOf(result[0]),
            String.valueOf(result[0]),
            ((Number) result[1]).longValue(),
            ((Number) result[2]).longValue()))
        .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<YWHistTotalTraineesDto> getTotalsByState() {
        List<Object[]> results = historicDataDao.findTotalsByState();
        return results.stream()
            .map(result -> new YWHistTotalTraineesDto(
                String.valueOf(result[0]),
                String.valueOf(result[1]),
                ((Number) result[2]).longValue(),
                ((Number) result[3]).longValue()))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<YWHistTotalTraineesDto> getTotalsByRseti() {
        List<Object[]> results = historicDataDao.findTotalsByRseti();
        return results.stream()
            .map(result -> new YWHistTotalTraineesDto(
                String.valueOf(result[0]),
                String.valueOf(result[1]),
                ((Number) result[2]).longValue(),
                ((Number) result[3]).longValue()))
            .collect(Collectors.toList());
    }

    @Transactional
    public void processExcelFile(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, HistDataYrWsExcelDataRowDto> dataRows = new HashMap<>();

            // Skip header row
            Iterator<Row> rowIterator = sheet.iterator();
            Row headerRow = rowIterator.next();

            while (rowIterator.hasNext()) {

                Row row = rowIterator.next();
                if (row.getRowNum() < 3) continue; // Skip header rows

                HistDataYrWsExcelDataRowDto rowDto = processRow(row);
                if (rowDto != null) {
                    dataRows.put(rowDto.getRsetiName(), rowDto);
                }
            }
            if (!dataRows.isEmpty()) {
                dataRows.remove(dataRows.keySet().toArray()[dataRows.size() - 1]);
            }
            //saveHistoricData(dataRows);
            saveHistoricDataBatch(dataRows);
        }
    }

    private HistDataYrWsExcelDataRowDto processRow(Row row) {
        HistDataYrWsExcelDataRowDto dto = new HistDataYrWsExcelDataRowDto();
        
       // System.out.println("Row number -"+ row.getRowNum() +" rseti value - " + getCellValueAsString(row.getCell(3)) );
        dto.setState(getCellValueAsString(row.getCell(1))); // State column
        dto.setDistrict(getCellValueAsString(row.getCell(2))); // District column
        dto.setRsetiName(getCellValueAsString(row.getCell(3))); // RSETI column

        // Process yearly data
        int columnIndex = 4; // Starting column for FY data
        for (String fiscalYear : FISCAL_YEARS) {
            Integer trained = getCellValueAsInteger(row.getCell(columnIndex));
            Integer settled = getCellValueAsInteger(row.getCell(columnIndex + 1));
            dto.addYearlyData(fiscalYear, trained, settled);
            columnIndex += 2;
        }

        return dto;
    }

    @Transactional
    public void saveHistoricData(Map<String, HistDataYrWsExcelDataRowDto> dataRows) {
        Map<String, State> stateMap = stateDao.findAllByLanguageCode(DEFAULT_LANGUAGE_CODE).stream()
            .collect(Collectors.toMap(State::getName, state -> state));

        stateMap.forEach((key, value) -> System.out.println("State Name: " + key + ", Address: " + value.getExtId()));
        // Prefetch all RSETIs and create a map
        Map<String, RSETI> rsetiMap = new HashMap<>();
      
        List<RsetiTranslation> translations = rsetiDao.findAllTranslationsByLanguageId(DEFAULT_LANGUAGE_ID);
        rsetiMap = translations.stream()
            .collect(Collectors.toMap(RsetiTranslation::getName, RsetiTranslation::getRseti));
        // Print all values in rsetiMap the key and address
     

        int slno = 0;
        for (HistDataYrWsExcelDataRowDto rowDto :  dataRows.values()) {
            // Get state from the map
            try {
                slno++;
                State state = stateMap.get(rowDto.getState());
                if (state == null) {
                    System.err.println("**Error processing row : " +slno+" state --" + rowDto.getState()+"**");
                    throw new RuntimeException("State not found: " + rowDto.getState());
                }

                // Get RSETI from the map
                RSETI rseti = rsetiMap.get(rowDto.getRsetiName());
                if (rseti == null) {
                  //  System.err.println("**Error processing row : " +slno+" RSETI --" + rowDto.getRsetiName() +"**");
                    throw new RuntimeException("RSETI not found: --" + rowDto.getRsetiName() +"--");
                }

                // Save historic data for each year
                for (Map.Entry<String, HistDataYrWsExcelDataRowDto.YearlyData> entry : rowDto.getYearlyData().entrySet()) {
                    HistoricDataYearWise historicData = new HistoricDataYearWise();
                    historicData.setRsetiUuid(rseti.getUuid().toString());
                    historicData.setStateId(state.getExtId());
                    historicData.setFiscalYear(entry.getKey());
                    historicData.setTrainedCount(entry.getValue().getTrained());
                    historicData.setSettledCount(entry.getValue().getSettled());
                    historicDataDao.save(historicData);
                }
            } catch (Exception e) {           
               System.out.println(e.getMessage());
                break;     
                // Continue processing with the next row
            }
        }
    }

    @Transactional
    public void saveHistoricDataBatch(Map<String, HistDataYrWsExcelDataRowDto> dataRows) {
        Map<String, State> stateMap = stateDao.findAllByLanguageCode(DEFAULT_LANGUAGE_CODE).stream()
            .collect(Collectors.toMap(State::getName, state -> state));

        Map<String, RSETI> rsetiMap = rsetiDao.findAllTranslationsByLanguageId(DEFAULT_LANGUAGE_ID).stream()
            .collect(Collectors.toMap(RsetiTranslation::getName, RsetiTranslation::getRseti));

        List<Object[]> batchArgs = new ArrayList<>();

        for (HistDataYrWsExcelDataRowDto rowDto : dataRows.values()) {
            try {
                State state = stateMap.get(rowDto.getState());
                if (state == null) {
                    throw new RuntimeException("State not found: " + rowDto.getState());
                }

                RSETI rseti = rsetiMap.get(rowDto.getRsetiName());
                if (rseti == null) {
                    throw new RuntimeException("RSETI not found: " + rowDto.getRsetiName());
                }

                for (Map.Entry<String, HistDataYrWsExcelDataRowDto.YearlyData> entry : rowDto.getYearlyData().entrySet()) {
                    batchArgs.add(new Object[]{
                        rseti.getUuid().toString(),
                        state.getExtId(),
                        entry.getKey(),
                        entry.getValue().getTrained(),
                        entry.getValue().getSettled()
                    });
                }
            } catch (Exception e) {
                System.out.println("Error processing row: " + e.getMessage());
                // Continue processing with the next row
            }
        }

        String sql = "INSERT INTO historic_data_yearwise (rseti_uuid, state_id, fiscal_year, trained_count, settled_count) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            default:
                return null;
        }
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) return 0;
        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0;
                }
            default:
                return 0;
        }
    }
}