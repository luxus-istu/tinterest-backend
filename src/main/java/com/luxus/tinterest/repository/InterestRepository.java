package com.luxus.tinterest.repository;

import com.luxus.tinterest.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

    @Query("select i from Interest i where lower(i.name) in :normalizedNames")
    List<Interest> findAllByNormalizedNames(@Param("normalizedNames") Collection<String> normalizedNames);
}
