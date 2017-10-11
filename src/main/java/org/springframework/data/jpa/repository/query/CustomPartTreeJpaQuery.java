

package org.springframework.data.jpa.repository.query;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.jeefix.jpa.acl.AclPredicateTargetSource;
import com.jeefix.jpa.acl.AclSpecification;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.JpaQueryExecution.DeleteExecution;
import org.springframework.data.jpa.repository.query.JpaQueryExecution.ExistsExecution;
import org.springframework.data.jpa.repository.query.ParameterMetadataProvider.ParameterMetadata;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.query.ResultProcessor;
import org.springframework.data.repository.query.parser.PartTree;

/**
 * A {@link AbstractJpaQuery} implementation based on a {@link PartTree}.
 *
 * @author Oliver Gierke
 * @author Thomas Darimont
 */
public class CustomPartTreeJpaQuery extends AbstractJpaQuery {

    private final Class<?> domainClass;
    private final PartTree tree;
    private final JpaParameters parameters;

    private final QueryPreparer query;
    private final QueryPreparer countQuery;
    private final EntityManager em;

    /**
     * Creates a new {@link PartTreeJpaQuery}.
     *
     * @param method must not be {@literal null}.
     * @param factory must not be {@literal null}.
     * @param em must not be {@literal null}.
     */
    public CustomPartTreeJpaQuery(JpaQueryMethod method, EntityManager em, PersistenceProvider persistenceProvider) {

        super(method, em);

        this.em = em;
        this.domainClass = method.getEntityInformation().getJavaType();
        this.tree = new PartTree(method.getName(), domainClass);
        this.parameters = method.getParameters();

        boolean recreationRequired = parameters.hasDynamicProjection() || parameters.potentiallySortsDynamically();

        this.countQuery = new CountQueryPreparer(persistenceProvider, recreationRequired);
        this.query = tree.isCountProjection() ? countQuery : new QueryPreparer(persistenceProvider, recreationRequired);




    }




