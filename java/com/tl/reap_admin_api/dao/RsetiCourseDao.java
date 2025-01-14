package com.tl.reap_admin_api.dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.tl.reap_admin_api.dto.RsetiCourseBatchUpdateDto;
import com.tl.reap_admin_api.model.Course;
import com.tl.reap_admin_api.model.RSETI;
import com.tl.reap_admin_api.model.RsetiCourse;
import com.tl.reap_admin_api.model.TraineeCredential;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Repository
public class RsetiCourseDao {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Transactional
    public RsetiCourse save(RsetiCourse rsetiCourse) {
        if (rsetiCourse.getId() == null) {
            entityManager.persist(rsetiCourse);
        } else {
            rsetiCourse = entityManager.merge(rsetiCourse);
        }
        return rsetiCourse;
    }
	
	
	public List<RsetiCourse> findByRseti(RSETI rseti) {
		TypedQuery<RsetiCourse> query = entityManager
				.createQuery("SELECT rc FROM RsetiCourse rc WHERE rc.rseti = :rseti", RsetiCourse.class);
		query.setParameter("rseti", rseti);
		return query.getResultList();
	}

	public Optional<RsetiCourse> findByRsetiAndCourseCode(RSETI rseti, String courseCode) {
		TypedQuery<RsetiCourse> query = entityManager
				.createQuery("SELECT rc FROM RsetiCourse rc JOIN rc.rseti r JOIN Course c ON rc.id = c.id "
						+ "WHERE r = :rseti AND c.courseCode = :courseCode", RsetiCourse.class);
		query.setParameter("rseti", rseti);
		query.setParameter("courseCode", courseCode);
		List<RsetiCourse> results = query.getResultList();
		return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
	}

	public Optional<RsetiCourse> findByRsetiAndCourse(RSETI rseti, Course course) {
		TypedQuery<RsetiCourse> query = entityManager.createQuery(
				"SELECT rc FROM RsetiCourse rc WHERE rc.rseti = :rseti AND rc.id = :courseId", RsetiCourse.class);
		query.setParameter("rseti", rseti);
		query.setParameter("courseId", course.getId());
		List<RsetiCourse> results = query.getResultList();
		return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
	}

	public Optional<RsetiCourse> findByRsetiUuidAndCourseUuid(UUID rsetiUuid, UUID courseUuid) {
        TypedQuery<RsetiCourse> query = entityManager.createQuery(
                "SELECT rc FROM RsetiCourse rc " +
                "JOIN rc.rseti r " +
                "JOIN rc.course c " +
                "WHERE r.uuid = :rsetiUuid AND c.uuid = :courseUuid", RsetiCourse.class);
        query.setParameter("rsetiUuid", rsetiUuid);
        query.setParameter("courseUuid", courseUuid);
        List<RsetiCourse> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

	public Optional<RsetiCourse> findByRsetiAndCourseAndStartDate(RSETI rseti, Course course, LocalDate startDate) {
        TypedQuery<RsetiCourse> query = entityManager.createQuery(
                "SELECT rc FROM RsetiCourse rc WHERE rc.rseti = :rseti AND rc.course = :course AND rc.startDate = :startDate", RsetiCourse.class);
        query.setParameter("rseti", rseti);
        query.setParameter("course", course);
        query.setParameter("startDate", startDate);
        List<RsetiCourse> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

	public List<RsetiCourse> findByCourse(Course course) {
        TypedQuery<RsetiCourse> query = entityManager.createQuery(
            "SELECT rc FROM RsetiCourse rc WHERE rc.course = :course", RsetiCourse.class);
        query.setParameter("course", course);
        return query.getResultList();
    }

	@Transactional
	public void delete(RsetiCourse rsetiCourse) {
		entityManager.remove(rsetiCourse);
	}

	private final JdbcTemplate jdbcTemplate;

    public RsetiCourseDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

	 public Map<String, Long> findAndCreateMapOfAllRsetiCourses() {
        String sql = "SELECT CONCAT(c.course_code, rt.name, TO_CHAR(rc.start_date, 'FMMonth'), EXTRACT(YEAR FROM rc.start_date)) "+
						"AS concatenated_result, rc.id as id from rseti_courses rc " + //
						"join courses c on c.id = rc.course_id " + //
						"join rsetis r on r.id = rc.rseti_id " + //
						"join rseti_translations rt on rt.rseti_id = r.id WHERE rt.language_id = 1";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Object[]{rs.getString("concatenated_result"), rs.getLong("id")})
            .stream()
            .collect(Collectors.toMap(
                arr -> (String) arr[0],
                arr -> (Long) arr[1],
                (existing, replacement) -> existing, // In case of duplicate keys, keep the existing value
                LinkedHashMap::new // Use LinkedHashMap to maintain insertion order
            ));
    }

    @Transactional
    public void batchInsert(List<RsetiCourseBatchUpdateDto> rsetiCourseDtos) {
        String sql = "INSERT INTO rseti_courses (uuid, rseti_id, course_id, start_date, end_date, created_at, updated_at, created_by, updated_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, rsetiCourseDtos, 100, (ps, dto) -> {
            ps.setObject(1, UUID.randomUUID());
            ps.setLong(2, dto.getRsetiId());
            ps.setLong(3, dto.getCourseId());
			ps.setDate(4, Date.valueOf(dto.getStartDate()));
            ps.setDate(5, Date.valueOf(dto.getEndDate()));
            ps.setTimestamp(6, dto.getCreatedAt() != null ? Timestamp.from(dto.getCreatedAt().toInstant()) : null);
            ps.setTimestamp(7, dto.getUpdatedAt() != null ? Timestamp.from(dto.getUpdatedAt().toInstant()) : null);
            ps.setString(8, dto.getCreatedBy());
            ps.setString(9, dto.getUpdatedBy());
        });
    }


	public Optional<RsetiCourse> findByRsetiUuidAndUuid(UUID rsetiUuid, UUID uuid) {
		TypedQuery<RsetiCourse> query = entityManager.createQuery(
				"SELECT rc FROM RsetiCourse rc " + "JOIN rc.rseti r " + "WHERE r.uuid = :rsetiUuid AND rc.uuid = :uuid",
				RsetiCourse.class);
		query.setParameter("rsetiUuid", rsetiUuid);
		query.setParameter("uuid", uuid);
		List<RsetiCourse> results = query.getResultList();
		return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
	}


	public Optional<RsetiCourse> findByUuid(UUID uuid) {
        try {
            RsetiCourse rsetiCourse = entityManager.createQuery(
                "SELECT rc FROM RsetiCourse rc WHERE rc.uuid = :uuid", RsetiCourse.class)
                .setParameter("uuid", uuid)
                .getSingleResult();
            return Optional.of(rsetiCourse);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}