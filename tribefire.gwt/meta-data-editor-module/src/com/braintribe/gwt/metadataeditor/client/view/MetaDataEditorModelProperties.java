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
package com.braintribe.gwt.metadataeditor.client.view;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.data.MetaData;
import com.braintribe.model.meta.selector.MetaDataSelector;
import com.google.gwt.editor.client.Editor.Path;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ModelKeyProvider;
import com.sencha.gxt.data.shared.PropertyAccess;

public interface MetaDataEditorModelProperties extends PropertyAccess<MetaDataEditorModel> {

	@Path("name")
	ModelKeyProvider<MetaDataEditorModel> key();

	ValueProvider<MetaDataEditorModel, MetaDataEditorModel> model();

	ValueProvider<MetaDataEditorModel, String> name();

	ValueProvider<MetaDataEditorModel, Object> value();

	ValueProvider<MetaDataEditorModel, MetaData> metaData();
	
	ValueProvider<MetaDataEditorModel, EntityType<?>> entityTypeValue();	

	ValueProvider<MetaDataEditorModel, MetaDataSelector> selector();

	ValueProvider<MetaDataEditorModel, Double> conflictPriority();

	ValueProvider<MetaDataEditorModel, Object> propertyValue();	

	ValueProvider<MetaDataEditorModel, MetaDataEditorModel> declaredModel();

	ValueProvider<MetaDataEditorModel, MetaDataEditorModel> declaredOwner();	
}
