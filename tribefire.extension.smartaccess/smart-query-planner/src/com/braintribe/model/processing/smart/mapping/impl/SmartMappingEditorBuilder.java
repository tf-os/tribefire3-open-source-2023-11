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
package com.braintribe.model.processing.smart.mapping.impl;

import java.util.List;
import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.smart.mapping.api.SmartMappingEditor;

/**
 * @author peter.gazdik
 */
public interface SmartMappingEditorBuilder {

	BasicSmartMappingEditorBuilder globalIdPrefix(String globalIdPrefix);

	SmartMappingEditorBuilder smartModel(GmMetaModel smartModel);

	/**
	 * We do not have to specify all delegates. The only important thing is that the smart model and all here stated
	 * delegates cover all the possible types we are referring to in our mappings.
	 */
	SmartMappingEditorBuilder delegateModels(List<GmMetaModel> delegateModels);

	SmartMappingEditorBuilder entityFactory(Function<EntityType<?>, GenericEntity> entityFactory);

	/**
	 * Lookup for entities based on their globalId.
	 */
	SmartMappingEditorBuilder entityLookup(Function<String, GenericEntity> entityLookup);

	SmartMappingEditor build();

}
