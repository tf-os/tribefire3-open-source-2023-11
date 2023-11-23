// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package com.braintribe.model.access.security.query;

import static com.braintribe.model.access.security.query.PasswordPropertyTools.getValueToReplacePassword;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.braintribe.exception.AuthorizationException;
import com.braintribe.logging.Logger;
import com.braintribe.model.access.security.InterceptorData;
import com.braintribe.model.acl.HasAcl;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.criteria.BasicCriterion;
import com.braintribe.model.generic.pr.criteria.CriterionType;
import com.braintribe.model.generic.pr.criteria.EntityCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyCriterion;
import com.braintribe.model.generic.reflection.BaseType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.generic.reflection.SimpleType;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.reflection.StrategyOnCriterionMatch;
import com.braintribe.model.meta.data.prompt.Visible;
import com.braintribe.model.meta.data.query.Queryable;
import com.braintribe.model.processing.aop.api.context.AroundContext;
import com.braintribe.model.processing.aop.api.interceptor.AroundInterceptor;
import com.braintribe.model.processing.aop.api.interceptor.InterceptionException;
import com.braintribe.model.processing.aop.api.interceptor.Interceptor;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.extended.MdDescriptor;
import com.braintribe.model.processing.meta.cmd.result.MdResult;
import com.braintribe.model.processing.security.query.context.PostQueryExpertContext;
import com.braintribe.model.processing.security.query.expert.PostQueryExpert;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.query.From;
import com.braintribe.model.query.Join;
import com.braintribe.model.query.PropertyOperand;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.QueryResult;
import com.braintribe.model.query.Source;
import com.braintribe.model.query.conditions.Condition;
import com.braintribe.utils.lcd.CollectionTools;

/** Base class for security query {@link Interceptor}s See {@link #run(AroundContext)} */
public abstract class QueryInterceptor<Q extends Query, R extends QueryResult> implements AroundInterceptor<Q, R> {

	protected static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	private static final Logger log = Logger.getLogger(QueryInterceptor.class);

	private final InterceptorData qiData;

	public QueryInterceptor(InterceptorData data) {
		this.qiData = data;
	}

	/**
	 * A query interceptor makes the following things:
	 * <ol>
	 * <li>Finds all the sources accessed by the query.</li>
	 * <li>Validates that querying the model is OK.</li>
	 * <li>Validates that querying the sources is OK.</li>
	 * <li>Injects "Efficient Transitive Instance Level Security" (ETILS) restrictions into the query.</li>
	 * <li>Executes the query - this must be implemented by the sub-class.</li>
	 * <li>Applies other (Inefficient) Transitive Instance Level Security (ITILS) on the {@link QueryResult} from previous step.</li>
	 * <li>Returns the modified result.</li>
	 * </ol>
	 * 
	 * <h4>Validation</h4>
	 * 
	 * A valid query must fulfill the following conditions:
	 * <ol>
	 * <li>All sources must be queryable - see {@linkplain Queryable}</li>
	 * <li>All sources must be visible - see {@link Visible}</li>
	 * <li>TODO NOT FINISHED In case we have a property query, we perform a separate check that given property owner is accessible</li>
	 * <li>There is no condition on any password property.</li>
	 * </ol>
	 * 
	 * <h4>Miscellaneous</h4>
	 * 
	 * Sources are of two types - explicit, which are {@link From}s and {@link Join}s and implicit, which are described by an explicit source and a
	 * property path ({@link PropertyOperand}).
	 */
	@Override
	public R run(AroundContext<Q, R> aroundContext) throws InterceptionException {
		if (isCurrentUserTrusted(qiData, aroundContext))
			return executeQueryHelper(aroundContext, aroundContext.getRequest());

		QueryInterceptorContext<Q, R> qiContext = new QueryInterceptorContext<>(aroundContext);

		validate(qiContext);

		adjustQuery(qiContext);

		R result = executeQueryHelper(qiContext);

		PasswordReplacer.replacePasswords(qiContext, result);

		applyItils(result, qiContext);

		return result;
	}

	public static boolean isCurrentUserTrusted(InterceptorData qiData, AroundContext<?, ?> aroundContext) {
		return CollectionTools.containsAny(qiData.trustedRoles, getUserRoles(aroundContext));
	}

