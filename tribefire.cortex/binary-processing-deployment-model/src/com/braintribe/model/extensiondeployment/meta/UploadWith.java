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

import java.util.List;

import com.braintribe.model.extensiondeployment.ResourceEnricher;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Legacy aggregation of {@link PersistResourceWith} and something like {@link PreEnrichResourceWith}, with a little strange post enrichers.
 * <p>
 * Originally this was a sub-type of {@link PreEnrichResourceWith}, but that makes it difficult to define default MD, because they are overridden by
 * existing UploadWith instances. Instead, both MD are independent and the resolution looks for {@link PreEnrichResourceWith} first, and falls back to
 * UploadWith.
 */
public interface UploadWith extends PersistResourceWith {

	EntityType<UploadWith> T = EntityTypes.T(UploadWith.class);

	/** @deprecated configure this with a separate MD - {@link PreEnrichResourceWith}. */
	@Deprecated
	List<ResourceEnricher> getPrePersistenceEnrichers();
	@Deprecated
	void setPrePersistenceEnrichers(List<ResourceEnricher> value);

	List<ResourceEnricher> getPostPersistenceEnrichers();
	void setPostPersistenceEnrichers(List<ResourceEnricher> value);

}