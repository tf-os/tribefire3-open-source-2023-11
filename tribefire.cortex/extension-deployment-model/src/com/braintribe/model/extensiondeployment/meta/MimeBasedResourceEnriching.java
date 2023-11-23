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
package com.braintribe.model.extensiondeployment.meta;

import java.util.Set;

import com.braintribe.model.extensiondeployment.ResourceEnricher;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.ModelMetaData;

/**
 * Special meta-data resolved by MimeBasedDispatchingResourceEnricher - a ResourceEnricher which handles an EnrichResource request by finding other
 * enrichers based on this MD.
 * <p>
 * This MD should be configured on the data model of an access.
 * <p>
 * Of course, this can only works as long as the mime type is present on given Resource, i.e. if it was specified or already enriched by a previous
 * enricher.
 */
public interface MimeBasedResourceEnriching extends ModelMetaData {

	EntityType<MimeBasedResourceEnriching> T = EntityTypes.T(MimeBasedResourceEnriching.class);

	ResourceEnricher getResourceEnricher();
	void setResourceEnricher(ResourceEnricher resourceEnricher);

	Set<String> getMimeTypes();
	void setMimeTypes(Set<String> mimeTypes);

}