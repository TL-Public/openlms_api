package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.HistoricDataMonthWiseDto;
import com.tl.reap_admin_api.model.HistoricDataMonthWise;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class HistoricDataMonthWiseMapper {
    public List<HistoricDataMonthWise> toEntities(HistoricDataMonthWiseDto dto) {
        List<HistoricDataMonthWise> entities = new ArrayList<>();
        
        for (int i = 0; i < dto.getMonthlyData().length; i++) {
            HistoricDataMonthWiseDto.MonthlyData monthData = dto.getMonthlyData()[i];
            if (monthData != null && (monthData.getProgrammesConducted() > 0 || monthData.getCandidatesTrained() > 0)) {
                entities.add(createHistoricData(dto, (long) (i + 1), monthData));
            }
        }
        
        return entities;
    }

    private HistoricDataMonthWise createHistoricData(HistoricDataMonthWiseDto dto, Long month, HistoricDataMonthWiseDto.MonthlyData monthlyData) {
        HistoricDataMonthWise entity = new HistoricDataMonthWise();
        entity.setStateId(dto.getStateId());
        entity.setStateName(dto.getStateName());
        entity.setCourseCode(dto.getCourseCode());
        entity.setMonth(month);
        entity.setCourseCount(monthlyData.getProgrammesConducted());
        entity.setTraineeCount(monthlyData.getCandidatesTrained());
        return entity;
    }
   
}