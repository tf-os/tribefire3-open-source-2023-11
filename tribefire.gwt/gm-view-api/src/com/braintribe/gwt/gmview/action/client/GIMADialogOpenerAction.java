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

import static com.braintribe.model.processing.session.api.common.GmSessions.getMetaData;

import java.util.Arrays;
import java.util.List;

import com.braintribe.gwt.action.client.TriggerInfo;
import com.braintribe.gwt.gmview.action.client.resources.GmViewActionResources;
import com.braintribe.gwt.gmview.client.EditEntityActionListener;
import com.braintribe.gwt.gmview.client.ModelAction;
import com.braintribe.gwt.gmview.client.ModelActionPosition;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.i18n.LocalizedString;
import com.braintribe.model.generic.path.ModelPath;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.path.RootPathElement;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.TypeCode;
import com.braintribe.model.meta.selector.KnownUseCase;
import com.google.gwt.user.client.ui.Widget;

/**
 * Action that will open GIMA.
 *
 */
public class GIMADialogOpenerAction extends ModelAction {
	
	private ModelPath modelPath;
	private boolean checkMetadata;
	
	/**
	 * @param checkMetadata - if false, then we won't check if the properties are editable in order to enable the action.
	 */
	public GIMADialogOpenerAction(boolean checkMetadata) {
		setHidden(true);
		setName(LocalizedText.INSTANCE.editEntity());
		setIcon(GmViewActionResources.INSTANCE.edit());
		setHoverIcon(GmViewActionResources.INSTANCE.editBig());
		put(ModelAction.PROPERTY_POSITION, Arrays.asList(ModelActionPosition.ActionBar, ModelActionPosition.ContextMenu));
		this.checkMetadata = checkMetadata;
	}
	
	@Override
	public void perform(TriggerInfo triggerInfo) {
		fireEditEntity();
	}
	
	@Override
	protected void updateVisibility(/*SelectorContext selectorContext*/) {
		modelPath = null;
		if (modelPaths != null && modelPaths.size() == 1) {
			List<ModelPath> selection = modelPaths.get(0);
			for (ModelPath modelPath : selection) {
				if (modelPath == null)
					continue;
				
				Object value = modelPath.last().getValue();
				if (modelPath.last().getType().isEntity() && value != null && !(value instanceof LocalizedString) && isEditable(modelPath)) {
					this.modelPath = modelPath;
					setHidden(false);
					return;
				}
			}
		}
		
		setHidden(true);
	}
	
	private boolean isEditable(ModelPath modelPath) {
		if (!checkMetadata)
			return true;
		
		ModelPathElement last = modelPath.last();
		if (last instanceof PropertyRelatedModelPathElement) {
			PropertyRelatedModelPathElement propertyRelatedModelPathElement = (PropertyRelatedModelPathElement) last;
			GenericEntity entity = propertyRelatedModelPathElement.getEntity();
			Property property = propertyRelatedModelPathElement.getProperty();
			return GMEMetadataUtil.isPropertyEditable(
					getMetaData(entity).entity(entity).property(property.getName()).useCase(KnownUseCase.gimaUseCase.getDefaultValue()), entity);
		} else if (last instanceof RootPathElement) {
			RootPathElement entityElement = (RootPathElement) last;
			Object value = entityElement.getValue();
			GenericModelType type = GMF.getTypeReflection().getType(value);
			if (type.getTypeCode() == TypeCode.entityType) {
				EntityType<GenericEntity> entityType = (EntityType<GenericEntity>) type;
				GenericEntity entity = (GenericEntity) value;
				for (Property p : entityType.getProperties()) {
					boolean editable = GMEMetadataUtil.isPropertyEditable(
							getMetaData(entity).entity(entity).property(p.getName()).useCase(KnownUseCase.gimaUseCase.getDefaultValue()), entity);
					if (editable) {
						// At least one property is editable
						return true;
					}
				}
				
				// We didn't find any editable property.
				return false;
			}
		}
		
		return true;
		
	}

	private void fireEditEntity() {
		EditEntityActionListener listener = getEditEntityActionListener(gmContentView);
		if (listener != null) {
			listener.onEditEntity(modelPath);
		}
	}
	
	private EditEntityActionListener getEditEntityActionListener(Object view) {
		if (view instanceof EditEntityActionListener)
			return (EditEntityActionListener) view;
		else if (view instanceof Widget)
			return getEditEntityActionListener(((Widget) view).getParent());
		
		return null;
	}

}
