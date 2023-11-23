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
package com.braintribe.model.meta.data.fulltext;


import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.data.PropertyMetaData;

/**
 * This MetaData defines how an entity property is persisted within the FullText store.
 * 
 * @author gunther.schenk
 *
 */

public interface StorageHint extends PropertyMetaData, FulltextMetaData {

	EntityType<StorageHint> T = EntityTypes.T(StorageHint.class);

	// @formatter:off
	void setOption(StorageOption option);
	StorageOption getOption();
	// @formatter:on

}
