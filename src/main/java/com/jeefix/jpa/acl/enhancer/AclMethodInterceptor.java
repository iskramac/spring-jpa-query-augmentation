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
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.JpaQueryCreator;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.ParametersParameterAccessor;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Method;

/**
 * Interceptor which enhances given createQuery method with ACL predicate
 * <p>
 * Created by Maciej Iskra (emacisk) on 2017-10-17.
 */
//TODO emacisk write unit tests
public class AclMethodInterceptor implements MethodInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(AclMethodInterceptor.class);
    private static final String INTERCEPTED_METHOD_NAME = "createQuery";
    private RepositoryMetadata metadata;
    private ProjectionFactory factory;
    private JpaQueryMethod jpaQueryMethod;
    private EntityManager em;
    private PersistenceProvider persistenceProvider;
    private QueryPreparerDecorator queryPreparer;

    public AclMethodInterceptor(JpaQueryMethod jpaQueryMethod, EntityManager em, ProjectionFactory factory, RepositoryMetadata metadata, Object queryPreparerOriginal) {
        this.jpaQueryMethod = jpaQueryMethod;
        this.em = em;
        this.factory = factory;
        this.metadata = metadata;
        persistenceProvider = PersistenceProvider.fromEntityManager(em);
        this.queryPreparer = new QueryPreparerDecorator(queryPreparerOriginal.getClass(), queryPreparerOriginal);
        ;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] arguments, MethodProxy proxy) throws Throwable {
        if (INTERCEPTED_METHOD_NAME.equals(method.getName()) == false) {
            LOG.trace("Method name '{}' is not supported by ACL interceptor ", method.getName());
            return proxy.invokeSuper(obj, arguments);
        }

        arguments = (Object[]) arguments[0];


        JpaQueryCreator creator = queryPreparer.createCreator(new ParametersParameterAccessor(jpaQueryMethod.getParameters(), arguments), persistenceProvider);
        Sort orders = jpaQueryMethod.getParameters().potentiallySortsDynamically() ? new ParametersParameterAccessor(jpaQueryMethod.getParameters(), arguments).getSort()
                : null;
        CriteriaQuery<?> criteriaQuery = creator.createQuery(orders);
        Root<Object> root = (Root<Object>) criteriaQuery.getRoots().iterator().next();

        //install predicate
        Predicate aclPredicate = new AclSpecification<>(null).toPredicate(root, criteriaQuery, em.getCriteriaBuilder());
        criteriaQuery.where(em.getCriteriaBuilder().and(criteriaQuery.getRestriction(), aclPredicate));

        Query query = queryPreparer.restrictMaxResultsIfNecessary(
                queryPreparer.getBinder(
                        arguments, creator.getParameterExpressions()).bindAndPrepare(
                        queryPreparer.createQuery(criteriaQuery)));
        LOG.debug("Successfully enhanced query with ACL predicate");
        return query;
    }
}
