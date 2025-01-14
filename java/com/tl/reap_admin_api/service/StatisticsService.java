package com.tl.reap_admin_api.service;

import com.tl.reap_admin_api.dto.CourseStatisticsDto;
import com.tl.reap_admin_api.dto.RsetiStatisticsDto;
import com.tl.reap_admin_api.dto.StatisticsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.UUID;

@Service
public class StatisticsService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public StatisticsDto getStatistics() {
        StatisticsDto stats = new StatisticsDto();

        stats.setTotalCourses(getTotalCourses());        
        stats.setTotalTrainingCenters(getTotalTrainingCenters());
        stats.setTotalTrainingCenterCourses(getTotalTrainingCenterCourses());
        stats.setTotalTraineesEnrolledThisYear(getTotalTraineesEnrolledThisYear());
        stats.setTotalCategories(getTotalCategories());
        stats.setTotalCourseDuration(getTotalCourseDuration());
        stats.setTotalVideoDuration(getTotalVideoDuration());
        stats.setTotalVideos(getTotalVideos());
        stats.setTotalTrainees(getTotalTrainees());
        stats.setTotalStates(getTotalStates());
        stats.setAvgTraineePerState(getAvgTraineePerState());
        stats.setAvgTraineePerRSETI(getAvgTraineePerRSETI());
        stats.setAvgRSETIPerState(getAvgRSETIPerState());

