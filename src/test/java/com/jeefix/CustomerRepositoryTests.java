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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(Configuration.class)
public class CustomerRepositoryTests {

    @Autowired
    private ManagedElementRepository meRepository;

    @Autowired
    private ConfigFileSetRepository cfsRepository;

    @Test
    public void testFindByLastName() {

        Iterable<ConfigFileSet> findAll = cfsRepository.findAll();
        assertEquals(findAll.iterator().next().getName(), "SCHWEDEN1CFS");
        assertEquals(StreamSupport.stream(findAll.spliterator(), false).count(), 1);

        Iterable<ConfigFileSet> findAllPredicate = cfsRepository.findAll(Arrays.asList(1l, 2l));
        assertEquals(findAllPredicate.iterator().next().getName(), "SCHWEDEN1CFS");
        assertEquals(StreamSupport.stream(findAllPredicate.spliterator(), false).count(), 1);

        ConfigFileSet findByName = cfsRepository.findByName("SCHWEDEN1CFS");
        assertNotNull(findByName);
        assertEquals(findByName.getName(), "SCHWEDEN1CFS");
        findByName = cfsRepository.findByName("SCHWEDEN2CFS");
        assertNull(findByName);


    }
}