/*
 * Copyright (c) 2017; Ericsson GmbH. ALL RIGHTS RESERVED.
 *
 * THIS FILE CONTAINS PROPRIETARY SOURCE CODE OF Ericsson GmbH. THIS FILE IS
 * SUBMITTED TO RECIPIENT IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT
 * BE USED, COPIED OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY
 * WRITTEN AGREEMENT SIGNED BY AN AUTHORIZED PERSON OF Ericsson GmbH.
 *
 */
package com.jeefix.spring.configuration;

import org.springframework.data.jpa.repository.query.PartTreeJpaQuery;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.RepositoryQuery;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 * TODO write class description here
 * <p>
 * Created by Maciej Iskra (emacisk) on 2017-10-10.
 */
//TODO emacisk write unit tests
//TODO emacisk add logging
public class CustomQueryLookupStrategyWrapper implements QueryLookupStrategy {

    private final QueryLookupStrategy queryLookupStrategy;
    private final EntityManager em;

    public CustomQueryLookupStrategyWrapper(QueryLookupStrategy original, EntityManager em) {
        this.queryLookupStrategy = original;
        this.em = em;
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) {


        RepositoryQuery query = queryLookupStrategy.resolveQuery(method, metadata, factory, namedQueries);
        return wrapQuery(method,metadata,query);
    }
    private RepositoryQuery wrapQuery(Method method, RepositoryMetadata metadata,
                                      RepositoryQuery query) {

        if (query instanceof PartTreeJpaQuery) {
            query = new CustomJpaQuery(method, query, metadata.getDomainType(), em);
        }
        return query;
    }
}
