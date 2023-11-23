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
package com.braintribe.model.processing.traversing.engine.impl.walk;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.pr.AbsenceInformation;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.engine.api.customize.ModelWalkerCustomization;

public class BasicModelWalkerCustomization implements ModelWalkerCustomization {

	/**
	 * By default, absence info will not be resolved.
	 * 
	 * @see #isAbsenceResolvable
	 */
	private boolean absenceResolvable = false;

	/**
	 * By default, absence info will not be traversed.
	 * 
	 * @see #isAbsenceResolvable
	 */
	private boolean absenceTraversable = false;

	@Override
	public boolean isAbsenceResolvable(GmTraversingContext context, Property property, GenericEntity entity,
			AbsenceInformation absenceInformation) {
		return this.absenceResolvable;
	}

	/** Sets a fixed result for {@link #isAbsenceResolvable} (unless overridden). */
	public void setAbsenceResolvable(boolean absenceResolvable) {
		this.absenceResolvable = absenceResolvable;
	}

	@Override
	public TraversingModelPathElement substitute(GmTraversingContext context, TraversingModelPathElement pathElement) {
		return pathElement;
	}

	@Override
	public boolean traverseAbsentProperty(GenericEntity entity, Property property, GmTraversingContext context,
			TraversingModelPathElement pathElement) {
		return absenceTraversable;
	}

	/** Sets a fixed result for {@link #traverseAbsentProperty} (unless overridden). */
	public void setAbsenceTraversable(boolean absenceTraversable) {
		this.absenceTraversable = absenceTraversable;
	}
}
