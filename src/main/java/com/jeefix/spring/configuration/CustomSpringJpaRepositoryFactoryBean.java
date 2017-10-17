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

import com.jeefix.jpa.acl.AclQueryLookupStrategyWrapper;
import com.jeefix.jpa.acl.AclSpringJpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;

import javax.persistence.EntityManager;
import java.io.Serializable;

/**
 * Overrides default spring JPA factories
 * <p>
 * Created by Maciej Iskra (emacisk) on 2017-10-05.
 */
//TODO emacisk write unit tests
public class CustomSpringJpaRepositoryFactoryBean<T extends JpaRepository<S, ID>, S, ID extends Serializable>
        extends JpaRepositoryFactoryBean<T, S, ID> {

    public CustomSpringJpaRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }


    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        return new JpaRepositoryFactory(entityManager) {
            @Override
            protected Object getTargetRepository(RepositoryInformation information) {
                return new AclSpringJpaRepository<>(getEntityInformation(information.getDomainType()), entityManager);
            }

            @Override
            protected QueryLookupStrategy getQueryLookupStrategy(QueryLookupStrategy.Key key, EvaluationContextProvider evaluationContextProvider) {
                return new AclQueryLookupStrategyWrapper(super.getQueryLookupStrategy(key, evaluationContextProvider), entityManager);
            }
        };
    }

}