    /*
     * (non-Javadoc)
     * @see org.springframework.data.jpa.repository.query.AbstractJpaQuery#doCreateQuery(java.lang.Object[])
     */
    @Override
    public Query doCreateQuery(Object[] values) {
        return query.createQuery(values);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.jpa.repository.query.AbstractJpaQuery#doCreateCountQuery(java.lang.Object[])
     */
    @Override
    @SuppressWarnings("unchecked")
    public TypedQuery<Long> doCreateCountQuery(Object[] values) {
        return (TypedQuery<Long>) countQuery.createQuery(values);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.jpa.repository.query.AbstractJpaQuery#getExecution()
     */
    @Override
    protected JpaQueryExecution getExecution() {

        if(this.tree.isDelete()) {
            return new JpaQueryExecution.DeleteExecution(em);
        } else if(this.tree.isExistsProjection()) {
            return new JpaQueryExecution.ExistsExecution();
        }

        return super.getExecution();
    }

    /**
     * Query preparer to create {@link CriteriaQuery} instances and potentially cache them.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private class QueryPreparer {

        private final CriteriaQuery<?> cachedCriteriaQuery;
        private final List<ParameterMetadataProvider.ParameterMetadata<?>> expressions;
        private final PersistenceProvider persistenceProvider;

        public QueryPreparer(PersistenceProvider persistenceProvider, boolean recreateQueries) {

            this.persistenceProvider = persistenceProvider;

            JpaQueryCreator creator = createCreator(null, persistenceProvider);

            this.cachedCriteriaQuery = recreateQueries ? null : creator.createQuery();
            this.expressions = recreateQueries ? null : creator.getParameterExpressions();
        }

        /**
         * Creates a new {@link Query} for the given parameter values.
         *
         * @param values
         * @return
         */
        public Query createQuery(Object[] values) {

            CriteriaQuery<?> criteriaQuery = cachedCriteriaQuery;
            List<ParameterMetadataProvider.ParameterMetadata<?>> expressions = this.expressions;
            ParametersParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);

            if (cachedCriteriaQuery == null || accessor.hasBindableNullValue()) {
                JpaQueryCreator creator = createCreator(accessor, persistenceProvider);
                criteriaQuery = creator.createQuery(getDynamicSort(values));
                expressions = creator.getParameterExpressions();
            }

            TypedQuery<?> jpaQuery = createQuery(criteriaQuery);

            return restrictMaxResultsIfNecessary(invokeBinding(getBinder(values, expressions), jpaQuery));
        }

        /**
         * Restricts the max results of the given {@link Query} if the current {@code tree} marks this {@code query} as
         * limited.
         *
         * @param query
         * @return
         */
        private Query restrictMaxResultsIfNecessary(Query query) {

            if (tree.isLimiting()) {

                if (query.getMaxResults() != Integer.MAX_VALUE) {
					/*
					 * In order to return the correct results, we have to adjust the first result offset to be returned if:
					 * - a Pageable parameter is present
					 * - AND the requested page number > 0
					 * - AND the requested page size was bigger than the derived result limitation via the First/Top keyword.
					 */
                    if (query.getMaxResults() > tree.getMaxResults() && query.getFirstResult() > 0) {
                        query.setFirstResult(query.getFirstResult() - (query.getMaxResults() - tree.getMaxResults()));
                    }
                }

                query.setMaxResults(tree.getMaxResults());
            }

            if(tree.isExistsProjection()) {
                query.setMaxResults(1);
            }

            return query;
        }

        /**
         * Checks whether we are working with a cached {@link CriteriaQuery} and synchronizes the creation of a
         * {@link TypedQuery} instance from it. This is due to non-thread-safety in the {@link CriteriaQuery} implementation
         * of some persistence providers (i.e. Hibernate in this case), see DATAJPA-396.
         *
         * @param criteriaQuery must not be {@literal null}.
         * @return
         */
        private TypedQuery<?> createQuery(CriteriaQuery<?> criteriaQuery) {

            if (this.cachedCriteriaQuery != null) {
                synchronized (this.cachedCriteriaQuery) {
                    return getEntityManager().createQuery(criteriaQuery);
                }
            }
            CriteriaQuery criteriaQuery1 = wrapQueryInAcl(criteriaQuery);
            return getEntityManager().createQuery(criteriaQuery1);
        }

        public CriteriaQuery wrapQueryInAcl(CriteriaQuery cachedCriteriaQuery) {
            // retrieve acl specification
            Specification<Object> aclJpaSpec = new AclSpecification<>(null);

            synchronized (cachedCriteriaQuery) {
                // force rerender by resetting alias
                Root<Object> root = (Root<Object>) cachedCriteriaQuery.getRoots().iterator().next();
                root.alias(null);

                // build acl predicate
                Predicate aclPredicate =
                        aclJpaSpec.toPredicate(root, cachedCriteriaQuery, em.getCriteriaBuilder());

                // install acl predicate
                AclPredicateTargetSource aclPredicateTargetSource = installAclPredicateTargetSource(cachedCriteriaQuery);
                aclPredicateTargetSource.installAcl(aclPredicate);

                try {
                    return cachedCriteriaQuery;
                } finally {
                    if (aclPredicateTargetSource != null) {
//                        aclPredicateTargetSource.uninstallAcl();

                    }
                }
            }
        }

        private AclPredicateTargetSource installAclPredicateTargetSource(CriteriaQuery cachedCriteriaQuery) {
            synchronized (cachedCriteriaQuery) {
                Predicate restriction = cachedCriteriaQuery.getRestriction();

                if (restriction instanceof Advised) {
                    Advised advised = (Advised) restriction;
                    if (advised.getTargetSource() instanceof AclPredicateTargetSource) {
                        return (AclPredicateTargetSource) advised.getTargetSource();
                    }
                }

                AclPredicateTargetSource targetSource = new AclPredicateTargetSource(em.getCriteriaBuilder(), restriction);
                ProxyFactoryBean factoryBean = new ProxyFactoryBean();
                factoryBean.setTargetSource(targetSource);
                factoryBean.setAutodetectInterfaces(true);
                Predicate enhancedPredicate = (Predicate) factoryBean.getObject();


                // install proxy inside criteria
                cachedCriteriaQuery.where(enhancedPredicate);
                return targetSource;
            }
        }



        protected JpaQueryCreator createCreator(ParametersParameterAccessor accessor,
                                                PersistenceProvider persistenceProvider) {

            EntityManager entityManager = getEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();

            ParameterMetadataProvider provider = accessor == null
                    ? new ParameterMetadataProvider(builder, parameters, persistenceProvider)
                    : new ParameterMetadataProvider(builder, accessor, persistenceProvider);

            ResultProcessor resultFactory = getQueryMethod().getResultProcessor().withDynamicProjection(accessor);

            return new JpaQueryCreator(tree, resultFactory.getReturnedType(), builder, provider);
        }

        /**
         * Invokes parameter binding on the given {@link TypedQuery}.
         *
         * @param binder
         * @param query
         * @return
         */
        protected Query invokeBinding(ParameterBinder binder, TypedQuery<?> query) {

            return binder.bindAndPrepare(query);
        }

        private ParameterBinder getBinder(Object[] values, List<ParameterMetadataProvider.ParameterMetadata<?>> expressions) {
            return new CriteriaQueryParameterBinder(parameters, values, expressions);
        }

        private Sort getDynamicSort(Object[] values) {

            return parameters.potentiallySortsDynamically() ? new ParametersParameterAccessor(parameters, values).getSort()
                    : null;
        }
    }

    /**
     * Special {@link QueryPreparer} to create count queries.
     *
     * @author Oliver Gierke
     * @author Thomas Darimont
     */
    private class CountQueryPreparer extends QueryPreparer {

        public CountQueryPreparer(PersistenceProvider persistenceProvider, boolean recreateQueries) {
            super(persistenceProvider, recreateQueries);
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.data.jpa.repository.query.PartTreeJpaQuery.QueryPreparer#createCreator(org.springframework.data.repository.query.ParametersParameterAccessor, org.springframework.data.jpa.provider.PersistenceProvider)
         */
        @Override
        protected JpaQueryCreator createCreator(ParametersParameterAccessor accessor,
                                                PersistenceProvider persistenceProvider) {

            EntityManager entityManager = getEntityManager();
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();

            ParameterMetadataProvider provider = accessor == null
                    ? new ParameterMetadataProvider(builder, parameters, persistenceProvider)
                    : new ParameterMetadataProvider(builder, accessor, persistenceProvider);

            return new JpaCountQueryCreator(tree, getQueryMethod().getResultProcessor().getReturnedType(), builder, provider);
        }

        /**
         * Customizes binding by skipping the pagination.
         *
         * @see org.springframework.data.jpa.repository.query.PartTreeJpaQuery.QueryPreparer#invokeBinding(org.springframework.data.jpa.repository.query.ParameterBinder,
         *      javax.persistence.TypedQuery)
         */
        @Override
        protected Query invokeBinding(ParameterBinder binder, javax.persistence.TypedQuery<?> query) {
            return binder.bind(query);
        }
    }
}
