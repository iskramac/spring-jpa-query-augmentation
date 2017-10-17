package com.jeefix.jpa.repository;

import com.jeefix.jpa.entity.ConfigFileSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConfigFileSetRepository extends CrudRepository<ConfigFileSet, Long> {


    @Query("SELECT cfs FROM ConfigFileSet cfs join fetch cfs.managedElement")
    List<ConfigFileSet> findByQuery();


    List<ConfigFileSet> findByName(String name);

    Page<ConfigFileSet> findByName(@Param("name") String name, Pageable pageable);




}
