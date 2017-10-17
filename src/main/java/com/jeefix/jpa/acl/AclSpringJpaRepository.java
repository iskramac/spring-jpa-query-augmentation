/*
 * Copyright (c) 2017; Ericsson GmbH. ALL RIGHTS RESERVED.
 *
 * THIS FILE CONTAINS PROPRIETARY SOURCE CODE OF Ericsson GmbH. THIS FILE IS
 * SUBMITTED TO RECIPIENT IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT
 * BE USED, COPIED OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY
 * WRITTEN AGREEMENT SIGNED BY AN AUTHORIZED PERSON OF Ericsson GmbH.
 *
 */
package com.jeefix.jpa.acl;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.Serializable;

/**
 *
 * <p>
 * Created by Maciej Iskra (emacisk) on 2017-10-05.
 */

@NoRepositoryBean
public class AclSpringJpaRepository<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements Repository<T, ID>, Serializable {

    public AclSpringJpaRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }


    @Override
    protected <S extends T> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass, Sort sort) {
        Specification<S> restrictionSpec = new AclSpecification<>(spec);
        return super.getQuery(restrictionSpec, domainClass, sort);
    }
}


