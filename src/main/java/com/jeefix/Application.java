package com.jeefix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.persistence.EntityManager;

@SpringBootApplication
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Bean
    public CommandLineRunner demo(ManagedElementRepository repository, ConfigFileSetRepository cfsRepository,EntityManager entityManager) {
        return (args) -> {
            // save a couple of customers

            ManagedElement me1 = repository.save(new ManagedElement("SCHWEDEN1", "SBG"));
            ManagedElement me2 = repository.save(new ManagedElement("SCHWEDEN2", "SBG"));
            ManagedElement me3 = repository.save(new ManagedElement("SCHWEDEN3", "CSCF"));
            ManagedElement me4 = repository.save(new ManagedElement("SCHWEDEN5", "MRS"));

            cfsRepository.save(new ConfigFileSet(me1, me1.getMeName() + "CFS"));
            cfsRepository.save(new ConfigFileSet(me2, me2.getMeName() + "CFS"));
            cfsRepository.save(new ConfigFileSet(me3, me3.getMeName() + "CFS"));
            cfsRepository.save(new ConfigFileSet(me4, me4.getMeName() + "CFS"));

            Iterable<ConfigFileSet> configFileSets = cfsRepository.findAll();
            log.info("");
            for (ConfigFileSet cfs : configFileSets) {
                log.info(cfs.toString());
            }

        };
    }

}
