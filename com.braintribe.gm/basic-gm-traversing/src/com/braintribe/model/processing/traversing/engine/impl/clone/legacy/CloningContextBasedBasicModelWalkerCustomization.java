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
package com.braintribe.model.processing.traversing.engine.impl.clone.legacy;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.processing.traversing.api.GmTraversingContext;
import com.braintribe.model.processing.traversing.api.path.TraversingModelPathElement;
import com.braintribe.model.processing.traversing.engine.impl.walk.BasicModelWalkerCustomization;

/**
 * @author peter.gazdik
 */
public class CloningContextBasedBasicModelWalkerCustomization extends BasicModelWalkerCustomization {

	private final CloningContext cc;

	/**
	 * @param cc
	 *            a CloningContext which is compatible with GMT, most likely something that is explicitly marked as
	 *            {@link GmtCompatibleCloningContext}, or at least follows the same limitations.
	 */
	public CloningContextBasedBasicModelWalkerCustomization(CloningContext cc) {
		this.cc = cc;
	}

	@Override
	public TraversingModelPathElement substitute(GmTraversingContext context, TraversingModelPathElement pathElement) {
		Object value = pathElement.getValue();
		if (!(value instanceof GenericEntity))
			return pathElement;

		GenericEntity substitute = cc.preProcessInstanceToBeCloned((GenericEntity) value);
		if (value != substitute)
			pathElement.substituteValue(substitute);
			
		return pathElement;
	}
}