        return stats;
    }

    private long getTotalCourses() {
        return executeCountQuery("SELECT COUNT(c) FROM Course c");
    }

    private long getTotalTrainingCenters() {
        return executeCountQuery("SELECT COUNT(r) FROM RSETI r");
    }

    private long getTotalTrainingCenterCourses() {
        return executeCountQuery("SELECT COUNT(rc) FROM RsetiCourse rc");
    }

    private long getTotalTraineesEnrolledThisYear() {
        int currentYear = Year.now().getValue();
        return executeCountQuery(
                "SELECT COUNT(DISTINCT tr.traineeProfile.id) FROM TraineeRseti tr " +
                "WHERE EXTRACT(YEAR FROM tr.enrolledOn) = :year",
                "year", currentYear);
    }

    private long getTotalStates() {
        return executeCountQuery("SELECT COUNT(s) FROM State s where s.languageCode='en'");
    }


    private long getTotalCategories() {
        return executeCountQuery("SELECT COUNT(c) FROM Category c");
    }

    private long getTotalCourseDuration() {
        return executeCountQuery("SELECT COALESCE(SUM(c.duration), 0) FROM Course c");
    }

    private long getTotalVideoDuration() {
        return executeCountQuery("SELECT COALESCE(SUM(v.duration), 0) FROM Video v");
    }

    private long getTotalVideos() {
        return executeCountQuery("SELECT COUNT(v) FROM Video v");
    }

    private long getTotalTrainees() {
        return executeCountQuery("SELECT COUNT(DISTINCT tp) FROM TraineeProfile tp");
    }

    private double getAvgTraineePerState() {
        return executeAvgQuery(
                "SELECT AVG(trainees) FROM " +
                "(SELECT COUNT(DISTINCT tr.traineeProfile.id) as trainees " +
                "FROM TraineeRseti tr " +
                "JOIN tr.rseti r " +
                "GROUP BY r.stateId) as state_trainees");
    }

    private double getAvgTraineePerRSETI() {
        return executeAvgQuery(
                "SELECT AVG(trainees) FROM " +
                "(SELECT COUNT(DISTINCT tr.traineeProfile.id) as trainees " +
                "FROM TraineeRseti tr " +
                "GROUP BY tr.rseti.id) as rseti_trainees");
    }

    private double getAvgRSETIPerState() {
        return executeAvgQuery(
                "SELECT AVG(rsetis) FROM " +
                "(SELECT COUNT(r.id) as rsetis " +
                "FROM RSETI r " +
                "GROUP BY r.stateId) as state_rsetis");
    }

    private long executeCountQuery(String queryString) {
        try {
            return ((Number) entityManager.createQuery(queryString).getSingleResult()).longValue();
        } catch (NoResultException | NullPointerException e) {
            return 0L;
        }
    }

    private long executeCountQuery(String queryString, String paramName, Object paramValue) {
        try {
            return ((Number) entityManager.createQuery(queryString)
                    .setParameter(paramName, paramValue)
                    .getSingleResult()).longValue();
        } catch (NoResultException | NullPointerException e) {
            return 0L;
        }
    }

    private double executeAvgQuery(String queryString) {
        try {
            Number result = (Number) entityManager.createQuery(queryString).getSingleResult();
            return result != null ? result.doubleValue() : 0.0;
        } catch (NoResultException | NullPointerException e) {
            return 0.0;
        }
    }
    
   //course details
    
    @Transactional(readOnly = true)
    public CourseStatisticsDto getCourseStatistics(UUID courseUuid) {
        CourseStatisticsDto stats = new CourseStatisticsDto();

        stats.setTotalChapters(getTotalChaptersForCourse(courseUuid));
        stats.setTotalVideoDuration(getTotalVideoDurationForCourse(courseUuid));
        stats.setCourseDuration(getCourseDuration(courseUuid));
        stats.setNumberOfStudents(getNumberOfStudentsForCourse(courseUuid));
        stats.setTotalRsetis(getTotalRsetisForCourse(courseUuid));

        return stats;
    }

    private long getTotalChaptersForCourse(UUID courseUuid) {
        return executeCountQuery(
            "SELECT COUNT(c) FROM Chapter c WHERE c.course.uuid = :courseUuid",
            "courseUuid", courseUuid);
    }

    private long getTotalVideoDurationForCourse(UUID courseUuid) {
        return executeCountQuery(
            "SELECT COALESCE(SUM(v.duration), 0) FROM Video v JOIN v.chapterVideos cv JOIN cv.chapter c WHERE c.course.uuid = :courseUuid",
            "courseUuid", courseUuid);
    }

    private long getCourseDuration(UUID courseUuid) {
        return executeCountQuery(
            "SELECT COALESCE(c.duration, 0) FROM Course c WHERE c.uuid = :courseUuid",
            "courseUuid", courseUuid);
    }

    private long getNumberOfStudentsForCourse(UUID courseUuid) {
        return executeCountQuery(
        	"SELECT COUNT(DISTINCT tr.traineeProfile.id) FROM TraineeRseti tr WHERE tr.rsetiCourse.course.uuid = :courseUuid",
            "courseUuid", courseUuid);
    }

    private long getTotalRsetisForCourse(UUID courseUuid) {
        return executeCountQuery(
            "SELECT COUNT(DISTINCT rc.rseti.id) FROM RsetiCourse rc WHERE rc.course.uuid = :courseUuid",
            "courseUuid", courseUuid);
    }
    
    // Rseti details
    
    
    @Transactional(readOnly = true)
    public RsetiStatisticsDto getRsetiStatistics(UUID rsetiUuid) {
        RsetiStatisticsDto stats = new RsetiStatisticsDto();
        stats.setTotalCourses(getTotalCoursesForRseti(rsetiUuid));
        stats.setTotalTrainees(getTotalTraineesForRseti(rsetiUuid));

        return stats;
    }

    private long getTotalCoursesForRseti(UUID rsetiUuid) {
        return executeCountQuery(
        		 "SELECT COUNT(rc) FROM RsetiCourse rc WHERE rc.rseti.uuid = :rsetiUuid",
                 "rsetiUuid", rsetiUuid);
    }

    private long getTotalTraineesForRseti(UUID rsetiUuid) {
        return executeCountQuery(
            "SELECT COUNT(DISTINCT tr.traineeProfile.id) FROM TraineeRseti tr WHERE tr.rseti.uuid = :rsetiUuid",
            "rsetiUuid", rsetiUuid);
    }
}