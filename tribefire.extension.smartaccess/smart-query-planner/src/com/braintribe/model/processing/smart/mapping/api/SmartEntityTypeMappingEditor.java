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
package com.braintribe.model.processing.smart.mapping.api;

import java.util.function.Consumer;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.meta.editor.EntityTypeMetaDataEditor;

/**
 * @author peter.gazdik
 */
public interface SmartEntityTypeMappingEditor {

	SmartEntityTypeMappingEditor forDelegate(String accessId);

	SmartEntityTypeMappingEditor withGlobalIdPart(String globalIdPart);

	SmartEntityTypeMappingEditor addMetaData(Consumer<EntityTypeMetaDataEditor> mdConfigurer);

	/** Maps this entity and all it's properties AsIs */
	SmartEntityTypeMappingEditor allAsIs();

	SmartEntityTypeMappingEditor entityAsIs();

	SmartEntityTypeMappingEditor propertiesAsIs();

	SmartEntityTypeMappingEditor allUnmapped();

	SmartEntityTypeMappingEditor entityUnmapped();

	SmartEntityTypeMappingEditor propertiesUnmapped();

	SmartEntityTypeMappingEditor entityTo(EntityType<?> delegateType);

	SmartEntityTypeMappingEditor entityTo(String typeSignature);

	SmartEntityTypeMappingEditor propertyTo(String smartProperty, String delegateProperty);

	SmartEntityTypeMappingEditor propertyUnmapped(String smartProperty);

	SmartEntityTypeMappingEditor propertiesUnmapped(String... smartProperties);

}
