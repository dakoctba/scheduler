package com.jacto.scheduler.repository;

import com.jacto.scheduler.model.Equipment;
import com.jacto.scheduler.model.Scheduling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByScheduling(Scheduling scheduling);
}
