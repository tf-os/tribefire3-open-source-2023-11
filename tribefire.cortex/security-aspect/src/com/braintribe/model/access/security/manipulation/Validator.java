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
package com.braintribe.model.access.security.manipulation;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.braintribe.model.access.security.InterceptorData;
import com.braintribe.model.generic.manipulation.AbsentingManipulation;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ManifestationManipulation;
import com.braintribe.model.generic.manipulation.NormalizedCompoundManipulation;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityContext;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpert;
import com.braintribe.model.processing.security.manipulation.SecurityViolationEntry;
import com.braintribe.model.processing.security.manipulation.ValidationResult;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * Validation implementation used for {@link ApplyManipulationInterceptor}.
 */
class Validator {

	private final Set<? extends ManipulationSecurityExpert> manipulationSecurityExperts;

	public Validator(InterceptorData iData) {
		this.manipulationSecurityExperts = iData.manipulationSecurityExperts;
	}

	public ValidationResult validate(NormalizedCompoundManipulation manipulation, PersistenceGmSession session) {

		ManipulationSecurityContextImpl context = new ManipulationSecurityContextImpl(session, session.getModelAccessory().getCmdResolver());

		/* NOTE: For performance reason we want the outer loop to be through all manipulations, and the inner loop
		 * through experts (because setting current manipulation for ManipulationSecurityContext is a little more
		 * expensive). This however means, that we need some mapping from ManipulationSecurityExpert to it's current
		 * expertContext (if we reversed the order, we would not need it). For this mapping we use the class
		 * ExpertWrapper. */

		Collection<ExpertWrapper> experts = expertWrappersFor(context);
		for (AtomicManipulation atomicManipulation : manipulation.inline()) {
			if (!isSecurityRelated(atomicManipulation))
				continue;

			context.setCurrentManipulation(atomicManipulation);

			for (ExpertWrapper expertWrapper : experts) {
				context.setExpertContext(expertWrapper.expertContext);
				expertWrapper.expert.expose(context);
			}
		}

		for (ExpertWrapper expertWrapper : experts) {
			context.setExpertContext(expertWrapper.expertContext);
			expertWrapper.expert.validate(context);
		}

		List<SecurityViolationEntry> violationEntries = context.getViolationEntries();

		ValidationResult result = ValidationResult.T.create();
		result.setViolationEntries(violationEntries);

		return result;
	}

	private static boolean isSecurityRelated(AtomicManipulation m) {
		return !(m instanceof ManifestationManipulation || m instanceof AbsentingManipulation);
	}

	private Collection<ExpertWrapper> expertWrappersFor(ManipulationSecurityContext context) {
		return manipulationSecurityExperts.stream() //
				.map(expert -> new ExpertWrapper(expert, context)) //
				.collect(Collectors.toList());
	}

	class ExpertWrapper {
		ManipulationSecurityExpert expert;
		Object expertContext;

		public ExpertWrapper(ManipulationSecurityExpert expert, ManipulationSecurityContext context) {
			this.expert = expert;
			this.expertContext = expert.createExpertContext(context);
		}
	}

}
