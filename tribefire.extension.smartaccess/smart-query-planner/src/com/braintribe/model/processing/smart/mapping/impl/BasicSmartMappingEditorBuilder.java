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

import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;
import static java.util.Collections.emptyList;

import java.util.List;
import java.util.function.Function;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.processing.meta.editor.BasicModelMetaDataEditor;
import com.braintribe.model.processing.meta.editor.ModelMetaDataEditor;
import com.braintribe.model.processing.meta.oracle.BasicModelOracle;
import com.braintribe.model.processing.meta.oracle.ModelOracle;
import com.braintribe.model.processing.smart.mapping.api.SmartMappingEditor;
import com.braintribe.utils.lcd.CommonTools;

/**
 * @author peter.gazdik
 */
public class BasicSmartMappingEditorBuilder implements SmartMappingEditorBuilder {

	private String globalIdPrefix;
	private GmMetaModel smartModel;
	private List<GmMetaModel> delegateModels = emptyList();
	private Function<EntityType<?>, GenericEntity> entityFactory = EntityType::create;
	private Function<String, GenericEntity> entityLookup = s -> null;

	@Override
	public BasicSmartMappingEditorBuilder globalIdPrefix(String globalIdPrefix) {
		this.globalIdPrefix = globalIdPrefix;
		return this;
	}

	@Override
	public BasicSmartMappingEditorBuilder smartModel(GmMetaModel smartModel) {
		this.smartModel = smartModel;
		return this;
	}

	@Override
	public BasicSmartMappingEditorBuilder delegateModels(List<GmMetaModel> delegateModels) {
		this.delegateModels = delegateModels;
		return this;
	}

	@Override
	public BasicSmartMappingEditorBuilder entityFactory(Function<EntityType<?>, GenericEntity> entityFactory) {
		this.entityFactory = entityFactory;
		return this;
	}
	
	@Override
	public BasicSmartMappingEditorBuilder entityLookup(Function<String, GenericEntity> entityLookup) {
		this.entityLookup = entityLookup;
		return this;
	}

	@Override
	public SmartMappingEditor build() {
		return new BasicSmartMappingEditor(this);
	}

	// ############################################
	// ## . . . . . . Internal stuff . . . . . . ##
	// ############################################

	/* package */ String globalIdPrefix() {
		return CommonTools.getValueOrDefault(globalIdPrefix, smartModel.getGlobalId());
	}

	/**
	 * Returns a model oracle which contains all the types from smart and all delegate levels, in order to find them
	 * when configuring mapping MD.
	 */
	/* package */ ModelOracle lookupOracle() {
		return new BasicModelOracle(lookupModel());
	}

	private GmMetaModel lookupModel() {
		if (isEmpty(delegateModels))
			return smartModel;

		GmMetaModel lookupModel = GmMetaModel.T.create();
		lookupModel.setName("smart.lookup:" + smartModel.getName());

		lookupModel.getDependencies().add(smartModel);
		lookupModel.getDependencies().addAll(delegateModels);

		return lookupModel;
	}

	/* package */ ModelMetaDataEditor smartModelMdEditor() {
		return BasicModelMetaDataEditor.create(smartModel).withEtityFactory(entityFactory).done();
	}

	/* package */ Function<EntityType<?>, GenericEntity> entityFactory() {
		return entityFactory;
	}

	/* package */ Function<String, GenericEntity> entityLookup() {
		return entityLookup;
	}
	
}
