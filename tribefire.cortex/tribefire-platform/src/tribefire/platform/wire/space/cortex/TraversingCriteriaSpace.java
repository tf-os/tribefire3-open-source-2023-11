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
package tribefire.platform.wire.space.cortex;

import static com.braintribe.model.generic.typecondition.TypeConditions.isAssignableTo;
import static com.braintribe.wire.api.util.Maps.entry;
import static com.braintribe.wire.api.util.Maps.map;

import java.util.Map;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.processing.pr.fluent.TC;
import com.braintribe.model.generic.typecondition.TypeConditions;
import com.braintribe.model.generic.typecondition.basic.TypeKind;
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
	private TraversingCriterion cortexDefault() {
		TraversingCriterion bean = 
				TC.create()
					.conjunction()
						.criterion(standard())
						.negation()
							.disjunction()
								.criterion(localizedStringProperty())
								.criterion(localizedString())
								.criterion(resourceSource())
							.close()
					.close()
				.done();
		return bean;
	}

	@Managed
	private TraversingCriterion localizedStringProperty() {
		TraversingCriterion bean = TC.create() //
				.typeCondition(TypeConditions.isAssignableTo(LocalizedString.T)) //
				.done();

		return bean;
	}

	@Managed
	private TraversingCriterion localizedString() {
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
	private TraversingCriterion standard() {
		// @formatter:off
		return TC.create()
				.pattern()
					.entity()
					.conjunction()
						.property()
						.typeCondition(TypeConditions.or(
								TypeConditions.isKind(TypeKind.collectionType), 
								TypeConditions.isKind(TypeKind.entityType)))
					.close()
				.close()
				.done();
		// @formatter:on
	}

	@Managed
	private TraversingCriterion resourceSource() {
		return TC.create()
				.typeCondition(isAssignableTo(ResourceSource.T))
			.done();
	}

}
