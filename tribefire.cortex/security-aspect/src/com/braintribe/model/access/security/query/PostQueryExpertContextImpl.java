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

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static com.braintribe.utils.lcd.CollectionTools2.size;

import java.util.Collections;
import java.util.Set;

import com.braintribe.model.access.security.cloning.IlsExpertsRegistry;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.pr.criteria.CriterionType;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.pr.criteria.matching.StandardMatcher;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.security.query.context.EntityExpertContext;
import com.braintribe.model.processing.security.query.context.PropertyExpertContext;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.query.Query;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.model.security.acl.AclTools;

public class PostQueryExpertContextImpl implements EntityExpertContext, PropertyExpertContext, Matcher {

	private final PersistenceGmSession session;
	private final IlsExpertsRegistry ilsMatcherRegistry;
	private final Matcher tcMatcher;

	private GenericEntity entity;
	private PropertyRelatedModelPathElement propertyRelatedModelPathElement;
	private ModelMdResolver modelMdResolver;
	private final boolean needsHasAclChecks;

	public PostQueryExpertContextImpl(QueryInterceptorContext<?, ?> qiContext, IlsExpertsRegistry ilsMatcherRegistry) {
		this.ilsMatcherRegistry = ilsMatcherRegistry;
		this.session = qiContext.getSession();
		this.tcMatcher = tcMatcher(qiContext);
		this.needsHasAclChecks = AclTools.supportsAcl(session) && !AclTools.isHasAclAdministrable(session);
	}

	private static Matcher tcMatcher(QueryInterceptorContext<?, ?> qiContext) {
		TraversingCriterion tc = qiContext.getOriginalQuery().getTraversingCriterion();
		if (tc == null || TC.containsPlaceholder(tc))
			return null;

		tc = extendIfSelectQueryResult(tc, qiContext);

		StandardMatcher result = new StandardMatcher();
		result.setCriterion(tc);
		return result;
	}

	private static TraversingCriterion extendIfSelectQueryResult(TraversingCriterion tc, QueryInterceptorContext<?, ?> qiContext) {
		Query q = qiContext.getOriginalQuery();
		if (!(q instanceof SelectQuery))
			return tc;

		SelectQuery sq = (SelectQuery) q;
		if (hasOneDimensionalResult(sq))
			return tc;

		// @formatter:off
		return TC.create().conjunction() //
				.criterion(tc)
				.negation()
					.pattern()
						.root()
						.listElement()
						.entity() // ListRecord
						.property()
					.close()
			.close().done();
		// @formatter:on
	}

	private static boolean hasOneDimensionalResult(SelectQuery sq) {
		switch (size(sq.getSelections())) {
			case 0:
				return size(sq.getFroms()) == 1 && //
						isEmpty(first(sq.getFroms()).getJoins());
			case 1:
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean matches(TraversingContext tc) {
		// If we are at an absent property, match would mean null is set as property value
		// Null is also handled this way -> it makes no sense matching anyway and null check is simpler
		if (isNullOrAbsentProperty(tc))
			return false;

		return (tcMatcher != null && tcMatcher.matches(tc)) //
				|| ilsMatcherRegistry.matches(this, tc);
	}

	private boolean isNullOrAbsentProperty(TraversingContext tc) {
		if (tc.getCurrentCriterionType() != CriterionType.PROPERTY)
			return false;

		return tc.getObjectStack().peek() == null;
	}

	@Override
	public PersistenceGmSession getSession() {
		return session;
	}

	@Override
	public ModelMdResolver getMetaData() {
		if (modelMdResolver == null)
			modelMdResolver = session.getModelAccessory().getMetaData();

		return modelMdResolver;
	}

	public boolean needsHasAclChecks() {
		return needsHasAclChecks;
	}

	@Override
	public Set<String> getRoles() {
		SessionAuthorization sessionAuthorization = session.getSessionAuthorization();

		return sessionAuthorization == null ? Collections.<String> emptySet() : sessionAuthorization.getUserRoles();
	}

	@Override
	public GenericEntity getEntity() {
		return entity;
	}

	public void setEntity(GenericEntity entity) {
		this.entity = entity;
	}

	@Override
	public PropertyRelatedModelPathElement getPropertyRelatedModelPathElement() {
		return propertyRelatedModelPathElement;
	}

	public void setPropertyRelatedModelPathElement(PropertyRelatedModelPathElement propertyRelatedModelPathElement) {
		this.propertyRelatedModelPathElement = propertyRelatedModelPathElement;
	}

}
