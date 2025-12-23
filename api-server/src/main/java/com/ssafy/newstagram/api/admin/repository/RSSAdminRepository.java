package com.ssafy.newstagram.api.admin.repository;

import com.ssafy.newstagram.domain.log.entity.SystemJobLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RSSAdminRepository extends JpaRepository<SystemJobLog, Long> {

    interface JobLogView {
        java.time.LocalDateTime getEndedAt();
        String getJobName();
        String getMessage();
        java.time.LocalDateTime getStartedAt();
        String getStatus();
    }

    @Query(
            value = """
            SELECT
                ended_at AS endedAt,
                job_name AS jobName,
                message  AS message,
                started_at AS startedAt,
                status   AS status
            FROM system_job_logs
            WHERE (
                run_date IS NOT NULL AND run_date::date = :periodDate
            ) OR (
                run_date IS NULL AND started_at::date = :periodDate
            )
            ORDER BY started_at DESC
            """,
            nativeQuery = true
    )
    List<JobLogView> findJobLogsByPeriodDate(@Param("periodDate") LocalDate periodDate);
}