	private static Set<String> getUserRoles(AroundContext<?, ?> aroundContext) {
		SessionAuthorization sa = aroundContext.getSession().getSessionAuthorization();
		if (sa == null)
			throw new NullPointerException("SessionAuthorization is null. This might happen if the session is not configured properly or "
					+ "if you are using the session in a different thread than where it was created.");

		return sa.getUserRoles();
	}

	/** See "Validation" section of {@link #run(AroundContext)}. */
	private void validate(QueryInterceptorContext<Q, R> qiContext) {
		validateModelIsVisible(qiContext);
		validateSourceEntities(qiContext);
		validateNoPasswordCondition(qiContext);
	}

	private void validateModelIsVisible(QueryInterceptorContext<Q, R> qiContext) {
		boolean visible = qiContext.getMetaData().is(Visible.T);
		if (!visible) {
			logQueryNotAllowed(qiContext.getMetaData().meta(Visible.T), "model is not visible");
			throw new AuthorizationException("Query not allowed as the corresponding model " + qiContext.getModelName() + " is not visible.");
		}
	}

	private void validateSourceEntities(QueryInterceptorContext<Q, R> qiContext) {
		Set<EntityType<?>> sourceEntities = qiContext.querySources.getSourceTypes();

		if (sourceEntities.isEmpty())
			throw new RuntimeException("No sources found in the query.");

		for (EntityType<?> sourceType : sourceEntities)
			visibileAndQueryable(qiContext, sourceType);
	}

	private void visibileAndQueryable(QueryInterceptorContext<Q, R> qiContext, EntityType<?> sourceType) {
		EntityMdResolver contextBuilder = qiContext.getMetaData().entityType(sourceType);

		if (!contextBuilder.is(Visible.T)) {
			logQueryNotAllowed(contextBuilder.meta(Visible.T), "entity is not visible");
			throw new AuthorizationException("Query not allowed. EEntity not visible: " + sourceType.getTypeSignature());
		}

		if (!contextBuilder.is(Queryable.T)) {
			logQueryNotAllowed(contextBuilder.meta(Queryable.T), "entity is not queryable");
			throw new InterceptionException("Query validation failed. Entity not queryable: " + sourceType.getTypeSignature());
		}
	}

	private void logQueryNotAllowed(MdResult<?, ?> mdResult, String msg) {
		MdDescriptor mdDescriptor = mdResult.exclusiveExtended();
		log.debug("Query not allowed as: " + msg + ". MD origin: " + mdDescriptor.origin());
	}

	private void validateNoPasswordCondition(QueryInterceptorContext<Q, R> qiContext) {
		Q query = qiContext.getQueryToRead();

		Condition c = QueryTools.extractCondition(query);

		if (c != null)
			PasswordConditionValidator.validate(c, qiContext.querySources, qiContext.getMetaData());
	}

	/** Adds ETILS conditions to the query */
	private void adjustQuery(QueryInterceptorContext<Q, R> qiContext) {
		SourcesDescriptor querySources = qiContext.querySources;

		List<Condition> securityConditions = newList();

		/* Add ACL and E(T)ILS conditions for EXPLICIT sources */
		for (Map.Entry<Source, EntityType<?>> explicitSource : querySources.explicitSources()) {
			Source source = querySources.sourceToInject(explicitSource.getKey());
			EntityType<?> sourceType = explicitSource.getValue();

			addAcls(securityConditions, qiContext, source, sourceType);
		}

		/* Add ACL and E(T)ILS conditions for IMPLICIT sources */
		for (Map.Entry<PropertyOperand, EntityType<?>> implicitSource : querySources.implicitSources()) {
			PropertyOperand propOperand = implicitSource.getKey();
			EntityType<?> propType = implicitSource.getValue();

			addAcls(securityConditions, qiContext, propOperand, propType);
		}

		if (!securityConditions.isEmpty())
			QueryTools.appendConditions(qiContext.getQueryToEdit(), securityConditions);

		if (qiContext.needsHasAclChecks())
			QueryTools.appendAclLoadingTc(qiContext.getQueryToEdit());
	}

