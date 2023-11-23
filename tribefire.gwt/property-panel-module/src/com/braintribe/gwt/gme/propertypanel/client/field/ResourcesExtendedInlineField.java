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
package com.braintribe.gwt.gme.propertypanel.client.field;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.braintribe.gwt.fileapi.client.FileList;
import com.braintribe.gwt.gme.propertypanel.client.LocalizedText;
import com.braintribe.gwt.gme.propertypanel.client.PropertyPanel;
import com.braintribe.gwt.gme.propertypanel.client.resources.PropertyPanelResources;
import com.braintribe.gwt.gmview.action.client.ActionUtil;
import com.braintribe.gwt.gmview.client.ControllableView;
import com.braintribe.gwt.gmview.client.ResourceUploadView;
import com.braintribe.gwt.gmview.client.ResourceUploadViewListener;
import com.braintribe.gwt.gmview.util.client.GMEMetadataUtil;
import com.braintribe.gwt.ioc.client.Required;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.path.ModelPathElement;
import com.braintribe.model.generic.path.PropertyRelatedModelPathElement;
import com.braintribe.model.generic.reflection.CollectionType;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.meta.data.constraint.Instantiable;
import com.braintribe.model.processing.meta.cmd.builders.EntityMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.ModelMdResolver;
import com.braintribe.model.processing.meta.cmd.builders.PropertyMdResolver;
import com.braintribe.model.resource.Resource;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Widget;

/**
 * Implementation of the extended inline field for Resources.
 * @author michel.docouto
 *
 */
public class ResourcesExtendedInlineField implements ExtendedInlineField {
	
	private ResourceUploadView resourceUploadView;
	private Supplier<Widget> widgetSupplier;
	private PropertyPanel propertyPanel;
	private PropertyRelatedModelPathElement modelPathElement;
	private String oldInnerHtml;
	private ControllableView controllableView;
	
	/**
	 * Configures the required upload view.
	 */
	@Required
	public void setResourceUploadView(ResourceUploadView resourceUploadView) {
		this.resourceUploadView = resourceUploadView;
		
		resourceUploadView.getWidget().getElement().getStyle().setWidth(400, Unit.PX);
		resourceUploadView.addResourceUploadViewListener(new ResourceUploadViewListener() {
			@Override
			public void onUploadStarted() {
				propertyPanel.finishEditing();
				controllableView = ControllableView.getParentControllableView(propertyPanel);
				if (controllableView != null)
					controllableView.disableComponents();
				else
					propertyPanel.mask(LocalizedText.INSTANCE.uploading());
			}
			
			@Override
			public void onUploadFinished(List<Resource> resources) {
				if (controllableView != null)
					controllableView.enableComponents();
				else
					propertyPanel.unmask();
				if (resources.isEmpty())
					return;
				
				propertyPanel.completeEditing();
				
				Property property = modelPathElement.getProperty();
				if (property.getType().isEntity()) {
					Resource resource = resources.get(0);
					property.set(modelPathElement.getEntity(), resource);
				} else {
					Collection<Resource> collection = modelPathElement.getValue();
					collection.addAll(resources);
				}
			}
			
			@Override
			public void onUploadCanceled() {
				if (controllableView != null)
					controllableView.enableComponents();
				else
					propertyPanel.unmask();
			}
		});
	}

	@Override
	public boolean isAvailable(ModelPathElement modelPath) {
		return isAvailable(modelPath, false);
	}
	
	@Override
	public boolean isAvailableInline(ModelPathElement modelPath) {
		return isAvailable(modelPath, true);
	}
	
	@Override
	public Supplier<Widget> getWidgetSupplier() {
		if (widgetSupplier != null)
			return widgetSupplier;

		widgetSupplier = resourceUploadView::getWidget;
		return widgetSupplier;
	}
	
	@Override
	public void configurePropertyPanel(PropertyPanel propertyPanel) {
		this.propertyPanel = propertyPanel;
		
		//if (resourceUploadView instanceof GmSessionHandler)
			//((GmSessionHandler) resourceUploadView).configureGmSession(propertyPanel.getGmSession());
	}

