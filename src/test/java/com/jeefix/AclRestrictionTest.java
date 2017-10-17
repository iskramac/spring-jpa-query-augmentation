/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jeefix;

import com.jeefix.jpa.entity.ConfigFileSet;
import com.jeefix.jpa.repository.ConfigFileSetRepository;
import com.jeefix.secuturity.SecurityContext;
import com.jeefix.spring.configuration.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(Configuration.class)
public class AclRestrictionTest {


    @Autowired
    private ConfigFileSetRepository cfsRepository;

    @Test
    public void testFindByRepositoryMethods() {
        SecurityContext.setRoles(Arrays.asList("SCHWEDEN1"));
        Iterable<ConfigFileSet> findAll = cfsRepository.findAll();
        assertEquals(findAll.iterator().next().getName(), "SCHWEDEN1CFS");
        assertEquals(StreamSupport.stream(findAll.spliterator(), false).count(), 100);

        Iterable<ConfigFileSet> findAllPredicate = cfsRepository.findAll(Arrays.asList(1l, 200l));
        assertEquals(findAllPredicate.iterator().next().getName(), "SCHWEDEN1CFS");
        assertEquals(StreamSupport.stream(findAllPredicate.spliterator(), false).count(), 1);

        List<ConfigFileSet> findByName = cfsRepository.findByName("SCHWEDEN1CFS");
        assertNotNull(findByName);
        assertEquals(findByName.iterator().next().getName(), "SCHWEDEN1CFS");
        findByName = cfsRepository.findByName("SCHWEDEN2CFS");
        assertTrue(findByName.isEmpty());
//

        Page<ConfigFileSet> findByNamePageableDenied = cfsRepository.findByName("SCHWEDEN2CFS",new PageRequest(0,10));
        assertEquals(StreamSupport.stream(findByNamePageableDenied.spliterator(), false).count(),0);
        SecurityContext.setRoles(Arrays.asList("SCHWEDEN1", "SCHWEDEN2"));

        findAll = cfsRepository.findAll();
        assertEquals(StreamSupport.stream(findAll.spliterator(), false).count(), 200);

        findByName = cfsRepository.findByName("SCHWEDEN2CFS");
        assertFalse(findByName.isEmpty());
        assertEquals(findByName.iterator().next().getName(), "SCHWEDEN2CFS");

        Page<ConfigFileSet> findByNamePageable = cfsRepository.findByName("SCHWEDEN1CFS",new PageRequest(0,10));
        assertNotNull(findByNamePageable);
        assertEquals(findByNamePageable.iterator().next().getName(), "SCHWEDEN1CFS");
        assertEquals(StreamSupport.stream(findByNamePageable.spliterator(), false).count(), 10);
        assertEquals(findByNamePageable.getTotalElements(), 100);
        Page<ConfigFileSet> findByNamePageable2 = cfsRepository.findByName("SCHWEDEN2CFS",new PageRequest(0,10));
        assertEquals(StreamSupport.stream(findByNamePageable2.spliterator(), false).count(),10);
        assertEquals(findByNamePageable2.getTotalElements(), 100);


    }
}