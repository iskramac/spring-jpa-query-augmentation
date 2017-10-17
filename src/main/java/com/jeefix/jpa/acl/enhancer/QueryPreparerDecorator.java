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

import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.JpaQueryCreator;
import org.springframework.data.jpa.repository.query.ParameterBinder;
import org.springframework.data.repository.query.ParametersParameterAccessor;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Creates decorator for QueryPreparer. Required as target object has protected access modifier and is not accessible from our context
 * <p>
 * Created by Maciej Iskra (emacisk) on 2017-10-17.
 */
//TODO emacisk write unit tests
public class QueryPreparerDecorator {

    private final Object target;
    private final Class<? extends Object> targetClass;

    public QueryPreparerDecorator(Class<?> targetClass, Object target) {
        this.target = target;
        this.targetClass = targetClass;
    }

    public JpaQueryCreator createCreator(ParametersParameterAccessor accessor,
                                         PersistenceProvider persistenceProvider) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = targetClass.getDeclaredMethod("createCreator", ParametersParameterAccessor.class, PersistenceProvider.class);
        return (JpaQueryCreator) invoke(method, new Object[]{accessor, persistenceProvider});
    }

    public TypedQuery<?> createQuery(CriteriaQuery<?> criteriaQuery) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = targetClass.getDeclaredMethod("createQuery", CriteriaQuery.class);
        return (TypedQuery<?>) invoke(method, new Object[]{criteriaQuery});
    }

    public ParameterBinder getBinder(Object[] values, List expressions) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = targetClass.getDeclaredMethod("getBinder", Object[].class, List.class);
        return (ParameterBinder) invoke(method, new Object[]{values, expressions});
    }

    public Query restrictMaxResultsIfNecessary(Query query) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method method = targetClass.getDeclaredMethod("restrictMaxResultsIfNecessary", Query.class);
        return (Query) invoke(method, new Object[]{query});
    }

    protected Object invoke(Method method, Object[] arguments) throws InvocationTargetException, IllegalAccessException {
        method.setAccessible(true);
        return method.invoke(target, arguments);
    }

}
