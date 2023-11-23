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
package com.braintribe.model.processing.manipulation.api;

import com.braintribe.model.generic.manipulation.AtomicManipulation;

/**
 * This was deprecated as it does not seem to be usable at all. Typically the code that deals with manipulations defines
 * it's own extension of ManipulationExpositionContext and this interface would then also have to be duplicated, which
 * IMHO leads to a much more complex hierarchy, without any real benefit. At the time this was marked as deprecated,
 * there were no invocation of this method via this interface (in all cases the method was invoked on the implementation
 * class directly).
 */
@Deprecated
public interface MutableManipulationExpositionContext extends ManipulationExpositionContext {

	void setCurrentManipulation(AtomicManipulation currentManipulation) throws ContextInitializationException;
}
