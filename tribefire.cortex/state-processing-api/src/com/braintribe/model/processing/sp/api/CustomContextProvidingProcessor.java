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
package com.braintribe.model.processing.sp.api;

import com.braintribe.model.generic.GenericEntity;

/**
 * Extension of {@link StateChangeProcessor} with capability of creating a custom context. This method is invoked before
 * any processing begins (before the {@link #onBeforeStateChange(BeforeStateChangeContext)} method) and the instance
 * returned will then be injected into contexts for all "onStateChange" methods.
 * <p>
 * This can be used when some context is needed that should span over more than one manipulation.
 * 
 * @see StateChangeProcessor
 * @see StateChangeContext#getProcessorContext()
 * @deprecated use {@link StateChangeProcessor} directly as it now declared {@link StateChangeProcessor#createProcessorContext()}
 */
public interface CustomContextProvidingProcessor<T extends GenericEntity, C extends GenericEntity> extends StateChangeProcessor<T, C> {

}
