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
package com.braintribe.gwt.gmview.action.client;

import java.util.Arrays;
import java.util.List;

import com.braintribe.codec.Codec;
import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.codec.registry.client.CodecRegistry;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.client.PropertyBean;
import com.braintribe.gwt.gmview.codec.client.PropertyRelatedCodec;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EnumType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.data.prompt.CopyTextVisible;
import com.braintribe.model.meta.data.prompt.Name;
import com.braintribe.model.meta.data.prompt.VirtualEnumConstant;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.processing.session.api.common.GmSessions;
import com.braintribe.utils.i18n.I18nTools;

/**
 * Action used for copying the value (text) of simple properties to the clipboard.
 * @author michel.docouto
 *
 */
public class CopyTextAction extends ModelAction {
	
	private ModelPath modelPathToCopy;
	private CodecRegistry<String> codecRegistry;
	
	public CopyTextAction() {
		setHidden(true);
		setName(LocalizedText.INSTANCE.copyText());
		setIcon(GmViewActionResources.INSTANCE.clipboard());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
	}
	
	/**
	 * Configure the codec registry.
	 */
	@Required
	public void setCodecRegistry(CodecRegistry<String> codecRegistry) {
		this.codecRegistry = codecRegistry;
	}

	@Override
	protected void updateVisibility() {
		modelPathToCopy = null;
		if (modelPaths == null || modelPaths.size() != 1) {
			setHidden(true, true);
			return;
		}
		
		List<ModelPath> selection = modelPaths.get(0);
		for (ModelPath modelPath : selection) {
			ModelPathElement lastElement = modelPath.last();
			ModelPathElement parentElement = null;
			if (modelPath.size() > 1)
				parentElement = lastElement.getPrevious();
			ModelPathElement useElement = lastElement;
			
			GenericModelType elementType = lastElement.getType();
			GenericModelType modelType = elementType.isCollection() ? ((CollectionType) elementType).getCollectionElementType() : elementType;			
			Object value = modelPath.last().getValue();
			boolean isVirtualEnumConstant = (modelType == VirtualEnumConstant.T); 		

			if (isVirtualEnumConstant) {
				if (parentElement instanceof PropertyRelatedModelPathElement) {
					useElement = parentElement;
					elementType = parentElement.getType();
					modelType = elementType.isCollection() ? ((CollectionType) elementType).getCollectionElementType() : elementType;							
				}
			}
			
			if ((!modelType.isSimple() && !modelType.isEnum() && !modelType.isCollection()) || value == null)
				continue;
						
			if ((value instanceof String && ((String) value).isEmpty()) || modelType.getJavaType() == Boolean.class)
				continue;
									
			if (useElement instanceof PropertyRelatedModelPathElement) {
				PropertyRelatedModelPathElement propertyElement = (PropertyRelatedModelPathElement) useElement;
				GenericEntity parentEntity = propertyElement.getEntity();

				ModelMdResolver modelMdResolver = GmSessions.getMetaData(parentEntity).useCase(gmContentView.getUseCase());
				PropertyMdResolver propertyMdResolver = modelMdResolver.entity(parentEntity).property(propertyElement.getProperty());
				if (GMEMetadataUtil.isPropertyPassword(propertyMdResolver) || !propertyMdResolver.is(CopyTextVisible.T))
					continue;
			}
			
			modelPathToCopy = modelPath;
			setHidden(false, true);
			return;
		}
		
		setHidden(true, true);
	}

	@Override
	public void perform(TriggerInfo triggerInfo) {
		try {
			GenericModelType valueType = modelPathToCopy.last().getType();
			Codec<Object, String> renderer = codecRegistry.getCodec(valueType.getJavaType());
			String text = null;
			Object value = modelPathToCopy.last().getValue();
			
			if (renderer == null)
				text = value.toString();
			else {
				if (renderer instanceof PropertyRelatedCodec && modelPathToCopy.last() instanceof PropertyRelatedModelPathElement) {
					PropertyRelatedModelPathElement propertyElement = (PropertyRelatedModelPathElement) modelPathToCopy.last();
					PropertyRelatedCodec propertyRelatedCodec = (PropertyRelatedCodec) renderer;
					propertyRelatedCodec.configureModelMdResolver(gmContentView
							.getGmSession()
							.getModelAccessory()
							.getMetaData());
					propertyRelatedCodec.configurePropertyBean(new PropertyBean(propertyElement.getProperty().getName(),
							propertyElement.getEntity(), null));
					propertyRelatedCodec.configureUseCase(gmContentView.getUseCase());
				}
				
				text = renderer.encode(value);
			}
			
			if (text == null && valueType instanceof EnumType) {
				String enumString = value.toString();
				Name displayInfo = GMEMetadataUtil.getName(valueType,
						value,
						gmContentView
							.getGmSession()
							.getModelAccessory()
							.getMetaData(),
						gmContentView
							.getUseCase());
				if (displayInfo != null && displayInfo.getName() != null)
					enumString = I18nTools.getLocalized(displayInfo.getName());
				text = enumString;
			}
			
			if (text != null && !text.isEmpty())
				ClipboardUtil.copyTextToClipboard(text);
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
	}	
}
