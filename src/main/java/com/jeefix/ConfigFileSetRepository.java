package com.jeefix;

import org.hibernate.engine.spi.Managed;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ConfigFileSetRepository extends CrudRepository<ConfigFileSet, Long> {


    @Query("SELECT cfs FROM ConfigFileSet cfs join fetch cfs.managedElement")
    List<ConfigFileSet> findByQuery();


    ConfigFileSet findByName(String name);


}
