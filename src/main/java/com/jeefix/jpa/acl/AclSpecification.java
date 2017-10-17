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

import com.jeefix.secuturity.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Applies ACL via Jpa Specification. Original specification if any, is applied as well.
 * <p>
 * Created by Maciej Iskra (emacisk) on 2017-10-10.
 */
//TODO emacisk write unit tests
//TODO emacisk add logging
public class AclSpecification<T> implements Specification<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AclSpecification.class);
    private Specification<T> originalSpec;

    public AclSpecification(Specification<T> originalSpec) {
        this.originalSpec = originalSpec;
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

//        root.fetch("managedElement", JoinType.INNER); TODO add fetching of element as well
        Path<T> restrictedPath = getRestrictedPath(root);

        if (restrictedPath == null) {
            LOG.debug("There are no ACL restrictions for entity {}", root.getJavaType());
            return originalSpec == null ? null : originalSpec.toPredicate(root, query, cb);
        }

        Predicate restrictionPredicate = restrictedPath.in(SecurityContext.getRoles());
        LOG.debug("Applied ACL restriction on path {} with values {}", restrictedPath, SecurityContext.getRoles());

        return originalSpec == null ? restrictionPredicate : cb.and(originalSpec.toPredicate(root, query, cb), restrictionPredicate);
    }

    protected Path<T> getRestrictedPath(Root<T> root) {
        ManagedElementRestricted annotation = (ManagedElementRestricted) root.getJavaType().getAnnotation(ManagedElementRestricted.class);
        if (annotation == null) {
            return null;
        }
        String[] pathChunks = annotation.value().split("\\.");
        Path<T> currentPath = root;
        for (String pathChunk : pathChunks) {
            currentPath = currentPath.get(pathChunk);
        }
        return currentPath;

    }

}
