package com.tl.reap_admin_api.mapper;

import com.tl.reap_admin_api.dto.TraineeCourseDto;
import com.tl.reap_admin_api.model.TraineeCourse;
import org.springframework.stereotype.Component;

@Component
public class TraineeCourseMapper {

    public TraineeCourseDto toDto(TraineeCourse traineeCourse) {
        TraineeCourseDto dto = new TraineeCourseDto();
        dto.setTraineeUuid(traineeCourse.getTrainee().getUuid());
        dto.setEnrollmentDate(traineeCourse.getEnrollmentDate());
        return dto;
    }

    public TraineeCourse toEntity(TraineeCourseDto dto) {
        TraineeCourse traineeCourse = new TraineeCourse();
        traineeCourse.setEnrollmentDate(dto.getEnrollmentDate());
        return traineeCourse;
    }
}

