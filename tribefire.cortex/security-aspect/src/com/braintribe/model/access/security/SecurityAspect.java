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
package com.braintribe.model.access.security;

import static com.braintribe.model.security.acl.AclTools.queryAclEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.braintribe.model.access.security.cloning.IlsExpertsRegistry;
import com.braintribe.model.access.security.manipulation.AclAccessibilityInterceptor;
import com.braintribe.model.access.security.manipulation.ApplyManipulationInterceptor;
import com.braintribe.model.access.security.query.QueryInterceptor;
import com.braintribe.model.access.security.query.QueryInterceptorContext;
import com.braintribe.model.acl.AclOperation;
import com.braintribe.model.acl.HasAcl;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.StandardCloningContext;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.aop.api.aspect.AccessAspect;
import com.braintribe.model.processing.aop.api.aspect.AccessJoinPoint;
import com.braintribe.model.processing.aop.api.aspect.PointCutConfigurationContext;
import com.braintribe.model.processing.aop.api.context.AroundContext;
import com.braintribe.model.processing.manipulation.basic.normalization.Normalizer;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpert;
import com.braintribe.model.processing.security.query.PostQueryExpertConfiguration;
import com.braintribe.model.query.EntityQuery;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.query.PropertyQuery;
import com.braintribe.model.query.PropertyQueryResult;
import com.braintribe.model.query.QueryResult;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.query.SelectQueryResult;

/**
 * Aspect that provides security for given AopAccess. This security involves querying and applying manipulations.
 * 
 * <h2>Querying</h2>
 * 
 * There are two types of security checking applied to queries. First the queries are validated, whether they can even be executed (e.g. querying
 * hidden entities is not allowed). If the validation fails, exception is thrown. The second part is "filtering", i.e. reducing the
 * {@link QueryResult} by removing all content that should be hidden (for given user, and/or in general). For details see
 * {@link QueryInterceptor#run(AroundContext)}.
 * <p>
 * The supported query "methods" are {@link AccessJoinPoint#query}, {@link AccessJoinPoint#queryEntities} and {@link AccessJoinPoint}.
 * 
 * <h2>Manipulations</h2>
 * 
 * Checking manipulations is implemented in {@link ApplyManipulationInterceptor}. First we "normalize" the manipulations (see {@link Normalizer} and
 * then we just use each {@link ManipulationSecurityExpert} (configured via {@link #setManipulationSecurityExperts(Set)} to validate all the
 * manipulations. To read more on this validation process see {@link ManipulationSecurityExpert}.
 */
public class SecurityAspect implements AccessAspect {

	private Collection<PostQueryExpertConfiguration> postQueryExpertConfigurations = Collections.emptySet();
	private Set<ManipulationSecurityExpert> manipulationSecurityExperts = Collections.emptySet();
	private Set<String> trustedRoles = Collections.emptySet();

	public void setPostQueryExpertConfigurations(Collection<PostQueryExpertConfiguration> postQueryExpertConfigurations) {
		this.postQueryExpertConfigurations = postQueryExpertConfigurations;
	}

	public void setManipulationSecurityExperts(Set<ManipulationSecurityExpert> manipulationSecurityExperts) {
		this.manipulationSecurityExperts = manipulationSecurityExperts;
	}

	public void setTrustedRoles(Set<String> trustedRoles) {
		this.trustedRoles = trustedRoles;
	}

	@Override
	public void configurePointCuts(PointCutConfigurationContext context) {
		try {
			IlsExpertsRegistry ilsExpertRegistry = new IlsExpertsRegistry(postQueryExpertConfigurations);
			InterceptorData data = new InterceptorData(ilsExpertRegistry, manipulationSecurityExperts, trustedRoles);

			context.addPointCutBinding(AccessJoinPoint.applyManipulation, new AclAccessibilityInterceptor());
			context.addPointCutBinding(AccessJoinPoint.applyManipulation, new ApplyManipulationInterceptor(data));
			context.addPointCutBinding(AccessJoinPoint.query, new SelectQueryInterceptor(data));
			context.addPointCutBinding(AccessJoinPoint.queryEntities, new QueryEntitiesInterceptor(data));
			context.addPointCutBinding(AccessJoinPoint.queryProperties, new QueryPropertiesInterceptor(data));

		} catch (Exception e) {
			throw new RuntimeException("Configuring PointCuts failed.", e);
		}
	}

	private static class SelectQueryInterceptor extends QueryInterceptor<SelectQuery, SelectQueryResult> {
		SelectQueryInterceptor(InterceptorData data) {
			super(data);
		}

		@Override
		protected <T> T cloneSecurely(T object, StandardCloningContext secureCloningContext) {
			// List<?> queryResult = (List<?>) object;
			// return (T) IncrementalAccesses.cloneSelectQueryResults(queryResult, secureCloningContext, StrategyOnCriterionMatch.skip, et -> null);

			return super.cloneSecurely(object, secureCloningContext);
		}

		@Override
		protected void applyItils(SelectQueryResult queryResult, QueryInterceptorContext<SelectQuery, SelectQueryResult> qiContext) {
			List<Object> results = queryResult.getResults();
			results = cloneSecurely(results, qiContext);
			queryResult.setResults(results);
		}
	}

	private static class QueryEntitiesInterceptor extends QueryInterceptor<EntityQuery, EntityQueryResult> {
		QueryEntitiesInterceptor(InterceptorData data) {
			super(data);
		}

		@Override
		protected void applyItils(EntityQueryResult queryResult, QueryInterceptorContext<EntityQuery, EntityQueryResult> qiContext) {
			List<GenericEntity> entities = queryResult.getEntities();
			entities = cloneSecurely(entities, qiContext);
			queryResult.setEntities(entities);
		}
	}

	private static class QueryPropertiesInterceptor extends QueryInterceptor<PropertyQuery, PropertyQueryResult> {
		QueryPropertiesInterceptor(InterceptorData data) {
			super(data);
		}

		@Override
		protected PropertyQueryResult executeQueryHelper(QueryInterceptorContext<PropertyQuery, PropertyQueryResult> qiContext) {
			if (canQueryOwner(qiContext))
				return super.executeQueryHelper(qiContext);
			else
				return emptyResult();
		}

		private boolean canQueryOwner(QueryInterceptorContext<PropertyQuery, PropertyQueryResult> qiContext) {
			PersistentEntityReference ref = qiContext.getQueryToRead().getEntityReference();

			if (!isAclSecured(ref))
				return true;

			HasAcl entity = queryAclEntity(qiContext.aroundContext.getSystemSession(), ref);
			return entity.isOperationGranted(AclOperation.READ, qiContext.getUserName(), qiContext.getUserRoles());
		}

		private boolean isAclSecured(PersistentEntityReference ref) {
			return HasAcl.T.isAssignableFrom(typeReflection.getType(ref.getTypeSignature()));
		}

		private PropertyQueryResult emptyResult() {
			return PropertyQueryResult.T.create();
		}

		@Override
		protected void applyItils(PropertyQueryResult queryResult, QueryInterceptorContext<PropertyQuery, PropertyQueryResult> qiContext) {
			Object propertyValue = queryResult.getPropertyValue();
			propertyValue = cloneSecurely(propertyValue, qiContext);
			queryResult.setPropertyValue(propertyValue);
		}
	}

}
