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
package com.braintribe.model.processing.traversing.engine.api.customize;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.engine.impl.walk.ModelWalker;

/**
 * Represents possible customizations for a {@link ModelWalker}
 *
 */
public interface ModelWalkerCustomization {

	/**
	 * Verify if absence Information can be resolved
	 * 
	 * @param context
	 *            The current {@link GmTraversingContext}
	 * @param property
	 *            {@link Property} that is absent
	 * @param entity
	 *            {@link GenericEntity} that holds the property
	 * @param absenceInformation
	 *            {@link AbsenceInformation} that represents the absent property
	 *            in the entity.
	 * @return boolean indicating if {@link AbsenceInformation} can be resolved
	 */
	boolean isAbsenceResolvable(GmTraversingContext context, Property property, GenericEntity entity, AbsenceInformation absenceInformation);

	/**
	 * Replace the {@link TraversingModelPathElement} prior to adjust the
	 * traversing events
	 * 
	 * @param context
	 *            The current {@link GmTraversingContext}
	 * @param pathElement
	 *            The current {@link TraversingModelPathElement} that is being
	 *            visited.
	 * @return The new {@link TraversingModelPathElement} that will be used for
	 *         the enter and exit events
	 */
	TraversingModelPathElement substitute(GmTraversingContext context, TraversingModelPathElement pathElement);

	/**
	 * Verify if absent property can be traversed
	 * 
	 * @return boolean indicating if an absent property could be traversed.
	 */
	boolean traverseAbsentProperty(GenericEntity entity, Property property, GmTraversingContext context, TraversingModelPathElement pathElement);

}
