package com.jeefix.jpa.repository;

import com.jeefix.jpa.entity.ConfigFileSet;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ConfigFileSetRepository extends CrudRepository<ConfigFileSet, Long> {


    @Query("SELECT cfs FROM ConfigFileSet cfs join fetch cfs.managedElement")
    List<ConfigFileSet> findByQuery();


    ConfigFileSet findByName(String name);


}
