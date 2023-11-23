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
package com.braintribe.model.processing.service.api;

import com.braintribe.common.attribute.AttributeAccessor;
import com.braintribe.model.generic.eval.EvalContextAspect;
import com.braintribe.model.generic.pr.AbsenceInformation;

/**
 * <p>
 * A {@link ServiceRequestContextAspect} and {@link EvalContextAspect} for propagating whether only local targets are to be taken into account.
 * 
 * <p>
 * This is particularly useful to avoid processing requests through redirecting components (like proxies) when the actual processors are also
 * requested to process such request (e.g.: when the request is fanned out or multicasted).
 * 
 */
public interface LocalOnlyAspect extends ServiceRequestContextAspect<Boolean>, EvalContextAspect<Boolean> {
	AttributeAccessor<Boolean> $ = AttributeAccessor.create(LocalOnlyAspect.class);

	Object absentResult = AbsenceInformation.T.create();

}
