package com.tl.reap_admin_api.controller;

import com.tl.reap_admin_api.service.HistoricMonthWiseDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/apis/v1/historic-data-monthwise")
public class HistoricDataMonthWiseController {

    @Autowired
    private HistoricMonthWiseDataService historicDataService;

    @GetMapping("/trainee-count-by-categories")
    public List<Map<String, Object>> getTraineeCountByCategoryAndMonth() {
        return historicDataService.getTraineeCountByCategoryAndMonth();
    }

    @GetMapping("/trainee-count-by-courses")
    public List<Map<String, Object>> getTraineeCountByCourseAndMonth() {
        return historicDataService.getTraineeCountByCourseAndMonth();
    }

    @GetMapping("/trainee-count-by-allcourses")
    public List<Map<String, Object>> getTotalTraineeCountByMonthForAllCourses() {
        return historicDataService.getTotalTraineeCountByMonthForAllCourses();
    }

    @GetMapping("/trainee-count-by-states")
    public List<Map<String, Object>> getTotalTraineeCountByMonthForStates() {
        return historicDataService.getTotalTraineeCountByMonthForStates();
    }

    @GetMapping("/course-count-by-states")
    public List<Map<String, Object>> getTotalCourseCountForStates() {
        return historicDataService.getTotalCourseCountForStates();
    }    

     @PostMapping("/bulk-upload")
    public ResponseEntity<?> uploadHistoricData(@RequestParam("file") MultipartFile file) {
        try {
            historicDataService.processExcelFile(file);
            return ResponseEntity.ok("Historic data uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing file: " + e.getMessage());
        }
    }
}