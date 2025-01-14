package com.tl.reap_admin_api.controller;

import com.tl.reap_admin_api.dto.CourseStatisticsDto;
import com.tl.reap_admin_api.dto.RsetiStatisticsDto;
import com.tl.reap_admin_api.dto.StatisticsDto;
import com.tl.reap_admin_api.service.StatisticsService;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/apis/v1/stats")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Autowired
    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public ResponseEntity<StatisticsDto> getStatistics() {
        StatisticsDto statistics = statisticsService.getStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    //course details
    
    @GetMapping("/courses/{courseUuid}")
    public ResponseEntity<CourseStatisticsDto> getCourseStatistics(@PathVariable UUID courseUuid) {
        CourseStatisticsDto courseStatistics = statisticsService.getCourseStatistics(courseUuid);
        return ResponseEntity.ok(courseStatistics);
    }
    
    
    //Rseti details
    
    @GetMapping("/rsetis/{rsetiUuid}")
    public ResponseEntity<RsetiStatisticsDto> getRsetiStatistics(@PathVariable UUID rsetiUuid) {
        RsetiStatisticsDto rsetiStatistics = statisticsService.getRsetiStatistics(rsetiUuid);
        return ResponseEntity.ok(rsetiStatistics);
    }
}