package com.jeefix;

import com.jeefix.jpa.entity.ConfigFileSet;
import com.jeefix.jpa.entity.ManagedElement;
import com.jeefix.jpa.repository.ConfigFileSetRepository;
import com.jeefix.jpa.repository.ManagedElementRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.stream.IntStream;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }

    @Bean
    public CommandLineRunner demo(ManagedElementRepository repository, ConfigFileSetRepository cfsRepository) {
        return (args) -> {
            // save a couple of customers
            IntStream.rangeClosed(1, 1000).forEach(i -> {
                int meNumber = i % 10;
                ManagedElement me1 = repository.save(new ManagedElement((long) i, "SCHWEDEN" + meNumber, "SBG"));
                cfsRepository.save(new ConfigFileSet(me1, me1.getMeName() + "CFS"));
            });


        };
    }

}
