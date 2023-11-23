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
package com.braintribe.model.processing.vde.evaluator.impl.bvd.context;

import java.util.List;
import java.util.stream.Collectors;

import com.braintribe.logging.Logger;
import com.braintribe.model.bvd.context.ModelPathElementAddressing;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.MapValuePathElement;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.path.api.ModelPathElementType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.path.GmListItemPathElement;
import com.braintribe.model.path.GmMapKeyPathElement;
import com.braintribe.model.path.GmMapValuePathElement;
import com.braintribe.model.path.GmModelPath;
import com.braintribe.model.path.GmModelPathElement;
import com.braintribe.model.path.GmPropertyPathElement;
import com.braintribe.model.path.GmPropertyRelatedPathElement;
import com.braintribe.model.path.GmRootPathElement;
import com.braintribe.model.path.GmSetItemPathElement;
import com.braintribe.model.processing.vde.evaluator.api.ValueDescriptorEvaluator;
import com.braintribe.model.processing.vde.evaluator.api.VdeContext;
import com.braintribe.model.processing.vde.evaluator.api.VdeResult;
import com.braintribe.model.processing.vde.evaluator.api.VdeRuntimeException;
import com.braintribe.model.processing.vde.evaluator.api.aspects.RootModelPathAspect;
import com.braintribe.model.processing.vde.evaluator.api.aspects.SelectedModelPathsAspect;
import com.braintribe.model.processing.vde.evaluator.impl.VdeResultImpl;


/**
 * {@link ValueDescriptorEvaluator} for {@link com.braintribe.model.bvd.context.ModelPath}
 * 
 */
public class ModelPathVde implements ValueDescriptorEvaluator<com.braintribe.model.bvd.context.ModelPath> {

	private static final Logger logger = Logger.getLogger(ModelPathVde.class);
	
	@Override
	public VdeResult evaluate(VdeContext context, com.braintribe.model.bvd.context.ModelPath valueDescriptor) throws VdeRuntimeException {
		
		ModelPathElementAddressing addressing = valueDescriptor.getAddressing();
		boolean useSelection = valueDescriptor.getUseSelection();
		int offset = valueDescriptor.getOffset();
		
		Object value = null;
		if (useSelection) {
			List<ModelPath> modelPaths = context.get(SelectedModelPathsAspect.class);
			if (modelPaths != null && !modelPaths.isEmpty()) {
				List<Object> values = 
					modelPaths
					.stream()
					.map(p -> extractValue(p, addressing, offset))
					.collect(Collectors.toList());
				value = (values.size() == 1) ? values.get(0) : values;
			}
		} else {
			ModelPath modelPath = context.get(RootModelPathAspect.class);
			value = extractValue(modelPath, addressing, offset);
		}
		
		logger.debug("Returning value: "+value+" for ModelPath VD with addressing: "+valueDescriptor.getAddressing());
		return new VdeResultImpl(value, false);
	}

	private Object extractValue(ModelPath modelPath, ModelPathElementAddressing addressing, int offset) {
		Object value = null;
		if(modelPath != null) {
			ModelPathElement element = null;
			int size = modelPath.size();
			switch(addressing) {
				case first:
					if (offset < size) {
						element = modelPath.get(offset);
					}
					break;
				case last:
					if (offset < size) {
						element = modelPath.get(size-offset-1);
					}
					break;
				case full:
					value = convertToGmModelPath(modelPath);
					break;
			}
			
			if (element != null) {
				logger.debug("Addressed element from model path is of type: "+element.getType()+" and value: "+element.getValue());
				value = element.getValue();
			}
		}
		return value;
	}
	
	private GmModelPath convertToGmModelPath(ModelPath modelPath) {
		GmModelPath gmModelPath = GmModelPath.T.create();
		
		for (ModelPathElement element : modelPath) {
			
			GenericModelType elementType = element.getType();
			Object elementValue = element.getValue();
			GenericEntity entity = null;
			String property = null;
			
			if (element instanceof PropertyRelatedModelPathElement) {
				PropertyRelatedModelPathElement propertyElement = (PropertyRelatedModelPathElement) element;
				entity = propertyElement.getEntity();
				property = propertyElement.getProperty().getName();
			}
			
			ModelPathElementType elementPathType = element.getElementType();
			GmModelPathElement gmElement = null;
			
			switch (elementPathType) {
				case EntryPoint: // TODO: there's no GmEntryPointElement type available in model-path-model.
				case Root:
					gmElement = createGmElement(GmRootPathElement.T, elementType, elementValue);
					break;
				case Property:
					gmElement = createGmElement(GmPropertyPathElement.T, elementType, elementValue, entity, property);
					break;
				case SetItem:
					gmElement = createGmElement(GmSetItemPathElement.T, elementType, elementValue, entity, property);
					break;
				case ListItem:
					gmElement = createGmElement(GmListItemPathElement.T, elementType, elementValue, entity, property);
					break;
				case MapKey:
					gmElement = createGmElement(GmMapKeyPathElement.T, elementType, elementValue, entity, property);
					break;
				case MapValue:
					MapValuePathElement mapValueElement = (MapValuePathElement) element;
					GenericModelType keyType = mapValueElement.getKeyType();
					Object key = mapValueElement.getKey();
					GmMapValuePathElement gmMapValueElement = createGmElement(GmMapValuePathElement.T, elementType, elementValue, entity, property);
					gmMapValueElement.setKeyTypeSignature(keyType.getTypeSignature());
					gmMapValueElement.setKey(key);
					gmElement = gmMapValueElement;
					break;
				default:
					break;
			}
			if (gmElement != null) {
				gmModelPath.getElements().add(gmElement);
			}
		}
		
		return gmModelPath;
	}
	
	private <T extends GmModelPathElement> T createGmElement(EntityType<T> type, GenericModelType elementType, Object elementValue) {
		return createGmElement(type, elementType, elementValue, null, null);
	}
	private <T extends GmModelPathElement> T createGmElement(EntityType<T> type, GenericModelType elementType, Object elementValue, GenericEntity entity, String property) {
		T element = type.create();
		element.setTypeSignature(elementType.getTypeSignature());
		element.setValue(elementValue);
		if (element instanceof GmPropertyRelatedPathElement) {
			GmPropertyRelatedPathElement propertyElement = (GmPropertyRelatedPathElement) element;
			propertyElement.setEntity(entity);
			propertyElement.setProperty(property);
		}
		return element;
	}

}
