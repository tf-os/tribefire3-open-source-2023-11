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
package com.braintribe.model.processing.model.tools;

import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Map;
import java.util.Set;

import com.braintribe.model.generic.reflection.GenericModelException;
import com.braintribe.model.meta.GmCustomType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmEnumConstant;
import com.braintribe.model.meta.GmEnumType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.meta.GmProperty;
import com.braintribe.model.meta.GmType;

/**
 * @deprecated see {@link MetaModelEditor}
 */
@Deprecated
public class MetaModelEditorRegistry {

	private final Map<GmMetaModel, MetaModelEditor> editors = newMap();
	private final Map<String, GmMetaModel> models = newMap();
	private final Map<String, GmType> types = newMap();

	// static class Type

	public MetaModelEditorRegistry(MetaModelEditor metaModelEditor, GmMetaModel metaModel) {
		editors.put(metaModel, metaModelEditor);
	}

	public MetaModelEditor acquireEditorFor(GmCustomType gmCustomType) {
		return acquireEditorFor(gmCustomType.getDeclaringModel());
	}

	public MetaModelEditor acquireEditorForModel(String modelName) {
		if (models.isEmpty()) {
			indexModelsAndTypes();
		}

		GmMetaModel model = models.get(modelName);
		if (model == null) {
			throw new GenericModelException("No model found with name:" + modelName);
		}

		return acquireEditorFor(model);
	}

	public MetaModelEditor acquireEditorFor(GmMetaModel declaringModel) {
		MetaModelEditor result = editors.get(declaringModel);
		if (result == null) {
			result = new MetaModelEditor(declaringModel, this);
			editors.put(declaringModel, result);
		}

		return result;
	}

	public <T extends GmType> T getType(String typeSignature) {
		if (types.isEmpty()) {
			indexModelsAndTypes();
		}

		T result = (T) types.get(typeSignature);
		if (result == null) {
			throw new GenericModelException("No type found with signature: " + typeSignature);
		}

		return result;
	}

	public GmProperty getProperty(String typeSignature, String propertyName) {
		GmEntityType gmEntityType = getType(typeSignature);
		return MetaModelEditor.getProperty(gmEntityType, propertyName);
	}

	public GmEnumConstant getConstant(String typeSignature, String constantName) {
		GmEnumType gmEnumType = getType(typeSignature);
		return MetaModelEditor.getConstant(gmEnumType, constantName);
	}

	private void indexModelsAndTypes() {
		GmMetaModel metaModel = first(editors.keySet());
		index(metaModel, newSet());

		if (types.isEmpty()) {
			throw new GenericModelException("No types in model or it's dependencies found!");
		}
	}

	private void index(GmMetaModel model, Set<GmMetaModel> visited) {
		if (!visited.add(model)) {
			return;
		}

		models.put(model.getName(), model);

		for (GmType gmType : model.getTypes()) {
			types.put(gmType.getTypeSignature(), gmType);
		}

		for (GmMetaModel dependencyModel : model.getDependencies()) {
			index(dependencyModel, visited);
		}
	}

}
