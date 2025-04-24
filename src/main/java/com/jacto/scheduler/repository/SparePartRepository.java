package com.jacto.scheduler.repository;

import com.jacto.scheduler.model.SparePart;
import com.jacto.scheduler.model.Scheduling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SparePartRepository extends JpaRepository<SparePart, Long> {
    List<SparePart> findByScheduling(Scheduling scheduling);
}