	//
	// ACL
	//

	private void addAcls(List<Condition> conditions, QueryInterceptorContext<Q, R> qiContext, Source source, EntityType<?> sourceType) {
		if (HasAcl.T.isAssignableFrom(sourceType) && qiContext.needsHasAclChecks())
			conditions.add(QueryTools.createAclCondition(source, qiContext.getUserName(), qiContext.getUserRoles()));
	}

	private void addAcls(List<Condition> conditions, QueryInterceptorContext<Q, R> qiContext, PropertyOperand propOperand, EntityType<?> propType) {
		if (HasAcl.T.isAssignableFrom(propType) && qiContext.needsHasAclChecks())
			conditions.add(QueryTools.createAclCondition(propOperand, qiContext.getUserName(), qiContext.getUserRoles()));
	}

	/**
	 * This is overridden by the {@link PropertyQuery} interceptor, which first checks whether the owner is accessible, and if not, returns an empty
	 * result.
	 */
	protected R executeQueryHelper(QueryInterceptorContext<Q, R> qiContext) {
		return executeQueryHelper(qiContext.aroundContext, qiContext.getQueryToRead());
	}

	private R executeQueryHelper(AroundContext<Q, R> aroundContext, Q query) {
		return aroundContext.proceed(query);
	}

	/**
	 * Applies ITILS (Inefficient Transitive Instance Level Security) and any other post-processing of the query result.
	 */
	protected abstract void applyItils(R queryResult, QueryInterceptorContext<Q, R> qiContext);

	/**
	 * Helper method for subclasses. Clones given object respecting the configured ILS constraints.
	 * 
	 * @see PostQueryExpert
	 * @see PostQueryExpertContext
	 */
	protected <T> T cloneSecurely(T object, QueryInterceptorContext<Q, R> qiContext) {
		StandardCloningContext secureCloningContext = newCloningContext(qiContext);
		return cloneSecurely(object, secureCloningContext);
	}

	protected <T> T cloneSecurely(T object, StandardCloningContext secureCloningContext) {
		return (T) BaseType.INSTANCE.clone(secureCloningContext, object, StrategyOnCriterionMatch.partialize);
	}

	private StandardCloningContext newCloningContext(QueryInterceptorContext<Q, R> qiContext) {
		StandardCloningContext ilsCloningContext = new PasswordFilteringCloningContext(qiContext.getMetaData());

		ilsCloningContext.setMatcher(ilsMatcher(qiContext));

		return ilsCloningContext;
	}

	private PostQueryExpertContextImpl ilsMatcher(QueryInterceptorContext<Q, R> qiContext) {
		return new PostQueryExpertContextImpl(qiContext, qiData.ilsExpertRegistry);
	}

	private static class PasswordFilteringCloningContext extends StandardCloningContext {

		private final ModelMdResolver mdResolver;

		public PasswordFilteringCloningContext(ModelMdResolver mdResolver) {
			this.mdResolver = mdResolver;
		}

		@Override
		public Object postProcessCloneValue(GenericModelType propertyType, Object clonedValue) {
			if (!(propertyType instanceof SimpleType))
				return super.postProcessCloneValue(propertyType, clonedValue);

			Stack<BasicCriterion> ts = getTraversingStack();

			BasicCriterion peekCriterion = ts.peek();
			if (peekCriterion.criterionType() != CriterionType.PROPERTY)
				return super.postProcessCloneValue(propertyType, clonedValue);

			Stack<Object> os = getObjectStack();
			GenericEntity entity = (GenericEntity) os.get(os.size() - 2);
			if (entity.getId() == null)
				return super.postProcessCloneValue(propertyType, clonedValue);

			PropertyCriterion pc = (PropertyCriterion) peekCriterion;

			EntityCriterion ec = (EntityCriterion) ts.get(ts.size() - 2);
			EntityType<?> et = typeReflection.getEntityType(ec.getTypeSignature());

			if (PasswordPropertyTools.isPasswordProperty(et, pc.getPropertyName(), mdResolver))
				return getValueToReplacePassword(propertyType);

			return super.postProcessCloneValue(propertyType, clonedValue);
		}
	}
}
