package com.tl.reap_admin_api.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tl.reap_admin_api.dto.CourseDto;
import com.tl.reap_admin_api.dto.RsetiCourseDto;
import com.tl.reap_admin_api.dto.RsetiDto;
import com.tl.reap_admin_api.model.RsetiCourse;
import com.tl.reap_admin_api.service.CourseService;
import com.tl.reap_admin_api.service.RsetiService;


@Component
public class RsetiCourseMapper {

	@Autowired
	private CourseService  courseService;

	@Autowired
	private RsetiService  rsetiService;

	@Autowired
	private CourseMapper  courseMapper;

	@Autowired
	private RsetiMapper  rsetiMapper;

	public RsetiCourseDto toDto(RsetiCourse rsetiCourse) {
		if (rsetiCourse == null) {
			return null;
		}

		return new RsetiCourseDto(rsetiCourse.getUuid(), rsetiCourse.getCourse().getUuid(), rsetiCourse.getRseti().getUuid(),
				rsetiCourse.getStartDate().getYear(), rsetiCourse.getStartDate().getMonthValue(), 
				rsetiCourse.getEndDate().getYear(), rsetiCourse.getEndDate().getMonthValue(),rsetiCourse.getStatus());
	}

	public RsetiCourse toEntity(RsetiCourseDto dto) {
		if (dto == null) {
			return null;
		}

		RsetiCourse rsetiCourse = new RsetiCourse();

		rsetiCourse.setUuid(dto.getUuid());

		CourseDto courseDto = courseService.getCourseByUuid(dto.getCourseUuid());
		rsetiCourse.setCourse(courseMapper.toEntity(courseDto));

		RsetiDto rsetiDto = rsetiService.getRsetiByUuid(dto.getRsetiUuid());
		rsetiCourse.setRseti(rsetiMapper.toEntity(rsetiDto));

		if (dto.getStartYear() != 0 && dto.getStartMonth() != 0) {
			rsetiCourse.setStartDate(LocalDate.of(dto.getStartYear(), dto.getStartMonth(), 1));
		}
		if (dto.getEndYear() != 0 && dto.getEndMonth() != 0) {
			rsetiCourse.setEndDate(LocalDate.of(dto.getEndYear(), dto.getEndMonth(), 1));
		}
		return rsetiCourse;
	}

	public List<RsetiCourseDto> toDtoList(List<RsetiCourse> rsetiCourses) {
		return rsetiCourses.stream().map(this::toDto).collect(Collectors.toList());
	}

	public List<RsetiCourse> toEntityList(List<RsetiCourseDto> dtos) {
		return dtos.stream().map(this::toEntity).collect(Collectors.toList());
	}
}