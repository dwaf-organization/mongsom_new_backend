package com.mongsom.dev.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mongsom.dev.entity.OptionCombinationMapping;

@Repository
public interface OptionCombinationMappingRepository extends JpaRepository<OptionCombinationMapping, Integer> {
    List<OptionCombinationMapping> findByCombinationId(Integer combinationId);
    void deleteByCombinationId(Integer combinationId);
}