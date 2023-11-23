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

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.meta.data.constraint.Deletable;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityContext;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpert;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpositionContext;
import com.braintribe.model.processing.security.manipulation.SecurityViolationEntry;

/**
 * {@link ManipulationSecurityExpert} for {@link Deletable} constraint.
 */
public class EntityDeletionExpert implements ManipulationSecurityExpert {

	GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	@Override
	public Object createExpertContext(ManipulationSecurityContext context) {
		return null;
	}

	@Override
	public void expose(ManipulationSecurityExpositionContext context) {
		if (context.getTargetReference() == null) {
			return;
		}

		if (context.getCurrentManipulationType() == ManipulationType.DELETE) {
			if (!deletable(context.getTargetReference().getTypeSignature(), context.getCmdResolver())) {
				SecurityViolationEntry violationEntry = SecurityViolationEntry.T.create();

				violationEntry.setCausingManipulation(context.getCurrentManipulation());
				violationEntry.setEntityReference(context.getTargetReference());
				violationEntry.setDescription("[EntityNotDeletable] Entity: " + context.getTargetReference().getTypeSignature());

				context.addViolationEntry(violationEntry);
			}
		}
	}

	protected boolean deletable(String typeSignature, CmdResolver cmdResolver) {
		return cmdResolver.getMetaData().entityTypeSignature(typeSignature).is(Deletable.T);
	}

	@Override
	public void validate(ManipulationSecurityContext context) {
		// Intentionally left blank
		return;
	}

}
