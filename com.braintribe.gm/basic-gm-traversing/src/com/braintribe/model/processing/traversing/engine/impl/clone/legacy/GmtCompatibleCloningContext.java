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
import com.braintribe.model.generic.pr.criteria.TraversingCriterion;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.processing.traversing.engine.GMT;

/**
 * Marks a CloningContext to be compatible with gm-traversing. This means the {@link GMT} based cloning can use the
 * {@link CloningContextBasedClonerCustomization}.
 * <p>
 * Not every {@link CloningContext} implementation can be used in GMT. ClongingContext typically has access to a stack of values and
 * {@link TraversingCriterion}s which are not accessible in the GMT context. So any Cc implementation that accesses them is not GMT compatible. The
 * only exception is to check the peek TraversingCriterion, which is set in case of a property (as that is the only way to find out which property is being cloned). 
 * 
 * Also, the {@link CloningContext#preProcessInstanceToBeCloned(GenericEntity)} must return the same instance it gets as an argument. 
 */
public interface GmtCompatibleCloningContext extends CloningContext {
	// empty
}
