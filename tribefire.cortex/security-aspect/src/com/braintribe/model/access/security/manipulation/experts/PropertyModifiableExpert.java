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
package com.braintribe.model.access.security.manipulation.experts;

import com.braintribe.model.generic.manipulation.PropertyManipulation;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.EntityReferenceType;
import com.braintribe.model.meta.data.constraint.Modifiable;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityContext;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpert;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpositionContext;
import com.braintribe.model.processing.security.manipulation.SecurityViolationEntry;

/**
 * {@link ManipulationSecurityExpert} for {@link Modifiable} constraint.
 */
public class PropertyModifiableExpert implements ManipulationSecurityExpert {

	@Override
	public Object createExpertContext(ManipulationSecurityContext context) {
		return null;
	}

	@Override
	public void expose(ManipulationSecurityExpositionContext context) {
		EntityReference ref = context.getTargetReference();
		if (ref == null)
			return;

		if (!(context.getCurrentManipulation() instanceof PropertyManipulation))
			return;

		String typeSignature = ref.getTypeSignature();
		String propertyName = context.getTargetPropertyName();
		// TODO FIX - just another example where preliminary entity could have persistent ref if id set by user
		boolean isPreliminary = ref.referenceType() == EntityReferenceType.preliminary;

		if (!context.getCmdResolver().getMetaData() //
				.entityTypeSignature(typeSignature) //
				.property(propertyName) //
				.preliminary(isPreliminary) //
				.is(Modifiable.T)) {

			SecurityViolationEntry violationEntry = SecurityViolationEntry.T.create();

			violationEntry.setCausingManipulation(context.getCurrentManipulation());
			violationEntry.setEntityReference(ref);
			violationEntry.setDescription("[EntityPropertyNotEditable] Property: " + typeSignature + "." + propertyName);
			violationEntry.setPropertyName(propertyName);

			context.addViolationEntry(violationEntry);
		}
	}

	@Override
	public void validate(ManipulationSecurityContext context) {
		// Intentionally left blank
		return;
	}

}
