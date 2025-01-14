package com.tl.reap_admin_api.dao;

import com.tl.reap_admin_api.model.HistoricDataMonthWise;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

@Repository
public class HistoricDataMonthWiseDao {

    @PersistenceContext
    private EntityManager entityManager;

    private final JdbcTemplate jdbcTemplate;

    public HistoricDataMonthWiseDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void batchInsert(List<HistoricDataMonthWise> historicDataList) {
        String sql = """
            INSERT INTO historicdata (state_id, mnth, crs_cnt, trainee_cnt, course_code, state_name)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.batchUpdate(sql,
            historicDataList,
            historicDataList.size(),
            (ps, historicData) -> {
                ps.setLong(1, historicData.getStateId());
                ps.setLong(2, historicData.getMonth());
                ps.setLong(3, historicData.getCourseCount());
                ps.setLong(4, historicData.getTraineeCount());
                ps.setString(5, historicData.getCourseCode());
                ps.setString(6, historicData.getStateName());
            });
    }

    @Transactional
    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM historicdata");
    }
}