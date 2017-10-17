/*
 * Copyright (c) 2017; Ericsson GmbH. ALL RIGHTS RESERVED.
 *
 * THIS FILE CONTAINS PROPRIETARY SOURCE CODE OF Ericsson GmbH. THIS FILE IS
 * SUBMITTED TO RECIPIENT IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT
 * BE USED, COPIED OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY
 * WRITTEN AGREEMENT SIGNED BY AN AUTHORIZED PERSON OF Ericsson GmbH.
 *
 */
package com.jeefix.jpa.acl.enhancer;

import com.jeefix.jpa.acl.AclSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.PartTreeJpaQuery;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.RepositoryQuery;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;

/**
 * Enhances PartTreeJpaQuery with Acl predicate
 * <p>
 * Created by Maciej Iskra (emacisk) on 2017-10-13.
 */
//TODO emacisk write unit tests
public class QueryEnhancer {

    private static final Logger LOG = LoggerFactory.getLogger(AclSpecification.class);

    public static PartTreeJpaQuery enhanceQuery(EntityManager em, RepositoryMetadata metadata, ProjectionFactory factory, PartTreeJpaQuery query) {
        try {

            Object queryPreparerOriginal = getQueryPreparer(query);
            Object proxy = createEnhancedQueryPreparerProxy(queryPreparerOriginal, em, metadata, factory, query);
            setQueryPreparer(query, proxy);
            LOG.debug("Query has been enhanced with ALC restriction predicate");
        } catch (Exception e) {
            LOG.error("Unable to enhance query with ACL restrictions", e);
        }
        return query;
    }


    protected static Object getQueryPreparer(RepositoryQuery query) throws NoSuchFieldException, IllegalAccessException {
        Field queryField = query.getClass().getDeclaredField("query");
        queryField.setAccessible(true);
        return queryField.get(query);
    }

    protected static void setQueryPreparer(RepositoryQuery query, Object queryPreparer) throws NoSuchFieldException, IllegalAccessException {
        Field queryField = query.getClass().getDeclaredField("query");
        queryField.setAccessible(true);
        queryField.set(query, queryPreparer);
    }

    protected static Object createEnhancedQueryPreparerProxy(Object queryPreparerOriginal,
                                                             EntityManager em, RepositoryMetadata metadata,
                                                             ProjectionFactory factory, PartTreeJpaQuery query) {
        //create CGLIB proxy
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(queryPreparerOriginal.getClass());

        //intercept create query method via proxy
        enhancer.setCallback(new AclMethodInterceptor(query.getQueryMethod(), em, factory, metadata, queryPreparerOriginal));

        //set proxy on original class
        return enhancer.create(
                new Class[]{PartTreeJpaQuery.class, PersistenceProvider.class, boolean.class},
                new Object[]{query, PersistenceProvider.fromEntityManager(em), true});
    }


}
