package com.jacto.scheduler.repository;

import com.jacto.scheduler.model.Scheduling;
import com.jacto.scheduler.enumerations.SchedulingStatus;
import com.jacto.scheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SchedulingRepository extends JpaRepository<Scheduling, Long> {
    List<Scheduling> findByTechnicianOrderByScheduledAtDesc(User technician);

    @Query("SELECT s FROM Scheduling s WHERE s.technician = ?1 AND s.scheduledAt > ?2 ORDER BY s.scheduledAt ASC")
    List<Scheduling> findUpcomingSchedulings(User technician, LocalDateTime now);

    @Query("SELECT s FROM Scheduling s WHERE s.scheduledAt BETWEEN ?1 AND ?2 AND s.status <> 'CANCELLED'")
    List<Scheduling> findSchedulingsForDateRange(LocalDateTime start, LocalDateTime end);

    @Query("SELECT AVG(s.clientRating) FROM Scheduling s WHERE s.technician = ?1 AND s.clientRating IS NOT NULL")
    Double findAverageRatingForTechnician(User technician);

    @Query("SELECT COUNT(s) FROM Scheduling s WHERE s.technician = ?1 AND s.status = 'COMPLETED'")
    Long countCompletedSchedulings(User technician);

    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, s.scheduledAt, s.completedAt)) FROM Scheduling s " +
           "WHERE s.technician = ?1 AND s.status = 'COMPLETED' AND s.completedAt IS NOT NULL")
    Double findAverageCompletionTimeForTechnician(User technician);
}
