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
package com.braintribe.model.processing.securityservice.basic.test.wire.space.access;

import static com.braintribe.wire.api.util.Lists.list;
import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;
import static com.braintribe.wire.api.util.Sets.set;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.pr.criteria.PatternCriterion;
import com.braintribe.model.generic.pr.criteria.PropertyTypeCriterion;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.pr.criteria.typematch.CollectionTypeMatch;
import com.braintribe.model.generic.pr.criteria.typematch.EntityTypeMatch;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.resource.source.ResourceSource;
import com.braintribe.wire.api.annotation.Managed;
import com.braintribe.wire.api.space.WireSpace;

// @formatter:off
@Managed
public class TraversingCriteriaSpace implements WireSpace {

	@Managed
	public Map<Class<? extends GenericEntity>, TraversingCriterion> cortexDefaultMap() {
		return 
			map(
				entry(GenericEntity.class, cortexDefault())
			);
	}
	
	@Managed
	public Map<Class<? extends GenericEntity>, TraversingCriterion> defaultMap() {
		return 
			map(
				entry(GenericEntity.class, all())
			);
	}

	@Managed
	public TraversingCriterion cortexDefault() {
		TraversingCriterion bean = 
				TC.create()
					.conjunction()
						.negation()
							.disjunction()
								.criterion(localizedStringProperty())
								.criterion(localizedString())
								.criterion(resourceSource())
							.close()
						.criterion(standard())
					.close()
				.done();
		return bean;
	}

	@Managed
	public TraversingCriterion localizedStringProperty() {
		PropertyTypeCriterion bean = PropertyTypeCriterion.T.create();
		
		EntityTypeMatch match = EntityTypeMatch.T.create();
		match.setTypeSignature(LocalizedString.T.getTypeSignature());
		
		bean.setTypes(set(match));
		
		return bean;
	}

	@Managed
	public TraversingCriterion localizedString() {
		TraversingCriterion bean = 
				TC.create()
					.pattern()
						.entity(LocalizedString.class)
						.property(LocalizedString.localizedValues)
					.close()
				.done();
		return bean;
	}

	@Managed
	public TraversingCriterion standard() {

		PropertyTypeCriterion ptc = PropertyTypeCriterion.T.create();
		ptc.setTypes(
				set(
					EntityTypeMatch.T.create(),
					CollectionTypeMatch.T.create()
				)
			);

		PatternCriterion bean = PatternCriterion.T.create();
		bean.setCriteria(
				list(
					com.braintribe.model.generic.pr.criteria.EntityCriterion.T.create(),
					ptc
				)
			);
		return bean;

	}

	@Managed
	public TraversingCriterion resourceSource() {
		TraversingCriterion bean = 
				TC.create().typeCondition(TypeConditions.isType(ResourceSource.class.getName())).done();
		return bean;
	}

	@Managed
	public TraversingCriterion all() {
		TraversingCriterion bean = TC.create().negation().joker().done();
		return bean;
	}

}
