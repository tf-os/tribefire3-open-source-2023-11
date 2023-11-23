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
package com.braintribe.gm.model.marshaller.api.request;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

/**
 * UnmarshallRequest is the base type for all request types that are intended to unmarshall (deserialize) a {@link Resource} into data based on a marshaller that is 
 * either selected by the {@link Resource#getMimeType()} property if given or a mimetype detection on the actual contents of the {@link Resource}.
 * @author Dirk Scheffler
 */
@Abstract
public interface UnmarshallRequest extends AbstractMarshallRequest {
	EntityType<UnmarshallRequest> T = EntityTypes.T(UnmarshallRequest.class);

	/**
	 * The {@link Resource} that will be should be unmarshalled. If given, its {@link Resource#getMimeType()} will be used to select a marshaller. Otherwise the mimetype will be detected from
	 * the contents of the {@link Resource}.  
	 */
	@Mandatory
	Resource getResource();
	void setResource(Resource resource);
}
