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
package tribefire.extension.okta.templates.wire.contract;

import com.braintribe.model.accessdeployment.hibernate.meta.EntityMapping;
import com.braintribe.model.accessdeployment.hibernate.meta.PropertyMapping;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.constraint.MaxLength;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.wire.api.space.WireSpace;

public interface OktaDbMappingsContract extends WireSpace {

	EntityMapping forceMapping();

	PropertyMapping clobProperty();

	MaxLength maxLen2k();

	MaxLength maxLen4k();

	MaxLength maxLen10Meg();

	MaxLength maxLen2GigProperty();

	MaxLength maxLen1k();

	void applyIndex(ModelMetaDataEditor editor, EntityType<?> entityType, String propertyName);

	void applyIndices(ModelMetaDataEditor editor, EntityType<?> entityType, String... propertyNames);

	EntityMapping entityUnmapped();

}
