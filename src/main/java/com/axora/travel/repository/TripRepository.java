package com.axora.travel.repository;

import com.axora.travel.entities.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, String> {

    // --- scoped trip finder start ---
    @Query("""
    select distinct t from Trip t
    left join t.participants p
    where t.owner = :user or p = :user
    """)
    List<Trip> findVisibleTo(@Param("user") String user);
    // --- scoped trip finder end ---
}
