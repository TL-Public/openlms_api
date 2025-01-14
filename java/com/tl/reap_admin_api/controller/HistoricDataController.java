package com.tl.reap_admin_api.controller;

import com.tl.reap_admin_api.dto.YWHistTotalTraineesDto;
import com.tl.reap_admin_api.service.HistoricDataService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/apis/v1/historic-data")
public class HistoricDataController {

    @Autowired
    private HistoricDataService historicDataService;

    @PostMapping("/bulk-upload")
    public ResponseEntity<?> uploadExcelFile(@RequestParam("file") MultipartFile file) {
        try {
            if (!file.getOriginalFilename().endsWith(".xlsx")) {
                return ResponseEntity.badRequest().body("Please upload an Excel file (.xlsx)");
            }
            historicDataService.processExcelFile(file);
            return ResponseEntity.ok("Data processed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error processing file: " + e.getMessage());
        }
    }

    @GetMapping("/grand-total")
    public ResponseEntity<List<YWHistTotalTraineesDto>> getGrandTotal() {
        return ResponseEntity.ok(historicDataService.getGrandTotal());
    }

    @GetMapping("/totals-by-state")
    public ResponseEntity<List<YWHistTotalTraineesDto>> getTotalsByState() {
        return ResponseEntity.ok(historicDataService.getTotalsByState());
    }

    @GetMapping("/totals-by-rseti")
    public ResponseEntity<List<YWHistTotalTraineesDto>> getTotalsByRseti() {
        return ResponseEntity.ok(historicDataService.getTotalsByRseti());
    }
}