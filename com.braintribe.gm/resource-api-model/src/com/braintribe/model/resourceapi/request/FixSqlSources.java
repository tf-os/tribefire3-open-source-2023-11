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
package com.braintribe.model.resourceapi.request;

import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

/**
 * <tt>SqlSource</tt>s got a new property <tt>blobId</tt> which must now be set in every sql based access. This request
 * can be used to trigger such an update round for an access. This request is usually triggered automatically and can be
 * removed again as soon as we are confident that all accesses are up to date.
 * 
 * @author Neidhart.Orlich
 *
 */
public interface FixSqlSources extends AccessRequest {

	EntityType<FixSqlSources> T = EntityTypes.T(FixSqlSources.class);

	@Override
	EvalContext<? extends FixSqlSourcesResponse> eval(Evaluator<ServiceRequest> evaluator);

	boolean getForceUpdate();
	void setForceUpdate(boolean forceUpdate);
}