	@Override
	public void prepareInlineElement(Element element) {
		createDropTarget(element, this);
	}
	
	private boolean isAvailable(ModelPathElement modelPath, boolean inline) {
		if (!modelPath.isPropertyRelated())
			return false;
		
		modelPathElement = (PropertyRelatedModelPathElement) modelPath;
		
		Object value = modelPath.getValue();
		GenericModelType modelType = modelPath.getType();
		if (!inline) {
			if (value != null && modelType.isEntity())
				return false;
		} else if (value == null && modelType.isEntity())
			return false;
		
		if (inline && modelType.isCollection() && (value instanceof Collection ? ((Collection<?>) value).isEmpty() : ((Map<?, ?>) value).isEmpty()))
			return false;
		
		EntityType<?> entityType = (EntityType<?>) (modelType.isEntity() ? modelType : ((CollectionType) modelType).getCollectionElementType());
		ModelMdResolver modelMdResolver = propertyPanel.getGmSession().getModelAccessory().getMetaData();
		EntityMdResolver entityMdResolver = modelMdResolver.entityType(entityType);
		GenericEntity parentEntity = modelPathElement.getEntity();
		
		PropertyMdResolver propertyMdResolver = modelMdResolver.lenient(propertyPanel.isLenient()).entity(parentEntity)
				.property(modelPathElement.getProperty());
		boolean available = GMEMetadataUtil.isPropertyEditable(propertyMdResolver, parentEntity) && entityMdResolver.is(Instantiable.T);
		
		if (available) {
			if (modelType.isEntity())
				resourceUploadView.setMaxAmountOfFiles(1);
			else
				resourceUploadView.setMaxAmountOfFiles(ActionUtil.getMaxEntriesToAdd(modelPathElement, propertyMdResolver));
		}
		
		return available;
	}
	
	private native void createDropTarget(Element element, ResourcesExtendedInlineField field) /*-{
		element.addEventListener("dragover", function(event) {
			event.preventDefault();
			event.dropEffect = "copy";
			field.@com.braintribe.gwt.gme.propertypanel.client.field.ResourcesExtendedInlineField::addDropLayoutToTarget(Lcom/google/gwt/dom/client/Element;)(element);
		}, false);
		
		element.addEventListener("dragenter", function(event) {
			event.preventDefault();
			event.dropEffect = "copy";
			field.@com.braintribe.gwt.gme.propertypanel.client.field.ResourcesExtendedInlineField::addDropLayoutToTarget(Lcom/google/gwt/dom/client/Element;)(element);
		}, false);
	    
		element.addEventListener("drop", function(event) {
			event.preventDefault();
			var files = event.dataTransfer.files;
			field.@com.braintribe.gwt.gme.propertypanel.client.field.ResourcesExtendedInlineField::uploadFileList(Lcom/braintribe/gwt/fileapi/client/FileList;)(files);
		}, false);
		
		element.addEventListener("dragleave", function(event) {
			event.preventDefault();
			field.@com.braintribe.gwt.gme.propertypanel.client.field.ResourcesExtendedInlineField::removeDropLayoutFromTarget(Lcom/google/gwt/dom/client/Element;)(element);
		}, false);
	}-*/;
	
	private void uploadFileList(FileList fileList) {
		resourceUploadView.uploadFileList(fileList);
	}
	
	private void addDropLayoutToTarget(Element element) {
		element.addClassName(PropertyPanelResources.INSTANCE.css().entered());
		
		String innerHtml = element.getInnerHTML();
		if (!LocalizedText.INSTANCE.dropFile().equals(innerHtml)) {
			int height = element.getClientHeight() - (element.getOffsetHeight() - element.getClientHeight());
			element.setInnerHTML(LocalizedText.INSTANCE.dropFile());
			element.getStyle().setHeight(height, Unit.PX);
			oldInnerHtml = innerHtml;
		}
	}
	
	private void removeDropLayoutFromTarget(Element element) {
		element.removeClassName(PropertyPanelResources.INSTANCE.css().entered());
		element.setInnerHTML(oldInnerHtml);
		element.getStyle().clearHeight();
		oldInnerHtml = null;
	}

}
